package space.commandf1.amlegit.command.subs;

import com.github.mardssss.commandlib.SubCommand;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import space.commandf1.amlegit.AmLegitPlugin;
import space.commandf1.amlegit.config.settings.SettingsConfig;
import space.commandf1.amlegit.data.PlayerData;

import java.util.List;

public record AlertCommand(@Getter AmLegitPlugin plugin) implements SubCommand {

    @Override
    public boolean executeCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                this.plugin.getSettingsConfig().getStringValue(SettingsConfig.Configs.PREFIX));
        if (!commandSender.hasPermission("amlegit.alert")) {
            commandSender.sendMessage(prefix + "You do not have permission to use this command!");
            return true;
        }

        if (commandSender instanceof Player player) {
            PlayerData playerData = PlayerData.getByUUID(player.getUniqueId());
            if (playerData == null) {
                return false;
            }

            playerData.setAlertEnabled(!playerData.isAlertEnabled());
            player.sendMessage(prefix + "Alert has been " + (playerData.isAlertEnabled()
                    ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
        } else {
            commandSender.sendMessage(prefix + "This command can only be executed by players.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "To toggle alert";
    }

    @Override
    public String getPermission() {
        return null;
    }
}
