package org.gradeskope.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.security.ProtectionDomain;

public class DumpingTransformer implements ClassFileTransformer {

  private static final String outputPathPrefix = "/tmp/javaroot/";

  // The transform method is called for each non-system class as they are being loaded
  public byte[] transform(ClassLoader loader, String className,
                          Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) throws IllegalClassFormatException {
    if (className != null) {
      // Skip all system classes
//            if (
//                    !className.startsWith("java")
//                    && !className.startsWith("sun")
//                    && !className.startsWith("javax")
//                    && !className.startsWith ("com")
//                    && !className.startsWith("jdk")
//                    && !className.startsWith("org")
//                    regexMatchCheck(className, TestJavaAgent.patterns)// This check has to be here again because Java
//            )
      {
        System.out.println("Dumping: " + className);

        // Replace all separator charactors
//                String newName = className.replaceAll("/", "#") + ".class";

        try {
          Files.createDirectories(new File(outputPathPrefix + className).getParentFile()
              .toPath()); // Make sure Parent directory exists
          FileOutputStream fos = new FileOutputStream(outputPathPrefix + className + ".class");
          fos.write(classfileBuffer);
//                    TestJavaAgent.classByteCodes.add(classfileBuffer);
          fos.close();
        } catch (Exception ex) {
          System.out.println("Exception while writing: " + className);
        }
      }
    }
    // We are not modifying the bytecode in anyway, so return it as-is
    return classfileBuffer;
  }
}
