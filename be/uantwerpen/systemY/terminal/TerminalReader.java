package be.uantwerpen.systemY.terminal;

import java.io.*;

/**
 * Runnable class that reads the console input and notified all observer connected to it with the input.
 */
public class TerminalReader implements Runnable
{
	private TerminalObserver observer;
	
	/**
	 * Constructor of TerminalReader
	 */
	public TerminalReader()
	{
		this.observer = new TerminalObserver();
	}
	
	/**
	 * Get the observer of the TerminalReader. Observer will notified when a new input has been entered.
	 * @return The observer instance of the TerminalReader.
	 */
	public TerminalObserver getObserver()
	{
		return this.observer;
	}
	
	/**
	 * Run code of the TerminalReader. 
	 * This will be executed to get the input of a console line.
	 */
	@Override
	public void run()
	{
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.print("# ");

		try
		{
			String command = input.readLine();
			this.observer.setChanged();
			this.observer.notifyObservers(command);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}