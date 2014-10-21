package be.uantwerpen.systemY.interfaces;

import java.rmi.*;

import be.uantwerpen.systemY.shared.Node;

public interface NodeManagerInterface extends Remote
{
	public boolean addNode(String hostname, String ipAddress) throws RemoteException;
	public boolean delNode(String hostname) throws RemoteException;
	public String getNode(String hostname) throws RemoteException;
	public String getFileLocation(String filename) throws RemoteException;
	public Node getNextNode(String hostname) throws RemoteException;
	public Node getPrevNode(String hostname) throws RemoteException;
}
