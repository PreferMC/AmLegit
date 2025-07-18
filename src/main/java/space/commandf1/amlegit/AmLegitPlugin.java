package space.commandf1.amlegit;

import com.github.mardssss.commandlib.CommandLib;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import space.commandf1.amlegit.check.CheckManager;
import space.commandf1.amlegit.check.impl.move.groundspoof.GroundSpoofA;
import space.commandf1.amlegit.check.impl.move.keepsprint.KeepSprintA;
import space.commandf1.amlegit.check.impl.move.scaffold.ScaffoldA;
import space.commandf1.amlegit.check.impl.packet.badpacket.BadPacketsA;
import space.commandf1.amlegit.check.impl.packet.badpacket.BadPacketsB;
import space.commandf1.amlegit.check.impl.packet.badpacket.BadPacketsC;
import space.commandf1.amlegit.command.AmLegitCommand;
import space.commandf1.amlegit.config.check.CheckConfig;
import space.commandf1.amlegit.config.settings.SettingsConfig;
import space.commandf1.amlegit.listener.CheckListener;
import space.commandf1.amlegit.listener.DebugListener;
import space.commandf1.amlegit.listener.PlayerListener;

import java.util.Arrays;

public class AmLegitPlugin extends JavaPlugin {
    @Getter
    private CheckConfig checkConfig;

    @Getter
    private SettingsConfig settingsConfig;

    @Getter
    private CheckManager checkManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        CommandLib.initialize(this);

        this.checkManager = CheckManager.getManager(this);

        this.checkManager.registerChecks(
                new BadPacketsA(this),
                new BadPacketsB(this),
                new BadPacketsC(this),
                new KeepSprintA(this),
                new GroundSpoofA(this),
                new ScaffoldA(this)
        );

        this.checkConfig = new CheckConfig(this, this.checkManager);
        this.settingsConfig = new SettingsConfig(this);
        this.registerBukkitListeners(new PlayerListener(this));
        PacketEvents.getAPI().getEventManager().registerListener(new CheckListener(this),
                PacketListenerPriority.HIGHEST);

        PacketEvents.getAPI().getEventManager().registerListener(new DebugListener(), PacketListenerPriority.MONITOR);

        CommandLib.registerCommand(new AmLegitCommand(this));
    }

    private void registerBukkitListeners(Listener... listeners) {
        Arrays.stream(listeners).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}
