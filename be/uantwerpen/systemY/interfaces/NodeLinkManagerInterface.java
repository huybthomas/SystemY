package be.uantwerpen.systemY.interfaces;

import java.rmi.*;
import be.uantwerpen.systemY.shared.Node;

public interface NodeLinkManagerInterface extends Remote
{
	public void setLinkedNodes(Node prevNode, Node nextNode) throws RemoteException;
	public void setNext(Node node) throws RemoteException;
	public void setPrev(Node node) throws RemoteException;
}