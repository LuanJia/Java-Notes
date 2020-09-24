package site.lksky.Huanghe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

@SpringBootTest
public class SelectorTest {

    @Test
    public void testSelect() throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(9999));
        listenChannel.configureBlocking(false);

        listenChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 创建一个缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(100);

        while (true) {
            selector.select(); // 阻塞，直到有监听的事件发生, 在select处被挂起
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

            // 通过迭代器依次访问select
            while (keyIter.hasNext()) {
                SelectionKey key = keyIter.next();

                if (key.isAcceptable()) {
                    SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);

                    System.out.println("与【" + channel.getRemoteAddress() + "】建立了连接！");
                } else if (key.isReadable()) {
                    buffer.clear();
                    // 读取到流末尾说明TCP连接已断开，
                    // 因此需要关闭通道或者取消监听READ事件
                    // 否则会无限循环
                    if (((SocketChannel) key.channel()).read(buffer) == -1) {
                        key.channel().close();
                        continue;
                    }


                    // 按字节遍历数据
                    buffer.flip();


                    while (buffer.hasRemaining()) {
                        byte b = buffer.get();

                        if (b == 0) { // 客户端消息末尾的\0
                            System.out.println();

                            // 响应客户端
                            buffer.clear();
                            buffer.put("Hello, Client!\0".getBytes());
                            buffer.flip();
                            while (buffer.hasRemaining()) {
                                ((SocketChannel) key.channel()).write(buffer);
                            }
                        } else {
                            System.out.print((char) b);
                        }
                    }
                }

                // 已经处理的事件一定要手动移除
                keyIter.remove();

            }
        }

    }


    /**
     * 客户端
     */
    @Test
    public void testClient() throws IOException {

        Socket socket = new Socket("localhost", 9999);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        // 先向服务端发送数据
        os.write("Hello, Server!\0".getBytes());

        // 读取服务端发来的数据
        int b;
        while ((b = is.read()) != 0) {
            System.out.print((char) b);
        }
        System.out.println();

        socket.close();

    }
}
