package space.commandf1.amlegit.command.subs;

import com.github.mardssss.commandlib.SubCommand;
import lombok.Getter;
import lombok.val;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import space.commandf1.amlegit.AmLegitPlugin;
import space.commandf1.amlegit.config.settings.SettingsConfig;
import space.commandf1.amlegit.listener.DebugListener;
import space.commandf1.amlegit.util.ListUtil;
import space.commandf1.amlegit.util.NumberUtil;
import space.commandf1.amlegit.util.StringUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record DebugCommand(@Getter AmLegitPlugin plugin) implements SubCommand {
    private static final List<String> ALLOWED_SUBCOMMANDS =
            List.of("start", "exempt", "stop", "received" + "only", "wrapper", "max" + "buffer", "statistics");

    @Override
    public boolean executeCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                this.plugin.getSettingsConfig().getStringValue(SettingsConfig.Configs.PREFIX));
        if (!commandSender.hasPermission("amlegit.debug")) {
            commandSender.sendMessage(prefix + "You do not have permission to use this command!");
            return true;
        }

        if (strings.length < 2 || !ALLOWED_SUBCOMMANDS.contains(strings[1].toLowerCase())) {
            commandSender.sendMessage(prefix + ChatColor.GRAY +
                    "Only start, exempt, receivedOnly, wrapper, maxBuffer, statistics and stop is allowed!");
            return true;
        }

        switch (strings[1].toLowerCase()) {
            case "start":
                Player target;
                if (strings.length < 3 || (target = Bukkit.getPlayer(strings[2])) == null) {
                    commandSender.sendMessage(prefix + ChatColor.GRAY +  "Unknown player.");
                    return true;
                }

                DebugListener.addMonitor(commandSender, target);
                break;
            case "stop":
                val nums = DebugListener.removeMonitor(commandSender);
                if (nums != null) {
                    List<BigDecimal> numbers = new ArrayList<>
                            (nums.stream().map(number -> new BigDecimal(number.toString())).toList());
                    BigDecimal min = new BigDecimal(ListUtil.getMin(numbers).toString());
                    BigDecimal basePlusNumber = new BigDecimal(0);
                    if (NumberUtil.isLessThanZero(min)) {
                        basePlusNumber = new BigDecimal(min.toString().replace("-", ""));
                    }

                    BigDecimal finalBasePlusNumber = basePlusNumber;
                    List<String> percents = numbers.stream().map(number -> StringUtil.getPercentMessage(number.add(finalBasePlusNumber).doubleValue(), '|', 'f', '8', 20)).toList();
                    if (commandSender instanceof Player player) {
                        for (int i = 0; i < percents.size(); i++) {
                            String percentString = percents.get(i);
                            TextComponent component = new TextComponent(percentString);
                            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§b" + numbers.get(i).toString())}));
                            player.spigot().sendMessage(component);
                        }
                    } else {
                        commandSender.sendMessage(String.join("\n", percents));
                    }
                }
                break;
            case "received" + "only":
                DebugListener.setReceivedPacketOnly(commandSender);
                break;
            case "wrapper":
                if (strings.length < 3) {
                    commandSender.sendMessage(prefix + ChatColor.GRAY +  "You have to provide a wrapper name.");
                    commandSender.sendMessage(prefix + ChatColor.GRAY +  "E.g. play.client.WrapperPlayClientPlayerPosition");
                    return true;
                }

                DebugListener.addWrapperMonitor(commandSender,  strings[2]);
                break;
            case "exempt":
                if (strings.length < 3) {
                    commandSender.sendMessage(prefix + ChatColor.GRAY +  "You have to provide a packet name.");
                    return true;
                }
                DebugListener.addFilter(commandSender, strings[2]);
                commandSender.sendMessage(prefix + ChatColor.GRAY + strings[2] + " has been added to filters.");
                break;
            case "max" + "buffer":
                if (strings.length < 3) {
                    commandSender.sendMessage(prefix + ChatColor.GRAY +  "You have to provide a max buffer.");
                    return true;
                }

                try {
                    long buffer = Long.parseLong(strings[2]);
                    DebugListener.setMaxBuffers(commandSender, buffer);
                } catch (Exception e) {
                    commandSender.sendMessage(prefix + ChatColor.GRAY +  "Provided buffer has to be a number.");
                    return true;
                }
                break;
            case "statistics":
                if (strings.length < 3) {
                    commandSender.sendMessage(prefix + ChatColor.GRAY +
                            "You have to provide a data needed to be counted(method name).");
                    return true;
                }

                DebugListener.addCalculator(commandSender, strings[2]);
                commandSender.sendMessage(prefix + ChatColor.GRAY +
                        "Successfully added a calculator.");
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length >= 3) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        return ALLOWED_SUBCOMMANDS;
    }

    @Override
    public String getDescription() {
        return "To check packets";
    }

    @Override
    public String getPermission() {
        return null;
    }
}
