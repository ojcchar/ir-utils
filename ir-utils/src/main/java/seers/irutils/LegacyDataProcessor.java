package seers.irutils;

import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wayne.cs.severe.ir4se.processor.entity.Query;
import edu.wayne.cs.severe.ir4se.processor.entity.RelJudgment;
import edu.wayne.cs.severe.ir4se.processor.entity.RetrievalDoc;
import edu.wayne.cs.severe.ir4se.processor.exception.QueryException;
import edu.wayne.cs.severe.ir4se.processor.utils.ExceptionUtils;
import edu.wayne.cs.severe.ir4se.processor.utils.ParameterUtils;
import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;
import seers.irda.dao.GenericDao;
import seers.irda.dao.GenericDaoImpl;
import seers.irda.dao.impl.CodeFileDao;
import seers.irda.dao.impl.IrDocumentDao;
import seers.irda.dao.impl.IssueDao;
import seers.irda.dao.impl.RevisionDao;
import seers.irda.dao.impl.SoftwareSystemDao;
import seers.irda.entity.CodeFile;
import seers.irda.entity.IrDocument;
import seers.irda.entity.IrQuery;
import seers.irda.entity.IrRelevanceJudgement;
import seers.irda.entity.IrRelevanceJudgementId;
import seers.irda.entity.Issue;
import seers.irda.entity.Revision;
import seers.irda.entity.SoftwareSystem;

/**
 *
 */
