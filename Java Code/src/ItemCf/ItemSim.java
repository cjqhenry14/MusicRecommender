package ItemCf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

public class ItemSim extends Base {
	/* artist_i: user1, user2,... */
	public ArrayList<HashSet<Integer>> art_user_table;
	/* user: art_id1, art_id2,... */
	public ArrayList<HashSet<Integer>> user_art_table;
	/* user: art_id1, art_id2,... */
	public ArrayList<HashSet<Integer>> art_knn_table;
	/* user-based item similarity(cosine similarity) matrix */
	public float user_item_sim[][];

	public ItemSim(int k, int rec_num, String itemcf_rec_res_file) throws IOException {
		super(k, rec_num, itemcf_rec_res_file);// init base
		art_user_table = new ArrayList<HashSet<Integer>>();
		user_art_table = new ArrayList<HashSet<Integer>>();
		art_knn_table = new ArrayList<HashSet<Integer>>();

		for (int i = 0; i <= user_num; i++)
			user_art_table.add(new HashSet<Integer>());
		for (int i = 0; i <= art_num; i++){
			art_user_table.add(new HashSet<Integer>());
			art_knn_table.add(new HashSet<Integer>());
		}

		user_item_sim = new float[art_num + 1][art_num + 1];

		/* start create */
		create_art_user_table();
		//System.out.println("create_art_user_table ok");
		//show_table(art_user_table);
		create_user_item_sim_matrix();
		//System.out.println("create_user_item_sim_matrix ok");
		//show_matrix(user_item_sim);
		get_art_knn();
		//System.out.println("get_art_knn ok");
		//show_table(art_knn_table);
	}

	/* art_user_table() -- artist_i: user1, user2,... */
	public void create_art_user_table() throws IOException {

		FileReader reader = new FileReader(userart_file);
		BufferedReader br = new BufferedReader(reader);

		String line = null;
		/* read_train_file() */
		while ((line = br.readLine()) != null) {
			String[] values = line.split(":");
			int uid = Integer.parseInt(values[0]);
			String[] art_id_array = values[1].split(",");

			for (int i = 0; i < art_id_array.length; i++) {
				int art_id = Integer.parseInt(art_id_array[i].trim());

				art_user_table.get(art_id).add(uid);
				user_art_table.get(uid).add(art_id);
			}
		}// end while
		br.close();
		reader.close();
	}
	/* ======================== create_user_item_sim_matrix and get knn ============================= */
	public Comparator<item_neighbour> nComparator = new Comparator<item_neighbour>() {//use as minHeap
		public int compare(item_neighbour c1, item_neighbour c2) {
			return (int) Float.compare(c1.similarity, c2.similarity);
		}
	};
	public class item_neighbour {
		int art_id;
		float similarity;
		item_neighbour(int id, float sim)
		{
			art_id = id;
			similarity = sim;
		}
	}
	/* create_user_item_sim_matrix */
	public void create_user_item_sim_matrix() {
		for (int i = 1; i <= art_num; i++) {
			if (art_user_table.get(i).size() == 0)
				continue;
			
			
			for (int j = i + 1; j <= art_num; j++) {
				if (art_user_table.get(j).size() == 0)
					continue;
				
				int common_user = 0;
				Iterator<Integer> ir = art_user_table.get(j).iterator();
				while (ir.hasNext()) {
					if (art_user_table.get(i).contains(ir.next()))
						common_user++;
				}
				double denominator = Math.sqrt(art_user_table.get(i).size() * art_user_table.get(j).size());
				denominator = (double)(Math.round(denominator*100)/100.0);
				double similarity = common_user / denominator;
				similarity = (double)(Math.round(similarity*100)/100.0);
				
				user_item_sim[i][j] = (float)similarity;
				user_item_sim[j][i] = user_item_sim[i][j];
				
			}// end for j
		}// end for i
	}

	public void get_art_knn(){
		PriorityQueue<item_neighbour> min_heap = new PriorityQueue<item_neighbour>(k, nComparator);

		for (int i = 1; i <= art_num; i++) {
			if (art_user_table.get(i).size() == 0)
				continue;
			for (int j = 1; j <= art_num; j++) {
				if(i == j || art_user_table.get(j).size() == 0)
					continue;
				/*update in art_i's min heap*/
				item_neighbour nb = new item_neighbour(j, user_item_sim[i][j]);
				if (min_heap.size() < k) {
					min_heap.add(nb);
				} else if (nb.similarity > min_heap.peek().similarity){/* meet capacity and > top*/
					min_heap.poll();
					min_heap.add(nb);
				}
			}//end j
			while (!min_heap.isEmpty()) {
				art_knn_table.get(i).add(min_heap.poll().art_id);
			}
		}//end i
	}
	// ========== show =============

	public void show_matrix(float[][] sim) {
		for (int i = 1; i <= art_num; i++) {
			if (art_user_table.get(i).size() == 0)
				continue;
			for (int j = 1; j <= art_num; j++) {
				if (art_user_table.get(j).size() == 0)
					continue;
				
				System.out.print(sim[i][j] + " ");
			}
			System.out.println("");
		}
	}

	public void show_table(ArrayList<HashSet<Integer>> table) {
		for (int i = 1; i <= art_num; i++) {
			if (table.get(i).size() == 0)
				continue;
			System.out.print(i + ": ");
			Iterator<Integer> ir = table.get(i).iterator();
			while (ir.hasNext()) {
				System.out.print(ir.next() + " ");
			}
			System.out.println("");
		}
	}
}
