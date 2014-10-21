package be.uantwerpen.systemY.server;

import java.net.DatagramPacket;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;

import be.uantwerpen.systemY.interfaces.BootstrapManagerInterface;
import be.uantwerpen.systemY.networkservices.MulticastObserver;

public class DiscoveryManager
{
	private Server server;
	
	/**
	 * Receives packets from the multicast.
	 * @param observer	observer from the multicast server
	 * @param server	the server
	 */
	public DiscoveryManager(MulticastObserver observer, Server server)
	{
		this.server = server;
		
		observer.addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				multicastReceived((DatagramPacket)object);
			}
		});
	}
	
	/**
	 * Reads received packets from the multicast.
	 * @param datagram message that is received from observer of multicast
	 */
	private void multicastReceived(DatagramPacket datagram)
	{
		String message = new String(datagram.getData());
		
		if(message.trim().split(" ").length == 2)
		{
			String clientname = message.trim().split(" ", 2)[0];
			String ipAddress = message.trim().split(" ", 2)[1];
			
			if(server.addNode(clientname, ipAddress))
			{
				if(sendNetworkInfo(clientname, ipAddress))
				{
					server.printTerminalInfo("New node connected. Host: " + clientname + " - ip: " + ipAddress);
				}
			}
			else
			{
				server.printTerminalError("New node: " + clientname + " already exists!");
			}
		}
	}
	
	/**
	 * Sends an answer to the new node.
	 * @param ip 			the ip from the node you want to reach
	 * @param clientname 	the name from the node you are trying to reach
	 * @return boolean True if successful, false if failed
	 */
	private boolean sendNetworkInfo(String clientname, String ip)
	{
		String bindLocation = "//" + ip + "/Bootstrap_" + clientname;
		
		BootstrapManagerInterface bInterface = (BootstrapManagerInterface)server.getRMIInterface(bindLocation);
		
		if(bInterface != null)
		{
			try
			{
				bInterface.setNetwork(server.getServerIP(), (server.getNetworkSize() - 1));
			}
			catch(RemoteException e)
			{
				System.err.println("RMI message to: " + ip + " failed!");
				server.nodeConnectionFailure(clientname);
				return false;
			}
			return true;
		}
		return false;
	}
}
