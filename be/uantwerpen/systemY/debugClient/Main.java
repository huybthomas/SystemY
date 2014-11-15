package be.uantwerpen.systemY.debugClient;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import be.uantwerpen.systemY.client.Client;
import be.uantwerpen.systemY.server.*;

/**
 * Startup class for the DebugManager of the client. This class initiates some Clients first.
 */
public class Main
{
	/**
	 * Creates 3 clients and then instantiates a DebugManager object.
	 * @param	args	not used.
	 */
	public static void main(String[] args) throws RemoteException
	{
		String networkIP;
		int rmiPort;
		int tcpPort;
		String multicastIP;
		int multicastPort;
		
		if(ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-ea"))
		{
			try
			{
				networkIP = InetAddress.getLocalHost().getHostAddress();
			}
			catch(UnknownHostException e)
			{
				networkIP = "localhost";
			}
			rmiPort = 1099;
			tcpPort = 1304;
			multicastIP = "224.13.4.94";
			multicastPort = 2453;
			
			Server server = new Server(false, networkIP, tcpPort, rmiPort, multicastIP, multicastPort);
			Client c1 = new Client(false, "Node_1", networkIP, tcpPort, rmiPort, multicastIP, multicastPort);
			Client c2 = new Client(false, "failureNode", networkIP, tcpPort, rmiPort, multicastIP, multicastPort);
			Client c3 = new Client(false, "Node_3", networkIP, tcpPort, rmiPort, multicastIP, multicastPort);
		  
			@SuppressWarnings("unused")
			DebugManager debug = new DebugManager(c1, c2, c3, server);
		}
		else
		{
			System.out.println("[INFO - DEBUGGER] Debugger can't start, enable asserts with argument '-ea' and reboot the system.");
		}
	}
}
