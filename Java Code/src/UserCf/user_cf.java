package UserCf;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import validation.measure;

public class user_cf {

	//public static final String shi_file = "dataset/test.txt";
	public static final String shi_file = "dataset/user_artists_train.dat";
	public static final String rec_res_file = "results_analysis/usercf_rec_res.dat";
	public static int user_num = 2100;// userid -> [2, 2100], 1877 valid users
	public static int k = 15;
	public static HashSet<Integer> topkset;
	public static int rec_num = 10;
	/* artist_i: user1, user2,... */
	public static HashMap<Integer, ArrayList<Integer>> art_user_table;
	/* user: art_id1, art_id2,... */
	public static ArrayList<ArrayList<Integer>> user_art_table;
	/* user_id, user's artist list size, use for calculating similarity */
	public static int[] user_art_size;
	/* co-occurrence matrix */
	public static int cooccur[][];
	/* similarity(cosine similarity) matrix */
	public static float sim[][];
	
	public static FileWriter writer;
	public static BufferedWriter bw;
	/* art_user_table() -- artist_i: user1, user2,... */
	static void create_art_user_table() throws IOException {
		// init all
		art_user_table = new HashMap<Integer, ArrayList<Integer>>();
		user_art_size = new int[user_num + 1];
		cooccur = new int[user_num + 1][user_num + 1];
		sim = new float[user_num + 1][user_num + 1];
		user_art_table = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i <= user_num; i++)
			user_art_table.add(new ArrayList<Integer>());

		topkset = new HashSet<Integer>();
		FileReader reader = new FileReader(shi_file);
		BufferedReader br = new BufferedReader(reader);

