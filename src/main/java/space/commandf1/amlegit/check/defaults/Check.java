package space.commandf1.amlegit.check.defaults;

import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import space.commandf1.amlegit.data.PlayerData;
import space.commandf1.amlegit.exception.InvalidCheckClassException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@ToString
@EqualsAndHashCode
@Getter
public abstract class Check {
    private final String name;
    private final String description;
    private final String type;
    private final long defaultMaxVL;
    private final Plugin plugin;

    @Getter
    private final Set<PacketTypeCommon> allowedPacketTypes = new HashSet<>();

    public Check(String name, long defaultMaxVL, String description, String type, Plugin plugin) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.defaultMaxVL = defaultMaxVL;
        this.plugin = plugin;
        this.init();
    }

    private boolean hasReceivedPacketOnly, hasSentPacketOnly;

    @SneakyThrows
    private void init() {
        Method method = this.getClass().getDeclaredMethod("onCheck", CheckHandler.class);
        this.hasReceivedPacketOnly = method.getAnnotation(ReceivedPacketOnly.class) != null;
        this.hasSentPacketOnly = method.getAnnotation(SentPacketOnly.class) != null;

        if (hasReceivedPacketOnly && hasSentPacketOnly) {
            throw new InvalidCheckClassException("Both ReceivedPacketOnly and SentPacketOnly check is not allowed");
        }
    }

    public final void addAllowedPackets(PacketTypeCommon... packetTypes) {
        this.allowedPacketTypes.addAll(Arrays.stream(packetTypes).toList());
    }

    @SneakyThrows
    public final @NotNull String getInfoMessage(String description) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Description: §b").append(description).append("\n");
        List<Field> fields = new ArrayList<>(List.of(this.getClass().getDeclaredFields()));
        Class<?> superclass = this.getClass().getSuperclass();
        if (superclass != null) {
            fields.addAll(List.of(superclass.getDeclaredFields()));
        }
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            AlertDescription alertDescription = field.getAnnotation(AlertDescription.class);
            if (alertDescription == null) {
                continue;
            }
            field.setAccessible(true);
            stringBuilder.append(alertDescription.name()).append(": §b").append(field.get(this).toString());
            if (i < fields.size() - 1) {
                stringBuilder.append("\n");
            }
        }

        return  stringBuilder.toString();
    }

    public final @NotNull String getInfoMessage() {
        return this.getInfoMessage(description);
    }

    public final boolean isSetbackable() {
        return this instanceof Setbackable;
    }

    public final boolean isSentPacketOnly() {
        return this.hasSentPacketOnly;
    }

    public final boolean isReceivedPacketOnly() {
        return this.hasReceivedPacketOnly;
    }

    public abstract void onCheck(final CheckHandler handler);

    public void onCheck(final AbstractCheckHandler handler) {
        if (handler instanceof CheckHandler) {
            this.onCheck((CheckHandler) handler);
        }
    }

    protected final void runTaskSynchronously(Runnable runnable) {
        Bukkit.getScheduler().runTask(this.getPlugin(), runnable);
    }

    protected final void runTaskAsynchronously(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(this.getPlugin(), runnable);
    }

    public AbstractCheckHandler newCheckHandler(PlayerData playerData,
                                                Check check,
                                                ProtocolPacketEvent packetEvent) {
        return new CheckHandler(playerData, check, packetEvent);
    }
}
