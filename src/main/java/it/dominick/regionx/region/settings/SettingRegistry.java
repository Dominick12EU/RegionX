package it.dominick.regionx.region.settings;

import it.dominick.regionx.region.Region;
import lombok.Getter;

import java.util.*;

public class SettingRegistry implements SettingRegistryAPI {

    private static final String MAIN_PLUGIN_NAME = "RegionX";
    @Getter
    private static final SettingRegistry instance = new SettingRegistry();

    @Getter
    private final Map<String, Setting> registeredSettings = new HashMap<>();
    private final Map<String, RegistrationInfo> registrationInfos = new HashMap<>();
    private final Map<String, Map<Region, RegistrationInfo>> pendingSettings = new HashMap<>();
    private final Set<String> settingAuthors = new HashSet<>();

    private SettingRegistry() {}

    @Override
    public void registerSetting(Setting setting) {
        String callerPackage = getCallerPlugin();
        RegistrationInfo.RegistrationType type = callerPackage.startsWith(MAIN_PLUGIN_NAME)
                ? RegistrationInfo.RegistrationType.DEFAULT
                : RegistrationInfo.RegistrationType.HOOK;

        registerSetting(setting, callerPackage, type);
    }

    public void registerSetting(Setting setting, String origin, RegistrationInfo.RegistrationType type) {
        String settingName = setting.getName().toUpperCase();
        registeredSettings.put(settingName, setting);
        registrationInfos.put(settingName, new RegistrationInfo(origin, type));
        settingAuthors.add(origin);
        if (type == RegistrationInfo.RegistrationType.HOOK) {
            applyPendingSettings(settingName);
        }
    }

    @Override
    public Setting getSetting(String name) {
        return registeredSettings.get(name.toUpperCase());
    }

    @Override
    public void unregisterSetting(String name) {
        String settingName = name.toUpperCase();
        registeredSettings.remove(settingName);
        registrationInfos.remove(settingName);
        pendingSettings.remove(settingName);

    }

    @Override
    public void applySettingToRegion(String settingName, Region region) {
        RegionSetting setting = (RegionSetting) getSetting(settingName);
        if (setting != null) {
            setting.setActive(true);
            setting.apply();
            region.getSettings().put(settingName.toUpperCase(), setting);
        } else {
            pendingSettings.computeIfAbsent(settingName.toUpperCase(), k ->
                            new HashMap<>())
                    .put(region, new RegistrationInfo(getCallerPlugin(), RegistrationInfo.RegistrationType.HOOK));
        }
    }

    @Override
    public void removeSettingFromRegion(String settingName, Region region) {
        RegionSetting setting = region.getSettings().remove(settingName.toUpperCase());
        if (setting != null) {
            setting.setActive(false);
            setting.apply();
        }
    }

    public RegistrationInfo getRegistrationInfo(String name) {
        return registrationInfos.get(name.toUpperCase());
    }

    private void applyPendingSettings(String settingName) {
        if (pendingSettings.containsKey(settingName.toUpperCase())) {
            for (Map.Entry<Region, RegistrationInfo> entry : pendingSettings.get(settingName.toUpperCase()).entrySet()) {
                Region region = entry.getKey();
                applySettingToRegion(settingName.toUpperCase(), region);
            }
            pendingSettings.remove(settingName.toUpperCase());
        }
    }

    private String getCallerPlugin() {
        String callerClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        String[] classParts = callerClassName.split("\\.");
        return classParts[classParts.length - 1];
    }

    public List<String> getSettingsByAuthor(String author) {
        List<String> settingsByAuthor = new ArrayList<>();
        for (Map.Entry<String, RegistrationInfo> entry : registrationInfos.entrySet()) {
            if (entry.getValue().getOrigin().equals(author)) {
                settingsByAuthor.add(entry.getKey());
            }
        }
        return settingsByAuthor;
    }

    public Set<String> getSettingAuthors() {
        return new HashSet<>(settingAuthors);
    }
}