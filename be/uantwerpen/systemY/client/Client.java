package be.uantwerpen.systemY.client;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import be.uantwerpen.systemY.client.agent.AgentManager;
import be.uantwerpen.systemY.client.downloadSystem.Download;
import be.uantwerpen.systemY.client.downloadSystem.FileManager;
import be.uantwerpen.systemY.client.downloadSystem.FileProperties;
import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.networkservices.Networkinterface;
import be.uantwerpen.systemY.networkservices.TCPConnection;
import be.uantwerpen.systemY.shared.Node;

/**
 * Client class is the main class of the network nodes in the SystemY project.
 */
public class Client
{
	private static final String version = "v0.6";
	private Terminal terminal;
	private NodeLinkManager nodeLinkManager;
	private BootstrapManager bootstrap;
	@SuppressWarnings("unused")
	private DiscoveryManager discovery;
	private ShutdownManager shutdownM;
	private AgentManager agentM;
	private FileManager fileManager;
	private FailureManager failureM;
	private Networkinterface iFace;
	private boolean activeSession;
	private boolean forceShutdown;
	private ClientObserver observer;
	
	/**
	 * Creates the Client Object.
	 * @param enableTerminal	Make client boot along with a terminal window for user input.
	 * @param hostname			The client's hostname used to identify a node in the network,
	 * 							this name must be unique in the network.	
	 * @param networkIP			The client's IP address of the networkcard connected to the systemY network,
	 * 							given as a String-value.
	 * @param tcpReceivePort	The port used for receiving file requests through TCP connections
	 * @param tcpSendPort		The port used for sending file requests through TCP connections
	 * @param rmiPort			The port used for the remote method invocation calls, this port must be
	 * 							the same on each node in the network.
	 * @param multicastIP		The IP address used for multicasts messages on the network, this ip address
	 * 							must be the same on each node in the network and in the range of valid
	 * 							broadcast address.
	 * @param multicastPort		The port used for multicast messages on the network.
	 * @throws RemoteException
	 */
	public Client(boolean enableTerminal, String hostname, String networkIP, int tcpReceivePort, int tcpSendPort, int rmiPort, String multicastIP, int multicastPort) throws RemoteException
	{
		activeSession = false;
		forceShutdown = false;
		
		this.observer = new ClientObserver();
		
		//setup terminal
		terminal = new Terminal(this);
		
		//setup networkinterface
		iFace = new Networkinterface(networkIP, rmiPort, tcpReceivePort, tcpSendPort, multicastIP, multicastPort);
		
		fileManager = new FileManager(this.iFace.getTCPObserver(), this);
		
		bootstrap = new BootstrapManager(this);
		discovery = new DiscoveryManager(this.iFace.getMulticastObserver(), this);
		shutdownM = new ShutdownManager(this);
		failureM = new FailureManager(this);
		agentM = new AgentManager(this);
		
		if(!setupServices())
		{
			printTerminalError("System not fully operational. Instability issues can occur. Resolve the issue and reboot the system.");
		}
		
		nodeLinkManager = new NodeLinkManager(new Node(hostname, networkIP));
		
		if(enableTerminal)
		{
			printTerminal("SystemY 2014 Client " + version + " - Created by: Thomas Huybrechts, Arthur Janssens, Quinten Van Hasselt, Dries Blontrock");
			terminal.activateTerminal();
		}
	}
	
	/**
	 * Get the client's observer.
	 */
	public ClientObserver getObserver()
	{
		return this.observer;
	}
	
	/**
	 * Make the client log in to SystemY.
	 * @return True if the function succeeded without errors, false if not.
	 */
	public boolean loginSystem()
	{
		if(!activeSession)
		{
			this.setSessionState(true);
			
			//Create services
			this.createServices();
			
			if(!bootstrap.startBootstrap())
			{
				this.setSessionState(false);
				
				//Stop the created services
				this.stopServices();
				
				printTerminalError("Login failed! Bootstrap didn't launch correctly.");
				this.observer.setChanged();
				this.observer.notifyObservers("LoginFailed");
				return false;
			}
		}
		else
		{
			printTerminalError("Already an active session running.");
			return false;
		}
		return true;
	}
	
