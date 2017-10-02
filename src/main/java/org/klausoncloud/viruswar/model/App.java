package org.klausoncloud.viruswar.model;

import java.util.ArrayList;

import org.klausoncloud.viruswar.actor.Actor;
import org.klausoncloud.viruswar.actor.BuiltInActorFire;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	ArrayList<Actor> viruses = new ArrayList<Actor>();
    	viruses.add(new BuiltInActorFire());
    	viruses.add(new BuiltInActorFire());
    	viruses.add(new BuiltInActorFire());
    	viruses.add(new BuiltInActorFire());

    	Umpire umpire = new Umpire(8, 8, viruses);
        System.out.println( "{\n" );
        System.out.println( umpire.runGame() );
    	System.out.println( "}" );
    }
}
