package be.uantwerpen.systemY.interfaces;

import java.rmi.*;

import be.uantwerpen.systemY.client.downloadSystem.FileProperties;
import be.uantwerpen.systemY.shared.Node;

public interface FileManagerInterface extends Remote
{
	public FileProperties getOwnerFile(String fileName) throws RemoteException;
	public void ownerSwitchFile(String fileName, Node ownerNode) throws RemoteException;
	public void downloadFile(String fileName) throws RemoteException;
	public boolean delOwnerFile(String fileName) throws RemoteException;
	public boolean addReplicationLocation(String fileName, Node replicationNode) throws RemoteException;
	public boolean delReplicationLocation(String fileName, Node replicationNode) throws RemoteException;
}
