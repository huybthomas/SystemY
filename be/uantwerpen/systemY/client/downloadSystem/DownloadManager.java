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
	private FileManager fileManager;
	private LinkedList<Download> downloadQueue;
	private ArrayList<Download> runningDownloads;
		
	public DownloadManager(FileManager fileManager)
	{
		this.fileManager = fileManager;
		this.downloadQueue = new LinkedList<Download>();
		this.runningDownloads = new ArrayList<Download>();
	}
	
	public boolean addDownload(Download download)
	{
		downloadQueue.add(download);
		
		startNextDownload();
		
		return true;
	}
	
	public void startNextDownload()
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
	
	public TCPConnection getTCPConnection(String destinationIP)
	{
		return fileManager.getTCPConnection(destinationIP);
	}
	
	public Object getFileManagerInterface(Node node)
	{
		return fileManager.getFileManagerInterface(node);
	}
	
	public boolean nodeConnectionFailure(String hostname)
	{
		return fileManager.nodeConnectionFailure(hostname);
	}
	
	public boolean checkLocalFileExist(String fileName)
	{
		return fileManager.checkLocalFileExistence(fileName);
	}
	
	public boolean createNewFile(String fileName) throws IOException
	{
		return fileManager.createNewLocalFile(fileName);
	}
	
	public boolean deleteFile(String fileName) throws IOException
	{
		return fileManager.deleteLocalFile(fileName);
	}
	
	public FileOutputStream getFileOutputStream(String fileName) throws FileNotFoundException
	{
		return fileManager.getFileOutputStream(fileName);
	}
	
	public void downloadFinished(Download download, boolean successful)
	{
		if(successful)
		{
			if(download.getOwnerSwitchState())
			{
				System.out.println("Switch owner file of: " + download.getFileName());
				fileManager.transferOwnerFile(download.getDownloadFileOwner(), download.getFileName());
			}
			else
			{
				System.out.println("Updated owner file with this node: " + download.getFileName());
				fileManager.updateReplicationLocation(download.getDownloadFileOwner(), download.getFileName());
			}
			fileManager.addLocalFile(download.getFileName());
		}

		runningDownloads.remove(download);

		startNextDownload();
	}
	
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
	
	public int getDownloadLimit()
	{
		return this.downloadLimit;
	}
	
	public void downloadRequest(TCPConnection connection)
	{
		byte[] fileRequest = null;
		
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
		connection.closeConnection();
	}
}
