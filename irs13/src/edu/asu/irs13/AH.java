package edu.asu.irs13;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static edu.asu.irs13.Utility.checkEigen;
import static edu.asu.irs13.Utility.matrixMulti;

/*
 * AH class is shortform of Authority and Hub. It computes A&H for a query.
 */
public class AH {
	
	static ArrayList<Integer> baseSet; // baseSet is stored for a given query.
	private static double[][] aaT; // A*A' matrix for this baseSet.
	private static double[][] aTa; // A'*A matrix for this baseSet.
	static double[] aut; // The authority vector.
	static double[] hub; // The hub vector.
	
	/*
	 * Method createBaseSet will create a baseSet for a query result and given value of k.
	 */
	public static void createBaseSet(double[][] results, int k, LinkAnalysis l)
	{
		if(results != null && results.length != 0)
		{
			long time = System.currentTimeMillis();
			baseSet = new ArrayList<Integer>(); // create baseSet.
			for(int i = 0; i < k && i < results.length; i++) // for all results in the range.
			{
				int docID = (int)results[i][1];
				if(!baseSet.contains(docID)) // add the document if it is not present.
					baseSet.add(docID);
				int[] links = l.getLinks(docID); // get all the documents the rootset point to and add them.
				for(int pb:links)
				{
					if(!baseSet.contains(pb)) // add only if they are not already present.
						baseSet.add(pb);
				}
				int[] cits = l.getCitations(docID); // get all the documents those point to rootset and add them.
				for(int ct:cits)
				{
					if(!baseSet.contains(ct)) // add only if they are not already present.
						baseSet.add(ct);
				}
			}
			time = (System.currentTimeMillis() - time); // total time taken to compute baseSet.
			System.out.println("[AH] baseSet (" +baseSet.size()+ ") computed in: " +time+ " milliseconds");
		}
	}
	/*
	 * Method createMatricesForPowerInteration creates A*A' and A'*A matrixes.
	 */
	private static void createMatricesForPowerInteration(LinkAnalysis link){
		if(baseSet != null)
		{
			long time = System.currentTimeMillis();
			int[][] adj = new int[baseSet.size()][baseSet.size()]; // A matrix
			int[][] adjT = new int[baseSet.size()][baseSet.size()]; // A' matrix
			for(int i = 0; i < baseSet.size(); i++) // for each row of the A matrix.
			{
				int[] tempLink = link.getLinks(baseSet.get(i)); // get all the links for the row.
				for(int tempID : tempLink) // Iterate through all the links.
					if(baseSet.contains(tempID)) // check if the link lies in the baseSet.
					{
						int loc = baseSet.indexOf(tempID); // get the location.
						adj[i][loc] = 1; // set 1 corresponding to that.
						adjT[loc][i] = 1;
					}
			}
			time = (System.currentTimeMillis() - time); // total time taken to compute baseSet.
			System.out.println("[AH] Adjacency matrix computed in: " +time+ " milliseconds");
			try {
				long time2 = System.currentTimeMillis();
				aaT = matrixMulti(adj,adjT); // multiple A and A'
				aTa = matrixMulti(adjT,adj); // multiple A' and A
				time2 = (System.currentTimeMillis() - time2); // total time taken to compute baseSet.
				System.out.println("[AH] A'A and AA' computed in: " +time2+ " milliseconds");
			} catch (IOException e) {
				System.out.println("ERROR: in Matrix multiplication");
				e.printStackTrace();
			}
		}
	}
	/*
	 * Method computeAuthAndHub will power iterate aut and hub vector to converge them.
	 */
	public static void computeAuthAndHub(LinkAnalysis link) throws IOException
	{
		if(baseSet != null)
		{
			createMatricesForPowerInteration(link); // creates AA' and A'A.
			long time = System.currentTimeMillis();
			aut = new double[baseSet.size()]; // aut vector.
			hub = new double[baseSet.size()]; // hub vector.
			for(int i = 0; i < aut.length; i++) // initialize with 1/size
			{
				aut[i] = 1.0/(double)aut.length;
				hub[i] = 1.0/(double)hub.length;
			}
			double[] tempAut = new double[baseSet.size()]; //tempAut to store previous aut vector.
			do
			{
				tempAut = aut;
				aut = matrixMulti(aTa, aut); // multiple A'A and aut vector.
			}
			while(!checkEigen(aut,tempAut)); // check for convergence.
			double[] tempHub = new double[baseSet.size()]; //tempHub to store previous hub vector.
			do
			{
				tempHub = hub;
				hub = matrixMulti(aaT, hub); // multiple AA' and hub vector.
			}while(!checkEigen(hub,tempHub)); // check for convergence.
			time = (System.currentTimeMillis() - time); // total time taken to compute baseSet.
			System.out.println("[AH] aut and hub computed in: " +time+ " milliseconds");
		}
	}
	public static void clean()
	{
		baseSet = null;
		aut = null;
		hub = null;
		aaT = null;
		aTa = null;
	}
}
/*
 * AHlist class maintains the list of authority and hub score for every document in the baseSet.
 */
class AHlist extends ArrayList<AHinfo>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/*
	 * 3-argument constructor. Takes in the baseSet, authority vector and hub vector. 
	 */
	public AHlist(ArrayList<Integer> baseSet, double[] aut, double[] hub){
		if(baseSet != null)
		{
			for(int i = 0; i < baseSet.size(); i++)
				this.add(baseSet.get(i), aut[i], hub[i]);
		}
	}
	
	/*
	 * enum Type to select between authority and hub.
	 */
	public enum Type{
		authority,
		hub
	}
	/*
	 * method add to add element in the list.
	 */
	public void add(int id, double auth, double hub)
	{
		AHinfo ah = new AHinfo(id, auth, hub);
		this.add(ah);
	}
	
	/*
	 * method display will write top 'N' authorities and hubs in respective files.
	 * with UI in place, this method is not in use. 
	 */
	public void display(int N)
	{
		try {
			PrintWriter aut_writer = new PrintWriter("authorites.txt", "UTF-8");
			this.sort(Type.authority);
			for(int i = 0; i < this.size() && i < N; i++)
			{
				aut_writer.println("Auth: "+ this.get(i).DocID + " " + this.get(i).auth);
			}
			aut_writer.close();
			PrintWriter hub_writer = new PrintWriter("hubs.txt", "UTF-8");
			this.sort(Type.hub);
			for(int i = 0; i < this.size() && i < N; i++)
			{
				hub_writer.println("Hub: "+ this.get(i).DocID + " " + this.get(i).hub);
			}
			hub_writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("Unable to open file");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.out.println("UTF-8 not supported");
			e.printStackTrace();
		}
	}
	
	/*
	 * method sort takes in the Type according to which you want to sort
	 */
	public void sort(final Type type){
		/*if(type != Type.authority || type != Type.hub)
			throw new IOException("Invalid Type");*/
		Collections.sort(this, new Comparator<AHinfo>(){
			public int compare(AHinfo ah1, AHinfo ah2)
			{
				if(type == Type.authority)
					return Double.compare(ah2.auth, ah1.auth);
				else
					return Double.compare(ah2.hub, ah1.hub);
			}
		});
	}
}

/*
 * AHinfo class stores the document ID, authority score and hub score.
 */
class AHinfo{
	int DocID;
	double auth;
	double hub;
	
	public AHinfo(){ DocID = 0; auth = 0.0; hub = 0.0; } // 0-argument constructor.
	public AHinfo(int id, double a, double h){ DocID = id; auth = a; hub = h; } // 3-argument constructor.
}
