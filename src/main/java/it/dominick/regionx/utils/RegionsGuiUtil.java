package it.dominick.regionx.utils;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RegionsGuiUtil {

    public static void fillerGui(PaginatedGui regionsGui, GuiItem item) {
        regionsGui.getFiller().fillBorder(item);

        int[][] positions = {
                {2, 2},
                {2, 8},
                {5, 2},
                {5, 8}
        };

        for (int[] pos : positions) {
            regionsGui.setItem(pos[0], pos[1], item);
        }
    }

    public static void setupPageNavigation(PaginatedGui gui) {
        setupNavigationArrows(gui, 2, 2,
                gui::previous, "&aPagina Precedente");
        setupNavigationArrows(gui, 8, 8,
                gui::next, "&aPagina Successiva");
    }

    public static void setupBack(PaginatedGui gui, PaginatedGui openGui, int row, int col) {
        gui.setItem(row, col, createBackItem(openGui));
    }

    public static void setupBack(PaginatedGui gui, Gui openGui, int row, int col) {
        gui.setItem(row, col, createBackItem(openGui));
    }

    public static void setupBack(Gui gui, PaginatedGui openGui, int row, int col) {
        gui.setItem(row, col, createBackItem(openGui));
    }


    private static void setupNavigationArrows(PaginatedGui gui, int col1, int col2,
                                              Runnable action, String arrowName) {
        if (gui.getPrevPageNum() != gui.getCurrentPageNum() ||
                gui.getNextPageNum() != gui.getCurrentPageNum()) {
            gui.setItem(3, col1, createNavigationItem(Material.ARROW, arrowName, action, gui));
            gui.setItem(4, col2, createNavigationItem(Material.ARROW, arrowName, action, gui));
        } else {
            gui.setItem(3, col1, createNavigationItem(Material.BARRIER, "&cUltima Pagina", null, gui));
            gui.setItem(4, col2, createNavigationItem(Material.BARRIER, "&cUltima Pagina", null, gui));
        }
    }

    private static GuiItem createBackItem(PaginatedGui gui) {
        return ItemBuilder.from(Material.FEATHER).name(Component.text(ChatUtils.color("&6Back"))).asGuiItem(event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            gui.open(player);
        });
    }

    private static GuiItem createBackItem(Gui gui) {
        return ItemBuilder.from(Material.FEATHER).name(Component.text(ChatUtils.color("&6Back"))).asGuiItem(event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();
            gui.open(player);
        });
    }


    private static GuiItem createNavigationItem(Material material, String name, Runnable action, PaginatedGui regionsGui) {
        if (action != null) {
            return ItemBuilder.from(material).name(Component.text(ChatUtils.color(name))).asGuiItem(event -> {
                action.run();
                regionsGui.update();
            });
        } else {
            return ItemBuilder.from(material).name(Component.text(ChatUtils.color(name))).asGuiItem();
        }
    }


}
