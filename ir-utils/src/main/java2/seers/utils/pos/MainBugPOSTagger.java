package seers.utils.pos;

import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.wayne.cs.severe.ir4se.processor.entity.Query;
import edu.wayne.cs.severe.ir4se.processor.exception.QueryException;
import edu.wayne.cs.severe.ir4se.processor.utils.ExceptionUtils;
import edu.wayne.cs.severe.ir4se.processor.utils.ParameterUtils;
import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;
import seers.irutils.CsvIssue;
import seers.irutils.LegacyDataParser;
import seers.utils.islandparsing.MainIslandParsingQueries;

public class MainBugPOSTagger {

	public static void main(String[] args) throws Exception {

		String[] systems = args[0].split(",");
		String irFolderBase = args[1];

		LegacyDataParser parser = new LegacyDataParser();

		Map<String, String> params = new HashMap<>();
		params.put(ParameterUtils.BASE_DIR, irFolderBase);

		for (String sys : systems) {

			System.out.println("Processing " + sys);

			params.put(ParameterUtils.SYSTEM, sys);

			List<CsvIssue> qInfo = readIssues(ParameterUtils.getQueriesFileInfoPath(params), false);
			List<Query> queries = parser.readQueries(ParameterUtils.getQueriesFilePath(params));

			for (Query query : queries) {
				int i = qInfo.indexOf(new CsvIssue((String) query.getInfoAttribute(Query.ISSUE_ID)));
				CsvIssue csvIssue = qInfo.get(i);
				processIssue(csvIssue);
			}

		}
	}

	private static void processIssue(CsvIssue csvIssue) {
		System.out.println(csvIssue.key + " ---------------------------------------------------------");
		String summary = csvIssue.summary;
		String description = csvIssue.description;

		String text = summary + ". " + description;
		System.out.println(text);

		text = MainIslandParsingQueries.deleteNewLinesAndTags(text);
		POSInfo info = POSAnnotator.annotate(text);

		for (POSSentence sentence : info.sentences) {
			List<POSToken> tokens = sentence.tokens;

			for (POSToken posToken : tokens) {
				System.out.print(posToken);
				System.out.print(", ");
			}

			System.out.println();
		}
	}

	public static List<CsvIssue> readIssues(String queriesFileInfoPath, boolean escape) throws QueryException {

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

				info.id = getField(list, 0, escape);
				info.key = getField(list, 1, escape);

				info.issueType = getField(list, 2, escape);
				info.fixVersions = getField(list, 3, escape);
				info.resolution = getField(list, 4, escape);
				info.resolutionDate = getDateField(list, 5);
				info.created = getDateField(list, 6);
				info.priority = getField(list, 7, escape);
				info.updated = getDateField(list, 8);
				info.status = getField(list, 9, escape);
				info.components = getField(list, 10, escape);
				info.description = getField(list, 11, escape);
				info.descriptionParsed = getField(list, 12, escape);
				info.summary = getField(list, 13, escape);
				info.summaryParsed = getField(list, 14, escape);
				info.creator = getField(list, 15, escape);
				info.reporter = getField(list, 16, escape);

				infoList.add(info);

			}

		} catch (Exception e) {
			QueryException e2 = new QueryException(e.getMessage());
			ExceptionUtils.addStackTrace(e, e2);
			throw e2;
		}
		return infoList;
	}

	private static String getField(List<String> list, int i, boolean escape) {
		String val = list.get(i);
		return "NA".equals(val) ? null : (escape ? escapeVal(val) : val);
	}

	private static String escapeVal(String val) {
		String text2 = val.replace("\\\\r\\\\n", "\r\n").replace("\\\\n\\\\r", "\n\r");
		text2 = text2.replace("\\\"", "\"");
		text2 = text2.replace("\\\\", "\\");
		return text2;
	}

	private static Date getDateField(List<String> list, int i) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String field = getField(list, i, false);
		if (field == null) {
			return null;
		}
		if (field.isEmpty()) {
			return null;
		}
		return dateFormat.parse(field);
	}

}
