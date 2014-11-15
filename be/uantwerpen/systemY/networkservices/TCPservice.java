package be.uantwerpen.systemY.networkservices;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Class that makes a TCP connection possible.
 */
public class TCPservice implements Runnable
{
	private int tcpListenPort; //Port
	private int tcpSendPort; //Port	
	private ServerSocket listenSocket;
	private TCPObserver observer;
	private boolean running;
	
	/**
	 * Starts a TCP service.
	 * @param int	packetSize		size of the packets
	 * @param int 	tcpPort		port for the service
	 */
	public TCPservice(int sendPort, int receivePort)
	{
		this.tcpListenPort = receivePort;
		this.tcpSendPort = sendPort;
		this.observer = new TCPObserver();
		this.running = false;
	}
	
	public TCPObserver getTCPObserver()
	{
		return this.observer;
	}
	
	public boolean setupTCPListener()
	{
		try
		{
			listenSocket = new ServerSocket(tcpListenPort);
		}
		catch(IOException e)
		{
			System.err.println("TCP listener IO: " + e.getMessage());
			return false;
		}
		catch(IllegalArgumentException e)
		{
			System.err.println("TCP port is out of range: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean terminate()
	{
		running = false;		//disable running flag
		
		if(listenSocket != null)
		{
			try
			{
				listenSocket.close();
			}
			catch(IOException e)
			{
				System.err.println("TCP socket closing: " + e.getMessage());
			}
			
			while(!listenSocket.isClosed())
			{
				//Wait until the socket is closed
			}
			
			listenSocket = null;
			return true;
		}
		return false;
	}
	
	@Override
	public void run()
	{
		if(listenSocket == null)
		{
			if(!setupTCPListener())
			{
				System.err.println("TCP listen socket not started.");
				return;
			}
		}
		
		running = true;			//set running flag
		try
		{
	        while(running)
	        {
	            Socket clientSocket = listenSocket.accept();
				TCPConnection connection = new TCPConnection(clientSocket);
				this.observer.setChanged();
	            this.observer.notifyObservers(connection);
	        }
		}
		catch(SocketException e)
		{
			System.out.println("TCP socket: " + e.getMessage());
		}
		catch(IOException e)
		{
			System.err.println("IO: " + e.getMessage());
		}
	}
    
    public TCPConnection getConnection(String ip)
    {
    	try
    	{
			Socket socket = new Socket(ip, this.tcpSendPort);
			return new TCPConnection(socket);
		}
    	catch(UnknownHostException e)
    	{
			System.err.println("TCP unkown host: " + e.getMessage());
		}
    	catch(IOException e)
    	{
			System.err.println("TCP IO: " + e.getMessage());
		}
    	return null;    	
    }
}
