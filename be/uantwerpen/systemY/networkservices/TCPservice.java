package be.uantwerpen.systemY.networkservices;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import be.uantwerpen.systemY.client.DownloadManager;
import be.uantwerpen.systemY.fileSystem.FileSystemManager;

/**
 * Class that makes a TCP connection possible.
 */
public class TCPservice
{
	@SuppressWarnings("unused")
	private static int tcpServerPort; //Port
	private static int tcpClientPort; //Port	
	//Every client should listen
	ServerSocket listenSocket;
	//Every client should be able to send files but Socket should only be assigned when really sending files.
	private FileSystemManager f;
	private DownloadManager d;
	
	/**
	 * Starts a TCP service.
	 * @param int	packetSize		size of the packets
	 * @param int 	tcpPort		port for the service
	 */
	public TCPservice(int packetSize, int tcpServerPort)
	{
		try 
		{
			//Make sure the server can listens for requests
			listenSocket = new ServerSocket(tcpServerPort);
			//And make sure it is constantly listening for connections
			@SuppressWarnings("unused")
			Thread listenThread = new Thread(connectionHandler);

		} 
		catch(SocketException e) 
		{
			System.err.println("Socket: " + e.getMessage());
		}
		catch (IOException e) {
			System.err.println("IO: " + e.getMessage());
		}
	}
	
	Runnable connectionHandler = new Runnable() 
	{
        @Override
        public void run() {
            try {
            	//System.out.println("Waiting for clients to connect...");
                while (true) {
                    Socket clientSocket = listenSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    @SuppressWarnings("unused")
					TCPConnection c = new TCPConnection(clientSocket, d);
                }
            } catch (IOException e) {
                System.err.println("Unable to process client request");
                e.printStackTrace();
            }
        }
    };
    
    public boolean requestFile(String ip, String fileName)
    {
    	//Code voor FILE op te vragen EN op te slagen in Downloads folder
    	f = new FileSystemManager();
    	Socket s = null;
    	
    	try
    	{
    		s = new Socket(ip, tcpClientPort);
    		DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out =	new DataOutputStream(s.getOutputStream());
			out.writeUTF(fileName); 			//UTF is a string encoding
			System.out.println("Contacting server: " + ip + ", requesting file: " + fileName);
			String data = in.readUTF();		//Get response of file existence
			System.out.println("Received: "+ data); 
			if(data.contains("File exists"))
			{
				long l = in.readLong();			//Get file size
				byte[] b = new byte[(int) l];
				in.readFully(b);			//Read file
				
				f.saveFile(b, fileName);	//Save file	
				System.out.println("File downloaded!");
				return true;
			}
			else if(data.contains("File not found"))
			{
				System.out.println("File not found on server");
				return false;
			}	
		}
		catch (UnknownHostException e)
		{
			System.out.println("Socket:" + e.getMessage());
			e.printStackTrace();
		}
		catch(EOFException e)
		{
			System.out.println("EOF:" + e.getMessage());
			e.printStackTrace();
		}
		catch(IOException e)
		{
			System.out.println("IO:" + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			if(s!=null)
			{
				try
				{
					s.close();		//Close connection
				}
				catch(IOException e)
				{
					System.out.println("Close:" + e.getMessage());
					e.printStackTrace();
				}
			}
		}
    	return false;
    }

}
