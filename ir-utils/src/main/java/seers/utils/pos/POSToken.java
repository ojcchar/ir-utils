package seers.utils.pos;

public class POSToken {
	public String token;
	public String pos;
	public String lemma;

	public POSToken(String token, String pos, String lemma) {
		super();
		this.token = token;
		this.pos = pos;
		this.lemma = lemma;
	}

	@Override
	public String toString() {
		return "[t=" + token + ", p=" + pos + ", l=" + lemma + "]";
	}

}
