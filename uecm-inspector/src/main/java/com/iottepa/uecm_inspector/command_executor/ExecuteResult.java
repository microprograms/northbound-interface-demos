package com.iottepa.uecm_inspector.command_executor;

public class ExecuteResult {
    private int exitCode;
    private String executeOutput;
    private String executeErrorOutput;

    public ExecuteResult(int exitCode, String executeOutput, String executeErrorOutput) {
        this.exitCode = exitCode;
        this.executeOutput = executeOutput;
        this.executeErrorOutput = executeErrorOutput;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getExecuteOutput() {
        return executeOutput;
    }

    public String getExecuteErrorOutput() {
        return executeErrorOutput;
    }
}
