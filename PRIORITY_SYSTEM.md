# Region Priority System

## Overview
The priority system has been successfully implemented to allow regions to override each other's settings based on priority values, similar to WorldGuard's priority system.

## How It Works

### Priority Field
- Each region now has a `priority` field (integer value)
- Default priority is `0` for regions created without specifying a priority
- Higher priority values take precedence over lower priority values

### Overlapping Regions
When multiple regions overlap at a location:
- The `RegionManager.getRegion(Location)` method returns the region with the **highest priority**
- This means settings from the highest priority region will be active
- If priorities are equal, behavior is undefined (one will be chosen arbitrarily)

### Usage

#### Creating a Region with Default Priority (0)
```
/region setpos1
/region setpos2
/region create <name>
```

#### Setting Region Priority
```
/region setpriority <name> <priority>
```

Example:
```
/region setpriority spawn 100
/region setpriority pvp 50
```

In this example, if `spawn` and `pvp` regions overlap, the `spawn` region's settings will take precedence because it has higher priority (100 > 50).

#### Viewing Region Priority
- Use `/region gui` to open the regions GUI
- Each region item displays its priority in the lore
- Right-click a region → Shows misc menu with priority information

### Persistence
- Region priority is saved to JSON files in the `plugins/RegionX/regions/` folder
- Priority is automatically loaded when the server starts
- Backward compatibility: Old region files without priority will default to 0

### GUI Features
- Region list GUI now shows priority for each region
- Misc menu displays current priority
- Misc menu provides instructions for changing priority via command

## Technical Implementation

### Files Modified

1. **Region.java**
   - Added `priority` field
   - Added constructor with priority parameter
   - Default constructor sets priority to 0

2. **RegionManager.java**
   - `getRegion(Location)`: Returns highest priority region when multiple overlap
   - `saveRegion(Region)`: Persists priority to JSON
   - `loadRegions()`: Loads priority from JSON (defaults to 0 if not present)
   - `setRegionPriority(Region, int)`: Updates region priority
   - `addRegion()`: Overloaded to accept optional priority parameter

3. **CmdRegion.java**
   - Added `/region setpriority` command
   - Updated region item display to show priority
   - Added priority display in misc menu

## Example Scenarios

### Scenario 1: Spawn Protection
```
/region create spawn
/region setpriority spawn 100
/region create shop
/region setpriority shop 50
```
If spawn and shop overlap, spawn's settings (e.g., PVP disabled) override shop's settings.

### Scenario 2: Nested Regions
```
/region create city
/region setpriority city 10
/region create city_pvp_arena
/region setpriority city_pvp_arena 20
```
The PVP arena inside the city has higher priority, so its settings (PVP enabled) override the city's settings (PVP disabled).

### Scenario 3: Equal Priority
```
/region create region1
/region create region2
```
Both have default priority 0. If they overlap, one will be chosen arbitrarily.

## Best Practices

1. **Use a clear priority hierarchy:**
   - Global/World regions: -10 to 0
   - Standard regions: 1 to 50
   - Important regions (spawn, admin areas): 51 to 100
   - Critical override regions: 100+

2. **Document your priority scheme** in your server documentation

3. **Avoid equal priorities** for overlapping regions to prevent unpredictable behavior

4. **Test overlapping regions** to ensure the correct region's settings are applied
