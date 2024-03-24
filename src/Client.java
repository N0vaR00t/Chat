import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.net.ServerSocket;

public class Client {

    String serverAddress;
    int serverPort;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);

    private String getLocalIP() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface net = nets.nextElement();
                Enumeration<InetAddress> addresses = net.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.isLoopbackAddress() && addr.getAddress().length == 4) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private int getLocalPort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            int port = socket.getLocalPort();
            socket.close();
            return port;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void printClientDetails() {
        System.out.println("Client IP Address: " + getLocalIP());
        System.out.println("Client Port Number: " + getLocalPort());
    }

    public Client(String serverAddress, int serverPort, JFrame frame) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.frame = frame;

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    private String getName() {

        return JOptionPane.showInputDialog(

                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE
        );
    }


    private void run() throws IOException {
        try {

            Socket socket = new Socket(serverAddress, serverPort);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();

                if (line.startsWith("SUBMIT NAME")) {
                    out.println(getName());

                } else if (line.startsWith("NAME ACCEPTED")) {
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);

                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                }
            }
        } finally {

            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        // Prompt the user to enter the server IP address
        String serverAddress = JOptionPane.showInputDialog(
                null,
                "Please enter the server IP address:",
                "Server IP Input",
                JOptionPane.QUESTION_MESSAGE);

        if (serverAddress == null || serverAddress.isEmpty()) {
            // If the user pressed cancel or entered an empty string, exit the program
            System.err.println("Server IP is required.");
            return;
        }

        // Prompt the user to enter the port number
        String port = JOptionPane.showInputDialog(
                null,
                "Please enter the port number:",
                "Port Number Input",
                JOptionPane.QUESTION_MESSAGE);

        if (port == null || port.isEmpty()) {
            // If the user pressed cancel or entered an empty string, exit the program
            System.err.println("Port number is required.");
            return;
        }

        // Create a GUI
        JFrame mainFrame = new JFrame();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a button that runs the Private class
        JButton privateButton = new JButton("Run Private Class");
        privateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Create an instance of the Private class
                Private privateClass = new Private();

                // Call the someMethod method from the Private class
                privateClass.someMethod();
            }
        });

        // Add the button to the GUI
        mainPanel.add(privateButton);
        mainFrame.add(mainPanel);

        // Set the GUI to be visible and exit when closed
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int serverPort = Integer.parseInt(port);

        // Create a new JFrame instance
        JFrame frame = new JFrame("Chatter");

        Client client = new Client(serverAddress, serverPort, frame);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}