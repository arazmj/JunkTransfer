package net.amirrazmjou;

import com.google.common.net.InetAddresses;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class Main {
    private static final int timeout = 1000000;  //timeout value for clients

    public static void main(String[] args) {
        String help =
                "usage for server mode: JunkTransfer -s [file name] [maximum transfer rate" +
                " in kbps] [list of ip and port numbers in ip:port format]\n" +
                "usage for client mode: JunkTransfer -c [port number]\n";

        switch (args[0]) {
            // client mode
            case "-c":
                if (args.length < 2) {
                    System.out.println(help);
                    System.exit(-1);
                }
                int port = 0;
                try {
                    port = Integer.parseUnsignedInt(args[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Bad ip:port format for " + args[1]);
                    System.exit(-1);
                }
                while (true) {
                    System.out.println("Waiting for the server...");
                    Client client = new Client(new InetSocketAddress(port), timeout);
                    client.start();
                }

            // server mode
            case "-s":
                if (args.length < 4) {
                    System.out.println(help);
                    System.exit(-1);
                }

                File file = new File(args[1]);
                if (!file.exists() || file.isDirectory()) {
                    System.out.printf("File %s does not exists or is invalid.", args[1]);
                    System.exit(-1);
                }

                int maxTransferRate = 0;
                try {
                    maxTransferRate = Integer.parseUnsignedInt(args[2]);
                } catch (NumberFormatException e) {
                    System.out.printf("The transfer rate value value for %s is not valid.", args[2]);
                    System.exit(-1);
                }

                Set<InetSocketAddress> clients = new HashSet<>();
                for (int i = 3; i < args.length; i++) {
                    String[] split = args[i].split(":");

                    if (split.length < 2) {
                        System.out.println("Bad ip:port format for " + args[i]);
                        System.exit(-1);
                    }
                    String ip = split[0];

                    if (!InetAddresses.isInetAddress(ip)) {
                        System.out.printf("The ip value for %s is not valid.", args[i]);
                        System.exit(-1);
                    }

                    try {
                        port = Integer.parseUnsignedInt(split[1]);
                        clients.add(new InetSocketAddress(ip, port));
                    } catch (NumberFormatException e) {
                        System.out.printf("The port value for %s is not valid.", args[i]);
                        System.exit(-1);
                    }
                }
                // run the server
                Server server = new Server(file.getPath(), clients, maxTransferRate);
                server.start();
                break;

            default:
                System.out.println("The first argument must be either -c or -s.");
                System.out.println(help);
                System.exit(-1);
        }
    }
}
