package be.uantwerpen.systemY.client;

import java.net.DatagramPacket;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;

import be.uantwerpen.systemY.interfaces.BootstrapManagerInterface;
import be.uantwerpen.systemY.networkservices.MulticastObserver;
import be.uantwerpen.systemY.shared.Node;

/**
 * Class that manages the discovery of a new connection
 */
public class DiscoveryManager
{
	private Client client;
	
	/**
	 * Creates a DiscoveryManager object instance with a given multicast observer and client
	 * @param MulticastObserver
	 * @param Client
	 */
	public DiscoveryManager(MulticastObserver observer, Client client)
	{
		this.client = client;
		
		observer.addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				multicastReceived((DatagramPacket)object);
			}
		});
	}
	
	/**
	 * Receive a multicast datagram packet.
	 * @param DatagramPacket 	the multicast packet
	 */
	private void multicastReceived(DatagramPacket datagram)
	{
		String message = new String(datagram.getData());
		
		if(message.trim().split(" ").length == 2)
		{
			String clientname = message.trim().split(" ", 2)[0];
			String ipAddress = message.trim().split(" ", 2)[1];
			
			if(!(clientname.equals(client.getHostname()) && ipAddress.equals(client.getIP())))							//Drop own discovery datagrampacket
			{
				Node oldNext = client.updateLinks(new Node(clientname, ipAddress));
				if(oldNext != null)																						//New node is the new nextNode
				{
					sendNetworkInfo(clientname, ipAddress, client.getThisNode(), oldNext);								//Send prevNode (this node) and nextNode (oldNext) to the new node
				}
			}
		}
	}
	
	/**
	 * Send info to the new node who just joined the network right next to this client node
	 * @param String	clientname
	 * @param String	ip
	 * @param Node	prevNode
	 * @param Node	nextNode
	 * @return boolean	True if success, false if not
	 */
	private boolean sendNetworkInfo(String clientname, String ip, Node prevNode, Node nextNode)
	{		
		BootstrapManagerInterface bInterface = (BootstrapManagerInterface)client.getBootstrapInterface(new Node(clientname, ip));
		
		if(bInterface != null)
		{
			try
			{
				bInterface.setLinkedNodes(prevNode, nextNode);
			}
			catch(NullPointerException | RemoteException e)
			{
				System.err.println("RMI message to: " + ip + " failed!");
				this.client.nodeConnectionFailure(clientname);
				return false;
			}
			return true;
		}
		return false;
	}
}
