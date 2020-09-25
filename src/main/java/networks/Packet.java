package networks;

import client.MultiClient;

public class Packet {
    enum Settings {
        SERVICE,
        DATA
    }

    private final int packetLength;
    private final byte[] packet;

    public Packet(String message, Tools.Settings settings) {
        packet = Tools.createPacket(message, settings);
        packetLength = packet.length;
    }

    public byte[] getBytes() {
        return packet;
    }

    public int getPacketLength() {
        return packetLength;
    }
}
