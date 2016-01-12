package net.amirrazmjou.test;

import junit.framework.TestCase;
import net.amirrazmjou.Client;
import net.amirrazmjou.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Amir Razmjou on 1/8/16.
 */
class ServerTest extends TestCase {
    private static final int timeout = 10000;  //timeout value for clients
    private static final String clientLocalAddress = "127.0.0.1";
    private static final int numClients = 64;

    public void testStart() throws Exception {
        Set<InetSocketAddress> clients = new HashSet<>();

        // create 64 clients with port numbers starting from 6660 on fly
        for (int i = 0; i < numClients; i++) {
            clients.add(new InetSocketAddress(clientLocalAddress, 6660 + i));
        }

        // run clients
        for (InetSocketAddress c: clients) {
            Runnable clientRunnable = () -> {
                Client client1 = new Client(c, timeout);
                client1.start();
            };
            Thread clientThread = new Thread(clientRunnable);
            clientThread.start();

        }

        // run the
        Server server = new Server("data/test_large", clients, 1024);
        server.start();
    }
}