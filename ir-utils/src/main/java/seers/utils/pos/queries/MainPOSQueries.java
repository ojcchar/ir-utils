package seers.utils.pos.queries;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wayne.cs.severe.ir4se.processor.entity.Query;
import edu.wayne.cs.severe.ir4se.processor.utils.ParameterUtils;
import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;
import preprocessor.GeneralStemmer;
import preprocessor2.QueryException;
import preprocessor2.QueryPreprocessor2;
import seers.utils.islandparsing.MainIslandParsingQueries;
import seers.utils.pos.POSAnnotator;
import seers.utils.pos.POSInfo;
import seers.utils.pos.POSSentence;
import seers.utils.pos.POSToken;

public class MainPOSQueries {

	static Logger LOGGER = LoggerFactory.getLogger(MainPOSQueries.class);

	// private static List<String> stopWords;
	static QueryPreprocessor2 processor = new QueryPreprocessor2();
	private static String outFolder;

	public static void main(String[] args) throws Exception {

		String[] systems = args[0].split(",");
		String irFolderBase = args[1];
		stopWordsFile = args[2];
		outFolder = args[3];

		Map<String, String> params = new HashMap<>();
		params.put(ParameterUtils.BASE_DIR, irFolderBase);

		processSystems(systems, params, true);
		processSystems(systems, params, false);

	}

	private static void processSystems(String[] systems, Map<String, String> params, boolean includePos)
			throws Exception, QueryException, IOException {

		List<String> stopWords = org.apache.commons.io.FileUtils.readLines(new File(stopWordsFile));

		for (String sys : systems) {

			System.out.println("Processing " + sys);

			params.put(ParameterUtils.SYSTEM, sys);

			List<Query> queries = readQueries(ParameterUtils.getFilePathPrefix(params) + "_Queries_parsed.txt");
			String queriesFilePath = ParameterUtils.getQueriesFilePath(params);
			List<preprocessor2.Query> oldQueries = processor.readQueries(queriesFilePath, true);

			String queriesPOSPath = ParameterUtils.getFilePathPrefix(params) + "_Queries" + (includePos ? "_pos" : "")
					+ ".txt";

			HashMap<String, List<POSToken>> queryTokens = new HashMap<>();

			for (Query query : queries) {

				String key = (String) query.getInfoAttribute(Query.ISSUE_ID);
				// System.out.println(key + " --------------------------");

				// if (!key.equalsIgnoreCase("BOOKKEEPER-294")) {
				// continue;
				// }
				List<String> relJudgement = getRelJudgement(key, oldQueries);

				List<POSToken> posTokens = getPOStokens(query.getTxt(), relJudgement, stopWords);
				queryTokens.put(key, posTokens);

			}

			updateOldQueries(oldQueries, queryTokens, queriesPOSPath, includePos);

		}
	}

	static List<POSToken> getPOStokens(String txt, List<String> relJudgement, List<String> stopWords) {

		List<List<String>> tokensSentences = TextPreprocessor.tokenizeInSentences(txt);
		POSInfo posInfo = new POSInfo();
		List<String> txtTokens = new ArrayList<>();
		for (List<String> sentenceTokens : tokensSentences) {
			List<String> preProcessedTokens = preprocessTxt2(sentenceTokens, relJudgement, stopWords);
			POSInfo sentencePosInfo = getPOSInfo(tokensToTxt(preProcessedTokens));

			posInfo.addPosInfo(sentencePosInfo);
			txtTokens.addAll(preProcessedTokens);

		}

		// System.out.println(txtTokens);

		List<POSToken> posTokens = mapPOSInfo(posInfo, txtTokens);
		return posTokens;
	}

	private static List<String> getRelJudgement(String key, List<preprocessor2.Query> oldQueries) {
		for (preprocessor2.Query query : oldQueries) {
			if (key.equals(query.getIssueId())) {
				return query.getRelJud();
			}
		}
		return null;
	}

	private static void updateOldQueries(List<preprocessor2.Query> oldQueries,
			HashMap<String, List<POSToken>> queryTokens, String queriesFilePath, boolean includePos)
					throws IOException {

		for (preprocessor2.Query query : oldQueries) {
			LinkedList<String> tokList = getTokenList(queryTokens.get(query.getIssueId()), includePos);
			query.setTxtFromTokens(tokList);
		}

		File childFile = new File(queriesFilePath);
		processor.writeQueries(outFolder, childFile, oldQueries, true);
	}

	public static LinkedList<String> getTokenList(List<POSToken> list, boolean includePos) {
		LinkedList<String> tokens = new LinkedList<>();
		for (POSToken posToken : list) {
			if (posToken != null) {
				tokens.add(GeneralStemmer.stemmingPorter(posToken.token).trim()
						+ (includePos ? (":" + getPOSValue(posToken.pos)) : ""));
			}
		}
		return tokens;
	}

	private static HashMap<String, String> POS_TAGS = new HashMap<String, String>();
	private static String stopWordsFile;

