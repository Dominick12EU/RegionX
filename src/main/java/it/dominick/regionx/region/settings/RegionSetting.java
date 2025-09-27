package it.dominick.regionx.region.settings;

import it.dominick.regionx.api.events.SettingEvent;
import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Setter
public abstract class RegionSetting implements Setting, Listener {

    private boolean active;
    private final JavaPlugin plugin;
    private final RegionManager regionManager;

    public RegionSetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        this.active = active;
        this.plugin = plugin;
        this.regionManager = regionManager;
    }

    @Override
    public void apply() {
        if (active) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } else {
            HandlerList.unregisterAll(this);
        }
    }

    protected abstract void handleEvent(Event event);

    protected boolean checkIfInRegion(Location location) {
        return regionManager.getRegion(location) != null;
    }

    protected void triggerAPIEvent(Player player, Event event, Region region) {
        boolean isCancelled = event instanceof Cancellable && ((Cancellable) event).isCancelled();
        String author = plugin.getName();

        SettingEvent settingEvent = new SettingEvent(player, this, region, isCancelled, author);
        Bukkit.getPluginManager().callEvent(settingEvent);
    }
}
