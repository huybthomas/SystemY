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
	
	/**
	 * Create a new file inventory manager.
	 */
	public FileInventoryManager()
	{
		ownedFiles = new HashMap<Integer, FileProperties>();
		localFiles = new ArrayList<String>();
		replicatedFiles = new ArrayList<String>();
		networkFiles = new ArrayList<String>();
	}
	
	/**
	 * Reset the file Lists.
	 */
	public void resetFileLists()
	{
		ownedFiles = new HashMap<Integer, FileProperties>();
		localFiles = new ArrayList<String>();
		replicatedFiles = new ArrayList<String>();
		networkFiles = new ArrayList<String>();
	}
	
	/**
	 * Get an owner file.
	 * @param fileName The file of which to get the owner file.
	 * @return The file properties of the requested file.
	 */
	public FileProperties getOwnerFile(String fileName)
	{
		return this.ownedFiles.get(new FileProperties(fileName, null).getHash());
	}
	
	/**
	 * Get the owned files.
	 * @return The owned files.
	 */
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
	
	/**
	 * Get the owned owner files.
	 * @return The owned owner files.
	 */
	public ArrayList<FileProperties> getOwnedOwnerFiles()
	{
		ArrayList<FileProperties> ownedFilesList = new ArrayList<FileProperties>();
		ownedFilesList.addAll(this.ownedFiles.values());
		return ownedFilesList;
	}
	
	/**
	 * Add an owner file.
	 * @param fileName The name of the file.
	 * @param node The node that owns the file.
	 * @return True if successful, false otherwise.
	 */
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
	
	/**
	 * Add an owner file.
	 * @param file The file.
	 * @return True if successful, false otherwise.
	 */
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
	
	/**
	 * Delete an owner file.
	 * @param fileName The file's name.
	 * @return True if successful, false otherwise.
	 */
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
	
	/**
	 * Check if an owner file exists for a given file.
	 * @param fileName The file's name.
	 * @return True if successful, false otherwise.
	 */
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
	
	/**
	 * Set the local files.
	 * @param localFiles The local files.
	 */
	public void setLocalFiles(ArrayList<String> localFiles)
	{
		synchronized(localFiles)
		{
			this.localFiles = localFiles;
		}
	}
	
	/**
	 * Get the local files.
	 * @return The local files.
	 */
	public ArrayList<String> getLocalFiles()
	{
		return this.localFiles;
	}
	
	/**
	 * Check if a file can be deleted.
	 * @param fileName The file's name.
	 * @return True if successful, false otherwise.
	 */
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
	
	/**
	 * Add a local file.
	 * @param fileName The file's name.
	 * @return True if successful, false otherwise.
	 */
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
	
	/**
	 * Delete the local files.
	 * @param fileName The file's name.
	 * @return True if successful, false otherwise.
	 */
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
	
	/**
	 * Get the replicated file list.
	 * @return The replicated file list.
	 */
	public ArrayList<String> getReplicatedFiles()
	{
		return this.replicatedFiles;
	}
	
	/**
	 * Add a file to the list of replicated files.
	 * @param fileName The file's name.
	 * @return True if successful, false otherwise.
	 */
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
	
	/**
	 * Delete a replicated file.
	 * @param fileName The file's name.
	 * @return True if successful, false otherwise.
	 */
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
	
	/**
	 * Set the network's files
	 * @param networkFiles The network's files.
	 */
	public void setNetworkFiles(ArrayList<String> networkFiles)
	{
		synchronized(networkFiles)
		{
			this.networkFiles = networkFiles;
		}
	}
	
	/**
	 * Get the network's files.
	 * @return The network's files.
	 */
	public ArrayList<String> getNetworkFiles()
	{
		return this.networkFiles;
	}
}
