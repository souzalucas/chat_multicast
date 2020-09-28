
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Properties;

/**
 * Gerencia o protocolo e o processamento das mensagens
 * @author rodrigo
 */
public class ProtocolController {

    private final MulticastSocket multicastSocket;
    private final DatagramSocket udpSocket;
    private final InetAddress group;
    private final Integer mport, uport;
    private final String nick;
    private final HashMap<String, InetAddress> onlineUsers;
    private final UIControl ui;

    public ProtocolController(Properties properties) throws IOException {
        mport = (Integer) properties.get("multicastPort");
        uport = (Integer) properties.get("udpPort");
        group = (InetAddress) properties.get("multicastIP");
        nick = (String) properties.get("nickname");
        ui = (UIControl) properties.get("UI");

        multicastSocket = new MulticastSocket(mport);
        udpSocket = new DatagramSocket(uport);
        
        onlineUsers = new HashMap<>();
        onlineUsers.put("Todos", group);  
    }

    public void send(String targetUser, String msg) throws IOException {
        Byte type;

        if (targetUser.equals("Todos") == true) {
            if (msg.equals("JOIN")) 
                type = 1;
            else if (msg.equals("LEAVE")) 
                type = 5;
            else 
                type = 3;

            /* Cria a mensagem */
            Message message = new Message(type, nick, msg);
            sendMessageGroup(message);
       
        } else {
            if (msg.equals("JOINACK")) 
                type = 2; 
            else
                type = 4;

            /* Cria a mensagem */
            Message message = new Message(type, nick, msg);
            sendMessage(message, onlineUsers.get(targetUser));
        }
    }

    private void sendMessageGroup(Message msg) throws IOException {
        byte [] m = msg.getBytes();
        String noticeString = new String("0");

        /* Criptografa mensagem, se necessário */
        if (ui.messageEncrypted() == true) {
            String mString = new String(m);
            String encryptedString = AES.encrypt(mString, "BCC36C.IC6A");
            m = encryptedString.getBytes();

            noticeString  = new String("1");
        }

        /* Envia primeira mensagem */
        byte [] noticeBytes = noticeString.getBytes();
        DatagramPacket messageOut = new DatagramPacket(noticeBytes, noticeBytes.length, group, mport);
        multicastSocket.send(messageOut);

        /* Envia segunda mensagem */
        String tam = Integer.toString(m.length);
        messageOut = new DatagramPacket(tam.getBytes(), tam.getBytes().length, group, mport);
        udpSocket.send(messageOut);

        /* Envia terceira mensagem */
        messageOut = new DatagramPacket(m, m.length, group, mport);
        multicastSocket.send(messageOut);	
    }

    private void sendMessage(Message msg, InetAddress target) throws IOException {
        byte [] m = msg.getBytes();
        String noticeString = new String("0");

        /* Criptografa mensagem, se necessário */
        if (ui.messageEncrypted() == true) {
            String mString = new String(m);
            String encryptedString = AES.encrypt(mString, "BCC36C.IC6A");
            m = encryptedString.getBytes();

            noticeString  = new String("1");
        }

        /* Envia primeira mensagem */
        byte [] noticeBytes = noticeString.getBytes();
        DatagramPacket messageOut = new DatagramPacket(noticeBytes, noticeBytes.length, target, uport);
        udpSocket.send(messageOut);

        /* Envia segunda mensagem */
        String tam = Integer.toString(m.length);
        messageOut = new DatagramPacket(tam.getBytes(), tam.getBytes().length, target, uport);
        udpSocket.send(messageOut);

        /* Envia terceira mensagem */
        messageOut = new DatagramPacket(m, m.length, target, uport);
        udpSocket.send(messageOut);	
    }

    public void join() throws IOException {
        multicastSocket.joinGroup(group);
    }

    public void leave() throws IOException {
        multicastSocket.leaveGroup(group);	
    }
    
    public void close() throws IOException {
        if (udpSocket != null) udpSocket.close();
        if (multicastSocket != null) multicastSocket.close();
    }

    public void processPacket(DatagramPacket p, int encrypted) throws IOException {
        Message message;
        
        /* Descriptografa a mensagem, se necessário */
        if(encrypted == 1) {
            String mString = new String(p.getData());
            String decryptedString = AES.decrypt(mString, "BCC36C.IC6A");
            /* Cria message */
            message = new Message(decryptedString.getBytes());   
        } else {
            /* Cria Message */
            message = new Message(p.getData()); 
        }

        /* Obtem o apelido de quem enviou a mensagem */
        String senderNick = message.getSource();   

        if (message.getType() == 1) {
            if(nick.equals(senderNick) == false) {
                /* Salva o apelido e endereço na lista de usuários ativos */
                onlineUsers.put(senderNick, p.getAddress());
                /* Envia JOINACK */
                send(senderNick, "JOINACK");
            }

        } else if (message.getType() == 2) {
            /* Salva o apelido e endereço na lista de suários ativos */
            onlineUsers.put(senderNick, p.getAddress());

        } else if (message.getType() == 5) {
            /* remove o apelido e endereço da lista de suários ativos */
            onlineUsers.remove(senderNick);
        }
        /* Atualiza UI */
        ui.update(message);
    }

    public void receiveMulticastPacket() throws IOException {
        /* Recebe a primeira mensagem */
        byte[] buffer = new byte[1];
        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
        multicastSocket.receive(messageIn);
        
        /* Verifica se a mensagem é ou não criptografada */
        byte[] msgBytes = messageIn.getData();
        String msgString = new String(msgBytes);
        int encrypted =  Integer.valueOf(msgString.trim());
        
        /* Recebe a segunda mensagem */
        buffer = new byte[4];
        messageIn = new DatagramPacket(buffer, buffer.length);
        multicastSocket.receive(messageIn);

        /* Verifica tamanho da mensagem */
        msgBytes = messageIn.getData();
        msgString = new String(msgBytes);
        int tam = Integer.valueOf(msgString.trim());

        /* Recebe a terceira mensagem */
        buffer = new byte[tam];
        messageIn = new DatagramPacket(buffer, buffer.length);
        multicastSocket.receive(messageIn);

        /* Processa a terceira mensagem */
        processPacket(messageIn, encrypted);       
    }

    public void receiveUdpPacket() throws IOException {
        /* Recebe a primeira mensagem */
        byte[] buffer = new byte[1];
        DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
        udpSocket.receive(messageIn);
        
        /* Verifica se a mensagem é ou não criptografada */
        byte[] msgBytes = messageIn.getData();
        String msgString = new String(msgBytes);
        int encrypted =  Integer.valueOf(msgString.trim());
        
        /* Recebe a segunda mensagem */
        buffer = new byte[4];
        messageIn = new DatagramPacket(buffer, buffer.length);
        udpSocket.receive(messageIn);

        /* Verifica tamanho da mensagem */
        msgBytes = messageIn.getData();
        msgString = new String(msgBytes);
        int tam = Integer.valueOf(msgString.trim());

        /* Recebe a terceira mensagem */
        buffer = new byte[tam];
        messageIn = new DatagramPacket(buffer, buffer.length);
        udpSocket.receive(messageIn);

        /* Processa a terceira mensagem */
        processPacket(messageIn, encrypted);      
    }
}
