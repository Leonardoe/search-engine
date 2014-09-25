package edu.asu.irs13;

//import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import java.io.*;
import java.util.*;

public class SearchFiles {
	/**
	 * @param args
	 * @throws Exception
	 */
	/**
	 * @param args
	 * @throws Exception
	 */
	
	static boolean AH_flag = false;
	static boolean pageRank_flag = true;
	static boolean enable_idf = true;
	static IndexReader r;
	static LinkAnalysis link;
	static double[] pageRank;
	static AHlist ahl;
	static double[][] results;
	static double w;
	static int k;
	static String[] sc;
	
	public static void main(String[] args) throws Exception
	{
		// To get the results in a file.
		
		r = IndexReader.open(FSDirectory.open(new File("index")));
		
		new PreprocessData(); // computing TF and TF/IDF.
		LinkAnalysis.numDocs = r.maxDoc();
		link = new LinkAnalysis(); // linkIndex is created.
		pageRank = PageRank.computePageRank(r, link); // page rank computed.
		SpellCorrector.getSpellIndex(); // spellIndex is populated.
		//pageRank = PrioritizedPageRank.computePageRank(r, link);
	}
	
	static public void searchQuery(String query) throws IOException
	{
		if(query.length() == 0){
			System.out.println("Empty query");
			return;
		}
		else{
			query = query.toLowerCase(); // changed to lower case.
			System.out.println("Query: " +query);
			// time recorded when user inputed the query.
			long time = System.currentTimeMillis();
			long time_total = time;
			// IndexTable created to store the relevant documents.
			IndexTable my_table = new IndexTable();
			int n = 25055;
			// The query string is split into separate terms.
			String[] terms = query.split("\\s+");
			for(String word : terms)
			{
				Term term = new Term("contents", word);
				// All the documents which contain this word
				TermDocs tdocs = r.termDocs(term);
				//suggestedTerm = suggestedTerm + SpellCorrector.checkSpell(word) + " "; // spellCorrector
				// get the IDF value for this term.
				if(enable_idf) // TF/IDF similarity computed
				{
					double idf = 1.0;
					if(PreprocessData.indexer != null && PreprocessData.indexer.get(word) != null)
						idf = PreprocessData.indexer.get(word);
					while(tdocs.next())
					{
						// Compute the IDF/TF similarity and store in IndexTable
						if(tdocs.doc() < n)
							if(!my_table.containsKey(tdocs.doc()))
								my_table.put(tdocs.doc(), ((double)tdocs.freq()*(double)idf*PreprocessData.docIndexer[tdocs.doc()].idfMagnitude));
							else
							{
								double value = my_table.get(tdocs.doc());
								my_table.put(tdocs.doc(), value + ((double)tdocs.freq()*(double)idf*PreprocessData.docIndexer[tdocs.doc()].idfMagnitude));
							}
					}
				}
				else // TF similarity computed
				{
					while(tdocs.next())
					{
						// Compute the IDF/TF similarity and store in IndexTable
						if(tdocs.doc() < n)
							if(!my_table.containsKey(tdocs.doc()))
								my_table.put(tdocs.doc(), ((double)tdocs.freq()*PreprocessData.docIndexer[tdocs.doc()].tfMagnitude));
							else
							{
								double value = my_table.get(tdocs.doc());
								my_table.put(tdocs.doc(), value + ((double)tdocs.freq()*PreprocessData.docIndexer[tdocs.doc()].tfMagnitude));
							}
					}
				}
			}
			// Time recorded to obtain results.
			time = System.currentTimeMillis() - time;
			
			
			long scalarTime = System.currentTimeMillis();
			sc = ScalarClusters.getScalarClusters(my_table, query);
			scalarTime = System.currentTimeMillis() - scalarTime;
			System.out.println("Scalar Clustering in: "+scalarTime+ " milliseconds");
			
			//System.out.println("Time taken to get the results: " +time+ " milliseconds");
			if(pageRank_flag) // similarity + page rank
			{
				Enumeration<Integer> enuKey = my_table.keys();
				while(enuKey.hasMoreElements())
				{
					int docID = enuKey.nextElement();
					double value = w*pageRank[docID]+ (1-w)*my_table.get(docID);
					my_table.put(docID, value);
				}
			}
			
			// Time recorded to check sorting time.
			time = System.currentTimeMillis();
			// IndexTable is sorted according to the similarity value and stored in an 2d Array
			results = my_table.toSortedArray();
			
			
			// Time recorded to sort results.
			time = System.currentTimeMillis() - time;
			//System.out.println("Time taken to sort the results: " +time+ " milliseconds");
			
			if(AH_flag)
			{
				long ahTime = System.currentTimeMillis();
				AH.createBaseSet(results, k, link); // baseSet is created
				AH.computeAuthAndHub(link); // Auth and hub vector computed

				ahl = new AHlist(AH.baseSet, AH.aut, AH.hub); // list populated.
				//ahl.display(20);
				
				ahTime = System.currentTimeMillis() - ahTime;
				System.out.println("[AH] Authorities and Hubs computed in: " +ahTime+ " milliseconds");
				AH.clean();
			}
			else
			{				
				System.out.println("Total results for \"" +query+ "\" : " +my_table.size());
				// Time recorded for the whole query.
				time_total = System.currentTimeMillis() - time_total;
				System.out.println("Time taken to handle the query: " +time_total+ " milliseconds");
			}
		}
	}
}

