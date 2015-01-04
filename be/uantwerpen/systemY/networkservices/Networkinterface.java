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
	 * Get the observer of the multicast service.
	 * @return The MulticastObserver object.
	 */
	public MulticastObserver getMulticastObserver()
	{
		return multicastservice.getObserver();
	}
	
	/**
	 * Start RMIServer.
	 * @return True if successful, false otherwise.
	 */
	public boolean startRMIServer()
	{
		return this.rmiService.startRMIServer();
	}
	
	/**
	 * Bind the RMI server.
	 * @param object	Object to bind.	
	 * @param bindName	Name of the service that needs to be bound.
	 * @return True if successful, false otherwise.
	 */
	public boolean bindRMIServer(Object object, String bindName)
	{
		return this.rmiService.bindRMIServer(object, bindName);
	}
	
	/**
	 * Unbind the RMI server.
	 * @param bindName 	Name of the service that needs to be unbound.
	 * @return True if successful, false otherwise.
	 */
	public boolean unbindRMIServer(String bindName)
	{
		return this.rmiService.unbindRMIServer(bindName);
	}
	
	/**
	 * Get the RMI Interface.
	 * @param bindLocation	The location where the RMI needs to be checked.
	 * @return True if successful, false otherwise.
	 */
	public Object getRMIInterface(String bindLocation)
	{
		return this.rmiService.getRMIInterface(bindLocation);
	}
	
	/**
	 * Set up the multicast service and returns success.
	 * @return True if successful, false otherwise.
	 */
	public boolean setupMulticastservice()
	{
		return multicastservice.setupMulticastservice();
	}
	
	/**
	 * Send a multicast message.
	 * @param message	The message that needs to be send.
	 * @return True if successful, false otherwise.
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
	 * @return True if successful, false otherwise.
	 */
	public boolean stopMulticastservice()
	{
		boolean state = multicastservice.terminate();
		multicastThread = new Thread(multicastservice);
		
		return state;
	}
	
	/**
	 * Create a TCP listener on the preset port.
	 * @return True if successful, false otherwise.
	 */
	public boolean setupTCPListener()
	{
		return tcpService.setupTCPListener();
	}
	
	/**
	 * Get the observer of the TCP service.
	 * @return The TCPObserver object.
	 */
	public TCPObserver getTCPObserver()
	{
		return tcpService.getTCPObserver();
	}
	
	/**
	 * Initiate the TCP listener thread.
	 */
	public void runTCPListener()
	{
		tcpThread.start();
	}
	
	/**
	 * Terminate the TCP listener thread.
	 * @return True if successful, false otherwise.
	 */
	public boolean stopTCPListener()
	{
		boolean state = tcpService.terminate();
		tcpThread = new Thread(tcpService);
			
		return state;
	}
	
	/**
	 * Get the object of the TCP connection with a given ip address. 
	 * @param destinationIP.
	 * @return The TCP connection object.
	 */
	public TCPConnection getTCPConnection(String destinationIP)
	{
		return tcpService.getConnection(destinationIP);
	}
}
