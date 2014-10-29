package be.uantwerpen.systemY.client;

import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.shared.Node;

/**
 * Class that manages the failure of a connection
 */
public class FailureManager
{
	private Client client;
	
	/**
	 * Creates the FailureManager object of a given Client
	 * @param Client	the client 
	 */
	public FailureManager(Client client)
	{
		this.client = client;
	}
	
	/**
	 * Handles the failure of a client connection, returns true if the failure is handled correctly
	 * @param String	hostname of a failed client
	 * @return boolean	True if connection failure handled correctly, false if not.
	 */
	public boolean nodeConnectionFailure(String hostname)
	{
		client.printTerminalError("Lost connection to node: " + hostname);
		
		String bindLocation = "//" + client.getServerIP() + "/NodeServer";
		Node prevNode = null;
		Node nextNode = null;
		
		try 
		{
			NodeManagerInterface iFace = (NodeManagerInterface)client.getRMIInterface(bindLocation);
			
			prevNode = iFace.getPrevNode(hostname);
			nextNode = iFace.getNextNode(hostname);
		} 
		catch(Exception e)
		{
			System.err.println("NodeServer exception: "+ e.getMessage());
			serverConnectionFailure();
			return false;
		}
		
		if(prevNode != null)
		{
			updateNode(prevNode, null, nextNode);
		}
		if(nextNode != null)
		{
			updateNode(nextNode, prevNode, null);
		}
		
		try
		{
			NodeManagerInterface iFace = (NodeManagerInterface)client.getRMIInterface(bindLocation);
			
			iFace.delNode(hostname);
		}
		catch(Exception e)
		{
			System.err.println("NodeServer exception: "+ e.getMessage());
			serverConnectionFailure();
			return false;
		}
		return true;
	}
	
	/**
	 * Stops the services of a client because the server failed.
	 */
	public void serverConnectionFailure()
	{
		client.printTerminalError("Lost connection to server.");
		
		client.stopServices();
		client.setSessionState(false);
	}
	
	/**
	 * Updates a given node with a new previous node and new next node.
	 * @param Node updateNode
	 * @param Node	prevNode
	 * @param Node	nextNode
	 */
	private void updateNode(Node updateNode, Node prevNode, Node nextNode)
	{
		String bindLocation = "//" + updateNode.getIpAddress() + "/NodeLinkManager_" + updateNode.getHostname();
		
		if(updateNode.equals(new Node(client.getHostname(), client.getIP())))	//Node to update is own node
		{
			if(prevNode != null)
			{
				client.setPrevNode(prevNode);
			}
			
			if(nextNode != null)
			{
				client.setNextNode(nextNode);
			}
		}
		else
		{
			try
			{
				NodeLinkManagerInterface iFace = (NodeLinkManagerInterface)client.getRMIInterface(bindLocation);
				
				if(prevNode != null)
				{
					iFace.setPrev(prevNode);
				}
				
				if(nextNode != null)
				{
					iFace.setNext(nextNode);
				}
			}
			catch(Exception e)
			{
				System.err.println("NodeLinkManager exception: " + e.getMessage());
				nodeConnectionFailure(updateNode.getHostname());
			}
		}
	}
}