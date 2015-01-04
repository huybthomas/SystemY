package be.uantwerpen.systemY.server;

import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.shared.Node;

/**
 * Class on the server side that handles Failures in the network.
 */
public class FailureManager
{
	private Server server;
	
	/**
	 * Creates the FailureManager object.
	 */
	public FailureManager(Server server)
	{
		this.server = server;
	}
	
	/**
	 * Deletes a node that suddenly failed to connect in the network and updates it's surrounding nodes.
	 * @param hostname	The hostname of the failed node.
	 */
	public void nodeConnectionFailure(String hostname)
	{
		server.printTerminalError("Lost connection to node: " + hostname);
		
		Node prevNode = server.getPrevNode(hostname);
		Node nextNode = server.getNextNode(hostname);
		
		server.delNode(hostname);
		
		if(!hostname.equals(prevNode.getHostname()))
		{
			if(prevNode != null)
			{
				updateNode(prevNode, null, nextNode);
			}
			if(nextNode != null)
			{
				updateNode(nextNode, prevNode, null);
			}
		}
	}
	
	/**
	 * Update a given node with a new previous node and new next node.
	 * @param updateNode 	The node that needs to be updated.
	 * @param prevNode		The new previous node.
	 * @param nextNode		The new next node.
	 */
	private void updateNode(Node updateNode, Node prevNode, Node nextNode)
	{		
		try
		{
			NodeLinkManagerInterface iFace = (NodeLinkManagerInterface)server.getNodeLinkInterface(updateNode);
		
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
