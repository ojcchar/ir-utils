package seers.utils.islandparsing;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.usi.inf.reveal.parsing.stormed.service.StormedClientJavaExample;
import edu.wayne.cs.severe.ir4se.processor.entity.Query;
import edu.wayne.cs.severe.ir4se.processor.utils.ParameterUtils;
import seers.irutils.CsvIssue;
import seers.irutils.LegacyDataParser;
import seers.utils.pos.MainBugPOSTagger;

public class MainIslandParsingQueries {

	private static String outFolder;

	public static void main(String[] args) throws Exception {

		String[] systems = args[0].split(",");
		String irFolderBase = args[1];
		outFolder = args[2];

		LegacyDataParser parser = new LegacyDataParser();

		Map<String, String> params = new HashMap<>();
		params.put(ParameterUtils.BASE_DIR, irFolderBase);

		String fileSuffix = "_Queries_parsed.txt";

		for (String sys : systems) {

			System.out.println("Processing " + sys);

			params.put(ParameterUtils.SYSTEM, sys);

			List<CsvIssue> qInfo = MainBugPOSTagger.readIssues(ParameterUtils.getQueriesFileInfoPath(params), false);
			List<Query> queries = parser.readQueries(ParameterUtils.getQueriesFilePath(params));

			File file = new File(getFileName(sys, outFolder, fileSuffix));
			file.delete();

			for (Query query : queries) {
				int i = qInfo.indexOf(new CsvIssue((String) query.getInfoAttribute(Query.ISSUE_ID)));
				CsvIssue csvIssue = qInfo.get(i);
				processIssue(sys, csvIssue, outFolder, fileSuffix);
			}

		}

	}

	static void processIssue(String sys, CsvIssue csvIssue, String outBaseFolder, String fileSuffix) throws Exception {
		String summary = csvIssue.summary;
		String description = csvIssue.description;

		String text = summary + ". " + description;

		String isolated = isolateText(text);

		File file = new File(getFileName(sys, outBaseFolder, fileSuffix));
		String line = "\"" + csvIssue.key + "\";\"" + isolated + "\"\r\n";
		org.apache.commons.io.FileUtils.write(file, line, true);

	}

	static String isolateText(String text) {

		if (text == null) {
			return null;
		}

		// *************************************
		String noCodeTxt = deleteNewLinesAndTags(text);
		String isolated = StormedClientJavaExample.isolateText(noCodeTxt);
		isolated = isolated.replace("\r\n", "\\\\r\\\\n").replace("\n\r", "\\\\n\\\\r");
		isolated = isolated.replace("\"", "\\\"");
		return isolated;
	}

	static String getFileName(String sys, String outBaseFolder, String fileSuffix) {
		return outBaseFolder + File.separator + sys + fileSuffix;
	}

	public static String deleteNewLinesAndTags(final String text) {
		String text2 = text.replace("\\\\r\\\\n", "\r\n").replace("\\\\n\\\\r", "\n\r");
		text2 = text2.replace("\\\"", "\"");

		// *************************************

		final String tagName = "code";
		final String tagAttrs = "(:[=a-zA-Z.\\|\\d\\(\\)\\s/\\\\_<>\\?#\\-\\'\"@]+)";

		String noNewLines = text2.replaceAll("\\R", " ");
		String noTags = noNewLines.replace("{quote}", "").replace("{noformat}", "").replace("{color}", "")
				.replaceAll("\\{color:[a-zA-Z]+\\}", "").replaceAll("\\{panel" + tagAttrs + "?\\}", "")
				.replace("{panel}", "");

		// ---------------------------

		StringBuilder regexBuilder = new StringBuilder();
		String leftTag = "\\{" + tagName + tagAttrs + "?\\}";
		String tagContent = "(.+?)";
		String rightTag = "\\{" + tagName + "\\}";
		regexBuilder.append(leftTag);
		regexBuilder.append(tagContent);
		regexBuilder.append(rightTag);

		// ---------------------------

		String noCodeTxt = noTags.replaceAll(regexBuilder.toString(), "");
		return noCodeTxt;
	}

}
