package be.uantwerpen.systemY.client.agent;

public class AgentFileEntry 
{
	
	private String name;
	private boolean lock;
	
	public AgentFileEntry(String name, boolean lock)
	{
		this.name = name;
		this.lock = lock;
	}

	public String getName()
	{
		return this.name;
	}
	
	public boolean getLock()
	{
		return this.lock;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setLock(boolean lock)
	{
		this.lock = lock;
	}
}
