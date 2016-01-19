package seers.irutils.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import edu.wayne.cs.severe.ir4se.processor.utils.ParameterUtils;
import seers.appcore.threads.CommandLatchRunnable;
import seers.appcore.threads.ThreadCommandExecutor;
import seers.appcore.threads.processor.ThreadProcessor;

public class POSTermStats {

	public static void main(String[] args) throws Exception {
		String[] systems = args[0].split(",");
		String irFolderBase = args[1];
		String outFolder = args[2];

		Map<String, String> params = new HashMap<>();
		params.put(ParameterUtils.BASE_DIR, irFolderBase);

		ThreadCommandExecutor executor = new ThreadCommandExecutor();
		executor.setCorePoolSize(4);
		try {

			// create the threads
			List<ThreadProcessor> procs = new ArrayList<>();

			for (String sys : systems) {
				procs.add(new PaginatedPOSTermStats(sys, outFolder, params));
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
