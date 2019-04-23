import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Client extends JFrame{

    private class Th extends Thread {
        JTextArea text;
        SocketChannel client;
        public Th(JTextArea text,SocketChannel client) {
            this.text = text;
            this.client=client;
        }

        public void run() {
            try {
                while(true){
                    ByteBuffer bufferR = ByteBuffer.allocate(256);
                    client.read(bufferR);
                    String result = new String(bufferR.array()).trim();
                    System.out.println(result);

                    if(result.split("#").length==2){
                        text.append("Message received from "+result.split("#")[0] +" : " + result.split("#")[1]+"\n");
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private String name="1";

    private void log(SocketChannel client){
        //login
        JLabel label_login = new JLabel("Username:");JTextField login = new JTextField();
        JLabel label_password = new JLabel("Password:");JPasswordField password = new JPasswordField();
        Object[] array = { label_login,  login, label_password, password };

        int res = JOptionPane.showConfirmDialog(null, array, "Login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            name=login.getText().trim();
            String m ="@"+login.getText().trim()+" "+new String(password.getPassword());
            byte[] tab = m.getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(tab);
            try {
                client.write(buffer);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            buffer.clear();
        }
    }


    public Client() {

        try{
            InetSocketAddress address = new InetSocketAddress("localhost", 1111);
            SocketChannel client = SocketChannel.open(address);

            //login
            log(client);

            //GUI
            setSize(450,500);
            setLayout(new BorderLayout());

            JTextArea text = new JTextArea();
            text.setEditable(false);
            JScrollPane jScrollPane = new JScrollPane(text);
            this.add(jScrollPane,BorderLayout.CENTER);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(1,2));
            this.add(panel,BorderLayout.PAGE_END);

            JTextArea message= new JTextArea();panel.add(message);
            JButton send = new JButton("SEND");panel.add(send);

            Th th= new Th(text,client);
            th.start();

            send.addActionListener((e)->{
                String mes=message.getText();
                if(!mes.isEmpty() || !mes.equals("")){
                    text.append("ME:"+mes+"\n");
                    mes=name+"#"+mes;
                    byte[] tab = mes.getBytes();
                    ByteBuffer buffer = ByteBuffer.wrap(tab);
                    try {
                        client.write(buffer);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    buffer.clear();

                    if(mes.equals("STOP")){
                        try {
                            client.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    message.setText("");
                }
            });

            this.setTitle("Client "+name);
            this.setVisible(true);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            SwingUtilities.updateComponentTreeUI(this);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void main(String[] args){
        EventQueue.invokeLater(()->new Client());
    }
}