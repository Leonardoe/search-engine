package edu.asu.irs13;

import java.util.*;

public class IndexTable extends Hashtable<Integer, Double>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// method to convert hashtable to double array and sort the array.
	public double[][] toSortedArray()
	{
		double[][] temp_array = new double[this.size()][2];
		Enumeration<Integer> enuKey = this.keys();
		int counter = 0;
		// loop to traverse all the keys. 
		while(enuKey.hasMoreElements())
		{
			// Array populated.
			temp_array[counter][1] = (double)enuKey.nextElement();
			temp_array[counter][0] =  this.get((int)temp_array[counter][1]);
			counter++;
		}
		// overload the sort function to sort the array.
		Arrays.sort(temp_array, new Comparator<double[]>() {
		    public int compare(double[] a, double[] b) {
		        return Double.compare(b[0], a[0]);
		    }
		});
		return temp_array;
	}
}
