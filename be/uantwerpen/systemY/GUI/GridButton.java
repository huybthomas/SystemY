package be.uantwerpen.systemY.GUI;

import javax.swing.JButton;

class GridButton extends JButton
{
	private static final long serialVersionUID = 1L;
	private int x;
	private int y;
	
	/**
	 * A JButton that holds x and y information
	 * @param x
	 * @param y
	 */
	public GridButton(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Get the button's column.
	 * @return The column.
	 */
	public int getColumn()
	{
		return x;
	}
	
	/**
	 * Get the button's row.
	 * @return The row.
	 */
	public int getRow()
	{
		return y;
	}
}