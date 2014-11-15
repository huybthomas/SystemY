package be.uantwerpen.systemY.networkservices;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPConnection
{
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	
	public TCPConnection(Socket socket) throws IOException 
	{
		this.socket = socket;
		this.socket.setSoTimeout(5000);
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}
	
	public TCPConnection(String ip, int port) throws IOException 
	{
		this.socket = new Socket(ip, port);
		this.socket.setSoTimeout(5000);
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}
	
	public DataInputStream getDataInputStream()
	{
		return this.in;
	}
	
	public DataOutputStream getDataOutputStream()
	{
		return this.out;
	}
	
	public String getConnectedHost()
	{
		return this.socket.getInetAddress().getHostName();
	}

    public byte[] receiveData() throws IOException
    {
    	byte[] b;
    	
    	long byteLength = in.readLong();
    	b = new byte[(int) byteLength];
    	in.readFully(b);
    	return b;
    }
    
    public void sendData(byte[] data) throws IOException
    {
		out.writeLong((long) data.length);
		out.write(data);
    }
    
    public boolean closeConnection()
    {
    	try
    	{
			this.socket.close();
		}
    	catch(IOException e)
    	{
			System.err.println("Socket close: " + e.getMessage());
			return false;
		}
    	return true;
    }
}
