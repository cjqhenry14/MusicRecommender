package bayesian;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;


public class Measure {
	public static int user_num = 2100;
	public static ArrayList<HashSet<Integer>> test_table;
	public static ArrayList<HashSet<Integer>> comp_table;
	public static HashMap<Integer,ArrayList<Integer>> recommandedUSER;
	public static HashMap<Integer,ArrayList<Integer>> testUSER;
	public static HashSet<Integer> art_union;
	
	public static final String test_file = "dataset/user_artists_test.dat";
	//public static final String usercf_res_file = "results_analysis/usercf_rec_res.dat";
	//public static final String social_usercf_res_file = "results_analysis/social_usercf_rec_res.dat";
	
	static void init_compare_files(String test_file, String comp_file) throws IOException {


		FileReader test_reader = new FileReader(test_file);
		BufferedReader test_br = new BufferedReader(test_reader);
		FileReader comp_reader = new FileReader(comp_file);
		BufferedReader comp_br = new BufferedReader(comp_reader);
		recommandedUSER=new HashMap<Integer,ArrayList<Integer>>();
		testUSER=new HashMap<Integer,ArrayList<Integer>>();
		ArrayList<Integer> artistsList;
		String line = null;
		
		/*read compared file*/
		while ((line = comp_br.readLine()) != null) {
			String[] values = line.split(":");
			int uid = Integer.parseInt(values[0]);
		
			String[] art_id_array = values[1].split(",");
			artistsList=new ArrayList<Integer>();
			for (int i = 0; i < art_id_array.length; i++) {
				
				int art_id = Integer.parseInt(art_id_array[i].trim());
				//System.out.println(art_id);
				artistsList.add(art_id);
				
			}
			recommandedUSER.put(uid,artistsList);
		}// end while
		/*read test file*/
		while ((line = test_br.readLine()) != null) {
			String[] values = line.split(":");
			int uid = Integer.parseInt(values[0]);
			String[] art_id_array = values[1].split(",");
			artistsList=new ArrayList<Integer>();
			for (int i = 0; i < art_id_array.length; i++) {
				int art_id = Integer.parseInt(art_id_array[i].trim());
				artistsList.add(art_id);
			}
			testUSER.put(uid,artistsList);
		}// end while
		
		comp_br.close();
		comp_reader.close();
		
		test_br.close();
		test_reader.close();
	}
	
	public static String start_measure()
	{
		float all_precision = 0.0f;
		float all_recall = 0.0f;
		
		int same_num = 0;
		int total_rec_num = 0;//for precision
		int total_test_num = 0;//for recall
		for (Entry<Integer,  ArrayList<Integer>> entry : recommandedUSER.entrySet()) {
			Integer i = entry.getKey();
			total_rec_num += recommandedUSER.get(i).size();
			
			/*calculate each pair's precision and recall*/
			for (int j=0;j<recommandedUSER.get(i).size();j++)
			
				if(testUSER.get(i).contains(recommandedUSER.get(i).get(j))){
					same_num++;
			}
			//float precision = (float)tmp_num/comp_table.get(i).size();
			//System.out.println(i + ": " + tmp_num + ", " + precision);
		}
		for (Entry<Integer,  ArrayList<Integer>> entry : testUSER.entrySet()) {
			Integer i = entry.getKey();
			total_test_num += testUSER.get(i).size();
		}
		all_precision = (float)same_num/total_rec_num;
		all_recall = (float)same_num/total_test_num;
		System.out.println("Precision: " + all_precision+ " total recommanded "+total_rec_num);
		System.out.println("Recall: " + all_recall+" total test "+total_test_num);
		//System.out.println("Coverage: " + (float)art_union.size()/15373);
		return "Precision: " + all_precision+ " total recommanded "+total_rec_num+"\n"+"Recall: " + all_recall+" total test "+total_test_num+"\n";
	}
	
	public static String call_measure(String rec_res) throws IOException {
		init_compare_files(test_file, rec_res);
		return start_measure();
	}
	public static void main(String[] args) throws IOException {
		init_compare_files(test_file, "results_analysis\\bayesian_res.dat");
		//init_compare_files(test_file, "results_analysis\\social_usercf_rec_res.dat");
		start_measure();
	}

}
