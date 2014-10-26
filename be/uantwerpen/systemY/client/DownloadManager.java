package be.uantwerpen.systemY.client;

import java.io.File;

public class DownloadManager 
{
	private String fileLocation;
		
	public DownloadManager(String fileLocation)
	{
		this.fileLocation = fileLocation;
	}
	
	public File getFile(String fileName)
	{
		File file = new File(fileLocation + fileName);
		if(file.exists())
		{
			return file;
		}
		else
		{
			return null;
		}
	}
}
