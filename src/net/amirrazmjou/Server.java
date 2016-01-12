package net.amirrazmjou;

import com.google.common.io.ByteStreams;
import org.biomart.common.utils.CompressedBlockOutputStream;

import java.io.*;
import java.net.*;
import java.util.Set;

/**
 * Created by Amir Razmjou on 1/7/16.
 */
public class Server {
    final private InetSocketAddress[] clients; // list of clients
    final private String filePath;             // the source file path
    final private int clientsCount;            // number of clients
    final private long chunkSize = 1024 * 1024; // 1 MB
    private final int maxTransferRate;         // maximum transfer rate
    private TokenBucket bucket = null;

    /**
     * @param filePath A file path to file of a fixed size (64MB)
     * @param clients An unordered list of 64 IP addresses/port numbers
     * @param maxTransferRate The maximum transfer rate for the sender (in Kbps).
     *                        The rate limits the total amount of bandwidth a sender
     */
    public Server(String filePath, Set<InetSocketAddress> clients, int maxTransferRate) {
        this.maxTransferRate = maxTransferRate;
        this.clients = clients.toArray(new InetSocketAddress[clients.size()]);
        this.clientsCount = clients.size();
        this.filePath = filePath;
    }

    /**
     * Starts the server, spawns the worker threads
     */
    public void start() {
        // instantiate the bucket which is going to be shared among all client threads
        bucket = new TokenBucket(maxTransferRate);

        // spawn the threads
        for (int i = 0; i < clientsCount; i++) {
            final int ci = i;
            Thread thread = new Thread(() -> {
                transfer(clients[ci], ci);
            });
            thread.start();
        }
    }

    /**
     * Transfers the file fragment specified by chunk number to the client, recovers from the socket
     * exceptions gracefully but exits if the server
     * @param client InetSocketAddress of the corresponding client that the server should upload to
     * @param chunkId  The chunk number is the part that supposed to be uploaded
     * @throws IOException
     * @throws InterruptedException
     */
    private void transfer(InetSocketAddress client, int chunkId) {
        String hostName = client.getHostString();
        int hostPort = client.getPort();

        try (   FileInputStream fileInputStream = new FileInputStream(filePath);
                BufferedInputStream fileBufferedInputStream =
                        new BufferedInputStream(fileInputStream);

                // socket stream we do not buffer
                Socket socket = new Socket(hostName, hostPort);
                OutputStream socketOutputStream =
                        new CompressedBlockOutputStream(1024,
                            new SpeedLimitedOutputStream(8, bucket,
                                    socket.getOutputStream()))) {

            System.out.printf("Client %s:%s has connected to this server.\n",
                    hostName, client.getPort());

            // skip initial chunk of the file up to the chunk that
            // must be uploaded for this specific client
            ByteStreams.skipFully(fileBufferedInputStream, chunkSize * chunkId);

            // copy the file limited file stream to socket output stream
            InputStream fileLimitedOutputStream = ByteStreams.limit(fileBufferedInputStream, chunkSize);
            long copy = ByteStreams.copy(fileLimitedOutputStream, socketOutputStream);
            socketOutputStream.flush();

            System.out.printf("Sent %d bytes from the fragment id %d.", copy, chunkId);

        }
        catch (FileNotFoundException e) {
            System.out.println("File named " + filePath + " does not exist or cannot be found.");
            System.exit(-1);
        }
        catch (UnknownHostException e) {
             System.out.println("Client " + hostName + ":" + hostPort + " is unknown.");
        }
        catch (SocketException e) {
            // This is caused by writing to a connection when the other end has already closed it.
            System.out.println("Error connecting to " + hostName + ":" + hostPort + ".");
        }
        catch (IOException e) {
            System.out.println("Unknown IO Exception.");
            System.out.println(e.getStackTrace()[0].getFileName() + " " + e.getMessage());
        }
    }
}
