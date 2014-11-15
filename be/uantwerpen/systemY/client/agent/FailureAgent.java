package be.uantwerpen.systemY.client.agent;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import be.uantwerpen.systemY.interfaces.FileManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.shared.Node;

public class FailureAgent implements Runnable, Serializable
{
//RUNNABLE, SERIALIZABLE OBJECT DIE WORDT DOORGEGEVEN ALS RMI PARAMETER AAN DE VOLGENDE AGENTMANAGER
// --> Agent(2)
	private static final long serialVersionUID = 1L;
	
	private Node failedNode;
	private AgentManager aManager;
	
	public FailureAgent (AgentManager aManager) 
	{
		this.aManager = aManager;
		this.failedNode = aManager.getFailedNode();
	}
	
	
	
	public void run()
	{
		ArrayList<String> files = aManager.getLocalFiles();
		int failedNodeHash = failedNode.getHash(); 
		
		for(String fileName : files)
		{
			Node newOwner = aManager.getOwnerLocation(fileName);
			int newOwnerHash = newOwner.getHash();
			int fileHash = calculateHash(fileName);
			
			boolean act = false;
			
			if(fileHash > newOwnerHash) 		// standard situation
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
				FileManagerInterface iFace = (FileManagerInterface) aManager.getFileManagerInterface(newOwner);
				// is dit voldoende voor puntjes 3.c.ii 1en2 ?
				try 
				{
					iFace.ownerSwitchFile(fileName, newOwner);
				}
				catch(RemoteException e)
				{
					System.err.println("Failed to contact to node '" + newOwner.getHostname() + "': " + e.getMessage());
					aManager.nodeConnectionFailure(newOwner.getHostname());
				}
				
			}
		}
		
//		if(client.getNextNode() == startUpNode)	
//		{
//			// Then: agent has been on every node. Finish:
//			
//		}
//		else 
//		{
//			// client.getNextNode();
//			// switch to next node
//		}
	}
	
	
	// functie die misschien gemakkelijker via iets anders opgeroepen kan worden.
	// nu gekopieerd uit gemakkelijkheid
	/**
	 * Calculates the hash of a string.
	 * @param String	name	String to be hashed
	 * @return int		The hash
	 */
	private int calculateHash(String name)
	{
		int i = name.hashCode();
		i = Math.abs(i % 32768);
		return i;
	}
}
