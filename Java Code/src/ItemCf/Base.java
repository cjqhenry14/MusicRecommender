package ItemCf;

public class Base {
	public int k;
	public int rec_num;
	public int user_num;
	public int art_num;
	public String itemcf_rec_res_file;
	//public final String userart_file = "dataset/test.txt";
	public final String userart_file = "dataset/user_artists_train.dat";
	//public final String itemcf_rec_res_file = "results_analysis/itemcf_rec_res.dat";
	
	public Base (int k, int rec_num, String itemcf_rec_res_file){
		this.k = k;
		this.rec_num = rec_num;
		this.user_num = 2100;//2100   5
		this.art_num = 18746;//18746   60
		this.itemcf_rec_res_file = itemcf_rec_res_file;
	}
}
