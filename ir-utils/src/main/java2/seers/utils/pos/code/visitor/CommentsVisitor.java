package seers.utils.pos.code.visitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Javadoc;

import seers.utils.pos.code.control.ASTTypePositions;

public class CommentsVisitor {

	private List<Comment> comments;
	private Map<String, ASTTypePositions> classesPositions;
	private Map<String, List<String>> classesComments = new LinkedHashMap<>();
	private String fileContent;

	public CommentsVisitor(List<Comment> comments, Map<String, ASTTypePositions> classesPositions, String fileContent) {
		this.comments = comments;
		this.classesPositions = classesPositions;
		this.fileContent = fileContent;
	}

	public void visit() {
		for (Comment comment : comments) {

			if (comment instanceof Javadoc) {
				continue;
			}

			String commentTxt = extractComment(comment);

			assignComment(comment, commentTxt);

		}
	}

	private void assignComment(Comment comment, String commentTxt) {

		int start = comment.getStartPosition();
		int end = start + comment.getLength();

		Set<Entry<String, ASTTypePositions>> entrySet = classesPositions.entrySet();
		for (Entry<String, ASTTypePositions> entry : entrySet) {
			ASTTypePositions positions = entry.getValue();

			if (positions.getStartPosition() <= start && end <= positions.getEndPosition()) {
				List<String> textItems = classesComments.get(entry.getKey());
				if (textItems == null) {
					textItems = new ArrayList<>();
					classesComments.put(entry.getKey(), textItems);
				}
				textItems.add(commentTxt);
			}
		}
	}

	private String extractComment(Comment comment) {

		int start = comment.getStartPosition();
		int end = start + comment.getLength();

		int offset = 0;
		if (comment instanceof BlockComment) {
			offset = 2;
		}
		String commentTxt = fileContent.substring(start + 2, end - offset);
		return commentTxt;
	}

	public Map<String, List<String>> getClassesComments() {
		return classesComments;
	}

}
