package space.commandf1.amlegit.check.analysis;

import lombok.Getter;
import space.commandf1.amlegit.check.defaults.AbstractCheckHandler;
import space.commandf1.amlegit.check.defaults.Check;
import space.commandf1.amlegit.check.defaults.CheckHandler;
import space.commandf1.amlegit.data.PlayerData;

import java.util.NavigableMap;

public class AnalyticCheckHandler extends AbstractCheckHandler {

    @Getter
    private final NavigableMap<Long, CheckHandler> checkHandlers;

    public AnalyticCheckHandler(PlayerData playerData,
                                Check check,
                                NavigableMap<Long, CheckHandler> checkHandlers) {
        super(playerData, check);
        this.checkHandlers = checkHandlers;
    }
}
