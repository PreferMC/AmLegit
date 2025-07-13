package space.commandf1.amlegit.command;

import com.github.mardssss.commandlib.BaseCommand;
import com.github.mardssss.commandlib.SubCommand;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import space.commandf1.amlegit.AmLegitPlugin;
import space.commandf1.amlegit.command.subs.AlertCommand;
import space.commandf1.amlegit.command.subs.DebugCommand;
import space.commandf1.amlegit.command.subs.ReloadCommand;
import space.commandf1.amlegit.config.settings.SettingsConfig;

import java.lang.reflect.Field;
import java.util.Map;

public final class AmLegitCommand extends BaseCommand {
    @Getter
    private final AmLegitPlugin plugin;

    public AmLegitCommand(AmLegitPlugin plugin) {
        super("amlegit");
        this.plugin = plugin;

        this.registerSubCommand("alert", new AlertCommand(this.getPlugin()));
        this.registerSubCommand("reload", new ReloadCommand(this.getPlugin()));
        this.registerSubCommand("debug", new DebugCommand(this.getPlugin()));
        this.setUsageMessage(this.getCommandUsageMessage());
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private String getCommandUsageMessage() {
        Field subCommandsField = this.getClass().getSuperclass().getDeclaredField("subCommands");
        subCommandsField.setAccessible(true);
        Map<String, SubCommand> subCommands = (Map<String, SubCommand>) subCommandsField.get(this);

        String prefix = ChatColor.translateAlternateColorCodes('&',
                this.plugin.getSettingsConfig().getStringValue(SettingsConfig.Configs.PREFIX));
        return prefix + ChatColor.GRAY + "AmLegit commands (" + ChatColor.YELLOW + subCommands.size() +
                ChatColor.GRAY + "): " + '\n' + String.join("\n", subCommands
                .keySet()
                .stream()
                .map(subCommandName -> prefix + ChatColor.AQUA + "/" + this.getCommandName() + " " +
                        subCommandName + ChatColor.GRAY + " - " + subCommands.get(subCommandName).getDescription())
                .toList());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                this.plugin.getSettingsConfig().getStringValue(SettingsConfig.Configs.PREFIX));

        if (!sender.hasPermission("amlegit.command")) {
            sender.sendMessage(prefix + "You do not have permission to use this command!");
            return true;
        }
        return super.onCommand(sender, cmd, label, args);
    }
}
