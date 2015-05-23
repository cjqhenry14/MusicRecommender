package SocialUserCf;
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

public class SocialUsercf extends Base{
	/* friend & interest similarity matrix */
	public float mix_sim[][];
	public HashSet<Integer> topkset;
	UserArt uArt;
	UserFriend uFri;
	
	public static FileWriter writer;
	public static BufferedWriter bw;
	
	
	/*combine friend & interest similarity matrix*/
	public void combine_sim(){
		for (int i = 2; i <= user_num; i++) {
			for (int j = 2; j <= user_num; j++) {
				mix_sim[i][j] = alpha*uFri.friend_sim[i][j] + (1-alpha)*uArt.interest_sim[i][j];
			}
		}
	}
	/* ===============================get knn =========================================== */
	public Comparator<neighbour> nComparator = new Comparator<neighbour>() {//use as minHeap
		public int compare(neighbour c1, neighbour c2) {
			return (int) Float.compare(c1.similarity, c2.similarity);
		}
	};
	public class neighbour {
		int uid;
		float similarity;
		neighbour(int id, float sim)
		{
			uid = id;
			similarity = sim;
		}
	}
	/* get KNN list*/
	public HashSet<Integer> get_knn(int uid) {
		HashSet<Integer> topkset2 = new HashSet<Integer>();
		PriorityQueue<neighbour> min_heap = new PriorityQueue<neighbour>(k, nComparator);
		
		for (int i = 0; i <= user_num; i++) {

			neighbour nb = new neighbour(i, mix_sim[uid][i]);
			
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
	public HashSet<Integer> get_potential_artset(int uid) {
		
		topkset = get_knn(uid);
		//show_topkset_sim(uid, topkset);
		HashSet<Integer> potential_artset = new HashSet<Integer>();
		Iterator<Integer> ir = topkset.iterator(); 
		while(ir.hasNext()) {
			int cur_neighbour = ir.next();
			for (int k = 0; k < uArt.user_art_table.get(cur_neighbour).size(); k++) {//get neighbour's art list
				int cur_art = uArt.user_art_table.get(cur_neighbour).get(k);
				if(!potential_artset.contains(cur_art)){
					potential_artset.add(cur_art);
				}
			}
		}//end while
		/*remove user's own art list*/
		for (int q = 0; q < uArt.user_art_table.get(uid).size(); q++) {
			int cur_art = uArt.user_art_table.get(uid).get(q);
			if(potential_artset.contains(cur_art)){
				potential_artset.remove(cur_art);
			}
		}
		return potential_artset;
	}
	
	public Comparator<rec_art> aComparator = new Comparator<rec_art>() {//use as minHeap
		public int compare(rec_art c1, rec_art c2) {
			return (int) Float.compare(c1.all_sim, c2.all_sim);
		}
	};
	public class rec_art {
		int art_id;
		float all_sim;
		rec_art(int id, float sim)
		{
			art_id = id;
			all_sim = sim;
		}
	}
	/*get top n art set as final recommendation for user i*/
	public HashSet<Integer> get_topn_artset(int uid, HashSet<Integer> potential_artset) {
		HashSet<Integer> topn_artset = new HashSet<Integer>();
		PriorityQueue<rec_art> min_heap = new PriorityQueue<rec_art>(rec_num, aComparator);

		//traverse potential_artset
		Iterator<Integer> ir = potential_artset.iterator(); 
		while(ir.hasNext()) {
			int cur_art = ir.next();
			float all_sim = 0.0f;//cur_art's total similarity
			//search in art_user_table, W = Wuv1 + Wuv2 + ...
			for (int i = 0; i < uArt.art_user_table.get(cur_art).size(); i++) {
				int cur_user = uArt.art_user_table.get(cur_art).get(i);
				if(cur_user != uid && topkset.contains(cur_user)){
					all_sim += mix_sim[uid][cur_user];
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
		}
		return topn_artset;
	}
	
	/*main function of social user cf recommendation*/
	public void start_recommend() throws IOException {
		writer = new FileWriter(social_usercf_rec_res_file);
        bw = new BufferedWriter(writer);
        
		for (int i = 2; i <= user_num; i++) {
			if(uArt.user_art_table.get(i).size() == 0)
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
	
	
	public SocialUsercf(float alpha, int k, int rec_num) throws IOException {
		super(alpha, k, rec_num);//init base
		//System.out.println("Social User CF Recommending...");
		topkset = new HashSet<Integer>();
		mix_sim = new float[user_num + 1][user_num + 1];
		
		uArt = new UserArt(alpha, k, rec_num);
		uFri = new UserFriend(alpha, k, rec_num);
		
		combine_sim();
		//uFri.show_matrix(mix_sim);
	}
	
	
	/*write to the rec res file*/
	public void write_rec_artset(int uid, HashSet<Integer> topn_artset) throws IOException {
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

}
