package bayesian;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class UserNode {
	public int id;

	public double[] prior;
	public HashMap<Integer, double[][]> probTable;
	public ArrayList<Integer> artists;
	public ArrayList<Integer> rates;

	public ArrayList<Integer> friends;
	public HashMap<Integer, Integer> estimateRate;

	public UserNode() {
		id = 0;
		artists = new ArrayList<Integer>();
		rates = new ArrayList<Integer>();
		friends = new ArrayList<Integer>();
		prior = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		probTable = new HashMap<Integer, double[][]>();
		estimateRate = new HashMap<Integer, Integer>();
	}

	public UserNode(int userid, ArrayList<Integer> artistList) {
		id = userid;
		artists = artistList;
		friends = new ArrayList<Integer>();
		prior = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		probTable = new HashMap<Integer, double[][]>();
		estimateRate = new HashMap<Integer, Integer>();
	}

	@Override
	public String toString() {
		return String.format("***\n" + id + ":" + prior[0] + "," + prior[1]
				+ "," + prior[2] + "," + prior[3] + "," + prior[4] + " \n"
				+ artists + "\n" + rates + "\n" + friends + "\n*****\n");
	}

}

class UserNodeComp implements Comparator<UserNode> {

	@Override
	public int compare(UserNode un1, UserNode un2) {
		return un1.id - un2.id;
	}
}