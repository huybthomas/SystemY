package be.uantwerpen.systemY.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import be.uantwerpen.systemY.shared.Node;

public interface BootstrapManagerInterface extends Remote
{
	public void setLinkedNodes(Node prevNode, Node nextNode) throws RemoteException;
	public void setNetwork(String serverIP, int networkSize) throws RemoteException;
}
