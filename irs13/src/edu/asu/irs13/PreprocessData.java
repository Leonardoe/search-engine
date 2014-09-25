package edu.asu.irs13;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;

public class PreprocessData {
	
	public static Hashtable<String, Double> indexer;
	public static DocIndex[] docIndexer;
	private static final String dirPath = "docIndex";
	public PreprocessData()
	{
		try {			
			// the IndexReader object is the main handle that will give you 
			// all the documents, terms and inverted index
			IndexReader r = IndexReader.open(FSDirectory.open(new File("index")));
			//System.out.println("The number of documents in this index is: " + r.maxDoc());
			
			/***** Preprocessing data to compute IDF and document norms *****/
			
			// time recorded at the start of preprocessing.
			long time = System.currentTimeMillis();
			
			// HashTable created to store the IDF values for all the terms.
			indexer = new Hashtable<String, Double>();
			// You can find out all the terms that have been indexed using the terms() function
			TermEnum t = r.terms();
			
			docIndexer = new DocIndex[r.maxDoc()];
			
			File dir = new File(dirPath);
			boolean indexPresent = !dir.mkdir();
			if(!indexPresent)
				System.out.println("This is gonna take some time.");
			/*if(!dir.exists());
			{
				indexPresent = false;
				dir.mkdir();
			}*/
			while(t.next())
			{
				// terms are now processed to compute IDF, Document magnitude and max frequency in a document.
				Term tempTe = new Term("contents", t.term().text());
				// IDF is computed and stored.
				double idf_tmp = log((double)r.maxDoc()/(double)r.docFreq(tempTe));
				indexer.put(t.term().text(), idf_tmp);
				// termDocs gives all the document which has this term.
				TermDocs tempTd = r.termDocs(tempTe);
				while(tempTd.next())
				{
					DocIndex d;
					if(docIndexer[tempTd.doc()] == null)
						d = new DocIndex();
					else
						d = docIndexer[tempTd.doc()];
					
					d.idfMagnitude += pow(tempTd.freq()*idf_tmp,2.0);
					d.tfMagnitude += pow(tempTd.freq(),2.0);
					if(d.maxTF < tempTd.freq())
						d.maxTF = tempTd.freq();
					//d.index.put(intToString(count), (double)tempTd.freq());
					//if(tempTd.doc() > 14000 && tempTd.doc() < 15000 || tempTd.doc() == 22990)
					if(!indexPresent)
						d.index.put(t.term().text(), (short)tempTd.freq());
					docIndexer[tempTd.doc()] = d;
				}
			}
			int i = 0;
			do{				
				docIndexer[i].idfMagnitude = 1/sqrt(docIndexer[i].idfMagnitude);
				docIndexer[i].tfMagnitude = 1/sqrt(docIndexer[i].tfMagnitude);
				
				if(!indexPresent)
				{
					File file = new File(dirPath+"/d"+i+".txt");
					PrintWriter docWriter = new PrintWriter(file, "UTF-8");			
					
					Hashtable<String, Short> dIndex = docIndexer[i].index;
					Enumeration<String> termKeys = dIndex.keys();
					while(termKeys.hasMoreElements()){
						String term = termKeys.nextElement();
						docWriter.println(term+"->"+dIndex.get(term));
					}
					docWriter.close();
				}
				docIndexer[i].index = null;
				}while(++i < r.maxDoc());
			// Time difference recorded for data preprocessing.
			time = System.currentTimeMillis() - time;
			System.out.println("Documents norms and IDF computed in: " +time+ " milliseconds");
			r.close();
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getDocIndex(int docId){
		try {
			BufferedReader br = new BufferedReader(new FileReader(dirPath+"/d"+docId+".txt"));
			Hashtable<String, Short> dIndex = new Hashtable<String, Short>();
			String s = "";
			while ((s = br.readLine())!=null)
			{
				String[] split = s.split("->"); // index '0' will contain the key and index '1' the set of index.
				dIndex.put(split[0], Short.valueOf(split[1]));
			}
			br.close();
			docIndexer[docId].index = dIndex;
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*private String intToString(int i){
		String str = "";
		char[] arr = new char[6];
		int count = 0;
		while(i != 0)
		{
			int rem = i%10;
			i = i/10;
			if(rem > 4)
			{
				if(rem > 7)
					if(rem == 8)
						arr[count] = '8';
					else
						arr[count] = '9';
				else
					if(rem == 5)
						arr[count] = '5';
					else if(rem == 6)
						arr[count] = '6';
					else
						arr[count] = '7';
			}
			else
			{
				if(rem > 2)
					if(rem == 3)
						arr[count] = '3';
					else
						arr[count] = '4';
				else
					if(rem == 0)
						arr[count] = '0';
					else if(rem == 1)
						arr[count] = '1';
					else
						arr[count] = '2';
			}
			count++;
		}
		char[] trueArr = new char[count];
		int m = 0;
		while(count > 0)
		{
			count--;
			trueArr[count] = arr[m];
			m++;
		}
		for(char c: trueArr)
			str = str + c;
		return str;
	}*/
	
	class DocIndex{
		
		//int docId;
		double idfMagnitude;
		double tfMagnitude;
		double maxTF;
		int clusterID;
		Hashtable<String, Short> index;
		
		public DocIndex(){
			idfMagnitude = 0.0;
			tfMagnitude = 0.0;
			maxTF = 0.0;
			clusterID = -1;
			index = new Hashtable<String, Short>();
		}
	}
}
