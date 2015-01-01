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
	 * @return observer		Observer of the TerminalReader
	 */
	public TerminalObserver getObserver()
	{
		return this.observer;
	}
	
	/**
	 * Runnable interface of the TerminalReader. Execute to get the input of the console once.
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