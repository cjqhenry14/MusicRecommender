package bayesian;

import java.util.Comparator;

public class ArtistUserPair {
	public int artist;
	public int user;

	public ArtistUserPair() {
		artist = 0;
		user = 0;
	}

	public ArtistUserPair(int artid, int uid) {
		artist = artid;
		user = uid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + artist;
		result = prime * result + user;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ArtistUserPair) {
			ArtistUserPair aup = (ArtistUserPair) obj;
			return (aup.artist == this.artist && aup.user == this.user);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return String.format("(" + artist + "," + user + ")\n");
	}
}

class KeyComp implements Comparator<ArtistUserPair> {

	@Override
	public int compare(ArtistUserPair k1, ArtistUserPair k2) {
		int primary = k1.artist - k2.artist;
		if (primary != 0)
			return primary;
		return k1.user - k2.user;
	}
}
