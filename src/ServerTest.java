import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertTrue;

public class ServerTest {

    @Test
    public void testServerStarts() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            Socket socket = new Socket("localhost", 8080);
            assertTrue(serverSocket.isBound());
        }
    }
}