package be.uantwerpen.systemY.client.agent;

import java.util.Observable;

public class FileAgentObserver extends Observable 
{
	public void clearChanged()
	{
		super.clearChanged();
	}
	
	public void setChanged()
	{
		super.setChanged();
	}
}