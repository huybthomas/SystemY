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
		private String fileName;
		
		public FileNotification(String event, String fileName)
		{
			this.event = event;
			this.fileName = fileName;
		}
		
		public String getEvent()
		{
			return this.event;
		}
		
		/**
		 * Get the name of a file
		 * @return	The name of the file.
		 */
		public String getFileName()
		{
			return this.fileName;
		}
	}
}
