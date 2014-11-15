package be.uantwerpen.systemY.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.shared.Node;

/**
 * Nodemanager class to execute operations on nodes in the NodeList.
 * @extends UnicastRemoteObject
 * @implements NodeManagerInterface
 */
public class NodeManager extends UnicastRemoteObject implements NodeManagerInterface
{
	private static final long serialVersionUID = 1L;
	private NodeList nodeList;
	
	/**
	 * Creates the NodeManager Object.
	 * @throws RemoteException
	 */
	public NodeManager() throws RemoteException
	{
		nodeList = new NodeList();
	}
	
	/**
	 * Adds a Node to the NodeList.
	 * @param String	hostname
	 * @param String 	ipAddress in string format
	 * @return boolean 	True if successful, false otherwise
	 */
	public boolean addNode(String hostname, String ipAddress)
	{
		return nodeList.addNode(hostname, ipAddress);
	}
	
	/**
	 * Deletes a node from the NodeList.
	 * @param String 	hostname
	 * @return	boolean True if successful, false otherwise
	 */
	public boolean delNode(String hostname)
	{
		return nodeList.delNode(hostname);
	}
	
	/**
	 * Sets the new list.
	 * @param NodeList		the nodelist
	 */
	public void setNodeList(NodeList nodeList)
	{
		this.nodeList = nodeList;
	}
	
	/**
	 * Get the node list.
	 * @return NodeList
	 */
	public NodeList getNodeList()
	{
		return this.nodeList;
	}
	
	/**
	 * Get ip address of a node.
	 * @param String	hostname	the name of the node you want
	 * @return String 	Ip addreess
	 */
	public String getNode(String hostname)
	{
		return this.nodeList.getNode(hostname);
	}
	
	/**
	 * Sends an answer to the new node.
	 * @param String 	hostname	the name of the node you want
	 * @return Node		returns the next node of the host
	 */
	public Node getNextNode(String hostname) 
	{
		return this.nodeList.getNextNode(hostname);
	}
	
	/**
	 * Sends an answer to the new node.
	 * @param hostname	the name of the node you want
	 * @return Node		returns the previous node of the host
	 */
	public Node getPrevNode(String hostname)
	{
		return this.nodeList.getPrevNode(hostname);
	}
	
	/**
	 * Returns the node where the file can be found.
	 * @param filename	filename in String format
	 * @return	Node	fileowner
	 */
	public Node getFileLocation(String filename)
	{
		return nodeList.getFileLocation(filename);
	}
	
	/**
	 * Returns the HashMap of the NodeList.
	 * @return HashMap<Integer, Node>
	 */
	public HashMap<Integer, Node> getNodes()
	{
		return nodeList.getNodeList();
	}
}
