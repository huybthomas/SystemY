package be.uantwerpen.systemY.fileSystem;

import java.util.Observable;

public class FileSystemObserver extends Observable
{
	public void clearChanged()
	{
		super.clearChanged();
	}
	
	public void setChanged()
	{
		super.setChanged();
	}
	
	public class FileNotification
	{
		private String event;
		private String fileLocation;
		
		public FileNotification(String event, String fileLocation)
		{
			this.event = event;
			this.fileLocation = fileLocation;
		}
		
		public String getEvent()
		{
			return this.event;
		}
		
		public String getFileLocation()
		{
			return this.fileLocation;
		}
	}
}
