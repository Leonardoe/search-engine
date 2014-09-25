package edu.asu.irs13;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

/*
 * SpellCorrector class creates k-gram index, uses Jaccard similarity and IDF values to give the closest word to query word.
 * main() and createSpellIndex() is already run to create the index files.
 * During run time getSpellIndex() is called to fetch this index from files which is much faster. 
 */
public class SpellCorrector {
	
	private static final String spellIndexFile = "spellIndex.txt"; // spellIndex is maintained in this file.
	private static final String termIndexFile = "termIndex.txt"; // terms corresponding to the each index is kept in this file.
	private static int kgram = 3;
	
	private static Hashtable<String, TermSet> spellIndex; // table with key as k-gram String and value as set of index of terms containing the key. 
	private static ArrayList<String> indexMap; // the term corresponding to each index.
	
	/*
	 * Method main() will populate the spellIndex and termIndex and write it in respective files.
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException {
		
		System.out.println("Starting SpellCorrector Indexing");
		IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
		TermEnum t = r.terms();
		while(t.next()) // for all terms.
		{
			SpellCorrector.createSpellIndex(t.term().text()); // creating index
		}
		// printing the spellIndex in file.
		PrintWriter spellWriter = new PrintWriter("spellIndex.txt", "UTF-8");
		Enumeration<String> enuKey = spellIndex.keys();
		while(enuKey.hasMoreElements())
		{
			String key = enuKey.nextElement();
			spellWriter.print(key+ "->");
			TermSet ts = spellIndex.get(key);
			for(int i:ts)
				spellWriter.print(i+ ",");
			spellWriter.println("");
		}
		spellWriter.close();
		// printing indexMap in file.
		PrintWriter termWriter = new PrintWriter("termIndex.txt", "UTF-8");
		int k = 0;
		while(k < indexMap.size())
		{
			termWriter.println(indexMap.get(k));
			k++;
		}
		spellIndex = null;
		indexMap = null;
		termWriter.close();
		r.close();
		System.out.println("SpellCorrector Completed!!");
	}
	/*
	 * Method createSpellIndex() breaks the term into k-grams and stores in the table.
	 */
	static private void createSpellIndex(String term){
		if(spellIndex == null)
			spellIndex = new Hashtable<String, TermSet>(400000); // create spellIndex.
		if(indexMap == null)
			indexMap = new ArrayList<String>(121100); // create indexMap.
		if(term != null && term.length() != 0 && Utility.isValidString(term)) // check for validity of term.
		{
			String term$ = "#"+term.concat("$"); // add '#' in the begin and '$' at the end.
			indexMap.add(term); // add the term to indexMap.
			int i = 0;
			TermSet ts;
			while(i < (term$.length() - kgram + 1)) // for all k-grams possible.
			{
				String kgramString = term$.substring(i,i+kgram); // get the k-gram string
				if(spellIndex.containsKey(kgramString)) // check if it is already present.
					ts = spellIndex.get(kgramString); // get the set of index.
				else
					ts = new TermSet(); // create a new set of index.
				ts.add(indexMap.indexOf(term)); // add the index of this term.
				spellIndex.put(kgramString, ts); // put this entry in table.
				i++;
			}
		}
	}
	
