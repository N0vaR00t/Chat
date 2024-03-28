import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static Set<String> names = new HashSet<>();
    public static Set<PrintWriter> writers = new HashSet<>();
    public static Map<String, PrintWriter> clients = new ConcurrentHashMap<>();
    public static String coordinator = null;

    public static void main(String[] args) throws IOException {
        InetAddress serverAddress = InetAddress.getLocalHost(); // the pc's server IP
        System.out.println("Server IP: " + serverAddress);
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);

        try (ServerSocket listener = new ServerSocket(8080)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    private static class Handler implements Runnable {
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

                while (true) {
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
                    writer.println("MESSAGE " + name + " has joined");
                }

                PrintWriter writerToAdd = out;
                Set<PrintWriter> tempWriters = new HashSet<>(writers);
                tempWriters.add(writerToAdd);

                writers = tempWriters;
                writers.add(out);
                clients.put(name, out);

                if (coordinator == null && !names.isEmpty()) {
                    coordinator = name;
                    out.println("COORDINATOR " + coordinator);
                }

                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }

                    String[] commands = input.split(" ");
                    if (commands.length > 1 && commands[0].equalsIgnoreCase("private")) {
                        if (clients.containsKey(commands[1])) {
                            String message = "MESSAGE (" + name + "): " + input.substring(commands[1].length() + 2);
                            clients.get(commands[1]).println(message);
                            out.println(message);
                        }
                    } else {
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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

                if (coordinator.equals(name)) {
                    selectNewCoordinator();
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void selectNewCoordinator() {
            if (!names.isEmpty()) {
                coordinator = names.iterator().next();
                for (PrintWriter writer : writers) {
                    writer.println("COORDINATOR " + coordinator);
                }
            } else {
                coordinator = null;
            }
        }
    }
}