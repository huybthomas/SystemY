package be.uantwerpen.systemY.networkservices;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TCPservice
{
	private static int packetSize;
	@SuppressWarnings("unused")
	private static int tcpPort;
	private static DatagramSocket aSocket;
	
	/**
	 * Starts a TCP service.
	 * @param packetSize	size of the packets (int)
	 * @param tcpPort		port for the service (int)
	 */
	public TCPservice(int packetSize, int tcpPort)
	{
		try 
		{
			aSocket = new DatagramSocket();
			TCPservice.packetSize = packetSize;
			TCPservice.tcpPort = tcpPort;
		} 
		catch(SocketException e) 
		{
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Send a byte.
	 * @param data	byte[]
	 * @return	boolean		True if successful, false otherwise
	 */
	public boolean send(byte data[])
	{
		DatagramPacket request = new DatagramPacket(data, packetSize);
		try 
		{
			aSocket.send(request);
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Receive a packet.
	 * @return	DatagramPacket if successful, false otherwise
	 */
	public DatagramPacket receive()
	{
		byte[] buffer = new byte[packetSize];
		DatagramPacket reply = new DatagramPacket(buffer, packetSize);
		try 
		{
			aSocket.receive(reply);
		}
		catch(IOException e) 
		{
			System.err.println(e.getMessage());
			return null;
		}
		return reply;
	}

}
