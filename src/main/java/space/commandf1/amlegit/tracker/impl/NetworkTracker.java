package space.commandf1.amlegit.tracker.impl;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.tracker.Tracker;
import space.commandf1.amlegit.util.PlayerUtil;

public class NetworkTracker extends Tracker implements Runnable {

    @Getter
    private long ping, lastPing, lastLastPing, lowestPing = Long.MAX_VALUE, highestPing = Long.MIN_VALUE;

    public NetworkTracker(PlayerData playerData) {
        super(playerData);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this.getPlayerData().getPlugin(), this, 0, 1);
    }

    @Override
    public void run() {
        PlayerData playerData = this.getPlayerData();
        int nowPing = PlayerUtil.getPing(playerData.getPlayer());
        this.lastLastPing = this.lastPing;
        this.lastPing = this.ping;
        this.ping = nowPing;

        if (lowestPing > nowPing) {
            this.lowestPing = nowPing;
        }

        if (highestPing < nowPing) {
            this.highestPing = nowPing;
        }
    }

    @Override
    public void handle(PacketReceiveEvent event) {
    }

    @Override
    public void handle(PacketSendEvent event) {
    }
}
