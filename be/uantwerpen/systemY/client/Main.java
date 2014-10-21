package be.uantwerpen.systemY.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Random;

import be.uantwerpen.systemY.GUI.ClientGUI;

public class Main
{
	private static boolean enableGUI;
	private static boolean enableTerminal;
	private static String hostname;
	private static String networkIP;
	private static int networkPort;
	private static String multicastIP;
	private static int multicastPort;
	
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
		networkPort = 1099;
		multicastIP = "228.1.2.3";
		multicastPort = 2453;
		
		//Default settings
		enableGUI = true;
		enableTerminal = false;
		
		//Read arguments
		argsCommand(args);
		
		Client c = new Client(enableTerminal, hostname, networkIP, networkPort, multicastIP, multicastPort);
		
		//Activate GUI
		if(enableGUI)
		{
			new ClientGUI(c);
		}
	}
	
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
}
