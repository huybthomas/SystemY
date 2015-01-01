package be.uantwerpen.systemY.client.agent;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import be.uantwerpen.systemY.client.downloadSystem.Download;
import be.uantwerpen.systemY.shared.HashFunction;

public class FileAgent implements Runnable, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private AgentObserver observer;
	private AgentManager agentM;
	private HashMap<String, AgentFileEntry> networkFiles;
	private boolean oneRoundCompleted;

	/**
	 * Create the AgentManager object.
	 * @param agentManager
	 * @throws RemoteException
	 */
	public FileAgent(AgentManager agentManager)
	{
		this.agentM = agentManager;
		this.networkFiles = new HashMap<String, AgentFileEntry>();
		this.observer = new AgentObserver();
		this.oneRoundCompleted = false;
	}
	
	/**
	 * Get the observer of the agent.
	 * @return	The observer of the agent.
	 */
	public AgentObserver getObserver()
	{
		return this.observer;
	}
	
	/**
	 * sets the manager of the agent.
	 * @param agentManager	The manager you want to use.
	 */
	public void setManager(AgentManager agentManager)
	{
		this.agentM = agentManager;
	}
	
	/**
	 * Checks if a complete round is made, when all the nodes have been passed.
	 * Indicates that the list on the file agent is complete.
	 */
	public void roundCompleted()
	{
		this.oneRoundCompleted = true;
	}
	
	/**
	 * Runs the fileAgent when it arrives.
	 */
	@Override
	public void run() 
	{
		if(this.oneRoundCompleted && agentM.isAgentMaster())	//Executed after each round
		{
			Iterator<Entry<String, AgentFileEntry>> fileIterator = networkFiles.entrySet().iterator();
			while(fileIterator.hasNext())
			{
				Entry<String, AgentFileEntry> entry = fileIterator.next();
				
				if(entry.getValue().getAvailability())			//File is still available in the network
				{
					entry.getValue().setAvailability(false);
				}
				else
				{
					fileIterator.remove();
				}
			}
			
			ArrayList<String> failedNodeQueue = agentM.getFailedNodeQueue();
			
			if(!failedNodeQueue.isEmpty())		//Unlock files from failed nodes
			{
				synchronized(failedNodeQueue)
				{
					Iterator<String> failedNodeIterator = failedNodeQueue.iterator();
					while(failedNodeIterator.hasNext())
					{
						String name = failedNodeIterator.next();
						
						for(Entry<String, AgentFileEntry> entry : networkFiles.entrySet())
						{
							if(entry.getValue().getLock().equals(name))
							{
								entry.getValue().unlock();
							}
						}
						failedNodeIterator.remove();
					}
				}
			}
		}
		
		//Check client for files.
		ArrayList<String> owned = agentM.getOwnedFiles();
		
		synchronized(owned)
		{
			//Update Agent fileList
			Iterator<String> it = owned.iterator();
			while(it.hasNext())
			{
			    String fileName = it.next();
			    if(!networkFiles.containsKey(new HashFunction().getHash(fileName)))
			    {
			    	networkFiles.put(fileName, new AgentFileEntry());		//Create new entry in file agent
			    }
			    else
			    {
			    	networkFiles.get(fileName).setAvailability(true);		//Check existence of file in system
			    }
			}
		}
		
		//Update list of files on node when list is completed after one round
		if(this.oneRoundCompleted)
		{
			agentM.setNetworkFiles(this.networkFiles.keySet());
		}
		
		ArrayList<Download> lockQueue = agentM.getLockQueue();
		
		if(!lockQueue.isEmpty() && this.oneRoundCompleted)
		{
			synchronized(lockQueue)
			{
				Iterator<Download> lockIterator = lockQueue.iterator();
				while(lockIterator.hasNext())
				{
					Download download = lockIterator.next();

					if(networkFiles.containsKey(download.getFileName()))
					{
						if((networkFiles.get(download.getFileName()).getLock() == null))
						{
							networkFiles.get(download.getFileName()).setLock(agentM.getHostname());
							
							agentM.runDownload(download);
							lockIterator.remove();
						}
					}
					else
					{
						if(download.getDownloadMode() == 1)				//Owner switch file
						{
							agentM.runDownload(download);
						}
						else
						{
							agentM.cancelDownload(download);
						}
						lockIterator.remove();
					}
				}
			}
		}
		
		ArrayList<String> unlockQueue = agentM.getUnlockQueue();
		
		if(!unlockQueue.isEmpty() && this.oneRoundCompleted)
		{
			synchronized(unlockQueue)
			{
				Iterator<String> unlockIterator = unlockQueue.iterator();
				while(unlockIterator.hasNext())
				{
					String name = unlockIterator.next();
					if(networkFiles.containsKey(name))
					{
						networkFiles.get(name).unlock();
					}
					unlockIterator.remove();
				}
			}
		}
		
		ArrayList<String> fileDeleteQueue = agentM.getDeleteFileQueue();
		
		if(!fileDeleteQueue.isEmpty() && this.oneRoundCompleted)
		{
			ArrayList<String> deleteGranted = new ArrayList<String>();
			
			synchronized(fileDeleteQueue)
			{
				Iterator<String> deleteIterator = fileDeleteQueue.iterator();
				while(deleteIterator.hasNext())
				{
					String name = deleteIterator.next();
					
					if(networkFiles.containsKey(name))
					{
						if((networkFiles.get(name).getLock() == null))
						{
							networkFiles.get(name).setLock(agentM.getHostname());
							
							deleteGranted.add(name);
							deleteIterator.remove();
						}
					}
					else
					{
						agentM.cancelDelete(name);
						deleteIterator.remove();
					}
				}
			}
			
			if(deleteGranted.size() > 0)
			{
				agentM.createFileDeletionAgent(deleteGranted);
			}
		}
		
		//Prepare for transfer
		this.agentM = null;		//Delete reference to agent manager
		
		//FileAgent ready to continue
		observer.setChanged();
		observer.notifyObservers(this);
	}
}
