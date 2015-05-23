package SocialUserCf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class UserFriend extends Base {
	/* friend similarity(cosine similarity) matrix */
	public float friend_sim[][];

	public ArrayList<HashSet<Integer>> user_friend_table;

	UserFriend(float alpha, int k, int rec_num) throws IOException {
		super(alpha, k, rec_num);
		friend_sim = new float[user_num + 1][user_num + 1];

		user_friend_table = new ArrayList<HashSet<Integer>>();
		for (int i = 0; i <= user_num; i++) {
			user_friend_table.add(new HashSet<Integer>());
			user_friend_table.get(i).add(i);
		}

		create_user_friend_table();
		create_friend_sim_table();
	}

	/* art_user_table() -- artist_i: user1, user2,... */
	public void create_user_friend_table() throws IOException {
		FileReader reader = new FileReader(userfriend_file);
		BufferedReader br = new BufferedReader(reader);

		String line = null;
		/* read user_friend file() */
		while ((line = br.readLine()) != null) {
			String[] values = line.split("\t");
			int uid = Integer.parseInt(values[0]);
			int frid = Integer.parseInt(values[1]);
			// System.out.println(uid + ", " + frid);
			user_friend_table.get(uid).add(frid);
		}// end while
		br.close();
		reader.close();
	}

	public void create_friend_sim_table() {
		for(int i = 2; i <= user_num; i++){
			//show_set(user_friend_table.get(i));
			for(int j = 2; j <= user_num; j++){
				if(j == i)
					continue;
				int common_fri = 0;//common friends i and j
				//calculate common friends i and j
				Iterator<Integer> ir = user_friend_table.get(j).iterator(); 
				while(ir.hasNext()) {
					if( user_friend_table.get(i).contains(ir.next()) )
						common_fri++;
				}
				
				double denominator = Math.sqrt(user_friend_table.get(i).size() *user_friend_table.get(j).size());
				denominator = (double)(Math.round(denominator*100)/100.0);
				double similarity = common_fri / denominator;
				similarity = (double)(Math.round(similarity*100)/100.0);
				friend_sim[i][j] = (float)similarity;
			}//end for j
		}//end for i
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
	
}
