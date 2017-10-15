package org.klausoncloud.viruswar.resources;

import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.klausoncloud.viruswar.model.PlayerTester;
import org.klausoncloud.viruswar.model.Umpire;
import org.klausoncloud.viruswar.actor.Actor;
import org.klausoncloud.viruswar.actor.BuiltActorMoveFire;
import org.klausoncloud.viruswar.actor.BuiltInActorFire;
import org.klausoncloud.viruswar.actor.BuiltInActorSpawn;
import org.klausoncloud.viruswar.actor.ExternalActorDocker;
import org.klausoncloud.viruswar.actor.ExternalActorWeb;

@Path("/")
public class GameVirusWar {


	@GET
	@Path("start")
	@Produces( "application/json;charset=utf-8" )
	public Response startGame( 
			@DefaultValue("0") @QueryParam("playerOne") int playerOne, 
			@DefaultValue("0") @QueryParam("playerTwo") int playerTwo, 
			@DefaultValue("0") @QueryParam("playerThree") int playerThree, 
			@DefaultValue("0") @QueryParam("playerFour") int playerFour
			) {
		ArrayList<Actor> viruses = new ArrayList<Actor>();
		viruses.add(newVirusFor(playerOne));
		viruses.add(newVirusFor(playerTwo));
		viruses.add(newVirusFor(playerThree));
		viruses.add(newVirusFor(playerFour));
		
    	Umpire umpire = new Umpire(40, 20, viruses);
		return Response.status(200).entity(umpire.runGame().toJsonString()).build();
	}

	private Actor newVirusFor(int playerType) {
		switch (playerType) {
		case 1:
			return new BuiltActorMoveFire();
		case 2:
			return new BuiltInActorSpawn();
		default:
			return new BuiltInActorFire();
		}
	}
	
	@POST
	@Path("start")
	@Produces( "application/json;charset=utf-8" )
	@Consumes("application/json")
	public Response startGame(ArrayList<PlayerResource> players) {
		ArrayList<Actor> viruses = new ArrayList<Actor>(players.size());
		try {
			for (PlayerResource player : players) {
				
				System.out.println(
						"Player id: " + player.getId() + " type: " + player.getType() + " data: " + player.getData());
				
				if (player.getType().equals("url")) {
					ExternalActorWeb virus = new ExternalActorWeb(player.getData());
					viruses.add(virus);
				} else if (player.getType().equals("code")) {
					viruses.add(new ExternalActorDocker(player.getData()));
				} else {
					viruses.add(newVirusFor(Integer.parseInt(player.getData())));
				}
			}

			Umpire umpire = new Umpire(40, 20, viruses);
			return Response.status(200).entity(umpire.runGame().toJsonString()).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(e.toString()).build();
		}
	}
	

	@POST
	@Path("testPlayer")
	@Produces( "application/json;charset=utf-8" )
	@Consumes("application/json")
	public Response testPlayer(PlayerResource player) {
			
		System.out.println("Player id: " + player.getId()
				+ " type: " + player.getType()
				+ " data: " + player.getData());
	
		Actor virus;
		try {
			if (player.getType().equals("url")) {
				virus = new ExternalActorWeb(player.getData());
			} else if (player.getType().equals("code")) {
				virus = new ExternalActorDocker(player.getData());
			} else {
				virus = newVirusFor(Integer.parseInt(player.getData()));
			}

			PlayerTester tester = new PlayerTester(virus);
			String result = tester.testVirus().toJsonString();
			System.out.println(result);
			return Response.status(200).entity(result).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(e.toString()).build();
		}
	}
}
