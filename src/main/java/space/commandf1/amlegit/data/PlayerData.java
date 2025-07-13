package space.commandf1.amlegit.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.entity.Player;
import space.commandf1.amlegit.tracker.Tracker;
import space.commandf1.amlegit.tracker.impl.PositionTracker;
import space.commandf1.amlegit.util.PlayerUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ToString
@EqualsAndHashCode
public final class PlayerData {
    private static final Map<UUID, PlayerData> data = new HashMap<>();

    public static PlayerData getByUUID(UUID uuid) {
        return data.get(uuid);
    }

    public void setValue(String name, Object value) {
        this.values.put(name, value);
    }

    public Object getValue(String name) {
        return this.values.get(name);
    }

    @Getter
    private final Set<Tracker> trackers = new HashSet<>();

    private final Map<String, Object> values = new ConcurrentHashMap<>();

    public Optional<Tracker> getTracker(Class<? extends Tracker> trackerClass) {
        return trackers.stream().filter(trackerClass::isInstance).findFirst();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static PlayerData of(Player player) {
        return new PlayerData(player);
    }

    public int getPing() {
        return PlayerUtil.getPing(this.getPlayer());
    }

    @Getter
    private final Player player;

    @Getter @Setter
    private boolean alertEnabled = true;

    private void initTrackers() {
        trackers.add(new PositionTracker(this));
    }

    private PlayerData(Player player) {
        this.player = player;
        this.initTrackers();
        data.put(player.getUniqueId(), this);
    }
}
