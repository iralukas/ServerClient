import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.sql.SQLOutput;
import java.util.Iterator;

public class Server {

    public static void main(String[] args) {
        try {
            ByteBuffer in = ByteBuffer.allocate(1024);
            ByteBuffer out = ByteBuffer.allocate(1024);
            Command command = null;
            MessageCommand msg = null;
            DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            InetSocketAddress address = new InetSocketAddress("localhost", 7001);
            channel.bind(address);
            System.out.println("Server started at #" + address);

            Selector selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ); //Зарегистрируйте канал в селекторе и укажите «слушать для получения событий»
            channel.register(selector, SelectionKey.OP_WRITE); //Зарегистрируйте канал в селекторе и укажите «слушать для получения событий»

            while (selector.select() > 0) { //Опрос для получения события "готово" в селекторе
                Iterator<SelectionKey> it = selector.selectedKeys().iterator(); // Получить все зарегистрированные «ключи выбора (готовые события прослушивания)» в текущем селекторе

                while (it.hasNext()) {
                    SelectionKey key = it.next();

                    if (key.isReadable() && (channel.receive(in) != null)) { //получаем объект Command, десериализуем, сохраняем в Message
                        in.flip();
                        byte[] bytes = new byte[in.limit()];
                        in.get(bytes, 0, in.limit());
                        try (
                                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                                ObjectInputStream oin = new ObjectInputStream(bais)) {
                            command = (Command) oin.readObject();
                            msg = command.execute();
                            in.clear();
                        }
                    }

                    if (key.isWritable() && (msg != null)) { //сериализуем message, посылаем, обнуляем
                        byte[] bytes = new byte[out.limit()];
                        try (
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                            oos.writeObject(msg);
                            msg = null;
                            oos.flush();
                            oos.close();
                            baos.write(bytes);
                            out.put(bytes);
                            channel.send(out, address);
                        }
                    }
                    it.remove();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка на сервере: " + e);
        }
    }
}
