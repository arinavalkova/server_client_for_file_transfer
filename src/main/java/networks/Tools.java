package networks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class Tools {
    private static void closeSocket(Socket socket){
        System.out.println("Closed connection " + socket.getInetAddress() + " " + socket.getPort());
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeBuffers(BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try {
            bufferedReader.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeSocketConnection(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        closeSocket(socket);
        closeBuffers(bufferedReader, bufferedWriter);
    }
}
