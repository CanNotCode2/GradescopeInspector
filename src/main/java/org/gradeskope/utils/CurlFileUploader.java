package org.gradeskope.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CurlFileUploader implements FileUploader {
  public CurlFileUploader(String url, String[] headers, File binaryFile) {
    String command = String.format("curl -F uploads=@%s %s", binaryFile.getPath(), url);
    ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
    Process process = null;
    try {
      process = processBuilder.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    int exitCode = process.exitValue();
    System.out.println(exitCode);
  }
}
