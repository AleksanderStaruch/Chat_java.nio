import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.List;

public class Server extends JFrame {

    private class Th extends Thread {
        JTextArea text;
        Selector selector;
        HashMap<String,String> log = new HashMap<>();

        public Th(JTextArea text) {
            try {
                this.text = text;
                selector = Selector.open();
                log.put("admin","admin");
                log.put("client","12345");
                log.put("a","a");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                ServerSocketChannel socketChannel = ServerSocketChannel.open();
                InetSocketAddress address = new InetSocketAddress("localhost", 1111);

                socketChannel.bind(address);
                socketChannel.configureBlocking(false);
                int ops = socketChannel.validOps();
                SelectionKey selectKy = socketChannel.register(selector, ops, null);

                List<SocketChannel> list = new ArrayList<>();
                while (true) {
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey myKey = iterator.next();

                        if (myKey.isAcceptable()) {
                            SocketChannel client = socketChannel.accept();
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                            list.add(client);
                            text.append("Connection Accepted: " + client.socket() + "\n");
                        } else if (myKey.isReadable()) {
                            SocketChannel client = (SocketChannel) myKey.channel();
                            ByteBuffer bufferR = ByteBuffer.allocate(256);
                            client.read(bufferR);
                            String result = new String(bufferR.array()).trim();

                            if(result.contains("@")){
                                //login
                                result= result.substring(1);
                                String l=result.split(" ")[0];
                                String p=result.split(" ")[1];
                                if(log.containsKey(l)){
                                    if(log.get(l).equals(p)){
                                        text.append("Login Accepted: " + l + "\n");
                                    }else{
                                        text.append("Login Failed: " + l + "\n");
                                        list.remove(client);
                                        client.close();
                                    }
                                }else{
                                    text.append("Login Failed: " + l + "\n");
                                    list.remove(client);
                                    client.close();
                                }
                            }else{
                                //messages
                                text.append("Message received from "+result.split("#")[0] +" : " + result.split("#")[1]+"\n");

                                if (result.equals("STOP")) {
                                    client.close();
                                    list.remove(client);
                                    text.append("Client "+client.socket()+" closed");
                                }else{
                                    for(SocketChannel socket:list){
                                        System.out.println("dddd");
                                        if(!socket.equals(client)){
                                            byte[] tab = result.getBytes();
                                            ByteBuffer bufferW = ByteBuffer.wrap(tab);
                                            socket.write(bufferW);
                                            bufferW.clear();
                                        }
                                    }
                                }
                            }


                        }
                        iterator.remove();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public Server() {
        try{
            //gui
            setSize(450,500);
            setLayout(new BorderLayout());

            JTextArea text = new JTextArea();
            text.setEditable(false);
            JScrollPane jScrollPane = new JScrollPane(text);
            this.add(jScrollPane,BorderLayout.CENTER);

            JButton stop = new JButton("STOP");
            stop.setBackground(Color.RED);
            this.add(stop,BorderLayout.PAGE_END);

            //nio
            Th th =new Th(text);
            th.start();

            stop.addActionListener((e)->{
                try {
                    th.selector.close();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                text.append("Server closed"+"\n");
                stop.setEnabled(false);
            });
            this.setTitle("Server");
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            SwingUtilities.updateComponentTreeUI(this);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(()->new Server());
    }
}