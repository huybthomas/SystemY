package be.uantwerpen.systemY.networkservices;

/**
 * Class that includes all communication classes.
 */
public class Networkinterface
{
	private Multicastservice multicastservice;
	private RMIservice rmiService;
	private TCPservice tcpService;
	private Thread multicastThread;
	private Thread tcpThread;
	
	/**
	 * Sets up the RMIsevice and multicastservice.
	 * @param networkIP		The ip of the object that makes the network interface.
	 * @param rmiPort		The port on which the rmi call needs to happen.
	 * @param multicastIP	The ip of the multicast.
	 * @param multicastPort	The port on which the multicast needs to happen.
	 */
	public Networkinterface(String networkIP, int rmiPort, int tcpSendPort, int tcpReceivePort, String multicastIP, int multicastPort)
	{
		//Setup RMIservice
		this.rmiService = new RMIservice(networkIP, rmiPort);
		
		//Setup multicastservice
		this.multicastservice = new Multicastservice(multicastIP, networkIP, multicastPort);
		
		//Setup TCPservice
		this.tcpService = new TCPservice(tcpSendPort, tcpReceivePort);
		
		multicastThread = new Thread(multicastservice);
		tcpThread = new Thread(tcpService);
	}
	
	/**
	 * Request multicast observer.
	 * @return MulticastObserver
	 */
	public MulticastObserver getMulticastObserver()
	{
		return multicastservice.getObserver();
	}
	
	/**
	 * Start RMIServer.
	 * @return boolean	True if successful, false if failed
	 */
	public boolean startRMIServer()
	{
		return this.rmiService.startRMIServer();
	}
	
	/**
	 * Bind the RMI server.
	 * @param object	Object to bind.	
	 * @param bindName	Name of the service that needs to be bound.
	 * @return boolean	True if successful, false if failed
	 */
	public boolean bindRMIServer(Object object, String bindName)
	{
		return this.rmiService.bindRMIServer(object, bindName);
	}
	
	/**
	 * Unbind the RMI server.
	 * @param bindName 	Name of the service that needs to be unbound.
	 * @return boolean	True if successful, false if failed.
	 */
	public boolean unbindRMIServer(String bindName)
	{
		return this.rmiService.unbindRMIServer(bindName);
	}
	
	/**
	 * Get the RMI Interface.
	 * @param bindLocation	The location where the RMI needs to be checked.
	 * @return boolean	True if successful, false if failed.
	 */
	public Object getRMIInterface(String bindLocation)
	{
		return this.rmiService.getRMIInterface(bindLocation);
	}
	
	/**
	 * Set up the multicast service and returns success.
	 * @return boolean	True if successful, false if failed.
	 */
	public boolean setupMulticastservice()
	{
		return multicastservice.setupMulticastservice();
	}
	
	/**
	 * Send a multicast message.
	 * @param message	The message that needs to be send.
	 * @return boolean	True if successful, false if failed.
	 */
	public boolean sendMulticast(byte[] message)
	{
		return multicastservice.sendMulticast(message);
	}
	
	/**
	 * Start the multicast service.
	 */
	public void runMulticastservice()
	{
		if(multicastThread.getState() == Thread.State.NEW)
		{
			multicastThread.start();
		}
	}
	
	/**
	 * Stop the multicast service.
	 * @return boolean True if successful, false if failed.
	 */
	public boolean stopMulticastservice()
	{
		boolean state = multicastservice.terminate();
		multicastThread = new Thread(multicastservice);
		
		return state;
	}
	
	public boolean setupTCPListener()
	{
		return tcpService.setupTCPListener();
	}
	
	public TCPObserver getTCPObserver()
	{
		return tcpService.getTCPObserver();
	}
	
	public void runTCPListener()
	{
		tcpThread.start();
	}
	
	public boolean stopTCPListener()
	{
		boolean state = tcpService.terminate();
		tcpThread = new Thread(tcpService);
			
		return state;
	}
	
	public TCPConnection getTCPConnection(String destinationIP)
	{
		return tcpService.getConnection(destinationIP);
	}
}
