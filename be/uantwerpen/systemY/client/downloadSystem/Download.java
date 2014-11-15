package be.uantwerpen.systemY.client.downloadSystem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import be.uantwerpen.systemY.interfaces.FileManagerInterface;
import be.uantwerpen.systemY.networkservices.TCPConnection;
import be.uantwerpen.systemY.shared.Node;

public class Download
{
	private DownloadManager downloadManager;
	private DownloadObserver observer;
	private String fileName;
	private Node fileOwner;
	private boolean ownerSwitch;
	private Thread downloadThread;
	
	public Download(DownloadManager downloadManager, String fileName, Node fileOwner, boolean ownerSwitch)
	{
		this.downloadManager = downloadManager;
		this.fileName = fileName;
		this.fileOwner = fileOwner;
		this.ownerSwitch = ownerSwitch;
		this.downloadThread = new Thread(downloadProcedure);
		this.observer = new DownloadObserver();
	}
	
	public DownloadObserver getObserver()
	{
		return this.observer;
	}
	
	public String getFileName()
	{
		return this.fileName;
	}
	
	public boolean getOwnerSwitchState()
	{
		return this.ownerSwitch;
	}
	
	public Node getDownloadFileOwner()
	{
		return this.fileOwner;
	}
	
	public boolean startDownload()
	{ 
		FileProperties fileProperties = null;
		
		try
		{
			FileManagerInterface iFace = (FileManagerInterface)downloadManager.getFileManagerInterface(fileOwner);
			fileProperties = iFace.getOwnerFile(fileName);
			
			if(fileProperties != null)
			{
				//Get all replications locations and balance load over all these nodes. IMPLEMENTATION FOLLOWS...
				downloadThread.start();
			}
			else
			{
				System.err.println("File not found on file owner.");
				return false;
			}
		}
		catch(RemoteException e)
		{
			System.err.println("Can't contact file owner for download (" + fileName + ") : " + e.getMessage());
			downloadManager.nodeConnectionFailure(fileOwner.getHostname());
			return false;
		}
		return true;
	}
	
	public boolean cancelDownload()
	{
		return false;					//Implementation later on
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object == null)
		{
			return false;
		}
		if(object == this)
		{
			return true;
		}
		if(!(object instanceof Node))
		{
			return false;
		}
		Download aDownload = (Download)object;
		if(aDownload.getFileName().equals(this.fileName))
		{
			return true;
		}
		return false;
	}
	
	private Runnable downloadProcedure = new Runnable()
	{
		public void run()
		{
			TCPConnection connection = null;
			boolean downloadStarted = false;
			long fileSize = 0;
			byte[] respons = null;
			byte[] fileRequest = fileName.getBytes();
			
			try
			{
				connection = downloadManager.getTCPConnection(fileOwner.getIpAddress());
				
				connection.sendData(fileRequest);
				
				respons = connection.receiveData();
				
				if(respons == String.valueOf(0L).getBytes())
				{
					connection.closeConnection();
					System.out.println("File '" + fileName + "' not found on file owner: " + fileOwner.getHostname());
					return;
				}
				else
				{
					fileSize = Long.valueOf(new String(respons)).longValue();
				}
				
				if(downloadManager.checkLocalFileExist(fileName))
				{
					connection.closeConnection();
					System.out.println("Requested file already exists.");
					downloadFinished(true, downloadStarted);
					return;
				}
	
				downloadManager.createNewFile(fileName);
				downloadStarted = true;
				
				BufferedInputStream fileReceiver = new BufferedInputStream(connection.getDataInputStream());
				BufferedOutputStream fileWriter = new BufferedOutputStream(downloadManager.getFileOutputStream(fileName));
				
				byte[] fileBuffer = new byte[1024];		//1kB packetsize
				long downloadedFileSize = 0;
				int packetSize = 0;
				while((packetSize = fileReceiver.read(fileBuffer, 0, 1024)) >= 0)		//End of file: packetSize == -1
				{
					System.out.println("Downloading '" + fileName + "': " + downloadedFileSize + " bytes - " + fileSize + " bytes.");
					downloadedFileSize += packetSize;
					fileWriter.write(fileBuffer, 0, packetSize);
					observer.setChanged();
					observer.notifyObservers(downloadedFileSize + "//" + fileSize);
				}
				
				fileReceiver.close();
				fileWriter.close();
				connection.closeConnection();
				downloadFinished(true, downloadStarted);
			}
			catch(IOException e)
			{
				System.err.println("Download file: " + e.getMessage());
				downloadFinished(false, downloadStarted);
				connection.closeConnection();
			}
		}
	};
	
	private void downloadFinished(boolean downloadSuccessful, boolean downloadStarted)
	{
		if(!downloadSuccessful && downloadStarted)
		{
			try
			{
				downloadManager.deleteFile(fileName);
			}
			catch(IOException e)
			{
				System.err.println("File can't be deleted: " + e.getMessage());
			}
		}
		downloadManager.downloadFinished(this, downloadSuccessful);
	}
}
