package it.dominick.regionx.region.settings;

public class RegistrationInfo {
    private final String origin;
    private final RegistrationType type;

    public RegistrationInfo(String origin, RegistrationType type) {
        this.origin = origin;
        this.type = type;
    }

    public String getOrigin() {
        return origin;
    }

    public RegistrationType getType() {
        return type;
    }

    public enum RegistrationType {
        DEFAULT,
        HOOK
    }
}