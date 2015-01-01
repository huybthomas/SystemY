package be.uantwerpen.systemY.shared;

public class HashFunction
{
	/**
	 * Calculates the hash of the given string value
	 * 
	 * @return 	The calculated hash value
	 */
	public int getHash(String string)
	{
		int i = string.hashCode();
		i = Math.abs(i % 32768);
		return i;
	}
}
