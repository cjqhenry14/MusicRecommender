package TagItemCf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import ItemCf.Base;

public class TagItemCf extends Base{
	
	public float tag_sim_table[][];
	
	public TagItemSim tagitemsim;
	/* user's interest to artist */
	public float interest_table[][];
	
	public static FileWriter writer;
	public static BufferedWriter bw;
	
	public TagItemCf(float alpha, int k, int rec_num, String tag_itemcf_rec_res_file) throws IOException {
		super(k, rec_num, tag_itemcf_rec_res_file);
		tagitemsim = new TagItemSim(alpha, k, rec_num, tag_itemcf_rec_res_file);
		
		interest_table = new float[user_num + 1][art_num + 1];
	}
	
	/* traverse users，for cur_user, traverse User-Art table,
	 * for cur_art, get cur_art's knn arts from knn-table
	 * p[cur_user][cur_art] += user_sim[cur_art][cur_art’s knn_i]*/
	public void start_recommend() throws IOException {
		writer = new FileWriter(itemcf_rec_res_file);
        bw = new BufferedWriter(writer);
        
		for(int i = 2; i<= user_num; i++){
			if(tagitemsim.user_art_table.get(i).size() == 0)
				continue;
			PriorityQueue<rec_art> min_heap = new PriorityQueue<rec_art>(rec_num, aComparator);
			
			int cur_user = i;
			Iterator<Integer> art_ir = tagitemsim.user_art_table.get(cur_user).iterator();
			while (art_ir.hasNext()) {
				int cur_art = art_ir.next();
				Iterator<Integer> knn_ir = tagitemsim.art_knn_table.get(cur_art).iterator();
				while (knn_ir.hasNext()) {
					int cur_neighbour = knn_ir.next();
					if(tagitemsim.user_art_table.get(cur_user).contains(cur_neighbour))
						continue;
					interest_table[cur_user][cur_neighbour] += tagitemsim.user_item_sim[cur_art][cur_neighbour];
				}
			}
			/*get cur_user's top n recommend arts*/
			for(int j = 1; j<= art_num; j++){
				if(tagitemsim.art_user_table.get(j).size() == 0)
					continue;
				rec_art ra = new rec_art(j, interest_table[cur_user][j]);
				if (min_heap.size() < rec_num) {
					min_heap.add(ra);
				} else if (ra.art_interest > min_heap.peek().art_interest){/* meet capacity and > top*/
					min_heap.poll();
					min_heap.add(ra);
				}
			}//end update min heap
			write_rec_artset(cur_user, min_heap);
		}//end for i
		
		
		bw.close();
        writer.close();
	}
	
	public Comparator<rec_art> aComparator = new Comparator<rec_art>() {//use as minHeap
		public int compare(rec_art c1, rec_art c2) {
			return (int) Float.compare(c1.art_interest, c2.art_interest);
		}
	};
	public class rec_art{
		int art_id;
		float art_interest;
		rec_art(int id, float interest){
			art_id = id;
			art_interest = interest;
		}
	}
	
	/*write to the rec res file*/
	public void write_rec_artset(int uid, PriorityQueue<rec_art> min_heap) throws IOException {
		if(min_heap.size() == 0)
			return;
		bw.write(uid + ": ");
		int i = 1;
		while (!min_heap.isEmpty()) {
			rec_art head = min_heap.poll();
			if(i == rec_num)
				bw.write(head.art_id + " ");
			else
				bw.write(head.art_id + ", ");
			i++;
		}
		bw.write("\n");
	}

}
