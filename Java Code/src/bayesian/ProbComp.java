package bayesian;

import java.util.Comparator;

public class ProbComp implements Comparator<ArtistProb> {

	@Override
	public int compare(ArtistProb a1, ArtistProb a2) {
		// sort in descending order base on prob
		int primary = Double.valueOf(a2.prob1).compareTo(
				Double.valueOf(a1.prob1));
		if (primary != 0)
			return primary;
		int secondary = Double.valueOf(a2.prob2).compareTo(
				Double.valueOf(a1.prob2));
		if (secondary != 0)
			return secondary;
		return a1.artist - a2.artist;
	}
}