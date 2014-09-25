package edu.asu.irs13;

import java.io.FileNotFoundException;
import java.io.IOException;
import static edu.asu.irs13.Utility.checkEigen;

import org.apache.lucene.index.IndexReader;

/*
 * PageRank class to compute the pageRank for the entire document corpus
 */
public class PageRank {
	
	static private double reset; // will store the reset matrix value. Currently it is 1/corpus size.
	static private double prob = 0.8; // the probability with which the user will not jump to some other link.
	static private double sink; // will store the value for sink nodes. Currently it is 1/corpus size.
	static private int iterations = 0; // to keep count of the iterations it takes to converge.
	
	/*
	 * Method computePageRank computes the pageRank vector.
	 * Input: IndexReader: Inverted index of the whole corpus.
	 * Input: LinkAnalysis: Indexed graph of the corpus.
	 * Output: pageRank vector(1d-array)
	 * Theory about pageRank: compute M* matrix which is c(M+Z)+ (1-c)K
	 * M is stochastic A' matrix, Z is sink matrix, K is reset matrix and c is the probability.
	 * 
	 */
	public static double[] computePageRank(IndexReader r, LinkAnalysis l) throws FileNotFoundException, IOException
	{
		long time = System.currentTimeMillis(); // for time analysis.
		int n = r.maxDoc();
		sink = 1.0/(double)n; // sink = 1/(corpus size)
		reset = 1.0/(double)n; // reset = 1/(corpus size)
		double[] pageRank = new double[n]; // pageRank vector created and initialized
		for(int i = 0; i < n; i++)
			pageRank[i] = 1.0/(double)n;
		double[] vec = new double[n]; // vector created to store the previous pageRank vector
		double max = 0.0; // to store the max pageRank value of a doc. Used to normalize the vector.
		double min = 0.0;
		do
		{
			long time2 = System.currentTimeMillis(); // to analyze time taken for each iteration.
			vec = pageRank;
			max = 0.0;
			min = 1.0;
			pageRank = new double[n]; // pageRank given new memory basically to overwrite old values.
			for(int k = 0; k < n; k++) // for each row of matrix M*
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
					pageRank[k] += (val*prob + reset*(1-prob))*vec[i]; // the summation will compute one cell of pageRank vector.
				}
				if(max < pageRank[k]) // to get the max value of the current pageRank vector.
					max = pageRank[k];
				if(min > pageRank[k])
					min = pageRank[k];
			}
			iterations++; // number of iterations incremented
			time2 = System.currentTimeMillis() - time2; // time taken for each iteration.
			System.out.println("[PR] Completed iteration " +iterations+ " in " +time2+ " milliseconds");
		}while(!checkEigen(vec, pageRank)); // check if the vector converge.
		
		for(int i = 0; i < n; i++)
		{
			pageRank[i] = (pageRank[i] - min)/(max - min); // scale between 0 and 1
		}
		time = (System.currentTimeMillis() - time)/1000; // total time taken to compute pageRank.
		System.out.println("Page Rank computed in: " +time+ " seconds");
		return pageRank; // pageRank computed. yuppiee!!
	}
}
