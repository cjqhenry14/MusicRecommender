package SocialUserCf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class UserArt extends Base{
	/* artist_i: user1, user2,... */
	public HashMap<Integer, ArrayList<Integer>> art_user_table;
	/* user: art_id1, art_id2,... */
	public ArrayList<ArrayList<Integer>> user_art_table;
	/* user_id, user's artist list size, use for calculating similarity */
	public int[] user_art_size;
	/* co-occurrence matrix */
	public int cooccur[][];
	/* interest similarity(cosine similarity) matrix */
	public float interest_sim[][];

	UserArt(float alpha, int k, int rec_num) throws IOException {
		super(alpha, k, rec_num);
		
		art_user_table = new HashMap<Integer, ArrayList<Integer>>();
		user_art_size = new int[user_num + 1];
		cooccur = new int[user_num + 1][user_num + 1];
		interest_sim = new float[user_num + 1][user_num + 1];

		user_art_table = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i <= user_num; i++)
			user_art_table.add(new ArrayList<Integer>());
		
		//topkset = new HashSet<Integer>();????
		/*start create*/
		create_art_user_table();
		create_cooccur_matrix();
		create_interest_sim_matrix();
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
	public void create_cooccur_matrix() {
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
	
	/* create interest similarity(cosine similarity) matrix, interest_sim[][] */
	public void create_interest_sim_matrix() {
		for (int i = 2; i <= user_num; i++) {
			for (int j = 2; j <= user_num; j++) {
				double denominator = Math.sqrt(user_art_size[i] * user_art_size[j]);
				denominator = (double)(Math.round(denominator*100)/100.0);
				double similarity = cooccur[i][j] / denominator;
				similarity = (double)(Math.round(similarity*100)/100.0);
				interest_sim[i][j] = (float)similarity;
			}
		}
	}
	
	
	//========== show =============
	public void show_matrix(float [][] sim) {
		for (int i = 2; i <= user_num; i++) {
			for (int j = 2; j <= user_num; j++) {
				System.out.print(sim[i][j] + " ");
			}
			System.out.println("");
		}
	}
	
	public void show_art_user_table() {
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
