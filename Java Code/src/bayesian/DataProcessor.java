package bayesian;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class DataProcessor {
	static String trainData = "dataset/user_artists_train.dat";
	static String artistsData = "dataset/user_artists.dat";
	static String friendData = "dataset/user_friends.dat";

	static int maxHop = 1;

	static double rateThreshold = 0.1;
	static double probThreshold = 0.2;
	static int group=5;

	public static ArrayList<Integer> getValidArtists() {
		try (BufferedReader br = new BufferedReader(new FileReader(artistsData))) {
			String line;
			UserNode un;
			String[] data;
			int artist;
			ArrayList<Integer> allArtistsList = new ArrayList<Integer>();
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				// System.out.println(++linenum);
				data = line.split("\\s+");
				artist = Integer.parseInt(data[1]);
				allArtistsList.add(artist);
			}
			Set<Integer> distinctArtistsList = new HashSet<Integer>();
			distinctArtistsList.addAll(allArtistsList);
			return new ArrayList<Integer>(distinctArtistsList);
		} catch (Exception e) {
			System.err.println("trainList " + e.getMessage());
		}
		return null;

	}

	/*
	 * get user's artists list
	 * */
	public static void readTrainList(HashMap<Integer, UserNode> userMap)
			throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(trainData))) {
			String line;
			UserNode un;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String[] data = line.split(":");
				int id = Integer.parseInt(data[0]);
				String[] artists = data[1].trim().split(", ");
				ArrayList<Integer> artistsList = new ArrayList<Integer>();
				for (int i = 0; i < artists.length; i++)
					artistsList.add(Integer.parseInt(artists[i]));
				Collections.sort(artistsList);

				un = new UserNode(id, artistsList);
				userMap.put(id, un);
			}

		} catch (Exception e) {
			System.err.println("trainList " + e.getMessage());
		}
	}

	/*
	 * store all user-artist rate pair in rateMap
	 */
	public static ArrayList<ArtistUserPair> readAllArtistsRate(
			HashMap<Integer, UserNode> userNodeMap,
			HashMap<ArtistUserPair, Integer> rateMap)
			throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(artistsData))) {
			String line;
			int currentID = 0;
			int lastUserID = 0;
			ArtistRate ar;
			ArrayList<ArtistRate> userRateList = new ArrayList<ArtistRate>();

			int id;
			String[] data;
			int artist;
			int rate;
			int linenum = 0;
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				// System.out.println(++linenum);
				data = line.split("\\s+");
				id = Integer.parseInt(data[0]);
				lastUserID = id;
				artist = Integer.parseInt(data[1]);
				rate = Integer.parseInt(data[2]);

				if (currentID != id) {
					updateRate(currentID, userRateList, userNodeMap, rateMap);
					userRateList = new ArrayList<ArtistRate>();
					currentID = id;
				}
				ar = new ArtistRate(artist, rate);
				// System.out.println("put "+ar.toString());
				userRateList.add(ar);
			}
			// update last user rating
			updateRate(lastUserID, userRateList, userNodeMap, rateMap);
			ArrayList<ArtistUserPair> sortedKeys = new ArrayList<ArtistUserPair>(
					rateMap.keySet());
			Collections.sort(sortedKeys, new KeyComp());
			return sortedKeys;
		} catch (Exception e) {
			System.err.println("artist rate " + e.getMessage());
		}
		return null;
	}

	/*
	 * update rate, convert from number of listens to discrete rate, e.g rate 0 - rate 5
	 * compute prior
	 */
	public static void updateRate(int id, ArrayList<ArtistRate> userRateList,
			HashMap<Integer, UserNode> userNodeMap,
			HashMap<ArtistUserPair, Integer> rateMap) {
		// sort by play count to convert to discrete rating
		if (id > 0 && userNodeMap.get(id) != null) {
			Collections.sort(userRateList, new RateComp());
			int min = userRateList.get(0).rate;
			int max = userRateList.get(userRateList.size() - 1).rate;
			double[] p = new double[group];
			for (int i = 0; i < group; i++)
				p[i] = min + (max - min) * (i + 1) / group;
			int[] count = new int[group];
			int total = userRateList.size();
			for (int i = 0; i < userRateList.size(); i++) {
				{
					for (int j = 0; j < group; j++) {
						if (userRateList.get(i).rate <= p[j]) {
							userRateList.get(i).rate = j;
							count[j]++;
							break;
						}
					}
				}
			}
			// sort by discrete rate then secondary by artist id
			Collections.sort(userRateList, new RateComp());
			ArtistUserPair aup;
			// System.out.println("user "+id);
			for (int i = 0; i < userRateList.size(); i++) {
				aup = new ArtistUserPair(userRateList.get(i).artist, id);
				rateMap.put(aup, userRateList.get(i).rate);
				// System.out.println(id+" "+userRateList.get(i).rate);
			}

			double[] prior = new double[group];
			for (int i=0;i<group;i++)
				prior[i]=count[i] * 1.0 / total;
			userNodeMap.get(id).prior = prior;
		}
	}
	
	/*
	 * update user's friends relation
	 */
	public static void readFriendsList(HashMap<Integer, UserNode> userNodeMap) {
		try (BufferedReader br = new BufferedReader(new FileReader(friendData))) {
			String line;
			int currentID = 0;
			int lastUserID = 0;
			ArrayList<Integer> userFriendList = new ArrayList<Integer>();
			int linenum = 0;
			while ((line = br.readLine()) != null) {
				// System.out.println(++linenum);
				// System.out.println(line);
				String[] data = line.split("\\s+");
				int id = Integer.parseInt(data[0]);
				lastUserID = id;
				int friend = Integer.parseInt(data[1]);

				if (currentID != id) {
					updateFriendList(currentID, userFriendList, userNodeMap);
					userFriendList = new ArrayList<Integer>();
					currentID = id;
				}
				if (userNodeMap.get(friend) != null)
					userFriendList.add(friend);
			}
			updateFriendList(lastUserID, userFriendList, userNodeMap);
			// System.out.println("friend "+userFriendList);
		} catch (Exception e) {
			System.err.println("friend list " + e.getMessage());
		}
	}

	public static void updateFriendList(int id, ArrayList<Integer> friendList,
			HashMap<Integer, UserNode> userNodeMap) {
		if (id > 0 && userNodeMap.get(id) != null) {
			Collections.sort(friendList);
			userNodeMap.get(id).friends = friendList;
		}
	}
	
	/*
	 * store artists rating information in userNode, estimate friend's rating if not exist
	 */
	public static void updateTrainArtistsRate(
			HashMap<Integer, UserNode> userNodeMap,
			HashMap<ArtistUserPair, Integer> rateMap) {
		ArrayList<Integer> rateList;
		for (Entry<Integer, UserNode> entry : userNodeMap.entrySet()) {
			Integer uid = entry.getKey();
			rateList = new ArrayList<Integer>();
			for (int i = 0; i < userNodeMap.get(uid).artists.size(); i++) {
				rateList.add(getRate(rateMap, userNodeMap,
						userNodeMap.get(uid).artists.get(i), uid));
			}
			userNodeMap.get(uid).rates = rateList;
		}
	}

	/*
	 * store artists rating information in userNode without estimate friend's rating
	 */
	public static void updateTrainArtistsRateNoEstimate(
			HashMap<Integer, UserNode> userNodeMap,
			HashMap<ArtistUserPair, Integer> rateMap) {
		ArrayList<Integer> rateList;
		for (Entry<Integer, UserNode> entry : userNodeMap.entrySet()) {
			Integer uid = entry.getKey();
			rateList = new ArrayList<Integer>();
			for (int i = 0; i < userNodeMap.get(uid).artists.size(); i++) {
				rateList.add(getRateNoEstimate(rateMap, userNodeMap,
						userNodeMap.get(uid).artists.get(i), uid));
			}
			userNodeMap.get(uid).rates = rateList;

		}

	}

	/*
	 * compute and store probability information in userNode
	 */
	public static void updateProbTable(HashMap<Integer, UserNode> userNodeMap,
			HashMap<ArtistUserPair, Integer> rateMap, int id, int friend) {
		ArrayList<Integer> commonArtists = getCommonArtists(userNodeMap, id,
				friend);
		ArrayList<int[]> ratePairList = new ArrayList<int[]>();
		int[] ratePair;
		for (int i = 0; i < commonArtists.size(); i++) {
			ratePair = new int[] {
					getRate(rateMap, userNodeMap, commonArtists.get(i), id),
					getRate(rateMap, userNodeMap, commonArtists.get(i), friend) };
			ratePairList.add(ratePair);
		}
		double[][] prob = new double[group][group];
		int[][] count = new int[group][group];
		for (int i = 0; i < ratePairList.size(); i++) {
			for (int row = 0; row < group; row++)
				for (int col = 0; col < group; col++) {
					if (ratePairList.get(i)[0] == row
							&& ratePairList.get(i)[1] == col)
						count[row][col]++;
				}
		}
		int[] sum = new int[group];
		for (int row = 0; row < group; row++)
			for (int col = 0; col < group; col++) {
				sum[row] += count[row][col];
			}

		for (int row = 0; row < group; row++)
			for (int col = 0; col < group; col++) {
				if (sum[row] == 0)
					prob[row][col] = -1;
				else
					prob[row][col] = count[row][col] * 1.0 / sum[row];
			}
		userNodeMap.get(id).probTable.put(friend, prob);
	}
	
	public static int getRate(HashMap<ArtistUserPair, Integer> RateMap,
			HashMap<Integer, UserNode> userNodeMap, int artistID, int userID) {
		ArtistUserPair au = new ArtistUserPair(artistID, userID);
		if (RateMap.get(au) != null) { 
			// System.out.println("Rate "+RateMap.get(au));
			return RateMap.get(au);
		} else if (userNodeMap.get(userID).estimateRate.get(artistID) != null)
			return userNodeMap.get(userID).estimateRate.get(artistID);
		// System.out.println("Key not found");
		
		return -1;
	}
	
	public static int getRateNoEstimate(
			HashMap<ArtistUserPair, Integer> RateMap,
			HashMap<Integer, UserNode> userNodeMap, int artistID, int userID) {
		ArtistUserPair au = new ArtistUserPair(artistID, userID);
		if (RateMap.get(au) != null) { // System.out.println("Rate "+RateMap.get(au));
			return RateMap.get(au);
		}
		return -1;
	}
	
	public static ArrayList<Integer> getCommonArtists(
			HashMap<Integer, UserNode> userNodeMap, int user1, int user2) {
		ArrayList<Integer> artist1 = userNodeMap.get(user1).artists;
		ArrayList<Integer> artist2 = userNodeMap.get(user2).artists;
		ArrayList<Integer> commonArtistsList = new ArrayList<Integer>(artist1);
		commonArtistsList.retainAll(artist2);
		// System.out.println(commonArtistsList);
		return commonArtistsList;
	}
	
	/*
	 * compute bayesian given prior and cpt
	 * return most probable rate and probabilities 
	 */
	public static double[] computeBayesian(int artist, int uid,
			ArrayList<Integer> friendRates,
			HashMap<Integer, UserNode> userNodeMap) {

		double summation = 0;
		double[] products = new double[group];
		for (int r = 0; r < group; r++) {

			products[r] = userNodeMap.get(uid).prior[r];
			// System.out.println("prior "+userNodeMap.get(uid).prior[r]);
			for (int i = 0; i < friendRates.size(); i++) {
				//System.out.println("friend "+i+" rate "+friendRates.get(i)+" give r="+r);
				if ((friendRates.get(i) > -1)
						&& (userNodeMap.get(uid).probTable.get(userNodeMap
								.get(uid).friends.get(i))[r][friendRates.get(i)] > -1))

				{
					products[r] *= (userNodeMap.get(uid).probTable
							.get(userNodeMap.get(uid).friends.get(i)))[r][friendRates
							.get(i)];
					// System.out.println(r+" "+friendRates.get(i));
				}

			}
			summation += products[r];

		}
		if (summation == 0)
			return new double[] { -1, 0, 0 };

		double prob1 = (products[group-1]) / summation, 
			   prob2 = (products[group-2]) / summation;
		double largest = rateThreshold;
		int rate = -1;
		for (int i = 0; i < products.length; i++) {
			if (products[i] > largest) {
				largest = products[i];
				rate = i;
			}
		}
		return new double[] { rate, prob1, prob2 };
	}


	/*
	 * compute and store estimate rate in userNode
	 */
	public static int estimateFriendRate(int artist, int uid,
			HashMap<Integer, UserNode> userNodeMap,
			HashMap<ArtistUserPair, Integer> rateMap, int hop) {
		if (hop > maxHop)
			return -1;
		hop++;
		UserNode un = new UserNode();
		UserNode fn = new UserNode();
		un = userNodeMap.get(uid);
		ArrayList<Integer> friendRateList;
		ArtistUserPair aup;
		ArtistRate ar;
		friendRateList = new ArrayList<Integer>();
		double[] temp = new double[2];
		// System.out.println("friend size "+un.friends.size());
		for (int fidx = 0; fidx < un.friends.size(); fidx++) {
			fn = userNodeMap.get(un.friends.get(fidx));
			aup = new ArtistUserPair(artist, un.friends.get(fidx));
			if (rateMap.get(aup) != null)
				friendRateList.add(rateMap.get(aup));
			else if (fn.estimateRate.get(artist) != null)
				friendRateList.add(fn.estimateRate.get(artist));
			else {
				int estimateRate = estimateFriendRate(artist,
						un.friends.get(fidx), userNodeMap, rateMap, hop);
				// if (estimateRate>-1)
				fn.estimateRate.put(artist, estimateRate);
				friendRateList.add(estimateRate);
				// friendRateList.add(-1);
			}
		}
		// System.out.println("  "+artist+" "+uid);

		temp = computeBayesian(artist, uid, friendRateList, userNodeMap);
		int rate = (int) temp[0];

		return rate;

	}

	


}
