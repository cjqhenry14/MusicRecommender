package naiveBayes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import validation.measure;
import bayesian.*;

public class NaiveBayes {

	static HashMap<Integer, UserNode> userNodeMap = new HashMap<Integer, UserNode>();
	static HashMap<ArtistUserPair, Integer> rateMap = new HashMap<ArtistUserPair, Integer>();
	static ArrayList<ArtistUserPair> rateMapKey = new ArrayList<ArtistUserPair>();
	static ArrayList<Integer> validArtistsList = new ArrayList<Integer>();
	static String result_file = "results_analysis\\naive_bayes_res.dat";
	static int maxArtistID = 18746;
	static int thresholdRate = 3;
	static int cn = 3;
	static int totalNumUser;

	static double[] prior;
	static double[][] cpt;

	public static void processData() throws FileNotFoundException, IOException {

		DataProcessor.readTrainList(userNodeMap);
		System.out.println(userNodeMap.size());
		validArtistsList = DataProcessor.getValidArtists();
		rateMapKey = DataProcessor.readAllArtistsRate(userNodeMap, rateMap);
		// DataProcessor.readFriendsList(userNodeMap);
		// Collections.sort(uList, new UserNodeComp());
		DataProcessor.updateTrainArtistsRateNoEstimate(userNodeMap, rateMap);

		prior = computePrior(userNodeMap);
		cpt = computeCPT(userNodeMap, prior);

	}

	public static double[] computePrior(HashMap<Integer, UserNode> userNodeMap) {
		double[] prior = new double[maxArtistID];
		ArtistUserPair aup;
		int artist;
		int user;

		for (int i = 0; i < rateMapKey.size(); i++) {
			artist = rateMapKey.get(i).artist;
			user = rateMapKey.get(i).user;
			aup = new ArtistUserPair(artist, user);
			if (rateMap.get(aup) != null && rateMap.get(aup) >= thresholdRate) {
				prior[artist] += 1;

			}
		}

		totalNumUser = userNodeMap.size();
		for (int i = 0; i < maxArtistID; i++)
			prior[i] = prior[i] * 1.0 / totalNumUser;

		return prior;
	}

	public static double[][] computeCPT(HashMap<Integer, UserNode> userNodeMap,
			double[] prior) {
		double[][] cpt = new double[maxArtistID][maxArtistID];

		UserNode un = new UserNode();
		for (Entry<Integer, UserNode> entry : userNodeMap.entrySet()) {
			Integer uid = entry.getKey();
			for (int i = 0; i < userNodeMap.get(uid).artists.size(); i++) {
				if (userNodeMap.get(uid).rates.get(i) >= thresholdRate) {
					for (int j = 0; j < userNodeMap.get(uid).artists.size(); j++) {
						if ((i != j)
								&& (userNodeMap.get(uid).rates.get(j) >= thresholdRate)) {
							cpt[userNodeMap.get(uid).artists.get(i)][userNodeMap
									.get(uid).artists.get(j)]++;
						}
					}
				}
			}
		}
		for (int i = 0; i < maxArtistID; i++)
			for (int j = 0; j < maxArtistID; j++) {
				if (prior[j] > 0)
					cpt[i][j] = cpt[i][j] * 1.0 / totalNumUser / prior[j];
			}
		return cpt;
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		// TODO Auto-generated method stub

		processData();
		PrintWriter writer;

		writer = new PrintWriter(result_file, "UTF-8");
		ArrayList<ArtistProb> recommandList;
		int num = 0;
		for (Entry<Integer, UserNode> entry : userNodeMap.entrySet()) {
			// if (num>200)
			// break;
			Integer uid = entry.getKey();
			num++;
			System.out.println("Recommand for " + uid);
			recommandList = recommand(uid, prior, cpt);
			if (recommandList.size() > 0) {
				Collections.sort(recommandList, new ProbComp());
				/*
				 * for (int i = 0; i < Math.min(10, recommandList.size()); i++)
				 * System.out.print(recommandList.get(i).toString());
				 * System.out.println("");
				 */
				if (recommandList.get(0).artist != 1) {
					writer.print(uid + ":");
					for (int i = 0; i < Math.min(10, recommandList.size()) - 1; i++) {
						writer.print(" " + recommandList.get(i).artist + ",");
					}

					writer.print(" "
							+ recommandList.get(Math.min(10,
									recommandList.size() - 1)).artist + "\n");
				}
			}

		}
		writer.close();

	     Measure.call_measure(result_file);
		measure m = new measure();
		m.call_measure(result_file);
	}

	public static ArrayList<ArtistProb> recommand(int uid, double[] prior,
			double[][] cpt) {
		ArrayList<ArtistProb> recommandList = new ArrayList<ArtistProb>();

		UserNode un = new UserNode();
		un = userNodeMap.get(uid);
		int artist;
		double r;
		int n;
		ArtistProb ap;
		for (int aIndex = 0; aIndex < validArtistsList.size(); aIndex++) {

			artist = validArtistsList.get(aIndex);
			if (!(un.artists.contains(artist))) {
				r = prior[artist];
				n = 0;
				for (int i = 0; i < un.artists.size(); i++) {
					if (un.rates.get(i) >= thresholdRate)
						n++;
				}
				for (int i = 0; i < un.artists.size(); i++) {
					if (un.rates.get(i) >= thresholdRate)
						r = r
								* Math.pow(cpt[artist][un.artists.get(i)] * 1.0
										/ prior[artist], cn * 1.0 / n);
				}
				ap = new ArtistProb(artist, r, 0);
				recommandList.add(ap);
			}
		}

		return recommandList;
	}

}