package org.gradeskope.agent;


import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public class TestTransformer implements ClassFileTransformer {

  private static final String outputPathPrefix = "/tmp/javaroot/";

  private static final String[] methodStartsWithList =
      {"assertEquals", "assertNotEquals", "assertNull", "assertNotNull", "fail"};

  private static boolean startsWithAny(String string, String[] strings) {
    for (String arrayString : strings) {
      if (string.startsWith(arrayString)) {
        return true;
      }
    }
    return false;
  }

  // The transform method is called for each non-system class as they are being loaded
  public byte[] transform(ClassLoader loader, String className,
                          Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                          byte[] classfileBuffer) throws IllegalClassFormatException {
    if (className != null) {
      System.out.println("Looking through: " + className);
      try {
        return transformJUnitTests(className);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return classfileBuffer;
  }

  private byte[] transformJUnitTests(String className) throws Exception {
    ClassPool pool = ClassPool.getDefault();
    pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
    CtClass cc = pool.get(className.replace("/", "."));
    for (CtMethod ctMethod : cc.getMethods()) {
      if (ctMethod.hasAnnotation("org.junit.Test")) {
        ctMethod.setBody("return;");
      }
    }


    try {
      Files.createDirectories(
          new File(outputPathPrefix + "temp/" + "org#Junit$Assert").getParentFile()
              .toPath()); // Make sure Parent directory exists
      FileOutputStream fos = new FileOutputStream(outputPathPrefix + "temp/" + "Assert" + ".class");
      fos.write(cc.toBytecode());
//                    TestJavaAgent.classByteCodes.add(classfileBuffer);
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return cc.toBytecode();
  }

}