	/**
	 * The Procedure to be done when the client's login fails.
	 */
	public void loginFailed()
	{
		shutdownM.shutdown();
		
		activeSession = false;
		this.agentM.setFileAgentMaster(false);
		
		if(agentM.isAgentMaster() && !this.getPrevNode().equals(this.getThisNode()))		//Next node becomes agent master
		{
			this.assignFileAgentMaster(this.getNextNode(), true);
		}
		
		stopServices();
		
		fileManager.shutdownFileClear();
		
		printTerminalError("Client couldn't login to the system.");
		this.observer.setChanged();
		this.observer.notifyObservers("LoginFailed");
	}
	
	/**
	 * Make the client log out of SystemY.
	 * @return True if the function succeeded without errors, false if not.
	 */
	public boolean logoutSystem()
	{		
		boolean status = true;
		
		if(!activeSession)
		{
			printTerminalError("No active session running.");
			this.observer.setChanged();
			this.observer.notifyObservers("Logout");
			return true;
		}
		
		if(fileManager.getQueuedDownloads() > 0 || agentM.getLockQueue().size() > 0 || agentM.getUnlockQueue().size() > 0 || agentM.getFailedNodeQueue().size() > 0 || agentM.getFailureAgentsRunning() > 0 || agentM.getDeleteFileQueue().size() > 0 || agentM.getDeletionAgentsRunning() > 0 || fileManager.getDownloadsHosting() > 0)
		{
			printTerminalInfo("Waiting for running downloads to finish.");
			this.observer.setChanged();
			this.observer.notifyObservers("WaitingForDownloads");
		}
		
		while((fileManager.getQueuedDownloads() > 0 || agentM.getLockQueue().size() > 0 || agentM.getUnlockQueue().size() > 0 || agentM.getFailedNodeQueue().size() > 0 || agentM.getFailureAgentsRunning() > 0 || agentM.getDeleteFileQueue().size() > 0 || agentM.getDeletionAgentsRunning() > 0 || fileManager.getDownloadsHosting() > 0) && !forceShutdown)
		{
			//Wait for downloads to finish or until forceShutdown flag is set
			// stalling the while loop a little
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		this.observer.setChanged();
		this.observer.notifyObservers("DownloadsReady");
		this.forceShutdown = false;
		
		if(!fileManager.shutdownFileUpdate())
		{
			printTerminalError("Shutdown file update failed.");
			status = false;
		}
		
		if(agentM.getDeletionAgentsRunning() > 0 || agentM.getDeleteFileQueue().size() > 0)
		{
			printTerminalInfo("Waiting for the notifications to the network to finish.");
			this.observer.setChanged();
			this.observer.notifyObservers("WaitingForDeletionAgents");
		}
		
		while((agentM.getDeletionAgentsRunning() > 0 || agentM.getDeleteFileQueue().size() > 0) && !forceShutdown)
		{
			//Wait for deletion agent to finish or until forceShutdown flag is set
			// stalling the while loop a little
			try
			{
				Thread.sleep(10);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		this.observer.setChanged();
		this.observer.notifyObservers("DeletionAgentsReady");
		this.forceShutdown = false;
		
		if(!shutdownM.shutdown())
		{
			printTerminalError("Client didn't logout correctly out of the system.");
			status = false;
		}
		
		activeSession = false;
		this.agentM.setFileAgentMaster(false);
		
		if(agentM.isAgentMaster() && !this.getPrevNode().equals(this.getThisNode()))		//Next node becomes agent master
		{
			this.assignFileAgentMaster(this.getNextNode(), true);
		}
		
		if(agentM.getAgentsRunning() > 0)
		{
			printTerminalInfo("Waiting for running agents to finish: " + agentM.getAgentsRunning() + " agents.");
			this.observer.setChanged();
			this.observer.notifyObservers("WaitingForAgents");
		}
		
		while(agentM.getAgentsRunning() > 0 && !forceShutdown)
		{
			//Wait for agents to finish
			// stalling the while loop a little
			try
			{
				Thread.sleep(10);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		this.observer.setChanged();
		this.observer.notifyObservers("AgentsReady");
		this.forceShutdown = false;
		
		if(!fileManager.shutdownTransfer() && status)
		{
			printTerminalError("Shutdown filetransfer failed.");
			status = false;
		}
		
		if(fileManager.getDownloadsHosting() > 0)
		{
			printTerminalInfo("Waiting for the hosted downloads to finish.");
			this.observer.setChanged();
			this.observer.notifyObservers("WaitingForHostedDownloads");
		}
		
		while((fileManager.getDownloadsHosting() > 0 || fileManager.getOwnedOwnerFiles().size() > 0) && !forceShutdown)
		{
			//Wait for hosted downloads to finish or until forceShutdown flag is set
			// stalling the while loop a little
			try
			{
				Thread.sleep(10);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		this.observer.setChanged();
		this.observer.notifyObservers("HostedDownloadsReady");
		this.forceShutdown = false;
		
		//End of file transaction, shutting down the system
		stopServices();
		
		fileManager.shutdownFileClear();
		
		this.observer.setChanged();
		this.observer.notifyObservers("Logout");
		
		if(!status)
		{
			this.observer.setChanged();
			this.observer.notifyObservers("LogoutFailed");
		}
		
		return status;
	}
	
	/**
	 * The procedure to be executed after a connection failure.
	 */
	public void criticalErrorStop()
	{
		this.stopServices();
		this.setSessionState(false);
		this.agentM.setFileAgentMaster(false);
		
		this.getObserver().setChanged();
		this.getObserver().notifyObservers("ConnectionFailure");
	}
	
	/**
	 * Adjust the session state variable to the given parameter.
	 * @param state	The given state: true if a session is made, false if not. 
	 */
	public void setSessionState(boolean state)
	{
		this.activeSession = state;
	}
	
	/**
	 * Get the session state.
	 * @return	True if session active, false if not.
	 */
	public boolean getSessionState()
	{
		return this.activeSession;
	}
	
	/**
	 * Set the forceShutdown variable of the client.
	 */
	public void forceShutdown()
	{
		this.forceShutdown = true;
	}
	
	/**
	 * Start RMI and discovery multicast service.
	 * @return True if success, false if not.
	 */
	public boolean createServices()
	{
		//Start RMI-server (NodeLinkManager)
		if(!iFace.bindRMIServer(this.nodeLinkManager, "NodeLinkManager_" + getHostname()))
		{
			return false;
		}
		
		//Reset queues before starting the agent manager service
		agentM.reset();
		
		//Start RMI-server (AgentManager)
		if(!iFace.bindRMIServer(this.agentM, "AgentManager_" + getHostname()))
		{
			return false;
		}
		
		//Start download tcpservice
		iFace.runTCPListener();
		
		//Start fileManager service
		if(!fileManager.startService())
		{
			return false;
		}
		
		//Wait for services to be fully configured
		try
		{
			Thread.sleep(1000);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}

		return true;
	}
	
	/**
	 * Start the MulticastService.
	 * @return True if successful, false otherwise.
	 */
	public boolean runService()
	{
		//Start discovery multicastservice
		iFace.runMulticastservice();
		
		return fileManager.bootTransfer();
	}
	
	/**
	 * Stop the DiscoveryService and the ConnectionServices.
	 * @return True if successful, false otherwise.
	 */
	public boolean stopServices()
	{
		if(this.stopDiscoveryService() && this.stopConnectionServices())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Stop multicast service.
	 * @return True if success, false if not.
	 */
	public boolean stopDiscoveryService()
	{
		//Stop discovery multicastservice
		if(!iFace.stopMulticastservice())
		{
			printTerminalError("Multicastservice didn't stop properly.");
			return false;
		}
		return true;
	}
	
	/**
	 * Stop tcp and rmi services.
	 * @return True if success, false if not.
	 */
	public boolean stopConnectionServices()
	{
		boolean stopSuccessful = true;
		
		//Stop download tcpservice
		if(!iFace.stopTCPListener())
		{
			printTerminalError("TCP service didn't stop properly.");
			stopSuccessful = false;
		}
				
		//Stop RMI-server nodeLinkManager
		if(!iFace.unbindRMIServer("NodeLinkManager_" + getHostname()))
		{
			printTerminalError("RMI service NodeLinkManager didn't stop properly.");
			stopSuccessful = false;
		}
		
		//Stop RMI-server fileManager
		if(!iFace.unbindRMIServer("FileManager_" + getHostname()))
		{
			printTerminalError("RMI service FileManager didn't stop properly.");
			stopSuccessful = false;
		}
		
		//Stop RMI-server agentManager
		if(!iFace.unbindRMIServer("AgentManager_" + getHostname()))
		{
			printTerminalError("RMI service AgentManager didn't stop properly.");
			stopSuccessful = false;
		}

		return stopSuccessful;
	}
	
	/**
	 * Send multicast to network.
	 * @param message	The message you wnat to send.
	 * @return True if success, false if not.
	 */
	public boolean sendMulticast(byte[] message)
	{
		return iFace.sendMulticast(message);
	}
	
	/**
	 * Closes the running session and shuts down the client.
	 */
	public void exitSystem()
	{
		if(activeSession)
		{
			logoutSystem();
		}
		System.exit(1);
	}
	
	/**
	 * Set the ip of the nameserver.
	 * @param ip	The IP of the server you want to set.
	 */
	public void setServerIP(String ip)
	{
		this.nodeLinkManager.setServerIP(ip);
	}
	
	/**
	 * Get the ip of the nameserver.
	 * @return ip	The IP of the server.
	 */
	public String getServerIP()
	{
		return this.nodeLinkManager.getServerIP();
	}
	
	/**
	 * Set the hostname of this node.
	 * @param name	The name of the host.
	 */
	public boolean setHostname(String name)
	{
		if(!activeSession)
		{
			this.nodeLinkManager.setMyHostname(name);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Get the hostname of this node.
	 * @return name	The name of the host.
	 */
	public String getHostname()
	{
		return this.nodeLinkManager.getMyHostname();
	}
	
	/**
	 * Get the ip address of this node.
	 * @return ip	The IP of the node.
	 */
	public String getIP()
	{
		return this.nodeLinkManager.getMyIP();
	}
	
	/**
	 * Set the ip address of this node.
	 * @param ip	The IP you want to set.
	 */
	public boolean setIP(String ip)
	{
		if(!activeSession)
		{
			this.nodeLinkManager.setMyIP(ip);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Set the next and previous nodes of this node.
	 * @param prevNode	The previous node you want to set.
	 * @param nextNode	The next node you want to set.
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode)
	{
		this.nodeLinkManager.setLinkedNodes(prevNode, nextNode);
		this.agentM.checkFileAgentMaster();
	}
	
	/**
	 * Let the nodeLinkManager add a new node.
	 * @param newNode	The new node you want to add.
	 */
	public Node updateLinks(Node newNode)
	{
		Node oldNode = this.nodeLinkManager.updateLinks(newNode);
		this.agentM.checkFileAgentMaster();
		return oldNode;
	}
	
	/**
	 * Set the next node of this node.
	 * @param nextNode	The next node you want to set.
	 */
	public boolean setNextNode(Node node)
	{
		if(node != null) 
		{
			try 
			{
				this.nodeLinkManager.setNext(node);
				return true;
			}
			catch(Exception e) 
			{
				System.err.println("NodeLinks exception:" + e.getMessage());
				return false;
			}
		} 
		else
		{
			return false;
		}
	}
	
	/**
	 * Set the previous node of this node.
	 * @param prevNode	The previous node that needs to be set.
	 */
	public boolean setPrevNode(Node node)
	{
		if(node != null) 
		{
			try 
			{
				this.nodeLinkManager.setPrev(node);
				this.agentM.checkFileAgentMaster();
				return true;
			}
			catch(Exception e) 
			{
				System.err.println("NodeLinks exception:" + e.getMessage());
				return false;
			}
		} 
		else
		{
			return false;
		}
	}
	
	/**
	 * Get the next node of this node.
	 * @return nextNode	The next node of the current node.
	 */
	public Node getNextNode()
	{
		return this.nodeLinkManager.getNext();
	}
	
	/**
	 * Get the previous node of this node.
	 * @return prevNode	The previous node of the current node.
	 */
	public Node getPrevNode()
	{
		return this.nodeLinkManager.getPrev();
	}
	
	/**
	 * Return this node.
	 * @return This node.
	 */
	public Node getThisNode()
	{
		return this.nodeLinkManager.getThisNode();
	}
	
	/**
	 * Execute DiscoveryFileTransfer.
	 */
	public void discoveryFileTransfer()
	{
		this.fileManager.discoveryTransfer();
	}
	
	/**
	 * Check if this filename exist on this node.
	 * @param fileName	The name of the file.
	 * @return True if successful, false otherwise.
	 */
	public boolean checkLocalExistence(String fileName)
	{
		return fileManager.checkSystemFileExistence(fileName);
	}
	
	/**
	 * Import a file.
	 * @param file The file to be imported.
	 */
	public void importFile(File file)
	{
		fileManager.importFile(file);
	}
	
	/**
	 * Open a file.
	 * @param fileName	The name of the file.
	 */
	public void openFile(String fileName)
	{
		fileManager.openFile(fileName);
	}
	
	/**
	 * Delete a file from the network.
	 * @param fileName	The name of the file.
	 */
	public void deleteFileFromNetwork(String fileName) 
	{
		ArrayList<String> deleteFileRequests = new ArrayList<String>(Arrays.asList(fileName));
		agentM.addDeleteFileQueue(deleteFileRequests);
	}
	
	/**
	 * Delete multiple files from the network.
	 * @param files	The files to be deleted.
	 */
	public void deleteFilesFromNetwork(ArrayList<String> files)
	{
		agentM.addDeleteFileQueue(files);
	}
	
	/**
	 * Check if a file can be deleted.
	 * @param fileName	The name of the file.
	 * @return	True if successful, false otherwise.
	 */
	public boolean canBeDeleted(String fileName) 
	{
		if(checkLocalExistence(fileName)) 
		{
			return fileManager.canBeDeleted(fileName);
		} 
		else 
		{
			return false;
		}
	}
	
	/**
	 * Delete a file locally.
	 * @param fileName The name of the file.
	 * @return	True of successful, false otherwise.
	 */
	public boolean deleteLocalFile(String fileName)
	{
		if(canBeDeleted(fileName))
		{
			fileManager.deleteFileRequest(fileName);
			fileManager.deleteDownloadLocation(fileName);
			
			this.observer.setChanged();
			this.observer.notifyObservers("UpdateNetworkFiles");
			return true;
		}
		return false;
	}
	
	/**
	 * Delete multiple files locally.
	 * @param deleteFileRequests The files to be deleted.
	 * @return True if successful, false otherwise.
	 */
	public boolean deleteFilesFromSystem(ArrayList<String> deleteFileRequests) 
	{
		return fileManager.deleteFilesFromSystem(deleteFileRequests);
	}
	
	/**
	 * Request a download.
	 * @param download	The download.
	 */
	public void downloadRequest(Download download)
	{
		this.agentM.addLockQueue(download);
	}
	
	/**
	 * Run a donwload.
	 * @param download Download.
	 */
	public void runDownload(Download download)
	{
		this.fileManager.runDownload(download);
	}
	
	/**
	 * Unlock the download after its finished.
	 * @param filename The file to be unlocked.
	 */
	public void downloadFinished(String filename)
	{
		this.agentM.addUnlockQueue(filename);
		
		this.observer.setChanged();
		this.observer.notifyObservers("UpdateNetworkFiles");
	}
	
	/**
	 * Get the download location.
	 * @return The download location.
	 */
	public String getDownloadLocation()
	{
		return this.fileManager.getDownloadLocation();
	}
	
	/**
	 * Set the download location.
	 * @param location The location
	 * @return True if successful, false otherwise.
	 */
	public boolean setDownloadLocation(String location)
	{
		return this.fileManager.setDownloadLocation(location);
	}
	
	public boolean addOwnerFile(String fileName, Node ownerNode)
	{
		return this.fileManager.addOwnerFile(fileName, ownerNode);
	}
	
	/**
	 * Bind object to bindlocation.
	 * @param object to bind
	 * @param bindName
	 * @return True if success, false if not.
	 */
	public boolean bindRMIservice(Object object, String bindName)
	{
		return iFace.bindRMIServer(object, bindName);
	}
	
	/**
	 * Unbind bindlocation.
	 * @param bindName
	 * @return True if success, false if not.
	 */
	public boolean unbindRMIservice(String bindName)
	{
		return iFace.unbindRMIServer(bindName);
	}
	
	/**
	 * Get object from bindlocation.
	 * @param String	bindName
	 * @return Object	the RMI object.
	 */
	public Object getRMIInterface(String bindLocation)
	{
		return this.iFace.getRMIInterface(bindLocation);
	}
	
	/**
	 * Get the node server's interface.
	 * @return The interface.
	 */
	public Object getNodeServerInterface()
	{
		return this.iFace.getRMIInterface("//" + this.getServerIP() + "/NodeServer");
	}
	
	/**
	 * Get the node's link interface.
	 * @param node The node
	 * @return The interface.
	 */
	public Object getNodeLinkInterface(Node node)
	{
		return this.iFace.getRMIInterface("//" + node.getIpAddress() + "/NodeLinkManager_" + node.getHostname());
	}
	
	/**
	 * Get the node's bootstrap interface.
	 * @param node The node
	 * @return The interface.
	 */
	public Object getBootstrapInterface(Node node)
	{
		return this.iFace.getRMIInterface("//" + node.getIpAddress() + "/Bootstrap_" + node.getHostname());
	}
	
	/**
	 * Get the node's file manager interface.
	 * @param node The node
	 * @return The interface.
	 */
	public Object getFileManagerInterface(Node node)
	{
		return this.iFace.getRMIInterface("//" + node.getIpAddress() + "/FileManager_" + node.getHostname());
	}
	
	/**
	 * Get the node's agent manager interface.
	 * @param node The node
	 * @return The interface.
	 */
	public Object getAgentManagerInterface(Node node)
	{
		return this.iFace.getRMIInterface("//" + node.getIpAddress() + "/AgentManager_" + node.getHostname());
	}
	
	/**
	 * Get the tcp connection.
	 * @param destinationIP The ip of the connection.
	 * @return The connection.
	 */
	public TCPConnection getTCPConnection(String destinationIP)
	{
		return this.iFace.getTCPConnection(destinationIP);
	}
	
	/**
	 * Handles the failure of a node, returns true if the failure is handled correctly.
	 * @param hostname
	 * @return True if connection failure handled correctly, false if not.
	 */
	public boolean nodeConnectionFailure(String hostname)
	{
		return this.failureM.nodeConnectionFailure(hostname);
	}
	
	/**
	 * Stops the services of this client because the server failed.
	 */
	public void serverConnectionFailure()
	{
		this.failureM.serverConnectionFailure();
	}
	
	/**
	 * Get the owned owner files.
	 * @return The owned owner files
	 */
	public ArrayList<FileProperties> getOwnedOwnerFiles()
	{
		return fileManager.getOwnedOwnerFiles();
	}
	
	/**
	 * Get the owned files.
	 * @return the files
	 */
	public ArrayList<String> getOwnedFiles()
	{
		return fileManager.getOwnedFiles();
	}
	
	public ArrayList<String> getReplicatedFiles()
	{
		return fileManager.getReplicatedFiles();
	}
	
	/**
	 * Get the local system files.
	 * @return the files
	 */
	public File[] getLocalSystemFiles()
	{
		return fileManager.getLocalSystemFiles();
	}
	
	/**
	 * Get the network files.
	 * @return the files
	 */
	public ArrayList<String> getNetworkFiles()
	{
		return fileManager.getNetworkFiles();
	}
	
	/**
	 * Set the network's files.
	 * @param networkFiles the network's files.
	 */
	public void setNetworkFiles(ArrayList<String> networkFiles)
	{
		if(!this.fileManager.getNetworkFiles().containsAll(networkFiles) || (this.fileManager.getNetworkFiles().size() != networkFiles.size()))
		{
			this.fileManager.setNetworkFiles(networkFiles);
			this.observer.setChanged();
			this.observer.notifyObservers("UpdateNetworkFiles");
		}
	}
	
	/**
	 * Create the fileAgent.
	 */
	public void createFileAgent()
	{
		this.agentM.createFileAgent();
	}
	
	public void createFailureAgent(Node failedNode)
	{
		this.agentM.createFailureAgent(failedNode);
	}
	
	/**
	 * Set this node as the file agent's master.
	 */
	public void setFileAgentMaster()
	{
		this.agentM.setFileAgentMaster(true);
	}
	
	/**
	 * Assign the node as the file agent's master.
	 * @param node The node.
	 * @param state the new status of the node as master
	 * @return True if successful, false otherwise.
	 */
	public boolean assignFileAgentMaster(Node node, boolean state)
	{
		return this.agentM.assignFileAgentMaster(node, state);
	}
	
	/**
	 * Prints to the Terminal.
	 * @param message
	 */
	public void printTerminal(String message)
	{
		terminal.printTerminal(message);
	}
	
	/**
	 * Prints info message on the Terminal.
	 * @param message
	 */
	public void printTerminalInfo(String message)
	{
		terminal.printTerminalInfo(message);
	}
	
	/**
	 * Prints error message on the Terminal.
	 * @param message
	 */
	public void printTerminalError(String message)
	{
		terminal.printTerminalError(message);
	}
	
	/**
	 * Returns the value of activeSession.
	 * @return	boolean
	 */
	public boolean returnActiveSession()
	{
		return activeSession;
	}
	
	/**
	 * Get the system's version.
	 * @return The Version.
	 */
	public String getVersion()
	{
		return version;
	}
	
	/**
	 * Pings the given node.
	 * @param the node to be pinged.
	 * @return True if connection succeed, otherwise false.
	 */
	public boolean ping(Node node)
	{
		String bindLocation = "//" + node.getIpAddress() + "/NodeLinkManager_" + node.getHostname();

		try 
		{
			NodeLinkManagerInterface iFace = (NodeLinkManagerInterface)this.getRMIInterface(bindLocation);		//If RMI succeed then the node is still online
			iFace.getNext();
		} 
		catch(Exception e)
		{
			System.err.println("NodeLinkManager exception: "+ e.getMessage());
			nodeConnectionFailure(node.getHostname());
			return false;
		}
		return true;
	}
	
	/**
	 * Start RMI and multicast services.
	 * @return True if connection succeed, otherwise false.
	 */
	private boolean setupServices()
	{
		boolean ready = true;
		
		//Start RMI-server
		if(!iFace.startRMIServer())
		{
			ready = false;
		}
						
		//Start multicastservice
		if(!iFace.setupMulticastservice())
		{
			ready = false;
		}
		
		return ready;
	}
	
	/**
	 * TESTMETHODE
	 */
	public void TESTprintLinkedNodes()
	{
		System.out.println("Prev: " + this.nodeLinkManager.getPrev().getHostname() + " - HASH: " + this.nodeLinkManager.getPrev().getHash());
		System.out.println("This: " + this.nodeLinkManager.getMyHostname());
		System.out.println("Next: " + this.nodeLinkManager.getNext().getHostname() + " - HASH: " + this.nodeLinkManager.getNext().getHash());
	}
	
	public void TESTprintOwnerFiles()
	{
		ArrayList<FileProperties> list = this.fileManager.getOwnedOwnerFiles();
		
		for(FileProperties fp : list)
		{
			System.out.println("Owner of file: " + fp.getFilename());
			if(fp.getReplicationLocation() != null)
			{
				System.out.println("Replication location: " + fp.getReplicationLocation().getHostname());
			}
			System.out.println("Download locations:");
			for(Node node: fp.getDownloadLocations())
			{
				System.out.println("- " + node.getHostname());
			}
			System.out.println("-------------------------------------------------");
		}
		
		ArrayList<String> repList = this.fileManager.getReplicatedFiles();
		System.out.println("Replicated files:");
		for(String f : repList)
		{
			System.out.println("- " + f);
		}
		System.out.println("_____________________________________________");
	}
}
