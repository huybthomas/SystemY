package be.uantwerpen.systemY.fileSystem;

import java.io.*;
import javax.xml.bind.*;

public class FileManager
{
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
			System.out.println("JAXB Marshalling: " + e.getMessage());
			return false;
		}
		catch(IOException e)
		{
			System.out.println("IOException: " + e.getMessage());
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
			System.out.println("JAXB Unmarshalling: " + e.getMessage());
		}
		return object;
	}
}
