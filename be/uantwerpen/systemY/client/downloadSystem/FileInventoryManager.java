package be.uantwerpen.systemY.client.downloadSystem;

import java.util.ArrayList;
import java.util.HashMap;

import be.uantwerpen.systemY.shared.Node;

public class FileInventoryManager
{
	private HashMap<Integer, FileProperties> ownedFiles;
	private ArrayList<String> localFiles;
	private ArrayList<String> networkFiles;
	
	public FileInventoryManager()
	{
		ownedFiles = new HashMap<Integer, FileProperties>();
		localFiles = new ArrayList<String>();
		networkFiles = new ArrayList<String>();
	}
	
	public FileProperties getOwnerFile(String fileName)
	{
		return this.ownedFiles.get(new FileProperties(fileName, null).getHash());
	}
	
	public ArrayList<FileProperties> getOwnedFiles()
	{
		ArrayList<FileProperties> ownedFilesList = new ArrayList<FileProperties>();
		ownedFilesList.addAll(this.ownedFiles.values());
		return ownedFilesList;
	}
	
	public boolean addOwnerFile(String fileName, Node node)
	{
		if(!checkOwnerFileExist(fileName))
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
	
	public boolean addOwnerFile(FileProperties file)
	{
		if(!checkOwnerFileExist(file.getFilename()))
		{
			this.ownedFiles.put(file.getHash(), file);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean delOwnerFile(String fileName)
	{
		if(checkOwnerFileExist(fileName))
		{
			this.ownedFiles.remove(new FileProperties(fileName, null).getHash());
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void setLocalFiles(ArrayList<String> localFiles)
	{
		this.localFiles = localFiles;
	}
	
	public ArrayList<String> getLocalFiles()
	{
		return this.localFiles;
	}
	
	public boolean addLocalFile(String fileName)
	{
		if(!this.localFiles.contains(fileName))
		{
			this.localFiles.add(fileName);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean delLocalFile(String fileName)
	{
		if(this.localFiles.contains(fileName))
		{
			this.localFiles.remove(fileName);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void setNetworkFiles(ArrayList<String> networkFiles)
	{
		this.networkFiles = networkFiles;
	}
	
	public ArrayList<String> getNetworkFiles()
	{
		return this.networkFiles;
	}
	
	private boolean checkOwnerFileExist(String fileName)
	{
		if(getOwnerFile(fileName) != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
