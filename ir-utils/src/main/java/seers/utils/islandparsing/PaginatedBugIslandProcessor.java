package seers.utils.islandparsing;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wayne.cs.severe.ir4se.processor.utils.ExceptionUtils;
import seers.appcore.threads.processor.ThreadException;
import seers.appcore.threads.processor.ThreadProcessor;
import seers.irutils.CsvIssue;

public class PaginatedBugIslandProcessor implements ThreadProcessor {
	Logger LOGGER;

	private List<CsvIssue> issuesSubList;
	private String sys, outFolder, fileSuffix;

	public PaginatedBugIslandProcessor(List<CsvIssue> issuesSubList, String sys, String outFolder, String fileSuffix) {
		super();
		this.issuesSubList = issuesSubList;
		this.sys = sys;
		this.outFolder = outFolder;
		this.fileSuffix = fileSuffix;

		LOGGER = LoggerFactory.getLogger(sys);
	}

	@Override
	public void processJob() throws ThreadException {
		try {
			for (CsvIssue csvIssue : issuesSubList) {
				// if (!csvIssue.key.equals("BOOKKEEPER-62")) {
				// continue;
				// }

				processIssue(sys, csvIssue, outFolder, fileSuffix);

			}
		} catch (Exception e) {
			ThreadException e2 = new ThreadException(e.getClass().getSimpleName() + ": " + e.getMessage());
			ExceptionUtils.addStackTrace(e, e2);
			throw e2;
		}
	}

	void processIssue(String sys, CsvIssue csvIssue, String outBaseFolder, String fileSuffix) throws Exception {
		String isolatedSummary = csvIssue.summary;
		try {
			isolatedSummary = MainIslandParsingQueries.isolateText(csvIssue.summary);
		} catch (Exception e) {
			LOGGER.error("Error for issue - summary: " + csvIssue.key + ", [" + sys + "]", e);
		}

		String isolatedDescription = csvIssue.description;
		try {
			isolatedDescription = MainIslandParsingQueries.isolateText(csvIssue.description);
		} catch (Exception e) {
			LOGGER.error("Error for issue - description: " + csvIssue.key + ", [" + sys + "]", e);
		}

		File file = new File(MainIslandParsingQueries.getFileName(sys, outBaseFolder, fileSuffix));
		String line = "\"" + csvIssue.key + "\";\"" + (isolatedSummary == null ? "" : isolatedSummary) + "\";\""
				+ (isolatedDescription == null ? "" : isolatedDescription) + "\"\r\n";
		org.apache.commons.io.FileUtils.write(file, line, true);
	}

	@Override
	public String getName() {
		return PaginatedBugIslandProcessor.class.getSimpleName() + "-" + sys;
	}

}
