import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class divide_train_test {
	public static final String data_name = "dataset/user_artists.dat";
	public static final String train_file = "dataset/user_artists_train.dat";
	public static final String test_file = "dataset/user_artists_test.dat";
	/* 80% train set, 20% test set */
	public static float train_per = 0.8f;
	public static float test_per = 0.2f;
	public static int user_num = 2100;
	/* user: art_id1, art_id2,... */
	public static ArrayList<ArrayList<Integer>> user_art_table;

	public static void user_art_randomize() {
		Random rand = new Random(2);
		/* randomize each list */
		for (int i = 0; i < user_art_table.size(); i++) {
			for (int j = user_art_table.get(i).size() - 1; j >= 1; --j) {
				// swap
				int tmp = user_art_table.get(i).get(j);
				int ran_idx = Math.abs(rand.nextInt() % j);
				user_art_table.get(i).set(j, user_art_table.get(i).get(ran_idx));
				user_art_table.get(i).set(ran_idx, tmp);
			}
		}
	}

	static void read_user_art_file() throws IOException {
		user_art_table = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i <= user_num; i++)
			user_art_table.add(new ArrayList<Integer>());

		FileReader reader = new FileReader(data_name);
		BufferedReader br = new BufferedReader(reader);

		String line = null;
		//int i = 0;
		while ((line = br.readLine()) != null) {
			String[] values = line.split("	");
			int uid = Integer.parseInt(values[0]);
			int aid = Integer.parseInt(values[1]);
			user_art_table.get(uid).add(aid);// uid: aid1, aid2, aid3
			//i++;
		}

		br.close();
		reader.close();
	}

	public static void divide() throws IOException {
		FileWriter train_writer = new FileWriter(train_file);
		BufferedWriter train_bw = new BufferedWriter(train_writer);
		FileWriter test_writer = new FileWriter(test_file);
		BufferedWriter test_bw = new BufferedWriter(test_writer);
        
		for (int i = 1; i < user_art_table.size(); i++) {
			int test_size = (int)(user_art_table.get(i).size() * test_per);
			if(test_size == 0)
				continue;
			
			train_bw.write(i + ": ");
			test_bw.write(i + ": ");
			
			for (int j = 0; j < user_art_table.get(i).size(); j++)
			{
				if(j < test_size)//test set
				{
					if(j == test_size - 1)
						test_bw.write(user_art_table.get(i).get(j) + "");
					else
						test_bw.write(user_art_table.get(i).get(j) + ", ");
				}
				else//train set
				{
					if(j == user_art_table.get(i).size() - 1)
						train_bw.write(user_art_table.get(i).get(j) + "");
					else
						train_bw.write(user_art_table.get(i).get(j) + ", ");
				}
			}
			train_bw.write("\n");
			test_bw.write("\n");
		}
		
		train_bw.close();
		train_writer.close();
		test_bw.close();
		test_writer.close();
		System.out.println("dividsion ok.");
	}

	public static void main(String[] args) throws IOException {
		read_user_art_file();
		user_art_randomize();
		// show_user_art_table_test();
		this.divide();
		System.out.print("hahahah");
	}

	public static void show_user_art_table_test() {
		for (int i = 0; i < 5; i++) {
			System.out.print(i + ":");
			for (int j = 0; j < user_art_table.get(i).size(); j++) {
				System.out.print(user_art_table.get(i).get(j) + ", ");
			}
			System.out.print("\n");
		}
	}
}
