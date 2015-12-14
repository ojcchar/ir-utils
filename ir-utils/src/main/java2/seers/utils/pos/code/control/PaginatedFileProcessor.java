package seers.utils.pos.code.control;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wayne.cs.severe.ir4se.processor.utils.ExceptionUtils;
import preprocessor.GeneralStemmer;
import seers.appcore.threads.processor.ThreadException;
import seers.appcore.threads.processor.ThreadProcessor;
import seers.codeparser.JavaCodeParser;
import seers.utils.pos.POSAnnotator;
import seers.utils.pos.POSInfo;
import seers.utils.pos.POSToken;
import seers.utils.pos.code.visitor.CommentsVisitor;
import seers.utils.pos.code.visitor.TextTypeTraversalVisitor;
import seers.utils.pos.queries.MainPOSQueries;
import seers.utils.pos.queries.TextPreprocessor;

public class PaginatedFileProcessor implements ThreadProcessor {

	private List<String> stopWords;
	private List<File> files;
	private String name;
	private JavaCodeParser codeParser;
	private HashMap<String, String> subFoldPrefixes;

	private Logger LOGGER;
	private File outFilePOS;
	private File outFileNoPOS;

	public PaginatedFileProcessor(int fromIndex, int toIndex, LinkedList<File> allFiles, String baseFolder,
			String[] classPaths, final String[] sourceFolders, List<String> stopWords, File outFile, File outFileNoPOS,
			HashMap<String, String> subFoldPrefixes) {
		if (toIndex >= allFiles.size()) {
			toIndex = allFiles.size();
		}
		this.files = allFiles.subList(fromIndex, toIndex);

		name = PaginatedFileProcessor.class.getSimpleName() + "-" + fromIndex + "-" + toIndex;
		codeParser = new JavaCodeParser(baseFolder, classPaths, sourceFolders);
		LOGGER = LoggerFactory.getLogger(name);

		this.stopWords = stopWords;
		this.outFilePOS = outFile;
		this.outFileNoPOS = outFileNoPOS;
		this.subFoldPrefixes = subFoldPrefixes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processJob() throws ThreadException {

		// int num = 0;
		try {
			for (File file : files) {

				// if (!file.toString().endsWith("HedwigSocketAddress.java")) {
				// continue;
				// }

				CompilationUnit cu = codeParser.parseFile(file);

				// -------------------------------------------------

				String prefix = getSubFoldPref(file.toString());
				TextTypeTraversalVisitor visitor = new TextTypeTraversalVisitor(prefix, file);
				cu.accept(visitor);
				Map<String, List<String>> classesTextItems = visitor.getClassesTextItems();
				Map<String, ASTTypePositions> classesPositions = visitor.getClassesPositions();

				CommentsVisitor visitor2 = new CommentsVisitor((List<Comment>) cu.getCommentList(), classesPositions,
						FileUtils.readFileToString(file));
				visitor2.visit();
				Map<String, List<String>> classesComments = visitor2.getClassesComments();

				// --------------------------------------------------

				// Set<Entry<String, List<String>>> entrySet =
				// classesTextItems.entrySet();
				// for (Entry<String, List<String>> entry : entrySet) {
				// LOGGER.debug(entry.getKey() + ": " + entry.getValue());
				// }

				// entrySet = classesComments.entrySet();
				// for (Entry<String, List<String>> entry : entrySet) {
				// LOGGER.debug(entry.getKey() + ": " + entry.getValue());
				// }

				// ----------------------------------------------------

				processTextItems(classesTextItems, classesComments);

				cu = null;

				// num++;
			}
			// LOGGER.debug("Done... " + num);

		} catch (Exception e) {
			ThreadException e2 = new ThreadException(e.getMessage());
			ExceptionUtils.addStackTrace(e, e2);
			throw e2;
		}
	}

	private void processTextItems(Map<String, List<String>> classesTextItems, Map<String, List<String>> classesComments)
			throws IOException {
		Set<Entry<String, List<String>>> entrySet = classesTextItems.entrySet();
		for (Entry<String, List<String>> entry : entrySet) {
			List<String> textItems = entry.getValue();
			String className = entry.getKey();

			// LOGGER.debug(className);

			List<String> comments = classesComments.get(className);
			if (comments != null) {
				textItems.addAll(comments);
			}

			List<String> allTokens = new ArrayList<>();
			List<String> allTokensSentences = new ArrayList<>();
			for (String text : textItems) {
				List<String> txtTokens = preprocessTxt(text);

				allTokens.addAll(txtTokens);
				allTokensSentences.addAll(txtTokens);
				allTokensSentences.add(".");
			}

			String txt = MainPOSQueries.tokensToTxt(allTokensSentences);
			POSInfo posInfo = POSAnnotator.annotate(txt);
			List<POSToken> posTokens = MainPOSQueries.mapPOSInfo(posInfo, allTokens);
			LinkedList<String> tokenList = MainPOSQueries.getTokenList(posTokens, true);

			String txtPos = MainPOSQueries.tokensToTxt(tokenList);
			String data = "\"" + className + "\";\"" + txtPos + "\"\r\n";
			FileUtils.write(outFilePOS, data, true);

			stemTokens(allTokens);
			txtPos = MainPOSQueries.tokensToTxt(allTokens);
			data = "\"" + className + "\";\"" + txtPos + "\"\r\n";
			FileUtils.write(outFileNoPOS, data, true);
		}
	}

	private void stemTokens(List<String> allTokens) {

		for (int i = 0; i < allTokens.size(); i++) {
			String token = allTokens.get(i);
			allTokens.set(i, GeneralStemmer.stemmingPorter(token).trim());
		}

	}

	public List<String> preprocessTxt(String text) {

		List<String> tokens = TextPreprocessor.tokenize(text);

		List<String> tokensPrep = TextPreprocessor.removeStopWords(tokens, stopWords);
		tokensPrep = TextPreprocessor.removePunctuation(tokensPrep);
		tokensPrep = TextPreprocessor.removeNonLiterals(tokensPrep);
		tokensPrep = TextPreprocessor.breakIdentifiers(tokensPrep);
		tokensPrep = TextPreprocessor.removeStopWords(tokensPrep, stopWords);
		tokensPrep = TextPreprocessor.removeIntegers(tokensPrep);
		tokensPrep = TextPreprocessor.removeBlanks(tokensPrep);
		tokensPrep = TextPreprocessor.removeShortTokens(tokensPrep, 2);

		return tokensPrep;
	}

	private String getSubFoldPref(String fileStr) {
		Set<Entry<String, String>> entrySet = subFoldPrefixes.entrySet();
		for (Entry<String, String> entry : entrySet) {
			if (fileStr.startsWith(entry.getKey())) {
				return entry.getValue();
			}
		}
		return "";
	}

	@Override
	public String getName() {
		return name;
	}

}
