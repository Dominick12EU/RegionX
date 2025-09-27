package it.dominick.regionx.region.settings;

import it.dominick.regionx.region.Region;

public interface SettingRegistryAPI {
    void registerSetting(Setting setting);

    Setting getSetting(String name);

    void unregisterSetting(String name);

    void applySettingToRegion(String settingName, Region region);

    void removeSettingFromRegion(String settingName, Region region);
}