package be.uantwerpen.systemY.client.downloadSystem;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;

import be.uantwerpen.systemY.interfaces.FileManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.shared.Node;

public class FileTransferManager
{
	private FileManager fileManager;
	
	public FileTransferManager(FileManager manager)
	{
		this.fileManager = manager;
	}
	
	public boolean bootTransfer()
	{
		fileManager.resetFileLists();
		
		File[] files = fileManager.getLocalSystemFiles();
		
		for(File f : files)
		{
			if(f.isFile())
			{
				fileManager.addOwnerFile(f.getName(), fileManager.getThisNode());
				fileManager.addLocalFile(f.getName());
			}
		}
		
		if(!fileManager.getPrevNode().equals(fileManager.getThisNode()))
		{
			//More than one node connected to the system
			try
			{
				FileManagerInterface iFace = (FileManagerInterface) this.fileManager.getFileManagerInterface(this.fileManager.getPrevNode());
				iFace.discoveryTransfer();
			}
			catch(RemoteException | NullPointerException e)
			{
				System.err.println("Can't contact previous node to start discovery transfer: " + e.getMessage());
				this.fileManager.nodeConnectionFailure(this.fileManager.getPrevNode().getHostname());
				return false;
			}
			
			fileReplicationTransfer(fileManager.getLocalFiles());
		}
		return true;
	}
	
	public void discoveryTransfer()
	{
		if(fileManager.getPrevNode().equals(fileManager.getNextNode()))		//When second node is connected to the network, all files on this system need to be replicated
		{
			fileReplicationTransfer(fileManager.getOwnedFiles());
		}
		else
		{
			for(FileProperties f : fileManager.getOwnedOwnerFiles())
			{
				if((f.getHash() > fileManager.getNextNode().getHash()) || (f.getHash() < fileManager.getThisNode().getHash()))
				{
					try
					{
						FileManagerInterface iFaceNode = (FileManagerInterface) fileManager.getFileManagerInterface(fileManager.getNextNode());
						
						iFaceNode.ownerSwitchFile(f.getFilename(), fileManager.getThisNode());
					}
					catch(NullPointerException | RemoteException e)
					{
						System.err.println("Failed to contact to node '" + fileManager.getNextNode().getHostname() + "': " + e.getMessage());
						fileManager.nodeConnectionFailure(fileManager.getNextNode().getHostname());
						continue;
					}
				}
			}
		}
	}
	
	public boolean shutdownFileUpdate()
	{
		ArrayList<String> deleteFiles = new ArrayList<String>();
		
		//Updating download locations or delete file from system if it's a local file that's not downloaded
		for(File f : fileManager.getLocalSystemFiles())
		{
			String fileName = f.getName();
			
			if(fileManager.getNetworkFiles().contains(fileName))
			{
				if(!fileManager.getOwnedFiles().contains(fileName)) 			//Only file not owned by this node
				{
					fileManager.deleteDownloadLocation(fileName);				//Delete download location from this node
				}
				
				if(fileManager.getLocalFiles().contains(fileName))
				{
					try
					{
						NodeManagerInterface iFaceServer = (NodeManagerInterface) fileManager.getNodeServerInterface();
						Node fileOwner = iFaceServer.getFileLocation(fileName);
						
						try
						{
							if(fileOwner != null)
							{
								FileManagerInterface iFaceNode = (FileManagerInterface) fileManager.getFileManagerInterface(fileOwner);
								FileProperties ownerFile = iFaceNode.getOwnerFile(fileName);
								
								if(ownerFile != null)
								{
									if(ownerFile.getDownloadLocations().size() == 0)
									{
										deleteFiles.add(fileName);
									}
								}
							}
							else
							{
								System.err.println("Can't find file owner node for checking the download locations.");
							}
						}
						catch(NullPointerException | RemoteException e)
						{
							System.err.println("Can't contact the file owner node for checking the download locations: " + e.getMessage());
							fileManager.nodeConnectionFailure(fileOwner.getHostname());
							return false;
						}
					}
					catch(NullPointerException | RemoteException e)
					{
						System.err.println("Can't contact the server for checking the download locations: " + e.getMessage());
						fileManager.serverConnectionFailure();
						return false;
					}
				}
			}
		}
		
		if(deleteFiles.size() > 0)
		{			
			fileManager.deleteFilesFromNetwork(deleteFiles);
		}
		return true;
	}
	
