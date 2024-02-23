package com.iottepa.uecm_inspector.command_executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamGobbler extends Thread {
    private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);

    private final InputStream inputStream;
    private final StringBuilder buf = new StringBuilder();
    private volatile boolean isStopped = false;

    public StreamGobbler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                buf.append(line + "\n");
            }
        } catch (IOException e) {
            logger.error("error", e);
        } finally {
            isStopped = true;
            synchronized (this) {
                notify();
            }
        }
    }

    public String getContent() {
        if (!isStopped) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ignore) {
                    logger.error("error", ignore);
                }
            }
        }
        return buf.toString();
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void closeQuietly() {
        try {
            inputStream.close();
        } catch (IOException e) {
            logger.error("closeQuietly error", e);
        }

        if (!isInterrupted()) {
            interrupt();
        }
    }
}
