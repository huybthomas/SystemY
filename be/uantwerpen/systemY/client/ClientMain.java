package be.uantwerpen.systemY.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Random;

import be.uantwerpen.systemY.GUI.ClientGUI;

/**
 * Main class to implement the initialization of a client in the SystemY project.
 */
public class ClientMain
{
	private static boolean enableGUI;
	private static boolean enableTerminal;
	private static String hostname;
	private static String networkIP;
	private static int tcpReceivePort;
	private static int tcpSendPort;
	private static int rmiPort;
	private static String multicastIP;
	private static int multicastPort;
	
	/**
	 * Starts a client.
	 * @param args	The input arguments
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException
	{
		//Default port settings
		try
		{
			networkIP = InetAddress.getLocalHost().getHostAddress();
			hostname = "Node" + networkIP.split("\\.", 4)[3] + "-" + Math.abs(new Random().nextInt());
		}
		catch(UnknownHostException e)
		{
			networkIP = "localhost";
			hostname = "Node" + networkIP + "-" + Math.abs(new Random().nextInt());
		}
		tcpReceivePort = 1304;
		tcpSendPort = 1305;
		rmiPort = 1099;
		multicastIP = "224.13.4.94";
		multicastPort = 2453;
		
		//Default settings
		enableGUI = true;
		enableTerminal = false;
		
		//Read arguments
		argsCommand(args);
		
		Client c = new Client(enableTerminal, hostname, networkIP, tcpReceivePort, tcpSendPort, rmiPort, multicastIP, multicastPort);
		
		//Activate GUI
		if(enableGUI)
		{
			new ClientGUI(c);
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
				case "-nogui":
					enableGUI = false;
					enableTerminal = true;
					break;
				case "-terminal":
					enableTerminal = true;
					break;
				case "-hostname":
					hostname = args[i + 1].trim();
					i++;
					break;
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
				case "-tcpreceive":
					tcpReceivePort = Integer.parseInt(args[i+1].trim());
					i++;
					break;
				case "-tcpsend":
					tcpSendPort = Integer.parseInt(args[i+1].trim());
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
					System.out.println("SystemY Client application - 2014");
					System.out.println("Starts the client for connecting to SystemY. SystemY is a distributed file system for local networks.");
					System.out.println("\nOptions: ");
					System.out.println("\t-noGUI\t\t\tStart the client without a the GUI interface.");
					System.out.println("\t-terminal\t\tStart a CLI interface to run commands from the terminal.");
					System.out.println("\t-hostname {name}\tThe given parameter {name} will be used to indentify the client in the network.\n\t\t\t\tThe hostname must be unique in the system.");
					System.out.println("\t-ip {ip}:{port}\t\tThe given ip will be used for all communication from the client to the network.\n\t\t\t\tThe ip must be the ip of the physical interface connected to the local network with SystemY.");
					System.out.println("\t-tcpReceive {port}\tThe given port will be used for receiving file request.");
					System.out.println("\t-tcpSend {port}\t\tThe given port will be used for sending file request.");
					System.out.println("\t-multicast {ip}:{port}\tThe given ip will be used to indentifing new clients in the network.\n\t\t\t\tThe ip must be in the legal range of multicast addresses.");
					System.exit(0);
				default:
					System.out.println("Unkown option '" + args[i] + "'");
					break;
			}
		}
	}
}
