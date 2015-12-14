package seers.utils.pos.code.control;

public class ASTTypePositions {

	private int startPosition, endPosition;

	public ASTTypePositions(int startPosition, int endPosition) {
		super();
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

}
