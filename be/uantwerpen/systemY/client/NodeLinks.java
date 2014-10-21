package be.uantwerpen.systemY.client;

import be.uantwerpen.systemY.shared.Node;

public class NodeLinks
{
	private Node thisNode;
	private Node nextNode;
	private Node prevNode;
	
	/**
	 * Reads a node form the list.
	 * @param node
	 */
	public NodeLinks(Node node) 
	{
		this.thisNode = node;
		this.nextNode = thisNode;	// initial next is this node
		this.prevNode = thisNode;	// initial prev is this node
	}
	
	/**
	 * Reads the host name.
	 * @return	String hostname
	 */
	public String getMyHostname()
	{
		return thisNode.getHostname();
	}
	
	/**
	 * Sets a new hostname.
	 * @param hostname
	 */
	public void setMyHostname(String hostname)
	{
		this.thisNode.setHostname(hostname);
	}
	
	/**
	 * Set the ip of the host.
	 * @param ip	the new Ip
	 */
	public void setMyIP(String ip)
	{
		this.thisNode.setIpAddress(ip);
	}
	
	/**
	 * Get the ip of a node.
	 * @return String	ip address
	 */
	public String getMyIP()
	{
		return this.thisNode.getIpAddress();
	}
	
	/**
	 * Get the node.
	 * @return Node
	 */
	public Node getThis()
	{
		return this.thisNode;
	}
	
	/**
	 * Request the next node of a specific node.
	 * @return Node	nextNode
	 */
	public Node getNext()
	{
		return this.nextNode;
	}
	
	/**
	 * Sets the next node for specific node.
	 * @param node
	 */
	public void setNext(Node node)
	{
		nextNode = node;
	}
	
	/**
	 * Gets the previous node of the given node.
	 * @return Node	the previous node
	 */
	public Node getPrev()
	{
		return this.prevNode;
	}
	
	/**
	 * Sets the previous node of the given node.
	 * @param node
	 */
	public void setPrev(Node node)
	{
		prevNode = node;
	}
}
