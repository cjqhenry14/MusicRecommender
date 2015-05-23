package hybrid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class HybridRec {
	public final String social_res = "results_analysis/social_usercf_rec_res.dat";
	public final String tag_res = "results_analysis/tag_itemcf_rec_res.dat";
	public final String hybrid_res = "results_analysis/hybrid_res.dat";
	public float social_w;
	public float tag_w;
	public float bayes_w;
	public int rec_num = 5;
	public static FileWriter writer;
	public static BufferedWriter bw;
	//init
	HybridRec (float _social_w, float _tag_w, float _bayes_w)  throws IOException {
		//System.out.println("Hybrid Recommendation Start.");
		this.social_w = _social_w;
		this.tag_w = _tag_w;
		this.bayes_w = _bayes_w;
		writer = new FileWriter(hybrid_res);
        bw = new BufferedWriter(writer);
	}
	public static class ValueComparator implements Comparator<Map.Entry<Integer, Float>> {  
        public int compare(Map.Entry<Integer, Float> m,Map.Entry<Integer, Float> n) {
            return (int) Float.compare(n.getValue(), m.getValue());
        }  
    }  
	public void start_recommend() throws IOException {
		FileReader social_reader = new FileReader(social_res);
		BufferedReader so_br = new BufferedReader(social_reader);
		FileReader tag_reader = new FileReader(tag_res);
		BufferedReader tag_br = new BufferedReader(tag_reader);
		//read each res file
		String so_line = null;
		String tag_line = null;
		//String bayes_line = null;
		int t = 0;
		while (t < 1877) {//total = 1877
			t++;
			tag_line = tag_br.readLine();
			so_line = so_br.readLine();
			//System.out.println(tag_line + so_line);
			Map<Integer, Float> hmap = new HashMap<Integer, Float>();//aid, rank
			String[] so_values = so_line.split(":");
			String[] tag_values = tag_line.split(":");
			int uid = Integer.parseInt(so_values[0]);
			bw.write(uid + ": ");
			
			String[] so_artid_arr = so_values[1].split(",");
			String[] tag_artid_arr = tag_values[1].split(",");
			//go through so_artid_arr
			for (int i = 0; i < so_artid_arr.length; i++) {
				int art_id = Integer.parseInt(so_artid_arr[i].trim());
				if (!hmap.containsKey(art_id))
					hmap.put(art_id, social_w * (i+1));
				else 
					hmap.put(art_id, hmap.get(art_id).floatValue() + social_w * (i+1));
			}
			//go through tag_artid_arr
			for (int i = 0; i < tag_artid_arr.length; i++) {
				int art_id = Integer.parseInt(tag_artid_arr[i].trim());
				if (!hmap.containsKey(art_id))
					hmap.put(art_id, tag_w * (i+1));
				else 
					hmap.put(art_id, hmap.get(art_id).floatValue() + tag_w * (i+1));
			}
			//sort hashmap
			List<Map.Entry<Integer,Float>> list=new ArrayList<Entry<Integer, Float>>();  
	        list.addAll(hmap.entrySet());  
	        ValueComparator vc=new ValueComparator();  
	        Collections.sort(list, vc);
	        Iterator<Map.Entry<Integer, Float>> it = list.iterator();
	        //write res
	        for(int j = 0; j< rec_num; j++)  {  
	        	if(j == rec_num - 1)
					bw.write(it.next().getKey() + "");
				else
					bw.write(it.next().getKey() + ", ");
	        }
	        bw.write("\n");
		}// end while
		
		bw.close();
        writer.close();
        
		so_br.close();
		social_reader.close();
		tag_br.close();
		tag_reader.close();
	}
	

}
