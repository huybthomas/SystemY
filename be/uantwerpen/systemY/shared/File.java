package be.uantwerpen.systemY.shared;

import java.util.Collection;
import java.util.HashMap;

public class File
{
	private String filename;
	private Node owner;
	private HashMap<Integer, Node> replications;
	
	public File(String filename, Node owner)
	{
		this.filename = filename;
		this.owner = owner;
		this.replications = new HashMap<Integer, Node>();
	}
	
	public String getFilename()
	{
		return this.filename;
	}
	
	public Node getOwner()
	{
		return this.owner;
	}
	
	public Collection<Node> getReplicationLocations()
	{
		return this.replications.values();
	}
	
	public Node checkReplicationOnNode(String hostname)
	{
		return this.replications.get(new Node(hostname, null).getHash());
	}
	
	public void addReplicationLocation(Node node)
	{
		this.replications.put(node.getHash(), node);
	}
	
	public int getHash()
	{
		int i = filename.hashCode();
		i = Math.abs(i % 32768);
		return i;
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
		File aFile = (File)object;
		if(aFile.getFilename().equals(this.filename))
		{
			return true;
		}
		return false;
	}
}
