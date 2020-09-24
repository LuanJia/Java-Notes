package site.lksky.Huanghe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

@SpringBootTest
public class NioTest {

    @Test
    public void SocketTest() throws IOException {
        FileChannel channel = new RandomAccessFile("text.txt", "rw").getChannel();
        channel.position(channel.size());   // 移动文件指针到末尾（追加写入）

        ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        // 数据写入buffer
        byteBuffer.put("Hello 世界\n".getBytes(StandardCharsets.UTF_8));

        // buffer -> Channel
        byteBuffer.flip();

        while (byteBuffer.hasRemaining()) {
            channel.write(byteBuffer);
        }

        channel.position(0); // 移动到文件头
        CharBuffer charBuffer = CharBuffer.allocate(20);
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();

        byteBuffer.clear();
        while (channel.read(byteBuffer) != -1 || byteBuffer.position() > 0) {
            byteBuffer.flip();
            charBuffer.clear();
            decoder.decode(byteBuffer, charBuffer, false);
            System.out.println(charBuffer.flip().toString());

            byteBuffer.compact();
        }

        channel.close();

    }

}
