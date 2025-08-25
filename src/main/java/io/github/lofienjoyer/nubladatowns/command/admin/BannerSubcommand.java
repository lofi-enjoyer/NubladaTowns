package io.github.lofienjoyer.nubladatowns.command.admin;

import io.github.lofienjoyer.nubladatowns.NubladaTowns;
import io.github.lofienjoyer.nubladatowns.command.SubCommand;
import io.github.lofienjoyer.nubladatowns.localization.LocalizationManager;
import io.github.lofienjoyer.nubladatowns.town.Town;
import io.github.lofienjoyer.nubladatowns.utils.BannerUtils;
import io.github.lofienjoyer.nubladatowns.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.block.Banner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class BannerSubcommand implements SubCommand {

    private final LocalizationManager localizationManager;

    public BannerSubcommand(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    @Override
    public void accept(CommandSender sender, String[] args) {
        if (!NubladaTowns.getInstance().isEconomyEnabled()) {
            sender.sendMessage(localizationManager.getMessage("economy-disabled"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(localizationManager.getMessage("not-enough-arguments"));
            return;
        }

        switch (args[0]) {
            case "get" -> handleGet(getTownNameFromArgs(args, 1), sender);
            case "set" -> handleSet(getTownNameFromArgs(args, 1), sender);
            default -> sender.sendMessage(localizationManager.getMessage("invalid-command"));
        }
    }

    private void handleSet(String townName, CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return;
        }

        var town = NubladaTowns.getInstance().getTownManager().getTownByName(townName);
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("non-existent-town"));
            return;
        }

        var banner = player.getInventory().getItemInMainHand();
        if (!(banner.getItemMeta() instanceof BannerMeta bannerMeta)) {
            sender.sendMessage(localizationManager.getMessage("invalid-item"));
            return;
        }

        town.setBannerPatterns(bannerMeta.getPatterns());
        var bannerColor = ((Banner) banner.getType().createBlockData().createBlockState()).getBaseColor().getColor().asARGB();
        town.setRgbColor(bannerColor);

        player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("banner-set"), town));
    }

    private void handleGet(String townName, CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(localizationManager.getMessage("invalid-command"));
            return;
        }

        var town = NubladaTowns.getInstance().getTownManager().getTownByName(townName);
        if (town == null) {
            sender.sendMessage(localizationManager.getMessage("non-existent-town"));
            return;
        }
        
        var banner = new ItemStack(BannerUtils.getBannerMaterialByColor(town.getRgbColor()));
        var bannerMeta = (BannerMeta) banner.getItemMeta();
        bannerMeta.displayName(Component.text(town.getName(), Style.style(TextDecoration.ITALIC)));
        bannerMeta.setPatterns(town.getBannerPatterns());
        banner.setItemMeta(bannerMeta);

        player.getInventory().addItem(banner);
        player.sendMessage(ComponentUtils.replaceTownName(localizationManager.getMessage("banner-received"), town));
    }

    private String getTownNameFromArgs(String[] args, int start) {
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("get", "set")
                    .filter(s1 -> s1.startsWith(args[0]))
                    .toList();
        }

        var townName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        return NubladaTowns.getInstance().getTownManager().getTowns().stream()
                .map(Town::getName)
                .filter(s1 -> s1.startsWith(townName))
                .toList();
    }

}
