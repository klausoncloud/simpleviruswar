package org.klausoncloud.viruswar.actor;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.klausoncloud.viruswar.model.Move;
import org.klausoncloud.viruswar.model.MoveNotification;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.base.Predicates;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;



public class ExternalActorDocker implements Actor {
	
	final static String CONTAINER_ACTOR_PORT = "80";
	final static String CONTAINER_ACTOR_IP = "0.0.0.0";
	
	final static String CONTAINER_IMAGE_NAME = "playerimage";
	
	final static String ACTOR_URL = "http://localhost:"; // Port needs to be dynamic
	
	private DockerClient docker = null;
	private String containerId = null;
	private ExternalActorWebCode dockerActor = null;
	
	// Docker puts the / in front of the name.
	// Expected pattern is /<name>-<port>
	final static String CONTAINER_NAME_DELIMITER = "-";
	final static String CONTAINER_NAME_DELIMITER_REGEX = "/|" + CONTAINER_NAME_DELIMITER;
	final static String CONTAINER_BASE_NAME = "playercontainer";
	final static int CONTAINER_BASE_PORT = 5000;
	
	public ExternalActorDocker(String code) throws Exception {
		
		System.out.println("Creating container with code: " + code);
		// Build the container
		docker = DefaultDockerClient.builder()
			    .uri(URI.create("http://localhost:2375"))
			    .build();
		
		// create a unique container name
		ContainerNamePort containerNamePort = generateContainerNameAndPort();
		if (containerNamePort == null) {
			throw new Exception("Cannot generate a unique container name.");
		}
		String containerName = containerNamePort.name;
		String containerPort = containerNamePort.port;
		
		// Bind container port 80 to host port 5000	
		final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
		List<PortBinding> hostPorts = new ArrayList<>();
	    hostPorts.add(PortBinding.of(CONTAINER_ACTOR_IP, containerPort));
	    portBindings.put(CONTAINER_ACTOR_PORT, hostPorts);

		final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

		// Create container with exposed ports
		final String[] ports = {CONTAINER_ACTOR_PORT};
		final ContainerConfig containerConfig = ContainerConfig.builder()
		    .hostConfig(hostConfig)
		    .image(CONTAINER_IMAGE_NAME)
		    .exposedPorts(ports)
		    .build();

		try {
			// Start container
			final ContainerCreation creation = docker.createContainer(containerConfig, containerName);
			containerId = creation.id();
			docker.startContainer(containerId);
			
			// Inspect container
			final ContainerInfo info = docker.inspectContainer(containerId);
			if (info.state().running()) {
				System.out.println("Container reports it is running.");
				
				dockerActor = new ExternalActorWebCode(ACTOR_URL + containerPort);
				if (!actorIsResponding()) {
					throw new Exception("Cannot reach the actor inside the container.");
				}
				
				// Load the code into the container
				System.out.println("Sending code.");
				dockerActor.setCode(code);
				System.out.println("Code set.");
			} else {
				// Darn. Need to abort.
				throw new Exception("Container did not start. " + info.state().toString());
			}
			
			final String logs;
			try (LogStream stream = docker.logs(containerId, LogsParam.stdout(), LogsParam.stderr())) {
			  logs = stream.readFully();
			  System.out.println("Container logs >>>>");
			  System.out.println(logs);
			  System.out.println("<<<<");
			}
			
		} catch (DockerException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
	
	final static int FIND_CONTAINER_NAME_MAX_ATTEMPTS = 3;
	
	private ContainerNamePort generateContainerNameAndPort() {
		Callable<ContainerNamePort> callable = new Callable<ContainerNamePort>() {
		    public ContainerNamePort call() throws Exception {
		    	return _generateContainerNameAndPort();
		    }
		};

		Retryer<ContainerNamePort> retryer = RetryerBuilder.<ContainerNamePort>newBuilder()
		        .retryIfResult(Predicates.isNull())
		        .retryIfExceptionOfType(Exception.class)
		        .retryIfRuntimeException()
		        .withStopStrategy(StopStrategies.stopAfterAttempt(FIND_CONTAINER_NAME_MAX_ATTEMPTS))
		        .build();
		try {
		    return retryer.call(callable);
		} catch (RetryException e) {
		    e.printStackTrace();
		    return null;
		} catch (ExecutionException e) {
		    e.printStackTrace();
		    return null;
		}
	}

	// ToDo: Retry this n times.
	private ContainerNamePort _generateContainerNameAndPort() throws DockerException, InterruptedException {
		final List<Container> containers = docker.listContainers(ListContainersParam.allContainers());
	    // Filter for containers created by this app using container name
		// Find the latest container generated
		// Add 1 to the name and return this new name
		int highNumber = CONTAINER_BASE_PORT;
		for (Container container : containers) {
			if (container.names().isEmpty() || !container.image().equals(CONTAINER_IMAGE_NAME)) {
				continue;
			}
			
			// Docker puts the / in front of the name.
			// Expected pattern is </><name>-<port>
			String[] nameParts = container.names().get(0).split(CONTAINER_NAME_DELIMITER_REGEX);
			
			if (!nameParts[1].equals(CONTAINER_BASE_NAME) || nameParts.length < 1) {
				continue;
			}
			try {
			    int containerNumber = Integer.parseInt(nameParts[2]);
			    if (containerNumber > highNumber) {
			    	highNumber = containerNumber;
			    }
			} catch (NumberFormatException e) {
				continue;
			}
		}
		return new ContainerNamePort(CONTAINER_BASE_NAME, CONTAINER_NAME_DELIMITER, highNumber + 1);
	}
	
	private class ContainerNamePort {
		String name;
		String port;
		
		ContainerNamePort(String name, String delimiter, int port) {
			this.name = name + delimiter + port;
			this.port = Integer.toString(port);
		}
	}
	
	protected void finalize() {
		try {
			destroyContainerAndClose();
		} catch (Exception e) {
			// NOP - Stack trace get's logged in the inner method.
		}
	}
	
	private void destroyContainerAndClose() {
		if (docker != null) {
			try {
				if (containerId != null) {
					final ContainerInfo info = docker.inspectContainer(containerId);
					if (info.state().running()) {
						// Kill container
						docker.killContainer(containerId);
					}
					// Remove container
					docker.removeContainer(containerId);
					containerId = null;
				}
			} catch (DockerException | InterruptedException e) {
				e.printStackTrace();
			} finally {
				// Close the docker client
				docker.close();
				docker = null;
			}
		}
	}
	
	private boolean actorIsResponding() {
		Callable<Boolean> callable = new Callable<Boolean>() {
		    public Boolean call() throws Exception {
		    	return dockerActor.isResponding();
		    }
		};

		Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
		        .retryIfResult(Predicates.alwaysFalse())
		        .retryIfExceptionOfType(Exception.class)
		        .retryIfRuntimeException()
		        .withStopStrategy(StopStrategies.stopAfterDelay(3, TimeUnit.SECONDS))
		        .build();
		try {
		    retryer.call(callable);
		} catch (RetryException e) {
		    e.printStackTrace();
		    return false;
		} catch (ExecutionException e) {
		    e.printStackTrace();
		    return false;
		}
		return true;
	}

	@Override
	public Move startGame(int width, int height, int virusNumber, int id) {
		// Accept an exception if constructor failed
		Move result =  dockerActor.startGame(width, height, virusNumber, id);
		
		String logs;
		try (LogStream stream = docker.logs(containerId, LogsParam.stdout(), LogsParam.stderr())) {
			  logs = stream.readFully();
			  System.out.println("Container logs >>>>");
			  System.out.println(logs);
			  System.out.println("<<<<");
			} catch (DockerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return result;
	}

	@Override
	public Move nextMove() {
		// Accept an exception if constructor failed
		return dockerActor.nextMove();
	}

	@Override
	public void moveNotification(List<MoveNotification> moveList) {
		// Accept an exception if constructor failed
        dockerActor.moveNotification(moveList);
	}

	@Override
	public void endOfGame(List<Integer> winnerIdList) {
		destroyContainerAndClose();
	}

}
