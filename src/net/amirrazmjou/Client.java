package net.amirrazmjou;

import com.google.common.io.ByteStreams;
import org.biomart.common.utils.CompressedBlockInputStream;

import java.io.*;
import java.net.*;


/**
 * Created by Amir Razmjou on 1/7/16.
 */
public class Client {

    private final int timeout;
    private final String filePath;
    private final int port;

    public Client(InetSocketAddress inetSocketAddress, int timeout) {
        this.timeout = timeout;
        this.port = inetSocketAddress.getPort();
        this.filePath = "data/output" + port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket socket = serverSocket.accept();

             InputStream socketBufferedInputStream =
                     new BufferedInputStream(
                        new CompressedBlockInputStream(socket.getInputStream()));

             // file streams
             FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             BufferedOutputStream fileBufferedOutputStream =
                     new BufferedOutputStream(fileOutputStream)) {

            System.out.println("Server " + socket.getInetAddress().getHostName() +
                    " has connected to this client.");

            serverSocket.setSoTimeout(timeout);

            // write the chunk file
            ByteStreams.copy(socketBufferedInputStream, fileBufferedOutputStream);
            fileBufferedOutputStream.flush();

        }
        catch (FileNotFoundException e) {
            System.out.printf("The file named %s does not exist or cannot be found or cannot be read.\n", filePath);
            System.out.println(e.getStackTrace()[0].getFileName() + " " + e.getMessage());
            System.exit(-1);
        }
        catch (UnknownHostException e) {
            System.out.println("Error connecting to loopback interface.");
            System.out.println(e.getStackTrace()[0].getFileName() + " " + e.getMessage());
            System.exit(-1);
        }
        catch (IOException e) {
            System.out.println("Unknown IO Exception.");
            System.out.println(e.getStackTrace()[0].getFileName() + " " + e.getMessage());
            System.exit(-1);
        }
    }
}