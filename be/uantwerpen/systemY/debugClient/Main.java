package be.uantwerpen.systemY.debugClient;

import java.rmi.RemoteException;

import be.uantwerpen.systemY.GUI.ClientGUI;
import be.uantwerpen.systemY.client.Client;
import be.uantwerpen.systemY.server.*;

/**
 * Startup class for the DebugManager of the client. This class initiates some Clients first.
 */
public class Main {
	
	/**
	 * Creates 3 clients and then instantiates a DebugManager object.
	 * @param	args	not used.
	 */
	public static void main(String[] args) 
	{
		try 
		{
			
			Server server = new Server(false, "localhost", 1099, "224.13.4.94", 2453);
			Client c1 = new Client(false, "Node_1", "localhost", 1099, "224.13.4.94", 2453);
			Client c2 = new Client(false, "failureNode", "localhost", 1099, "224.13.4.94", 2453);
			Client c3 = new Client(false, "Node_3", "localhost", 1099, "224.13.4.94", 2453);
		  
			@SuppressWarnings("unused")
			DebugManager debug = new DebugManager(c1, c2, c3, server);			
		} 
		catch (RemoteException e) 
		{
			System.out.println(e.getMessage());
			;
		}
	}
}
