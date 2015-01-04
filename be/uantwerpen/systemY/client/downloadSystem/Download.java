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
	private int downloadMode;
	private Thread downloadThread;
	private TCPConnection connection;
	
	/**
	 * Start a download object.
	 * @param downloadManager The Download's manager.
	 * @param fileName The file's name.
	 * @param fileOwner The file's owner.
	 * @param downloadMode The downloadMode.
	 */
	public Download(DownloadManager downloadManager, String fileName, Node fileOwner, int downloadMode)
	{
		this.downloadManager = downloadManager;
		this.fileName = fileName;
		this.fileOwner = fileOwner;
		this.downloadMode = downloadMode;
		this.downloadThread = new Thread(downloadProcedure);
		this.observer = new DownloadObserver();
	}
	
	/**
	 * Get the download observer.
	 * @return
	 */
	public DownloadObserver getObserver()
	{
		return this.observer;
	}
	
	/**
	 * Get the file's name.
	 * @return
	 */
	public String getFileName()
	{
		return this.fileName;
	}
	
	/**
	 * Get the download's mode.
	 * @return
	 */
	public int getDownloadMode()
	{
		return this.downloadMode;
	}
	
	/**
	 * Get the download's file owner.
	 * @return
	 */
	public Node getDownloadFileOwner()
	{
		return this.fileOwner;
	}
	
	/**
	 * Start the download.
	 * @return True if successful, false otherwise.
	 */
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
				if(downloadThread.getState() == Thread.State.NEW)
				{
					downloadThread.start();
				}
			}
			else
			{
				System.err.println("File '" + fileName + "' not found on file owner.");
				return false;
			}
		}
		catch(NullPointerException | RemoteException e)
		{
			System.err.println("Can't contact file owner for download (" + fileName + ") : " + e.getMessage());
			downloadManager.nodeConnectionFailure(fileOwner.getHostname());
			return false;
		}
		return true;
	}
	
	/**
	 * Cancel the download.
	 * @return True if successful, false otherwise.
	 */
	public boolean cancelDownload()
	{
		if(downloadThread.isAlive())
		{
			if(connection != null)
			{
				return connection.closeConnection();
			}
		}
		return false;
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
	
	/**
	 * The download's actual runnable
	 */
	private Runnable downloadProcedure = new Runnable()
	{
		@Override
		public void run()
		{
			boolean downloadStarted = false;
			long fileSize = 0;
			byte[] respons = null;
			byte[] fileRequest = fileName.getBytes();
			
			try
			{
				if(downloadManager.checkFileExist(fileName))		//File already exists in folder, no need to download
				{
					System.out.println("Requested file '" + fileName + "' already exists.");
					downloadFinished(true, downloadStarted);
					return;
				}
				
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
	
				downloadManager.createNewFile(fileName);
				downloadStarted = true;
				
				BufferedInputStream fileReceiver = new BufferedInputStream(connection.getDataInputStream());
				BufferedOutputStream fileWriter = new BufferedOutputStream(downloadManager.getFileOutputStream(fileName));
				
				byte[] fileBuffer = new byte[1024];		//1kB packetsize
				long downloadedFileSize = 0;
				int packetSize = 0;
				
				System.out.println("Download '" + fileName + "' started...");
				
				while((packetSize = fileReceiver.read(fileBuffer, 0, 1024)) >= 0)		//End of file: packetSize == -1
				{
					//System.out.println("Downloading '" + fileName + "': " + downloadedFileSize + " bytes - " + fileSize + " bytes.");
					downloadedFileSize += packetSize;
					fileWriter.write(fileBuffer, 0, packetSize);
					observer.setChanged();
					observer.notifyObservers(downloadedFileSize + "//" + fileSize);
				}
				
				System.out.println("Download '" + fileName + "' finished.");
				
				fileReceiver.close();
				fileWriter.close();
				connection.closeConnection();
				downloadFinished(true, downloadStarted);
			}
			catch(IOException e)
			{
				System.err.println("Download file: " + fileName + " failed: " + e.getMessage());
				downloadFinished(false, downloadStarted);
				connection.closeConnection();
			}
			catch(NullPointerException e)
			{
				System.err.println("Download file: " + fileName + " failed: No TCP-connection available - " + e.getMessage());
				downloadFinished(false, downloadStarted);
			}
		}
	};
	
	/**
	 * A function that checks whether the download has finished.
	 * @param downloadSuccessful 
	 * @param downloadStarted
	 */
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
