package seers.irutils.migration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.wayne.cs.severe.ir4se.processor.controllers.impl.DefaultRetrievalParser;
import edu.wayne.cs.severe.ir4se.processor.entity.RetrievalDoc;
import preprocessor2.Query;
import preprocessor2.QueryPreprocessor2;

public class UpdateRelevanceJudgements {

	public static void main(String[] args) throws Exception {
		String queriesFile = args[0];
		String corpusFile = args[1];
		String outFolder = args[2];

		QueryPreprocessor2 p = new QueryPreprocessor2();
		List<Query> queries = p.readQueries(queriesFile, true);

		DefaultRetrievalParser p2 = new DefaultRetrievalParser();
		List<RetrievalDoc> corpus = p2.readCorpus(corpusFile);

		// -------------------------------------

		for (Query query : queries) {

			List<String> relJud = query.getRelJud();

			List<String> newRelJud = getNewRelJud(corpus, relJud);

			query.setRelJud(newRelJud);

		}

		// -------------------------------------

		p.writeQueries(outFolder, new File(queriesFile), queries, true);
	}

	private static List<String> getNewRelJud(List<RetrievalDoc> corpus, List<String> relJud) {

		List<String> newRel = new ArrayList<>();

		for (String rj : relJud) {

			String newR = rj;
			List<RetrievalDoc> potentialDocs = getPotentialDocs(rj, corpus);
			if (potentialDocs.size() > 1) {
				System.err.println("Multiiple potential docs found for " + rj);
				for (RetrievalDoc doc : potentialDocs) {
					System.err.println(doc.getDocName());
				}
			} else if (potentialDocs.isEmpty()) {
				System.err.println("No potential docs found for " + rj);
			} else {
				newR = potentialDocs.get(0).getDocName();
			}

			newRel.add(newR);

		}
		return newRel;
	}

	private static List<RetrievalDoc> getPotentialDocs(String rj, List<RetrievalDoc> corpus) {
		List<RetrievalDoc> docs = new ArrayList<>();
		for (RetrievalDoc doc : corpus) {
			if (doc.getDocName().endsWith(rj)) {
				docs.add(doc);
			}
		}
		return docs;
	}

}
