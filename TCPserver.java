/**
 * Created by Alena on 01.12.2016.
 */


import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TCPserver implements myProtocol {

    static private ServerSocket serverSocket = null;
    static private Selector selector = null;

    static private PrintWriter senderToClient = null;
    static private BufferedReader getterFromClient = null;
    static private InputStream inFromClient = null;
    static private FileOutputStream fileOutputStream = null;
    static private List<GetterData> getterDataList = new LinkedList<>();

    private static void shutdown() {
        try {
            fileOutputStream.close();
            inFromClient.close();
            senderToClient.close();
            getterFromClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String argv[]) {
        try {
            selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.socket().bind(new InetSocketAddress(Constants.PORT));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverSocket = serverChannel.socket();
        } catch (IOException e) {
            System.err.println("Error with creating socket");
            e.printStackTrace();
        }

        while (true) {
            inFromClient = null;
            try {
                int count = selector.select();
                if (count < 0)
                    continue;

                Set keys = selector.selectedKeys();
                Iterator it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) { //если новое соединение
                        accept();
                    }
                    else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) { //если готовы читать
                        SocketChannel sc = (SocketChannel) key.channel();
                        recvData(sc);
                    }
                }
                keys.clear();

            } catch (IOException e) {
                System.err.println("Error witn transfer data to file");
                e.printStackTrace();
            }
        }
    }

    static void accept() {
        try {
            Socket clientSocket = serverSocket.accept();
            ByteBuffer buffer = ByteBuffer.allocate(Constants.BUF_SIZE);
            buffer.clear();
            clientSocket.getChannel().configureBlocking(true);
            int count = clientSocket.getChannel().read(buffer);  //получаем имя
            String name = new String(buffer.array());
            name = name.substring(0, count);
            System.out.println(count + " "+ name);
            File file = new File(name);
            if (!name.contains("/") && !file.exists()) {
                file.createNewFile();
                if (file.canWrite()) {
                    clientSocket.getChannel().write(ByteBuffer.wrap(SUCCESS.getBytes()));
                    file.delete();
                }
            } else {
                clientSocket.getChannel().write(ByteBuffer.wrap(ERROR.getBytes()));
            }
            clientSocket.getChannel().shutdownOutput();
            fileOutputStream = new FileOutputStream(name);
            clientSocket.getChannel().configureBlocking(false);
            clientSocket.getChannel().register(selector, SelectionKey.OP_READ);
            getterDataList.add(new GetterData(clientSocket.getChannel(), name, fileOutputStream));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void recvData(SocketChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.BUF_SIZE);
        Iterator<GetterData> iterator = getterDataList.iterator();
        GetterData getterData = null;
        while(iterator.hasNext()){
            getterData = iterator.next();
            if(getterData.getSc().equals(channel)){
                break;
            }
        }
        try {
            int count = channel.read(buffer);
            System.out.println(getterData.getName() +" get "+ count);
            if(count == -1){ //если достигнут конец файла
                channel.close();
                getterDataList.remove(iterator);
                return;
            }
            if(count > 0) { // читеам данные
                getterData.getFileOutputStream().write(buffer.array(), 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}