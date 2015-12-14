package seers.irutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.wayne.cs.severe.ir4se.processor.controllers.impl.author.cg.AuthorCGParser;
import edu.wayne.cs.severe.ir4se.processor.entity.Query;
import edu.wayne.cs.severe.ir4se.processor.entity.RelJudgment;
import edu.wayne.cs.severe.ir4se.processor.entity.RetrievalDoc;
import edu.wayne.cs.severe.ir4se.processor.exception.CorpusException;
import edu.wayne.cs.severe.ir4se.processor.exception.RelJudgException;
import edu.wayne.cs.severe.ir4se.processor.utils.ExceptionUtils;

public class LegacyDataParser extends AuthorCGParser {

	@Override
	protected void setDocNames(List<RetrievalDoc> docList, String mapDocsPath) throws CorpusException {
		String line;
		List<String> docNames = new ArrayList<String>();
		List<String> originalDocNames = new ArrayList<String>();

		BufferedReader inMapping = null;

		try {
			inMapping = new BufferedReader(new FileReader(mapDocsPath));

			while ((line = inMapping.readLine()) != null) {

				String lineTrimmed = line.trim();
				if (!lineTrimmed.isEmpty()) {
					// docNames.add(lineTrimmed);
					docNames.add(getDocQueryStr(lineTrimmed));
					originalDocNames.add(getDocQueryStr(lineTrimmed));
				}
			}
		} catch (Exception e) {
			CorpusException e2 = new CorpusException(e.getMessage());
			ExceptionUtils.addStackTrace(e, e2);
			throw e2;
		} finally {
			try {
				if (inMapping != null) {
					inMapping.close();
				}
			} catch (IOException e) {
				CorpusException e2 = new CorpusException(e.getMessage());
				ExceptionUtils.addStackTrace(e, e2);
				throw e2;
			}
		}

		if (docList.size() != docNames.size()) {
			throw new CorpusException("Number of documents and names do not match!");
		}

		for (int i = 0; i < docList.size(); i++) {
			RetrievalDoc doc = docList.get(i);
			doc.setDocName(docNames.get(i));
			doc.setOriginalDocName(originalDocNames.get(i));
		}

	}

	@Override
	protected String getDocQueryStr(String line) {
		String docStr = line.trim();
		return docStr;
	}

	@Override
	public Map<Query, RelJudgment> readReleJudgments(String releJudgmentPath, String mapDocsPath)
			throws RelJudgException {

		Map<Query, RelJudgment> relJudgMap = new LinkedHashMap<Query, RelJudgment>();
		File fileRelJudg = new File(releJudgmentPath);

		if (!fileRelJudg.isFile() || !fileRelJudg.exists()) {
			throw new RelJudgException("The Relevance Judgments file (" + releJudgmentPath + ") is not valid!");
		}

		File mapDocsFile = new File(mapDocsPath);
		if (!mapDocsFile.isFile() || !mapDocsFile.exists()) {
			throw new RelJudgException("The Mappings file (" + releJudgmentPath + ") is not valid!");
		}

		List<String> mapDocsStr = readDocuments(mapDocsPath, true);
		List<String> mapDocsStrCleaned = readDocuments(mapDocsPath, false);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileRelJudg));

			String line;
			int lineNumber = 0;
			int numberTargetDocs = -1;
			Integer queryId = 0;

			Query query = new Query();
			List<String> targetDocs = null;
			while ((line = reader.readLine()) != null) {

				// it is not a blank line
				String lineTrimmed = line.trim();
				if (!lineTrimmed.isEmpty()) {
					lineNumber++;
					switch (lineNumber) {
					case 1:
						if (lineTrimmed.contains(" ")) {
							queryId = Integer.valueOf(lineTrimmed.split(" ")[0]);
						} else {
							queryId = Integer.valueOf(lineTrimmed);
						}
						// System.out.println(queryId);
						query.setQueryId(queryId);
						// queryId++;
						break;
					// case 2:
					// query.setTxt(line.trim().toLowerCase());
					// break;
					case 3:
						numberTargetDocs = Integer.parseInt(lineTrimmed);
						targetDocs = new ArrayList<String>(numberTargetDocs);
						break;
					default:
						if (lineNumber >= 4) {
							targetDocs.add(lineNumber - 4, super.getDocQueryStr(line));
							// System.out.println(getDocQueryStr(line));
						}
						break;
					}
				} else {

					if (targetDocs != null) {
						RelJudgment relJud = new RelJudgment();
						List<RetrievalDoc> relevantDocs = getRelevantDocs(targetDocs, mapDocsStr, mapDocsStrCleaned);
						relJud.setRelevantDocs(relevantDocs);
						relJudgMap.put(query, relJud);
					}

					lineNumber = 0;
					numberTargetDocs = -1;
					query = new Query();
					targetDocs = null;
				}
			}

			if (targetDocs != null) {
				RelJudgment relJud = new RelJudgment();
				List<RetrievalDoc> relevantDocs = getRelevantDocs(targetDocs, mapDocsStr);
				relJud.setRelevantDocs(relevantDocs);
				relJudgMap.put(query, relJud);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RelJudgException(e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RelJudgException(e.getMessage());
				}
			}
		}
		return relJudgMap;
	}

	private List<String> readDocumentsCleaned(String mapDocsPath) {
		// TODO Auto-generated method stub
		return null;
	}

	protected List<RetrievalDoc> getRelevantDocs(List<String> targetDocs, List<String> mapDocsStr,
			List<String> mapDocsStrCleaned) throws RelJudgException {

		List<RetrievalDoc> relJudgDocs = new ArrayList<>();

		for (String targetDoc : targetDocs) {
			// System.out.println("Searching " + targetDoc);
			int docId = mapDocsStrCleaned.indexOf(targetDoc);
			if (docId != -1) {
				RetrievalDoc relJudgDoc = new RetrievalDoc();
				relJudgDoc.setDocId(docId);
				relJudgDoc.setDocName(mapDocsStr.get(docId));
				relJudgDocs.add(relJudgDoc);
			} else {
				LOGGER.warn("Doc not found in corpus (rel. judg. not considered): " + targetDoc);
			}

		}

		// if (relJudgDocs.isEmpty()) {
		// System.out.println("vacio");
		// // throw new RelJudgException(
		// // "Could not find the relevant judgement documents");
		// }

		return relJudgDocs;
	}

	protected List<String> readDocuments(String mapDocsPath, boolean clean) throws RelJudgException {

		List<String> mapDocsStr = new ArrayList<>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(mapDocsPath));

			String line;
			while ((line = reader.readLine()) != null) {

				if (line.trim().isEmpty()) {
					continue;
				}
				// mapDocsStr.add();
				String docStr = null;
				if (clean) {
					docStr = getDocQueryStr(line);
				} else {
					docStr = super.getDocQueryStr(line);
				}
				// System.out.println(docStr);
				mapDocsStr.add(docStr);

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RelJudgException(e.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RelJudgException(e.getMessage());
				}
			}
		}
		return mapDocsStr;
	}

}
