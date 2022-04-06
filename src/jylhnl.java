import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DatagramChannelBuilder {

        DatagramChannel client = DatagramChannel.open();
        String msg = "Hello, this is a Baeldung's DatagramChannel based UDP client!";
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", 7001);
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        client.send(buffer, serverAddress);
    }

    InetSocketAddress address = new InetSocketAddress("localhost", 7001);
    DatagramChannel server = DatagramChannel.open().bind(address);
        System.out.println("Server started at #" + address);