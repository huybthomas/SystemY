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
	 * sets up the RMIsevice and multicastservice
	 * @param String	networkIP
	 * @param int	rmiPort
	 * @param String	multicastIP
	 * @param int	multicastPort
	 */
	public Networkinterface(String networkIP, int rmiPort, int tcpSendPort, int tcpReceivePort, String multicastIP, int multicastPort)
	{
		//Setup RMIservice
		this.rmiService = new RMIservice(networkIP, rmiPort);
		
		//Setup multicastservice
		this.multicastservice = new Multicastservice(multicastIP, multicastPort);
		
		//Setup TCPservice
		this.tcpService = new TCPservice(tcpSendPort, tcpReceivePort);
		
		multicastThread = new Thread(multicastservice);
		tcpThread = new Thread(tcpService);
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
	 * @return boolean True if successful, false if failed
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
