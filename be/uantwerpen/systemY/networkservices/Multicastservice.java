package be.uantwerpen.systemY.networkservices;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

/**
 * Class that enables multi casts in the network.
 * @implements	Runnable
 */
public class Multicastservice implements Runnable
{
	private int port;
	private String multicastIP;
	private MulticastSocket socket;
	private MulticastObserver observer;
	private boolean running;
	private final int bufferSize = 1024;
	
	/**
	 * Creates the MulticastService Object
	 * @param String	multicastIP
	 * @param int	port
	 */
	public Multicastservice(String multicastIP, int port)
	{
		this.multicastIP = multicastIP;
		this.port = port;
		this.observer = new MulticastObserver();
		this.running = false;
	}
	
	/**
	 * Get a multicast observer
	 * @return	MulticastObserver	the observer
	 */
	public MulticastObserver getObserver()
	{
		return this.observer;
	}
	
	/**
	 * Setup the Multicast Service
	 * @return	boolean	True if successful, false otherwise
	 */
	public boolean setupMulticastservice()
	{
		try
		{
			InetAddress ipAddress = InetAddress.getByName(multicastIP);
			socket = new MulticastSocket(port);
			socket.joinGroup(ipAddress);
		}
		catch(SocketException e)
		{
			System.out.println("Socket: " + e.getMessage());
			return false;
		}
		catch(IOException e)
		{
			System.out.println("IO: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Send a multicast message
	 * @param byte[]	message		the message you want to send
	 * @return boolean 	true when success, false when failed
	 */
	public boolean sendMulticast(byte[] message)
	{
		if(message.length > bufferSize)
		{
			System.err.println("Message to large to send for multicastbuffer.");
			return false;
		}
		
		if(socket == null)
		{
			if(!setupMulticastservice())
			{
				System.err.println("Multicastservice not started.");
				return false;
			}
		}
		
		try
		{
			InetAddress ipAddress = InetAddress.getByName(multicastIP);
			DatagramPacket messageOut = new DatagramPacket(message, message.length, ipAddress, port);
			socket.send(messageOut);
		}
		catch(IOException e)
		{
			System.out.println("IO: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Terminate the multicast service
	 * @return boolean	True if successful, false if failed
	 */
	public boolean terminate()
	{
		running = false;	//disable running flag
		
		if(socket != null)
		{
			socket.close();
			while(!socket.isClosed())
			{
				//wait until the socket is closed
			}
			socket = null;
			return true;
		}
		return false;
	}
	
	/**
	 * Run the multicast service.
	 */
	@Override
	public void run()
	{
		if(socket == null)
		{
			if(!setupMulticastservice())
			{
				System.err.println("Multicastservice not started.");
				return;
			}
		}
		
		running = true;		//set running flag
		try
		{
			while(running)
			{
				byte[] buffer = new byte[bufferSize];
				DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
				socket.receive(messageIn);
				this.observer.setChanged();
				this.observer.notifyObservers(messageIn);
			}
		}
		catch(SocketException e)
		{
			System.out.println("Socket: " + e.getMessage());
		}
		catch(IOException e)
		{
			System.out.println("IO: " + e.getMessage());
		}
	}
}
