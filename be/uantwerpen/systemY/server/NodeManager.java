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
	 * @param hostname	The hostname you want to add.
	 * @param ipAddress The ipAddress of the host.
	 * @return boolean 	True if successful, false otherwise
	 */
	public boolean addNode(String hostname, String ipAddress)
	{
		return nodeList.addNode(hostname, ipAddress);
	}
	
	/**
	 * Deletes a node from the NodeList.
	 * @param hostname	The name of the node you want to delete.
	 * @return	boolean True if successful, false otherwise.
	 */
	public boolean delNode(String hostname)
	{
		return nodeList.delNode(hostname);
	}
	
	/**
	 * Clear all nodes from the list
	 */
	public void clearList()
	{
		nodeList.clearList();
	}
	
	/**
	 * Sets the new list.
	 * @param nodeList	The nodeList you want to set.
	 */
	public void setNodeList(NodeList nodeList)
	{
		this.nodeList = nodeList;
	}
	
	/**
	 * Get the node list.
	 * @return nodeList
	 */
	public NodeList getNodeList()
	{
		return this.nodeList;
	}
	
	/**
	 * Get ip address of a node.
	 * @param hostname	The name of the node you want.
	 * @return 	Ip address.
	 */
	public String getNode(String hostname)
	{
		return this.nodeList.getNode(hostname);
	}
	
	/**
	 * Sends an answer to the new node.
	 * @param hostname	The name of the node you want.
	 * @return The next node of the host.
	 */
	public Node getNextNode(String hostname) 
	{
		return this.nodeList.getNextNode(hostname);
	}
	
	/**
	 * Sends an answer to the new node.
	 * @param hostname	The name of the node you want.
	 * @return The previous node of the host.
	 */
	public Node getPrevNode(String hostname)
	{
		return this.nodeList.getPrevNode(hostname);
	}
	
	/**
	 * Returns the node where the file can be found.
	 * @param filename	The name of the file you want the location from.
	 * @return	The owner of the file.
	 */
	public Node getFileLocation(String filename)
	{
		return nodeList.getFileLocation(filename);
	}
	
	/**
	 * Returns the HashMap of the NodeList.
	 * @return The complete nodeList.
	 */
	public HashMap<Integer, Node> getNodes()
	{
		return nodeList.getNodeList();
	}
}
