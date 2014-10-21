package be.uantwerpen.systemY.debugClient;

import java.rmi.RemoteException;

import be.uantwerpen.systemY.client.Client;
import be.uantwerpen.systemY.interfaces.NodeManagerInterface;
import be.uantwerpen.systemY.shared.Node;

/**
 * Class that runs test on a given Client
 */
public class DebugManager
{
	private int tests = 0;
	private int passed = 0;
	private Client c1, c2, c3;
	
	/**
	 * Does tests on 3 client instances
	 * @param Client1
	 * @param Client2
	 * @param Client3
	 */
	public DebugManager(Client c1, Client c2, Client c3)
	{		
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
		
		printDebugInfo("INFO", "Clients debugmode active, running tests...");
		runTests();
		
		printDebugInfo("INFO", "Tests finished...");
		
		if(passed >= tests)
		{
			printDebugInfo("INFO", "Passed all " + tests + " tests.");
		}
		else
		{
			printDebugInfo("WARNING", "Passed " + passed + " of " + tests + " tests.");
		}
		
		printDebugInfo("INFO", "Clients will now exit.");
		c1.exitSystem();
		c2.exitSystem();
		c3.exitSystem();
	}
	
	/**
	 * Runs specific tests 1 by 1.
	 */
	private void runTests()
	{
		testBootstrap();
		//Logout kan vertekken vanuit de situatie gecreeerd door testBootstrap
		//testLogout();
		//test voor de failure
		testFailure();
	}
		
