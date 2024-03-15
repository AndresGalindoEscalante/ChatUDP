import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPChatClient {
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName("localhost");
            
            Thread hiloCliente = new Thread(() -> {
            	System.out.println("Cliente:"+ serverAddress+"/"+socket.getLocalPort()+"conectado");
                try {
                    while (true) {
                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(receivePacket);

                        String mensajeCliente = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println(mensajeCliente);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            hiloCliente.start();

            //Envio de mensajes
            BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String cadena = lector.readLine();
                byte[] sendData = cadena.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PUERTO);
                socket.send(sendPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
