package be.uantwerpen.systemY.client.agent;

import java.io.Serializable;

public class AgentFileEntry implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String lockNode;
	private boolean available;
	
	public AgentFileEntry()
	{
		this.lockNode = null;
		this.available = true;
	}
	
	/**
	 * Puts a new file in the list on the agent.
	 * @param lockNode	The node who locked the file.
	 */
	public AgentFileEntry(String lockNode)
	{
		this.lockNode = lockNode;
		this.available = true;
	}
	
	/**
	 * Get the node who locked the file.
	 * @return	Get the name of the node who locked the file or null if the file is not locked.
	 */
	public String getLock()
	{
		return this.lockNode;
	}
	
	/**
	 * Set a lock to the file.
	 * @param lockNode	The name of the node who locks the file.
	 */
	public void setLock(String lockNode)
	{
		this.lockNode = lockNode;
	}
	
	/**
	 * Unlock the file.
	 */
	public void unlock()
	{
		this.lockNode = null;
	}
	
	/**
	 * Set the file's availability.
	 * @param available availability.
	 */
	public void setAvailability(boolean available)
	{
		this.available = available;
	}
	
	/**
	 * Get the file's availability.
	 * @return True if available, false otherwise.
	 */
	public boolean getAvailability()
	{
		return this.available;
	}
}
