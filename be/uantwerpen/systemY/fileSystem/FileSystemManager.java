package be.uantwerpen.systemY.fileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
	 * @param object		The object to be saved
	 * @param fileLocation	The location for the file to be saved
	 * @return	boolean		True if successful, false otherwise
	 */
	public boolean saveXMLFile(Object object, String fileLocation)
	{
		try
		{
			File file = new File(fileLocation);
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
	 * @param typeClass		The type of object to be loaded
	 * @param fileLocation	The location of the file
	 * @return	boolean		True if successful, false otherwise
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
	
	public boolean createDirectory(String directory) throws SecurityException
	{
		File dir = new File(directory);
		return dir.mkdirs();
	}
	
	public boolean createFile(String location, String name) throws IOException
	{
		File file = new File(location + File.separator + name);
		return file.createNewFile();
	}
	
	public boolean deleteFile(String location, String name) throws IOException
	{
		File file = new File(location + File.separator + name);
		return file.delete();
	}
	
	public FileInputStream getFileInputStream(String location, String name) throws FileNotFoundException
	{
		return new FileInputStream(getFile(location, name));
	}
	
	public FileOutputStream getFileOutputStream(String location, String name) throws FileNotFoundException
	{
		return new FileOutputStream(getFile(location, name));
	}
	
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
	
	public FileSystemObserver getFileWatchObserver()
	{
		return fileSystemWatcher.getObserver();
	}
	
	public void setFileWatchDirectory(String directory)
	{
		if(watcherThread.isAlive())
		{
			stopFileWatcher();
		}
		fileSystemWatcher.setPathLocation(directory);
	}
	
	public void startFileWatcher()
	{
		watcherThread.start();
	}
	
	public void stopFileWatcher()
	{
		if(fileSystemWatcher.stopWatcher())
		{
			watcherThread = new Thread(fileSystemWatcher);
		}
	}
}
