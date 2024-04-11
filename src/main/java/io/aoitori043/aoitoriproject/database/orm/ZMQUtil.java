package io.aoitori043.aoitoriproject.database.orm;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * @Author: natsumi
 * @CreateTime: 2024-04-03  12:49
 * @Description: ?
 */
public class ZMQUtil {

    public static void main(String[] args) {
        try (ZContext context = new ZContext()) {
            // 创建ROUTER套接字
            ZMQ.Socket router = context.createSocket(ZMQ.ROUTER);
            router.bind("tcp://*:5555");

            // 创建一个线程来模拟客户端
            Thread clientThread = new Thread(() -> {
                ZMQ.Socket client = context.createSocket(ZMQ.DEALER);
                client.connect("tcp://localhost:5555");

                for (int i = 0; i < 100; i++) {
                    String request = "Hello, ROUTER!";
                    client.send(request);
                    System.out.println(client.recvStr());
                }

                client.close();
            });

            clientThread.start();

            for (int i = 0; i < 3; i++) {
                int finalI = i;
                new Thread(() -> {
                    for (int i1 = 0; i1 < 100; i1++) {
                        long currentTimeMillis = System.nanoTime();
                        byte[] clientAddress = router.recv(0);
                        String msg = router.recvStr(0);
                        if (clientAddress != null && msg != null) {
                            // 打印ROUTER接收到的消息
                            System.out.println("ROUTER received: " + msg);
                            // 发送回应消息
                            router.send(clientAddress, ZMQ.SNDMORE);
                            router.send((" "+ finalI).getBytes(), 0);
                        }
                        System.out.println(System.nanoTime()-currentTimeMillis+"ms");
                    }
                }).start();
            }

            // 等待客户端线程结束
            clientThread.join();

            router.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
