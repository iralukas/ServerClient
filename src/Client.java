import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try {
            ByteBuffer in = ByteBuffer.allocate(1024);
            ByteBuffer out = ByteBuffer.allocate(1024);
            Command command;
            MessageCommand msg = null;
            DatagramChannel channel = DatagramChannel.open();
            channel.configureBlocking(false);
            InetSocketAddress address = new InetSocketAddress("localhost", 7001);
            channel.bind(address);
            System.out.println("Server started at #" + address);
            if (channel.isConnected()){
                System.out.println("Client is connected");
            }

            Selector selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ); //Зарегистрируйте канал в селекторе и укажите «слушать для получения событий»
            channel.register(selector, SelectionKey.OP_WRITE); //Зарегистрируйте канал в селекторе и укажите «слушать для получения событий»

            while (selector.select() > 0) { //Опрос для получения события "готово" в селекторе
                Iterator<SelectionKey> it = selector.selectedKeys().iterator(); // Получить все зарегистрированные «ключи выбора (готовые события прослушивания)» в текущем селекторе

                while (it.hasNext()) {
                    SelectionKey key = it.next();

                    if (key.isReadable() && (channel.receive(in) != null)) { //получаем месседж, десериализуем, принтим
                        in.flip();
                        byte[] bytes = new byte[in.limit()];
                        in.get(bytes, 0, in.limit());
                        try (
                                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                                ObjectInputStream oin = new ObjectInputStream(bais)) {
                            msg = (MessageCommand) oin.readObject();
                            System.out.println("Новое сообщение: " + msg.getMessage());
                            in.clear();
                        }
                    }

                    if (key.isWritable()) {                           //создаем команду и посылаем
                        byte[] bytes = new byte[out.limit()];
                        try (
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ObjectOutputStream oos = new ObjectOutputStream(baos);
                                Scanner scanner = new Scanner(System.in)) {
                            System.out.println("Введите сообщение: ");
                            while (scanner.nextLine() == null) {
                                System.out.println("Сообщение хочу! Введи!");
                            }
                            String console = scanner.nextLine();
                            oos.writeObject(new Command(console));
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
            System.out.println("Ошибка на клиенте: " + e);
        }
    }
}