		String line = null;
		/* read_train_file() */
		while ((line = br.readLine()) != null) {
			String[] values = line.split(":");
			int uid = Integer.parseInt(values[0]);
			String[] art_id_array = values[1].split(",");
			user_art_size[uid] = art_id_array.length;

			for (int i = 0; i < art_id_array.length; i++) {
				int art_id = Integer.parseInt(art_id_array[i].trim());
				if (!art_user_table.containsKey(art_id))
					art_user_table.put(art_id, new ArrayList<Integer>());

				art_user_table.get(art_id).add(uid);
				user_art_table.get(uid).add(art_id);
			}

		}// end while
		br.close();
		reader.close();
	}
	
	/* create co occurrence matrix, cooccur[][] */
	static void create_cooccur_matrix() {
		Iterator<Integer> it = art_user_table.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer) it.next();

			for (int i = 0; i < art_user_table.get(key).size(); i++) {
				for (int j = i + 1; j < art_user_table.get(key).size(); j++) {
					int user_id1 = art_user_table.get(key).get(i);
					int user_id2 = art_user_table.get(key).get(j);
					cooccur[user_id1][user_id2] ++;
					cooccur[user_id2][user_id1] ++;
				}
			}

		}//end while
	}
	
	/* create similarity(cosine similarity) matrix, sim[][] */
	static void create_sim_matrix() {
		for (int i = 2; i <= user_num; i++) {
			for (int j = 2; j <= user_num; j++) {
				double denominator = Math.sqrt(user_art_size[i] * user_art_size[j]);
				denominator = (double)(Math.round(denominator*100)/100.0);
				double similarity = cooccur[i][j] / denominator;
				similarity = (double)(Math.round(similarity*100)/100.0);
				sim[i][j] = (float)similarity;
			}
		}
	}
	
	/* ===============================get knn =========================================== */
	public static Comparator<neighbour> nComparator = new Comparator<neighbour>() {//use as minHeap
		public int compare(neighbour c1, neighbour c2) {
			return (int) Float.compare(c1.similarity, c2.similarity);
		}
	};
	public static class neighbour {
		int uid;
		float similarity;
		neighbour(int id, float sim)
		{
			uid = id;
			similarity = sim;
		}
	}
	/* get KNN list*/
	static HashSet<Integer> get_knn(int uid) {
		HashSet<Integer> topkset2 = new HashSet<Integer>();
		PriorityQueue<neighbour> min_heap = new PriorityQueue<neighbour>(k, nComparator);
		
		for (int i = 0; i <= user_num; i++) {

			neighbour nb = new neighbour(i, sim[uid][i]);
			
			if (min_heap.size() < k) {
				min_heap.add(nb);
			} else if (nb.similarity > min_heap.peek().similarity){/* meet capacity and > top*/
				min_heap.poll();
				min_heap.add(nb);
			}
		}
		/*put into topkset and return*/
		while (!min_heap.isEmpty()) {
			topkset2.add(min_heap.poll().uid);
		}
		
		return topkset2;
	}
	/* =============================== start recommend =========================================== */
	/*get potential art list: user's knn's art list, and remove user's own art list*/
	static HashSet<Integer> get_potential_artset(int uid) {
		
		topkset = get_knn(uid);
		//show_topkset_sim(uid, topkset);
		HashSet<Integer> potential_artset = new HashSet<Integer>();
		Iterator<Integer> ir = topkset.iterator(); 
		while(ir.hasNext()) {
			int cur_neighbour = ir.next();
			for (int k = 0; k < user_art_table.get(cur_neighbour).size(); k++) {//get neighbour's art list
				int cur_art = user_art_table.get(cur_neighbour).get(k);
				if(!potential_artset.contains(cur_art)){
					potential_artset.add(cur_art);
				}
			}
		}//end while
		/*remove user's own art list*/
		for (int q = 0; q < user_art_table.get(uid).size(); q++) {
			int cur_art = user_art_table.get(uid).get(q);
			if(potential_artset.contains(cur_art)){
				potential_artset.remove(cur_art);
			}
		}
		return potential_artset;
	}
	
	public static Comparator<rec_art> aComparator = new Comparator<rec_art>() {//use as minHeap
		public int compare(rec_art c1, rec_art c2) {
			return (int) Float.compare(c1.all_sim, c2.all_sim);
		}
	};
	public static class rec_art {
		int art_id;
		float all_sim;
		rec_art(int id, float sim)
		{
			art_id = id;
			all_sim = sim;
		}
	}
	/*get top n art set as final recommendation for user i*/
	static HashSet<Integer> get_topn_artset(int uid, HashSet<Integer> potential_artset) {
		HashSet<Integer> topn_artset = new HashSet<Integer>();
		PriorityQueue<rec_art> min_heap = new PriorityQueue<rec_art>(rec_num, aComparator);

		//traverse potential_artset
		Iterator<Integer> ir = potential_artset.iterator(); 
		while(ir.hasNext()) {
			int cur_art = ir.next();
			float all_sim = 0.0f;//cur_art's total similarity
			//search in art_user_table, W = Wuv1 + Wuv2 + ...
			for (int i = 0; i < art_user_table.get(cur_art).size(); i++) {
				int cur_user = art_user_table.get(cur_art).get(i);
				if(cur_user != uid && topkset.contains(cur_user)){
					all_sim += sim[uid][cur_user];
				}
			}
			//update in min heap
			rec_art ra = new rec_art(cur_art, all_sim);
			
			if (min_heap.size() < rec_num) {
				min_heap.add(ra);
			} else if (ra.all_sim > min_heap.peek().all_sim){/* meet capacity and > top*/
				min_heap.poll();
				min_heap.add(ra);
			}
		}
		/*put into topn_artset and return*/
		while (!min_heap.isEmpty()) {
			rec_art head = min_heap.poll();
			topn_artset.add(head.art_id);
			//System.out.println(head.art_id + ":" + head.all_sim + " ");
		}
		return topn_artset;
	}
	
	/*main function of user cf recommendation*/
	public static void usercf_recommend()  throws IOException {
		writer = new FileWriter(rec_res_file);
        bw = new BufferedWriter(writer);
        
		for (int i = 2; i <= user_num; i++) {
			if(user_art_table.get(i).size() == 0)
				continue;
			int uid = i;
			/*get potential art list: user's knn's art list, and remove user's own art list*/
			HashSet<Integer> potential_artset = get_potential_artset(uid);
			//show_artset(uid, potential_artset);
			HashSet<Integer> topn_artset = get_topn_artset(uid, potential_artset);
			write_rec_artset(uid, topn_artset);
		}
		bw.close();
        writer.close();
	}
	
	
	public static void main(String[] args) throws IOException {
		create_art_user_table();
		create_cooccur_matrix();
		create_sim_matrix();
		System.out.println("Recommendation start...");
		//show_sim_matrix();
		//show_user_topk();
		usercf_recommend();
		System.out.println("Recommendation completed.");
		measure me = new measure();
		me.call_measure(rec_res_file);
	}

	
	//==================================
	/*write to the rec res file*/
	public static void write_rec_artset(int uid, HashSet<Integer> topn_artset) throws IOException {
		if(topn_artset.size() == 0)
			return;
		bw.write(uid + ": ");
		int i = 0;
		Iterator<Integer> ir = topn_artset.iterator(); 
		while(ir.hasNext()) {
			if(i == topn_artset.size() - 1)
				bw.write(ir.next() + "");
			else
				bw.write(ir.next() + ", ");
			i++;
		}
		bw.write("\n");
	}
	
	public static void show_topkset_sim(int uid, HashSet<Integer> potential_artset) {
		System.out.print(uid + ": ");
		Iterator<Integer> ir=potential_artset.iterator(); 
		while(ir.hasNext()) {
			System.out.print(sim[uid][ir.next()] + ",");
		}
		System.out.println("");
	}
	
	public static void show_artset(int uid, HashSet<Integer> potential_artset) {
		System.out.print(uid + ": ");
		Iterator<Integer> ir=potential_artset.iterator(); 
		while(ir.hasNext()) { 
			System.out.print(ir.next() + " ");
		}
		System.out.println("");
	}
	public static void show_user_topk() {
		for (int i = 2; i <= user_num; i++) {
			System.out.print(i + ": ");
			HashSet<Integer> topkset = get_knn(i);
			Iterator<Integer> ir=topkset.iterator(); 
			while(ir.hasNext()) { 
				System.out.print(ir.next() + " ");
			}
			System.out.println("");
		}
	}
	public static void show_sim_matrix() {
		for (int i = 2; i <= user_num; i++) {
			for (int j = 2; j <= user_num; j++) {
				System.out.print(sim[i][j] + " ");
			}
			System.out.println("");
		}
	}
	
	public static void show_cooccur_matrix() {
		for (int i = 2; i <= user_num; i++) {
			for (int j = 2; j <= user_num; j++) {
				System.out.print(cooccur[i][j] + " ");
			}
			System.out.println("");
		}
	}

	public static void show_art_user_table() {
		Iterator<Integer> it = art_user_table.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer) it.next();
			System.out.print(key + ": ");
			for (int i = 0; i < art_user_table.get(key).size(); i++) {
				System.out.print(art_user_table.get(key).get(i) + ", ");
			}
			System.out.println("");
		}
	}


}
