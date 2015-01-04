package be.uantwerpen.systemY.interfaces;

import java.rmi.*;

import be.uantwerpen.systemY.client.downloadSystem.FileProperties;
import be.uantwerpen.systemY.shared.Node;

/**
 * Interface to the FileManager Class of a Client.
 */
public interface FileManagerInterface extends Remote
{
	/**
	 * Get the ownerfile properties of a filename.
	 * @param fileName The filename whose properties is requested.
	 * @return The FileProperties object.
	 * @throws RemoteException
	 */
	public FileProperties getOwnerFile(String fileName) throws RemoteException;
	
	/**
	 * Transfer a file to it's new owner.
	 * @param fileName	The name of the file to be transferred.
	 * @param ownerNode	The new owner.
	 * @throws RemoteException
	 */
	public void ownerSwitchFile(String fileName, Node ownerNode) throws RemoteException;
	
	/**
	 * Transfer the owner file properties of a given file from an old owner to the caller.
	 * @param oldOwner	The owner where to obtain the owner file properties from.
	 * @param fileName	The name of the file.
	 * @return True if successful, false otherwise.
	 * @throws RemoteException
	 */
	public boolean transferOwnerFile(Node oldOwner, String fileName) throws RemoteException;
	
	/**
	 * Replicate a file given its owner node and the file's name.
	 * @param fileName	The file's name.
	 * @param ownerNode	The owner node.
	 * @throws RemoteException
	 */
	public void replicateFile(String fileName, Node ownerNode) throws RemoteException;
	
	/**
	 * Replicate a file given its name.
	 * @param fileName The file's name.
	 * @throws RemoteException
	 */
	public void replicateFile(String fileName) throws RemoteException;
	
	/**
	 * Execute the discovery transfer function.
	 * Possibly there will be files transferred to the new node.
	 * @throws RemoteException
	 */
	public void discoveryTransfer() throws RemoteException;
	
	/**
	 * Download a file given its file name.
	 * @param fileName	The name of the file.
	 * @throws RemoteException
	 */
	public void downloadFile(String fileName) throws RemoteException;
	
	/**
	 * Delete the owner file of a given file name.
	 * @param fileName	The name of the file.
	 * @return True if successful, false otherwise.
	 * @throws RemoteException
	 */
	public boolean delOwnerFile(String fileName) throws RemoteException;
	
	/**
	 * Request the deletion of a file. It will be deleted if it is not a local file.
	 * @param fileName	The file name.
	 * @return True if successful, false otherwise.
	 * @throws RemoteException
	 */
	public boolean deleteFileRequest(String fileName) throws RemoteException;
	
	/**
	 * Add a node to the download locations list of a file.
	 * @param fileName	The file name.
	 * @param downloadNode	The node that will be added.
	 * @return True if successful, false otherwise.
	 * @throws RemoteException
	 */
	public boolean addDownloadLocation(String fileName, Node downloadNode) throws RemoteException;
	
	/**
	 * Delete a node from the download locations list of a file.
	 * @param fileName	The file name.
	 * @param downloadNode	The node that will be removed.
	 * @return True if successful, false otherwise.
	 * @throws RemoteException
	 */
	public boolean delDownloadLocation(String fileName, Node downloadNode) throws RemoteException;
	
	/**
	 * Check whether a file exists on this system.
	 * @param fileName	The file name.
	 * @return True if successful, false otherwise.
	 * @throws RemoteException
	 */
	public boolean checkSystemFileExistence(String fileName) throws RemoteException;
	
	/**
	 * Set the replication location of a given file.
	 * @param fileName	The file name.
	 * @param replicationNode	The new replication location.
	 * @return True if successful, false otherwise.
	 * @throws RemoteException
	 */
	public boolean setReplicationLocation(String fileName, Node replicationNode) throws RemoteException;
}
