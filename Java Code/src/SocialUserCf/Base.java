package SocialUserCf;

public class Base {
	/* sim = alpha*friend_sim + (1-alpha)*interest_sim */
	public float alpha;
	public int k;
	public int rec_num;
	public int user_num;
	
	//public final String userart_file = "dataset/test.txt";
	//public final String userfriend_file = "dataset/friend_test.txt";
	public final String userart_file = "dataset/user_artists_train.dat";
	public final String userfriend_file = "dataset/user_friends.dat";
	public final String social_usercf_rec_res_file = "results_analysis/social_usercf_rec_res.dat";
	
	public Base (float alpha, int k, int rec_num){
		this.alpha = alpha;
		this.k = k;
		this.rec_num = rec_num;
		this.user_num = 2100;
	}
}
