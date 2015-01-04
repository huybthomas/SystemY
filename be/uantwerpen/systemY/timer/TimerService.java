package be.uantwerpen.systemY.timer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class that enables a timer in a separate thread.
 */
public class TimerService
{
	private long time;
	private TimerObserver observer;
	private Timer timer;
	private TimerTask timerTask;
	
	/**
	 * Sets the time and starts the timer.
	 * @param The time when the observers will be notified.
	 */
	public TimerService(long time)
	{
		this.time = time;
		this.observer = new TimerObserver();
		this.timer = new Timer();
	}
	
	/**
	 * Set the duration of the timer.
	 * @param The time when the observers will be notified.
	 */
	public void setTimer(long time)
	{
		this.time = time;
	}
	
	/**
	 * Get the observer instance of the TimerService.
	 * @return The observer instance of the TimerService.
	 */
	public TimerObserver getObserver()
	{
		return this.observer;
	}
	
	/**
	 * Function to start the timer.
	 * Creates a new timer with the given time in the construction of the TimerService.
	 */
	public void startTimer()
	{
		try
		{
			if(timerTask != null)
			{
				timerTask.cancel();
			}
			
			timerTask = (new TimerTask()
			{
				@Override
				public void run()
				{
					timerFinished();
				}
			});
			
			timer.schedule(timerTask, time);
		}
		catch(IllegalArgumentException | IllegalStateException e)
		{
			System.err.println("Timer: " + e.getMessage());
		}
	}
	
	/**
	 * Stops the timer.
	 */
	public void stopTimer()
	{
		timerTask.cancel();
	}
	
	/**
	 * Make the timer finish and notify the timer observer.
	 */
	private void timerFinished()
	{
		timerTask.cancel();
		observer.setChanged();
		observer.notifyObservers();
	}
}
