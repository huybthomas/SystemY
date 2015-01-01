package be.uantwerpen.systemY.client.agent;

import java.io.Serializable;
import java.util.Observable;

public class AgentObserver extends Observable implements Serializable
{
	private static final long serialVersionUID = 1L;

	public void clearChanged()
	{
		super.clearChanged();
	}
	
	public void setChanged()
	{
		super.setChanged();
	}
}