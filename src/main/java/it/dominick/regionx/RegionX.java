package it.dominick.regionx;

import com.github.retrooper.packetevents.PacketEvents;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import it.dominick.regionx.commands.CmdRegion;
import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.handlers.ParticleHandler;
import it.dominick.regionx.region.settings.*;
import it.dominick.regionx.listeners.RegionListener;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.Set;

public final class RegionX extends JavaPlugin {

    private BukkitCommandManager<CommandSender> commandManager;
    @Getter
    public static RegionX instance;
    @Getter
    public RegionManager regionManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        PacketEvents.getAPI().init();
        //TODO: PVP, FALL_DAMAGE,
        // INTERACTION (MONDO), ITEM_PICKUP & DROP, EXPLOSIONS,
        // BLOCK_BURN, LEAF_DECAY, ITEMFRAME_DESTROY, BLOCK_MESSAGE_NOTIFY
        // BLOCKED_COMMANDS, ALLOWED_COMMANDS, PRIORITY_REGION
        regionManager = new RegionManager(this);

        commandManager = BukkitCommandManager.create(this);
        ParticleHandler particleHandler = new ParticleHandler();
        registerCommands(
                new CmdRegion(regionManager, particleHandler)
        );

        registerAllSettingsInPackage("it.dominick.regionx.region.settings.impl", regionManager);
        regionManager.loadRegions();

        getServer().getPluginManager().registerEvents(new RegionListener(regionManager), this);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    private void registerCommands(Object... commands) {
        for (Object command : commands) {
            commandManager.registerCommand(command);
        }
    }

    public void registerAllSettingsInPackage(String path, RegionManager regionManager) {
        Reflections reflections = new Reflections(path);
        Set<Class<? extends Setting>> settingClasses = reflections.getSubTypesOf(Setting.class);

        for (Class<? extends Setting> settingClass : settingClasses) {
            if (settingClass.equals(RegionSetting.class)) {
                continue;
            }

            try {
                Constructor<? extends Setting> constructor = settingClass.getConstructor(boolean.class, JavaPlugin.class, RegionManager.class);
                Setting settingInstance = constructor.newInstance(false, this, regionManager);
                SettingRegistryAPI registry = SettingRegistry.getInstance();
                registry.registerSetting(settingInstance);
            } catch (NoSuchMethodException e) {
                getLogger().severe("Constructor not found for setting: " + settingClass.getName() + ". Make sure it has the correct parameters.");
            } catch (Exception e) {
                getLogger().severe("Failed to register setting: " + settingClass.getName());
                e.printStackTrace();
            }
        }
    }

}
