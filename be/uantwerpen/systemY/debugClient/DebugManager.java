package be.uantwerpen.systemY.debugClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;

import be.uantwerpen.systemY.client.Client;
import be.uantwerpen.systemY.interfaces.NodeLinkManagerInterface;
import be.uantwerpen.systemY.shared.Node;
import be.uantwerpen.systemY.server.*;

/**
 * Class that runs test on a given Client
 */
public class DebugManager
{
	private int tests = 0;
	private int passed = 0;
	private Client c1, c2, c3;
	private Server server;
	
	/**
	 * Does tests on 3 client instances
	 * @param Client1	One of the 3 clients made for the test.
	 * @param Client2	One of the 3 clients made for the test.
	 * @param Client3	One of the 3 clients made for the test.
	 */
	public DebugManager(Client c1, Client c2, Client c3, Server server)
	{		
		this.server = server;
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
		
		printDebugInfo("INFO", "Clients and server will now exit.");
		c1.exitSystem();
		c2.exitSystem();
		c3.exitSystem();
		server.exitServer();
	}
	
	/**
	 * Runs specific tests 1 by 1.
	 */
	private void runTests()
	{
		testBootstrap();
		//Logout kan vertekken vanuit de situatie gecreeerd door testBootstrap
		testLogout();
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
			Node prevNode1 = null;
			Node prevNode1s = null;
			Node nextNode1 = null;
			Node prevNode2 = null;
			Node nextNode2 = null;
			Node prevNode3 = null;
			Node nextNode3 = null;
			
			Runnable login1 = new Runnable()
			{
				@Override
				public void run()
				{
					assert c1.loginSystem(): "Failed to bootstrap client 1 (login failed)";
				}
			};
			
			new Thread(login1).start();
			
			printDebugInfo("INFO", "Wait for " + c1.getHostname() +" to finish startup.");
			
			while(!c1.getSessionState())
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
					System.err.println("Thread sleep: " + e.getMessage());
				}
				//hold off, wait for the client (stop blocking the client thread with polling)
				System.out.println("Waiting for node: " + c1.getHostname() + " ...");
			}
			
			NodeLinkManagerInterface iFace1 = (NodeLinkManagerInterface)c1.getRMIInterface("//localhost/NodeLinkManager_" + c1.getHostname());
			
			prevNode1 = iFace1.getPrev();
			prevNode1s = prevNode1;
			nextNode1 = iFace1.getNext();
			
			assert (prevNode1.equals(nextNode1)): "First node in network does not refer to itself";
			
			
			//Second Client		next and prev node should be referring to other node than itself
			prevNode1 = null;
			nextNode1 = null;
		
			Runnable login2 = new Runnable()
			{
				@Override
				public void run()
				{
					assert c2.loginSystem(): "Failed to bootstrap client 2 (login failed)";
				}
			};
			
			new Thread(login2).start();
			
			printDebugInfo("INFO", "Wait for " + c2.getHostname() +" to finish startup.");
			
			while(!c2.getSessionState())
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
					System.err.println("Thread sleep: " + e.getMessage());
				}
				//hold off, wait for the client (stop blocking the client thread with polling)
				System.out.println("Waiting for node: " + c2.getHostname() + " ...");
			}
			
			NodeLinkManagerInterface iFace2 = (NodeLinkManagerInterface)c2.getRMIInterface("//localhost/NodeLinkManager_" + c2.getHostname());
			
			prevNode1 = iFace1.getPrev();
			nextNode1 = iFace1.getNext();
			
			prevNode2 = iFace2.getPrev();
			nextNode2 = iFace2.getNext();
			
			assert(prevNode1.equals(nextNode1) && nextNode2.equals(prevNode2) && !(prevNode1.equals(prevNode1s))): "NodeLinks were not created correctly with 2 nodes";
			
			
			//Third Client		There should be a loop between nodes
			prevNode1 = null;
			nextNode1 = null;
			prevNode2 = null;
			nextNode2 = null;
			
			Runnable login3 = new Runnable()
			{
				@Override
				public void run()
				{
					assert c3.loginSystem(): "Failed to bootstrap client 3 (login failed)";
				}
			};
			
			new Thread(login3).start();

			printDebugInfo("INFO", "Wait for " + c3.getHostname() +" to finish startup.");
			while(!c3.getSessionState())
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
					System.err.println("Thread sleep: " + e.getMessage());
				}
				//hold off, wait for the client (stop blocking the client thread with polling)
				System.out.println("Waiting for node: " + c3.getHostname() + " ...");
			}
			
			NodeLinkManagerInterface iFace3 = (NodeLinkManagerInterface)c3.getRMIInterface("//localhost/NodeLinkManager_" + c3.getHostname());
			
			prevNode1 = iFace1.getPrev();
			nextNode1 = iFace1.getNext();
			
			prevNode2 = iFace2.getPrev();
			nextNode2 = iFace2.getNext();
			
			prevNode3 = iFace3.getPrev();
			nextNode3 = iFace3.getNext();
			
			assert(
					(nextNode1.equals(prevNode3) && nextNode2.equals(prevNode1) && nextNode3.equals(prevNode2))		//1-2-3
					|| (nextNode3.equals(prevNode1) && nextNode2.equals(prevNode3) && nextNode1.equals(prevNode2))	//3-2-1
					): "NodeLinks were not created correctly with 3 nodes";
			
			printDebugInfo("Bootstrap", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Bootstrap", e.getMessage());
		}
		catch(NullPointerException | RemoteException e) 
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
			Node prevNode2 = null;
			Node nextNode2 = null;
			Node prevNode3 = null;
			Node nextNode3 = null;
			
			NodeLinkManagerInterface iFace2 = (NodeLinkManagerInterface)c2.getRMIInterface("//localhost/NodeLinkManager_" + c2.getHostname());
			NodeLinkManagerInterface iFace3 = (NodeLinkManagerInterface)c3.getRMIInterface("//localhost/NodeLinkManager_" + c3.getHostname());
			
			//first client		after logging out there should be a circle of 2 clients
			Runnable logout1 = new Runnable()
			{
				@Override
				public void run()
				{
					assert c1.logoutSystem(): "Failed to logout client 1";
				}
			};
			
			new Thread(logout1).start();
			
			printDebugInfo("INFO", "Wait for " + c1.getHostname() +" to finish shutdown.");
			while(c1.getSessionState())
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
					System.err.println("Thread sleep: " + e.getMessage());
				}
				//hold off, wait for the client (stop blocking the client thread with polling)
				System.out.println("Waiting for node: " + c1.getHostname() + " ...");
			}
			
			prevNode2 = iFace2.getPrev();
			nextNode2 = iFace2.getNext();
			
			prevNode3 = iFace3.getPrev();
			nextNode3 = iFace3.getNext();
			
			assert (nextNode2.equals(prevNode2) && nextNode3.equals(prevNode3)): "Failed to restore nodelinks when going from 3->2 clients";
			
			//Second Client
			Runnable logout2 = new Runnable()
			{
				@Override
				public void run()
				{
					assert c2.logoutSystem(): "Failed to logout client 2";
				}
			};
			
			new Thread(logout2).start();
			
			printDebugInfo("INFO", "Wait for " + c2.getHostname() +" to finish shutdown.");
			while(c2.getSessionState())
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(InterruptedException e)
				{
					System.err.println("Thread sleep: " + e.getMessage());
				}
				//hold off, wait for the client (stop blocking the client thread with polling)
				System.out.println("Waiting for node: " + c2.getHostname() + " ...");
			}
			
			prevNode3 = iFace3.getPrev();
			nextNode3 = iFace3.getNext();
			
			assert(prevNode3.equals(nextNode3)): "Failed to restore nodelinks when going from 2->1 clients";
			
			//Derde Client		There should be a loop between nodes
			
			assert c3.logoutSystem(): "Failed to logout client 3";
			//no more connected nodes -> nothing to check
			
			printDebugInfo("Logout ", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Shutdown", e.getMessage());
		}
		catch(NullPointerException | RemoteException e) 
		{
			System.err.println("NodeServer exception"+ e.getMessage());
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
			printDebugInfo("Failure test", "Press 'enter' to continu...");
			commandLineWait();
			
			printDebugInfo("Failure test", "Login three new nodes to the network and press 'enter' to continu...");
			commandLineWait();
			
			HashMap<Integer, Node> nodeList = server.getNodeList();
			assert(nodeList.size() == 3) : "The number of nodes do not equal 3! (" + nodeList.size() + " nodes available)";
			
			Node node1 = (Node)nodeList.values().toArray()[0];
			Node node2 = (Node)nodeList.values().toArray()[1];
			Node node3 = (Node)nodeList.values().toArray()[2];
			
			Node prevNode1 = null;
			Node nextNode1 = null;			
			Node prevNode2 = null;
			Node nextNode2 = null;
			Node prevNode3 = null;
			Node nextNode3 = null;
			
			NodeLinkManagerInterface iFace1 = null;
			NodeLinkManagerInterface iFace3 = null;
			NodeLinkManagerInterface iFace2 = null;
			
			try
			{
				iFace1 = (NodeLinkManagerInterface)Naming.lookup("//" + node1.getIpAddress() + "/NodeLinkManager_" + node1.getHostname());
				iFace2 = (NodeLinkManagerInterface)Naming.lookup("//" + node2.getIpAddress() + "/NodeLinkManager_" + node2.getHostname());
				iFace3 = (NodeLinkManagerInterface)Naming.lookup("//" + node3.getIpAddress() + "/NodeLinkManager_" + node3.getHostname());
			}
			catch(Exception e)
			{
				System.out.println("RMI lookup: " + e.getMessage());
			}
			
			//there should still be a loop between the 3 clients
			prevNode1 = iFace1.getPrev();
			nextNode1 = iFace1.getNext();
			
			prevNode2 = iFace2.getPrev();
			nextNode2 = iFace2.getNext();
			
			prevNode3 = iFace3.getPrev();
			nextNode3 = iFace3.getNext();
			
			assert(
					(nextNode1.equals(prevNode3) && nextNode2.equals(prevNode1) && nextNode3.equals(prevNode2))		//1-2-3
					|| (nextNode1.equals(prevNode2) && nextNode3.equals(prevNode1) && nextNode2.equals(prevNode3))	//1-3-2
					): "NodeLinks were not created correctly with 3 nodes";
			
			printDebugInfo("Failure test", "Terminate the node: '" + node2.getHostname() + "' and press 'enter' to continu...");
			commandLineWait();
			
			printDebugInfo("Failure test", "Execute the 'testLinks' command on one of the nodes and press 'enter' to continu...");
			commandLineWait();
			
			//Error should be detected when calling c2 -> 2 client loop should be created
			prevNode1 = iFace1.getPrev();
			nextNode1 = iFace1.getNext();
			
			prevNode3 = iFace3.getPrev();
			nextNode3 = iFace3.getNext();
			
			assert(nextNode1.equals(prevNode1) && nextNode3.equals(prevNode3)): "NodeLinks aren't correct.";
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Failure", e.getMessage());
		}
		catch(NullPointerException | RemoteException e) 
		{
			System.err.println("NodeServer exception: "+ e.getMessage());
		}
	}
	
	/**
	 * Prints debug information.
	 * @param header	The nature of the message.
	 * @param message	The message that needs to be printed.
	 */
	private void printDebugInfo(String header, String message)
	{
		System.out.println("[DEBUG - " + header + "] - " + message);
	}
	
	private void commandLineWait()
	{
		try
		{
			new BufferedReader(new InputStreamReader(System.in)).readLine();
		}
		catch(IOException e)
		{
			System.err.println("BufferReader: " + e.getMessage());
		}
	}
}