import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client {
    public String serverAddress;
    public int serverPort;
    public Scanner in;
    public PrintWriter out;
    public JFrame frame;
    public JTextField textField;
    public JTextArea messageArea;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        frame = new JFrame("Chatter");
        textField = new JTextField(40);
        messageArea = new JTextArea(20, 40);
    }

    public void launch() {
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);

        JButton sendButton = new JButton("Send");
        frame.getContentPane().add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                out.println(message);
                //appendMessage("You", message); // Append sent message to GUI
                textField.setText("");
            }
        });

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();

                if (line.startsWith("SUBMIT NAME")) {
                    out.println(getName());

                } else if (line.startsWith("NAME ACCEPTED")) {
                    frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);

                } else if (line.startsWith("MESSAGE")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String sender = parts[0].substring(8).trim();
                        String message = parts[1].trim();
                        appendMessage(sender, message);
                    }
                } else if (line.startsWith("COORDINATOR")) {
                    String newCoordinator = line.substring(12);
                    appendMessage("System", "New Admin is: " + newCoordinator);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void appendMessage(String sender, String message) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String currentTime = dateFormat.format(date);
        messageArea.append("[" + currentTime + "] " + sender + ": " + message + "\n");
    }

    public static void main(String[] args) {
        String serverAddress = JOptionPane.showInputDialog(
                null,
                "Please enter the server IP address:",
                "Server IP Input",
                JOptionPane.QUESTION_MESSAGE
        );

        if (serverAddress == null || serverAddress.isEmpty()) {
            System.err.println("Server IP is required.");
            return;
        }

        String port = JOptionPane.showInputDialog(
                null,
                "Please enter the port number:",
                "Port Number Input",
                JOptionPane.QUESTION_MESSAGE
        );

        if (port == null || port.isEmpty()) {
            System.err.println("Port number is required.");
            return;
        }

        int serverPort = Integer.parseInt(port);

        Client client = new Client(serverAddress, serverPort);
        client.launch();
    }
}