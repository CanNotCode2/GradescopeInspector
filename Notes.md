- We know from the instantiation tests that different tests are run on the same JVM instance, meaning vars are preserved within the same submission.
- Command execution seems to be blocked, no errors or any notification but our RunCommand class doesn't work

- System is Java 1.8 (OpenJDK 64-Bit Server VM 25.362-b09) Host: Linux 4.14.309-159.529.amzn1.x86_64 (amd64)
- Public IPv4 Address is: 
- tools.jar is located at /usr/lib/jvm/java-8-openjdk-amd64/lib/tools.jar
Classpath: /autograder/submission/classes
Main Class Location: /autograder/submission/classes/


    at assn06.AVLTree.<init>:55 (AVLTree.java)
    at ex12Grader.src.edu.unc.ex12.tests.AVLTreeTests.setup:19 (AVLTreeTests.java)
    at sun.reflect.NativeMethodAccessorImpl.invoke0 (native method)
    at sun.reflect.NativeMethodAccessorImpl.invoke:62 (NativeMethodAccessorImpl.java)
    at sun.reflect.DelegatingMethodAccessorImpl.invoke:43 (DelegatingMethodAccessorImpl.java)
    at java.lang.reflect.Method.invoke:498 (Method.java)
    at ex12Grader.src.edu.unc.ex12.tests.RunTests.main:19 (RunTests.java)
