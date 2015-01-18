package be.uantwerpen.systemY.client.agent;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import be.uantwerpen.systemY.client.Client;
import be.uantwerpen.systemY.client.downloadSystem.Download;
import be.uantwerpen.systemY.client.downloadSystem.FileProperties;
import be.uantwerpen.systemY.interfaces.AgentManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.shared.Node;
import be.uantwerpen.systemY.timer.TimerService;

public class AgentManager extends UnicastRemoteObject implements AgentManagerInterface
{
	private static final long serialVersionUID = 1L;
	
	private Client client;
	private TimerService timeOut;
	private TimerService masterAgentDelay;
	private boolean master;
	private int agentsRunning;
	private int deletionAgentsRunning;
	private int failureAgentsRunning;
	private FileAgent bufferedFileAgent;
	private ArrayList<Download> lockQueue;
	private ArrayList<Download> secondAttemptLockQueue;
	private ArrayList<String> unlockQueue;
	private ArrayList<String> failedNodeQueue;
	private ArrayList<String> deleteFileQueue;
	
	/**
	 * Activates the agent manager on the client.
	 * @param client	The current client.
	 * @throws RemoteException
	 */
	public AgentManager(Client client) throws RemoteException
	{
		this.client = client;
		this.timeOut = new TimerService(13000);	//13 seconds
		this.masterAgentDelay = new TimerService(500);	//0.5 second
		this.master = false;
		this.agentsRunning = 0;
		this.deletionAgentsRunning = 0;
		this.failureAgentsRunning = 0;
		this.lockQueue = new ArrayList<Download>();
		this.secondAttemptLockQueue = new ArrayList<Download>();	//Lock queue to allow file agent to update his list with new files before disposing the lock permanently
		this.unlockQueue = new ArrayList<String>();
		this.failedNodeQueue = new ArrayList<String>();
		this.deleteFileQueue = new ArrayList<String>();
		
		timeOut.getObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				fileAgentTimeOut();
			}
		});
		
		masterAgentDelay.getObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				fileAgentSend(bufferedFileAgent);
			}
		});
	}
	
	/**
	 * Reset the manager
	 */
	public void reset()
	{
		lockQueue = new ArrayList<Download>();
		secondAttemptLockQueue = new ArrayList<Download>();
		unlockQueue = new ArrayList<String>();
		failedNodeQueue = new ArrayList<String>();
		deleteFileQueue = new ArrayList<String>();
		this.agentsRunning = 0;
		this.deletionAgentsRunning = 0;
		this.failureAgentsRunning = 0;
	}
	
	/**
	 * Create a file Agent.
	 */
	public void createFileAgent()
	{
		FileAgent newAgent = new FileAgent(this);
		
		newAgent.getObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				fileAgentNextNode((FileAgent) object);
			}
		});
		
		new Thread(newAgent).start();
		addAgentsRunning();
	}
	
	/**
	 * Assign the specified node as the file agent's master
	 * @param node the node
	 * @param state the new status of the node as master
	 * @return true if successful, false otherwise
	 */
	public boolean assignFileAgentMaster(Node node, boolean state)
	{
		try
		{
			AgentManagerInterface iFace = (AgentManagerInterface) this.client.getAgentManagerInterface(node);
			iFace.setFileAgentMaster(state);
			return true;
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't change state of file agent master to next node: " + e.getMessage());
			client.nodeConnectionFailure(client.getNextNode().getHostname());
			return false;
		}
	}

	/**
	 * Gets the file agent from the previous node and runs it.
	 * @param agent	The received agent.
	 */
	public void fileAgentReceived(FileAgent agent)
	{
		if(this.client.getSessionState())
		{
			if(this.master)
			{
				timeOut.stopTimer();
				agent.roundCompleted();
			}
			else
			{
				if(this.client.getPrevNode().getHash() >= this.client.getThisNode().getHash())		//Check if node will be new master
				{
					setFileAgentMaster(true);
				}
				else
				{
					setFileAgentMaster(false);
				}
			}
			
			agent.getObserver().addObserver(new Observer()
			{
				public void update(Observable source, Object object)
				{
					fileAgentNextNode((FileAgent) object);
				}
			});
			
			agent.setManager(this);
			
			new Thread(agent).start();
			addAgentsRunning();
		}
	}
	
	/**
	 * Send the agent to the next node.
	 * @param agent	The agent that needs to be send to the next agent.
	 */
	public void fileAgentNextNode(FileAgent agent)
	{		
		agent.getObserver().deleteObservers();
		
		if(this.master)
		{
			bufferedFileAgent = agent;		//Make pointer to agent to buffer for delayed sending
			masterAgentDelay.startTimer();
		}
		else
		{
			fileAgentSend(agent);
		}
	}
	
	/**
	 * Send the file Agent to the next node
	 * @param agent This agent
	 */
	public void fileAgentSend(FileAgent agent)
	{
		if(agent != null)
		{
			delAgentsRunning();
			
			if(!client.getNextNode().equals(client.getThisNode()))
			{
				try
				{
					AgentManagerInterface iFace = (AgentManagerInterface)client.getAgentManagerInterface(client.getNextNode());
					iFace.fileAgentReceived(agent);
				}
				catch(RemoteException | NullPointerException e)
				{
					System.err.println("Can't pass file agent to next node: " + e.getMessage());
					client.nodeConnectionFailure(client.getNextNode().getHostname());
				}
			}
			else
			{
				this.fileAgentReceived(agent);
			}
			
			if(this.master)
			{
				bufferedFileAgent = null;		//Clear pointer to buffered agent before GC run
				timeOut.startTimer();
			}
			
			//Force garbage collector to remove file agent
			System.gc();	
		}
		else
		{
			client.printTerminalError("Illegal file agent detected - status: null");
			if((this.getAgentsRunning() - this.getFailureAgentsRunning() - this.getDeletionAgentsRunning()) > 0)
			{
				delAgentsRunning();
			}
		}
	}
	
	/**
	 * Checks the file agent master.
	 */
	public void checkFileAgentMaster()
	{
		if((client.getPrevNode().getHash() > client.getThisNode().getHash()) && !this.master)
		{
			setFileAgentMaster(true);
		}
		else if((client.getPrevNode().getHash() < client.getThisNode().getHash()) && this.master)
		{
			setFileAgentMaster(false);
		}
	}
	
	/**
	 * Sets the file agent master.
	 * @param isMaster True for master, false for not master.
	 */
	public void setFileAgentMaster(boolean isMaster)
	{
		if(isMaster && !this.master)
		{
			timeOut.startTimer();
		}
		else if(!isMaster && this.master)
		{
			timeOut.stopTimer();
		}
		this.master = isMaster;
	}
	
	/**
	 * Request if a specific node is the master.
	 * @return	True for master, false for not master.
	 */
	public boolean isAgentMaster()
	{
		return this.master;
	}
	
	/**
	 * Creates a new agent when the Time out has been activated.
	 */
	public void fileAgentTimeOut()
	{
		client.printTerminalInfo("File agent timed-out. New file agent created.");
		createFileAgent();
	}
	
	/**
	 * Creates a failure agent when a node has failed.
	 * @param failedNode	The node that failed.
	 */
	public void createFailureAgent(Node failedNode)
	{
		FailureAgent newAgent = new FailureAgent(failedNode, client.getThisNode(), this);
		
		newAgent.getObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				failureAgentNextNode((FailureAgent) object);
			}
		});
		
		new Thread(newAgent).start();
		addAgentsRunning();
		addFailureAgentsRunning();
	}
	
	/**
	 * Gets the failure agent from the previous node and runs it.
	 * @param agent	The received or made failureAgent.
	 */
	public void failureAgentReceived(FailureAgent agent)
	{
		if(this.client.getSessionState())
		{
			if(this.master)
			{
				failedNodeQueue.add(agent.getFailedNode().getHostname());
			}
			
			if(!agent.getStartNode().equals(client.getThisNode()))
			{
				agent.getObserver().addObserver(new Observer()
				{
					public void update(Observable source, Object object)
					{
						failureAgentNextNode((FailureAgent) object);
					}
				});
				
				agent.setManager(this);
				
				new Thread(agent).start();
				addAgentsRunning();
			}
			else													//Agent made the whole cycle through all the nodes
			{
				delFailureAgentsRunning();
			}
		}
	}
	
	/**
	 * Sends the agent to the next node.
	 * @param agent	The agent that needs to be send to the next node.
	 */
	public void failureAgentNextNode(FailureAgent agent)
	{		
		agent.getObserver().deleteObservers();
		
		delAgentsRunning();
		
		try
		{
			AgentManagerInterface iFace = (AgentManagerInterface)client.getAgentManagerInterface(client.getNextNode());
			iFace.failureAgentReceived(agent);
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't pass failure agent to next node: " + e.getMessage());
			client.nodeConnectionFailure(client.getNextNode().getHostname());
		}
		
		//Force garbage collector to remove failure agent
		System.gc();
	}
	
	/**
	 * Creates an agent that will go around all the nodes and delete the requested file on all of them.
	 * @param deleteFileRequests	The list with all the files in that need to be removed.
	 */
	public void createFileDeletionAgent(ArrayList<String> deleteFileRequests)
	{
		FileDeletionAgent newAgent = new FileDeletionAgent(deleteFileRequests, client.getThisNode(), this);
		
		newAgent.getObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				fileDeletionAgentNextNode((FileDeletionAgent) object);
			}
		});
		
		addAgentsRunning();
		addDeletionAgentsRunning();
		new Thread(newAgent).start();
	}
	
	/**
	 * Gets the deletion agent from the previous node and runs it.
	 * @param agent	The agent that needs to be received.
	 */
	public void fileDeletionAgentReceived(FileDeletionAgent agent)
	{
		if(this.client.getSessionState())
		{
			if(!agent.getStartNode().equals(client.getThisNode()))
			{
				agent.getObserver().addObserver(new Observer()
				{
					public void update(Observable source, Object object)
					{
						fileDeletionAgentNextNode((FileDeletionAgent) object);
					}
				});
				
				agent.setManager(this);
				
				addAgentsRunning();
				new Thread(agent).start();
			}
			else													//Agent made the whole cycle through all the nodes
			{
				delDeletionAgentsRunning();
			}
		}
	}
	
	/**
	 * Geeft de agent door aan de volgende node.
	 * @param agent	De agent die moet worden doorgegeven.
	 */
	public void fileDeletionAgentNextNode(FileDeletionAgent agent)
	{
		agent.getObserver().deleteObservers();
		
		delAgentsRunning();
		
		try
		{
			AgentManagerInterface iFace = (AgentManagerInterface)client.getAgentManagerInterface(client.getNextNode());
			iFace.fileDeletionAgentReceived(agent);
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't pass file deletion agent to next node: " + e.getMessage());
			client.nodeConnectionFailure(client.getNextNode().getHostname());
		}
		
		//Force garbage collector to remove file deletion agent
		System.gc();
	}
	
	/**
	 * Adds files to the networkFiles list.
	 * @param files	The names of the files that need to be added.
	 */
	public void setNetworkFiles(Set<String> files)
	{
		ArrayList<String> networkFiles = new ArrayList<String>();
		networkFiles.addAll(files);
		
		Collections.sort(networkFiles);
		
		client.setNetworkFiles(networkFiles);
	}
	
	/**
	 * Get the list of local system files.
	 * @return	The list of local system files.
	 */
	public File[] getLocalSystemFiles()
	{
		return client.getLocalSystemFiles();
	}
	
	/**
	 * Get the owned files.
	 * @return the files.
	 */
	public ArrayList<String> getOwnedFiles()
	{
		return client.getOwnedFiles();
	}
	
	public ArrayList<FileProperties> getOwnedOwnerFiles()
	{
		return client.getOwnedOwnerFiles();
	}
	
	public ArrayList<String> getReplicatedFiles()
	{
		return client.getReplicatedFiles();
	}
	
	/**
	 * Delete files locally.
	 * @param deleteFileRequests The files.
	 * @return True is successful, false otherwise.
	 */
	public boolean deleteFilesFromSystem(ArrayList<String> deleteFileRequests) 
	{
		return client.deleteFilesFromSystem(deleteFileRequests);
	}
	
	/**
	 * Adds files to the locked queue.
	 * @param download	The download to be queued for downloading from the network.
	 * @return	True when successful, false when failed.
	 */
	public boolean addLockQueue(Download download)
	{
		synchronized(lockQueue)
		{
			return this.lockQueue.add(download);
		}
	}
	
	/**
	 * Deletes a file from the locked queue.
	 * @param fileName	The name of the file that needs to be unlocked.
	 * @return	True when successful, false when failed.
	 */
	public boolean delLockQueue(String fileName)
	{
		synchronized(lockQueue)
		{
			return this.lockQueue.remove(fileName);
		}
	}
	
	/**
	 * Adds a file to the unlock queue.
	 * @param fileName	The file that needs to be unlocked.
	 * @return	True when file is add to unlock queue, false when failed.
	 */
	public boolean addUnlockQueue(String fileName)
	{
		synchronized(unlockQueue)
		{
			return this.unlockQueue.add(fileName);
		}
	}
	
	/**
	 * Deletes a file from the unlock queue.
	 * @param fileName	The file that was unlocked.
	 * @return	True when file is deleted from unlock queue, false when failed.
	 */
	public boolean delUnlockQueue(String fileName)
	{
		synchronized(unlockQueue)
		{
			return this.unlockQueue.remove(fileName);
		}
	}
	
	/**
	 * Adds a node to the failed node list.
	 * @param hostname	Name of the failed node that needs to be added to the list.
	 * @return	True when node added, false when failed.
	 */
	public boolean addFailedNodeQueue(String hostname)
	{
		synchronized(failedNodeQueue)
		{
			return this.failedNodeQueue.add(hostname);
		}
	}
	
	/**
	 * Delete a node from the failed node list
	 * @param hostname	Name of the node that needs to be deleted from the list.
	 * @return	True when node was deleted, false when failed.
	 */
	public boolean delFailedNodeQueue(String hostname)
	{
		synchronized(failedNodeQueue)
		{
			return this.failedNodeQueue.remove(hostname);
		}
	}
	
	/**
	 * Add a delete file queue for files.
	 * @param deleteFiles The files.
	 * @return True if successful, false otherwise.
	 */
	public boolean addDeleteFileQueue(ArrayList<String> deleteFiles)
	{
		synchronized(deleteFileQueue)
		{
			return this.deleteFileQueue.addAll(deleteFiles);
		}
	}
	
	/**
	 * Get the list with lock requests.
	 * @return	The list with lock requests.
	 */
	public ArrayList<Download> getLockQueue()
	{
		return this.lockQueue;
	}
	
	/**
	 * Get the list with all lock requests for a second attempt to lock the file. File agent will make a second round around the network.
	 * @return The list with all second attempt lock requests.
	 */
	public ArrayList<Download> getSecondAttemptLockQueue()
	{
		return this.secondAttemptLockQueue;
	}
	
	/**
	 * Get the list with unlock requests.
	 * @return	The list with unlock requests.
	 */
	public ArrayList<String> getUnlockQueue()
	{
		return this.unlockQueue;
	}
	
	/**
	 * Get the list with failed nodes.
	 * @return	The list with failed nodes.
	 */
	public ArrayList<String> getFailedNodeQueue()
	{
		return this.failedNodeQueue;
	}
	
	/**
	 * Get the delete file queue.
	 * @return The queue
	 */
	public ArrayList<String> getDeleteFileQueue()
	{
		return this.deleteFileQueue;
	}
	
	/**
	 * Run a donwload.
	 * @param download The download0
	 */
	public void runDownload(Download download)
	{
		client.runDownload(download);
	}
	
	/**
	 * Cancel a donwload.
	 * @param download The download.
	 */
	public void cancelDownload(Download download)
	{
		client.printTerminalError("File: " + download.getFileName() + " not available in the network.");
	}
	
	/**
	 * Cancel a delete request.
	 * @param fileName The file's name.
	 */
	public void cancelDelete(String fileName)
	{
		client.printTerminalError("File: " + fileName + " not available for deletion in the network.");
	}
	
	/**
	 * Get the information of the owner of a specific file.
	 * @param fileName	The name of the file you want to get.
	 * @return	The information of the node where the file is located.
	 */
	public Node getOwnerLocation(String fileName)
	{
		try
		{
			NodeManagerInterface iFace = (NodeManagerInterface) client.getNodeServerInterface();
			return iFace.getFileLocation(fileName);
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't contact server for filelocation: " + e.getMessage());
			client.serverConnectionFailure();
		}
		return null;
	}
	
	/**
	 * Get the fileManagerInterface of a specific node.
	 * @param node	The node of which you want the interface.
	 * @return	The fileManagerInterface.
	 */
	public Object getFileManagerInterface(Node node)
	{
		return client.getFileManagerInterface(node);
	}
	
	/**
	 * Check if the connection to the node is broken.
	 * @param hostname	The name of the node you want to check.
	 * @return	True if connection failure handled correctly, false if not.
	 */
	public boolean nodeConnectionFailure(String hostname)
	{
		return client.nodeConnectionFailure(hostname);
	}
	
	/**
	 * Get the hostname.
	 * @return The name of the host.
	 */
	public String getHostname()
	{
		return client.getHostname();
	}
	
	public Node getThisNode()
	{
		return client.getThisNode();
	}
	
	public Node getPrevNode()
	{
		return client.getPrevNode();
	}
	
	public Node getNextNode()
	{
		return client.getNextNode();
	}
	
	public boolean addOwnerFile(String fileName, Node ownerNode)
	{
		return this.client.addOwnerFile(fileName, ownerNode);
	}
	
	/**
	 * Get the number of running agents.
	 * @return The number of running agents.
	 */
	public int getAgentsRunning()
	{
		return this.agentsRunning;
	}
	
	/**
	 * Get the number of failure agents running.
	 * @return The number of running failure agents.
	 */
	public int getFailureAgentsRunning()
	{
		return this.failureAgentsRunning;
	}
	
	/**
	 * Get the number of deletion agents running.
	 * @return The number of deletion agents.
	 */
	public int getDeletionAgentsRunning()
	{
		return this.deletionAgentsRunning;
	}
	
	/**
	 * Add one to the number of running agents.
	 */
	private synchronized void addAgentsRunning()
	{
		this.agentsRunning = this.agentsRunning + 1;
	}
	
	/**
	 * Delete one from the number of running agents.
	 */
	private synchronized void delAgentsRunning()
	{
		this.agentsRunning = this.agentsRunning - 1;
	}
	
	/**
	 * Add one to the number of running failure agents.
	 */
	private synchronized void addFailureAgentsRunning()
	{
		this.failureAgentsRunning = this.failureAgentsRunning + 1;
	}
	
	/**
	 * Delete one from the running failure agents.
	 */
	private synchronized void delFailureAgentsRunning()
	{
		this.failureAgentsRunning = this.failureAgentsRunning - 1;
	}
	
	/**
	 * Add one to the number of deletion agents.
	 */
	private synchronized void addDeletionAgentsRunning()
	{
		this.deletionAgentsRunning = this.deletionAgentsRunning + 1;
	}
	
	/**
	 * remove one from the number of deletion agents.
	 */
	private synchronized void delDeletionAgentsRunning()
	{
		this.deletionAgentsRunning = this.deletionAgentsRunning - 1;
	}
}