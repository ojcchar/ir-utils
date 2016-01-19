package seers.irutils.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.wayne.cs.severe.ir4se.processor.controllers.impl.author.cg.AuthorCGParser;
import edu.wayne.cs.severe.ir4se.processor.controllers.termrem.utils.QueryTermUtils;
import edu.wayne.cs.severe.ir4se.processor.entity.Query;
import edu.wayne.cs.severe.ir4se.processor.utils.ExceptionUtils;
import edu.wayne.cs.severe.ir4se.processor.utils.ParameterUtils;
import seers.appcore.threads.processor.ThreadException;
import seers.appcore.threads.processor.ThreadProcessor;

public class PaginatedPOSTermStats implements ThreadProcessor {

	private String sys, outFolder;
	private Map<String, String> params;
	private static AuthorCGParser parser = new AuthorCGParser();

	private static final List<String> POS_TAGS = new ArrayList<>();

	static {
		POS_TAGS.add("CC");
		POS_TAGS.add("CD");
		POS_TAGS.add("DT");
		POS_TAGS.add("EX");
		POS_TAGS.add("FW");
		POS_TAGS.add("IN");
		POS_TAGS.add("JJ");
		POS_TAGS.add("JJR");
		POS_TAGS.add("JJS");
		POS_TAGS.add("LS");
		POS_TAGS.add("MD");
		POS_TAGS.add("NN");
		POS_TAGS.add("PDT");
		POS_TAGS.add("POS");
		POS_TAGS.add("PRP");
		POS_TAGS.add("RB");
		POS_TAGS.add("RP");
		POS_TAGS.add("SYM");
		POS_TAGS.add("TO");
		POS_TAGS.add("UH");
		POS_TAGS.add("VB");
		POS_TAGS.add("WH");
		POS_TAGS.add("$");
	}

	public PaginatedPOSTermStats(String sys, String outFolder, Map<String, String> params) {
		super();
		this.sys = sys;
		this.outFolder = outFolder;
		this.params = new HashMap<>(params);
		this.params.put(ParameterUtils.SYSTEM, sys);
	}

	@Override
	public void processJob() throws ThreadException {
		try {
			List<Query> queries = parser.readQueries(ParameterUtils.getFilePathPrefix(params) + "_Queries_pos.txt");

			for (Query query : queries) {
				HashMap<String, Set<String>> queryTermPos = new LinkedHashMap<>();
				String[] terms = QueryTermUtils.getQueryTerms(query);
				processTerms(queryTermPos, terms);
				printTermsInfo(query.getQueryId(), queryTermPos);
			}

			// System.out.println("--------------------------------------------------------");
			// HashMap<String, Set<String>> docTermPos = new LinkedHashMap<>();
			// List<RetrievalDoc> corpus =
			// parser.readCorpus(ParameterUtils.getFilePathPrefix(params) +
			// "_Corpus_pos.txt");
			// for (RetrievalDoc doc : corpus) {
			// String[] terms = QueryTermUtils.getTerms(doc.getDocText());
			// try {
			// processTerms(docTermPos, terms);
			// } catch (RuntimeException e) {
			// System.err.println("Error for doc: " + doc.getDocName());
			// e.printStackTrace();
			// }
			// }
			// printTermsInfo(docTermPos);
		} catch (Exception e) {
			ThreadException e2 = new ThreadException(e.getClass().getSimpleName() + ": " + e.getMessage());
			ExceptionUtils.addStackTrace(e, e2);
			throw e2;
		}
	}

	private void printTermsInfo(Integer id, HashMap<String, Set<String>> termPos) {
		Set<Entry<String, Set<String>>> entrySet = termPos.entrySet();
		StringBuffer buffer = new StringBuffer();
		for (Entry<String, Set<String>> entry : entrySet) {
			// if (entry.getValue().size() == 1) {
			// continue;
			// }
			buffer.append(sys);
			buffer.append(";");
			buffer.append(id);
			buffer.append(";");
			buffer.append(entry.getKey());
			buffer.append(";");
			buffer.append(getPosString(entry.getValue()));
			buffer.append(";");
			buffer.append(entry.getValue().size());
			buffer.append("\r\n");
		}
		System.out.print(buffer.toString());

	}

	private String getPosString(Set<String> value) {
		List<String> tags = new ArrayList<>();
		for (String tag : POS_TAGS) {
			if (value.contains(tag)) {
				tags.add(tag);
			}
		}

		if (value.size() != tags.size()) {
			throw new RuntimeException("Could not find all the tags");
		}
		StringBuffer buf = new StringBuffer();
		for (String tag : tags) {
			buf.append(tag);
			buf.append("-");
		}
		buf.delete(buf.length() - 1, buf.length());

		return buf.toString();
	}

	private void processTerms(HashMap<String, Set<String>> termPos, String[] terms) {
		for (String term : terms) {
			int i = term.indexOf(":");

			if (i == -1) {
				throw new RuntimeException("sys: " + sys + ", temr: " + term);
			}

			String pos = term.substring(i + 1, term.length());
			String termPref = term.substring(0, i);

			Set<String> set = termPos.get(termPref);

			if (set == null) {
				set = new LinkedHashSet<>();
				termPos.put(termPref, set);
			}
			set.add(pos);
		}
	}

	@Override
	public String getName() {
		return sys;
	}

}
