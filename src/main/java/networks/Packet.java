package networks;

import client.MultiClient;

public class Packet {

    enum Settings {
        SERVICE,
        DATA
    }

    private final String packet;

    public Packet(String message, Tools.Settings settings) {
        packet = Tools.createPacket(message, settings);
    }

    public String getString() {
        return packet;
    }
}
