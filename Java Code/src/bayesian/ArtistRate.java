package bayesian;

import java.util.Comparator;

public class ArtistRate {
	public int artist;
	public int rate;

	public ArtistRate() {

	}

	public ArtistRate(int a, int r) {
		artist = a;
		rate = r;
	}

	@Override
	public String toString() {
		return String.format("(" + artist + "," + rate + ")\n");
	}
}

class RateComp implements Comparator<ArtistRate> {

	@Override
	public int compare(ArtistRate r1, ArtistRate r2) {
		int primary = r1.rate - r2.rate;
		if (primary != 0)
			return primary;
		return r1.artist - r2.artist;
	}
}
