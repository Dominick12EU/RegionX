package it.dominick.regionx.commands;

import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.annotations.Command;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import it.dominick.regionx.RegionX;
import it.dominick.regionx.api.player.UserRegion;
import it.dominick.regionx.manager.RegionManager;
import it.dominick.regionx.region.Region;
import it.dominick.regionx.region.settings.RegionSetting;
import it.dominick.regionx.region.settings.SettingRegistry;
import it.dominick.regionx.utils.ChatUtils;
import it.dominick.regionx.utils.LocationUtil;
import it.dominick.regionx.utils.RegionsGuiUtil;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Command("region")
public class CmdRegion {
    private final RegionManager regionManager;
    private final it.dominick.regionx.handlers.ParticleHandler particleHandler;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Command("setpos1")
    @Permission("region.admin")
    public void onSetPos1(CommandSender sender) {
        if (sender instanceof Player player) {
            Location pos1 = player.getLocation().clone()
                    .subtract(0, 1, 0).getBlock().getLocation();

            regionManager.setPlayerPos1(player, pos1);
            ChatUtils.send(player, "&aPosizione 1 impostata: " + formatLocation(pos1));

            Location pos2 = regionManager.getPlayerPos2(player);
            if (pos2 != null) {
                particleHandler.showAreaParticles(player, pos1, pos2);
            }
        }
    }

    @Command("setpos2")
    @Permission("region.admin")
    public void onSetPos2(CommandSender sender) {
        if (sender instanceof Player player) {
            Location pos1 = regionManager.getPlayerPos1(player);
            Location pos2 = player.getLocation().clone()
                    .subtract(0, 1, 0).getBlock().getLocation();

            if (pos1 != null) {
                regionManager.setPlayerPos2(player, pos2);
                ChatUtils.send(player, "&aPosizione 2 impostata: " + formatLocation(pos2));

                particleHandler.showAreaParticles(player, pos1, pos2);
            } else {
                ChatUtils.send(player, "&cImposta prima Posizione 1!");
            }
        }
    }


    @Command("create")
    @Permission("region.admin")
    public void onCreateSubCommand(CommandSender sender, String name) {
        if (sender instanceof Player player) {
            Location pos1 = regionManager.getPlayerPos1(player);
            Location pos2 = regionManager.getPlayerPos2(player);

            if (pos1 == null || pos2 == null) {
                ChatUtils.send(player, "&cImposta prima entrambe le posizioni della regione.");
                return;
            }

            particleHandler.playBlackHoleCreate(player, pos1, pos2);
            regionManager.addRegion(name, pos1, pos2);
            ChatUtils.send(player, "&aRegione '" + name + "' creata!");
        }
    }

    @Command("delete")
    @Permission("region.admin")
    public void onDeleteSubCommand(CommandSender sender, String name) {
        if (sender instanceof Player player) {
            if (name.isEmpty()) {
                ChatUtils.send(player, "&cFornisci il nome della regione da eliminare: /region delete <name>");
                return;
            }

            if (!regionManager.regionExists(name)) {
                ChatUtils.send(player, "&cLa regione '" + name + "' non esiste.");
                return;
            }

            regionManager.removeRegion(name);
            ChatUtils.send(player, "&aLa regione '" + name + "' è stata eliminata!");
        }
    }

    @Command("list")
    @Permission("region.admin")
    public void onListRegionsSubCommand(CommandSender sender) {
        List<Region> regions = regionManager.getAllRegions();

        if (regions.isEmpty()) {
            ChatUtils.send(sender, "&cNon sono state create regioni.");
            return;
        }

        StringBuilder regionList = new StringBuilder("&aRegioni:\n");
        regions.forEach(region -> regionList.append("&e- ").append(region.getName()).append("\n"));

        ChatUtils.send(sender, regionList.toString());
    }

    @Command("gui")
    @Permission("region.admin")
    public void onGuiRegionsSubCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            PaginatedGui regionsGui = Gui.paginated()
                    .title(Component.text(ChatUtils.color("&cRegions")))
                    .rows(6)
                    .pageSize(20)
                    .disableAllInteractions()
                    .create();

