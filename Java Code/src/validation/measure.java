package validation;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


public class measure {
	public static int user_num = 2100;
	public static ArrayList<HashSet<Integer>> test_table;
	public static ArrayList<HashSet<Integer>> comp_table;
	
	public static HashSet<Integer> art_union;
	
	public static final String test_file = "dataset/user_artists_test.dat";
	//public static final String usercf_res_file = "results_analysis/usercf_rec_res.dat";
	//public static final String social_usercf_res_file = "results_analysis/social_usercf_rec_res.dat";
	
	static void init_compare_files(String test_file, String comp_file) throws IOException {
		
		test_table = new ArrayList<HashSet<Integer>>();
		comp_table= new ArrayList<HashSet<Integer>>();
		
		art_union = new HashSet<Integer>();
		for(int i = 0;i <= user_num;i++)//some id's HashSet's size = 0
		{
			test_table.add(new HashSet<Integer>());
			comp_table.add(new HashSet<Integer>());
		}

		FileReader test_reader = new FileReader(test_file);
		BufferedReader test_br = new BufferedReader(test_reader);
		FileReader comp_reader = new FileReader(comp_file);
		BufferedReader comp_br = new BufferedReader(comp_reader);
		
		/*read test file*/
		String line = null;
		while ((line = test_br.readLine()) != null) {
			String[] values = line.split(":");
			int uid = Integer.parseInt(values[0]);
			String[] art_id_array = values[1].split(",");

			for (int i = 0; i < art_id_array.length; i++) {
				int art_id = Integer.parseInt(art_id_array[i].trim());
				test_table.get(uid).add(art_id);
			}
		}// end while
		
		/*read compared file*/
		while ((line = comp_br.readLine()) != null) {
			String[] values = line.split(":");
			int uid = Integer.parseInt(values[0]);
			String[] art_id_array = values[1].split(",");

			for (int i = 0; i < art_id_array.length; i++) {
				int art_id = Integer.parseInt(art_id_array[i].trim());
				comp_table.get(uid).add(art_id);
				if(!art_union.contains(art_id))
					art_union.add(art_id);
			}
		}// end while
		
		comp_br.close();
		comp_reader.close();
		
		test_br.close();
		test_reader.close();
	}
	
	public static void start_measure()
	{
		float all_precision = 0.0f;
		float all_recall = 0.0f;
		
		int same_num = 0;
		int total_rec_num = 0;//for precision
		int total_test_num = 0;//for recall
		for (int i = 0; i <= user_num; i++) {

			total_rec_num += comp_table.get(i).size();
			total_test_num += test_table.get(i).size();
			int tmp_num = 0;
			/*calculate each pair's precision and recall*/
			Iterator<Integer> ir = comp_table.get(i).iterator();	
			while(ir.hasNext()) {
				if(test_table.get(i).contains(ir.next())){
					same_num++;
				    tmp_num++;
				}
			}
			//float precision = (float)tmp_num/comp_table.get(i).size();
			//System.out.println(i + ": " + tmp_num + ", " + precision);
		}
		all_precision = (float)same_num/total_rec_num;
		all_recall = (float)same_num/total_test_num;
		System.out.println("Precision: " + all_precision);
		System.out.println("Recall: " + all_recall);
		System.out.println("Coverage: " + (float)art_union.size()/15373);
	}
	
	public void call_measure(String rec_res) throws IOException {
		init_compare_files(test_file, rec_res);
		start_measure();
	}
	public static void main(String[] args) throws IOException {
		init_compare_files(test_file, "results_analysis\\bayesian_res.dat");
		//init_compare_files(test_file, "results_analysis\\social_usercf_rec_res.dat");
		start_measure();
	}

}
