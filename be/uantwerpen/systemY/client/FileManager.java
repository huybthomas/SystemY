package be.uantwerpen.systemY.client;

import java.util.HashMap;

import be.uantwerpen.systemY.fileSystem.FileSystemManager;
import be.uantwerpen.systemY.shared.FileProperties;
import be.uantwerpen.systemY.shared.Node;

public class FileManager
{
	private HashMap<Integer, FileProperties> ownedFiles;
	
	public FileManager(String fileDirectory)
	{
		this.ownedFiles = new HashMap<Integer, FileProperties>();
	}
	
	public FileProperties getFile(String fileName)
	{
		return this.ownedFiles.get(new FileProperties(fileName, null).getHash());
	}
	
	public boolean addFile(String fileName, Node node)
	{
		if(!checkFileExistence(fileName))
		{
			FileProperties file = new FileProperties(fileName, node);
			this.ownedFiles.put(file.getHash(), file);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean addFile(FileProperties file)
	{
		if(!checkFileExistence(file.getFilename()))
		{
			this.ownedFiles.put(file.getHash(), file);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean delFile(String fileName)
	{
		if(checkFileExistence(fileName))
		{
			this.ownedFiles.remove(new FileProperties(fileName, null).getHash());
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean checkFileExistence(String fileName)
	{
		if(getFile(fileName) != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