	/*
	 * Method getSpellIndex() creates spellIndex and indexMap from the files.
	 */
	public static void getSpellIndex(){
		spellIndex = new Hashtable<String, TermSet>(400000);
		try {
			long time = System.currentTimeMillis();
			BufferedReader br = new BufferedReader(new FileReader(spellIndexFile)); // read spellIndex file.
			String s = "";
			while ((s = br.readLine())!=null)
			{
				String[] split = s.split("->"); // index '0' will contain the key and index '1' the set of index.
				String[] termIndex = split[1].split(","); // split to get the indexes.
				TermSet ts = new TermSet();
				for(String term:termIndex)
				{
					ts.add(Integer.parseInt(term)); // populate the set.
				}
				spellIndex.put(split[0], ts); // put this value in table.
			}
			br.close();
			indexMap = new ArrayList<String>(121100);
			br = new BufferedReader(new FileReader(termIndexFile)); // read termIndex file.
			s = "";
			while ((s = br.readLine())!=null)
			{
				indexMap.add(s); // add each term in the indexMap.
			}
			br.close();
			time = System.currentTimeMillis() - time;
			System.out.println("SpellCorrector Indexed in: "+time+ " milliseconds");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * method checkSpell() creates table of all the terms which have query term k-grams.
	 * return the mostSimiliar term. Uses Jaccard Similarity and IDF.
	 */
	static public String checkSpell(String query){
		if(spellIndex == null || indexMap == null)
			return "";
		String closestTerm = "";
		if(query != null && query.length() != 0)
		{
			String[] terms = query.toLowerCase().split("\\s+");
			for(String term : terms)
			{
				String term$ = "#" + term.concat("$"); // add '#' in the begin and '$' at the end.
				int i = 0;
				TermSet ts;
				Hashtable<Integer, Integer> checkTable = new Hashtable<Integer,Integer>(); // table created.
				while(i < (term$.length() - kgram + 1)) // for all k-gram string of the term.
				{
					String kgramString = term$.substring(i,i+kgram); // get k-gram string.
					if(spellIndex.containsKey(kgramString)) // check if it exists.
					{
						ts = spellIndex.get(kgramString); // get the set corresponding to it.
						for(int k:ts) // for all indexes.
						{
							if(checkTable.containsKey(k)) // check if index is already present.
							{
								int value = checkTable.get(k);
								value++; // increment the value/occurrence of this term. 
								checkTable.put(k, value); // populate the table.
							}
							else
							{
								checkTable.put(k, 1); // populate the table with value 1. first occurrence.
							}
						}
					}
					i++;
				}
				closestTerm += mostSimilarTerm(term.length(), checkTable) + " "; // gives the closest term
			}
			closestTerm = closestTerm.substring(0, closestTerm.length()-1);
			System.out.println("::"+closestTerm+"::");
		}
		return closestTerm;
	}

	/*
	 * Method mostSimilarTerm() computes similarity for each term in table with the query term.
	 * Returns with the highest similarity value. 
	 */
	private static String mostSimilarTerm(int size, Hashtable<Integer, Integer> table){
		String correctedStr = "";
		Enumeration<Integer> enuKey = table.keys(); // get keys.
		double max_sim = 0.0;
		while(enuKey.hasMoreElements()) // for all keys in table.
		{
			int index = enuKey.nextElement();
			String term = indexMap.get(index); // get the string corresponding to the index.
			// the corrected word should not be very different from the query word in length. 
			if(PreprocessData.indexer.get(term) != null && (term.length() == size || term.length() == (size-1) || term.length() == (size+1)))
			{
				double union = table.get(index); // the union value for Jaccard Similarity.
				double intersection = size + term.length() - union; // the intersection value for Jaccard Similarity.
				double temp_sim = union/intersection; // Jaccard Similarity.
				temp_sim = temp_sim/(1 + PreprocessData.indexer.get(term)); // Considering the IDF with it.
				if(temp_sim > max_sim)
				{
					max_sim = temp_sim; // maximum similarity stored.
					correctedStr = term; // term stored.
				}
			}
		}
		return correctedStr; // corrected string returned. Yippiee!!
	}
	
	static public String wordSuggestion(String query){
		if(spellIndex == null || indexMap == null)
			return "";
		String closestTerm = "";
		if(query != null && query.length() != 0)
		{
			if(query.charAt(query.length() -1) != ' ')
			{
				String[] terms = query.split("\\s+");
				String term = terms[terms.length - 1];
				String term$ = "#" + term; // add '#' in the begin and '$' at the end.
				int i = 0;
				TermSet ts;
				Hashtable<Integer, Integer> checkTable = new Hashtable<Integer,Integer>(); // table created.
				while(i < (term$.length() - kgram)) // for all k-gram string of the term.
				{
					String kgramString = term$.substring(i,i+kgram); // get k-gram string.
					if(spellIndex.containsKey(kgramString)) // check if it exists.
					{
						ts = spellIndex.get(kgramString); // get the set corresponding to it.
						for(int k:ts) // for all indexes.
						{
							if(checkTable.containsKey(k)) // check if index is already present.
							{
								int value = checkTable.get(k);
								value++; // increment the value/occurrence of this term. 
								checkTable.put(k, value); // populate the table.
							}
							else
							{
								checkTable.put(k, 1); // populate the table with value 1. first occurrence.
							}
						}
					}
					i++;
				}
				String suggestedTerm = suggestTerm(term, checkTable); // gives the closest term
				int k = 0;
				while(k < (terms.length - 1))
				{
					closestTerm += terms[k] + " ";
					k++;
				}
				closestTerm += suggestedTerm;
			}
			else
				closestTerm = query;
		}
		return closestTerm;
	}
	private static String suggestTerm(String queryTerm, Hashtable<Integer, Integer> table){
		int size = queryTerm.length();
		String correctedStr = "";
		Enumeration<Integer> enuKey = table.keys(); // get keys.
		double max_sim = 0.0;
		while(enuKey.hasMoreElements()) // for all keys in table.
		{
			int index = enuKey.nextElement();
			String term = indexMap.get(index); // get the string corresponding to the index.
			if(term.length() > size && term.substring(0, size).equals(queryTerm))
				// the corrected word should not be very different from the query word in length. 
				if(PreprocessData.indexer.get(term) != null)
				{
					double union = table.get(index); // the union value for Jaccard Similarity.
					double intersection = size + term.length() - union; // the intersection value for Jaccard Similarity.
					double temp_sim = union/intersection; // Jaccard Similarity.
					temp_sim = temp_sim/(1 + PreprocessData.indexer.get(term)); // Considering the IDF with it.
					if(temp_sim > max_sim)
					{
						max_sim = temp_sim; // maximum similarity stored.
						correctedStr = term; // term stored.
					}
				}
		}
		return correctedStr; // corrected string return. Yippiee!!
	}
	
}

// TermSet class extends HashSet
class TermSet extends HashSet<Integer>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	// no method.
}
