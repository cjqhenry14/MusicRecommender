package tagBasedTFIDF;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;




import java.util.HashSet;
import java.util.Map.Entry;

import validation.measure;
import bayesian.ArtistProb;
import bayesian.ArtistUserPair;
import bayesian.DataProcessor;
import bayesian.Measure;
import bayesian.ProbComp;
import bayesian.UserNode;


public class TagBasedTFIDF {

	static String userTaggedArtistsData="dataset/user_taggedartists.dat";
	static String result_file="results_analysis/tagBasedTFIDF_res.dat";
	static HashMap<Integer,UserNode> userNodeMap=new HashMap<Integer,UserNode>();
	static ArrayList<Integer> validArtistsList=new ArrayList<Integer>();
	static ArrayList<int[]> records=new ArrayList<int []>();
	static int maxArtistID=18746, maxTagID=12648 , maxUserID=2101;
			
	static int[][] userTags, tagArtists, userArtists;
	static int[]tagDistinctUserCount, artistDistinctUserCount;
	
	public static void processData() throws FileNotFoundException, IOException{
		
		DataProcessor.readTrainList(userNodeMap);
		System.out.println(userNodeMap.size());
		validArtistsList=DataProcessor.getValidArtists();
		readUserArtistsTag();
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		
		processData();
		
		PrintWriter writer;
	
		writer = new PrintWriter(result_file, "UTF-8");
		ArrayList<ArtistProb> recommandList=new ArrayList<ArtistProb>();
		for (Entry<Integer, UserNode> entry : userNodeMap.entrySet()) {
			Integer uid = entry.getKey();
			System.out.println("Recommand for "+uid);
			recommandList=recommand(uid);
			if (recommandList.size()>0 )
			{	Collections.sort(recommandList, new ProbComp());

			
				writer.print(uid+":");
			for (int i=0;i<Math.min(10,recommandList.size())-1;i++)
			{
				writer.print(" "+recommandList.get(i).artist+",");
			}
			
			writer.print(" "+recommandList.get(Math.min(10,recommandList.size()-1)).artist+"\n");
			}
		}
		writer.close();
		//Measure.call_measure(result_file);
		measure m = new measure();
		m.call_measure(result_file);
		
	}
	

	

	

	public static void readUserArtistsTag(){
	try (BufferedReader br = new BufferedReader(new FileReader(userTaggedArtistsData))) {
	    String line;
	    int user,artist,tag;
	    int []record;
	    HashSet<Integer> tags=new HashSet<Integer>();
	   
	    while ((line = br.readLine()) != null) {
	       //System.out.println(line);
	       String[] data = line.split("\\s");
	       user=Integer.parseInt(data[0].trim());
	       artist=Integer.parseInt(data[1].trim());
	       tag=Integer.parseInt(data[2].trim());
	       tags.add(tag);
	       record=new int[]{user,artist,tag};
	       //System.out.println(record[0]+" "+record[1]+" "+record[2]);
	     
	       records.add(record);
	      
	    }
	    
	    userTags=new int [maxUserID][maxTagID];
	    tagArtists=new int [maxTagID][maxArtistID];
	    userArtists=new int [maxUserID][maxArtistID];
	    tagDistinctUserCount=new int[maxTagID];
	    artistDistinctUserCount=new int[maxArtistID];
	    
	    for (int i=0;i<records.size();i++)
	    	{
	    	userTags[records.get(i)[0]][records.get(i)[2]]++;
	    	tagArtists[records.get(i)[2]][records.get(i)[1]]++;
	    	userArtists[records.get(i)[0]][records.get(i)[1]]++;
	    	}
	    
	    for (int tid=1;tid<maxTagID;tid++)
	    	{for (int uid=1;uid<maxUserID;uid++)
	    		{if (userTags[uid][tid]>0)
	    			tagDistinctUserCount[tid]++;
	    		}	
	    	}
	    for (int aid=1;aid<maxArtistID;aid++)
    	{for (int uid=1;uid<maxUserID;uid++)
    		{if (userArtists[uid][aid]>0)
    			artistDistinctUserCount[aid]++;
    		}	
    	}
	
	}
	catch (Exception e)
	{
	    System.err.println("trainList "+e.getMessage()); 
	}
	
	}
	
	public static ArrayList<ArtistProb> recommand(int uid){
		UserNode un=new UserNode();
		un=userNodeMap.get(uid);
		ArrayList<ArtistProb> recommandList=new ArrayList<ArtistProb>();
		HashMap<Integer,ArtistProb> scoreMap = new HashMap<Integer,ArtistProb>();
		ArtistProb as;
		for (int tag=1;tag<maxTagID;tag++)		
		{
			
			if (userTags[uid][tag]==0)
				continue;
			for (int artist=1;artist<maxArtistID;artist++)
			{
				if (tagArtists[tag][artist]==0 || un.artists.contains(artist))
					continue;
				
				if (scoreMap.get(artist)==null)
					{
					//as=new ArtistProb(artist,userTags[uid][tag]*tagArtists[tag][artist],0);
					as=new ArtistProb(artist,userTags[uid][tag]*1.0/Math.log(1+tagDistinctUserCount[tag])*tagArtists[tag][artist]*1.0/Math.log(1+artistDistinctUserCount[artist]),0);
					scoreMap.put(artist,as);
					}
				else
					{
					//scoreMap.get(artist).prob1+=userTags[uid][tag]*tagArtists[tag][artist];
					scoreMap.get(artist).prob1+=userTags[uid][tag]*1.0/Math.log(1+tagDistinctUserCount[tag])*tagArtists[tag][artist]*1.0/Math.log(1+artistDistinctUserCount[artist]);
					}
			}
		}
		
		for (Entry<Integer,ArtistProb> entry : scoreMap.entrySet()) {
			Integer key = entry.getKey();
			recommandList.add(scoreMap.get(key));
		}
		return recommandList;
		
	}
	
	
}