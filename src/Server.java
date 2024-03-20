import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static Set<String> names = new HashSet<>();                            // All client names
    private static Set<PrintWriter> writers = new HashSet<>();                     // The set for all the clients
    private static final int PORT = 8080;
    private static InetAddress serverAddress;

    public static void main(String[] args) throws Exception {

        serverAddress = InetAddress.getLocalHost();
        System.out.println("Server IP address: " + serverAddress.getHostAddress());

        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);

        try (ServerSocket listener = new ServerSocket(PORT)) {

            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    public static class Handler implements Runnable {

        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {                                                      // Keep requesting until unique
                    out.println("SUBMIT NAME");
                    name = in.nextLine();

                    if (name == null) {
                        return;
                    }
                    synchronized (names) {

                        if (!name.isEmpty() && !names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                out.println("NAME ACCEPTED " + name);

                for (PrintWriter writer : writers) {

                    writer.println("MESSAGE " + name + " has joined");             // Let everyone know that the new person has joined!
                }

                writers.add(out);                                                  // Add the socket's print writer

                while (true) {                                                     // Accept messages

                    String input = in.nextLine();

                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }

                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }

            } catch (Exception e) {
                System.out.println(e);

            } finally {
                if (out != null) {
                    writers.remove(out);
                }

                if (name != null) {
                    System.out.println(name + " is leaving");
                    names.remove(name);

                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                }

                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}
