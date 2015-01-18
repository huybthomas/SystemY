package be.uantwerpen.systemY.fileSystem;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Class that implements the file handling
 */
public class FileSystemManager
{
	private FileSystemWatcher fileSystemWatcher;
	private Thread watcherThread;
	
	public FileSystemManager(String watchDirectory)
	{
		fileSystemWatcher = new FileSystemWatcher(watchDirectory);
		watcherThread = new Thread(fileSystemWatcher);
	}
	
	public FileSystemManager()
	{
		fileSystemWatcher = new FileSystemWatcher(new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + File.separator + "SystemY" + File.separator + "Files");
		watcherThread = new Thread(fileSystemWatcher);
	}
	
	/**
	 * Saves an Object in an xml file.
	 * 
	 * Use {@link loadXMLFile(Class<?> typeClass, String fileLocation)} to load an object.
	 * 
	 * @param object		The object to be saved.
	 * @param fileLocation	The location for the file to be saved.
	 * @return	boolean		True if successful, false otherwise.
	 */
	public boolean saveXMLFile(Object object, String fileLocation)
	{
		try
		{
			File file = new File(fileLocation);
			file.getParentFile().mkdirs();

			if(!file.exists())
			{
				file.createNewFile();													//Make new file if not existing
			}
			JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass()); 		//Java architecture for XML Binding (JAXB)
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller(); 				//Serialize from class to XML
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 		//Set better layout for XML output to file
			jaxbMarshaller.marshal(object, file); 										//Write marshaled object to file
		}
		catch(JAXBException e)
		{
			System.err.println("JAXB Marshalling: " + e.getMessage());
			return false;
		}
		catch(IOException e)
		{
			System.err.println("IOException: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Loads an Object in an xml file.
	 * 
	 * Use {@link saveXMLFile(Object object, String fileLocation)} to save an object.
	 * 
	 * @param typeClass		The type of object to be loaded.
	 * @param fileLocation	The location of the file.
	 * @return	boolean		True if successful, false otherwise.
	 */
	public Object loadXMLFile(Class<?> typeClass, String fileLocation)
	{
		Object object = null;
		try
		{
			File file = new File(fileLocation);
			JAXBContext jaxbContext = JAXBContext.newInstance(typeClass);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller(); 		  //Deserialize from class to XML
			object = jaxbUnmarshaller.unmarshal(file); 								  //Read XML file and reconstruct object
		}
		catch(JAXBException e)
		{
			System.err.println("JAXB Unmarshalling: " + e.getMessage());
		}
		return object;
	}
	
	/**
	 * Saves a file
	 * @param data	The file you want to save
	 * @param fileLocation	The location where you want to save the file
	 * @return True if successful, false when failed
	 */
	public boolean saveFile(byte[] data, String fileLocation)
	{
		FileOutputStream f = null;
		
		try
		{
			f = new FileOutputStream(fileLocation);
			f.write(data);
			f.close();
			return true;
		}
		catch(FileNotFoundException e)
		{
			System.err.println("File not found: " + e);
			e.printStackTrace();
		}
		catch(IOException e)
		{
			System.err.println("IO: " + e);
			e.printStackTrace();
		}
		finally
		{
			if(f != null)
			{
				try 
				{
					f.close();
				} 
				catch(IOException e) 
				{
					System.err.println("IO: " + e);
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	/**
	 * Get a specific file.
	 * @param location	The location of the file you want.
	 * @param name	The name of the file you want
	 * @return	The requested file.
	 */
	public File getFile(String location, String name)
	{
		File file = new File(location + File.separator + name);
		if(file.isFile())
		{
			return file;
		}
		else
		{
			System.err.println("File does not exist.");
			return null;
		}
	}
	
	/**
	 * Checks if the name is a directory.
	 * @param directory	Name of the directory.
	 * @return The directory when it exists otherwise returns null.
	 * @throws SecurityException
	 */
	public File getDirectory(String directory) throws SecurityException
	{
		File dir = new File(directory);
		
		//Check if downloadlocation exists
		if(dir.isDirectory())
		{
			return dir;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Make a directory.
	 * @param directory	The name you want to give the directory.
	 * @return	True when directory is made, false when failed.
	 * @throws SecurityException
	 */
	public boolean createDirectory(String directory) throws SecurityException
	{
		File dir = new File(directory);
		return dir.mkdirs();
	}
	
	/**
	 * Creates a file.
	 * @param location	Where the file needs to be made.
	 * @param name		The name of the file.
	 * @return	True when file is made, false when failed.
	 * @throws IOException
	 */
	public boolean createFile(String location, String name) throws IOException
	{
		File file = new File(location + File.separator + name);
		return file.createNewFile();
	}
	
	/**
	 * Deletes a file.
	 * @param location	The location of the file.
	 * @param name		The name of the file.
	 * @return	True when the file is deleted, false when failed.
	 * @throws IOException
	 */
	public boolean deleteFile(String location, String name) throws IOException
	{
		File file = new File(location + File.separator + name);
		if(fileExist(location, name))
		{
			return file.delete();
		}
		else 
		{
			return false;
		}
	}
	
	/**
	 * Copies a file to a new location.
	 * @param oldLocation	The old location of the file.
	 * @param newLocation	The new location where you want the file.
	 * @return	True when the file is moved, false when failed.
	 * @throws IOException
	 */
	public boolean copyFile(String oldLocation, String newLocation) throws IOException
	{
		try
		{
			File file = new File(oldLocation);
			File newFile = new File(newLocation);
			
			Files.copy(file.toPath(), newFile.toPath());
		}
		catch(Exception e)
		{
			System.err.println("Error while copying file '" + oldLocation + "': " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * Open a file if it is on the local host.
	 * If this is not the case, the file will be downloaded from SystemY first.
	 * @param location	The location of the file.
	 * @param name		The name of the file.
	 * @return	True when the file is opened successfully, false when failed.
	 * @throws 	IOException 
	 */
	public boolean openFile(String location, String name) throws IOException
	{
		File file = new File(location + File.separator + name);
		
		try
		{
			if(OSDetector.isWindows())
			{
				//Windows only.
				Desktop.getDesktop().open(file);
				return true;
			}
			else if(OSDetector.isLinux() || OSDetector.isMac())
			{
				Runtime.getRuntime().exec(new String[]{"/usr/bin/open", file.getAbsolutePath()});
				return true;
			}
			System.err.println("Opening files is not supported for: " + OSDetector.getOSVersion() + ". Please open the file manually.");
			return false;
		}
		catch(Exception e)
		{
			System.err.println("Can't open the file: " + name + ".");
			System.err.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * Creates a FileInputStream by opening a connection to an actual file, the file named by the File object file in the file system.
	 * @param location	The location of the file.
	 * @param name		The name of the file.
	 * @return	The created fileInputStream.
	 * @throws FileNotFoundException
	 */
	public FileInputStream getFileInputStream(String location, String name) throws FileNotFoundException
	{
		return new FileInputStream(getFile(location, name));
	}
	
	/**
	 * Creates a file output stream to write to the file represented by the specified File object.
	 * @param location	The location where the file needs to be written.
	 * @param name		The name of the file.
	 * @return	The created OutputStream.
	 * @throws FileNotFoundException
	 */
	public FileOutputStream getFileOutputStream(String location, String name) throws FileNotFoundException
	{
		return new FileOutputStream(getFile(location, name));
	}
	
	/**
	 * Checks if the file exists.
	 * @param location	The location of the file you want to check.
	 * @param name		The name of the file you want to check.
	 * @return	True when file exists, false when not.
	 */
	public boolean fileExist(String location, String name)
	{
		File file = new File(location + File.separator + name);
		if(file.isFile())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Get the fileWatchObserver.
	 * @return The observer of the file.
	 */
	public FileSystemObserver getFileWatchObserver()
	{
		return fileSystemWatcher.getObserver();
	}
	
	/**
	 * Sets the directory which needs to be watched.
	 * @param directory	The directory that needs to be watched.
	 */
	public void setFileWatchDirectory(String directory)
	{
		if(watcherThread.isAlive())
		{
			stopFileWatcher();
		}
		fileSystemWatcher.setPathLocation(directory);
	}
	
	/**
	 * Starts the file watcher.
	 */
	public void startFileWatcher()
	{
		if(watcherThread.getState() == Thread.State.NEW)
		{
			watcherThread.start();
		}
	}
	
	/**
	 * Stops the file watcher.
	 */
	public void stopFileWatcher()
	{
		if(fileSystemWatcher.stopWatcher())
		{
			watcherThread = new Thread(fileSystemWatcher);
		}
	}
}
