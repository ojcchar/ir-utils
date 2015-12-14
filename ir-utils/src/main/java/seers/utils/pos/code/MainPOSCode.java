package seers.utils.pos.code;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import seers.appcore.threads.CommandLatchRunnable;
import seers.appcore.threads.ThreadCommandExecutor;
import seers.appcore.threads.processor.ThreadProcessor;
import seers.utils.pos.code.control.PaginatedFileProcessor;

public class MainPOSCode {

	private static String projectFolder;
	private static String inFolder;
	private static String[] sourceSubFolders;
	private static String projectFolderName;
	private static List<String> stopWords;
	private static File outFilePOS;
	private static File outFileNoPOS;

	public static void main(String[] args) throws Exception {
		inFolder = args[0];
		sourceSubFolders = args[1].split(";");
		projectFolderName = args[2];
		String stopWordsFile = args[3];
		String projectSysName = args[4];
		outFilePOS = new File(args[5] + File.separator + projectSysName + "_Corpus_pos.txt");
		outFileNoPOS = new File(args[5] + File.separator + projectSysName + "_Corpus.txt");

		outFilePOS.delete();
		outFileNoPOS.delete();

		projectFolder = inFolder + File.separator + projectFolderName;

		stopWords = org.apache.commons.io.FileUtils.readLines(new File(stopWordsFile));

		File projectDir = new File(projectFolder);
		LinkedList<File> allFiles = (LinkedList<File>) FileUtils.listFiles(projectDir, new String[] { "java" }, true);

		processAllFiles(allFiles);
	}

	private static void processAllFiles(LinkedList<File> allFiles) throws InterruptedException {

		HashMap<String, String> subFoldPrefixes = buildSubFoldPrefixes();

		ThreadCommandExecutor executor = new ThreadCommandExecutor();
		executor.setCorePoolSize(5);
		try {

			// create the threads
			List<PaginatedFileProcessor> procs = new ArrayList<>();
			int num = allFiles.size();
			int pageSize = 50;
			for (int offset = 0; offset < num; offset += pageSize) {
				procs.add(new PaginatedFileProcessor(offset, offset + pageSize, allFiles, projectFolder,
						new String[] { projectFolder }, sourceSubFolders, stopWords, outFilePOS, outFileNoPOS,
						subFoldPrefixes));
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

	private static HashMap<String, String> buildSubFoldPrefixes() {
		HashMap<String, String> map = new LinkedHashMap<>();
		for (int i = 0; i < sourceSubFolders.length; i++) {
			String key = FilenameUtils.separatorsToSystem(projectFolder + File.separator + sourceSubFolders[i]);
			String value = FilenameUtils.separatorsToSystem(sourceSubFolders[i]).replace(File.separator, ".");
			map.put(key, value);
		}
		return map;
	}

}
