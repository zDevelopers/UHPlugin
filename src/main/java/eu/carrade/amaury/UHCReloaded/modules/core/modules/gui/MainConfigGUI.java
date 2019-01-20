/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package eu.carrade.amaury.UHCReloaded.modules.core.modules.gui;

import eu.carrade.amaury.UHCReloaded.UHConfig;
import eu.carrade.amaury.UHCReloaded.core.ModuleWrapper;
import eu.carrade.amaury.UHCReloaded.modules.beginning.wait.Config;
import eu.carrade.amaury.UHCReloaded.modules.beginning.wait.WaitModule;
import eu.carrade.amaury.UHCReloaded.modules.core.game.GamePhase;
import eu.carrade.amaury.UHCReloaded.modules.core.modules.gui.modules.ModulesListGUI;
import eu.carrade.amaury.UHCReloaded.modules.core.modules.gui.start.StartGameGUI;
import eu.carrade.amaury.UHCReloaded.modules.core.spectators.SpectatorsModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.gui.ActionGui;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.gui.PromptGui;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.runners.RunTask;
import fr.zcraft.zteams.ZTeams;
import fr.zcraft.zteams.guis.TeamsSelectorGUI;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainConfigGUI extends ActionGui
{
    private BukkitTask headsCycleTask = null;
    private String[] headCycleNames = null;
    private int headCycleIndex = 0;

    @Override
    protected void onUpdate()
    {
        setTitle(I.tl(getPlayerLocale(), "{black}Game Configuration"));
        setHeight(UR.game().currentPhaseAfter(GamePhase.WAIT) ? 3 : 5);

        final Set<String> alivePlayers = (UR.game().currentPhaseBefore(GamePhase.IN_GAME)
                    ? Bukkit.getOnlinePlayers().stream().filter(player -> !UR.module(SpectatorsModule.class).isSpectator(player))
                    : UR.game().getAlivePlayers().stream())
                .map(OfflinePlayer::getName)
                .collect(Collectors.toSet());

        final Set<String> spectators = UR.module(SpectatorsModule.class).getSpectators().stream()
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .collect(Collectors.toSet());

        headCycleNames = Stream.concat(alivePlayers.stream(), spectators.stream()).toArray(String[]::new);

        action("players", 10, new ItemStackBuilder(Material.SKULL_ITEM)
                .data((short) SkullType.PLAYER.ordinal())
                .amount(Math.max(alivePlayers.size(), 1))
                .title(ChatColor.GREEN, ChatColor.BOLD + I.tl(getPlayerLocale(), "Players"))
                .loreLine(
                        ChatColor.DARK_GRAY,
                        I.tln(getPlayerLocale(), "{0} player", "{0} players", alivePlayers.size()),
                        " - ",
                        I.tln(getPlayerLocale(), "{0} spectator", "{0} spectators", UR.module(SpectatorsModule.class).getSpectators().size())
                )
                .loreSeparator()
                .longLore(ChatColor.BLUE, I.tl(getPlayerLocale(), "Players"))
                .longLore(ChatColor.GRAY, alivePlayers.isEmpty() ? ChatColor.DARK_GRAY + I.tl(getPlayerLocale(), "(none)") : String.join(", ", alivePlayers), 38)
                .loreSeparator()
                .longLore(ChatColor.BLUE, I.tl(getPlayerLocale(), "Spectators"))
                .longLore(ChatColor.GRAY, spectators.isEmpty() ? ChatColor.DARK_GRAY + I.tl(getPlayerLocale(), "(none)") : String.join(", ", spectators), 38)
                .loreSeparator()
                .longLore(ChatColor.DARK_GRAY, I.tl(getPlayerLocale(), "Actions on players coming soon: in the mean time, use commands."), 38)
        );

        action("teams", 12, new ItemStackBuilder(UR.module(WaitModule.class) != null ? Config.TEAM_SELECTOR.ITEM.get() : Material.NETHER_STAR)
                .title(ChatColor.AQUA, ChatColor.BOLD + I.tl(getPlayerLocale(), "Teams"))
                .amount(Math.max(ZTeams.get().countTeams(), 1))
                .loreLine(ChatColor.DARK_GRAY, I.tln(getPlayerLocale(), "{0} team", "{0} teams", ZTeams.get().countTeams()))
                .loreSeparator()
                .longLore(ChatColor.GRAY, I.tl(getPlayerLocale(), "The game can either be solo or in teams. In the second case, click here to create or update teams."), 38)
                .loreSeparator()
                .longLore(ChatColor.DARK_GRAY + " » " + I.tl(getPlayerLocale(), "{white}Click {gray}to manage the teams"))
        );

        action("title", 14, new ItemStackBuilder(Material.BOOK_AND_QUILL)
                .title(ChatColor.DARK_PURPLE, ChatColor.BOLD + I.tl(getPlayerLocale(), "Game Title"))
                .loreSeparator()
                .longLore(ChatColor.GRAY, I.tl(getPlayerLocale(), "Click to update the game title. It is displayed on the sidebar and may be used by other modules (like the reports one)."), 38)
                .loreSeparator()
                .longLore(ChatColor.BLUE, I.tl(getPlayerLocale(), "Current Title"))
                .longLore(UHConfig.TITLE.get())
                .loreSeparator()
                .longLore(ChatColor.DARK_GRAY + " » " + I.tl(getPlayerLocale(), "{white}Click {gray}to change the title"))
        );

        final int modules = UR.get().getModulesManager().getModules().size();
        final int modulesEnabled = (int) UR.get().getModulesManager().getModules().stream().filter(ModuleWrapper::isEnabled).count();
        final int modulesLoaded = (int) UR.get().getModulesManager().getModules().stream().filter(ModuleWrapper::isLoaded).count();

        action("modules", 16, new ItemStackBuilder(Material.COMMAND)
                .title(ChatColor.LIGHT_PURPLE, ChatColor.BOLD + I.tl(getPlayerLocale(), "Modules"))
                .loreLine(
                        ChatColor.DARK_GRAY,
                        I.tln(getPlayerLocale(), "{0} module", "{0} modules", modules),
                        " - ",
                        I.tln(getPlayerLocale(), "{0} enabled", "{0} enabled", modulesEnabled),
                        " - ",
                        I.tln(getPlayerLocale(), "{0} loaded", "{0} loaded", modulesLoaded)
                )
                .loreSeparator()
                .longLore(ChatColor.GRAY, I.tl(getPlayerLocale(), "This plugin is divided into modules, each one bringing one small or large piece of the game."), 52)
                .loreSeparator()
                .lore(ChatColor.DARK_GRAY + " » " + I.tl(getPlayerLocale(), "{white}Click {gray}to manage modules and their configuration"))
        );

        if (!UR.game().currentPhaseAfter(GamePhase.WAIT))
        {
            action("start", 31, new ItemStackBuilder(Material.INK_SACK)  // FIXME 1.13
                    .data(DyeColor.LIME.getDyeData())
                    .title(ChatColor.GREEN, ChatColor.BOLD + I.tl(getPlayerLocale(), "Start the game"))
                    .longLore(ChatColor.GRAY, I.tl(getPlayerLocale(), "If you're ready, click here to start the game!"))
            );
        }

        if (headsCycleTask != null) headsCycleTask.cancel();

        headsCycleTask = RunTask.timer(() -> {
            final ItemStack skull = getInventory().getItem(10);
            if (skull != null)
            {
                new ItemStackBuilder(getInventory().getItem(10)).head(headCycleNames[headCycleIndex]).item();

                headCycleIndex = (headCycleIndex + 1) % headCycleNames.length;
                if (headCycleIndex < 0) headCycleIndex += headCycleNames.length;
            }
        }, 0L, 40L);
    }

    @Override
    protected void onClose()
    {
        if (headsCycleTask != null)
        {
            headsCycleTask.cancel();
            headsCycleTask = null;
            headCycleIndex = 0;
        }

        super.onClose();
    }

    @GuiAction
    protected void players() {
        update();
    }


    @GuiAction
    protected void teams()
    {
        Gui.open(getPlayer(), new TeamsSelectorGUI(), this);
    }

    @GuiAction
    protected void title()
    {
        PromptGui.prompt(getPlayer(), newTitle -> UHConfig.TITLE.set(ChatColor.translateAlternateColorCodes('&', newTitle), false), UHConfig.TITLE.get().replace(ChatColor.COLOR_CHAR, '&'), this);
    }

    @GuiAction
    protected void modules()
    {
        Gui.open(getPlayer(), new ModulesListGUI(), this);
    }

    @GuiAction
    protected void start()
    {
        Gui.open(getPlayer(), new StartGameGUI());
    }
}
