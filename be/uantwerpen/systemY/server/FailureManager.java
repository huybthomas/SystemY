package be.uantwerpen.systemY.server;

import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.shared.Node;

public class FailureManager
{
	private Server server;
	
	public FailureManager(Server server)
	{
		this.server = server;
	}
	
	public void nodeConnectionFailure(String hostname)
	{
		server.printTerminalError("Lost connection to node: " + hostname);
		
		Node prevNode = server.getPrevNode(hostname);
		Node nextNode = server.getNextNode(hostname);
		
		if(prevNode != null)
		{
			updateNode(prevNode, null, nextNode);
		}
		if(nextNode != null)
		{
			updateNode(nextNode, prevNode, null);
		}
		
		server.delNode(hostname);
	}
	
	private void updateNode(Node updateNode, Node prevNode, Node nextNode)
	{
		String bindLocation = "//" + updateNode.getIpAddress() + "/NodeLinkManager_" + updateNode.getHostname();
		
		try
		{
			NodeLinkManagerInterface iFace = (NodeLinkManagerInterface)server.getRMIInterface(bindLocation);
		
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
