package be.uantwerpen.systemY.networkservices;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import be.uantwerpen.systemY.client.DownloadManager;

public class TCPConnection extends Thread
{
	private Socket client;
	private DownloadManager d;
	private DataInputStream in;
	private DataOutputStream out;
	private FileInputStream fileReader;
	
	public TCPConnection(Socket client, DownloadManager d) throws IOException 
	{
		this.client = client;
		this.d = d;
		this.in = new DataInputStream(client.getInputStream());
		this.out = new DataOutputStream(client.getOutputStream());
		this.start();
	}
	
	public void run()
	{
		File file = null;

		try
		{
			String filename = in.readUTF();			//Get requested filename
			file = d.getFile(filename);				//Search for file on filesystem
			System.out.println("File requested: " + filename + " by " + client.getInetAddress());
			
			if(file != null)						//File exists
			{
				out.writeUTF("File exists");		//Notify client
				
				byte[] byteArray = new byte[(int)file.length()];

				fileReader = new FileInputStream(file);
				fileReader.read(byteArray);		//Read file
				
				out.writeLong(byteArray.length);			//Send file size
				out.write(byteArray, 0, byteArray.length);	//Send file
				client.close();					//Close connection
				System.out.println("File successfully sent");
			}
			else
			{
				out.writeUTF("File not found");
			}
		}
		catch(IOException e)
		{
			System.err.println("IO: " + e);
			e.printStackTrace();
		}
		
		try
		{
			if(file != null)
			{
				fileReader.close();
			}
		}
		catch(IOException e)
		{
			System.err.println("IO: " + e);
			e.printStackTrace();
		}
		

	}
}
