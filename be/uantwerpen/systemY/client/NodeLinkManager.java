package be.uantwerpen.systemY.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.shared.Node;

public class NodeLinkManager extends UnicastRemoteObject implements NodeLinkManagerInterface
{
	private static final long serialVersionUID = 1L;
	
	private NodeLinks nodeLinks;
	private String serverIP;
	
	/**
	 * Creates Nodelinks.
	 * @param node
	 * @throws RemoteException
	 */
	public NodeLinkManager(Node node) throws RemoteException
	{
		nodeLinks = new NodeLinks(node);
	}
	
	/**
	 * Request hostname.
	 * @return hostname
	 */
	public String getMyHostname()
	{
		return nodeLinks.getMyHostname();
	}
	
	/**
	 * Set new hostname.
	 * @param hostname
	 */
	public void setMyHostname(String hostname)
	{
		this.nodeLinks.setMyHostname(hostname);
	}
	
	/**
	 * Set new ip.
	 * @param ip
	 */
	public void setMyIP(String ip)
	{
		this.nodeLinks.setMyIP(ip);
	}
	
	/**
	 * Get own ip.
	 * @return String
	 */
	public String getMyIP()
	{
		return this.nodeLinks.getMyIP();
	}
	
	/**
	 * Get next node.
	 * @return Node
	 */
	public Node getNext()
	{
		return nodeLinks.getNext();
	}
	
	/**
	 * Get previous node.
	 * @return	Node
	 */
	public Node getPrev()
	{
		return nodeLinks.getPrev();
	}
	
	/**
	 * Sets the next node.
	 * @param node
	 */
	public void setNext(Node node)
	{
		nodeLinks.setNext(node);
	}
	
	/**
	 * Sets previous node.
	 * @param node
	 */
	public void setPrev(Node node)
	{
		nodeLinks.setPrev(node);
	}
	
	/**
	 * Sets previous and next node.
	 * @param prevNode
	 * @param nextNode
	 */
	public void setLinkedNodes(Node prevNode, Node nextNode)
	{
		nodeLinks.setPrev(prevNode);
		nodeLinks.setNext(nextNode);
	}
	
	/**
	 * Sets the ip address of the server.
	 * @param ip
	 */
	public void setServerIP(String ip)
	{
		this.serverIP = ip;
	}
	
	/**
	 * Gets the ip address of the server.
	 * @return String
	 */
	public String getServerIP()
	{
		return this.serverIP;
	}
	
	/**
	 * Update the list when a new node is added.
	 * @param newNode
	 * @return Node
	 */
	public Node updateLinks(Node newNode)
	{
		int ownHash = nodeLinks.getThis().getHash();
		int newHash = newNode.getHash();
		int nextHash = nodeLinks.getNext().getHash();
		int prevHash = nodeLinks.getPrev().getHash();
		
		if(nodeLinks.getThis().equals(nodeLinks.getNext()) && nodeLinks.getThis().equals(nodeLinks.getPrev()))
		{
			setLinkedNodes(newNode, newNode);	//Second node will be next and previous node
			return nodeLinks.getThis();
		}
		else
		{
			// new node comes between this node and the next node
			if(((ownHash < newHash) && (newHash < nextHash)) || ((nextHash < ownHash) && (ownHash < newHash)) || ((nextHash < ownHash) && (newHash < nextHash))) 
			{
				Node oldNext = nodeLinks.getNext();	// make temporary node to return later
				nodeLinks.setNext(newNode);			// set the new node as nextNode
				return oldNext;						// return this nodes old nextNode so it can be the nextNode of the new node 
			} 
			// new node comes right before this node
			else if(((prevHash < newHash) && (newHash < ownHash)) || ((ownHash < prevHash) && (newHash < ownHash)) || ((ownHash < prevHash) && (prevHash < newHash))) 
			{
				nodeLinks.setPrev(newNode);			// set the new node as prevNode
				return null;						// no return to new node needed as it learns everything from the old prevNode
			}
			else 
			{
				return null;						// no return because new node is not near this node
			}
		}
	}
}
