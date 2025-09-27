package it.dominick.regionx.api.events;

import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class SettingEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final RegionSetting setting;
    private final Region region;
    private final boolean cancelled;
    private final String author;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}