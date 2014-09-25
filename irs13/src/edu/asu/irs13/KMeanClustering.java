package edu.asu.irs13;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeSet;

import edu.asu.irs13.PreprocessData.DocIndex;

public class KMeanClustering {
	
	private static int ck = 3;
	private static final int cn = 50;
	private static int size = cn;
	//static int[] probInd;
	//static int[] finalInd;
	
	public static void setClusterCount(int count){
		ck = count;
	}
	public static int getClusterCount(){
		return ck;
	}
	
	public static Clusters[] getClusters(IndexTable table){
		Clusters[] clusters = null;
		if(table.size() != 0){
			clusters = getClusters(table.toSortedArray());
		}
		return clusters;
	}
	
	public static Clusters[] getClusters(double[][] results){
		Clusters[] clusters = null;
		if(results.length != 0){
			if(size > results.length)
				size = results.length;
			int[] docIds = new int[size];
			for(int i = 0; i < size; i++)
			{
				docIds[i] = (int)results[i][1];
			}
			try {
				clusters = getClusters(docIds);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return clusters;
	}
	// first call.
	private static Clusters[] getClusters(int[] docIds) throws IOException{
		Clusters[] clusters = null;
		if(docIds.length != 0){
			System.out.println("Value of k: "+ck);
			double previousSim = 100.0;
			ArrayList<Hashtable<String, Double>> finalKCentroids = null;
			if(size > docIds.length)
				size = docIds.length;
			int[] storeDocIds = new int[size];
			for(int d = 0; d < size; d++)
				PreprocessData.getDocIndex(docIds[d]);
			int compute = 0;
			int coverganceCount = 0;
			while(compute++ < 10){
				ArrayList<Hashtable<String, Double>> kCentroids = init(docIds);
				boolean clusterConverged = false;
				double currentSim = 0.0;
				int[] nonZeroClusters = null;
				while(!clusterConverged)
				{
					//System.out.println("Iteration: "+compute);
					nonZeroClusters = new int[ck];
					clusterConverged = true;
					currentSim = 0.0;
					
					for(int i = 0; i < size; i++)
					{
						double[] sim = new double[ck];
						DocIndex d = PreprocessData.docIndexer[docIds[i]];
						Enumeration<String> keys = d.index.keys();
						while(keys.hasMoreElements())
						{
							String termIndex = keys.nextElement();
							double idf = 1.0;
							if(PreprocessData.indexer != null && PreprocessData.indexer.get(termIndex) != null)
								idf = PreprocessData.indexer.get(termIndex);
							int m = 0;
							for(Hashtable<String, Double> centroid : kCentroids)
							{
								if(centroid.containsKey(termIndex))
									sim[m] += centroid.get(termIndex)*d.index.get(termIndex)*PreprocessData.docIndexer[docIds[i]].idfMagnitude*idf;
								m++;
							}
						}
						double max = 0.0;
						int itr = 0;
						int clus = -1;
						while(itr < ck)
						{
							if(max < sim[itr])
							{
								max = sim[itr];
								clus = itr;
							}
							itr++;
						}
						currentSim += max;
						if(d.clusterID != clus)
							clusterConverged = false;
						d.clusterID = clus;
						nonZeroClusters[clus]++;
						PreprocessData.docIndexer[docIds[i]] = d;
					}
					kCentroids = reCompute(docIds);
					coverganceCount++;
				}
				int zeroClusterChecker = 1;
				for(int nonZero = 0; nonZero < ck; nonZero++)
				{
					zeroClusterChecker *= nonZeroClusters[nonZero];
				}
				//System.out.println(compute+" Seed took "+tempItr+ " to converge.");
				/*if(zeroClusterChecker == 0)
					System.out.println("Zero cluster observed");*/
				if(zeroClusterChecker != 0 && previousSim > currentSim)
				{
					previousSim = currentSim;
					finalKCentroids = kCentroids;
					for(int d = 0; d < size; d++)
						storeDocIds[d] = PreprocessData.docIndexer[docIds[d]].clusterID;
					//finalInd = probInd;
				}
			}
			System.out.println("Similarity: "+previousSim+ " : coverganceCount"+coverganceCount);
			clusters = new Clusters[ck];
			PrintWriter clusterWriter = new PrintWriter("clusters.txt", "UTF-8");
			for(int i = 0; i < size; i++)
			{
				int docId = docIds[i];
				//PreprocessData.docIndexer[docId].clusterID = storeDocIds[i];
				int ci = storeDocIds[i];
				if(clusters[ci] == null)
					clusters[ci] = new Clusters();
				clusters[ci].doc.add(docId);
				//PreprocessData.docIndexer[docId].clusterID = -1;
				//clusterWriter.println("Doc: "+docId+ " in cluster: "+ci);
			}
			//System.out.println(":: "+finalInd[0]+" :: "+finalInd[1]+" :: "+finalInd[2]);
			clusterWriter.close();
			computeTerms(finalKCentroids, clusters);
			freeDocIndex(docIds);
		}
		return clusters;
	}
	
	// getting initial seeds
	private static ArrayList<Hashtable<String, Double>> init(int[] docIds){
		ArrayList<Hashtable<String, Double>> kCentroids = new ArrayList<Hashtable<String, Double>>(ck);
		int i = 0;
		int[] checkInd = new int[ck];
		while(i < ck)
		{
			int ind = (int)((size-1)*Math.random());
			int k = i;
			while(k-- > 0){
				if(ind == checkInd[k]){
					ind = (int)((size-1)*Math.random());
					break;
				}
			}
			checkInd[i] = ind;
			//probInd = checkInd;
			Hashtable<String, Double> centroid = new Hashtable<String, Double>();
			int docId = docIds[ind];
			Hashtable<String, Short> temp = PreprocessData.docIndexer[docId].index;
			double tfMag = PreprocessData.docIndexer[docId].idfMagnitude;
			Enumeration<String> keys = temp.keys();
			while(keys.hasMoreElements())
			{
				double idf = 1.0;
				String termIndex = keys.nextElement();
				if(PreprocessData.indexer != null && PreprocessData.indexer.get(termIndex) != null)
					idf = PreprocessData.indexer.get(termIndex);
				double value = temp.get(termIndex);
				double newValue = value*idf*tfMag;
				centroid.put(termIndex, newValue);
			}
			kCentroids.add(centroid);
			i++;
		}
		return kCentroids;
	}
	
	// compute the centroids
	private static ArrayList<Hashtable<String, Double>> reCompute(int[] docIds){
		ArrayList<Hashtable<String, Double>> kCentroids = new ArrayList<Hashtable<String, Double>>(ck);
		for(int i = 0; i < ck; i++)
		{
			Hashtable<String, Double> centroid = new Hashtable<String, Double>();
			kCentroids.add(i, centroid);
		}
		
		for(int i = 0; i < size; i++)
		{
			int docId = docIds[i];
			DocIndex d = PreprocessData.docIndexer[docId];
			int clusterIndex = d.clusterID;
			Hashtable<String, Double> centroid = kCentroids.get(clusterIndex);
			
			Enumeration<String> keys = d.index.keys();
			while(keys.hasMoreElements())
			{
				String termIndex = keys.nextElement();
				if(centroid.get(termIndex) == null)
					centroid.put(termIndex, (double)d.index.get(termIndex));
				else
				{
					double val = centroid.get(termIndex);
					centroid.put(termIndex, (val + d.index.get(termIndex)));
				}
			}
		}
		double[] mag = new double[ck];
		int p = 0;
		for(Hashtable<String, Double> c: kCentroids)
		{
			Enumeration<String> cKeys = c.keys();
			while(cKeys.hasMoreElements()){
				String key = cKeys.nextElement();
				double val = c.get(key);
				mag[p] += Math.pow(val, 2.0);
			}
			mag[p] = 1/Math.sqrt(mag[p]);
			p++;
		}
		p = 0;
		for(Hashtable<String, Double> c: kCentroids)
		{
			Enumeration<String> cKeys = c.keys();
			while(cKeys.hasMoreElements()){
				String key = cKeys.nextElement();
				double val = c.get(key);
				val = val*mag[p];
				c.put(key, val);
			}
			p++;
		}
		return kCentroids;
	}
	
	// assigning null to free the index.
	private static void freeDocIndex(int[] docIds)
	{
		if(docIds.length != 0)
			for(int i = 0; i < size; i++)
			{
				PreprocessData.docIndexer[docIds[i]].index = null;
				if(PreprocessData.docIndexer[docIds[i]].clusterID != -1){
					PreprocessData.docIndexer[docIds[i]].clusterID = -1;
				}
				else
					System.out.println("Shit!! I'm called");
			}
	}
	
	// computing cluster summary. 
	private static void computeTerms(ArrayList<Hashtable<String, Double>> kCentroids, Clusters[] clusters)
	{
		// already normalized
		// add 2 and subtract the other to check for negative values...those are this clusters prominent features.
		for(int i = 0; i < ck; i++)
		{
			Hashtable<String, Double> c = kCentroids.get(i);
			Enumeration<String> cKeys = c.keys();
			KMeanSet Kset = new KMeanSet();
			while(cKeys.hasMoreElements()){
				String key = cKeys.nextElement();
				if(PreprocessData.indexer.containsKey(key) && PreprocessData.indexer.get(key) > 0.5 && PreprocessData.indexer.get(key) < 6 && Utility.allFilter(key))
				{
					double val = c.get(key);
					double temp = 0;
					for(int j = 0; j < ck; j++){
						if(j != i)
						{
							temp += (kCentroids.get(j).get(key) == null)?0:kCentroids.get(j).get(key);
						}
					}
					Kset.add(key, (val - temp));
				}
			}
			
			//System.out.println(":::Cluster "+i+":::");
			KMeanSet Kset3 = new KMeanSet();
			Enumeration<Double> termKeys = Kset.index.keys();
			while(termKeys.hasMoreElements()){
				double key = termKeys.nextElement();
				String value = Kset.index.get(key);
				double idf = PreprocessData.indexer.get(value);
				Kset3.add(value, idf*key);
			}
			if(clusters[i] != null)
				clusters[i].summary = Kset3.clusterTerms();
		}
	}
}

class KMeanSet{
	private int count = 10; // TODO: hard code value
	private TreeSet<Double> closeness = new TreeSet<Double>();
	Hashtable<Double, String> index = new Hashtable<Double,String>();
	void add(String value, double key){
		if(closeness.size() < count)
		{
			closeness.add(key);
			index.put(key, value);
		}
		else{
			double first = closeness.first();
			if(first < key)
			{
				closeness.pollFirst(); //TODO: can use remove, as the object is know.
				closeness.add(key);
				index.remove(first);
				index.put(key, value);
			}
		}
	}
	String[] clusterTerms(){
		String[] terms = null;
		if(index!= null && index.size() != 0 && closeness != null && closeness.size() != 0){
			terms = new String[5];
			int i = 0;
			while(i < 5){ // TODO: hard code value
				terms[i] = index.get(closeness.pollLast());
				//System.out.print(terms[i]+", ");
				i++;
			}
			//System.out.println("");
		}
		return terms;
	}
}

class Clusters{
	String[] summary;
	ArrayList<Integer> doc;
	Clusters(){
		doc = new ArrayList<Integer>();
	}
}
