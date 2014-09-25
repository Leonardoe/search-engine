package edu.asu.irs13;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;

public class Utility {
	
	private static String[] htmlTags = {"html", "li", "endlibraryitem", "nbsp", "jpg", "alt", "ul", "pn", "colspan", "rsquo", "div", "rel", "js", "doctype", "gif", "bgcolor", "hr", "cols", "css", "cellspacing", "org", "stylesheet", "instanceparam", "instanceend", "begineditable", "ref", "href", "bordercolor", "centercol", "hidden", "endeditable", "tbody", "trgb", "instancebegineditable", "instanceendeditable", "helvetica", "padding","ol","th", "htm", "span", "htmltxt", "dr", "em", "dl", "rdquo", "ldquo", "https"};
	private static String[] pronounTags = {"me", "my", "her", "he","she", "we", "you", "your", "our", "his"};
	private static String[] otherTags = {"have", "each", "much", "other", "have", "many", "shall", "should", "would", "has", "its", "arial", "verdana", "sans", "serif", "eeeeee", "target", "onmouseover", "onmouseout", "menulink"};
	// method to display results populated in double array.
	public static void displayResults(double[][] arr, IndexReader r) throws CorruptIndexException, IOException
	{
		int k = 0;
		PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
		while(k < arr.length) {
			// Fetch the document ID from sorted array.
			Document d2 = r.document((int)arr[k][1]);
			// the 'path' field of the Document object holds the URL
			String url2 = d2.getFieldable("path").stringValue();
			int rank = k + 1;
			// print the result.
			writer.println("Rank: " + rank +" Document ID: "+(int)arr[k][1]+ " :  URL: " +url2.replace("%%", "/"));
			k++;
		}
		writer.close();
	}
	// method to check if the convergence is achieved.
	static boolean checkEigen(double[] a, double[]b)
	{
		int i = 0;
		while(i < a.length)
		{
			if(abs(a[i] - b[i]) > 0.001)
				return false;
			i++;
		}
		return true;
	}
	// Multiples a Matrix with a vector and returns the normalized output vector.
	static double[] matrixMulti(double[][] a, double[]b) throws IOException
	{
		int aRow = a.length;
		int aCol = a[0].length;
		int bRow = b.length;
		double mag = 0;
		if(aCol != bRow)
			throw new IOException("Column and Rows don't match");
		double[] mat = new double[aRow];
		for(int i = 0; i < aRow; i++)
		{
			for(int k = 0; k < aCol; k++)
				mat[i] += a[i][k]*b[k]; // vector computed.
			mag += pow(mat[i],2.0); // magnitude computed.
		}
		mag = sqrt(mag);
		for(int i = 0; i < mat.length; i++)
			mat[i] = mat[i]/mag; // vector normalized.
		return mat;
	}
	
	// method to compute matrix multiplication
	static double[][] matrixMulti(int[][] a, int[][]b) throws IOException
	{
		int aRow = a.length;
		int aCol = a[0].length;
		int bRow = b.length;
		int bCol = b[0].length;
		if(aCol != bRow)
			throw new IOException("Column and Rows don't match");
		double[][] mat = new double[aRow][bCol];
		for(int i = 0; i < aRow; i++)
			for(int j = 0; j < bCol; j++)
				for(int k = 0; k < aCol; k++)
					mat[i][j] += (double)a[i][k]*(double)b[k][j];
		return mat;
	}
	static boolean isValidString(String str){
		return str.matches("(([a-z])+([a-z])+([a-z])+)");
	}
	
	static boolean isNotHtml(String str){
		for(String s:htmlTags)
		{
			if(s.equals(str))
				return false;
		}
		return true;
	}
	static boolean isNotPronoun(String str){
		for(String s:pronounTags)
		{
			if(s.equals(str))
				return false;
		}
		return true;
	}
	static boolean isNotOtherTags(String str){
		for(String s:otherTags)
		{
			if(s.equals(str))
				return false;
		}
		return true;
	}
	static boolean allFilter(String str){
		if(isValidString(str) && isNotOtherTags(str) && isNotPronoun(str) && isNotHtml(str))
			return true;
		return false;
	}
}
