// this test checks if the server is running and searching for connections

import org.junit.Test;                                                          // indicates a test method
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import static org.junit.Assert.assertTrue;

public class ServerTest {

    @Test
    public void testServerStarts() throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(8080)) {        //   incoming connections on port
            Socket socket = new Socket("localhost", 8080);           //  connects to the server
            assertTrue(serverSocket.isBound());                                 //  checks if the socket is bound to the port
        }
    }
}