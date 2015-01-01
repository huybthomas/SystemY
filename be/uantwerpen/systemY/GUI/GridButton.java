package be.uantwerpen.systemY.GUI;

import javax.swing.JButton;

class GridButton extends JButton
{
	private static final long serialVersionUID = 1L;
	private int x;
	private int y;
	
	public GridButton(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public int getColumn()
	{
		return x;
	}
	
	public int getRow()
	{
		return y;
	}
}