package it.dominick.regionx.region.settings;

public interface Setting {
    String getName();
    boolean isActive();
    void apply();
}
