package com.gradescope.jh61b.grader;

import org.json.simple.JSONValue;

public class TestResult {
   final String name;
   final String number;
   final double maxScore;
   double score;
   final String visibility;
   private StringBuilder outputSB;

   public TestResult(String name, String number, double maxScore, String visibility) {
      this.name = name;
      this.number = number;
      this.maxScore = maxScore;
      this.outputSB = new StringBuilder();
      this.visibility = visibility;
   }

   public String toString() {
      String var10000 = this.name;
      return "name: " + var10000 + ", number: " + this.number + ", score: " + this.score + ", max_score: " + this.maxScore + ", detailed output if any (on next line): \n" + this.outputSB.toString();
   }

   public String toJSON() {
      String output = JSONValue.escape(this.outputSB.toString());
      String[] var10001 = new String[]{String.format("\"%s\": \"%s\"", "name", this.name), String.format("\"%s\": \"%s\"", "number", this.number), String.format("\"%s\": %s", "score", this.score), String.format("\"%s\": %s", "max_score", this.maxScore), String.format("\"%s\": \"%s\"", "visibility", this.visibility), String.format("\"%s\": \"%s\"", "output", output)};
      return "{" + String.join(",", var10001) + "}";
   }

   public void setScore(double score) {
      this.score = score;
   }

   public void addOutput(String x) {
      this.outputSB.append(x);
   }
}
