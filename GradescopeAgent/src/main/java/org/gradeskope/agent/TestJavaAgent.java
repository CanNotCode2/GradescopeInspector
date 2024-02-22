package org.gradeskope.agent;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TestJavaAgent {

  public static ArrayList<byte[]> classByteCodes = new ArrayList<byte[]>();

  public static boolean enableAssertTransform = false;
  public static boolean enableDumpingTransform = false;

  public static boolean enableTestTransform = false;

  public static Pattern[] includePatterns = {
//        Pattern.compile("^assn06.utils"), // For Testing
//        Pattern.compile("^ex12Grader")
      Pattern.compile(".*")
  };

  public static Pattern[] excludePatterns = {
//        Pattern.compile("java.lang.VerifyError"),
      Pattern.compile("^sun.security"),
      Pattern.compile("^sun.reflect"),
      Pattern.compile("^java")
  };

  public static Pattern[] testTransformExcludes = {
      Pattern.compile("^sun.security"),
      Pattern.compile("^sun.reflect"),
      Pattern.compile("^java"),
      Pattern.compile("^org.junit"),
      Pattern.compile("junit.framework"),
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

          //I don't know why we need to add true as a second argument, but it made it work, so...
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
            !regexMatchCheck(klass.getName(), testTransformExcludes)) {
          AssertTransformer assertTransformer = new AssertTransformer();

          //I don't know why we need to add true as a second argument, but it made it work, so...
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