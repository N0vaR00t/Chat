// this test checks if client runs correctly and has all expected components of user interface

import org.junit.Test;                                                      // indicates a test method
import javax.swing.*;
import java.awt.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClientTest {

    @Test
    public void testClientStarts() {                                         // defines the test method

        SwingUtilities.invokeLater(() -> {
            String serverAddress = "localhost";                              // initializes the server address

            int serverPort = 8080;
            Client client = new Client(serverAddress, serverPort);           // creates a new client
            client.launch();

            JFrame frame = client.frame;
            assertNotNull(frame);                                            // checks if the main window was initialized correctly.
            assertEquals("Chatter", frame.getTitle());              // checks the title

            JTextField textField = client.textField;
            assertNotNull(textField);                                        // checks if the input field was initialized correctly
            assertEquals("", textField.getText());                  // checks if text is empty

            JTextArea messageArea = client.messageArea;
            assertNotNull(messageArea);                                      // checks if that the message area was initialized correctly.
            assertEquals("", messageArea.getText());                // checks if text is empty

            JButton sendButton = findButton(frame, "Send");        // checks the Send button
            assertNotNull(sendButton);

            JButton exitButton = findButton(frame, "Exit");        // checks the Exit button
            assertNotNull(exitButton);
        });
    }

    private JButton findButton(JFrame frame, String buttonText) {             // finds buttons by the names
                                                                              // to check GUI and buttons
        Component[] components = frame.getContentPane().getComponents();

        for (Component component : components) {

            if (component instanceof JButton) {
                JButton button = (JButton) component;

                if (button.getText().equals(buttonText)) {
                    return button;
                }
            }
        }
        return null;                                                          // if doesn't find a button - returns null
    }
}