	/**
	 * This function requires runs with a hard coded ip (see funcion for details)
	 * It bootstraps 3 nodes into a network.
	 */
	private void testBootstrap()
	{
		tests++;
		try 
		{
			//First Client		next and prev node should be referring to itself
			String bindLocation = "//" + "localhost" + "/NodeServer";		//Hard coded ip (Change localhost)
			String hostname1 = c1.getHostname();
			Node prevNode1 = null;
			Node prevNode1s = null;
			Node nextNode1 = null;
			Node prevNode2 = null;
			Node nextNode2 = null;
			Node prevNode3 = null;
			Node nextNode3 = null;
			
			assert c1.loginSystem(): "Failed to bootstrap client 1 (login failed)";
			
			NodeManagerInterface iFace = (NodeManagerInterface)c1.getRMIInterface(bindLocation);
			
			prevNode1 = iFace.getPrevNode(hostname1);
			prevNode1s = prevNode1;
			nextNode1 = iFace.getNextNode(hostname1);
			
			assert (prevNode1.equals(nextNode1)): "First node in network does not refer to itself";
			
			//Second Client		next and prev node should be referring to other node than itself
			String hostname2 = c2.getHostname();
			prevNode1 = null;
			nextNode1 = null;
			
			assert c2.loginSystem(): "Failed to bootsrap client 2 (login failed)";
			
			prevNode1 = iFace.getPrevNode(hostname1);
			nextNode1 = iFace.getNextNode(hostname1);
			
			prevNode2 = iFace.getPrevNode(hostname2);
			nextNode2 = iFace.getNextNode(hostname2);
			
			assert(prevNode1.equals(nextNode1) && nextNode2.equals(prevNode2) && !(prevNode1.equals(prevNode1s))): "NodeLinks were not created correctly with 2 nodes";
			
			//Derde Client		There should be a loop between nodes
			String hostname3 = c3.getHostname();
			prevNode1 = null;
			nextNode1 = null;
			prevNode2 = null;
			nextNode2 = null;
			
			assert c3.loginSystem(): "Failed to bootsrap client 3 (login failed)";
			
			prevNode1 = iFace.getPrevNode(hostname1);
			nextNode1 = iFace.getNextNode(hostname1);
			
			prevNode2 = iFace.getPrevNode(hostname2);
			nextNode2 = iFace.getNextNode(hostname2);
			
			prevNode3 = iFace.getPrevNode(hostname3);
			nextNode3 = iFace.getNextNode(hostname3);
			
			assert(
					(nextNode1.equals(prevNode3) && nextNode2.equals(prevNode1) && nextNode3.equals(prevNode2))		//1-2-3
					|| (nextNode3.equals(prevNode1) && nextNode2.equals(prevNode3) && nextNode1.equals(prevNode2))	//3-2-1
					): "NodeLinks were not created correctly with 3 nodes";
			
			printDebugInfo("Bootstrap", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Bootstrap: ", e.getMessage());
		}
		catch (RemoteException e) 
		{
			System.err.println("NodeServer exception: "+ e.getMessage());
		}
	}
	
	/**
	 * Tests the network after a logout, do the nodes restore the network correctly?
	 */
	private void testLogout()
	{
		//Nodes moeten zichzelf herstellen na logout
		//Beginsituatie = 3 nodes
		tests++;
		try
		{	
			String bindLocation = "//" + "localhost" + "/NodeServer";		//Hard coded ip (Change localhost)
			String hostname2 = c2.getHostname();
			String hostname3 = c3.getHostname();
			Node prevNode2 = null;
			Node nextNode2 = null;
			Node prevNode3 = null;
			Node nextNode3 = null;
			
			//first client		after looging out there should be a circle of 2 clients
			assert c1.logoutSystem(): "Failed to logout client 1";
			
			NodeManagerInterface iFace = (NodeManagerInterface)c1.getRMIInterface(bindLocation);
			
			prevNode2 = iFace.getPrevNode(hostname2);
			nextNode2 = iFace.getNextNode(hostname2);
			
			prevNode3 = iFace.getPrevNode(hostname3);
			nextNode3 = iFace.getNextNode(hostname3);
			
			assert (nextNode2.equals(prevNode3) && nextNode3.equals(prevNode2)): "Failed to restore nodelinks when going from 3->2 clients";
			
			//Second Client
			assert c2.logoutSystem(): "Failed to logout client 2";
			
			prevNode3 = iFace.getPrevNode(hostname3);
			nextNode3 = iFace.getNextNode(hostname3);
			
			assert(prevNode3.equals(nextNode3)): "Failed to restore nodelinks when going from 2->1 clients";
			
			//Derde Client		There should be a loop between nodes
			
			assert c3.logoutSystem(): "Failed to logout last client";
			//no more connected nodes -> nothing to check
			
			printDebugInfo("Logout ", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Bootstrap: ", e.getMessage());
		}
		catch (RemoteException e) 
		{
			System.err.println("NodeServer exception: "+ e.getMessage());
		}
	}
	
	/**
	 * tests if the nodes change their neighbors in case of failure of a node.
	 * 3 Nodes will be created, one node will be rewritten to null.
	 * Request file from that node, other nodes will see that no node is present.
	 * Start failure process.
	 */
	private void testFailure()
	{
		try
		{
			tests++;
			
			String bindLocation = "//" + "localhost" + "/NodeServer";		//Hard coded ip (Change localhost)
			String hostname1 = c1.getHostname();
			String hostname2 = c2.getHostname();
			String hostname3 = c3.getHostname();
			Node prevNode1 = null;
			Node nextNode1 = null;			
			Node prevNode2 = null;
			Node nextNode2 = null;
			Node prevNode3 = null;
			Node nextNode3 = null;
			
			NodeManagerInterface iFace = (NodeManagerInterface)c1.getRMIInterface(bindLocation);
			
			//there should still be a loop between the 3 clients
			prevNode1 = iFace.getPrevNode(hostname1);
			nextNode1 = iFace.getNextNode(hostname1);
			
			prevNode2 = iFace.getPrevNode(hostname2);
			nextNode2 = iFace.getNextNode(hostname2);
			
			prevNode3 = iFace.getPrevNode(hostname3);
			nextNode3 = iFace.getNextNode(hostname3);
			
			assert(
					(nextNode1.equals(prevNode3) && nextNode2.equals(prevNode1) && nextNode3.equals(prevNode2))		//1-2-3
					|| (nextNode1.equals(prevNode2) && nextNode3.equals(prevNode1) && nextNode2.equals(prevNode3))	//1-3-2
					): "NodeLinks were not created correctly with 3 nodes";
			
			c2.serverConnectionFailure();
					
			boolean answer = c1.ping(new Node("failureNode","localhost"));
					
			//Error should be detected when calling c2 -> 2 client loop should be created
			prevNode1 = iFace.getPrevNode(hostname1);
			nextNode1 = iFace.getNextNode(hostname1);
			
			prevNode3 = iFace.getPrevNode(hostname3);
			nextNode3 = iFace.getNextNode(hostname3);
			
			assert(nextNode1.equals(prevNode1) && nextNode3.equals(prevNode3)): "NodeLinks aren't correct.";
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Bootstrap: ", e.getMessage());
		}
		catch(RemoteException e) 
		{
			System.err.println("NodeServer exception: "+ e.getMessage());
		}
	}
	
	/**
	 * Prints debug information.
	 * @param String	header	
	 * @param String	message	
	 */
	private void printDebugInfo(String header, String message)
	{
		System.out.println("[DEBUG - " + header + "] - " + message);
	}
}