package SocialUserCf;

import java.io.IOException;

import validation.measure;

public class CallSocialUserCf{
	public static void main(String[] args) throws IOException {
		measure me = new measure();
		//for(float i = 0.2f; i<= 1.0f; i = i + 0.9f){
		for(int i = 5; i<= 20; i = i + 5){
			float alpha = 0.2f;
			int k = i;
			//System.out.println("Social User CF (alpha = " + alpha + ") start...");
			System.out.println("Social User CF (k = " + k + ") start...");
			SocialUsercf su = new SocialUsercf(alpha, k, 10);//Base (float alpha, int k, int rec_num)
			su.start_recommend();
			
			me.call_measure("results_analysis/social_usercf_rec_res.dat");
		}
		
		System.out.println("Social User CF completed.");
	}

}
