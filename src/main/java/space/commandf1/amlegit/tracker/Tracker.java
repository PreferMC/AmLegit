package space.commandf1.amlegit.tracker;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import lombok.Getter;
import space.commandf1.amlegit.data.PlayerData;

public abstract class Tracker implements PacketListener {

    @Getter
    private final PlayerData playerData;

    public Tracker(PlayerData playerData) {
        this.playerData = playerData;
        PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.MONITOR);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        this.handle(event);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        this.handle(event);
    }

    public abstract void handle(PacketReceiveEvent event);

    public abstract void handle(PacketSendEvent event);

    public abstract <T extends TrackerDataProvider<?>> T currentDataProvider();
}
