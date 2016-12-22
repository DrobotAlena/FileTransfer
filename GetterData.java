/**
 * Created by Alena on 15.12.2016.
 */

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.SocketChannel;

/**
 * Created by Alena on 14.12.2016.
 */
public class GetterData {
    SocketChannel sc;
    String name;
    FileOutputStream fileOutputStream;


    public String getName() {
        return name;
    }


    public FileOutputStream getFileOutputStream() {
        return fileOutputStream;
    }

    public SocketChannel getSc() {
        return sc;
    }

    public GetterData(SocketChannel sc, String name,FileOutputStream fileOutputStream) {
        this.sc = sc;

        this.name = name;
        this.fileOutputStream = fileOutputStream;

    }
}
