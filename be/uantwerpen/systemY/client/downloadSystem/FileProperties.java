package be.uantwerpen.systemY.client.downloadSystem;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import be.uantwerpen.systemY.shared.HashFunction;
import be.uantwerpen.systemY.shared.Node;

public class FileProperties implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String filename;
	private Node owner;
	private Node replicationLocation;
	private HashMap<Integer, Node> downloadLocations;
	
	/**
	 * Create a file properties class.
	 * @param filename The file's name.
	 * @param owner The file's owner.
	 */
	public FileProperties(String filename, Node owner)
	{
		this.filename = filename;
		this.owner = owner;
		this.replicationLocation = null;
		this.downloadLocations = new HashMap<Integer, Node>();
	}
	
	/**
	 * Get the file's name.
	 * @return The file's name.
	 */
	public String getFilename()
	{
		return this.filename;
	}
	
	/**
	 * Get the file's owner.
	 * @return The file's owner.
	 */
	public Node getOwner()
	{
		return this.owner;
	}
	
	/**
	 * Set the file's owner.
	 * @param node The node.
	 */
	public void setOwner(Node node)
	{
		this.owner = node;
	}
	
	/**
	 * Set the file's replication location.
	 * @param node The node.
	 */
	public void setReplicationLocation(Node node)
	{
		this.replicationLocation = node;
	}
	
	/**
	 * Get replication location.
	 * @return The node.
	 */
	public Node getReplicationLocation()
	{
		return this.replicationLocation;
	}
	
	/**
	 * Get the available download location.
	 * @return the download location.
	 */
	public Collection<Node> getDownloadLocations()
	{
		return this.downloadLocations.values();
	}
	
	/**
	 * Add a node to the download location.
	 * @param node The node.
	 * @return True if successful, false otherwise.
	 */
	public boolean addDownloadLocation(Node node)
	{
		if(!downloadLocationExist(node))
		{
			this.downloadLocations.put(node.getHash(), node);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Delete a node from the download locations.
	 * @param node The node.
	 * @return True if successful, false otherwise.
	 */
	public boolean delDownloadLocation(Node node)
	{
		if(this.downloadLocations.remove(node.getHash()) != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * The file's hash.
	 * @return The hash.
	 */
	public int getHash()
	{
		return new HashFunction().getHash(this.filename);
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object == null)
		{
			return false;
		}
		if(object == this)
		{
			return true;
		}
		if(!(object instanceof Node))
		{
			return false;
		}
		FileProperties aFile = (FileProperties)object;
		if(aFile.getFilename().equals(this.filename))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Check if a download location exists.
	 * @param node The node.
	 * @return True if found, false otherwise.
	 */
	private boolean downloadLocationExist(Node node)
	{
		if(this.downloadLocations.get(node.getHash()) != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
