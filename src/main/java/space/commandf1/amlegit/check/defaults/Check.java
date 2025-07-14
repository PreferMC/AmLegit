package space.commandf1.amlegit.check.defaults;

import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import org.bukkit.plugin.Plugin;
import space.commandf1.amlegit.exception.InvalidCheckClassException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    public final String getInfoMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Description: §b").append(this.description).append("\n");
        for (int i = 0; i < this.getClass().getDeclaredFields().length; i++) {
            Field field = this.getClass().getDeclaredFields()[i];
            AlertDescription alertDescription = field.getAnnotation(AlertDescription.class);
            if (alertDescription == null) {
                continue;
            }
            field.setAccessible(true);
            stringBuilder.append(alertDescription.name()).append(": §b").append(field.get(this).toString());
            if (i < this.getClass().getDeclaredFields().length - 1) {
                stringBuilder.append("\n");
            }
        }

        return  stringBuilder.toString();
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

    public abstract void onCheck(CheckHandler handler);
}
