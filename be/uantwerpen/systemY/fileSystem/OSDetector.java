package be.uantwerpen.systemY.fileSystem;

public class OSDetector
{
	private static boolean isWindows = false;
	private static boolean isLinux = false;
	private static boolean isMac = false;
	
	/**
	 * Create a OSdetector object
	 * 
	 * This class detect what os the program is running on.
	 */
	static
	{
		String OS = System.getProperty("os.name").toLowerCase();
		isWindows = OS.contains("win");
		isLinux = OS.contains("nux") || OS.contains("nix"); 	//Linux or Unix
		isMac = OS.contains("mac");
	}
	
	/**
	 * Return true if the host is Windows
	 * @return true if Windows, false otherwise
	 */
	public static boolean isWindows()
	{
		return isWindows;
	}
	
	/**
	 * Return true if the host is Linux
	 * @return true if Linux, false otherwise
	 */
	public static boolean isLinux()
	{
		return isLinux;
	}
	
	/**
	 * Return true if the host is Mac
	 * @return true if Mac, false otherwise
	 */
	public static boolean isMac()
	{
		return isMac;
	}
	
	/**
	 * Return the OS's version
	 * @return the version number
	 */
	public static String getOSVersion()
	{
		return System.getProperty("os.name");
	}
}
