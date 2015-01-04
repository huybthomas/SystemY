package be.uantwerpen.systemY.networkservices;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class that is responsible for the TCP connection between two instances in the network.
 * Files can be sent and received with this connection.
 */
public class TCPConnection
{
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	
	/**
	 * Sets up a TCP connection on a specific socket by making an DataInput- and -OutputStream.
	 * @param socket	On which socket the streams need to be made.
	 * @throws IOException
	 */
	public TCPConnection(Socket socket) throws IOException 
	{
		this.socket = socket;
		this.socket.setSoTimeout(5000);
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}
	
	/**
	 * Creates a TCP connection, by first creating the socket.
	 * @param ip	The ip on which the TCP needs to be set up.
	 * @param port	The port on which the TCP needs to be set up.
	 * @throws IOException
	 */
	public TCPConnection(String ip, int port) throws IOException 
	{
		this.socket = new Socket(ip, port);
		this.socket.setSoTimeout(5000);
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}
	
	/**
	 * Get the DataInputStream.
	 * @return	DataInputStream object.
	 */
	public DataInputStream getDataInputStream()
	{
		return this.in;
	}
	
	/**
	 * Get the DataOutputStream.
	 * @return	DataOutputStream object.
	 */
	public DataOutputStream getDataOutputStream()
	{
		return this.out;
	}
	
	/**
	 * Returns the connected host.
	 * @return	The hostname of the host that is connected.
	 */
	public String getConnectedHost()
	{
		return this.socket.getInetAddress().getHostName();
	}
	
	/**
	 * Makes a buffer of the length of the DataInputStream.
	 * @return The buffered inputstream.
	 * @throws IOException
	 */
    public byte[] receiveData() throws IOException
    {
    	byte[] b;
    	
    	long byteLength = in.readLong();
    	b = new byte[(int) byteLength];
    	in.readFully(b);
    	return b;
    }
    
    /**
     * Sends data over the DataOutputStream.
     * @param data	The data that needs to be send over the output stream.
     * @throws IOException
     */
    public void sendData(byte[] data) throws IOException
    {
		out.writeLong((long) data.length);
		out.write(data);
    }
    
    /**
     * Closes the connection to a socket.
     * @return True if successful, false otherwise.
     */
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
