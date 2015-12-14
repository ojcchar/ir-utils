package seers.utils.islandparsing;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.wayne.cs.severe.ir4se.processor.utils.ExceptionUtils;
import edu.wayne.cs.severe.ir4se.processor.utils.ParameterUtils;
import seers.appcore.threads.CommandLatchRunnable;
import seers.appcore.threads.ThreadCommandExecutor;
import seers.appcore.threads.processor.ThreadException;
import seers.appcore.threads.processor.ThreadProcessor;
import seers.irutils.CsvIssue;
import seers.utils.pos.MainBugPOSTagger;

public class PaginatedSysBugIslandProcessor implements ThreadProcessor {

	Logger LOGGER;

	private String sys;
	private String outFolder;
	private Map<String, String> params;
	private String fileSuffix;

	public PaginatedSysBugIslandProcessor(String sys, String outFolder, Map<String, String> params, String fileSuffix) {
		this.sys = sys;
		this.outFolder = outFolder;
		this.params = new HashMap<>(params);
		this.fileSuffix = fileSuffix;

		LOGGER = LoggerFactory.getLogger(sys);
	}

	@Override
	public void processJob() throws ThreadException {

		try {
			LOGGER.debug("Processing " + sys);

			params.put(ParameterUtils.SYSTEM, sys);

			File file = new File(MainIslandParsingQueries.getFileName(sys, outFolder, fileSuffix));
			file.delete();

			String path = ParameterUtils.getQueriesFileInfoPath(params);
			// LOGGER.debug("file " + path);

			List<CsvIssue> qInfo = MainBugPOSTagger.readIssues(path, false);
			processQInfo(sys, qInfo);

			LOGGER.debug("Done " + sys);
		} catch (Exception e) {
			ThreadException e2 = new ThreadException(e.getClass().getSimpleName() + ": " + e.getMessage());
			ExceptionUtils.addStackTrace(e, e2);
			throw e2;
		}
	}

	@Override
	public String getName() {
		return sys;
	}

	private void processQInfo(String sys, List<CsvIssue> qInfo) throws Exception {

		// ------------------------------------

		ThreadCommandExecutor executor = new ThreadCommandExecutor();
		executor.setCorePoolSize(5);
		try {

			// create the threads
			List<ThreadProcessor> procs = new ArrayList<>();
			int num = qInfo.size();
			int pageSize = 50;
			for (int offset = 0; offset < num; offset += pageSize) {

				int fromIndex = offset;
				int toIndex = offset + pageSize;
				if (toIndex >= num) {
					toIndex = num;
				}
				List<CsvIssue> issuesSubList = qInfo.subList(fromIndex, toIndex);

				procs.add(new PaginatedBugIslandProcessor(issuesSubList, sys, outFolder, fileSuffix));
			}

			// run the threads
			CountDownLatch cntDwnLatch = new CountDownLatch(procs.size());
			for (ThreadProcessor proc : procs) {
				executor.executeCommRunnable(new CommandLatchRunnable(proc, cntDwnLatch));
			}
			cntDwnLatch.await();

		} finally {
			executor.shutdown();
		}

	}

}
