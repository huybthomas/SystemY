package be.uantwerpen.systemY.client.agent;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import be.uantwerpen.systemY.client.downloadSystem.FileProperties;
import be.uantwerpen.systemY.networkservices.TCPConnection;
import be.uantwerpen.systemY.timeOut.TimeOutService;

/**
 * what has to be done?
 * 
 * Access tot de client methods		ok?
 * 
 * --
 * Download had to be stopped and started using the Agent's observer
 * Observer to notify of successful lock		partially ok? (none to notify)
 * Observer to activate requestUnlock
 * --
 * 
 * Niet lockbaar maken als het al gelocked is
 * 
 * --
 * Sending of Agent to next Client		RMI interface has to be implemented to be able to call FileAgentReceived()
 * Create RMI interface for Agent
 *--
 *
 */

public class FileAgent implements Runnable, Serializable
{

	private static final long serialVersionUID = 1L;
	private FileAgentObserver observer;
	private AgentManager agentM;
	private ArrayList<AgentFileEntry> files;
	
	HashMap<Integer, Boolean> lockHash = null;				//Unieke integer	//Lock				//Lock status of all files
	HashMap<Integer, FileProperties> dataHash = null;		//Unieke integer	//FileProperties	//All Files of all nodes
	
	ArrayList<String> locks, unlocks = new ArrayList<String>();

	/**
	 * Create the BootstrapManager object.
	 * @param Client	the client
	 * @throws RemoteException
	 */
	public FileAgent(AgentManager agentM)
	{
		this.agentM = agentM;
		this.observer = new FileAgentObserver();

	}

	public FileAgentObserver getObserver()
	{
		return this.observer;
	}
	
	public void FileAgentReceived(FileAgent agent)
	{
		this.lockHash = agent.lockHash;
		this.dataHash = agent.dataHash;
		this.run();
	}
	
	@Override
	public void run() 
	{
		/*
		//Check client for files.
		ArrayList<String> local = agentM.getLocalFiles();			//To be created
		//Update Agent fileList
		Iterator<String> it = local.iterator();
		while(it.hasNext())
		{
		    String str = it.next();
		    if(files.contains(new AgentFileEntry(str, false)) || files.contains(new AgentFileEntry(str, true)))
		    	;//do nothing
		    else
		    	files.add(new AgentFileEntry(str, false));
		    	
		}
		
		//Update list of files on node? Maybe not necessary since the Agent class has them already?
		agentM.setAvailableFiles(files);					//Method to be implemented?
		
		if(!locks.isEmpty())		//Aanvraag tot download, laten weten van zodra download mag beginnen (file gelocked)
		{
			Iterator<String> lockIterator = locks.iterator();
			while(lockIterator.hasNext())
			{
				String name = lockIterator.next();
				lockHash.put(name.hashCode(), true);
				observer.setChanged();
				observer.notifyObservers("Locked" + name);		//This can prob be better
			}
		}
		
		if(!unlocks.isEmpty())		//Download gedaan. File mag unlocked worden. (Wanneer gedaan? Als downloadmanage rewuestunlock activeert met observable?) 
		{
			Iterator<String> unLockIterator = unlocks.iterator();
			while(unLockIterator.hasNext())
			{
				String name = unLockIterator.next();
				lockHash.put(name.hashCode(), true);
				observer.setChanged();
				observer.notifyObservers("Unlocked" + name);	//This can prob be better
			}
		}
		
		try
		{
			//Send Agent to next client
			//RMIinterface.FileAgentReceived(this);			//Send to rmi of next node
		}
		catch(Exception e)
		{
			System.err.println("Err: " + e.getMessage());
		}
		*/
	}
	
	public void requestLockFile(String name)
	{
		locks.add(name);
		//lockHash.put(name.hashCode(), true);	//Volledige hash mogelijk
	}
	
	public void requestUnlockFile(String name)
	{
		unlocks.add(name);
		//lockHash.put(name.hashCode(), false);	//Volledige hash mogelijk
	}
	//RUNNABLE, SERIALIZABLE OBJECT DIE WORDT DOORGEGEVEN ALS RMI PARAMETER AAN DE VOLGENDE AGENTMANAGER
	// --> Agent(1)

	
}
