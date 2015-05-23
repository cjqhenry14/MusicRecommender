package bayesian;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import validation.measure;

public class Bayesian {
	static String result_file = "results_analysis\\bayesian_res.dat";

	static HashMap<Integer, UserNode> userNodeMap = new HashMap<Integer, UserNode>();
	static HashMap<ArtistUserPair, Integer> rateMap = new HashMap<ArtistUserPair, Integer>();
	static ArrayList<ArtistUserPair> rateMapKey = new ArrayList<ArtistUserPair>();
	static ArrayList<Integer> validArtistsList = new ArrayList<Integer>();

	static int iteration = 1;
	static int norate=0,erate=0,hasrate=0,total=0;
	
	public static void processData() throws FileNotFoundException, IOException {
		DataProcessor.readTrainList(userNodeMap);
		System.out.println(userNodeMap.size());
		validArtistsList = DataProcessor.getValidArtists();
		rateMapKey = DataProcessor.readAllArtistsRate(userNodeMap, rateMap);
		DataProcessor.readFriendsList(userNodeMap);
		DataProcessor.updateTrainArtistsRate(userNodeMap, rateMap);

		for (Entry<Integer, UserNode> entry : userNodeMap.entrySet()) {
			Integer uid = entry.getKey();

			for (int i = 0; i < userNodeMap.get(uid).friends.size(); i++) {
				DataProcessor.updateProbTable(userNodeMap, rateMap, uid,
						userNodeMap.get(uid).friends.get(i));
			}
		}

	}
	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		// TODO Auto-generated method stub
		long startTime = System.currentTimeMillis();

		PrintWriter writer;

		processData();

		PrintWriter writer2 = new PrintWriter("temp.txt", "UTF-8");

		for (int rt = 0; rt < iteration; rt++) { 
			// for (int pt=0;pt<iteration;pt++)
			{
				writer = new PrintWriter(result_file, "UTF-8");
				ArrayList<ArtistProb> recommandList;
				int num = 1;
				for (Entry<Integer, UserNode> entry : userNodeMap.entrySet()) {
					// if (num>200)
					// break;
					Integer uid = entry.getKey();
					num++;
					System.out.println("Recommand for " + uid);
					recommandList = recommand(uid);
					if (recommandList.size() > 0) {
						Collections.sort(recommandList, new ProbComp());
						/*
						 * for (int i=0;i<Math.min(10,recommandList.size());i++)
						 * System.out.print(recommandList.get(i).toString());
						 * System.out.println("");
						 */
						writer.print(uid + ":");
						for (int i = 0; i < Math.min(10, recommandList.size()) - 1; i++) {
							writer.print(" " + recommandList.get(i).artist
									+ ",");
						}

						writer.print(" "
								+ recommandList.get(Math.min(10,
										recommandList.size() - 1)).artist
								+ "\n");
					}

				}

				writer.close();

				writer2.print("rateThreshold: " + DataProcessor.rateThreshold
						+ " probThreshold: " + DataProcessor.probThreshold
						+ "\n");
				writer2.print(Measure.call_measure(result_file));
				writer2.print("\n");
				measure m = new measure();
				m.call_measure(result_file);
				// DataProcessor.probThreshold+=1.0/iteration;
			}
			// DataProcessor.probThreshold=0.1;
			DataProcessor.rateThreshold += 0.1 / iteration;
		}
		writer2.println("Total time in Min  :"
				+ (int) ((System.currentTimeMillis() - startTime) / 60000));
		writer2.close();

		System.out.println("Query returns Num of No Rate="+norate+" Num of Estimate Rate="+erate+" Num of Actual Rate="+hasrate+" Total Query="+total);
		System.out.println("No Rate ratio="+norate*1.0/total+" Estimate Rate Ratio="+erate*1.0/total+" Actual Rate Ratio="+hasrate*1.0/total);


	}

	public static ArrayList<ArtistProb> recommand(int uid) {
		ArrayList<ArtistProb> recommandList = new ArrayList<ArtistProb>();

		UserNode un = new UserNode();
		un = userNodeMap.get(uid);
		UserNode fn = new UserNode();
		ArtistUserPair aup;
		ArrayList<Integer> friendRateList;

		ArtistProb ap;
		double[] temp = new double[3];
		double prob1, prob2;
		int rate;
		int artist;
		if (un.friends.size() > 0) {
			for (int aIndex = 0; aIndex < validArtistsList.size(); aIndex++) {
				artist = validArtistsList.get(aIndex);
				if (!(un.artists.contains(artist))) {
					friendRateList = new ArrayList<Integer>();
					// System.out.println("friend size "+un.friends.size());
					for (int fidx = 0; fidx < un.friends.size(); fidx++) {
						total++;
						aup = new ArtistUserPair(artist, un.friends.get(fidx));
						fn = userNodeMap.get(un.friends.get(fidx));
						if (rateMap.get(aup) != null){
							friendRateList.add(rateMap.get(aup));
							hasrate++;
							}
						else if (fn.estimateRate.get(artist) != null){
							erate++;
							friendRateList.add(fn.estimateRate.get(artist));
							}
						else {
							int estimateRate = DataProcessor
									.estimateFriendRate(artist,
											un.friends.get(fidx), userNodeMap,
											rateMap, 1);

							friendRateList.add(estimateRate);
							if (estimateRate==-1)
								norate++;
							else
								erate++;
							fn.estimateRate.put(artist, estimateRate);
						}

					}
					// System.out.println("  "+artist+" "+uid);

					temp = DataProcessor.computeBayesian(artist, uid,
							friendRateList, userNodeMap);
					rate = (int) temp[0];
					prob1 = temp[1];
					prob2 = temp[2];
					if (prob1 > DataProcessor.probThreshold) {
						ap = new ArtistProb(artist, prob1, prob2);
						recommandList.add(ap);
					}
				}
			}
		}
		return recommandList;

	}



	public static void printMap(HashMap<ArtistUserPair, Integer> mp) {
		for (Entry<ArtistUserPair, Integer> entry : mp.entrySet()) {
			ArtistUserPair key = entry.getKey();
			System.out.print(key.toString());
		}
	}

}
