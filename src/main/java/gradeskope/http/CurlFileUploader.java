package gradeskope.http;

import java.io.File;
import java.io.IOException;

public class CurlFileUploader implements FileUploader, Runnable {

  private final String url;
  private final String[] headers;
  private final File binaryFile;
  private int exitCode;

  public CurlFileUploader(String url, String[] headers, File binaryFile) {
    this.url = url;
    this.headers = headers;
    this.binaryFile = binaryFile;
  }

  @Override
  public void run() {
    String command = String.format("curl -F uploads=@%s %s", binaryFile.getPath(), url);
    ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
    Process process = null;
    try {
      process = processBuilder.start();
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
    this.exitCode = process.exitValue();
  }

  @Override
  public int getExitCode() {
    return this.exitCode;
  }
}
