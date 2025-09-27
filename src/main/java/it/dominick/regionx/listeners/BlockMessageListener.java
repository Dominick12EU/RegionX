package it.dominick.regionx.listeners;

import it.dominick.regionx.RegionX;
import it.dominick.regionx.api.events.SettingEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class BlockMessageListener implements Listener {

    private final RegionX plugin;


    @EventHandler
    public void onBlockMessage(SettingEvent event) {
        if (event.getAuthor().equals(plugin.getName())) {

        }
    }
}
