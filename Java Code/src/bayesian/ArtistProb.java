package bayesian;

import java.util.Comparator;

public class ArtistProb {
	public int artist;
	public double prob1;
	public double prob2;

	public ArtistProb() {

	}

	public ArtistProb(int a, double p1, double p2) {
		artist = a;
		prob1 = p1;
		prob2 = p2;
	}

	@Override
	public String toString() {
		return String.format("(" + artist + "," + prob1 + "," + prob2 + ")");
	}
}
