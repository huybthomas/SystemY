package be.uantwerpen.systemY.server;

import be.uantwerpen.systemY.debugServer.DebugManager;

import java.rmi.RemoteException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Main class to implement the initialization of the nameserver in the SystemY project.
 */
public class ServerMain
{
	private static Server server;
	private static String networkIP;
	private static int rmiPort;
	private static int tcpPort;
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
		rmiPort = 1099;
		tcpPort = 1304;
		multicastIP = "224.13.4.94";
		multicastPort = 2453;
		
		//Read arguments
		argsCommand(args);
		
		server = new Server(true, networkIP, tcpPort, rmiPort, multicastIP, multicastPort);
		
		//To run the tests, asserts needs to be enabled: runconfig -> arguments -> VM arguments, add the argument '-ea'.
		if(ManagementFactory.getRuntimeMXBean().getInputArguments().contains("-ea"))
		{
			runDebug();
		}
	}
	
	/**
	 * Makes it possible to add options as an argument when starting the server.
	 * @param args	The options added in the run configurations.
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
						rmiPort = Integer.parseInt(args[i + 1].split(":", 2)[1].trim());
						i++;
					}
					else
					{
						System.out.println("Invalid arguments for -ip");
					}
					break;
				case "-tcp":
					tcpPort = Integer.parseInt(args[i+1].trim());
					i++;
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
				case "-help":
					System.out.println("SystemY Server application - 2014");
					System.out.println("Starts the server for connecting to SystemY. SystemY is a distributed file system for local networks.");
					System.out.println("\nOptions: ");
					System.out.println("\t-ip {ip}:{port}\t\tThe given ip will be used for all communication from the server to the network.\n\t\t\t\tThe ip must be the ip of the physical interface connected to the local network with SystemY.");
					System.out.println("\t-tcpPort {port}\tThis feature is for future purposes.");
					System.out.println("\t-multicast {ip}:{port}\tThis feature is for future purposes.");
					System.exit(0);
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
