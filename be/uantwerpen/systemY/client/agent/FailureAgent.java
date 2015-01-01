package be.uantwerpen.systemY.client.agent;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

import be.uantwerpen.systemY.interfaces.FileManagerInterface;
import be.uantwerpen.systemY.shared.HashFunction;
import be.uantwerpen.systemY.shared.Node;

public class FailureAgent implements Runnable, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private AgentManager agentM;
	private Node failedNode;
	private Node startNode;
	private AgentObserver observer;
	
	/**
	 * Create the AgentManager object.
	 * @param failedNode	The node that failed, where some files still may be pointing to as Owner.
	 * @param startNode		The node where the failure agent was first summoned upon.
	 * @param agentManager	The manager that is steering the agent over the node circle.
	 * @throws RemoteException
	 */
	public FailureAgent(Node failedNode, Node startNode, AgentManager agentManager) 
	{
		this.agentM = agentManager;
		this.failedNode = failedNode;
		this.startNode = startNode;
		this.observer = new AgentObserver();
	}
	
	/**
	 * Get the failure agent observer instance
	 * @return FailureAgent observer
	 */
	public AgentObserver getObserver()
	{
		return observer;
	}
	
	/**
	 * Give this failureAgent instance its manager
	 * @param AgentManager	The manager you want to bind the failureAgent on.
	 */
	public void setManager(AgentManager agentManager)
	{
		this.agentM = agentManager;
	}
	
	/**
	 * Get the node where the failure agent was first summoned upon
	 * @return The node on which the failure agent started.
	 */
	public Node getStartNode()
	{
		return this.startNode;
	}
	
	/**
	 * Get the node that failed
	 * @return the failed node.
	 */
	public Node getFailedNode()
	{
		return this.failedNode;
	}
	
	/**
	 * Runs the failure agent when it arrives.
	 */
	@Override
	public void run()
	{
		ArrayList<String> files = agentM.getLocalFiles();
		int failedNodeHash = failedNode.getHash(); 
		
		//Check for entries of failed node in owner files
		//ArrayList<FileProperties> owned = agentM.getOwnedOwnerFiles();
		/*
		synchronized(owned)
		{
			Iterator<FileProperties> it = owned.iterator();
			while(it.hasNext())
			{
			    FileProperties ownerFile = it.next();
			    if(ownerFile.getDownloadLocations().)
			    
			    if(!networkFiles.containsKey(new HashFunction().getHash(fileName)))
			    {
			    	networkFiles.put(fileName, new AgentFileEntry());		//Create new entry in file agent
			    }
			    else
			    {
			    	networkFiles.get(fileName).setAvailability(true);		//Check existence of file in system
			    }
			}
		}*/
		
		for(String fileName : files)
		{
			Node newOwner = agentM.getOwnerLocation(fileName);
			int newOwnerHash = newOwner.getHash();
			int fileHash = calculateHash(fileName);
			
			boolean act = false;
			
			if(fileHash > newOwnerHash) 		// standard situation: new owner is nearest smaller hash value
			{
				if((failedNodeHash > newOwnerHash) && (failedNodeHash < fileHash)) // failed node between newOwner and file
				{
					act = true;
				}
			}
			else	// end of circle 
			{
				if((failedNodeHash > newOwnerHash) && (failedNodeHash > fileHash)) // failed node is the last in the circle, file at start
				{
					act = true;
				}
				else if((failedNodeHash < newOwnerHash) && (failedNodeHash < fileHash)) 
				{
					act = true;
				}
			}
			
			if(act) 
			{
				// is dit voldoende voor puntjes 3.c.ii 1en2 ?
				try 
				{
					FileManagerInterface iFace = (FileManagerInterface) agentM.getFileManagerInterface(newOwner);
					// ownerfile aanmaken
					iFace.ownerSwitchFile(fileName, newOwner);
				}
				catch(NullPointerException | RemoteException e)
				{
					System.err.println("Failed to contact to node '" + newOwner.getHostname() + "': " + e.getMessage());
					agentM.nodeConnectionFailure(newOwner.getHostname());
				}
				
			}
		}
		
		//Prepare for transfer
		this.agentM = null;		//Delete reference to agent manager
		
		//FailureAgent ready to continue
		observer.setChanged();
		observer.notifyObservers(this);
	}
	
	
	/**
	 * Calculates the hash of a string.
	 * @param name	String to be hashed.
	 * @return The calculated hash value.
	 */
	private int calculateHash(String name)
	{
		return new HashFunction().getHash(name);
	}
}
