package be.uantwerpen.systemY.interfaces;

import java.rmi.*;

import be.uantwerpen.systemY.client.agent.FileAgent;

public interface AgentInterface extends Remote
{
	public void FileAgentReceived(FileAgent agent) throws RemoteException;
}
