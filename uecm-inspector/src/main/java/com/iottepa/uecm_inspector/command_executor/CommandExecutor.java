package com.iottepa.uecm_inspector.command_executor;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private static ExecutorService pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 3L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    public static boolean existsShellFile(String shellFileWithoutSuffix) {
        return new File(String.format("/command/%s.sh", shellFileWithoutSuffix)).exists();
    }

    public static String executeShellFile(String shellFileWithoutSuffix, long timeoutMilliseconds) {
        if (!existsShellFile(shellFileWithoutSuffix)) {
            return String.format("execute shell file error, /command/%s.sh not exists", shellFileWithoutSuffix);
        }

        String outputFile = CommandExecutor.randomOutputFile(shellFileWithoutSuffix);
        try {
            String command = String.format("/bin/sh /command/%s.sh --output=%s", shellFileWithoutSuffix, outputFile);
            CommandExecutor.executeCommand(command, 60 * 1000);
            return new StringBuffer(shellFileWithoutSuffix + ">").append("\n")
                    .append(CommandExecutor.getOutputString(outputFile)).toString();
        } finally {
            CommandExecutor.deleteOutputFile(outputFile);
        }
    }

    public static ExecuteResult executeCommand(String command, long timeoutMilliseconds) {
        Future<Integer> executeFuture = null;
        StreamGobbler outputGobbler = null;
        StreamGobbler errorGobbler = null;
        try {
            logger.info("executeCommand: command={}, timeoutMilliseconds={}", command, timeoutMilliseconds);
            final Process p = Runtime.getRuntime().exec(command);
            p.getOutputStream().close();

            outputGobbler = new StreamGobbler(p.getInputStream());
            outputGobbler.start();

            errorGobbler = new StreamGobbler(p.getErrorStream());
            errorGobbler.start();

            executeFuture = pool.submit(() -> {
                p.waitFor();
                return p.exitValue();
            });
            int exitCode = executeFuture.get(timeoutMilliseconds, TimeUnit.MILLISECONDS);
            String executeOutput = outputGobbler.getContent();
            String executeErrorOutput = errorGobbler.getContent();
            if (StringUtils.isNotBlank(executeErrorOutput)) {
                logger.error("executeCommand: executeErrorOutput={}", executeErrorOutput);
            }
            return new ExecuteResult(exitCode, executeOutput, executeErrorOutput);
        } catch (Throwable e) {
            logger.error("executeCommand: error", e);
            return new ExecuteResult(-1, null, null);
        } finally {
            if (null != executeFuture) {
                executeFuture.cancel(true);
            }
            if (null != outputGobbler) {
                outputGobbler.closeQuietly();
            }
            if (null != errorGobbler) {
                errorGobbler.closeQuietly();
            }
        }
    }

    public static String randomOutputFile(String prefix) {
        return String.format("%s-%s", prefix, UUID.randomUUID().toString());
    }

    public static List<String> getOutput(String outputFile) {
        try {
            return IOUtils.readLines(new FileReader(outputFile));
        } catch (Throwable e) {
            logger.error("getOutput: error", e);
            return Collections.emptyList();
        }
    }

    public static String getOutputString(String outputFile) {
        return StringUtils.join(getOutput(outputFile), "\n");
    }

    public static boolean deleteOutputFile(String outputFile) {
        return new File(outputFile).delete();
    }
}