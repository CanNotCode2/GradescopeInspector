package gradeskope.process;

import java.util.Optional;

// Only works on Java 9+, should work on all platforms
// https://stackoverflow.com/questions/54686/how-to-get-a-list-of-current-open-windows-process-with-java
public class Java9UniversalProcessHandler implements ProcessFinder {

  public static void main(String[] args) {

    Java9UniversalProcessHandler processHandler = new Java9UniversalProcessHandler();

    Long pid = processHandler.findTargetPID("");
    System.out.println(pid);
  }

  @Override
  public Long findTargetPID(final String matchString) {
    ProcessHandle returnProcess = ProcessHandle.allProcesses().filter(process -> {
      return text(process.info().commandLine()).contains(matchString);
    }).findFirst().orElse(null);
    return returnProcess == null ? null : returnProcess.pid();
  }

  private String text(Optional<?> optional) {
    return optional.map(Object::toString).orElse("-");
  }
}
