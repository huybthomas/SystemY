package be.uantwerpen.systemY.fileSystem;

public class OSDetector
{
	private static boolean isWindows = false;
	private static boolean isLinux = false;
	private static boolean isMac = false;
	
	static
	{
		String OS = System.getProperty("os.name").toLowerCase();
		isWindows = OS.contains("win");
		isLinux = OS.contains("nux") || OS.contains("nix"); 	//Linux or Unix
		isMac = OS.contains("mac");
	}
	
	public static boolean isWindows()
	{
		return isWindows;
	}
	
	public static boolean isLinux()
	{
		return isLinux;
	}
	
	public static boolean isMac()
	{
		return isMac;
	}
	
	public static String getOSVersion()
	{
		return System.getProperty("os.name");
	}
}