            ItemStack redGlass = XMaterial.RED_STAINED_GLASS_PANE.parseItem();
            GuiItem redGlassItem = ItemBuilder.from(redGlass).name(Component.text(ChatUtils.color("&7"))).asGuiItem();

            RegionsGuiUtil.fillerGui(regionsGui, redGlassItem);
            RegionsGuiUtil.setupPageNavigation(regionsGui);

            for (Region region : regionManager.getAllRegions()) {
                ItemStack regionItem = createRegionItem(region); //TODO: la region funziona a cazzo di cane | fixare
                regionsGui.addItem(ItemBuilder.from(regionItem).asGuiItem(event -> {
                    if (event.isLeftClick()) {
                        PaginatedGui authorGui = Gui.paginated()
                                .title(Component.text(ChatUtils.color("&cRegion: " + region.getName() + " | &bSelect Author")))
                                .rows(6)
                                .pageSize(20)
                                .disableAllInteractions()
                                .create();

                        RegionsGuiUtil.fillerGui(authorGui, redGlassItem);
                        RegionsGuiUtil.setupPageNavigation(authorGui);
                        RegionsGuiUtil.setupBack(authorGui, regionsGui, 6, 1);

                        SettingRegistry registry = SettingRegistry.getInstance();

                        for (String author : registry.getSettingAuthors()) {
                            ItemStack authorItem = new ItemStack(Material.BOOK);
                            ItemMeta meta = authorItem.getItemMeta();
                            meta.setDisplayName(ChatUtils.color("&a" + author));
                            authorItem.setItemMeta(meta);

                            authorGui.addItem(ItemBuilder.from(authorItem).asGuiItem(authorEvent -> {
                                PaginatedGui settingsGui = Gui.paginated()
                                        .title(Component.text(ChatUtils.color("&cRegion: " + region.getName() + " | &bAuthor: " + author)))
                                        .rows(6)
                                        .pageSize(20)
                                        .disableAllInteractions()
                                        .create();

                                RegionsGuiUtil.fillerGui(authorGui, redGlassItem);
                                RegionsGuiUtil.setupPageNavigation(authorGui);

                                List<String> settings = SettingRegistry.getInstance().getSettingsByAuthor(author);

                                for (String setting : settings) {
                                    settingsGui.addItem(ItemBuilder.from(settingsItem(setting, region)).asGuiItem(toggleEvent -> {
                                        if (region.getSettings().containsKey(setting)) {
                                            registry.removeSettingFromRegion(setting, region);
                                            regionManager.toggleRegionSetting(region, setting);
                                        } else {
                                            try {
                                                registry.applySettingToRegion(setting, region);
                                                regionManager.toggleRegionSetting(region, setting);
                                            } catch (IllegalArgumentException e) {
                                                ChatUtils.error(e);
                                            }
                                        }

                                        settingsGui.updatePageItem(toggleEvent.getSlot(), settingsItem(setting, region));
                                    }));
                                }

                                settingsGui.open(player);
                            }));
                        }


                        authorGui.open(player);
                    } else if (event.isRightClick()) {
                        Gui miscGui = Gui.gui()
                                .title(Component.text(ChatUtils.color("&bVarie")))
                                .rows(3)
                                .disableAllInteractions()
                                .create();

                        ItemStack cyanPane = XMaterial.CYAN_STAINED_GLASS_PANE.parseItem();
                        GuiItem cyanGlassItem = ItemBuilder.from(cyanPane)
                                .name(Component.text(" ", NamedTextColor.GRAY))
                                .asGuiItem();

                        miscGui.getFiller().fillBorder(cyanGlassItem);

                        RegionsGuiUtil.setupBack(miscGui, regionsGui, 3, 1);

                        miscGui.setItem(2, 3, ItemBuilder.from(Material.COMPASS).name(Component.text(ChatUtils.color("&aTp to region"))).asGuiItem(tpEvent -> {
                            Location safeLocation = LocationUtil.getSafeCenterLocation(region);
                            if (safeLocation != null) {
                                player.teleport(safeLocation);
                                player.sendMessage(ChatUtils.color("&aTeleported to the center of the region!"));
                            } else {
                                player.sendMessage(ChatUtils.color("&cNo safe location found in the region!"));
                            }
                        }));

                        String skullTexture = "ewogICJ0aW1lc3RhbXAiIDogMTYzMzA5ODYwMDUwNCwKICAicHJvZmlsZUlkIiA6ICIwYTUzMDU0MTM4YWI0YjIyOTVhMGNlZmJiMGU4MmFkYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQX0hpc2lybyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yNmM1OTI2YzlhZjlmNDY2ZGQ0NWFkYzcxM2RjOTVkNzI3NDEzNjJjY2Y5NDVjNWU4NDA4MjcwNDc3M2M4ODhlIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
                        miscGui.setItem(2, 5, ItemBuilder.skull().texture(skullTexture).name(Component.text(ChatUtils.color("&cPlayers in region"))).asGuiItem(playerListEvent -> {
                            PaginatedGui playersInRegionGui = Gui.paginated()
                                    .title(Component.text(ChatUtils.color("&bPlayers in Region")))
                                    .rows(6)
                                    .pageSize(20)
                                    .disableAllInteractions()
                                    .create();

                            RegionsGuiUtil.fillerGui(playersInRegionGui, cyanGlassItem);
                            RegionsGuiUtil.setupPageNavigation(playersInRegionGui);
                            RegionsGuiUtil.setupBack(playersInRegionGui, miscGui, 6, 1);

                            updatePlayersInRegionGui(playersInRegionGui, region);

                            CompletableFuture.runAsync(() -> {
                                while (true) {
                                    try {
                                        TimeUnit.SECONDS.sleep(1);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        break;
                                    }

                                    if (!(player.getOpenInventory().getTitle().equals(playersInRegionGui.title().toString()))) {
                                        break;
                                    }

                                    Bukkit.getScheduler().runTask(RegionX.getInstance(), () -> updatePlayersInRegionGui(playersInRegionGui, region));
                                }
                            }, executorService);

                            playersInRegionGui.open(player);
                        }));

                        miscGui.open(player);
                    }

                }));
            }

