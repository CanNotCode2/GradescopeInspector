package org.gradeskope.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ProcessWrapper {
    public static void main(String args[]) {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        String subProcessID = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
        String mainProcessID = args[0];
        String pathToAgentJar = args[1];
        String agentArgs = args[2];
        File toolsJarFile = new File(args[3]);

        URLClassLoader child = null;
        try {
            child = new URLClassLoader(
                    new URL[]{toolsJarFile.toURI().toURL()},
                    ProcessWrapper.class.getClassLoader()
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }



        System.out.println("Main Process: " + mainProcessID);

        System.out.println("Subprocess: " + subProcessID);

        try {
            attachGivenAgentToThisVM(pathToAgentJar, child, agentArgs, mainProcessID);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void attachGivenAgentToThisVM(String pathToAgentJar, URLClassLoader child, String agentArgs, String mainProcessID) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        Class classToLoad = Class.forName("com.sun.tools.attach.VirtualMachine", true, child);
        Method vmStaticAttach = classToLoad.getDeclaredMethod("attach", String.class);
        Method vmInstanceLoadAgent = classToLoad.getDeclaredMethod("loadAgent", String.class, String.class);
        Method vmInstanceDetach = classToLoad.getDeclaredMethod("detach");
        Object vmClassInstance = vmStaticAttach.invoke(null, mainProcessID); //Get VMClassInstance by calling VirtualMachine.attach(pid), a static method
        vmInstanceLoadAgent.invoke(vmClassInstance, pathToAgentJar, agentArgs); // Load Java Agent using vmClassInstance as the VirtualMachine vm Object
        vmInstanceDetach.invoke(vmClassInstance); // Detach Instance, execute with vmClassInstance as the Object

    }

}
