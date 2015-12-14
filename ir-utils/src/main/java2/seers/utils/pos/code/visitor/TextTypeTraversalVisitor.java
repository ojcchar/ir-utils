package seers.utils.pos.code.visitor;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import seers.utils.pos.code.control.ASTTypePositions;

public class TextTypeTraversalVisitor extends ASTVisitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(TextTypeTraversalVisitor.class);

	private Map<String, List<String>> classesTextItems = new LinkedHashMap<>();
	private Map<String, ASTTypePositions> classesPositions = new LinkedHashMap<>();
	private File file;
	private String prefix;

	public TextTypeTraversalVisitor(String prefix, File file) {
		this.prefix = prefix;
		this.file = file;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		addQualfNameInfo(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		addQualfNameInfo(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		addQualfNameInfo(node);
		return super.visit(node);
	}

	private void addQualfNameInfo(AbstractTypeDeclaration node) {
		int startPosition = node.getStartPosition();
		int endPosition = startPosition + node.getLength();

		ITypeBinding resolveBinding = node.resolveBinding();
		String qualifiedName = resolveBinding.getQualifiedName();
		String className = node.getName().toString();

		// check for the names
		if (qualifiedName == null || qualifiedName.isEmpty()) {
			LOGGER.error("The qual. name is null or empty, class " + className + ", file: " + file);
			return;
		}

		List<String> identifiers = getIdentifiers(node);

		String completeQualifiedName = prefix + (prefix.isEmpty() ? "" : ".") + qualifiedName;

		classesTextItems.put(completeQualifiedName, identifiers);
		classesPositions.put(completeQualifiedName, new ASTTypePositions(startPosition, endPosition));

	}

	private List<String> getIdentifiers(AbstractTypeDeclaration node) {
		TextVisitor identifiersVisitor = new TextVisitor();
		node.accept(identifiersVisitor);
		List<String> textItems = identifiersVisitor.getTextItems();
		return textItems;
	}

	public Map<String, List<String>> getClassesTextItems() {
		return classesTextItems;
	}

	public Map<String, ASTTypePositions> getClassesPositions() {
		return classesPositions;
	}

}
