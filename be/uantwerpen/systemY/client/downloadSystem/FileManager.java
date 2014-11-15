package be.uantwerpen.systemY.client.downloadSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	private DownloadManager downloadManager;
	private Client client;
	private String downloadLocation;
	
	public FileManager(TCPObserver tcpObserver, Client client) throws RemoteException
	{
		this.downloadLocation = new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + File.separator + "SystemY" + File.separator + "Files";
		this.client = client;
		this.fileSystemManager = new FileSystemManager();
		this.fileInventoryManager = new FileInventoryManager();
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
		return client.bindRMIservice(this, "FileManager_" + client.getHostname());
	}
	
	public boolean stopService()
	{
		return client.unbindRMIservice("FileManager_" + client.getHostname());
	}
	
	//File owner section
	public FileProperties getOwnerFile(String fileName)
	{
		return this.fileInventoryManager.getOwnerFile(fileName);
	}
	
	public boolean addOwnerFile(String fileName, Node node)
	{
		return this.fileInventoryManager.addOwnerFile(fileName, node);
	}
	
	public boolean addOwnerFile(FileProperties file)
	{
		return this.fileInventoryManager.addOwnerFile(file);
	}
	
	public boolean delOwnerFile(String fileName)
	{
		return this.fileInventoryManager.delOwnerFile(fileName);
	}
	
	public ArrayList<FileProperties> getOwnedFiles()
	{
		return this.fileInventoryManager.getOwnedFiles();
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
	
	//File system section
	public boolean checkLocalFileExistence(String fileName)
	{
		return fileSystemManager.fileExist(downloadLocation, fileName);
	}
	
	public boolean createNewLocalFile(String fileName) throws IOException
	{
		return fileSystemManager.createFile(downloadLocation, fileName);
	}
	
	public boolean deleteLocalFile(String fileName) throws IOException
	{
		return fileSystemManager.deleteFile(downloadLocation, fileName);
	}
	
	public FileOutputStream getFileOutputStream(String fileName) throws FileNotFoundException
	{
		return fileSystemManager.getFileOutputStream(downloadLocation, fileName);
	}
	
	
	
	//File replication locations section
	public boolean addReplicationLocation(String fileName, Node replicationNode)
	{
		FileProperties fileProperties = getOwnerFile(fileName);
		
		if(fileProperties != null)
		{
			return fileProperties.addReplicationLocation(replicationNode);
		}
		else
		{
			return false;
		}
	}
	
	public boolean delReplicationLocation(String fileName, Node replicationNode)
	{
		FileProperties fileProperties = getOwnerFile(fileName);
		
		if(fileProperties != null)
		{
			return fileProperties.delReplicationLocation(replicationNode);
		}
		else
		{
			return false;
		}
	}
	
	public boolean updateReplicationLocation(Node fileOwner, String fileName)
	{
		try
		{
			FileManagerInterface iFace = (FileManagerInterface) getFileManagerInterface(fileOwner);
			return iFace.addReplicationLocation(fileName, client.getThisNode());
		}
		catch(RemoteException e)
		{
			System.err.println("Can't contact owner node for new replication location: " + e.getMessage());
			client.nodeConnectionFailure(fileOwner.getHostname());
			return false;
		}
	}
	
	//File transfer action section
	public void bootTransfer()
	{
		try
		{			
			File[] files = getLocalSystemFiles();
			
			for(File f : files)
			{
				if(f.isFile())
				{
					addOwnerFile(f.getName(), client.getThisNode());
					addLocalFile(f.getName());
					
					if(client.getPrevNode().equals(client.getThisNode()))
					{
						continue;		//Only one node connected to the system
					}
					
					Node ownerNode;
					try
					{
						NodeManagerInterface iFaceServer = (NodeManagerInterface) getNodeServerInterface();
						
						ownerNode = iFaceServer.getFileLocation(f.getName());
					}
					catch(RemoteException e)
					{
						System.err.println("Failed to contact the server: " + e.getMessage());
						client.serverConnectionFailure();
						return;
					}
					
					try
					{
						if(ownerNode.equals(client.getThisNode()))
						{
							FileManagerInterface iFaceNode = (FileManagerInterface) client.getFileManagerInterface(client.getPrevNode());

							iFaceNode.downloadFile(f.getName());
						}
						else
						{
							FileManagerInterface iFaceNode = (FileManagerInterface) client.getFileManagerInterface(ownerNode);
							
							iFaceNode.ownerSwitchFile(f.getName(), client.getThisNode());
						}
					}
					catch(RemoteException e)
					{
						System.err.println("Failed to contact to node '" + ownerNode.getHostname() + "': " + e.getMessage());
						client.nodeConnectionFailure(ownerNode.getHostname());
						continue;
					}
				}
			}
		}
		catch(SecurityException e)
		{
			System.err.println("Security Exception: " + e.getMessage());
			return;
		}
	}
	
	public void discoveryTransfer()
	{
		if(client.getPrevNode().equals(client.getNextNode()))		//When second node is connected to the network, all files on this system need to be replicated
		{
			bootTransfer();
		}
		else
		{
			for(FileProperties f : fileInventoryManager.getOwnedFiles())
			{
				if(f.getHash() > client.getNextNode().getHash())
				{
					try
					{
						FileManagerInterface iFaceNode = (FileManagerInterface) client.getFileManagerInterface(client.getNextNode());
						
						iFaceNode.ownerSwitchFile(f.getFilename(), client.getThisNode());
					}
					catch(RemoteException e)
					{
						System.err.println("Failed to contact to node '" + client.getNextNode().getHostname() + "': " + e.getMessage());
						client.nodeConnectionFailure(client.getNextNode().getHostname());
						continue;
					}
				}
			}
		}
	}
	
	public void shutdownTransfer()
	{
		for(FileProperties f: fileInventoryManager.getOwnedFiles())
		{
			//if()
		}
	}
	
	public void downloadFile(String fileName)
	{
		try
		{
			NodeManagerInterface iFace = (NodeManagerInterface) getNodeServerInterface();
			Node ownerNode = iFace.getFileLocation(fileName);
			
			Download newDownload = new Download(downloadManager, fileName, ownerNode, false);
			
			this.downloadManager.addDownload(newDownload);
		}
		catch(RemoteException e)
		{
			System.err.println("Can't contact server for downloadlocation: " + e.getMessage());
			client.serverConnectionFailure();
		}
	}
	
	public void ownerSwitchFile(String fileName, Node ownerNode)
	{
		Download newDownload = new Download(downloadManager, fileName, ownerNode, true);
		
		this.downloadManager.addDownload(newDownload);
	}
	
	public boolean transferOwnerFile(Node newOwner, String fileName)
	{
		try
		{
			FileManagerInterface iFace = (FileManagerInterface) getFileManagerInterface(newOwner);
			FileProperties transferedOwnerFile = iFace.getOwnerFile(fileName);
			
			if(transferedOwnerFile != null)
			{
				transferedOwnerFile.addReplicationLocation(newOwner);
				transferedOwnerFile.delReplicationLocation(client.getThisNode());
				
				this.addOwnerFile(transferedOwnerFile);
				
				iFace.delOwnerFile(fileName);
				
				return true;
			}
			else
			{
				return false;
			}
		}
		catch(RemoteException e)
		{
			System.err.println("Can't contact node for owner switch: " + e.getMessage());
			client.nodeConnectionFailure(newOwner.getHostname());
			return false;
		}
	}
	
	public boolean nodeConnectionFailure(String hostname)
	{
		return client.nodeConnectionFailure(hostname);
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
	
	public String getDownloadLocation()
	{
		return this.downloadLocation;
	}
	
	public void printTerminalError(String message)
	{
		client.printTerminalError(message);
	}
	
	//File change detection section
	private void fileChangeDetected(FileSystemObserver.FileNotification notification)
	{
		switch(notification.getEvent())
		{
			case "ENTRY_CREATE":
				System.out.println("File creation detected: " + notification.getFileLocation());
				break;
			case "ENTRY_DELETE":
				System.out.println("File deletion detected: " + notification.getFileLocation());
				break;
			case "ENTRY_MODIFY":
				System.out.println("File modification detected: " + notification.getFileLocation());
				break;
		}
	}
	
	private File[] getLocalSystemFiles() 
	{
		File directory = fileSystemManager.getDirectory(downloadLocation);
		
		if(directory == null)
		{
			fileSystemManager.createDirectory(downloadLocation);
			directory = fileSystemManager.getDirectory(downloadLocation);
		}
		
		return directory.listFiles().clone();
	}
}
