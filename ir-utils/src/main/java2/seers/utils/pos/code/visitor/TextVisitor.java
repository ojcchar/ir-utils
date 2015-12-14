package seers.utils.pos.code.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;

public class TextVisitor extends ASTVisitor {

	private List<String> textItems = new ArrayList<>();

	@Override
	public boolean visit(SimpleName node) {
		textItems.add(node.getIdentifier());
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		textItems.add(node.getLiteralValue());
		return super.visit(node);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean visit(Javadoc node) {
		List<TagElement> tags = node.tags();

		for (TagElement tagEl : tags) {

			List fragments = tagEl.fragments();

			StringBuffer comment = new StringBuffer();
			for (Object fragment : fragments) {
				comment.append(fragment);
				comment.append(" ");
			}
			textItems.add(comment.toString().trim());

		}
		return super.visit(node);
	}

	public List<String> getTextItems() {
		return textItems;
	}

}
