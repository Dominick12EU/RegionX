package it.dominick.regionx.api.events;

import it.dominick.regionx.region.Region;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class RegionQuitEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Region region;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
