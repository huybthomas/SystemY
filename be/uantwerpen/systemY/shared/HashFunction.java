package be.uantwerpen.systemY.shared;

/**
 * Class that enables the calculation of a hash value of a string.
 */
public class HashFunction
{
	/**
	 * Calculates the hash of the given string value.
	 * 
	 * @return 	The calculated hash value between 0 and 32767.
	 */
	public int getHash(String string)
	{
		int i = string.hashCode();
		i = Math.abs(i % 32768);
		return i;
	}
}
