package be.uantwerpen.systemY.timer;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService
{
	private long time;
	private TimerObserver observer;
	private Timer timer;
	private TimerTask timerTask;
	
	/**
	 * Sets the time and starts the timer.
	 * @param time	the time when the observers will be notified
	 */
	public TimerService(long time)
	{
		this.time = time;
		this.observer = new TimerObserver();
		this.timer = new Timer();
	}
	
	/**
	 * Set the duration of the timer
	 * @param time	the time when the observers will be notified
	 */
	public void setTimer(long time)
	{
		this.time = time;
	}
	
	public TimerObserver getObserver()
	{
		return this.observer;
	}
	
	/**
	 * Function to start the timer.
	 * Creates a new timer with the given time.
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
	
	private void timerFinished()
	{
		timerTask.cancel();
		observer.setChanged();
		observer.notifyObservers();
	}
}
