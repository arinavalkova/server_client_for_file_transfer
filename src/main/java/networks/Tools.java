package networks;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Tools {

    private static void closeSocket(Socket socket) {
        System.out.println("Closed connection " + socket.getInetAddress() + " " + socket.getPort());
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeStreams(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        try {
            dataInputStream.close();
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeSocketConnection(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        closeSocket(socket);
        closeStreams(dataInputStream, dataOutputStream);
    }

    public static byte[] createHeader(byte[] pathName) {
        String pathNameString = new String(pathName);
        File file = new File(pathNameString);

        byte[] hash = getHash(pathNameString);
        byte[] fileName = file.getName().getBytes();

        byte[] header = new byte[Integer.BYTES + fileName.length + Integer.BYTES + hash.length + Integer.BYTES];

        int i = 0;
        System.arraycopy(ByteBuffer.allocate(Integer.BYTES).putInt(fileName.length).array(), 0, header, i, Integer.BYTES);

        i += Integer.BYTES;

        System.arraycopy(fileName, 0, header, i, fileName.length);

        i += fileName.length;

        System.arraycopy(ByteBuffer.allocate(Integer.BYTES).putInt(hash.length).array(), 0, header, i, Integer.BYTES);

        i += Integer.BYTES;

        System.arraycopy(hash, 0, header, i, hash.length);

        i += hash.length;

        int fileLength = (int) (file.length() % Consts.bufferSize == 0 ? file.length() / Consts.bufferSize : file.length() / Consts.bufferSize + 1);

        System.arraycopy(ByteBuffer.allocate(Integer.BYTES).putInt(fileLength).array(), 0, header, i, Integer.BYTES);

        return header;
    }

    private static byte[] getHash(String pathNameString) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(pathNameString);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return md.digest();
    }

    public enum Settings {
        SERVICE,
        DATA
    }

    public static void sendBytes(DataOutputStream out, byte[] array, Settings settings) {

        if (settings.equals(Settings.SERVICE)) {
            sendPacket(out, new Packet(array));
        } else {
            sendFile(out, array);
        }
    }

    private static void sendFile(DataOutputStream out, byte[] array) {
        sendPacket(out, new Packet(createHeader(array)));

        String fileName = new String(array);
        File file = new File(fileName);
        long fileLength = file.length();

        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);

            long currentFileLength = fileLength;
            while (currentFileLength > 0) {
                if (currentFileLength < Consts.bufferSize) {
                    sendPacket(out, new Packet(readBytes(fileInputStream, currentFileLength)));
                    break;
                }

                Packet packet = new Packet(readBytes(fileInputStream, Consts.bufferSize));
                sendPacket(out, packet);

                currentFileLength -= Consts.bufferSize;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readBytes(FileInputStream fileInputStream, long length) {
        byte[] answer = new byte[(int) length];
        try {
            for (int i = 0; i < length; i++) {
                answer[i] = (byte) fileInputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

    private static void sendPacket(DataOutputStream out, Packet packet) {
        try {
            out.writeInt(packet.getPacketLength());
            out.write(packet.getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getBytes(DataInputStream in, Tools.Settings settings, String path) {
        if (settings.equals(Settings.SERVICE)) {
            return getPacket(in);
        } else {
            return getFile(in, path);
        }
    }

    private static byte[] getFile(DataInputStream in, String path) {
        byte[] headerArray = getPacket(in);

        Header header = new Header(headerArray);

        File file = new File(path + header.getFileName());

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            for(int i = 0; i < header.getCountOfPackets(); i++) {
                byte[] a = getPacket(in);
                fileOutputStream.write(a);
            }

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        byte[] hashGottenFile = Tools.getHash(path + header.getFileName());

        if (Arrays.equals(hashGottenFile, header.getFileHash())) {
            return header.getFileName().getBytes();
        } else {
            System.out.println("Hashes are not the same");
            file.delete();
            return null;
        }
    }

    public static byte[] getPacket(DataInputStream in) {
        try {
            int packetLength = in.readInt();
            return in.readNBytes(packetLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}