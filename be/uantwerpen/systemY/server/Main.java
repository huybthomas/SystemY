package be.uantwerpen.systemY.server;

import be.uantwerpen.systemY.debugServer.DebugManager;

import java.rmi.RemoteException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFileChooser;

/**
 * Main class to implement the initialization of the nameserver in the SystemY project.
 */
public class Main
{
	private static Server server;
	private static String networkIP;
	private static int networkPort;
	private static String multicastIP;
	private static int multicastPort;
	
	/**
	 * Starts the server.
	 * @param args	The input arguments
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException
	{
		//Default port settings
		try
		{
			networkIP = InetAddress.getLocalHost().getHostAddress();
		}
		catch(UnknownHostException e)
		{
			networkIP = "localhost";
		}
		networkPort = 1099;
		multicastIP = "224.13.4.94";
		multicastPort = 2453;
		
		//Read arguments
		argsCommand(args);
		
		server = new Server(true, networkIP, networkPort, multicastIP, multicastPort);
		
		//To run the tests, asserts needs to be enabled: runconfig -> arguments -> VM arguments, add the argument '-ea'.
		if(ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-ea"))
		{
			runDebug();
		}
	}
	
	/**
	 * Makes it possible to add options as an argument when starting the server.
	 * @param args
	 */
	private static void argsCommand(String[] args)
	{
		for(int i = 0; i < args.length; i++)
		{
			switch(args[i].toLowerCase())
			{
				case "-ip":
					if(args[i + 1].trim().split(":").length == 2)
					{
						networkIP = args[i + 1].split(":", 2)[0].trim();
						networkPort = Integer.parseInt(args[i + 1].split(":", 2)[1].trim());
						i++;
					}
					else
					{
						System.out.println("Invalid arguments for -ip");
					}
					break;
				case "-multicast":
					if(args[i + 1].trim().split(":").length == 2)
					{
						multicastIP = args[i + 1].trim().split(":", 2)[0];
						multicastPort = Integer.parseInt(args[i + 1].trim().split(":", 2)[1]);
						i++;
					}
					else
					{
						System.out.println("Invalid arguments for -multicast");
					}
					break;
				default:
					System.out.println("Unkown option '" + args[i] + "'");
					break;
			}
		}
	}
	
	/**
	 * Runs the debugManager.
	 */
	private static void runDebug()
	{
		new DebugManager(server);
	}
}
