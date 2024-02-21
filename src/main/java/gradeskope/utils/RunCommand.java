package gradeskope.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunCommand {

    private Runtime rt = Runtime.getRuntime();
    private String[] command;

    private String stdout = "";
    private String stderr = "";

    private boolean executionCompleted = false;
    RunCommand(String[] command) {

        this.command = command;
    }

    public void execute() {
        try {
            Process process = rt.exec(command);
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(process.getErrorStream()));

            String output = null;
            while ((output = stdInput.readLine()) != null) {
                stdout = stdout + output + "\n";
            }

            String errorOutput = null;
            while ((errorOutput = stdError.readLine()) != null) {
                stderr = stderr + errorOutput + "\n";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        executionCompleted = true;
    }

    public String getStdout() {
        if (!executionCompleted) {
            throw new RuntimeException("Command has not finished running");
        }
        return stdout;
    }

    public String getStderr() {
        if (!executionCompleted) {
            throw new RuntimeException("Command has not finished running");
        }
        return stderr;
    }
}
