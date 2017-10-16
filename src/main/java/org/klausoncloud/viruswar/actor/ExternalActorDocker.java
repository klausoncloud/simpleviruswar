package org.klausoncloud.viruswar.actor;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.klausoncloud.viruswar.model.Logger;
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
import com.spotify.docker.client.LogMessage;
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
		
		Logger.logMessage(this.getClass(), "contructor", Logger.INFO, "Creating container with code: " + code);
		
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
				Logger.logMessage(this.getClass(), "contructor", Logger.INFO, "Container reports it is running.");
				
				dockerActor = new ExternalActorWebCode(ACTOR_URL + containerPort);
				if (!actorIsResponding()) {
					throw new Exception("Cannot reach the actor inside the container.");
				}
				
				// Load the code into the container
				dockerActor.setCode(code);
			} else {
				// Darn. Need to abort.
				throw new Exception("Container did not start. " + info.state().toString());
			}
			
			// Checking if the actor logged errors
			checkContainerLogsAndForwardErrors("constructor", Logger.ERROR);
			
		} catch (DockerException | InterruptedException e) {
			// TODO Auto-generated catch block
			Logger.logException(this.getClass(), "contructor", Logger.ERROR, e);
			throw e;
		}
	}
	
	static final String ERROR_PREFIX = "ERROR";
	
	private boolean checkContainerLogsAndForwardErrors(String method, int severity) throws Exception {
		boolean errorsFound = false;
		try {
			LogStream stream = docker.logs(containerId, LogsParam.stdout(), LogsParam.stderr());
			String logs = stream.readFully();
			String [] logLines = logs.split("\n");
			for (String line : logLines) {
				if (line.startsWith(ERROR_PREFIX)) {
					errorsFound = true;
					Logger.logMessage(this.getClass(), method + "/" + "checkContainerLogsAndForwardErrors", severity, line);
				}
			}
		} catch (Exception e) {
			Logger.logException(this.getClass(), "checkContainerLogsAndForwardErrors", Logger.ERROR, e);
			throw e;
		}
		
		return errorsFound;
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
		} catch (RetryException | ExecutionException e) {
			Logger.logException(this.getClass(), "generateContainerNameAndPort", Logger.INFO, e);
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
			
			// Docker puts a / in front of the container name. So:
			// Our generated container names have the pattern of </><name>-<port> and should only have one name.
			final int NAME_IDX = 1;
			final int PORT_IDX = 2;
			final int EXPECTED_PARTS = 3;
			String[] nameParts = container.names().get(0).split(CONTAINER_NAME_DELIMITER_REGEX);
			
			if (nameParts.length != EXPECTED_PARTS || !nameParts[NAME_IDX].equals(CONTAINER_BASE_NAME)) {
				continue;
			}
			try {
			    int containerNumber = Integer.parseInt(nameParts[PORT_IDX]);
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
				Logger.logException(this.getClass(), "destroyContainerAndClose", Logger.ERROR, e);
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
		} catch (RetryException | ExecutionException e) {
			Logger.logException(this.getClass(), "actorIsResponding", Logger.INFO, e);
		    return false;
		}
		return true;
	}

	@Override
	public Move startGame(int width, int height, int virusNumber, int id) {
		// Accept an exception if constructor failed
		Move result =  dockerActor.startGame(width, height, virusNumber, id);
		
		try {
			checkContainerLogsAndForwardErrors("startGame", Logger.WARNING);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.logException(this.getClass(), "startGame", Logger.WARNING, e);
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
