package be.uantwerpen.systemY.client;

import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.shared.Node;

public class ShutdownManager
{
	private Client client;
	
	public ShutdownManager(Client client)
	{
		this.client = client;
	}
	
	public boolean shutdown()
	{
		client.stopServices();
		
		Node nextNode = client.getNextNode();
		Node prevNode = client.getPrevNode();

		String bindLocationNext = "//" + nextNode.getIpAddress() + "/NodeLinkManager_" + nextNode.getHostname();
		String bindLocationPrev = "//" + prevNode.getIpAddress() + "/NodeLinkManager_" + prevNode.getHostname();
		
		boolean skipStep;
		
		// Step1 : set prevNode on nextNode as my prevNode
		boolean step1 = false; 
		skipStep = false;
		while(!(skipStep || step1))
		{
			try 
			{
				if(!nextNode.equals(new Node(client.getHostname(), client.getIP())))	//If next node is himself, no need to change the nextNode
				{
					NodeLinkManagerInterface iFace = (NodeLinkManagerInterface)client.getRMIInterface(bindLocationNext);
					iFace.setPrev(prevNode);
				}
				step1 = true;
			} 
			catch(Exception e)
			{
				System.err.println("Step1 in shutdown fail: "+ e.getMessage());
				if(this.client.nodeConnectionFailure(nextNode.getHostname()))
				{
					nextNode = client.getNextNode();
					bindLocationNext = "//" + nextNode.getIpAddress() + "/NodeLinkManager_" + nextNode.getHostname();
				}
				else
				{
					skipStep = true;
				}
			}
		}
		
		// Step2: set nextNode on prevNode as my nextNode
		boolean step2 = false;  
		skipStep = false;
		while(!(skipStep || step2))
		{
			try 
			{
				if(!prevNode.equals(new Node(client.getHostname(), client.getIP())))	//If previous node is himself, no need to change the prevNode
				{
					NodeLinkManagerInterface iFace = (NodeLinkManagerInterface)client.getRMIInterface(bindLocationPrev);
					iFace.setNext(nextNode);
				}
				step2 = true;
			} 
			catch(Exception e)
			{
				System.err.println("Step2 in shutdown fail: "+ e.getMessage());
				if(this.client.nodeConnectionFailure(prevNode.getHostname()))
				{
					prevNode = client.getPrevNode();
					bindLocationPrev = "//" + prevNode.getIpAddress() + "/NodeLinkManager_" + prevNode.getHostname();
				}
				else
				{
					skipStep = true;
				}
			}
		}
		
		// Step3 : delete my node on NameServer
		boolean step3 = deleteMyNode();
		
		return(step1 && step2 && step3);
	}
	
	private boolean deleteMyNode()
	{
		String bindLocation = "//" + client.getServerIP() + "/NodeServer";		
		try 
		{
			NodeManagerInterface iFace = (NodeManagerInterface)client.getRMIInterface(bindLocation);
			return iFace.delNode(client.getHostname());
		} 
		catch(Exception e)
		{
			System.err.println("NodeServer exception: "+ e.getMessage());	//No connection with the server could be established, drop serverConnectionFailure (services already down)
		}
		return false;
	}
}
