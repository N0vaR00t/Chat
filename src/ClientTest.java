import org.junit.Test;
import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClientTest {

    @Test
    public void testClientStarts() {
        SwingUtilities.invokeLater(() -> {
            String serverAddress = "localhost";
            int serverPort = 8080;
            Client client = new Client(serverAddress, serverPort);
            client.launch();

            JFrame frame = client.frame;
            assertNotNull(frame);
            assertEquals("Chatter", frame.getTitle());

            JTextField textField = client.textField;
            assertNotNull(textField);
            assertEquals("", textField.getText());

            JTextArea messageArea = client.messageArea;
            assertNotNull(messageArea);
            assertEquals("", messageArea.getText());

            JButton sendButton = findButton(frame, "Send");
            assertNotNull(sendButton);

            JButton exitButton = findButton(frame, "Exit");
            assertNotNull(exitButton);
        });
    }

    private JButton findButton(JFrame frame, String buttonText) {
        Component[] components = frame.getContentPane().getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button.getText().equals(buttonText)) {
                    return button;
                }
            }
        }
        return null;
    }
}