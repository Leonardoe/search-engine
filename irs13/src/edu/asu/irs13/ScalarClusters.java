package edu.asu.irs13;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.TreeSet;

import edu.asu.irs13.PreprocessData.DocIndex;

public class ScalarClusters {
	
	private static int returnCount = 5;
	private static int sck = 10;
	private static int size = sck;
	public static String[] getScalarClusters(IndexTable table, String query){
		return getScalarClusters(table.toSortedArray(), query);
	}
	public static String[] getScalarClusters(double[][] results, String query){
		if(size > results.length)
			size = results.length;
		int[] docIds = new int[size];
		for(int i = 0; i < size; i++)
		{
			docIds[i] = (int)results[i][1];
		}
		return getScalarClusters(docIds, query);
	}
	public static String[] getScalarClusters(int[] docIds, String query){
		// TODO: check for inputs.
		String[] queryTerms = query.split("\\s+");
		ArrayList<String> terms = new ArrayList<String>();
		if(size > docIds.length)
			size = docIds.length;
		//boolean enableFilter = true;
		for(int i = 0; i < size; i++){
			int docId = (int)docIds[i];
			DocIndex d = PreprocessData.docIndexer[docId];
			PreprocessData.getDocIndex(docId);
			Enumeration<String> keys = d.index.keys();
			while(keys.hasMoreElements()){
				String termIndex = keys.nextElement();
				if(checkTerm(termIndex, queryTerms))
				{
					/*if(!Utility.filterPassed(termIndex))
						enableFilter = false;*/
					if(!terms.contains(query))
						terms.add(query);
				}
				else
				{
					if(/*enableFilter && */PreprocessData.indexer.containsKey(termIndex) && PreprocessData.indexer.get(termIndex) > 0.2 && Utility.allFilter(termIndex))
						if(!terms.contains(termIndex))
							terms.add(termIndex);
				}
			}
		}
		// we have computed the terms which are there in these documents.
		double[][] td = new double[terms.size()][size];
		
		for(int i = 0; i < size; i++){
			DocIndex d = PreprocessData.docIndexer[docIds[i]];
			Enumeration<String> keys = d.index.keys();
			while(keys.hasMoreElements()){
				String termIndex = keys.nextElement();
				if(checkTerm(termIndex, queryTerms))
				{
					int ind = terms.indexOf(query);
					td[ind][i] += d.index.get(termIndex);
				}
				else if(terms.contains(termIndex))
				{
					int ind = terms.indexOf(termIndex);
					td[ind][i] = d.index.get(termIndex);
				}
			}
		}
		freeDocIndex(docIds);
		double[][] sc = mulMat(td, terms.indexOf(query), terms);
		td = null;
		double[][] temp_array = new double[sc.length][2];
		for(int i = 0; i < sc.length; i++){
			String currentTerm = terms.get((int)sc[i][1]);
			double idf = PreprocessData.indexer.get(currentTerm);
			temp_array[i][0] = idf*sc[i][0];
			temp_array[i][1] = sc[i][1];
		}
		Arrays.sort(temp_array, new Comparator<double[]>() {
		    public int compare(double[] a, double[] b) {
		        return Double.compare(b[0], a[0]);
		    }
		});
		System.out.println("");
		String[] similarTerms = new String[returnCount];
		for(int i = 0; i < returnCount; i++){
			String currentTerm = terms.get((int)temp_array[i][1]);
			//double idf = PreprocessData.indexer.get(currentTerm);
			//System.out.println(currentTerm+ " : "+idf);
			System.out.print(currentTerm+", ");
			similarTerms[i] = currentTerm;
		}
		System.out.println("");
		return similarTerms;
	}
	
	private static double[][] mulMat(double[][] mat, int queryIndex, ArrayList<String> terms){
		int row = mat.length;
		double[][] tt = new double[row][row];
		double[] diag = new double[row];
		
		// mat*mat'
		for(int i = 0; i < row; i++)
		{
			for(int j = 0; j < row; j++)
			{
				for(int k = 0; k < size; k++)
				{
					tt[i][j] += mat[i][k]*mat[j][k];
				}
				if(i == j)
					diag[i] = tt[i][i];
			}
		}
		double[] mag = new double[row];
		
		// association cluster // s12 = s12/(s11 + s22 - s12)
		for(int i = 0; i < row; i++)
		{
			for(int j = 0; j < row; j++)
			{
				tt[i][j] = tt[i][j]/(diag[i] + diag[j] - tt[i][j]);
				mag[i] += Math.pow(tt[i][j], 2.0);
			}
			mag[i] = 1/Math.sqrt(mag[i]);
		}
		diag = null;
		
		// normalized scalar cluster // s12 = s1.s2/(|s1|*|s2|)
		ScalarSet testSet = new ScalarSet(); // TODO: change the name.
		for(int j = 0; j < row; j++)
		{
			double computeJ = 0.0;
			for(int k = 0; k < size; k++)
			{
				computeJ += (tt[queryIndex][k]*tt[j][k]*mag[queryIndex]*mag[j]);
			}
			if(j != queryIndex/* && Utility.isValidString(terms.get(j))*/)
				testSet.add(j, computeJ);
		}
		return testSet.index;
	}
	private static void freeDocIndex(int[] results)
	{
		if(results.length != 0)
			for(int i = 0; i < size; i++)
			{
				int docId = results[i];
				PreprocessData.docIndexer[docId].index = null;
			}
	}
	private static boolean checkTerm(String term, String[] queryTerms)
	{
		for(String word: queryTerms)
			if(word.toLowerCase().equals(term))
				return true;
		return false;
	}
}

class ScalarSet{
	int count = 10;
	TreeSet<Double> sim = new TreeSet<Double>();
	double[][] index = new double[count][2];
	void add(int ind, double value){
		if(sim.size() < count)
		{
			int size = sim.size();
			sim.add(value);
			index[size][0] = value;
			index[size][1] = ind;
		}
		else{
			double first = sim.first();
			if(first < value)
			{
				sim.pollFirst(); //TODO: can use remove as the object is know.
				sim.add(value);
				for(int i = 0; i < count; i++)
				{
					if(index[i][0] == first )
					{
						index[i][0] = value;
						index[i][1] = ind;
						break;
					}
				}
			}
		}
	}
}
