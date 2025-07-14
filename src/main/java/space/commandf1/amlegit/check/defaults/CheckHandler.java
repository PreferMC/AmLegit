package space.commandf1.amlegit.check.defaults;

import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import space.commandf1.amlegit.data.PlayerData;

public class CheckHandler extends AbstractCheckHandler {
    private final ProtocolPacketEvent packetEvent;

    public CheckHandler(PlayerData playerData, Check check, ProtocolPacketEvent packetEvent) {
        super(playerData, check);
        this.packetEvent = packetEvent;
    }

    public final ProtocolPacketEvent getEvent() {
        return this.packetEvent;
    }
}
