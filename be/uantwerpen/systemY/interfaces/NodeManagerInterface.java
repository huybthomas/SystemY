package be.uantwerpen.systemY.interfaces;

import java.rmi.*;
import be.uantwerpen.systemY.shared.Node;


/**
 * Interface to the NodeManager class of the Server.
 * @extends Remote
 */
public interface NodeManagerInterface extends Remote
{
	/**
	 * Adds a Node to the NodeList.
	 * @param String	hostname
	 * @param String 	ipAddress in string format
	 * @return boolean 	True if successful, false otherwise
	 */
	public boolean addNode(String hostname, String ipAddress) throws RemoteException;
	
	/**
	 * Deletes a node from the NodeList.
	 * @param String 	hostname
	 * @return	boolean True if successful, false otherwise
	 */
	public boolean delNode(String hostname) throws RemoteException;
	
	/**
	 * Get ip address of a node.
	 * @param String	hostname	the name of the node you want
	 * @return String 	Ip addreess
	 */
	public String getNode(String hostname) throws RemoteException;
	
	/**
	 * Returns the ip where the file can be found.
	 * @param filename	filename in String format
	 * @return	String	ip
	 */
	public String getFileLocation(String filename) throws RemoteException;
	
	/**
	 * Sends an answer to the new node.
	 * @param String 	hostname	the name of the node you want
	 * @return Node		returns the next node of the host
	 */
	public Node getNextNode(String hostname) throws RemoteException;
	
	/**
	 * Sends an answer to the new node.
	 * @param hostname	the name of the node you want
	 * @return Node		returns the previous node of the host
	 */
	public Node getPrevNode(String hostname) throws RemoteException;
}
