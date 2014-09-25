package edu.asu.irs13;

import java.util.ArrayList;
import java.util.Enumeration;

import edu.asu.irs13.PreprocessData.DocIndex;

public class ScalarClusters {
	
	private static int sck = 10;
	private static int size = sck;
	public static void getScalarClusters(double[][] results, PreprocessData pd, String query){
		String[] queryTerms = query.split("\\s+");
		ArrayList<String> terms = new ArrayList<String>();
		if(size > results.length)
			size = results.length;
		for(int i = 0; i < size; i++){
			int docId = (int)results[i][1];
			DocIndex d = PreprocessData.docIndexer[docId];
			if(d.index == null)
				PreprocessData.getDocIndex(docId);
			else
				System.out.println("I'm not null"); //TODO: remove this.
			Enumeration<String> keys = d.index.keys();
			while(keys.hasMoreElements()){
				String termIndex = keys.nextElement();
				//if(PreprocessData.indexer.containsKey(termIndex) && PreprocessData.indexer.get(termIndex) > 0.8)
				if(checkTerm(termIndex, queryTerms))
				{
					if(!terms.contains(query))
						terms.add(query);
				}
				else
				{
					if(!terms.contains(termIndex))
						terms.add(termIndex);
				}
			}
		}
		// we have computed the terms which are there in these documents.
		double[][] td = new double[terms.size()][size];
		
		for(int i = 0; i < size; i++){
			int docId = (int)results[i][1];
			DocIndex d = PreprocessData.docIndexer[docId];
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
		freeDocIndex(results);
		double[][] sc = mulMat(td);
		td = null;
		//ArrayList<String> queryElaborated = new ArrayList<String>();
				int wordIndex = terms.indexOf(query);
				double[] vec = sc[wordIndex];
				//int j = 0;
				double maxIdf = 0.0;
				double maxCo = 0.0;
				int index3 = 0;
				
				double secondCo = 0.0;
				int index4 = 0;
				
				int index1 = 0;
				int index2 = 0;
				double secondMax = 0.0;
				for(int i = 0; i < vec.length; i++){
					if(i != wordIndex)
					{
						String elaTerm = terms.get(i);
						double idf = PreprocessData.indexer.get(elaTerm); // TODO: null check
						//if(idf > 0.8)
							if(vec[i] > maxCo)
							{
								secondCo = maxCo;
								index4 = index3;
								maxCo = vec[i];
								index3 = i;
							}
							else if(vec[i] > secondCo)
							{
								secondCo = vec[i];
								index4 = i;
							}
						
						double div = 3.0;
						if(Utility.isValidString(elaTerm))
							if((idf/div + vec[i]) > maxIdf)
							{
								index2 = index1;
								index1 = i;
								secondMax = maxIdf;
								maxIdf = (idf/div + vec[i]);
							}
							else if((idf/div + vec[i]) > secondMax)
							{
								index2 = i;
								secondMax = (idf/div + vec[i]);
							}
					}
				}
				System.out.println("max term: " +terms.get(index3)+" value: "+maxCo+ " idf: "+PreprocessData.indexer.get(terms.get(index3)));
				System.out.println("max term: " +terms.get(index4)+" value: "+secondCo+ " idf: "+PreprocessData.indexer.get(terms.get(index4)));
				System.out.println("term: " +terms.get(index1)+" value: "+maxIdf);
				System.out.println("term: " +terms.get(index2)+" value: "+secondMax);
		/*for(String word : queryTerms)
		{
			if(terms.contains(word))
			{
				int wordIndex = terms.indexOf(word);
				double[] vec = sc[wordIndex];
				//int j = 0;
				double maxIdf = 0.0;
				double maxCo = 0.0;
				int index3 = 0;
				
				double secondCo = 0.0;
				int index4 = 0;
				
				int index1 = 0;
				int index2 = 0;
				double secondMax = 0.0;
				for(int i = 0; i < vec.length; i++){
					if(i != wordIndex)
					{
						String elaTerm = terms.get(i);
						double idf = PreprocessData.indexer.get(elaTerm);
						if(idf > 0.8)
							if(vec[i] > maxCo)
							{
								secondCo = maxCo;
								index4 = index3;
								maxCo = vec[i];
								index3 = i;
							}
							else if(vec[i] > secondCo)
							{
								secondCo = vec[i];
								index4 = i;
							}
						
						double div = 1.0;
						if(Utility.isValidString(elaTerm))
							if((idf/div + vec[i]) > maxIdf)
							{
								index2 = index1;
								index1 = i;
								secondMax = maxIdf;
								maxIdf = (idf/div + vec[i]);
							}
							else if((idf/div + vec[i]) > secondMax)
							{
								index2 = i;
								secondMax = (idf/div + vec[i]);
							}
					}
				}
				queryElaborated.add(terms.get(index1));
				queryElaborated.add(terms.get(index2));
				System.out.println("max term: " +terms.get(index3)+" value: "+maxCo+ " idf: "+PreprocessData.indexer.get(terms.get(index3)));
				System.out.println("max term: " +terms.get(index4)+" value: "+secondCo+ " idf: "+PreprocessData.indexer.get(terms.get(index4)));
				System.out.println("term: " +terms.get(index1)+" value: "+maxIdf);
				System.out.println("term: " +terms.get(index2)+" value: "+secondMax);
			}
		}*/
		//System.out.println("timepass");
	}
	
	private static double[][] mulMat(double[][] mat){
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
		double[][] sc = new double[row][row];
		for(int i = 0; i < row; i++)
		{
			for(int j = 0; j < row; j++)
			{
				for(int k = 0; k < size; k++)
				{
					sc[i][j] += (tt[i][k]*tt[j][k]*mag[i]*mag[j]);
				}
			}
		}
		// scaling [0,1]
		for(int i = 0; i < row; i++)
		{
			for(int j = 0; j < row; j++)
			{
				sc[i][j] = sc[i][j]/sc[i][i];
			}
		}
		return sc;
	}
	private static void freeDocIndex(double[][] results)
	{
		if(results.length != 0)
			for(int i = 0; i < size; i++)
			{
				int docId = (int)results[i][1];
				PreprocessData.docIndexer[docId].index = null;
			}
	}
	
	private static boolean checkTerm(String term, String[] queryTerms)
	{
		for(String word: queryTerms)
			if(word.equals(term))
				return true;
		return false;
	}
}
