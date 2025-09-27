package it.dominick.regionx.region;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;

@RequiredArgsConstructor
@Getter
public class RegionData {
    private final String name;
    private final Location pos1;
    private final Location pos2;
}
