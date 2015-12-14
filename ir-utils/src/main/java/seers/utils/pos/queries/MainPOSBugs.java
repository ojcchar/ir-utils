package seers.utils.pos.queries;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import edu.wayne.cs.severe.ir4se.processor.utils.ParameterUtils;
import preprocessor2.QueryPreprocessor2;
import seers.appcore.threads.CommandLatchRunnable;
import seers.appcore.threads.ThreadCommandExecutor;
import seers.appcore.threads.processor.ThreadProcessor;

public class MainPOSBugs {

	private static List<String> stopWords;
	static QueryPreprocessor2 processor = new QueryPreprocessor2();
	private static String outFolder;
	static boolean includePos = true;

	public static void main(String[] args) throws Exception {

		String[] systems = args[0].split(",");
		String irFolderBase = args[1];
		String stopWordsFile = args[2];
		outFolder = args[3];

		stopWords = org.apache.commons.io.FileUtils.readLines(new File(stopWordsFile));

		Map<String, String> params = new HashMap<>();
		params.put(ParameterUtils.BASE_DIR, irFolderBase);

		// --------------------------

		ThreadCommandExecutor executor = new ThreadCommandExecutor();
		executor.setCorePoolSize(10);
		try {

			// create the threads
			List<PaginatedPOSBugsProcessor> procs = new ArrayList<>();

			for (String sys : systems) {
				procs.add(new PaginatedPOSBugsProcessor(sys, outFolder, params, stopWords));
			}

			// run the threads
			CountDownLatch cntDwnLatch = new CountDownLatch(procs.size());
			for (ThreadProcessor proc : procs) {
				executor.executeCommRunnable(new CommandLatchRunnable(proc, cntDwnLatch, (long) procs.size()));
			}
			cntDwnLatch.await();

		} finally {
			executor.shutdown();
		}

	}
}
