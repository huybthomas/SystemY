package be.uantwerpen.systemY.networkservices;

/**
 * Class that includes all communication classes.
 */
public class Networkinterface
{
	private Multicastservice multicastservice;
	private RMIservice rmiService;
	private Thread multicastThread;
	
	/**
	 * sets up the RMIsevice and multicastservice
	 * @param String	networkIP
	 * @param int	rmiPort
	 * @param String	multicastIP
	 * @param int	multicastPort
	 */
	public Networkinterface(String networkIP, int rmiPort, String multicastIP, int multicastPort)
	{
		//Setup RMIservice
		this.rmiService = new RMIservice(networkIP, rmiPort);
		
		//Setup multicastservice
		this.multicastservice = new Multicastservice(multicastIP, multicastPort);
		
		multicastThread = new Thread(multicastservice);
	}
	
	/**
	 * request multicast observer
	 * @return MulticastObserver
	 */
	public MulticastObserver getMulticastObserver()
	{
		return multicastservice.getObserver();
	}
	
	/**
	 * start RMIServer
	 * @return boolean	True if successful, false if failed
	 */
	public boolean startRMIServer()
	{
		return this.rmiService.startRMIServer();
	}
	
	/**
	 * bind the RMI server
	 * @param Object	object to bind	
	 * @param String	bindName for bindlocation
	 * @return boolean	True if successful, false if failed
	 */
	public boolean bindRMIServer(Object object, String bindName)
	{
		return this.rmiService.bindRMIServer(object, bindName);
	}
	
	/**
	 * unbind the RMI server
	 * @param String 	bindName to unbind
	 * @return boolean	True if successful, false if failed
	 */
	public boolean unbindRMIServer(String bindName)
	{
		return this.rmiService.unbindRMIServer(bindName);
	}
	
	/**
	 * get the RMI Interface
	 * @param String	bindLocation
	 * @return boolean	True if successful, false if failed
	 */
	public Object getRMIInterface(String bindLocation)
	{
		return this.rmiService.getRMIInterface(bindLocation);
	}
	
	/**
	 * Set up the multi cast service and returns success.
	 * @return boolean	True if successful, false if failed
	 */
	public boolean setupMulticastservice()
	{
		return multicastservice.setupMulticastservice();
	}
	
	/**
	 * send a multicast message
	 * @param byte[]	message	to send
	 * @return boolean	True if successful, false if failed
	 */
	public boolean sendMulticast(byte[] message)
	{
		return multicastservice.sendMulticast(message);
	}
	
	/**
	 * start the multicast service
	 */
	public void runMulticastservice()
	{
		multicastThread.start();
	}
	
	/**
	 * stop the multicast service
	 */
	public void stopMulticastservice()
	{
		if(multicastservice.terminate())
		{
			multicastThread = new Thread(multicastservice);
		}
	}	
}
