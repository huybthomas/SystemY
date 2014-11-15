package be.uantwerpen.systemY.timeOut;

import java.util.Timer;
import java.util.TimerTask;

public class TimeOutService
{
	private long timeoutTime;
	private TimeOutObserver observer;
	private Timer timer;
	
	public TimeOutService(long timeout)
	{
		this.timeoutTime = timeout;
		this.observer = new TimeOutObserver();
		this.timer = new Timer();
	}
	
	public void setTimeOutTime(long timeout)
	{
		this.timeoutTime = timeout;
	}
	
	public TimeOutObserver getObserver()
	{
		return this.observer;
	}
	
	public void startTimer()
	{
		try
		{
			timer.cancel();
			
			timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					observer.setChanged();
					observer.notifyObservers();
				}
			}, timeoutTime);
		}
		catch(IllegalArgumentException | IllegalStateException e)
		{
			System.err.println("Timer: " + e.getMessage());
		}
	}
	
	public void stopTimer()
	{
		timer.cancel();
	}
}
