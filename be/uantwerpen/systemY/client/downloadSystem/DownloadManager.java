package be.uantwerpen.systemY.client.downloadSystem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import be.uantwerpen.systemY.networkservices.TCPConnection;
import be.uantwerpen.systemY.shared.Node;

public class DownloadManager 
{
	private int downloadLimit = 4;
	private int downloadsHosting;
	private FileManager fileManager;
	private LinkedList<Download> downloadQueue;
	private ArrayList<Download> runningDownloads;
	
	/**
	 * Create a download manager.
	 * @param fileManager The fileManger that created the download.
	 */
	public DownloadManager(FileManager fileManager)
	{
		this.downloadsHosting = 0;
		this.fileManager = fileManager;
		this.downloadQueue = new LinkedList<Download>();
		this.runningDownloads = new ArrayList<Download>();
	}
	
	/**
	 * Add a download to the queue.
	 * @param download The download to be added.
	 * @return True if successful, false otherwise.
	 */
	public synchronized boolean addDownload(Download download)
	{
		downloadQueue.add(download);
		
		startNextDownload();
		
		return true;
	}
	
	/**
	 * Start the next download in the queue.
	 */
	public synchronized void startNextDownload()
	{
		if(runningDownloads.size() <= downloadLimit && downloadQueue.size() > 0)
		{
			Download newDownload = downloadQueue.poll();

			runningDownloads.add(newDownload);
			
			if(!newDownload.startDownload())
			{
				runningDownloads.remove(newDownload);
				startNextDownload();
			}
		}
	}
	
	/**
	 * Get the number of queued downloads.
	 * @return The number of queued downloads.
	 */
	public int getQueuedDownloads()
	{
		return (downloadQueue.size() + runningDownloads.size());
	}
	
	/**
	 * Get the TCP connection to an ip.
	 * @param destinationIP The ip to be connected to
	 * @return TCPConnection
	 */
	public TCPConnection getTCPConnection(String destinationIP)
	{
		return fileManager.getTCPConnection(destinationIP);
	}
	
	/**
	 * Get a node's file manger's interface.
	 * @param node The node.
	 * @return The interface.
	 */
	public Object getFileManagerInterface(Node node)
	{
		return fileManager.getFileManagerInterface(node);
	}
	
	/**
	 * Handles the failure of a node, returns true if the failure is handled correctly.
	 * @param hostname
	 * @return True if connection failure handled correctly, false if not.
	 */
	public boolean nodeConnectionFailure(String hostname)
	{
		return fileManager.nodeConnectionFailure(hostname);
	}
	
	/**
	 * Check if a file exists.
	 * @param fileName The file
	 * @return True if it exists, false otherwise.
	 */
	public boolean checkFileExist(String fileName)
	{
		return fileManager.checkSystemFileExistence(fileName);
	}
	
	/**
	 * Creates a file.
	 * @param location	Where the file needs to be made.
	 * @param name		The name of the file.
	 * @return	True when file is made, false when failed.
	 * @throws IOException
	 */
	public boolean createNewFile(String fileName) throws IOException
	{
		return fileManager.createNewSystemFile(fileName);
	}
	
	/**
	 * Deletes a file.
	 * @param location	The location of the file.
	 * @param name		The name of the file.
	 * @return	True when the file is deleted, false when failed.
	 * @throws IOException
	 */
	public boolean deleteFile(String fileName) throws IOException
	{
		return fileManager.deleteSystemFile(fileName);
	}
	
	/**
	 * Creates a file output stream to write to the file represented by the specified File object.
	 * @param location	The location where the file needs to be written.
	 * @param name		The name of the file.
	 * @return	The created OutputStream.
	 * @throws FileNotFoundException
	 */
	public FileOutputStream getFileOutputStream(String fileName) throws FileNotFoundException
	{
		return fileManager.getFileOutputStream(fileName);
	}
	
	/**
	 * Set a download as finished
	 * @param download The Download itself
	 * @param successful
	 */
	public synchronized void downloadFinished(Download download, boolean successful)
	{
		fileManager.downloadFinished(download, successful);

		runningDownloads.remove(download);

		startNextDownload();
	}
	
	/**
	 * Set the download limit.
	 * @param limit the limit.
	 * @return true if successful, false otherwise.
	 */
	public boolean setDownloadLimit(int limit)
	{
		if(limit > 0)
		{
			this.downloadLimit = limit;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Get the download limit.
	 * @return The download limit.
	 */
	public int getDownloadLimit()
	{
		return this.downloadLimit;
	}
	
	/**
	 * Get the running downloads.
	 * @return The running downloads.
	 */
	public ArrayList<String> getRunningDownloads()
	{
		ArrayList<String> downloads = new ArrayList<String>();
		
		synchronized(runningDownloads)
		{
			for(Download download : runningDownloads)
			{
				downloads.add(download.getFileName());
			}
		}
		
		return downloads;
	}
	
	/**
	 * Request and execute a download
	 * @param connection The connection on which the download is handles.
	 */
	public void downloadRequest(TCPConnection connection)
	{
		byte[] fileRequest = null;
		
		this.addDownloadHosting();
		
		try
		{
			fileRequest = connection.receiveData();								//File request
			
			if(fileManager.fileExist(fileManager.getDownloadLocation(), new String(fileRequest)))
			{
				connection.sendData(String.valueOf(fileManager.getFile(fileManager.getDownloadLocation(), new String(fileRequest)).length()).getBytes());	//Send file size
			}
			else
			{
				connection.sendData(String.valueOf(0L).getBytes());				//File not found: filesize = 0;
				connection.closeConnection();
				return;
			}
			
			BufferedInputStream fileReader = new BufferedInputStream(fileManager.getFileInputStream(fileManager.getDownloadLocation(), new String(fileRequest)));
			BufferedOutputStream fileSender = new BufferedOutputStream(connection.getDataOutputStream(), 1024);
			
			byte[] fileBuffer = new byte[1024];									//1kB packetsize
			int packetSize = 0;
			while((packetSize = fileReader.read(fileBuffer, 0, 1024)) >= 0)		//End of file: packetSize == -1
			{
				fileSender.write(fileBuffer, 0, packetSize);
			}
			
			fileReader.close();
			fileSender.close();
		}
		catch(FileNotFoundException e)
		{
			fileManager.printTerminalError("Requested file '" + new String(fileRequest) + "' not found: " + e.getMessage());
		}
		catch(IOException e)
		{
			fileManager.printTerminalError("Connection error while transferring file to " + connection.getConnectedHost() + ": " + e.getMessage());
		}

		this.delDownloadHosting();
		
		connection.closeConnection();
	}
	
	/**
	 * Get the number of hosted downloads.
	 * @return The number of hosted downloads.
	 */
	public int getDownloadsHosting()
	{
		return this.downloadsHosting;
	}
	
	/**
	 * Add one to the number of hosted downloads.
	 */
	private synchronized void addDownloadHosting()
	{
		this.downloadsHosting = this.downloadsHosting + 1;
	}
	
	/**
	 * Remove one form the number of hosted downloads.
	 */
	private synchronized void delDownloadHosting()
	{
		this.downloadsHosting = this.downloadsHosting - 1;
	}
}