            regionsGui.open(player);
        }
    }

    private void updatePlayersInRegionGui(PaginatedGui gui, Region region) {
        Set<Player> playersInRegion = UserRegion.getPlayersInRegion(region);

        gui.clearPageItems();

        playersInRegion.forEach(regionPlayer -> {
            GuiItem playerHead = ItemBuilder.skull()
                    .owner(Bukkit.getOfflinePlayer(regionPlayer.getUniqueId()))
                    .name(Component.text(ChatUtils.color("&a" + regionPlayer.getName())))
                    .asGuiItem();

            gui.addItem(playerHead);
        });

        gui.update();
    }

    private ItemStack settingsItem(String setting, Region region) {
        ItemStack settingItem = new ItemStack(Material.PAPER);
        ItemMeta settingMeta = settingItem.getItemMeta();
        settingMeta.setDisplayName(ChatUtils.color("&a" + setting));

        List<String> lore = new ArrayList<>();
        boolean isSettingActive = isSettingActive(region, setting);
        lore.add(ChatUtils.color("&7Status: " + (isSettingActive ? "&aAbilitato" : "&cDisabilitato")));

        settingMeta.setLore(lore);
        settingItem.setItemMeta(settingMeta);

        return settingItem;
    }

    private boolean isSettingActive(Region region, String settingName) {
        if (region != null) {
            RegionSetting setting = region.getSettings().get(settingName.toUpperCase());
            return setting != null && setting.isActive();
        }
        return false;
    }

    private ItemStack createRegionItem(Region region) {
        ItemStack itemStack = new ItemStack(Material.GRASS);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatUtils.color("&a" + region.getName()));

        List<String> formattedLore = new ArrayList<>();

        formattedLore.add(ChatUtils.color("&7Nome della Region: &f" + region.getName()));
        formattedLore.add(ChatUtils.color("&7Coord. Minime: &f" + region.getMinX() + ", " + region.getMinY() + ", " + region.getMinZ()));
        formattedLore.add(ChatUtils.color("&7Coord. Massime: &f" + region.getMaxX() + ", " + region.getMaxY() + ", " + region.getMaxZ()));

        itemMeta.setLore(formattedLore);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    private String formatLocation(Location location) {
        return String.format("X: %.2f, Y: %.2f, Z: %.2f", location.getX(), location.getY(), location.getZ());
    }
}
