package gradeskope.process;

import gradeskope.AgentManager;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class MagicManager {

  private static final String JAVA_HOME = "java.home";
  private static final String OS_NAME = "os.name";
  private static final String CLASS_PATH_ARGUMENT = "-cp";
  private static final int SUCCESSFUL_ATTACH = 0;

//    private static Unsafe unsafe;
//
//    private static long constructorModifiersOffset;
//
//    private static long methodModifiersOffset;
//
//    private static long fieldModifiersOffset;
//
//    static
//    {
//        try {
//            Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
//            unsafeConstructor.setAccessible(true);
//            unsafe = unsafeConstructor.newInstance();
//            Field constructorModifiers = Constructor.class.getDeclaredField("modifiers");
//            long constructorModifiersOffset = unsafe.objectFieldOffset(constructorModifiers);
//            Field methodModifiers = Method.class.getDeclaredField("modifiers");
//            long methodModifiersOffset = unsafe.objectFieldOffset(methodModifiers);
//            Field fieldModifiers = Field.class.getDeclaredField("modifiers");
//            long fieldModifiersOffset = unsafe.objectFieldOffset(fieldModifiers);
//            Method setAccessible = AccessibleObject.class.getDeclaredMethod("setAccessible0", boolean.class);
//            setForceAccessible(setAccessible);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

  private static final RuntimeMXBean runtimeMX = ManagementFactory.getRuntimeMXBean();
  private static final OperatingSystemMXBean osMX = ManagementFactory.getOperatingSystemMXBean();

  public static String getJavaVersion() {
    return runtimeMX.getSpecVersion();
  }

  public static String getJavaInfo() {
    return "Java " + runtimeMX.getSpecVersion() + " (" + runtimeMX.getVmName() + " " +
        runtimeMX.getVmVersion() + ")";
  }

  public static String getOsInfo() {
    return "Host: " + osMX.getName() + " " + osMX.getVersion() + " (" + osMX.getArch() + ")";
  }

  public static boolean isRemote() {
    return getOsInfo().toLowerCase().contains("amzn");
  }

  public static void attachAgentToVM() {
    if (MagicManager.isRemote()) {

      MagicManager.attachAgentToVM(AgentManager.TMP_DIR + AgentManager.AGENT_JAR,
          AgentManager.AGENT_ARGS);
    } else {
      MagicManager.attachAgentToVM(AgentManager.AGENT_LOCAL_SRC, AgentManager.AGENT_ARGS);
    }
  }

  public static void attachAgentToVM(String pathToAgentJar, String agentArgs) {

    String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
    String pid = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));


    String toolsFilePath = null;
    // The servers run Amazon Linux, which we are (poorly) checking for here
    if (MagicManager.isRemote()) {
      toolsFilePath = "/usr/lib/jvm/java-8-openjdk-amd64/lib/tools.jar"; // Location on remote
    } else {
      toolsFilePath = "/usr/lib/jvm/java-8-openjdk/lib/tools.jar"; // Location on my test machine
    }
    File toolsJarFile = new File(toolsFilePath);

    // Load tools.jar so VirtualMachine instance can be created
    URLClassLoader child = null;
    try {
      child = new URLClassLoader(
          new URL[] {toolsJarFile.toURI().toURL()},
          MagicManager.class.getClassLoader()
      );
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    // Load tools.jar file, to access VirtualMachine
    try {
      // Dirty hack for Java 11 support (assuming Java 8 is installed on the system, because its more effort otherwise)
      // We work around selfAttach being off by default by creating a subprocess
      // That then attaches our agent to this VM
      // For this to work on your local machine, you must add ProcessWrapper's path to the classpath
      if (!MagicManager.getJavaVersion().equals("1.8")) {
        List<String> jvmArgs = new ArrayList<String>();
        List<String> args = new ArrayList<String>();
        args.add(pid);
        args.add(pathToAgentJar);
        args.add(agentArgs);
        args.add(toolsFilePath);

        System.out.println(System.getProperty("java.class.path"));
        ProcessBuilder processBuilder = execJavaClass(ProcessWrapper.class, jvmArgs, args);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process process = processBuilder.start();
        int returnCode = process.waitFor();
        if (returnCode != SUCCESSFUL_ATTACH) {
          throw new IllegalStateException(
              "Could not self-attach to current VM using external process");
        }

      } else {
        attachAgentToVM(pathToAgentJar, child, agentArgs, pid);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void attachAgentToVM(String pathToAgentJar, URLClassLoader child,
                                     String agentArgs, String mainProcessID)
      throws InvocationTargetException, IllegalAccessException, ClassNotFoundException,
      NoSuchMethodException {
    Class classToLoad = Class.forName("com.sun.tools.attach.VirtualMachine", true, child);
    Method vmStaticAttach = classToLoad.getDeclaredMethod("attach", String.class);
    Method vmInstanceLoadAgent =
        classToLoad.getDeclaredMethod("loadAgent", String.class, String.class);
    Method vmInstanceDetach = classToLoad.getDeclaredMethod("detach");
    Object vmClassInstance = vmStaticAttach.invoke(null,
        mainProcessID); //Get VMClassInstance by calling VirtualMachine.attach(pid), a static method
    vmInstanceLoadAgent.invoke(vmClassInstance, pathToAgentJar,
        agentArgs); // Load Java Agent using vmClassInstance as the VirtualMachine vm Object
    vmInstanceDetach.invoke(
        vmClassInstance); // Detach Instance, execute with vmClassInstance as the Object

  }

  public static ProcessBuilder execJavaClass(Class clazz, List<String> jvmArgs, List<String> args) {
    String javaHome = System.getProperty("java.home");
    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
    String classpath = System.getProperty("java.class.path");
    String className = clazz.getName();

    List<String> command = new ArrayList<>();
    command.add(javaBin);
    command.addAll(jvmArgs);
    command.add("-cp");
    command.add(classpath);
    command.add(className);
    command.addAll(args);

    return new ProcessBuilder(command);
  }
}
