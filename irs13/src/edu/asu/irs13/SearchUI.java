package edu.asu.irs13;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;
import javax.swing.JEditorPane;

import java.awt.EventQueue;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SearchUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static JTextField query;
	
	static JLabel timeLabel;
	static JLabel resultLabel;
	static JLabel pageLabel;
	static JEditorPane resultArea;
	static int resultCount = 0;
	static int total = 6;
	static int pageCount;
	static JButton nextButton;
	static JButton previousButton;
	private static JTextField pageRankW;
	//private static String filePath = "file:///C:/Users/Ishh/Downloads/Courses/IR/Project/irs13/result3/"; 
	private static String filePath = "file:///" + System.getProperty("user.dir").replace('\\', '/') + "/result3/"; 
	private static JTextField kText;
	
	static JLabel correctedLabel;
	static JLabel spellLabel;
	
	static JLabel queryLabel;
	static JLabel qe1Label;
	static JLabel qe2Label;
	static JLabel qe3Label;
	static JLabel qe4Label;
	static JLabel qe5Label;
	
	static JLabel cluster;
	private JTextField kmk;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchUI frame = new SearchUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws Exception 
	 */
	public SearchUI() throws Exception {
		//Directory dir = FSDirectory.open(new File("result3"));
		setTitle("ASU Search");
		setResizable(false);
		SearchFiles.main(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 677, 687);
		contentPane = new JPanel();
		contentPane.setBackground(Color.LIGHT_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBounds(10, 11, 651, 23);
		contentPane.add(horizontalBox);
		
		JLabel AsuSearch = new JLabel("ASU Search");
		AsuSearch.setForeground(new Color(128, 0, 0));
		horizontalBox.add(AsuSearch);
		AsuSearch.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		query = new JTextField();
		query.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String check = SpellCorrector.wordSuggestion(query.getText());
				System.out.println("suggestion for "+ query.getText()+ ": "+check);
			}
		});
		horizontalBox.add(query);
		query.setColumns(10);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue);
		
		JButton GoButton = new JButton("Go Devils!!");
		GoButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		GoButton.setForeground(new Color(128, 0, 0));
		
		horizontalBox.add(GoButton);
		GoButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		horizontalBox_2.setBounds(10, 34, 651, 23);
		contentPane.add(horizontalBox_2);
		
		JRadioButtonMenuItem radioTF = new JRadioButtonMenuItem("TF");
		horizontalBox_2.add(radioTF);
		radioTF.setSelected(true);
		radioTF.setEnabled(false);
		radioTF.setForeground(new Color(128, 0, 0));
		radioTF.setPreferredSize(new Dimension(30, 22));
		
		final JRadioButtonMenuItem radioIDF = new JRadioButtonMenuItem("IDF");
		horizontalBox_2.add(radioIDF);
		radioIDF.setForeground(new Color(128, 0, 0));
		
		radioIDF.setSelected(true);
		radioIDF.setPreferredSize(new Dimension(50, 22));
		
		final JCheckBox cbAH = new JCheckBox("Authorities and Hubs");
		horizontalBox_2.add(cbAH);
		cbAH.setForeground(new Color(0, 0, 0));
		
		final JCheckBox cbPageRank = new JCheckBox("PageRank");
		horizontalBox_2.add(cbPageRank);
		cbPageRank.setForeground(new Color(128, 0, 0));
		cbPageRank.setSelected(true);
		cbPageRank.setPreferredSize(new Dimension(125, 23));
		
		cbPageRank.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(SearchFiles.pageRank_flag)
				{
					cbPageRank.setForeground(new Color(0, 0, 0));
					SearchFiles.pageRank_flag = false;
					//SearchFiles.AH_flag = true;
					//chckbxAuthoritiesAndHubs_1.setEnabled(true);
				}
				else
				{
					cbPageRank.setForeground(new Color(128, 0, 0));
					SearchFiles.pageRank_flag = true;
					//SearchFiles.AH_flag = false;
					//chckbxAuthoritiesAndHubs_1.setEnabled(false);
				}
			}
		});
		
		cbAH.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(SearchFiles.AH_flag)
				{
					cbAH.setForeground(new Color(0, 0, 0));
					SearchFiles.AH_flag = false;
					//SearchFiles.pageRank_flag = true;
					//chckbxPagerank_1.setEnabled(true);
				}
				else
				{
					cbAH.setForeground(new Color(128, 0, 0));
					SearchFiles.AH_flag = true;
					//SearchFiles.pageRank_flag = false;
					//chckbxPagerank_1.setEnabled(false);
				}
			}
		});
		
		radioIDF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(SearchFiles.enable_idf)
				{
					SearchFiles.enable_idf = false;
					radioIDF.setForeground(new Color(0, 0, 0));
				}
				else
				{
					SearchFiles.enable_idf = true;
					radioIDF.setForeground(new Color(128, 0, 0));
				}
			}
		});
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setBounds(10, 56, 400, 20);
		contentPane.add(horizontalBox_1);
		
		spellLabel = new JLabel("Did you mean? ");
		spellLabel.setVisible(false);
		spellLabel.setForeground(new Color(128, 0, 0));
		spellLabel.setFont(new Font("Tahoma", Font.ITALIC, 11));
		horizontalBox_1.add(spellLabel);
		
		correctedLabel = new JLabel("");
		correctedLabel.setForeground(new Color(128, 0, 0));
		correctedLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		horizontalBox_1.add(correctedLabel);
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		horizontalBox_3.setBounds(410, 56, 251, 20);
		contentPane.add(horizontalBox_3);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_3.add(rigidArea_1);
		
		JLabel kLabel = new JLabel("k: ");
		kLabel.setForeground(new Color(128, 0, 0));
		kLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		horizontalBox_3.add(kLabel);
		
		kText = new JTextField();
		kText.setText("10");
		horizontalBox_3.add(kText);
		kText.setColumns(10);
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_3.add(rigidArea_2);
		
		JLabel wLabel = new JLabel("w: ");
		wLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		wLabel.setForeground(new Color(128, 0, 0));
		horizontalBox_3.add(wLabel);
		
		pageRankW = new JTextField();
		horizontalBox_3.add(pageRankW);
		pageRankW.setText("0.4");
		pageRankW.setPreferredSize(new Dimension(125, 23));
		pageRankW.setColumns(10);
		
		resultLabel = new JLabel("");
		resultLabel.setForeground(new Color(128, 0, 0));
		resultLabel.setBounds(10, 76, 188, 20);
		contentPane.add(resultLabel);
		
		timeLabel = new JLabel("");
		timeLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		timeLabel.setFont(new Font("Tahoma", Font.ITALIC, 10));
		timeLabel.setForeground(Color.GRAY);
		timeLabel.setBounds(505, 76, 156, 20);
		contentPane.add(timeLabel);
		
		resultArea = new JEditorPane();
		//JScrollPane sp = new JScrollPane(resultArea);
		resultArea.setBounds(10, 129, 651, 485);
		resultArea.setEditable(false);
		contentPane.add(resultArea);
		//editorPane.setVisible(false);
		resultArea.setContentType("text/html");
		resultArea.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                    System.out.println(e.getURL());
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
		
		previousButton = new JButton("Previous");
		previousButton.setVisible(false);
		previousButton.setForeground(new Color(128, 0, 0));
		previousButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		previousButton.setBounds(10, 625, 89, 23);
		contentPane.add(previousButton);
		
		previousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					if(resultCount > 0)
					{
						resultCount--;
						int currentPage = resultCount+1;
						if(SearchFiles.AH_flag)
						{
							resultArea.setText("");
							pageLabel.setText("Page: "+currentPage+"/" +pageCount);
							displayResults(SearchFiles.ahl);
						}
						else
						{
							resultArea.setText("");
							pageLabel.setText("Page: "+currentPage+"/" +pageCount);
							displayResults(SearchFiles.results);
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		pageLabel = new JLabel("");
		contentPane.add(pageLabel);
		pageLabel.setBounds(new Rectangle(304, 625, 110, 23));
		pageLabel.setMinimumSize(new Dimension(100, 23));
		pageLabel.setSize(new Dimension(110, 23));
		
		nextButton = new JButton("Next");
		nextButton.setVisible(false);
		nextButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		nextButton.setForeground(new Color(128, 0, 0));
		nextButton.setSize(80, 23);
		nextButton.setLocation(581, 625);
		contentPane.add(nextButton);
		nextButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		Box horizontalBox_4 = Box.createHorizontalBox();
		horizontalBox_4.setBounds(10, 107, 519, 23);
		contentPane.add(horizontalBox_4);
		
		qe1Label = new JLabel("");
		qe1Label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String eq = qe1Label.getText();
				String newQuery = query.getText() + " " + eq;
				query.setText(newQuery);
				runQuery();
			}
		});
		
		queryLabel = new JLabel("Add to query: ");
		queryLabel.setForeground(new Color(128, 0, 0));
		queryLabel.setVisible(false);
		horizontalBox_4.add(queryLabel);
		horizontalBox_4.add(qe1Label);
		qe1Label.setForeground(new Color(0, 0, 255));
		
		Component rigidArea = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_4.add(rigidArea);
		
		qe2Label = new JLabel("");
		qe2Label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String eq = qe2Label.getText();
				String newQuery = query.getText() + " " + eq;
				query.setText(newQuery);
				runQuery();
			}
		});
		horizontalBox_4.add(qe2Label);
		qe2Label.setForeground(new Color(0, 0, 255));
		
		Component rigidArea_3 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_4.add(rigidArea_3);
		
		qe3Label = new JLabel("");
		qe3Label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String eq = qe3Label.getText();
				String newQuery = query.getText() + " " + eq;
				query.setText(newQuery);
				runQuery();
			}
		});
		horizontalBox_4.add(qe3Label);
		qe3Label.setForeground(new Color(0, 0, 255));
		
		Component rigidArea_4 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_4.add(rigidArea_4);
		
		qe4Label = new JLabel("");
		qe4Label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String eq = qe4Label.getText();
				String newQuery = query.getText() + " " + eq;
				query.setText(newQuery);
				runQuery();
			}
		});
		horizontalBox_4.add(qe4Label);
		qe4Label.setForeground(new Color(0, 0, 255));
		
		Component rigidArea_5 = Box.createRigidArea(new Dimension(20, 20));
		horizontalBox_4.add(rigidArea_5);
		
		qe5Label = new JLabel("");
		qe5Label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String eq = qe5Label.getText();
				String newQuery = query.getText() + " " + eq;
				query.setText(newQuery);
				runQuery();
			}
		});
		horizontalBox_4.add(qe5Label);
		qe5Label.setForeground(new Color(0, 0, 255));
		
		Box horizontalBox_5 = Box.createHorizontalBox();
		horizontalBox_5.setBounds(528, 110, 133, 20);
		contentPane.add(horizontalBox_5);
		
		cluster = new JLabel("::Get Clusters::");
		horizontalBox_5.add(cluster);
		cluster.setVisible(false);
		cluster.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				kmk.getText();
				try{
					KMeanClustering.setClusterCount(Integer.parseInt(kmk.getText()));
					}
					catch(NumberFormatException e1){
						KMeanClustering.setClusterCount(3);
					}
				long time = System.currentTimeMillis();
				Clusters[] clusters = KMeanClustering.getClusters(SearchFiles.results);
				time = System.currentTimeMillis() - time;
				System.out.println("Clustering time: "+time+" milliseconds");
				System.out.println("");
				displayClusters(clusters);
			}
		});
		cluster.setForeground(new Color(128, 0, 0));
		
		kmk = new JTextField();
		kmk.setText("3");
		horizontalBox_5.add(kmk);
		kmk.setColumns(10);
		
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					if(resultCount < (pageCount-1))
					{
						resultCount++;
						int currentPage = resultCount+1;
						if(SearchFiles.AH_flag)
						{
							resultArea.setText("");
							pageLabel.setText("Page: "+currentPage+"/" +pageCount);
							displayResults(SearchFiles.ahl);
						}
						else
						{
							resultArea.setText("");
							pageLabel.setText("Page: "+currentPage+"/" +pageCount);
							displayResults(SearchFiles.results);
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
				
		GoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runQuery();
			}
		});
	}
	
	static private void runQuery(){
		init();
		try {
			if(query.getText().compareTo("") != 0){
				if(pageRankW.getText().compareTo("") == 0)
					SearchFiles.w = 0.4;
				if(kText.getText().compareTo("") == 0)
					SearchFiles.k = 10;
				else
				{
					try{
					SearchFiles.w = Double.parseDouble(pageRankW.getText());
					SearchFiles.k = Integer.parseInt(kText.getText());
					}
					catch(NumberFormatException e){
						SearchFiles.w = 0.4;
						SearchFiles.k = 10;
					}
				}
				long time = System.currentTimeMillis();
				String suggest = SpellCorrector.checkSpell(query.getText());
				SearchFiles.searchQuery(query.getText());
				time = System.currentTimeMillis() - time;
				timeLabel.setText("Results in: "+time+" milliseconds");
				correctedLabel.setText("");
				spellLabel.setVisible(false);
				if(SearchFiles.AH_flag)
				{
					if(!query.getText().toLowerCase().equals(suggest))
					{
						correctedLabel.setText(suggest);
						spellLabel.setVisible(true);
					}
					resultLabel.setText("Total results: " +SearchFiles.ahl.size());
					if(SearchFiles.ahl == null || SearchFiles.ahl.size() == 0)
					{
						resultArea.setText("No Results");
					}
					else{
						pageCount = (int)Math.ceil((double)SearchFiles.ahl.size()/(double)total);
						pageLabel.setText("Page: 1/" +pageCount);
						displayResults(SearchFiles.ahl);
						nextButton.setVisible(true);
						previousButton.setVisible(true);
					}
				}
				else
				{
					if(!query.getText().toLowerCase().equals(suggest))
					{
						correctedLabel.setText(suggest);
						spellLabel.setVisible(true);
					}
					resultLabel.setText("Total results: " +SearchFiles.results.length);
					if(SearchFiles.results.length == 0)
					{
						resultArea.setText("No Results");
					}
					else{
						pageCount = (int)Math.ceil((double)SearchFiles.results.length/(2.0*(double)total));
						pageLabel.setText("Page: 1/" +pageCount);
						displayResults(SearchFiles.results);
						nextButton.setVisible(true);
						previousButton.setVisible(true);
					}
				}
			}
			else
			{
				//correctedLabel.setText("");
				resultLabel.setText("No query. Really!!! :P");
				pageLabel.setText("");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	static private void displayResults(double[][] results) throws CorruptIndexException, IOException
	{
		setSC();
		int k = 2*total*resultCount;
		StringBuilder url = new StringBuilder();
		int counter = 1;
		long time = System.currentTimeMillis();
		while(k < results.length && k < 2*total*(resultCount+1))
		{
			String[] docParsed = DocParser.docParser((int)results[k][1], query.getText());
		
			url.append("+ <a href='" + docParsed[2] + "'>"/*+(int)results[k][1] +" : "*/ +docParsed[0]+ "</a><br>");
			url.append("\t"+docParsed[3]+"<br>");
			k++;
			counter++;
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Snippet generation time: "+time);
		resultArea.setText(url.toString());
	}
	
	static private void displayResults(AHlist ahl) throws CorruptIndexException, IOException
	{
		int k = total*resultCount;
		ahl.sort(AHlist.Type.authority);
		StringBuilder url = new StringBuilder();
		url.append("<b>Authorities:</b><br>");
		int counter = 1;
		while(k < ahl.size() && k < total*(resultCount+1))
		{
			Document d2 = SearchFiles.r.document(ahl.get(k).DocID);
			// the 'path' field of the Document object holds the URL
			String url2 = d2.getFieldable("path").stringValue();
			url2 = filePath + url2.replace("%%", "%25%25");
			//System.out.println(url2);
			url.append(counter+". <a href='" + url2 + "'> Doc " +ahl.get(k).DocID + "</a><br>");
			k++;
			counter++;
		}
		ahl.sort(AHlist.Type.hub);
		k = total*resultCount;
		url.append("<b>Hubs:</b><br>");
		counter = 1;
		while(k < ahl.size() && k < total*(resultCount+1))
		{
			Document d2 = SearchFiles.r.document(ahl.get(k).DocID);
			// the 'path' field of the Document object holds the URL
			String url2 = d2.getFieldable("path").stringValue();
			url2 = filePath + url2.replace("%%", "%25%25");
			url.append(counter+". <a href='" + url2 + "'> Doc " +ahl.get(k).DocID + "</a><br>");
			k++;
			counter++;
		}
		resultArea.setText(url.toString());
	}
	
	static private void setSC(){
		cluster.setVisible(true);
		queryLabel.setVisible(true);
		String[] sc = SearchFiles.sc;
		qe1Label.setText(sc[0]);
		qe2Label.setText(sc[1]);
		qe3Label.setText(sc[2]);
		qe4Label.setText(sc[3]);
		qe5Label.setText(sc[4]);
	}
	
	static private void init()
	{
		resultCount = 0;
		pageCount = 0;
		nextButton.setVisible(false);
		previousButton.setVisible(false);
		pageLabel.setText("");
		resultArea.setText("");
		timeLabel.setText("");
		queryLabel.setVisible(false);
		cluster.setVisible(false);
		
		qe1Label.setText("");
		qe2Label.setText("");
		qe3Label.setText("");
		qe4Label.setText("");
		qe5Label.setText("");
	}
	
	void displayClusters(Clusters[] clusters) {
		StringBuilder text = new StringBuilder();
		for(int i = 0; i < clusters.length; i++) {
			int j = i+1;
			//System.out.print("cluster: "+j+"  ");
			text.append("<b>Cluster"+j+":  </b><i>");
			for(int k = 0; k < 4; k++) {
				//System.out.print(clusters[i].summary[k]+", ");
				text.append(clusters[i].summary[k]+", ");
			}
			//System.out.println("");
			text.append(clusters[i].summary[4] +"</i><br>");
			for(int k = 0; k < 3 && k < clusters[i].doc.size(); k++) {
				int docid = clusters[i].doc.get(k);
				String[] docParsed = DocParser.docParser(docid, query.getText());
				text.append("+ <a href='" + docParsed[2] + "'>"/*+docid +" : " */+docParsed[0]+ "</a><br>");
				text.append("\t"+docParsed[3]+"<br>");
				//System.out.println(docParsed[2]);
			}
		}
		resultArea.setText(text.toString());
	}
}
