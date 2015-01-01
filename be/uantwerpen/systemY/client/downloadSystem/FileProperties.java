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
	
	public FileProperties(String filename, Node owner)
	{
		this.filename = filename;
		this.owner = owner;
		this.replicationLocation = null;
		this.downloadLocations = new HashMap<Integer, Node>();
	}
	
	public String getFilename()
	{
		return this.filename;
	}
	
	public Node getOwner()
	{
		return this.owner;
	}
	
	public void setOwner(Node node)
	{
		this.owner = node;
	}
	
	public void setReplicationLocation(Node node)
	{
		this.replicationLocation = node;
	}
	
	public Node getReplicationLocation()
	{
		return this.replicationLocation;
	}
	
	public Collection<Node> getDownloadLocations()
	{
		return this.downloadLocations.values();
	}
	
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
