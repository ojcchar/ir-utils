package seers.utils.pos;

import java.util.ArrayList;
import java.util.List;

public class POSSentence {

	public List<POSToken> tokens = new ArrayList<>();

	@Override
	public String toString() {
		return "POSSentence [tokens=" + tokens + "]";
	}

}
