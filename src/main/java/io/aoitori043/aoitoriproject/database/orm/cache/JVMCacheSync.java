//package io.aoitori043.aoitoriproject.database.orm.cache;
//
//import io.aoitori043.aoitoriproject.database.DatabaseProperties;
//import io.aoitori043.aoitoriproject.database.orm.SQLClient;
//import io.aoitori043.aoitoriproject.database.redis.RedisCore;
//import org.zeromq.SocketType;
//import org.zeromq.ZContext;
//import org.zeromq.ZMQ;
//import redis.clients.jedis.Jedis;
//
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class JVMCacheSync {
//
//    public static class ZeroMQEntity{
//        public int serverPort;
//        public String serverId;
//
//        public HashMap<String,String> toMap(){
//            HashMap<String,String> map = new HashMap<>();
//            map.put("serverPort",String.valueOf(serverPort));
//            map.put("serverId",serverId);
//            map.put("timestamp",String.valueOf(System.currentTimeMillis()));
//            return map;
//        }
//    }
//
//    public static SQLClient sqlClient;
//    public static ZeroMQEntity zeroMQEntity;
//    public static final String redisHashKey = "zeromq";
//    public static final List<Integer> ports = new ArrayList<>();
//    public static final List<Integer> inConnectingPorts = new ArrayList<>();
//
//    public static ConcurrentHashMap<Integer,ZMQ.Socket> map = new ConcurrentHashMap<>();
//
//    //1->客户端连接服务器
//    //2->服务端回报
//    //与其它子节点服务器建立双向通信
//    public void connectJeroMQServers(List<Integer> ports){
//        for (Integer port : ports) {
//            connectJeonMQServer(port);
//        }
//    }
//
//    private synchronized static void connectJeonMQServer(Integer port) {
//        try {
//            if(zeroMQEntity.serverPort == port){
//                return;
//            }
//            if(map.containsKey(port)){
//                return;
//            }
//            if(!inConnectingPorts.contains(port)){
//                inConnectingPorts.add(port);
//            }else{
//                return;
//            }
//            ZMQ.Socket pullSocket = context.createSocket(SocketType.DEALER);
//            pullSocket.setReceiveTimeOut(5000);
//            pullSocket.connect("tcp://localhost:" + port);
//            //发送1的请求
//            pullSocket.send("1#" + DatabaseProperties.cache.zeromq$serverId);
//            String msg = pullSocket.recvStr();
//            String[] split = msg.split("#");
//            if (split[0].equals("02")) {
//                //等待2的请求
//                System.out.println("[client] 成功与 " + split[1] + " 节点建立内连接");
//                map.put(port, pullSocket);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            System.out.println("[client] 无法连接服务器 (localhost:"+ port +") :"+e.getMessage());
//        }finally {
//            inConnectingPorts.remove(port);
//        }
//    }
//
//    public static ZMQ.Socket pushSocket;
//    public static ZContext context = new ZContext();
//
//    public static Map<Integer,String> lockAggregateAllServer(String root){
//        Random random = new Random();
//        HashMap<Integer,String> replyMap = new HashMap<>();
//        for (Map.Entry<Integer, ZMQ.Socket> entry : map.entrySet()) {
//            ZMQ.Socket value = entry.getValue();
//            int i = random.nextInt(9);
//            value.send("4#"+root+"#"+ i);
//            String s = value.recvStr();
//            if(s.equals("04#"+i)){
//                replyMap.put(entry.getKey(),s);
//            }else{
//                System.out.println(s);
//            }
//        }
//        return replyMap;
//    }
//
//    public static Map<Integer,String> unlockAggregateAllServer(String root){
//        Random random = new Random();
//        HashMap<Integer,String> replyMap = new HashMap<>();
//        for (Map.Entry<Integer, ZMQ.Socket> entry : map.entrySet()) {
//            ZMQ.Socket value = entry.getValue();
//            int i = random.nextInt(9);
//            value.send("5#"+root+"#"+ i);
//            String s = value.recvStr();
//            if(s.equals("05#"+i)){
//                replyMap.put(entry.getKey(),s);
//            }else{
//                System.out.println(s);
//            }
//        }
//        return replyMap;
//    }
//
//    public static Map<Integer,String> sendToAllServer(String msg){
//        HashMap<Integer,String> replyMap = new HashMap<>();
//        for (Map.Entry<Integer, ZMQ.Socket> entry : map.entrySet()) {
//            ZMQ.Socket value = entry.getValue();
//            value.send("3#"+msg);
//            String s = new String(value.recv(),StandardCharsets.UTF_8);
//            replyMap.put(entry.getKey(),s);
//        }
//        return replyMap;
//    }
//
//
//
//    public void createJeroMQServer(int port){
//            pushSocket = context.createSocket(SocketType.ROUTER);
//            pushSocket.bind("tcp://*:"+port);
//            executorService.execute(()->{
//            while(true) {
//                byte[] clientAddress = pushSocket.recv(0);
//                String msg = pushSocket.recvStr(0);
//                String[] split = msg.split("#");
//                switch (split[0]) {
//                    case "5":{
////                        sqlClient.getLockSemaphore(LockSemaphore.LockType.JAVA_UTIL).releaseWriteLockRemote(split[1]);
//                        System.out.println("对"+split[1]+"下锁");
//                        pushSocket.send(clientAddress, ZMQ.SNDMORE);
//                        pushSocket.send("05#"+split[2], 0);
//                        break;
//                    }
//                    case "4":{
////                        CacheSemaphore.acquireWriteLockRemote(split[1]);
//                        System.out.println("对"+split[1]+"上锁");
//                        pushSocket.send(clientAddress, ZMQ.SNDMORE);
//                        pushSocket.send("04#"+split[2], 0);
//                        break;
//                    }
//                    case "3":{
//                        pushSocket.send(clientAddress, ZMQ.SNDMORE);
//                        pushSocket.send(("03#" + DatabaseProperties.cache.zeromq$serverId+"#"+"你好").getBytes(StandardCharsets.UTF_8), 0);
//                        break;
//                    }
//                    case "1":{
//                        pushSocket.send(clientAddress, ZMQ.SNDMORE);
//                        pushSocket.send(("02#" + DatabaseProperties.cache.zeromq$serverId).getBytes(), 0);
//                        try(Jedis resource = RedisCore.mainRedis.getConnection()) {
//                            int clientPort = Integer.parseInt(resource.hget(redisHashKey + "_" + split[1], "serverPort"));
//                            connectJeonMQServer(clientPort);
//                        }
//                        break;
//                    }
//                    case "2":{
//                        try(Jedis resource = RedisCore.mainRedis.getConnection()) {
//                            //服务端也关闭自己的客户端对它的服务器的连接
//                            int clientPort = Integer.parseInt(resource.hget(redisHashKey + "_" + split[1], "serverPort"));
//                            ZMQ.Socket socket = map.getData(clientPort);
//                            pushSocket.send(clientAddress, ZMQ.SNDMORE);
//                            pushSocket.send(("03#" + DatabaseProperties.cache.zeromq$serverId).getBytes(), 0);
//                            ports.remove(clientPort);
//                            inConnectingPorts.remove(clientPort);
//                            map.remove(clientPort);
//                            socket.close();
//                        }
//                        System.out.println("[server] 与 " + DatabaseProperties.cache.zeromq$serverId + " 节点断开连接");
//                        break;
//                    }
//
//
//                }
//            }
//        });
//    }
//
//    public static ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//    public static void stopZeroMQService(){
//        try(Jedis resource = RedisCore.mainRedis.getConnection()){
//            //服务器关闭前通知 所有客户端与服务端断开连接
//            for (Map.Entry<Integer, ZMQ.Socket> entry : map.entrySet()) {
//                ZMQ.Socket server = entry.getValue();
//                server.send("2#" + DatabaseProperties.cache.zeromq$serverId);
//                String msg = server.recvStr();
//                String[] split = msg.split("#");
//                System.out.println("[server] 与 " + split[0] + " 节点断开连接");
//                server.close();
//            }
//            List<String> lrange = resource.lrange(redisHashKey + "_list", 0, -1);
//            if(lrange.contains(DatabaseProperties.cache.zeromq$serverId)){
//                resource.lrem(redisHashKey + "_list", 1, DatabaseProperties.cache.zeromq$serverId);
//            }
//            resource.del(redisHashKey + "_" + DatabaseProperties.cache.zeromq$serverId);
//        }finally {
//            pushSocket.close();
//            for (Map.Entry<Integer, ZMQ.Socket> entry : map.entrySet()) {
//                entry.getValue().close();
//            }
//            context.close();
//        }
//
//    }
//
//
//    //通过zeromq与所有节点建立连接，在增删改时，直接通知对应的节点上锁
//    public JVMCacheSync(SQLClient sqlClient) {
//        this.sqlClient = sqlClient;
//        this.zeroMQEntity = new ZeroMQEntity();
//        this.zeroMQEntity.serverId = DatabaseProperties.cache.zeromq$serverId;
//        try(Jedis resource = RedisCore.getJedisPool().getResource()){
//            List<String> lrange = resource.lrange(redisHashKey + "_list", 0, -1);
//            for (String serverId : lrange) {
//                String serverPort = resource.hget(redisHashKey + "_" + serverId, "serverPort");
//                if(serverId.equals(this.zeroMQEntity.serverId) && serverPort!=null){
//                    this.zeroMQEntity.serverPort = Integer.parseInt(serverPort);
//                    continue;
//                }
//                if(serverPort==null){
//                    try {
//                        throw new RuntimeException("无法从redis获取 " + serverId + " 套接字端口");
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                    continue;
//                }
//                ports.add(Integer.parseInt(serverPort));
//            }
//            ports.sort(Collections.reverseOrder());
//            if(ports.isEmpty()){
//                zeroMQEntity.serverPort = DatabaseProperties.cache.zeromq$serverPort;
//            }else{
//                if(zeroMQEntity.serverPort == 0){
//                    zeroMQEntity.serverPort = ports.getData(0)+1;
//                }
//            }
//            HashMap<String, String> map = zeroMQEntity.toMap();
//            resource.hmset(redisHashKey + "_" + zeroMQEntity.serverId,map);
//            if(!lrange.contains(this.zeroMQEntity.serverId)){
//                resource.lpush(redisHashKey + "_list",this.zeroMQEntity.serverId);
//            }
//
//            //创建jeromq服务器
//            createJeroMQServer(zeroMQEntity.serverPort);
//            connectJeroMQServers(ports);
//        }
//
//
//    }
//
//
//
//}
