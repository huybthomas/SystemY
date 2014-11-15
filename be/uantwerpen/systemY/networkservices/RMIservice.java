package be.uantwerpen.systemY.networkservices;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Class that implements Remote Method Invocation services.
 */
public class RMIservice
{
	private String RMIip;
	private int RMIPort;
	
	/**
	 * Load the existing RMIip and RMIPort
	 * @param String	RMIip
	 * @param int	RMIPort
	 */
	public RMIservice(String RMIip, int RMIPort)
	{
		this.RMIip = RMIip;
		this.RMIPort = RMIPort;
	}
	
	/**
	 * Start the RMI server
	 * @return boolean	True if successful, false if failed
	 */
	public boolean startRMIServer() 
	{
		try
		{
			LocateRegistry.createRegistry(RMIPort);
		}
		catch(RemoteException e)
		{
			System.err.println("JAVA RMI registry already exists or port: " + RMIPort + " is already in use.");
			return false;
		}
		return true;
	}
	
	/**
	 * Binds the RMI server to specific object.
	 * @param Object	object to bind to
	 * @param String	bindName	RMI server name
	 * @return	boolean True if successful, false if failed
	 */
	public boolean bindRMIServer(Object object, String bindName)
	{
		String bindLocation = "//" + RMIip + ":" + RMIPort + "/" + bindName;
		
		try
		{
			Naming.bind(bindLocation, (Remote)object);
			System.out.println(object.getClass().getSimpleName() + " Server is ready at: " + bindLocation);
		}
		catch(RemoteException | MalformedURLException | AlreadyBoundException e)
		{
			System.err.println("JAVA RMI can't be bound to: " + bindLocation);
			return false;
		}
		catch(ClassCastException e)
		{
			System.err.println("JAVA RMI: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Unbind the RMI server.
	 * @param String	bindName	name of the server
	 * @return	boolean	True if successful, false if failed
	 */
	public boolean unbindRMIServer(String bindName)
	{
		String bindLocation = "//" + RMIip + ":" + RMIPort + "/" + bindName;
		
		try
		{
			Naming.unbind(bindLocation);
		}
		catch(MalformedURLException | NotBoundException e)
		{
			System.err.println("JAVA RMI registry doesn't exists.");
			return false;
		}
		catch(RemoteException e)
		{
			System.err.println("JAVA RMI registry can't be unbound from: " + bindLocation);
			return false;
		}
		return true;
	}
	
	/**
	 * Get the interface of a specific RMI
	 * @param String	bindLocation
	 * @return Object	interface object
	 */
	public Object getRMIInterface(String bindLocation)
	{
		try
		{
	        return Naming.lookup(bindLocation);
	    }
	    catch(Exception e)
	    {
	        System.err.println("RMI lookup: "+ e.getMessage());
	        return null;
	    }
	}
}
