package be.uantwerpen.systemY.interfaces;

import java.rmi.*;

import be.uantwerpen.systemY.client.agent.FailureAgent;
import be.uantwerpen.systemY.client.agent.FileAgent;
import be.uantwerpen.systemY.client.agent.FileDeletionAgent;

public interface AgentManagerInterface extends Remote
{
	public void fileAgentReceived(FileAgent agent) throws RemoteException;
	public void failureAgentReceived(FailureAgent agent) throws RemoteException;
	public void fileDeletionAgentReceived(FileDeletionAgent agent) throws RemoteException;
	public void setFileAgentMaster(boolean isMaster) throws RemoteException;
}
