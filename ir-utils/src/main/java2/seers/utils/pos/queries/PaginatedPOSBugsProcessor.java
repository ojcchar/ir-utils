package seers.utils.pos.queries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wayne.cs.severe.ir4se.processor.utils.ExceptionUtils;
import edu.wayne.cs.severe.ir4se.processor.utils.ParameterUtils;
import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;
import net.quux00.simplecsv.CsvWriter;
import net.quux00.simplecsv.CsvWriterBuilder;
import seers.appcore.threads.processor.ThreadException;
import seers.appcore.threads.processor.ThreadProcessor;
import seers.irutils.CsvIssue;
import seers.utils.pos.MainBugPOSTagger;
import seers.utils.pos.POSToken;

public class PaginatedPOSBugsProcessor implements ThreadProcessor {

	private String sys, outFolder;
	private Map<String, String> params;
	private List<String> stopWords;

	Logger LOGGER;

	public PaginatedPOSBugsProcessor(String sys, String outFolder, Map<String, String> params, List<String> stopWords) {
		super();
		this.sys = sys;
		this.outFolder = outFolder;
		this.params = new HashMap<>(params);
		this.stopWords = stopWords;
		LOGGER = LoggerFactory.getLogger(sys);
	}

	@Override
	public void processJob() throws ThreadException {

		try {

			LOGGER.debug("Processing " + sys);
			params.put(ParameterUtils.SYSTEM, sys);

			List<CsvIssue> issue = readIssues(ParameterUtils.getFilePathPrefix(params) + "_Queries_info_parsed.txt");

			HashMap<String, List<POSToken>> summaryTokens = new HashMap<>();
			HashMap<String, List<POSToken>> descriptionTokens = new HashMap<>();

			for (CsvIssue csvIssue : issue) {

				List<POSToken> posTokensSummary = MainPOSQueries.getPOStokens(csvIssue.summary, null, stopWords);
				summaryTokens.put(csvIssue.key, posTokensSummary);

				List<POSToken> posTokensDescription = MainPOSQueries.getPOStokens(csvIssue.description, null,
						stopWords);
				descriptionTokens.put(csvIssue.key, posTokensDescription);

			}

			List<CsvIssue> originalIssues = MainBugPOSTagger.readIssues(ParameterUtils.getQueriesFileInfoPath(params),
					true);

			File file = new File(outFolder + File.separator + sys + "_Queries_info_pos.txt");
			file.delete();
			try (CsvWriter csvw = new CsvWriterBuilder(new FileWriter(file)).separator(';').build()) {
				updateQueriesInfo(originalIssues, summaryTokens, descriptionTokens, true, csvw);
			}

			file = new File(outFolder + File.separator + sys + "_Queries_info.txt");
			file.delete();
			try (CsvWriter csvw = new CsvWriterBuilder(new FileWriter(file)).separator(';').build()) {
				updateQueriesInfo(originalIssues, summaryTokens, descriptionTokens, false, csvw);
			}

			LOGGER.debug("Done " + sys);

		} catch (Exception e) {
			ThreadException e2 = new ThreadException(e.getClass().getSimpleName() + ": " + e.getMessage());
			ExceptionUtils.addStackTrace(e, e2);
			throw e2;
		}
	}

	private void updateQueriesInfo(List<CsvIssue> originalIssues, HashMap<String, List<POSToken>> summaryTokens,
			HashMap<String, List<POSToken>> descriptionTokens, boolean includePos, CsvWriter csvw) {

		for (CsvIssue csvIssue : originalIssues) {

			// if (!"BOOKKEEPER-232".equals(csvIssue.key)) {
			// continue;
			// }

			LinkedList<String> tokenList = MainPOSQueries.getTokenList(summaryTokens.get(csvIssue.key), includePos);
			csvIssue.summaryParsed = tokenList.isEmpty() ? null : MainPOSQueries.tokensToTxt(tokenList);

			tokenList = MainPOSQueries.getTokenList(descriptionTokens.get(csvIssue.key), includePos);
			csvIssue.descriptionParsed = tokenList.isEmpty() ? null : MainPOSQueries.tokensToTxt(tokenList);

			List<String> nextLine = getNextLine(csvIssue);
			csvw.writeNext(nextLine);
		}

	}

	private List<String> getNextLine(CsvIssue csvIssue) {
		List<String> line = new ArrayList<>();
		line.add(getFieldName(csvIssue.id));
		line.add(getFieldName(csvIssue.key));
		line.add(getFieldName(csvIssue.issueType));
		line.add(getFieldName(csvIssue.fixVersions));
		line.add(getFieldName(csvIssue.resolution));
		line.add(getDateFormat(csvIssue.resolutionDate));
		line.add(getDateFormat(csvIssue.created));
		line.add(getFieldName(csvIssue.priority));
		line.add(getDateFormat(csvIssue.updated));
		line.add(getFieldName(csvIssue.status));
		line.add(getFieldName(csvIssue.components));
		line.add(getFieldName(csvIssue.description));
		line.add(getFieldName(csvIssue.descriptionParsed));
		line.add(getFieldName(csvIssue.summary));
		line.add(getFieldName(csvIssue.summaryParsed));
		line.add(getFieldName(csvIssue.creator));
		line.add(getFieldName(csvIssue.reporter));
		return line;
	}

	private String getFieldName(String val) {
		if (val == null) {
			return "NA";
		}
		val = val.replace("\r\n", "\\r\\n").replace("\n\r", "\\n\\r");
		return val;
	}

	private String getDateFormat(Date date) {
		if (date == null) {
			return "NA";
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		return dateFormat.format(date);
	}

	private List<CsvIssue> readIssues(String fileIn) throws IOException {

		List<CsvIssue> infoList = new ArrayList<>();

		CsvParser csvParser = new CsvParserBuilder().separator(';').build();
		try (CsvReader csvReader = new CsvReader(new FileReader(fileIn), csvParser)) {

			List<List<String>> readAll = csvReader.readAll();

			for (List<String> list : readAll) {

				CsvIssue info = new CsvIssue();

				info.key = list.get(0);
				info.summary = list.get(1);
				info.description = list.get(2);

				infoList.add(info);

			}

		}
		return infoList;
	}

	@Override
	public String getName() {
		return sys;
	}

}
