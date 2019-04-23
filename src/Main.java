import java.io.IOException;

public class Main {

  private static void startServer() throws IOException {
    String path = System.getProperty("java.home") + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java";
    ProcessBuilder processBuilder = new ProcessBuilder(
            path,
            "-Dfile.encoding=utf-8",
            "-cp",
            System.getProperty("java.class.path"),
            Server.class.getCanonicalName()
    );
    processBuilder.start();
  }

  private static void startClient() throws IOException {
    String path = System.getProperty("java.home") + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java";
    ProcessBuilder processBuilder = new ProcessBuilder(
            path,
            "-Dfile.encoding=utf-8",
            "-cp",
            System.getProperty("java.class.path"),
            Client.class.getCanonicalName()
    );
    processBuilder.start();
  }

  public static void main(String[] args) throws Exception {
        startServer();


        startClient();
        startClient();
  }
}