	static {

		POS_TAGS.put("JJ", "JJ");
		POS_TAGS.put("JJR", "JJ");
		POS_TAGS.put("JJS", "JJ");

		POS_TAGS.put("NN", "NN");
		POS_TAGS.put("NNS", "NN");
		POS_TAGS.put("NNP", "NN");
		POS_TAGS.put("NNPS", "NN");

		POS_TAGS.put("PRP", "PRP");
		POS_TAGS.put("PRP$", "PRP");

		POS_TAGS.put("RB", "RB");
		POS_TAGS.put("RBR", "RB");
		POS_TAGS.put("RBS", "RB");

		POS_TAGS.put("VB", "VB");
		POS_TAGS.put("VBD", "VB");
		POS_TAGS.put("VBG", "VB");
		POS_TAGS.put("VBN", "VB");
		POS_TAGS.put("VBP", "VB");
		POS_TAGS.put("VBZ", "VB");

		POS_TAGS.put("WDT", "WH");
		POS_TAGS.put("WP", "WH");
		POS_TAGS.put("WP$", "WH");
		POS_TAGS.put("WRB", "WH");
	}

	private static String getPOSValue(String pos) {
		String tag = POS_TAGS.get(pos);
		if (tag != null) {
			return tag;
		}
		return pos;
	}

	public static String tokensToTxt(List<String> txtTokens) {
		StringBuffer buffer = new StringBuffer();
		for (String token : txtTokens) {
			buffer.append(token);
			buffer.append(" ");
		}
		return buffer.toString().trim();
	}

	public static List<POSToken> mapPOSInfo(POSInfo posInfo, List<String> txtTokens) {

		List<POSToken> tokens = getListTerms(posInfo);
		List<POSToken> tokensAssigned = new ArrayList<>();

		int j = 0;
		int i = 0;
		int jPrev = j;
		while (i < txtTokens.size()) {
			String word = txtTokens.get(i);
			POSToken token = tokens.get(j);

			String token2 = token.token;
			if (word.equals(token2)
			// || token2.startsWith(word) || token2.endsWith(word)
			) {
				tokensAssigned.add(token);
				i++;
				jPrev = j;
			} else {
				j++;
			}

			if (j >= tokens.size()) {
				LOGGER.error("Word not found: " + word);
				tokensAssigned.add(null);
				i++;
				j = jPrev;
			}
		}

		return tokensAssigned;

	}

	private static List<POSToken> getListTerms(POSInfo posInfo) {
		List<POSToken> tokens = new ArrayList<>();
		List<POSSentence> sentences = posInfo.sentences;
		for (POSSentence posSentence : sentences) {
			tokens.addAll(posSentence.tokens);
		}
		return tokens;
	}

	public static List<String> preprocessTxt2(List<String> tokens, List<String> relJudg, List<String> stopWords) {

		Set<String> tokensToRemove = removeClassesInText(tokens, relJudg);

		List<String> tokensPrep = TextPreprocessor.removeSpecifiedTokens(tokens, tokensToRemove);
		tokensPrep = TextPreprocessor.removeStopWords(tokensPrep, stopWords);
		tokensPrep = TextPreprocessor.removePunctuation(tokensPrep);
		tokensPrep = TextPreprocessor.removeNonLiterals(tokensPrep);
		tokensPrep = TextPreprocessor.splitIdentifiers(tokensPrep);
		tokensPrep = TextPreprocessor.removeIntegers(tokensPrep);
		tokensPrep = TextPreprocessor.removeBlanks(tokensPrep);
		tokensPrep = TextPreprocessor.removeShortTokens(tokensPrep, 2);

		return tokensPrep;
	}

	public static Set<String> removeClassesInText(List<String> tokens, List<String> relJuds) {

		Set<String> tokensToRemove = new HashSet<>();

		if (relJuds == null) {
			return tokensToRemove;
		}

		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);

			if (token.contains(".java") || token.contains(".class")) {
				tokensToRemove.add(token);
			}
		}

		// --------------------------------

		for (String relJud : relJuds) {
			int indexOf = relJud.indexOf(".");

			while (indexOf != -1) {
				String subStr = relJud.substring(indexOf + 1, relJud.length());

				for (int i = 0; i < tokens.size(); i++) {
					String token = tokens.get(i);

					if (token.contains(subStr)) {
						tokensToRemove.add(token);
					}

				}

				indexOf = relJud.indexOf(".", indexOf + 1);
			}
		}

		return tokensToRemove;

	}

	public static POSInfo getPOSInfo(String text) {
		String text2 = MainIslandParsingQueries.deleteNewLinesAndTags(text);
		POSInfo info = POSAnnotator.annotate(text2);
		return info;
	}

	private static List<Query> readQueries(String file) throws Exception {
		List<Query> queries = new ArrayList<>();
		CsvParser csvParser = new CsvParserBuilder().separator(';').build();
		try (CsvReader csvReader = new CsvReader(new FileReader(file), csvParser)) {

			List<List<String>> readAll = csvReader.readAll();

			int qId = 0;
			for (List<String> list : readAll) {
				Query q = new Query(qId++, list.get(1));
				q.addInfoAttribute(Query.ISSUE_ID, list.get(0));
				queries.add(q);

			}

		}

		return queries;
	}
}
