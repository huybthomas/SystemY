package be.uantwerpen.systemY.client.downloadSystem;

import java.util.ArrayList;
import java.util.HashMap;

import be.uantwerpen.systemY.shared.HashFunction;
import be.uantwerpen.systemY.shared.Node;

public class FileInventoryManager
{
	private HashMap<Integer, FileProperties> ownedFiles;
	private ArrayList<String> localFiles;
	private ArrayList<String> replicatedFiles;
	private ArrayList<String> networkFiles;
	
	public FileInventoryManager()
	{
		ownedFiles = new HashMap<Integer, FileProperties>();
		localFiles = new ArrayList<String>();
		replicatedFiles = new ArrayList<String>();
		networkFiles = new ArrayList<String>();
	}
	
	public void resetFileLists()
	{
		ownedFiles = new HashMap<Integer, FileProperties>();
		localFiles = new ArrayList<String>();
		replicatedFiles = new ArrayList<String>();
		networkFiles = new ArrayList<String>();
	}
	
	public FileProperties getOwnerFile(String fileName)
	{
		return this.ownedFiles.get(new FileProperties(fileName, null).getHash());
	}
	
	public ArrayList<String> getOwnedFiles()
	{
		ArrayList<String> ownedFilesList = new ArrayList<String>();
		
		synchronized(ownedFiles)
		{
			for(FileProperties f : this.ownedFiles.values())
			{
				ownedFilesList.add(f.getFilename());
			}
		}
		
		return ownedFilesList;
	}
	
	public ArrayList<FileProperties> getOwnedOwnerFiles()
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
			synchronized(ownedFiles)
			{
				this.ownedFiles.put(file.getHash(), file);
			}
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
			synchronized(ownedFiles)
			{
				this.ownedFiles.put(file.getHash(), file);
			}
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
			synchronized(ownedFiles)
			{
				this.ownedFiles.remove(new HashFunction().getHash(fileName));
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean checkOwnerFileExist(String fileName)
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
	
	public void setLocalFiles(ArrayList<String> localFiles)
	{
		synchronized(localFiles)
		{
			this.localFiles = localFiles;
		}
	}
	
	public ArrayList<String> getLocalFiles()
	{
		return this.localFiles;
	}
	
	public boolean canBeDeleted(String fileName) 
	{
		if(localFiles.contains(fileName) || replicatedFiles.contains(fileName) || checkOwnerFileExist(fileName))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public boolean addLocalFile(String fileName)
	{
		if(!this.localFiles.contains(fileName))
		{
			synchronized(localFiles)
			{
				this.localFiles.add(fileName);
			}
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
			synchronized(localFiles)
			{
				this.localFiles.remove(fileName);
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public ArrayList<String> getReplicatedFiles()
	{
		return this.replicatedFiles;
	}
	
	public boolean addReplicatedFile(String fileName)
	{
		if(!this.replicatedFiles.contains(fileName))
		{
			synchronized(replicatedFiles)
			{
				this.replicatedFiles.add(fileName);
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean delReplicatedFile(String fileName)
	{
		if(this.replicatedFiles.contains(fileName))
		{
			synchronized(replicatedFiles)
			{
				this.replicatedFiles.remove(fileName);
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void setNetworkFiles(ArrayList<String> networkFiles)
	{
		synchronized(networkFiles)
		{
			this.networkFiles = networkFiles;
		}
	}
	
	public ArrayList<String> getNetworkFiles()
	{
		return this.networkFiles;
	}
}
