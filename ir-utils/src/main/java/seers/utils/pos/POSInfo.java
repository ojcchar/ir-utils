package seers.utils.pos;

import java.util.ArrayList;
import java.util.List;

public class POSInfo {

	public List<POSSentence> sentences = new ArrayList<>();

	public void addPosInfo(POSInfo posInfo) {
		sentences.addAll(posInfo.sentences);
	}

	@Override
	public String toString() {
		return "POSInfo [sentences=" + sentences + "]";
	}

}
