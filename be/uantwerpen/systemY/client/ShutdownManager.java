package be.uantwerpen.systemY.client;

import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.shared.Node;

/**
 * Class that implements the shutdown of a client node
 */
public class ShutdownManager
{
	private Client client;
	
	public ShutdownManager(Client client)
	{
		this.client = client;
	}
	
	/**
	 * Let the client log off the SystemY and close the connection.
	 * Log out is in 3 steps:
	 * 1. Set the prevNode on the client's nextNode as my prevNode
	 * 2. Set the nextNode on the client's prevNode as my nextNode
	 * 3. Delete client on nameserver
	 * @return boolean	True if success, false if not
	 */
	public boolean shutdown()
	{		
		Node nextNode = client.getNextNode();
		Node prevNode = client.getPrevNode();
		
		// Step1 : set prevNode on nextNode as my prevNode
		boolean step1 = false; 
		while(!step1)
		{
			try 
			{
				if(!nextNode.equals(client.getThisNode()))			//If next node is himself, no need to change the nextNode
				{
					NodeLinkManagerInterface iFace = (NodeLinkManagerInterface)client.getNodeLinkInterface(nextNode);
					iFace.setPrev(prevNode);
				}
				step1 = true;
			} 
			catch(Exception e)
			{
				System.err.println("Step 1 in shutdown fail: "+ e.getMessage());
				if(this.client.nodeConnectionFailure(nextNode.getHostname()))
				{
					nextNode = client.getNextNode();
				}
				else
				{
					return false;
				}
			}
		}
		
		// Step2: set nextNode on prevNode as my nextNode
		boolean step2 = false;  
		while(!step2)
		{
			try 
			{
				if(!prevNode.equals(client.getThisNode()))		//If previous node is himself, no need to change the prevNode
				{
					NodeLinkManagerInterface iFace = (NodeLinkManagerInterface)client.getNodeLinkInterface(prevNode);
					iFace.setNext(nextNode);
				}
				step2 = true;
			} 
			catch(Exception e)
			{
				System.err.println("Step 2 in shutdown fail: "+ e.getMessage());
				if(this.client.nodeConnectionFailure(prevNode.getHostname()))
				{
					prevNode = client.getPrevNode();
				}
				else
				{
					return false;
				}
			}
		}
		
		// Step3 : delete my node on NameServer
		boolean step3 = deleteMyNode();
		
		return(step1 && step2 && step3);
	}
	
	/**
	 * Implementation of the deletion of the client node on the nameserver
	 * @return boolean	True if success, false if not
	 */
	private boolean deleteMyNode()
	{
		if(client.getServerIP() != null)
		{	
			try 
			{
				NodeManagerInterface iFace = (NodeManagerInterface)client.getNodeServerInterface();
				return iFace.delNode(client.getHostname());
			} 
			catch(Exception e)
			{
				System.err.println("NodeServer exception: "+ e.getMessage());	//No connection with the server could be established, drop serverConnectionFailure (services already down)
			}
		}
		return false;
	}
}
