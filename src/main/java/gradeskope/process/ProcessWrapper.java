package gradeskope.process;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ProcessWrapper {
  public static void main(String[] args) {
    String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
    String subProcessID = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
    String mainProcessID = args[0];
    String pathToAgentJar = args[1];
    String agentArgs = args[2];
    File toolsJarFile = new File(args[3]);

    URLClassLoader child = null;
    try {
      child = new URLClassLoader(
          new URL[] {toolsJarFile.toURI().toURL()},
          ProcessWrapper.class.getClassLoader()
      );
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }


    System.out.println("Main Process: " + mainProcessID);

    System.out.println("Subprocess: " + subProcessID);

    try {
      MagicManager.attachAgentToVM(pathToAgentJar, child, agentArgs, mainProcessID);
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

}
