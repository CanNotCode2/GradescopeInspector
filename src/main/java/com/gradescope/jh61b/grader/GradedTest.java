package com.gradescope.jh61b.grader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GradedTest {
   String name() default "Unnamed test";

   String number() default "";

   double max_score() default 1.0;

   String visibility() default "visible";
}
