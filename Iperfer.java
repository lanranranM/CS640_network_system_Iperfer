import java.util.Arrays;
import java.util.regex.Pattern;
import java.net.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.net.UnknownHostException;
import java.io.IOException;
import java.net.InetAddress;

public class Iperfer {
    // just a default constructor
    public Iperfer() {}

    /**
     * Helper functions that operate Iperfer in client mode
     * 
     * @param serverHostName the hostname or IP address of the iperf server which will consume data
     * @param serverPort the port on which the remote host is waiting to consume data; the port
     *        should be in the range 1024 ≤ server port ≤ 65535
     * @param time the duration in seconds for which data should be generated
     */
    public static void clientMode(String serverHostName, int serverPort, float time) {
        // check portnumber range
        if (serverPort < 1024 || serverPort > 65535) {
            System.err.println("Error: port number must be in the range 1024 to 65535");
            return;
        }

        int sentKByte = 0;
        float rate = 0;
        double start = System.currentTimeMillis();
        double currentTime = 0;
        try {
            Socket socket = new Socket(serverHostName, serverPort);
            while ((currentTime=(System.currentTimeMillis() - start)/1000.0) < time) {
                socket.getOutputStream().write(new byte[1000]);
                sentKByte++;
            }
            // close the client and related streams
            socket.getOutputStream().close();
            socket.close();
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (SecurityException e) {
            System.err.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        // output the summary
        rate = ((float) sentKByte) / 1000 * 8 / time;
        System.out.println(String.format("sent=%d KB rate=%.3f Mbps", sentKByte, rate));
    }

    /**
     * Helper functions that operate Iperfer in server mode
     * The time is taken between after reading 1st byte of data and last byte of data
     * @param serverPort the port on which the host is waiting to consume data
     */
    public static void serverMode(int serverPort) {
        // check portnumber range
        if (serverPort < 1024 || serverPort > 65535) {
            System.err.println("Error: port number must be in the range 1024 to 65535");
            return;
        }
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            Socket clientSocket = serverSocket.accept();
            // receive the data from client as chunck in size of 1KB
            int receivedByte = 0;
            double rate = 0;
            long start = 0;
            double time =0;
            byte[] received = new byte[1000];
            start = System.currentTimeMillis();
            int receivedSize = clientSocket.getInputStream().read(received);
            receivedByte += receivedSize;
            time = (System.currentTimeMillis() - start);
            while ((receivedSize = clientSocket.getInputStream().read(received)) > 0) {
                time = (System.currentTimeMillis() - start)/1000;
                receivedByte+=receivedSize;
            }
            clientSocket.getInputStream().close();
            serverSocket.close();
            // output summery
            int receivedKByte = receivedByte/1000;
            rate = ((double) receivedKByte) / 1000 * 8 / time;
            System.out.println(String.format("received=%d KB rate=%.3f Mbps", receivedKByte, rate));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Validate the iperfer commnand in client mode. The validation is based on - length of command
     * - order of arguments - type of arguments
     * 
     * @param command the user input command
     * @return true if the command is valid, false otherwise
     */
    public static boolean clientValidator(String[] command) {
        if (command.length == 7) {
            if (command[1].equals("-h") && command[3].equals("-p") && command[5].equals("-t")) {
                // argument type validation
                boolean validHostName = false;
                boolean validPort = false;
                boolean validTime = false;
                try {
                    // validte if the hostname is a valid DNS/IPv4/IPv6 or exist
                    InetAddress.getByName(command[2]);
                    validHostName = true;
                    // portNumber argument type should be integer without non-numeric chars
                    validPort = Pattern.compile("\\d+").matcher(command[4]).matches();
                    // time argument type should be non-negative integer or decimal
                    validTime = Pattern.compile("\\d+(\\.\\d+)?").matcher(command[6]).matches();
                } catch (Exception e) {
                }
                return validHostName && validPort && validTime;
            }
        }
        return false;
    }
    
    /**
     * Validate the iperfer commnand in server mode. The validation is based on - length of command
     * - order of arguments - type of arguments
     * 
     * @param command the user input command
     * @return true if the command is valid, false otherwise
     */
    public static boolean serverValidator(String[] command) {
        if (command.length == 3) {
            if (command[1].equals("-p")) {
                // argument type validation
                boolean validPort = false;
                try {
                    // portNumber argument type should be integer without non-numeric chars
                    validPort = Pattern.compile("\\d+").matcher(command[2 ]).matches();
                } catch (Exception e) {
                }
                return validPort;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        if (args[0].equals("-c")) {
            if (clientValidator(args)) {
                clientMode(args[2], Integer.parseInt(args[4]), Float.parseFloat(args[6]));
            } else {
                System.err.println("Error: invalid arguments");
            }
        } else if (args[0].equals("-s")) {
            if (serverValidator(args)){
                serverMode(Integer.parseInt(args[2]));
            } else {
                System.err.println("Error: invalid arguments");
            }
        } else {
            System.err.println("Error: invalid arguments");
        }
        System.exit(0);
    }
}