	public boolean shutdownTransfer()
	{
		Node prevPrevNode;

		if(!fileManager.getPrevNode().equals(fileManager.getThisNode()))		//Check if this node was the only node in the network
		{
			try
			{
				NodeLinkManagerInterface iFaceNodeLink = (NodeLinkManagerInterface) fileManager.getNodeLinkInterface(fileManager.getPrevNode());
				prevPrevNode = iFaceNodeLink.getPrev();
			}
			catch(NullPointerException | RemoteException e)
			{
				System.err.println("Can't contact previous node for nodelinkmanager: " + e.getMessage());
				fileManager.nodeConnectionFailure(fileManager.getPrevNode().getHostname());
				return false;
			}
			
			for(FileProperties f: fileManager.getOwnedOwnerFiles())
			{
				try
				{
					if(prevPrevNode.equals(fileManager.getPrevNode()))						//The previous node is the only node still left in the network, no replication location available
					{
						f.setReplicationLocation(null);
						FileManagerInterface iFaceFileManager = (FileManagerInterface) fileManager.getFileManagerInterface(fileManager.getPrevNode());
						iFaceFileManager.ownerSwitchFile(f.getFilename(), fileManager.getThisNode());
					}
					else
					{
						if(f.getReplicationLocation().equals(fileManager.getPrevNode()))	//Previous node is replication owner
						{
							FileManagerInterface iFaceFileManager = (FileManagerInterface) fileManager.getFileManagerInterface(fileManager.getPrevNode());
							iFaceFileManager.transferOwnerFile(fileManager.getThisNode(), f.getFilename());
							
							try
							{
								FileManagerInterface iFaceFileManagerPrev = (FileManagerInterface) fileManager.getFileManagerInterface(prevPrevNode);
								iFaceFileManagerPrev.replicateFile(f.getFilename());
							}
							catch(NullPointerException | RemoteException e)
							{
								System.err.println("Can't contact the previous node of the previous node to transfer the file: " + e.getMessage());
								fileManager.nodeConnectionFailure(prevPrevNode.getHostname());
								return false;
							}
						}
						else
						{
							FileManagerInterface iFaceFileManager = (FileManagerInterface) fileManager.getFileManagerInterface(fileManager.getPrevNode());
							iFaceFileManager.ownerSwitchFile(f.getFilename(), fileManager.getThisNode());
						}
					}
				}
				catch(NullPointerException | RemoteException e)
				{
					System.err.println("Can't contact previous node for filetransfer: " + e.getMessage());
					fileManager.nodeConnectionFailure(fileManager.getPrevNode().getHostname());
					return false;
				}
			}
			
			for(String f: fileManager.getReplicatedFile())
			{
				try
				{
					if(prevPrevNode.equals(fileManager.getPrevNode()))				//The previous node is the only node still left in the network, no replication location available
					{
						try
						{
							FileManagerInterface iFaceFileManagerPrev = (FileManagerInterface) fileManager.getFileManagerInterface(fileManager.getPrevNode());
							iFaceFileManagerPrev.setReplicationLocation(f, null);
						}
						catch(NullPointerException | RemoteException e)
						{
							System.err.println("Can't contact the previous node to change replication location: " + e.getMessage());
							fileManager.nodeConnectionFailure(fileManager.getPrevNode().getHostname());
						}
					}
					else
					{
						FileManagerInterface iFaceFileManager = (FileManagerInterface) fileManager.getFileManagerInterface(fileManager.getPrevNode());
						
						if(iFaceFileManager.getOwnerFile(f) != null)						//Previous node is owner of the file
						{
							try
							{
								FileManagerInterface iFaceFileManagerPrev = (FileManagerInterface) fileManager.getFileManagerInterface(prevPrevNode);
								iFaceFileManagerPrev.replicateFile(f);
							}
							catch(NullPointerException | RemoteException e)
							{
								System.err.println("Can't contact the previous node of the previous node to transfer the file: " + e.getMessage());
								fileManager.nodeConnectionFailure(prevPrevNode.getHostname());
							}
						}
						else
						{
							iFaceFileManager.replicateFile(f); 					//Previous node becomes new replication location
						}
					}
				}
				catch(NullPointerException | RemoteException e)
				{
					System.err.println("Can't contact previous node for filetransfer: " + e.getMessage());
					fileManager.nodeConnectionFailure(fileManager.getPrevNode().getHostname());
					return false;
				}
			}
		}
		return true;
	}
	
	public void fileReplicationTransfer(ArrayList<String> fileList)
	{
		synchronized(fileList)
		{
			try
			{			
				for(String file : fileList)
				{
					File f = fileManager.getSystemFile(file);
					
					try
					{
						if(f.isFile())
						{						
							Node ownerNode;
							try
							{
								NodeManagerInterface iFaceServer = (NodeManagerInterface) fileManager.getNodeServerInterface();
								
								ownerNode = iFaceServer.getFileLocation(f.getName());
							}
							catch(NullPointerException | RemoteException e)
							{
								System.err.println("Failed to contact the server: " + e.getMessage());
								fileManager.serverConnectionFailure();
								return;
							}
							
							try
							{
								//This node is owner, put replication on previous node
								if(ownerNode.equals(fileManager.getThisNode())) 
								{
									FileManagerInterface iFaceNode = (FileManagerInterface) fileManager.getFileManagerInterface(fileManager.getPrevNode());
		
									iFaceNode.replicateFile(f.getName(), fileManager.getThisNode());
								}
								//Other node should be owner, make him owner, make yourself replication location
								else
								{
									FileManagerInterface iFaceNode = (FileManagerInterface) fileManager.getFileManagerInterface(ownerNode);
									
									fileManager.getOwnerFile(f.getName()).setReplicationLocation(fileManager.getThisNode());
									
									iFaceNode.ownerSwitchFile(f.getName(), fileManager.getThisNode());
								}
							}
							catch(NullPointerException | RemoteException e)
							{
								System.err.println("Failed to contact to node '" + ownerNode.getHostname() + "': " + e.getMessage());
								fileManager.nodeConnectionFailure(ownerNode.getHostname());
								continue;
							}
						}
					}
					catch(NullPointerException e)
					{
						System.err.println("File '" + file + "' cannot be found: " + e.getMessage());
					}
				}
			}
			catch(SecurityException e)
			{
				System.err.println("Security Exception: " + e.getMessage());
				return;
			}
		}
	}
}
