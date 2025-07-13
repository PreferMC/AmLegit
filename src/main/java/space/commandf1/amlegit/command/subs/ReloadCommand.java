package space.commandf1.amlegit.command.subs;

import com.github.mardssss.commandlib.SubCommand;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import space.commandf1.amlegit.AmLegitPlugin;
import space.commandf1.amlegit.config.settings.SettingsConfig;

import java.util.List;

public record ReloadCommand(@Getter AmLegitPlugin plugin) implements SubCommand {

    @Override
    public boolean executeCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                this.plugin.getSettingsConfig().getStringValue(SettingsConfig.Configs.PREFIX));
        if (!commandSender.hasPermission("amlegit.reload")) {
            commandSender.sendMessage(prefix + "You do not have permission to use this command!");
            return true;
        }

        this.getPlugin().getSettingsConfig().reloadConfig();
        this.getPlugin().getCheckConfig().reloadConfig();

        commandSender.sendMessage(prefix + "Successfully reloaded!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Reload configurations";
    }

    @Override
    public String getPermission() {
        return null;
    }
}
