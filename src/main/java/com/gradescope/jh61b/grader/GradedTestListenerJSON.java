package com.gradescope.jh61b.grader;

import com.gradescope.jh61b.junit.JUnitUtilities;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class GradedTestListenerJSON extends RunListener {
   private static final int MAX_OUTPUT_LENGTH = 8192;
   private static ByteArrayOutputStream capturedData = new ByteArrayOutputStream();
   private static final PrintStream STDOUT;
   private static TestResult currentTestResult;
   private static List<TestResult> allTestResults;
   private static long startTime;

   public GradedTestListenerJSON() {
   }

   static {
      STDOUT = System.out;
   }

   public void testRunStarted(Description description) throws Exception {
      allTestResults = new ArrayList();
      startTime = System.currentTimeMillis();
   }

   public void testRunFinished(Result result) throws Exception {
      long elapsed = System.currentTimeMillis() - startTime;
      ArrayList<String> objects = new ArrayList();
      Iterator var5 = allTestResults.iterator();

      while(var5.hasNext()) {
         TestResult tr = (TestResult)var5.next();
         objects.add(tr.toJSON());
      }

      String testsJSON = String.join(",", objects);
      PrintStream var10000 = System.out;
      String[] var10002 = new String[]{String.format("\"execution_time\": %d", elapsed), String.format("\"tests\": [%s]", testsJSON)};
      var10000.println("{" + String.join(",", var10002) + "}");
   }

   private static String getAnnotationString(Annotation x, String annotationStringName) throws IllegalAccessException, InvocationTargetException {
      Method[] methods = x.getClass().getDeclaredMethods();
      Method[] var3 = methods;
      int var4 = methods.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Method m = var3[var5];
         if (m.getName().equals(annotationStringName) && m.getReturnType().getCanonicalName().equals("java.lang.String")) {
            return (String)m.invoke(x);
         }
      }

      return "Uh-oh, getAnnotationString failed to get test String. This should never happen!";
   }

   private static double getAnnotationDouble(Annotation x, String annotationDoubleName) throws IllegalAccessException, InvocationTargetException {
      Method[] methods = x.getClass().getDeclaredMethods();
      Method[] var3 = methods;
      int var4 = methods.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Method m = var3[var5];
         if (m.getName().equals(annotationDoubleName) && m.getReturnType().getCanonicalName().equals("double")) {
            return (Double)m.invoke(x);
         }
      }

      return -31337.0;
   }

   private static String getTestName(GradedTest x) throws IllegalAccessException, InvocationTargetException {
      return getAnnotationString(x, "name");
   }

   private static String getTestNumber(GradedTest x) throws IllegalAccessException, InvocationTargetException {
      return getAnnotationString(x, "number");
   }

   private static double getTestMaxScore(GradedTest x) throws IllegalAccessException, InvocationTargetException {
      return getAnnotationDouble(x, "max_score");
   }

   private static String getTestVisibility(GradedTest x) throws IllegalAccessException, InvocationTargetException {
      return getAnnotationString(x, "visibility");
   }

   public void testStarted(Description description) throws Exception {
      GradedTest gradedTestAnnotation = (GradedTest)description.getAnnotation(GradedTest.class);
      String testName = getTestName(gradedTestAnnotation);
      String testNumber = getTestNumber(gradedTestAnnotation);
      double testMaxScore = getTestMaxScore(gradedTestAnnotation);
      String visibility = getTestVisibility(gradedTestAnnotation);
      currentTestResult = new TestResult(testName, testNumber, testMaxScore, visibility);
      currentTestResult.setScore(testMaxScore);
      capturedData = new ByteArrayOutputStream();
      System.setOut(new PrintStream(capturedData));
   }

   public void testFinished(Description description) throws Exception {
      String capturedDataString = capturedData.toString();
      if (capturedDataString.length() > 0) {
         if (capturedDataString.length() > 8192) {
            capturedDataString = capturedDataString.substring(0, 8192) + "... truncated due to excessive output!";
         }

         currentTestResult.addOutput(capturedDataString);
      }

      System.setOut(STDOUT);
      allTestResults.add(currentTestResult);
   }

   public void testFailure(Failure failure) throws Exception {
      currentTestResult.setScore(0.0);
      currentTestResult.addOutput("Test Failed!\n");
      System.out.println(JUnitUtilities.failureToString(failure));
   }
}
