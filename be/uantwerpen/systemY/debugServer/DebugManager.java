package be.uantwerpen.systemY.debugServer;

import be.uantwerpen.systemY.server.Server;

public class DebugManager
{
	private Server server;
	private int tests = 0;
	private int passed = 0;
	
	/**
	 * runs tests on the server
	 * @param server
	 */
	public DebugManager(Server server)
	{		
		this.server = server;
		
		printDebugInfo("INFO", "Server debugmode active, running tests...");
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
		
		printDebugInfo("INFO", "Server will now exit.");
		server.exitServer();
	}
	
	/**
	 * identifies which tests needs to be run
	 */
	private void runTests()
	{
		test1(server);
		test2(server);
		test3(server);
		test4(server);
		test5(server);
		test6(server);
		test7(server);
	}
	
	/**
	 * test the add node function
	 * @param server
	 */
	private void test1(Server server)
	{
		printDebugInfo("Test 1", "Add a new node");
		tests++;
		try 
		{
			assert server.addNode("Node1", "192.168.0.2"): "Failed to add 'Node1'!";
			printDebugInfo("Test 1", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Test 1", e.getMessage());
		}
	}
	
	/**
	 * tests the safety against adding 2 nodes with the same name
	 * @param server
	 */
	private void test2(Server server)
	{
		printDebugInfo("Test 2", "Adding an already existing node");
		tests++;
		try 
		{
			assert server.addNode("Node2", "192.168.0.2"): "Failed to add 'Node2'!";
			assert !server.addNode("Node2", "192.168.0.3"): "Error, 'Node2' is accepted twice!";
			printDebugInfo("Test 2", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Test 2", e.getMessage());
		}
	}
	
	/**
	 * testing delete a node
	 * @param server
	 */
	private void test3(Server server)
	{
		printDebugInfo("Test 3", "Deleting a node");
		tests++;
		try
		{
			assert server.addNode("Node3", "192.168.0.4"): "Failed to add 'Node3'!";
			assert server.delNode("Node3"): "Failed to delete 'Node3'!";
			printDebugInfo("Test 3", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Test 3", e.getMessage());
		}
	}
	
	/**
	 * test: delete a node that does not exist.
	 * @param server
	 */
	private void test4(Server server)
	{
		printDebugInfo("Test 4", "Deleting a non-existing node");
		tests++;
		try
		{
			assert !server.delNode("Node4"): "Error, 'Node4' is deleted!";
			printDebugInfo("Test 4", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Test 4", e.getMessage());
		}
	}
	
	/**
	 * Test: Save Node List.
	 * @param server
	 */
	private void test5(Server server)
	{
		printDebugInfo("Test 5", "Saving the node list");
		tests++;
		try
		{
			assert server.saveNodeList("data/nodeList.XML"): "Failed to save the node list!";
			printDebugInfo("Test 5", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Test 5", e.getMessage());
		}
	}
	
	/**
	 * Test: load the node list.
	 * @param server
	 */
	private void test6(Server server)
	{
		printDebugInfo("Test 6", "Loading the node list");
		tests++;
		try
		{
			assert server.loadNodeList("data/nodeList.XML"): "Failed to save the node list!";
			printDebugInfo("Test 6", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Test 6", e.getMessage());
		}
	}
	
	/**
	 * Test: search file with hash value smaller then smallest hash of the node list.
	 * @param server
	 */
	private void test7(Server server)
	{
		printDebugInfo("Test 7", "Search file with hash-value smaller than smallest hash-value in the node list");
		tests++;
		try
		{
			server.addNode("SMALL", "0.0.0.0");		//HASH: 7399
			server.addNode("LARGE", "1.1.1.1");		//HASH: 17179
			assert server.getFileLocation("MINI") == "1.1.1.1": "Returned node isn't the highest hash-value in the node list!";	//HASH: 7255
			printDebugInfo("Test 7", "Passed");
			passed++;
		}
		catch(AssertionError e)
		{
			printDebugInfo("Test 7", e.getMessage());
		}
	}
	
	/**
	 * Prints debug information.
	 * @param header
	 * @param message
	 */
	private void printDebugInfo(String header, String message)
	{
		System.out.println("[DEBUG - " + header + "] - " + message);
	}
}