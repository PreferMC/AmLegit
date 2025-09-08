package space.commandf1.amlegit.tracker;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author commandf1
 */
@ToString
@EqualsAndHashCode
public class TrackerDataProvider<T extends Tracker> implements Serializable {

    @Getter
    private final T tracker;

    public TrackerDataProvider(T tracker) {
        this.tracker = tracker;
    }
}
