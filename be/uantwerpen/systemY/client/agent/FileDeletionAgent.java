package be.uantwerpen.systemY.client.agent;

import java.io.Serializable;
import java.util.ArrayList;

import be.uantwerpen.systemY.shared.Node;

public class FileDeletionAgent implements Runnable, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private AgentObserver observer;
	private AgentManager agentM;
	private Node startNode;
	private ArrayList<String> deleteFilesRequests;
	
	/**
	 * Create a file deletion agent.
	 * @param deleteFilesRequests The files requested for deletion.
	 * @param startNode The Node on which the agent starts.
	 * @param agentManager The agent's manager.
	 */
	public FileDeletionAgent(ArrayList<String> deleteFilesRequests, Node startNode, AgentManager agentManager)
	{
		this.agentM = agentManager;
		this.startNode = startNode;
		this.deleteFilesRequests = deleteFilesRequests;
		this.observer = new AgentObserver();
	}
	
	/**
	 * Get the agent's observer.
	 * @return
	 */
	public AgentObserver getObserver()
	{
		return this.observer;
	}
	
	/**
	 * Set the agent's manager.
	 * @param agentManager
	 */
	public void setManager(AgentManager agentManager)
	{
		this.agentM = agentManager;
	}
	
	/**
	 * Get this agent's starting node.
	 * @return The node.
	 */
	public Node getStartNode()
	{
		return this.startNode;
	}
	
	/**
	 * Runs the fileAgent when it arrives.
	 */
	@Override
	public void run() 
	{		
		agentM.deleteFilesFromSystem(deleteFilesRequests);
		
		//Prepare for transfer
		this.agentM = null;		//Delete reference to agent manager
		
		//FileAgent ready to continue
		observer.setChanged();
		observer.notifyObservers(this);
	}
}
