package space.commandf1.amlegit.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DebugListener implements PacketListener {
    private static final Map<CommandSender, Player> monitors = new ConcurrentHashMap<>();
    private static final Map<CommandSender, List<String>> filters = new ConcurrentHashMap<>();
    private static final Map<CommandSender, String> wrapperMonitors = new ConcurrentHashMap<>();
    private static final Map<CommandSender, Boolean> receivedPacketOnly = new ConcurrentHashMap<>();
    private static final Map<CommandSender, Long> maxBuffers = new ConcurrentHashMap<>();
    private static final Map<CommandSender, Long> buffers = new ConcurrentHashMap<>();
    private static final Map<CommandSender, String> calculators = new ConcurrentHashMap<>();
    private static final Map<CommandSender, List<Number>> calculatedData = new ConcurrentHashMap<>();

    public static void addMonitor(CommandSender sender, Player target) {
        monitors.put(sender, target);
        filters.put(sender, Collections.synchronizedList(new ArrayList<>()));
        receivedPacketOnly.put(sender, false);
        maxBuffers.put(sender, 5L);
        buffers.put(sender, 0L);
    }

    public static void addCalculator(CommandSender sender, String targetMethod) {
        calculators.put(sender, targetMethod);
        calculatedData.put(sender, new ArrayList<>());
    }

    public static void setMaxBuffers(CommandSender sender, long buffer) {
        maxBuffers.put(sender, buffer);
    }

    public static void addFilter(CommandSender sender, String filter) {
        List<String> senderFilters = filters.computeIfAbsent(sender, k ->
                Collections.synchronizedList(new ArrayList<>())
        );
        senderFilters.add(filter);
    }

    public static void addWrapperMonitor(CommandSender sender, String wrapperName) {
        wrapperMonitors.put(sender, wrapperName);
    }

    public static void setReceivedPacketOnly(CommandSender sender) {
        receivedPacketOnly.put(sender, true);
    }

    public static @Nullable List<Number> removeMonitor(CommandSender sender) {
        monitors.remove(sender);
        filters.remove(sender);
        wrapperMonitors.remove(sender);
        receivedPacketOnly.remove(sender);
        maxBuffers.remove(sender);
        buffers.remove(sender);
        List<Number> toReturn = calculatedData.remove(sender);
        calculators.remove(sender);
        return toReturn;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        this.processEvent(event);
    }

    private void processEvent(ProtocolPacketEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        Set<CommandSender> senders = new HashSet<>(monitors.keySet());

        for (CommandSender sender : senders) {
            Player target = monitors.get(sender);
            if (target == null || !target.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            String packetName = event.getPacketType().toString();
            Boolean isReceiveOnly = receivedPacketOnly.get(sender);

            if (isReceiveOnly != null && isReceiveOnly && !(event instanceof PacketReceiveEvent)) {
                continue;
            }

            List<String> senderFilters;
            synchronized (filters) {
                senderFilters = filters.get(sender);
            }

            String wrapperName = wrapperMonitors.get(sender);
            if (wrapperName != null) {
                handleWrapperMonitor(sender, event, wrapperName);
            }
            else if (senderFilters != null && !senderFilters.contains(packetName)) {
                sender.sendMessage(packetName);
            }
        }
    }

    private void handleWrapperMonitor(CommandSender sender, ProtocolPacketEvent event, String wrapperName) {
        Long maxBuffer = maxBuffers.get(sender);
        Long buffer = buffers.get(sender);
        buffer++;
        if (buffer < maxBuffer) {
            buffers.put(sender, buffer);
            return;
        } else {
            buffers.put(sender, 0L);
        }

        try {
            String fullClassName = "com.github.retrooper.packetevents.wrapper." + wrapperName;
            Class<?> targetClass = Class.forName(fullClassName);

            Optional<Constructor<?>> constructorOpt = Arrays.stream(targetClass.getDeclaredConstructors())
                    .filter(con -> con.getParameterCount() == 1)
                    .findFirst();

            if (constructorOpt.isEmpty()) {
                sender.sendMessage("§cNo suitable constructor found in " + fullClassName);
                return;
            }

            Constructor<?> constructor = constructorOpt.get();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance(event);

            List<String> messages = new ArrayList<>();
            messages.add("Time: §b" + System.currentTimeMillis() + "§f");

            for (Method method : targetClass.getDeclaredMethods()) {
                if ((method.getName().startsWith("get") || method.getName().startsWith("is"))
                        && method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    String name = "";
                    try {
                        name = method.getName().replace("get", "").replace("is", "");
                        Object value = method.invoke(instance);
                        messages.add(name + ": §b" + (value != null ? value.toString() : "null") + "§f");
                    } catch (Exception e) {
                        messages.add(name + ": §cError - " + e.getClass().getSimpleName());
                    }
                }
            }

            sender.sendMessage(String.join("\n", messages));

            handleCalculator(sender, instance);
        } catch (Exception e) {
            String errorDetail = "Wrapper: " + wrapperName + "\n";
            errorDetail += "Error: " + e.getClass().getName() + "\n";
            if (e.getMessage() != null) {
                errorDetail += "Message: " + e.getMessage();
            }
            sender.sendMessage("§c" + errorDetail);
        }
    }

    private void handleCalculator(CommandSender sender, Object instance) {
        String methodChain = calculators.get(sender);
        if (methodChain == null) return;

        try {
            List<Number> dataList = calculatedData.get(sender);
            if (dataList == null) {
                sender.sendMessage("§cCalculator data list not initialized");
                return;
            }

            Object current = instance;
            String[] methods = methodChain.split("\\.");

            for (String methodName : methods) {
                Method method = current.getClass().getDeclaredMethod(methodName);
                method.setAccessible(true);
                current = method.invoke(current);

                if (current == null) {
                    sender.sendMessage("§cMethod chain broken at '" + methodName + "': returned null");
                    return;
                }
            }

            if (current instanceof Number) {
                dataList.add((Number) current);
            } else {
                sender.sendMessage("§cResult is not a number: " + current.getClass().getName());
            }
        } catch (NoSuchMethodException e) {
            sender.sendMessage("§cMethod not found: " + e.getMessage());
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            sender.sendMessage("§cCalculator error: " + errorMsg);
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        this.processEvent(event);
    }
}
