package edu.asu.irs13;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DocParser {
	
	private static String filePath = "file:///" + System.getProperty("user.dir").replace('\\', '/') + "/result3/";
	//private static int maxSize = 150;
	private static int minSize = 90;
	
	public static String[] docParser(int docId, String querys){
		String[] terms = querys.split("\\s+");
		String query = terms[0];
		String[] returnStr = new String[4];
		try {
		org.apache.lucene.document.Document d2 = SearchFiles.r.document(docId);
		// the 'path' field of the Document object holds the URL
		String url = d2.getFieldable("path").stringValue();
		String url2 = filePath + url.replace("%%", "%25%25");
		URL my_url = new URL(url2);
		
		//InputStream input2 = my_url.openStream();
		
		File in = new File(my_url.toURI());
		
		Document doc = Jsoup.parse(in, "UTF-8");
		
		Elements meta = doc.select("meta");
		String snippet = "";
		boolean snippetFound = false;
		for(Element m:meta)
		{
			if(m.attr("name").equals("description"))
			{
				String description = m.attr("content");
				if(description.toLowerCase().contains(query))
				{
					snippet = getSnippet(description, query);
					//System.out.println(docId+ " Description: ");
					snippetFound = true;
				}
			}
		}
		if(!snippetFound)
		{
			Elements para = doc.select("p");
			for(Element p:para)
			{
				Attributes atr = p.attributes();
				if(atr.size() == 0 && p.text().toLowerCase().contains(query) && p.text().length() > minSize/2)
				{
					//System.out.println(p.text());
					//System.out.println(docId+" Para 1");
					snippet = getSnippet(p.text(), query);
					snippetFound = true;
					break;
				}
			}
			if(!snippetFound)
			{
				for(Element p:para)
				{
					if(p.text().toLowerCase().contains(query))
					{
						//System.out.println(p.text());
						//System.out.println(docId+ " PARA 2");
						snippet = getSnippet(p.text(), query);
						snippetFound = true;
						break;
					}
				}
			}
		}
		if(!snippetFound)
		{
			Elements htmlList = doc.select("li");
			for(Element li: htmlList)
			{
				if(li.text().toLowerCase().contains(query))
				{
					//System.out.println(p.text());
					//System.out.println(docId+ " li");
					snippet = getSnippet(li.text(), query);
					snippetFound = true;
					break;
				}
			}
		}
		if(!snippetFound){
			String body = doc.body().text();
			if(body.toLowerCase().contains(query)){
				//System.out.println(docId+" BODY");
				body = body.replaceAll("  ", "...");
				snippet = getSnippet(body, query);
				snippetFound = true;
			}
			else{
				// for multiple word query.
			}
		}
		if(!snippetFound)
		{
			String title = doc.title();
			if(title.length() < 80)
				snippet = doc.title();
			else
				snippet = title.substring(0, 80);
			//System.out.println(docId+ " Snippet not found :(");
		}
		snippet += "...";
		
		returnStr[0] = doc.title(); // title
		returnStr[1] = url.replace("%%", "/"); // path
		returnStr[2] = url2; // fullpath
		returnStr[3] = snippet; // snippet
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnStr;
	}
	
	private static String getSnippet(String str, String query)
	{
		String ret = str;
		if(str.length() > 80)
		{
			int ind = str.toLowerCase().indexOf(query);
			if(ind > 40){
				int spaceIndex = str.substring(ind-40).indexOf(" ");
				int beginIndex = ind - 40 + spaceIndex + 1;
				if(str.length() - beginIndex > minSize){
					spaceIndex = str.substring(beginIndex+minSize).indexOf(" ");
					ret = str.substring(beginIndex, beginIndex+minSize+spaceIndex);
				}
				else
					ret = str.substring(beginIndex);
			}
			else
				ret = str.substring(0, str.length()<minSize?str.length():minSize);
		}
		return ret;
	}
}
