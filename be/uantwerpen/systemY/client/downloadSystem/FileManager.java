package be.uantwerpen.systemY.client.downloadSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;

import be.uantwerpen.systemY.client.Client;
import be.uantwerpen.systemY.fileSystem.FileSystemManager;
import be.uantwerpen.systemY.fileSystem.FileSystemObserver;
import be.uantwerpen.systemY.interfaces.FileManagerInterface;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.networkservices.TCPConnection;
import be.uantwerpen.systemY.networkservices.TCPObserver;
import be.uantwerpen.systemY.shared.Node;

public class FileManager extends UnicastRemoteObject implements FileManagerInterface
{
	private static final long serialVersionUID = 1L;
	
	private FileSystemManager fileSystemManager;
	private FileInventoryManager fileInventoryManager;
	private FileTransferManager fileTransferManager;
	private DownloadManager downloadManager;
	private Client client;
	private String downloadLocation;
	
	public FileManager(TCPObserver tcpObserver, Client client) throws RemoteException
	{
		this.downloadLocation = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + File.separator + "SystemY" + File.separator + "Files";
		this.client = client;
		this.fileSystemManager = new FileSystemManager();
		this.fileInventoryManager = new FileInventoryManager();
		this.fileTransferManager = new FileTransferManager(this);
		this.downloadManager = new DownloadManager(this);
		
		tcpObserver.addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				downloadRequest((TCPConnection) object);
			}
		});
		
		fileSystemManager.getFileWatchObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				fileChangeDetected((FileSystemObserver.FileNotification)object);
			}
		});
	}
	
	public boolean startService()
	{
		fileSystemManager.startFileWatcher();
		return client.bindRMIservice(this, "FileManager_" + client.getHostname());
	}
	
	public boolean stopService()
	{
		fileSystemManager.stopFileWatcher();
		return client.unbindRMIservice("FileManager_" + client.getHostname());
	}
	
	//File owner section
	public FileProperties getOwnerFile(String fileName)
	{
		return this.fileInventoryManager.getOwnerFile(fileName);
	}
	
	public boolean addOwnerFile(String fileName, Node ownerNode)
	{
		return this.fileInventoryManager.addOwnerFile(fileName, ownerNode);
	}
	
	public boolean addOwnerFile(FileProperties file)
	{
		return this.fileInventoryManager.addOwnerFile(file);
	}
	
	public boolean delOwnerFile(String fileName)
	{
		return this.fileInventoryManager.delOwnerFile(fileName);
	}
	
	public ArrayList<FileProperties> getOwnedOwnerFiles()
	{
		return this.fileInventoryManager.getOwnedOwnerFiles();
	}
	
	public ArrayList<String> getOwnedFiles()
	{
		return this.fileInventoryManager.getOwnedFiles();
	}
	
	public boolean checkFileOwned(String fileName)
	{
		return this.fileInventoryManager.checkOwnerFileExist(fileName);
	}
	
	//File replication section
	public ArrayList<String> getReplicatedFile()
	{
		return this.fileInventoryManager.getReplicatedFiles();
	}
	
	//File local section
	public void setLocalFiles(ArrayList<String> localFiles)
	{
		this.fileInventoryManager.setLocalFiles(localFiles);
	}
	
	public ArrayList<String> getLocalFiles()
	{
		return this.fileInventoryManager.getLocalFiles();
	}
	
	public boolean addLocalFile(String fileName)
	{
		return this.fileInventoryManager.addLocalFile(fileName);
	}
	
	public boolean delLocalFile(String fileName)
	{
		return this.fileInventoryManager.delLocalFile(fileName);
	}
	
	//File network section
	public void setNetworkFiles(ArrayList<String> networkFiles)
	{
		this.fileInventoryManager.setNetworkFiles(networkFiles);
	}
	
	public ArrayList<String> getNetworkFiles()
	{
		return this.fileInventoryManager.getNetworkFiles();
	}
	
	public void importFile(File file) 
	{
		if(file != null)
		{
			if(!checkSystemFileExistence(file.getName()))
			{
				File newFile = new File(downloadLocation + File.separator + file.getName());
				try
				{
					Files.copy(file.toPath(), newFile.toPath());
				}
				catch(IOException e)
				{
					System.err.println("Can't copy file to SystemY folder location: " + e.getMessage());
				}
				
			}
			else
			{
				System.out.println("File already exists!");
			}
		}
	}
	
	//File system section
	public boolean checkSystemFileExistence(String fileName)
	{
		return fileSystemManager.fileExist(downloadLocation, fileName);
	}
	
	public boolean createNewSystemFile(String fileName) throws IOException
	{
		return fileSystemManager.createFile(downloadLocation, fileName);
	}
	
	public boolean openFile(String fileName) 
	{
		if(fileSystemManager.fileExist(downloadLocation, fileName)) 
		{
			try 
			{
				if(this.getOwnerFile(fileName) == null)		//When node is no owner of the file, add himself as downloadlocation (needed for replication location)
				{
					try
					{
						NodeManagerInterface iFace = (NodeManagerInterface) getNodeServerInterface();
						Node ownerNode = iFace.getFileLocation(fileName);
						
						this.updateDownloadLocation(ownerNode, fileName);
					}
					catch(NullPointerException | RemoteException e)
					{
						System.err.println("Can't contact server for owner location: " + e.getMessage());
						client.serverConnectionFailure();
					}
				}
				
				return fileSystemManager.openFile(downloadLocation, fileName);
			} 
			catch(IOException e) 
			{
				System.err.println("Can't open the file: " + fileName + ".");
				System.err.println(e.getMessage());
				return false;
			}
		} 
		else 
		{
			downloadFile(fileName);
			return true;
		}
	}
	
	public boolean canBeDeleted(String fileName) 
	{
		return fileInventoryManager.canBeDeleted(fileName);
	}
	
	public boolean deleteSystemFile(String fileName) throws IOException
	{
		return fileSystemManager.deleteFile(downloadLocation, fileName);
	}
	
	public boolean deleteFileRequest(String fileName)
	{
		if(!fileInventoryManager.getLocalFiles().contains(fileName))
		{
			try
			{
				return deleteSystemFile(fileName);
			}
			catch(IOException e)
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	public void deleteFilesFromNetwork(ArrayList<String> files)
	{
		client.deleteFilesFromNetwork(files);
	}
	
	public boolean deleteFilesFromSystem(ArrayList<String> deleteFileRequests) 
	{
		boolean status = true;
		
		if(deleteFileRequests != null) 
		{
			for(String fileName : deleteFileRequests) 
			{
				ArrayList<String> localfiles = fileInventoryManager.getLocalFiles();
				if(localfiles != null)
				{
					boolean isLocal = localfiles.contains(fileName);
					fileInventoryManager.delOwnerFile(fileName);
					fileInventoryManager.delReplicatedFile(fileName);
					
					if(!isLocal)
					{
						try
						{
							deleteSystemFile(fileName);
						}
						catch(IOException e)
						{
							System.err.println("Could not delete file: " + e.getMessage());
							status = false;
						}
					}
				} 
				else 
				{
					return false;
				}
			}
		}
		return status;
	}
	
	public FileOutputStream getFileOutputStream(String fileName) throws FileNotFoundException
	{
		return fileSystemManager.getFileOutputStream(downloadLocation, fileName);
	}
	
	public File getSystemFile(String file)
	{
		return fileSystemManager.getFile(downloadLocation, file);
	}
	
	public boolean shutdownFileClear()
	{
		boolean status = true;
		
		for(File f: this.getLocalSystemFiles())
		{
			String fileName = f.getName();
			
			if(this.getNetworkFiles().contains(fileName))
			{
				if(!this.getLocalFiles().contains(fileName))
				{
					try
					{
						this.deleteSystemFile(fileName);
					}
					catch(IOException e)
					{
						System.err.println("Could not delete file: " + e.getMessage());
						status = false;
					}
				}
			}
		}
		return status;
	}
	
	//File replication locations section
	public boolean addDownloadLocation(String fileName, Node downloadNode)
	{
		FileProperties fileProperties = getOwnerFile(fileName);
		
		if(fileProperties != null)
		{
			return fileProperties.addDownloadLocation(downloadNode);
		}
		else
		{
			return false;
		}
	}
	
	public boolean delDownloadLocation(String fileName, Node downloadNode)
	{
		FileProperties fileProperties = getOwnerFile(fileName);
		
		if(fileProperties != null)
		{
			return fileProperties.delDownloadLocation(downloadNode);
		}
		else
		{
			return false;
		}
	}
	
	public boolean setReplicationLocation(String fileName, Node replicationNode)
	{
		FileProperties fileProperties = getOwnerFile(fileName);
		
		if(fileProperties != null)
		{
			fileProperties.setReplicationLocation(replicationNode);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//File transfer action section
	public boolean bootTransfer()
	{
		return fileTransferManager.bootTransfer();
	}
	
	public void discoveryTransfer()
	{
		fileTransferManager.discoveryTransfer();
	}
	
	public boolean shutdownTransfer()
	{
		return fileTransferManager.shutdownTransfer();
	}
	
	public boolean shutdownFileUpdate()
	{
		return fileTransferManager.shutdownFileUpdate();
	}
	
	public void ownerSwitchFile(String fileName, Node ownerNode)
	{
		Download newDownload = new Download(downloadManager, fileName, ownerNode, 1);		//Mode 1: file owner switch
		
		this.client.downloadRequest(newDownload);
	}
	
	public void replicateFile(String fileName, Node ownerNode)
	{
		Download newDownload = new Download(downloadManager, fileName, ownerNode, 2);		//Mode 2: replicate file
		
		this.client.downloadRequest(newDownload);
	}
	
	public void replicateFile(String fileName)
	{
		try
		{
			NodeManagerInterface iFace = (NodeManagerInterface) getNodeServerInterface();
			Node ownerNode = iFace.getFileLocation(fileName);
			
			Download newDownload = new Download(downloadManager, fileName, ownerNode, 2);	//Mode 2: replicate file
			
			this.client.downloadRequest(newDownload);
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't contact server for downloadlocation: " + e.getMessage());
			client.serverConnectionFailure();
		}
	}
	
	public void downloadFile(String fileName)
	{
		try
		{
			NodeManagerInterface iFace = (NodeManagerInterface) getNodeServerInterface();
			Node ownerNode = iFace.getFileLocation(fileName);
			
			Download newDownload = new Download(downloadManager, fileName, ownerNode, 3);	//Mode 3: download file
			
			this.client.downloadRequest(newDownload);
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't contact server for downloadlocation: " + e.getMessage());
			client.serverConnectionFailure();
		}
	}
	
	public void runDownload(Download download)
	{
		this.downloadManager.addDownload(download);
	}
	
	public void downloadFinished(Download download, boolean successful)
	{
		if(successful)
		{
			switch(download.getDownloadMode())
			{
				//File owner switch
				case 1: this.transferOwnerFile(download.getDownloadFileOwner(), download.getFileName());
						break;
				//Replicate file
				case 2:	this.updateReplicationLocation(download.getDownloadFileOwner(), download.getFileName());
						break;
				//Download file
				case 3:	this.updateDownloadLocation(download.getDownloadFileOwner(), download.getFileName());
						this.openFile(download.getFileName());
						break;
			}
		}
		else
		{
			this.client.printTerminalError("Download: '" + download.getFileName() + "' failed.");
		}
		client.downloadFinished(download.getFileName());
	}
	
	public int getDownloadsHosting()
	{
		return downloadManager.getDownloadsHosting();
	}
	
	public boolean transferOwnerFile(Node oldOwner, String fileName)
	{
		try
		{
			FileManagerInterface iFace = (FileManagerInterface) getFileManagerInterface(oldOwner);
			FileProperties transferedOwnerFile = iFace.getOwnerFile(fileName);
			
			if(transferedOwnerFile != null)
			{				
				this.addOwnerFile(transferedOwnerFile);
				
				iFace.delOwnerFile(fileName);
				
				if(!transferedOwnerFile.getDownloadLocations().contains(oldOwner) && !(transferedOwnerFile.getReplicationLocation() == null))
				{
					iFace.deleteFileRequest(fileName);
				}
				
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't contact node for owner switch: " + e.getMessage());
			client.nodeConnectionFailure(oldOwner.getHostname());
			return false;
		}
	}
	
	public boolean updateReplicationLocation(Node fileOwner, String fileName)
	{
		try
		{
			FileManagerInterface iFace = (FileManagerInterface) getFileManagerInterface(fileOwner);
			if(iFace.setReplicationLocation(fileName, client.getThisNode()))
			{
				fileInventoryManager.addReplicatedFile(fileName);
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't contact owner node for new replication location: " + e.getMessage());
			client.nodeConnectionFailure(fileOwner.getHostname());
			return false;
		}
	}
	
	public boolean updateDownloadLocation(Node fileOwner, String fileName)
	{
		try
		{
			FileManagerInterface iFace = (FileManagerInterface) getFileManagerInterface(fileOwner);
			return iFace.addDownloadLocation(fileName, this.getThisNode());
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't contact owner node for new download location: " + e.getMessage());
			client.nodeConnectionFailure(fileOwner.getHostname());
			return false;
		}
	}
	
	public boolean deleteDownloadLocation(Node fileOwner, String fileName)
	{
		try
		{
			FileManagerInterface iFace = (FileManagerInterface) getFileManagerInterface(fileOwner);
			return iFace.delDownloadLocation(fileName, this.getThisNode());
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't contact owner node for deleting download location: " + e.getMessage());
			client.nodeConnectionFailure(fileOwner.getHostname());
			return false;
		}
	}
	
	public boolean deleteDownloadLocation(String fileName)
	{
		try
		{
			NodeManagerInterface iFaceServer = (NodeManagerInterface) client.getNodeServerInterface();
			Node fileOwner = iFaceServer.getFileLocation(fileName);
			
			try
			{
				if(fileOwner != null)
				{
					FileManagerInterface iFace = (FileManagerInterface) getFileManagerInterface(fileOwner);
					return iFace.delDownloadLocation(fileName, this.getThisNode());
				}
				else
				{
					System.err.println("Can't find owner node for deleting download location.");
					return false;
				}
			}
			catch(NullPointerException | RemoteException e)
			{
				System.err.println("Can't contact owner node for deleting download location: " + e.getMessage());
				client.nodeConnectionFailure(fileOwner.getHostname());
				return false;
			}
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't contact server for deleting download location: " + e.getMessage());
			client.serverConnectionFailure();
			return false;
		}
		
	}
	
	public void resetFileLists()
	{
		this.fileInventoryManager.resetFileLists();
	}
	
	public boolean nodeConnectionFailure(String hostname)
	{
		return client.nodeConnectionFailure(hostname);
	}
	
	public void serverConnectionFailure()
	{
		client.serverConnectionFailure();
	}
	
	//RMI section
	public Object getNodeServerInterface()
	{
		return client.getNodeServerInterface();
	}
	
	public Object getFileManagerInterface(Node node)
	{
		return client.getFileManagerInterface(node);
	}
	
	public Object getNodeLinkInterface(Node node)
	{
		return client.getNodeLinkInterface(node);
	}
	
	//TCP section
	public TCPConnection getTCPConnection(String destinationIP)
	{
		return client.getTCPConnection(destinationIP);
	}
	
	public boolean fileExist(String location, String name)
	{
		return fileSystemManager.fileExist(location, name);
	}
	
	public File getFile(String location, String name)
	{
		return fileSystemManager.getFile(location, name);
	}
	
	public FileInputStream getFileInputStream(String location, String name) throws FileNotFoundException
	{
		return fileSystemManager.getFileInputStream(location, name);
	}
	
	public void downloadRequest(TCPConnection connection)
	{
		downloadManager.downloadRequest(connection);
	}
	
	public int getQueuedDownloads()
	{
		return downloadManager.getQueuedDownloads();
	}
	
	public String getDownloadLocation()
	{
		return this.downloadLocation;
	}
	
	public boolean setDownloadLocation(String location)
	{
		try
		{
			File newLocation = new File(location);
			
			if(newLocation.isDirectory())
			{
				this.downloadLocation = location;
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(NullPointerException e)
		{
			return false;
		}
	}
	
	public void printTerminalError(String message)
	{
		client.printTerminalError(message);
	}
	
	public File[] getLocalSystemFiles() 
	{
		File directory = fileSystemManager.getDirectory(downloadLocation);
		
		if(directory == null)
		{
			fileSystemManager.createDirectory(downloadLocation);
			directory = fileSystemManager.getDirectory(downloadLocation);
		}
		
		return directory.listFiles().clone();
	}
	
	public Node getThisNode()
	{
		return this.client.getThisNode();
	}
	
	public Node getPrevNode()
	{
		return this.client.getPrevNode();
	}
	
	public Node getNextNode()
	{
		return this.client.getNextNode();
	}
	
	//File change detection section
	private void fileChangeDetected(FileSystemObserver.FileNotification notification)
	{
		switch(notification.getEvent())
		{
			case "ENTRY_CREATE":
				String fileName = notification.getFileName();
				
				if(this.client.getSessionState())
				{
					if(this.fileExist(downloadLocation, fileName))
					{
						if(!this.getNetworkFiles().contains(fileName))
						{
							ArrayList<String> downloads = downloadManager.getRunningDownloads();
							
							//Check if filecreation is from running download
							for(String download : downloads)
							{
								if(download.equals(fileName))
								{
									return;			//File creation is from running download
								}
							}
							
							//Add file to owned and local files
							addOwnerFile(fileName, client.getThisNode());
							addLocalFile(fileName);
							
							//Start the transfer procedure to the system
							ArrayList<String> fileList = new ArrayList<String>();
							fileList.add(fileName);
							
							this.fileTransferManager.fileReplicationTransfer(fileList);
						}
					}
				}
				break;
				
			case "ENTRY_DELETE":
				//Implementation later on
				break;
				
			case "ENTRY_MODIFY":
				//Implementation later on
				break;
		}
	}
}
