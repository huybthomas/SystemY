package be.uantwerpen.systemY.interfaces;

import java.rmi.*;

import be.uantwerpen.systemY.client.agent.FailureAgent;
import be.uantwerpen.systemY.client.agent.FileAgent;
import be.uantwerpen.systemY.client.agent.FileDeletionAgent;

/**
 *	Interface to the AgentManager class of a Client.
 */
public interface AgentManagerInterface extends Remote
{
	/**
	 * Receive the file agent from the previous node and run it.
	 * @param agent	The FileAgent object.
	 * @throws RemoteException
	 */
	public void fileAgentReceived(FileAgent agent) throws RemoteException;
	
	/**
	 * Receive the failure agent form the previous node and run it.
	 * @param agent	The FailureAgent object.
	 * @throws RemoteException
	 */
	public void failureAgentReceived(FailureAgent agent) throws RemoteException;
	
	/**
	 * Receive the file deletion agent from the previous node and run it.
	 * @param agent	The FileDeletionAgent object.
	 * @throws RemoteException
	 */
	public void fileDeletionAgentReceived(FileDeletionAgent agent) throws RemoteException;
	
	/**
	 * Set the agent manager as file agent master or not.
	 * @param isMaster	True if master, false if not.
	 * @throws RemoteException
	 */
	public void setFileAgentMaster(boolean isMaster) throws RemoteException;
}
