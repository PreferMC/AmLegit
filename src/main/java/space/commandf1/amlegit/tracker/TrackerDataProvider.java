package space.commandf1.amlegit.tracker;

import lombok.Getter;

import java.io.Serializable;

public class TrackerDataProvider<T extends Tracker> implements Serializable {

    @Getter
    private final T tracker;

    public TrackerDataProvider(T tracker) {
        this.tracker = tracker;
    }
}
