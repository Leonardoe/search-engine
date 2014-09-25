package edu.asu.irs13;

import static java.lang.Math.abs;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;

public class PrioritizedPageRank {

	
	static private double reset; // will store the reset matrix value. Currently it is 1/corpus size.
	static private double prob = 0.8; // the probability with which the user will not jump to some other link.
	static private double sink; // will store the value for sink nodes. Currently it is 1/corpus size.
	static private int iterations = 0; // to keep count of the iterations it takes to converge.
	
	public static double[] computePageRank(IndexReader r, LinkAnalysis l) throws FileNotFoundException, IOException
	{
		long time = System.currentTimeMillis(); // for time analysis.
		int n = r.maxDoc();
		sink = 1.0/(double)n; // sink = 1/(corpus size)
		reset = 1.0/(double)n; // reset = 1/(corpus size)
		double[][] pageRank = new double[n][2]; // pageRank vector created and initialized
		for(int i = 0; i < n; i++)
		{
			pageRank[i][0] = 1.0/(double)n;
			pageRank[i][1] = 1; // to check if further computation is required or not.
		}
		double[][] vec = new double[n][2]; // vector created to store the previous pageRank vector
		
		do
		{
			iterations++; // number of iterations incremented
			long time2 = System.currentTimeMillis(); // to analyze time taken for each iteration.
			vec = pageRank;
			pageRank = new double[n][2]; // pageRank given new memory basically to overwrite old values.
			for(int k = 0; k < n; k++) // for each row of matrix M*
			{
				if(vec[k][1] == iterations) // if true, further computation for this index is required.
				{	
					int[] cits = l.getCitations(k); // will give us cells which will be non-zero in this row.
					boolean[] my_cits = new boolean[n]; // boolean array to store these cells.
					for(int c = 0; c < cits.length; c++)
						my_cits[cits[c]] = true;
					for(int i = 0; i < n; i++) // for each column of matrix M*
					{
						double val = 0.0;
						int[] link = l.getLinks(i);
						if(link.length > 0) // not a sink node.
						{
							if(my_cits[i]) // if true then non-zero value else zero.
								val = 1.0/(double)link.length;
						}
						else // sink node.
							val = sink;
						pageRank[k][0] += (val*prob + reset*(1-prob))*vec[i][0]; // the summation will compute one cell of pageRank vector.
					}
					pageRank[k][1] = iterations;
				}
				else // resave the previous values.
				{
					pageRank[k][0] = vec[k][0];
					pageRank[k][1] = vec[k][1];
				}
			}
			time2 = System.currentTimeMillis() - time2; // time taken for each iteration.
			System.out.println("Completed iteration " +iterations+ " in " +time2+ " milliseconds");
		}while(!checkEigen(vec, pageRank)); // check if the vector converge.
		
		double max = 0.0; // to store the max pageRank value of a doc. Used to normalize the vector.
		double min = 1.0;
		double[] returnPageRank = new double[n];
		for(int ind = 0; ind < n; ind++)
		{
			if(max < pageRank[ind][0]) // to get the max value of the current pageRank vector.
				max = pageRank[ind][0];
			if(min > pageRank[ind][0])
				min = pageRank[ind][0];
			returnPageRank[ind] = pageRank[ind][0];
		}
		
		for(int i = 0; i < n; i++)
		{
			returnPageRank[i] = (returnPageRank[i] - min)/(max - min); // TODO: the scaling is incorrect.
		}
		time = (System.currentTimeMillis() - time)/1000; // total time taken to compute pageRank.
		System.out.println("Page Rank computed in: " +time+ " seconds");
		
		return returnPageRank; // pageRank computed. yuppiee!!
	}
	
	static boolean checkEigen(double[][] a, double[][]b)
	{
		int i = 0;
		boolean flag = true;
		while(i < a.length)
		{
			if(abs(a[i][0] - b[i][0]) > 0.000001)
			{
				b[i][1] = iterations +1;
				flag = false;
			}
			i++;
		}
		return flag;
	}
	
}
