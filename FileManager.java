package be.uantwerpen.systemY.server;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class FileManager {

	public static void saveNodeList(Object object, String fileLocation) {
		try {
			File file = new File(fileLocation);
			JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass()); 	//Java architecture for XML Binding (JAXB)
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();    			//Serializer from class to XML
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,  true); 	//Set better layout for XML output to file
			jaxbMarshaller.marshal(object, file);          							//Write marshalled object to file
		} catch(JAXBException e) {
			System.out.println("JAXB Marshalling: " + e.getMessage());
		}
	}	 


	public static Object loadNodeList(Object typeClass, String fileLocation) {
		Object object = null;

		try  {
			File file = new File(fileLocation);
			JAXBContext jaxbContext = JAXBContext.newInstance(typeClass.getClass());
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();   	//Deserializer from class to XML
			object = jaxbUnmarshaller.unmarshal(file);         						//Read XML file and reconstruct object
		} catch(JAXBException e) {
			System.out.println("JAXB Unmarshalling: " + e.getMessage());
		}
		return object;
	}
}
