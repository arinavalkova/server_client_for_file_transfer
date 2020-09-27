package networks;

public class Packet {
    enum Settings {
        SERVICE,
        DATA
    }

    private final int packetLength;
    private final byte[] message;

    public Packet(byte[] message) {
        this.message = message;
        packetLength = message.length;
    }

    public byte[] getBytes() {
        return message;
    }

    public int getPacketLength() {
        return packetLength;
    }
}
