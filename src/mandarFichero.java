import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class mandarFichero {
    private static final int PUERTO = 12345;
    private static Set<InetSocketAddress> clientes = new HashSet<>();

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(PUERTO);
            System.out.println("Servidor UDP iniciado en el puerto " + PUERTO);

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);

                InetAddress clientAddress = receivePacket.getAddress();
                int puertoCliente = receivePacket.getPort();
                InetSocketAddress socketCliente = new InetSocketAddress(clientAddress, puertoCliente);

                if (!clientes.contains(socketCliente)) {
                    clientes.add(socketCliente);
                }

                // Manejar el mensaje entrante
                mensajeEntrante(socket, receivePacket, socketCliente);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mensajeEntrante(DatagramSocket socket, DatagramPacket paqueteRecibido, InetSocketAddress direccionEnvio) throws IOException {
        byte[] data = paqueteRecibido.getData();
        String mensaje = new String(data, 0, paqueteRecibido.getLength());

        // Ver si se esta recibiendo un archivo o un mensaje
        if (mensaje.startsWith("Archivo:")) {
            gestionarMensajeArchivo(socket, paqueteRecibido, mensaje.substring(8), direccionEnvio);
        } else {
            System.out.println("Mensaje recibido del cliente " + direccionEnvio.getAddress() + ":" + direccionEnvio.getPort() + ": " + mensaje);
            mensajeGlobal(socket, "[" + direccionEnvio.getAddress() + ":" + direccionEnvio.getPort() + "] " + mensaje);
        }
    }

    private static void gestionarMensajeArchivo(DatagramSocket socket, DatagramPacket paqueteRecibido, String nombreFichero, InetSocketAddress direccionEnvio) throws IOException {
        // Guardo el archivo en el servidor
        String directorioArchivos = "C:\\Users\\Andres\\eclipse3\\FTPChatPractica\\ficheros";
        byte[] data = paqueteRecibido.getData();
        FileOutputStream fileOutputStream = new FileOutputStream(directorioArchivos+""+nombreFichero);
        fileOutputStream.write(data, 8, paqueteRecibido.getLength() - 8); 
        fileOutputStream.close();

        // Envio el archivo a todos los clientes
        enviarFichero(socket, nombreFichero, direccionEnvio);
    }

    private static void mensajeGlobal(DatagramSocket socket, String message) throws IOException {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);

        for (InetSocketAddress clientAddress : clientes) {
            sendPacket.setSocketAddress(clientAddress);
            socket.send(sendPacket);
        }
    }

    private static void enviarFichero(DatagramSocket socket, String nombreFichero, InetSocketAddress direccionEnvio) throws IOException {
        byte[] datosFichero = leerFichero(nombreFichero);

        for (InetSocketAddress direccionCliente : clientes) {
            DatagramPacket enviarPaquete = new DatagramPacket(datosFichero, datosFichero.length, direccionCliente);
            socket.send(enviarPaquete);
        }
    }

    private static byte[] leerFichero(String filename) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        byte[] data = new byte[fileInputStream.available()];
        fileInputStream.read(data);
        fileInputStream.close();
        return data;
    }
}
