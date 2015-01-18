package be.uantwerpen.systemY.hashCalculator;

import java.util.Observable;
import java.util.Observer;

import be.uantwerpen.systemY.shared.HashFunction;
import be.uantwerpen.systemY.terminal.TerminalReader;

public class HashCalculator
{
	private static final String version = "v0.6";
	private static TerminalReader terminalReader;
	
	public static void main(String[] args)
	{
		//Setup terminal reader
		terminalReader = new TerminalReader();
		
		terminalReader.getObserver().addObserver(new Observer()
		{
			public void update(Observable source, Object object)
			{
				calcHash((String) object);
			}
		});
		
		System.out.println("SystemY 2014 Hash Calculator " + version + " - Created by: Thomas Huybrechts, Arthur Janssens, Quinten Van Hasselt, Dries Blontrock");
		System.out.println("\nEnter the stringvalue to caculate the hash value of:");
		activateTerminal();
	}
	
	/**
	 * Calculate the hash value for SystemY of the given string and print it to terminal
	 * @param value the String value to calculate the hash value of
	 */
	private static void calcHash(String value)
	{
		int hashValue = new HashFunction().getHash(value);
		System.out.println("--> Value: " + hashValue);
		activateTerminal();
	}
	
	/**
	 * Activates the terminal.
	 */
	private static void activateTerminal()
	{
		new Thread(terminalReader).start();
	}
}
