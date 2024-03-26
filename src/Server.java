import java.io.IOException;                                                                //  import everything needed
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;


public class Server {

    private static Set<String> names = new HashSet<>();                                  // all client names
    private static Set<PrintWriter> writers = new HashSet<>();                           // the set for all the clients
    private static final int PORT = 8080;
    private static InetAddress serverAddress;
    private static Map<String, PrintWriter> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {                             // start the server


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

                while (true) {                                                           // keep requesting until unique
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

                    writer.println("MESSAGE " + name + " has joined");                     // let everyone know that the new person has joined!
                }

                PrintWriter writerToAdd = out;
                Set<PrintWriter> tempWriters = new HashSet<>(writers);
                tempWriters.add(writerToAdd);

                writers = tempWriters;
                writers.add(out);                                                          // add the socket's print writer
                clients.put(name, out);                                                    // add the socket's print writer to the clients map

                while (true) {                                                             // accept messages

                    String input = in.nextLine();

                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }

                    String[] commands = input.split(" ");

                    if (commands.length > 1                                                // send private message
                            && commands[0].equalsIgnoreCase("private")) {
                        if (clients.containsKey(commands[1])) {
                            String message = "MESSAGE (" + name + "): "
                                    + input.substring(commands[1].length()
                                    + 2);

                            clients.get(commands[1]).println(message);                      // display massage to the sender
                            out.println(message);
                        }
                    } else {

                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);

            } finally {

                if (out != null) {
                    clients.remove(name);
                    writers.remove(out);
                }

                if (name != null) {
                    names.remove(name);

                    for (PrintWriter writer : writers) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                }

                try {
                    socket.close();

                } catch (IOException e) {
                }
            }
        }
    }
}