public class LegacyDataProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(LegacyDataProcessor.class);

	public static void main(String[] args) throws Exception {

		String[] systems = args[0].split(",");
		String irFolderBase = args[1];

		LegacyDataParser parser = new LegacyDataParser();

		Map<String, String> params = new HashMap<>();
		params.put(ParameterUtils.BASE_DIR, irFolderBase);

		Session session = GenericDao.openSession();

		try {

			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				for (String sys : systems) {

					System.out.println("Processing " + sys);

					params.put(ParameterUtils.SYSTEM, sys);

					List<CsvIssue> qInfo = readIssues(ParameterUtils.getQueriesFileInfoPath(params));
					List<Query> queries = parser.readQueries(ParameterUtils.getQueriesFilePath(params));
					Map<Query, RelJudgment> releJudgments = parser.readReleJudgments(
							ParameterUtils.getRelJudFilePath(params), ParameterUtils.getDocMapPath(params));
					List<RetrievalDoc> corpus = parser.readCorpus(ParameterUtils.getCorpFilePath(params),
							ParameterUtils.getDocMapPath(params));
					writeData(qInfo, queries, releJudgments, corpus, sys, session);

				}

				tx.rollback();
			} catch (Exception e) {
				if (tx != null)
					tx.rollback();
				throw e;
			}
		} finally {
			session.close();
			GenericDao.close();
		}

	}

	private static void writeData(List<CsvIssue> qInfo, List<Query> queries, Map<Query, RelJudgment> releJudgments,
			List<RetrievalDoc> corpus, String sys, Session session) {

		SoftwareSystem system = saveSystem(sys, session);

		// ------------------------------

		List<Issue> issues = saveIssues(qInfo, session, system);

		// ------------------------------------

		Revision rev = saveRevision(session, system, issues, sys);

		// ------------------------------------

		saveCorpus(corpus, session, rev, system);

		// ------------------------------------

		saveQueriesAndRelJudg(queries, releJudgments, session, system, rev);

	}

	private static void saveQueriesAndRelJudg(List<Query> queries, Map<Query, RelJudgment> releJudgments,
			Session session, SoftwareSystem system, Revision rev) {
		GenericDaoImpl<IrQuery, Integer> queryDao = new GenericDaoImpl<>(session);
		GenericDaoImpl<IrRelevanceJudgement, IrRelevanceJudgementId> rjDao = new GenericDaoImpl<>(session);
		IssueDao issueDao = new IssueDao(session);
		IrDocumentDao docDao = new IrDocumentDao(session);

		for (Query query : queries) {
			IrQuery entity = new IrQuery();

			Issue issue = issueDao.getIssue((String) query.getInfoAttribute(Query.ISSUE_ID), system);
			entity.setIssue(issue);
			entity.setTxt(query.getTxt());
			entity.setType("default");

			queryDao.persist(entity);

			List<RetrievalDoc> relJudgments = releJudgments.get(query).getRelevantDocs();
			for (RetrievalDoc retrievalDoc : relJudgments) {
				IrDocument document = docDao.getIrDocument(retrievalDoc.getDocName(), rev);
				IrRelevanceJudgement entityRJ = new IrRelevanceJudgement(
						new IrRelevanceJudgementId(document.getId(), entity.getId()), document, entity);

				rjDao.persist(entityRJ);
			}

		}
	}

	private static void saveCorpus(List<RetrievalDoc> corpus, Session session, Revision rev, SoftwareSystem system) {
		IrDocumentDao docDao = new IrDocumentDao(session);
		for (RetrievalDoc retrievalDoc : corpus) {
			CodeFile codeFile = findCodeFile(retrievalDoc, session, system);
			if (codeFile == null) {
				continue;
			}
			IrDocument entity = new IrDocument(codeFile, rev, retrievalDoc.getDocName(), retrievalDoc.getDocText(),
					"class");

			IrDocument irDocument = docDao.getIrDocument(retrievalDoc.getDocName(), rev);
			if (irDocument == null) {
				docDao.persist(entity);
			}

		}
	}

	private static CodeFile findCodeFile(RetrievalDoc retrievalDoc, Session session, SoftwareSystem system) {
		CodeFileDao dao = new CodeFileDao(session);
		String path = getPath(retrievalDoc.getDocName());
		CodeFile codeFile = dao.getCodeFile(path, system);
		if (codeFile == null) {
			// path = retrievalDoc.getDocName().replace(".", "/") + ".java";
			// codeFile = dao.getCodeFile(path, system);
			LOGGER.error("Sys: " + system.getName() + ", file not found: " + path);
		}
		return codeFile;
	}

	private static String getPath(String docName) {
		// StringBuffer buffer = new StringBuffer(docName);
		//
		// int i = buffer.lastIndexOf(".");
		// buffer.replace(i, i+1, buffer.substring(i, i+1).tou)
		//
		int i = docName.indexOf("$");
		if (i != -1) {
			docName = docName.substring(0, i);
		}

		return docName.replace(".", "/") + ".java";
	}

	private static Revision saveRevision(Session session, SoftwareSystem system, List<Issue> issues, String sys) {
		RevisionDao revDao = new RevisionDao(session);
		String commitId = "";
		Revision revision = revDao.getRevision(commitId, system);
		if (revision != null) {
			return revision;
		}

		Revision rev = new Revision(system, commitId, "", "", new Date());
		// rev.setIssueRevisions(issueRevisions);
		rev.setAlias(sys.split("-")[1]);
		revDao.persist(rev);
		return rev;
	}

	private static List<Issue> saveIssues(List<CsvIssue> qInfo, Session session, SoftwareSystem system) {
		IssueDao issueDao = new IssueDao(session);
		for (CsvIssue queryInfo : qInfo) {
			Issue issue = new Issue();

			issue.setIssueId(queryInfo.key);
			issue.setType(queryInfo.issueType);
			issue.setFixversion(queryInfo.fixVersions);
			issue.setResolution(queryInfo.resolution);
			issue.setResolutionDate(queryInfo.resolutionDate);
			issue.setCreateDate(queryInfo.created);
			issue.setPriority(queryInfo.priority);
			issue.setStatus(queryInfo.status);
			issue.setComponents(queryInfo.components);
			issue.setDescription(queryInfo.description);
			issue.setSummary(queryInfo.summary);
			issue.setReporter(queryInfo.reporter);
			issue.setSoftwareSystem(system);

			Issue issue2 = issueDao.getIssue(queryInfo.key, system);
			if (issue2 == null) {
				issueDao.persist(issue);
			}

		}

		return issueDao.getIssues(system);
	}

	private static SoftwareSystem saveSystem(String sys, Session session) {
		SoftwareSystemDao dao = new SoftwareSystemDao(session);
		String name = sys.split("-")[0];

		SoftwareSystem system = dao.getSystem(name);
		if (system == null) {
			system = new SoftwareSystem(name);
			dao.persist(system);
		}
		return system;
	}

	public static List<CsvIssue> readIssues(String queriesFileInfoPath) throws QueryException {

		List<CsvIssue> infoList = new ArrayList<>();
		File fileIn = new File(queriesFileInfoPath);

		if (!fileIn.isFile() || !fileIn.exists()) {
			throw new QueryException("Query info file (" + queriesFileInfoPath + ") is not valid!");
		}

		CsvParser csvParser = new CsvParserBuilder().separator(';').build();
		try (CsvReader csvReader = new CsvReader(new FileReader(fileIn), csvParser)) {

			List<List<String>> readAll = csvReader.readAll();

			for (List<String> list : readAll) {

				CsvIssue info = new CsvIssue();

				info.key = getField(list, 1);

				info.issueType = getField(list, 2);
				info.fixVersions = getField(list, 3);
				info.resolution = getField(list, 4);
				info.resolutionDate = getDateField(list, 5);
				info.created = getDateField(list, 6);
				info.priority = getField(list, 7);
				info.updated = getDateField(list, 8);
				info.status = getField(list, 9);
				info.components = getField(list, 10);
				info.description = getField(list, 11);
				info.summary = getField(list, 13);
				info.creator = getField(list, 15);
				info.reporter = getField(list, 16);

				infoList.add(info);

			}

		} catch (Exception e) {
			QueryException e2 = new QueryException(e.getMessage());
			ExceptionUtils.addStackTrace(e, e2);
			throw e2;
		}
		return infoList;
	}

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private static Date getDateField(List<String> list, int i) throws ParseException {
		String field = getField(list, i);
		if (field == null) {
			return null;
		}
		return dateFormat.parse(field);
	}

	private static String getField(List<String> list, int i) {
		return "NA".equals(list.get(i)) ? null : list.get(i);
	}

}
