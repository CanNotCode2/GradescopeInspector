package gradeskope.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
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

    private static RuntimeMXBean runtimeMX = ManagementFactory.getRuntimeMXBean();
    private static OperatingSystemMXBean osMX = ManagementFactory.getOperatingSystemMXBean();

    public static String getJavaVersion() {
        return runtimeMX.getSpecVersion();
    }
    public static String getJavaInfo() {
        return "Java " + runtimeMX.getSpecVersion() + " (" + runtimeMX.getVmName() + " " + runtimeMX.getVmVersion() + ")";
    }

    public static String getOsInfo() {
        return "Host: " + osMX.getName() + " " + osMX.getVersion() + " (" + osMX.getArch() + ")";
    }

    public static boolean isRemote() {
        if (getOsInfo().toLowerCase().contains("amzn")) {
            return true;
        } else {
            return false;
        }
    }

    public static void attachGivenAgentToThisVM(String pathToAgentJar, String agentArgs) {

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
                    new URL[]{toolsJarFile.toURI().toURL()},
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
//                    jvmArgs.add("");
                List<String> args = new ArrayList<String>();
                    args.add(pid.toString());
                    args.add(pathToAgentJar);
                    args.add(agentArgs);
                    args.add(toolsFilePath);
//                ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/java", "-version");
                System.out.println(System.getProperty("java.class.path"));
                ProcessBuilder processBuilder = exec(gradeskope.utils.ProcessWrapper.class, jvmArgs, args);
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

                // Thank you ByteBuddy, I stole a few lines from you
                Process process = processBuilder.start();
                int returnCode = process.waitFor();
                if (returnCode != SUCCESSFUL_ATTACH) {
                    throw new IllegalStateException("Could not self-attach to current VM using external process");
                }

            } else {
                ProcessWrapper.attachGivenAgentToThisVM(pathToAgentJar, child, agentArgs, pid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ProcessBuilder exec(Class clazz, List<String> jvmArgs, List<String> args) {
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

//    private static void installExternal(ByteBuddyAgent.AttachmentProvider.Accessor.ExternalAttachment externalAttachment,
//                                        String processId,
//                                        File agent,
//                                        boolean isNative,
//                                        @MaybeNull String argument) throws Exception {
//        File selfResolvedJar = trySelfResolve(), attachmentJar = null;
//        try {
//            StringBuilder classPath = new StringBuilder().append((selfResolvedJar == null
//                    ? attachmentJar
//                    : selfResolvedJar).getCanonicalPath());
//            for (File jar : externalAttachment.getClassPath()) {
//                classPath.append(File.pathSeparatorChar).append(jar.getCanonicalPath());
//            }
//            if (new ProcessBuilder(System.getProperty(JAVA_HOME)
//                    + File.separatorChar + "bin"
//                    + File.separatorChar + (System.getProperty(OS_NAME, "").toLowerCase(Locale.US).contains("windows") ? "java.exe" : "java"),
//                    "-D" + Attacher.DUMP_PROPERTY + "=" + System.getProperty(Attacher.DUMP_PROPERTY, ""),
//                    CLASS_PATH_ARGUMENT,
//                    classPath.toString(),
//                    Attacher.class.getName(),
//                    externalAttachment.getVirtualMachineType(),
//                    processId,
//                    agent.getAbsolutePath(),
//                    Boolean.toString(isNative),
//                    argument == null ? "" : ("=" + argument)).start().waitFor() != SUCCESSFUL_ATTACH) {
//                throw new IllegalStateException("Could not self-attach to current VM using external process");
//            }
//        } finally {
//            if (attachmentJar != null) {
//                if (!attachmentJar.delete()) {
//                    attachmentJar.deleteOnExit();
//                }
//            }
//        }
//    }



//    private static boolean setForceAccessible(AccessibleObject accessibleObject)
//    {
//        try
//        {
//            if (accessibleObject instanceof Constructor)
//            {
//                Constructor<?> object = (Constructor<?>) accessibleObject;
//                unsafe.getAndSetInt(object, constructorModifiersOffset, addPublicModifier(object.getModifiers()));
//                return true;
//            }
//            if (accessibleObject instanceof Method)
//            {
//                Method object = (Method) accessibleObject;
//                unsafe.getAndSetInt(object, methodModifiersOffset, addPublicModifier(object.getModifiers()));
//                return true;
//            }
//            if (accessibleObject instanceof Field)
//            {
//                Field object = (Field) accessibleObject;
//                unsafe.getAndSetInt(object, fieldModifiersOffset, addPublicModifier(object.getModifiers()));
//                return true;
//            }
//            return false;
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    private static int addPublicModifier(int mod)
//    {
//        mod &= ~ (Modifier.PRIVATE);
//        mod &= ~ (Modifier.PROTECTED);
//        mod |= (Modifier.PUBLIC);
//        return mod;
//    }
}
