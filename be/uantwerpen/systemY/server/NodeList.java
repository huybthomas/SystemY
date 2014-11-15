package be.uantwerpen.systemY.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import be.uantwerpen.systemY.shared.Node;

/**
 * NodeList class that contains the Nodes in the network and operations on them.
 * @implements	Serializable
 */
@XmlRootElement(name = "NodeList")
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeList implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private HashMap<Integer, Node> nodeMap;
	
	/**
	 * Creates the NodeList Object.
	 */
	public NodeList()
	{
		nodeMap = new HashMap<Integer, Node>();
	}
	
	/**
	 * Returns the HashMap of the NodeList.
	 * @return HashMap<Integer, Node>
	 */
	public HashMap<Integer, Node> getNodeList()
	{
		return this.nodeMap;
	}
	
	/**
	 * Adds a Node to the NodeList.
	 * @param String	hostname of the new node
	 * @param String 	ipAddress of the new node
	 * @return boolean	true if successful, false otherwise
	 */
	public boolean addNode(String hostname, String ipAddress)
	{
		if(!checkNodeExistence(hostname))
		{
			Node newNode = new Node(hostname, ipAddress);
			nodeMap.put(newNode.getHash(), newNode);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Deletes a node from the NodeList.
	 * @param String 	hostname to delete
	 * @return boolean	True if successful, false otherwise
	 */
	public boolean delNode(String hostname)
	{
		if(checkNodeExistence(hostname))
		{
			int hashValue = calculateHash(hostname);
			nodeMap.remove(hashValue);
			return true;
		}
		else
		{
			return false;
		}	
	}
	
	/**
	 * Returns the ip of the given hostname.
	 * @param String	hostname 
	 * @return String	ip in String format
	 */
	public String getNode(String hostname)
	{
		try
		{
			return nodeMap.get(calculateHash(hostname)).getIpAddress();
		}
		catch(Exception e)
		{
			System.err.println("Host not found in nodelist.");
			return null;
		}
	}
	
	/**
	 * Returns the node of the file's location.
	 * @param String	filename 	name of the requested file
	 * @return Node		fileowner of the requested file, null if there are no hosts in the list
	 */
	public Node getFileLocation(String filename)
	{
		int hashValue = calculateHash(filename);
		Set<Integer> nodeIDs = nodeMap.keySet();
		
		Integer[] id = nodeIDs.toArray(new Integer[nodeIDs.size()]);
		Arrays.sort(id);
		
		try
		{
			if(hashValue > id[nodeIDs.size() - 1])
			{
				return nodeMap.get(id[nodeIDs.size()-1]);
			}
			else
			{
				int i = 0;
				while(hashValue > id[i]) 
				{
					i++;
				}
				
				if(i == 0)
				{
					return nodeMap.get(id[nodeIDs.size() - 1]);
				}
				else
				{
					return nodeMap.get(id[i - 1]);
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.err.println("Nodelist has no entries.");
			return null;
		}
	}
	
	/**
	 * Calculates the next node.
	 * @param String	hostname	the hostname of the host
	 * @return Node		returns the next node of the host, null if the host is not in the list
	 */
	public Node getNextNode(String hostname) 
	{
		int nodeID = calculateHash(hostname);
		Set<Integer> nodeIDs = nodeMap.keySet();
		Integer[] ids = nodeIDs.toArray(new Integer[nodeIDs.size()]);
		Arrays.sort(ids);
		
		int i = 0;
		while((i < ids.length))
		{
			if(ids[i] == nodeID)
			{
				if(i != (ids.length - 1))
				{
					return nodeMap.get(ids[i + 1]);
				}
				else
				{
					return nodeMap.get(ids[0]);		//Next node of the highest hash node is node with the lowest hash
				}
			}
			i++;
		}
		return null;
	}
	
	/**
	 * Calculates the previous node.
	 * @param String	hostname	the hostname of the host
	 * @return Node		returns the previous node of the host, null if the host is not in the list
	 */
	public Node getPrevNode(String hostname) 
	{
		int nodeID = calculateHash(hostname);
		Set<Integer> nodeIDs = nodeMap.keySet();
		Integer[] ids = nodeIDs.toArray(new Integer[nodeIDs.size()]);
		Arrays.sort(ids);
		
		int i = 0;
		while((i < ids.length))
		{
			if(ids[i] == nodeID)
			{
				if(i != 0)
				{
					return nodeMap.get(ids[i - 1]);
				}
				else
				{
					return nodeMap.get(ids[ids.length - 1]);		//previous node of the lowest hash node is node with the highest hash
				}
			}
			i++;
		}
		return null;
	}
	
	/**
	 * Checks if a hostname exists.
	 * @param String	hostname	name of the host to be checked
	 * @return	boolean	True if successful, false otherwise
	 */
	private boolean checkNodeExistence(String hostname)
	{
		return nodeMap.containsKey(calculateHash(hostname));
	}
	
	/**
	 * Calculates the hash of a string.
	 * @param String	name	String to be hashed
	 * @return int		The hash
	 */
	private int calculateHash(String name)
	{
		int i = name.hashCode();
		i = Math.abs(i % 32768);
		return i;
	}
}
