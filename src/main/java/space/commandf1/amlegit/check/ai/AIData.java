package space.commandf1.amlegit.check.ai;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

public class AIData implements Cloneable, ConfigurationSerializable {
    @Override
    public Map<String, Object> serialize() {
        return Map.of();
    }

    public static AIData deserialize(Map<String, Object> args) {
        return new AIData();
    }

    @Override
    public AIData clone() {
        try {
            return (AIData) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
