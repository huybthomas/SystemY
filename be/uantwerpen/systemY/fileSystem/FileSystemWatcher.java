package be.uantwerpen.systemY.fileSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import javax.swing.JFileChooser;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileSystemWatcher implements Runnable
{
	private Path pathlocation;
	private FileSystemObserver observer;
	private WatchService watcher;
	
	public FileSystemWatcher(String pathlocation)
	{
		try
		{
			this.pathlocation = FileSystems.getDefault().getPath(pathlocation);
		}
		catch(InvalidPathException e)
		{
			System.err.println("Invalid path: " + e.getMessage());
			this.pathlocation = FileSystems.getDefault().getPath(new JFileChooser().getFileSystemView().getDefaultDirectory().toString() + File.separator + "SystemY" + File.separator + "Files");
		}
		this.observer = new FileSystemObserver();
	}
	
	public FileSystemObserver getObserver()
	{
		return observer;
	}
	
	public boolean setPathLocation(String pathlocation)
	{
		try
		{
			this.pathlocation = FileSystems.getDefault().getPath(pathlocation);
		}
		catch(InvalidPathException e)
		{
			System.err.println("Invalid path: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean stopWatcher()
	{
		if(watcher != null)
		{
			try
			{
				watcher.close();
				return true;
			}
			catch(IOException e)
			{
				System.err.println("Watcher close: " + e.getMessage());
			}
		}
		return false;
	}
	
	@Override
	public void run()
	{
		WatchKey watchKey = null;
		
		try
		{
			watcher = FileSystems.getDefault().newWatchService();
			watchKey = pathlocation.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		}
		catch(InvalidPathException e)
		{
			System.err.println("Invalid path: " + e.getMessage());
			return;
		}
		catch(IOException e)
		{
			System.err.println("IO: " + e.getMessage());
			return;
		}
		
		while(true)
		{
			try
			{
				watchKey = watcher.take();
			}
			catch(InterruptedException e)	//Invoked when watcher is closed by the stopWachter() method
			{
				return;
			}
			
			for(WatchEvent<?> event : watchKey.pollEvents())
			{
				WatchEvent.Kind<?> kind = event.kind();
				
				if(kind == OVERFLOW)
				{
					continue;
				}
				
				observer.setChanged();
				observer.notifyObservers(observer.new FileNotification(kind.toString(), event.context().toString()));						 
			}
			
			if(!watchKey.reset())
			{
				System.err.println("FileWatch directory is no longer valid.");
				return;
			}
		}
	}
}
