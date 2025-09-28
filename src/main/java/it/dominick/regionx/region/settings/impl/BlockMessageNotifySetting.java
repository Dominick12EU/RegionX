package it.dominick.regionx.region.settings.impl;

import it.dominick.regionx.api.events.SettingEvent;
import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import it.dominick.regionx.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockMessageNotifySetting extends RegionSetting {

    public BlockMessageNotifySetting(boolean active, JavaPlugin plugin, RegionManager regionManager) {
        super(active, plugin, regionManager);
    }

    @Override
    public String getName() {
        return "BLOCK_MESSAGE_NOTIFY";
    }

    @EventHandler
    public void onBlockMessageNotify(SettingEvent event) {
        Player player = event.getPlayer();
        if (shouldProcess(player, event.getRegion())) {
            handleEvent(event);
        }
    }

    @Override
    protected void handleEvent(Event event) {
        SettingEvent settingEvent = (SettingEvent) event;
        Player player = settingEvent.getPlayer();
        if (player == null) return;

        ChatUtils.send(player, "&cYou can't do this");
    }
}
