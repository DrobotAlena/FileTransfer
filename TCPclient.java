/**
 * Created by Alena on 15.12.2016.
 */

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

import static java.lang.System.exit;

public class TCPclient implements myProtocol {
    static private Socket clientSocket = null;
    static private OutputStream outToServer = null;
    static private InputStream inFromServer = null;
    static private PrintWriter senderToServer = null;
    static private BufferedReader getterFromServer = null;

    private static void shutdown() {
        try {
            clientSocket.close();
            outToServer.close();
            inFromServer.close();
            senderToServer.close();
            getterFromServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        InetAddress ipAddress = null;
        String answer = null;
        try {
            System.out.println("Please write server address");
            String address = reader.readLine(); // get server IP
            //"127.0.0.1";
            ipAddress = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.err.println("Error with getting server address");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error with getting server IP address");
        }
        try {
            clientSocket = new Socket(ipAddress, Constants.PORT);//name of server + host
            outToServer = clientSocket.getOutputStream();
            inFromServer = clientSocket.getInputStream();
            senderToServer = new PrintWriter(new OutputStreamWriter(outToServer));
            getterFromServer = new BufferedReader(new InputStreamReader(inFromServer));

            String name = null;
            FileInputStream fileInputStream = null;
            System.out.println("Please write fail name");
            name = reader.readLine();
            if (name == null) {
                System.out.println("Error: file name was empty");
            }
            File file = new File(name);
            if (!file.exists()) {
                System.err.println("Error file with such name dosen't exist");
                exit(1);
            }

            fileInputStream = new FileInputStream(name);

            byte[] buffer = new byte[Constants.BUF_SIZE];
            String[] splitName = name.split("/");
            String fileNameForServer = splitName[splitName.length-1];
            outToServer.write(fileNameForServer.getBytes());
            outToServer.flush();
            answer = getterFromServer.readLine();
            System.out.println(answer);
            if (!answer.equals(SUCCESS)) {
                shutdown();
                System.err.println("Error: incorrect name of file");
            }

            while (fileInputStream.available() > 0) {
                int count = fileInputStream.read(buffer);
                outToServer.write(buffer, 0, count);
            }
            outToServer.flush();
            outToServer.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error with transfer data to server");
            e.printStackTrace();
        }
    }
}