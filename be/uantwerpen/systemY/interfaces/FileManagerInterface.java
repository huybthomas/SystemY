package be.uantwerpen.systemY.interfaces;

import java.rmi.*;

import be.uantwerpen.systemY.client.downloadSystem.FileProperties;
import be.uantwerpen.systemY.shared.Node;

public interface FileManagerInterface extends Remote
{
	public FileProperties getOwnerFile(String fileName) throws RemoteException;
	public void ownerSwitchFile(String fileName, Node ownerNode) throws RemoteException;
	public boolean transferOwnerFile(Node oldOwner, String fileName) throws RemoteException;
	public void replicateFile(String fileName, Node ownerNode) throws RemoteException;
	public void replicateFile(String fileName) throws RemoteException;
	public void discoveryTransfer() throws RemoteException;
	public void downloadFile(String fileName) throws RemoteException;
	public boolean delOwnerFile(String fileName) throws RemoteException;
	public boolean deleteFileRequest(String fileName) throws RemoteException;
	public boolean addDownloadLocation(String fileName, Node downloadNode) throws RemoteException;
	public boolean delDownloadLocation(String fileName, Node downloadNode) throws RemoteException;
	public boolean checkSystemFileExistence(String fileName) throws RemoteException;
	public boolean setReplicationLocation(String fileName, Node replicationNode) throws RemoteException;
}
