package at.luca.antiitemdrop;

import net.minecraft.network.Packet;

public class PacketHandler {
    public static boolean shouldSendPacket(Packet<?> packet) {
        return AntiItemDrop.shouldSendPacket(packet);
    }
}
