package space.commandf1.amlegit.config.check;

import lombok.Getter;
import lombok.Setter;

public class CheckConfigHolder<T> {
    @Getter
    private final String name;

    @Getter @Setter
    private T value;

    public CheckConfigHolder(String name) {
        this.name = name;
    }

    public CheckConfigHolder(String name, T value) {
        this(name);
        this.value = value;
    }
}
