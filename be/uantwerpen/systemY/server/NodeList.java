package be.uantwerpen.systemY.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import be.uantwerpen.systemY.shared.HashFunction;
import be.uantwerpen.systemY.shared.Node;

/**
 * NodeList class that contains the Nodes in the network and operations on them.
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
	 * @return nodeMap.	
	 */
	public HashMap<Integer, Node> getNodeList()
	{
		return this.nodeMap;
	}
	
	/**
	 * Adds a Node to the NodeList.
	 * @param hostname	Hostname of the new node.
	 * @param ipAddress	IpAddress of the new node.
	 * @return True if successful, false otherwise.
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
	 * @param hostname 	Hostname of node you want to delete.
	 * @return True if successful, false otherwise.
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
	 * Clear all nodes from the list.
	 */
	public void clearList()
	{
		nodeMap.clear();
	}
	
	/**
	 * Returns the ip of the given hostname.
	 * @param hostname	The name of the node you want to get.
	 * @return Returns the ipAddress of the node.
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
	 * @param filename 	Name of the requested file.
	 * @return Fileowner of the requested file in the form of a node, null if there are no hosts in the list.
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
	 * @param hostname	The hostname of the host you want to calculate next node of.
	 * @return The next node of the host, null if the host is not in the list.
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
	 * @param hostname	The hostname of the host you want the previous node of.
	 * @return The previous node of the host, null if the host is not in the list.
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
	 * @param hostname	Name of the host to be checked.
	 * @return True if successful, false otherwise.
	 */
	private boolean checkNodeExistence(String hostname)
	{
		return nodeMap.containsKey(calculateHash(hostname));
	}
	
	/**
	 * Calculates the hash of a string.
	 * @param name	String to be hashed.
	 * @return The hashvalue.
	 */
	private int calculateHash(String name)
	{
		return new HashFunction().getHash(name);
	}
}
