package be.uantwerpen.systemY.client;

import java.util.Observable;

public class ClientObserver extends Observable 
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
