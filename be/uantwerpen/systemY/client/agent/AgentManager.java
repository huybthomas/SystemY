package be.uantwerpen.systemY.client.agent;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import be.uantwerpen.systemY.client.Client;
import be.uantwerpen.systemY.interfaces.AgentInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.shared.Node;

public class AgentManager extends UnicastRemoteObject implements AgentInterface
{
	//RMI OBJECT DIE DE AGENTS ONTVANGT/DOORSTUURT ALS PARAMETER...
	private Client client;
	private FileAgent fileAgent;
	private FailureAgent failureAgent;
	private Node failedNode;
	
	public AgentManager(Client client) throws RemoteException
	{
		this.client = client;
		this.fileAgent = new FileAgent(this);
		this.failureAgent = new FailureAgent(this);
		// this.failedNode = blabla bla;
		
		//client.bindRMIservice(this, "Agent_" + client.getHostname());
	}

	//@Override //Override Nodig??
	public void FileAgentReceived(FileAgent agent) throws RemoteException 
	{
		fileAgent.FileAgentReceived(agent);
	}
	
	public void FileAgentNextNode()
	{
		AgentInterface iFace = (AgentInterface)client.getAgentManagerInterface(client.getNextNode());
		try {
			iFace.FileAgentReceived(this.fileAgent);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void FailureAgentNextNode()
	{
		/*
		AgentInterface iFace = (AgentInterface)client.getAgentManagerInterface(client.getNextNode());
		try {
			//Code die Q wilt activeren
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public void unbindRMI()
	{
		client.unbindRMIservice("Agent_" + client.getHostname());
	}
	
	
	public ArrayList<String> getLocalFiles()
	{
		return null; //client.getLocalFiles(); schrijven? 
	}
	
	/*
	public void setAvailableFiles(ArrayList<AgentFileEntry> files)
	{
		client.setAvailableFiles(files);
	}
	*/
	
	public Node getFailedNode()
	{
		return this.failedNode;
	}
	
	public Node getOwnerLocation(String fileName)
	{
		try
		{
			NodeManagerInterface iFace = (NodeManagerInterface) client.getNodeServerInterface();
			return iFace.getFileLocation(fileName);
		}
		catch(RemoteException e)
		{
			System.err.println("Can't contact server for filelocation: " + e.getMessage());
			client.serverConnectionFailure();
		}
		return null;
	}
	
	public Object getFileManagerInterface(Node node)
	{
		return client.getFileManagerInterface(node);
	}
	
	public void nodeConnectionFailure(String hostName)
	{
		client.nodeConnectionFailure(hostName);
	}
}
