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

package eu.carrade.amaury.quartzsurvivalgames.modules.end.deathAnnouncement;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.players.AlivePlayerDeathEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.events.players.TeamDeathEvent;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGSound;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.components.rawtext.RawTextPart;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import fr.zcraft.quartzlib.tools.text.RawMessage;
import fr.zcraft.quartzteams.QuartzTeam;
import fr.zcraft.quartzteams.QuartzTeams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

@ModuleInfo(
        name = "Death Announcements",
        description = "Adds announcements for players & teams deaths.",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.END,
        icon = Material.BLAZE_ROD,
        settings = Config.class
)
public class DeathAnnouncementModule extends QSGModule {
    private QSGSound deathSound;

    @Override
    protected void onEnable() {
        deathSound = Config.SOUND.get();
    }

    @EventHandler
    public void onPlayerDeath(final AlivePlayerDeathEvent ev) {
        final PlayerDeathEvent pdev = ev.getPlayerDeathEvent();

        // Highlights the death message in the console
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "-- Death of " + ev.getPlayer().getName() +
                (pdev != null ? " (" + pdev.getDeathMessage() + ")" : "") + " --");

        // If this is a real death
        if (pdev != null) {
            // We send a lightning strike.

            if (Config.LIGHTNING_STRIKE.get()) {
                pdev.getEntity().getLocation().getWorld().strikeLightningEffect(pdev.getEntity().getLocation());
            }
        }

        // If it is a death of an offline player
        else {
            Bukkit.broadcastMessage(
                    colorizePlayerInString(I.t("{0} died, following a game master's order.", ev.getPlayer().getName()),
                            ev.getPlayer()));
        }


        // Play sound
        if (Config.PLAY_SOUND.get()) {
            deathSound.broadcast();
        }
    }

    @EventHandler
    public void onTeamDeath(final TeamDeathEvent ev) {
        if (Config.NOTIFY_IF_TEAM_HAS_FALLEN.get()) {
            // Used to display this message after the death message.
            RunTask.later(() ->
            {
                final QuartzTeam team = ev.getTeam();
                final String format =
                        ChatColor.translateAlternateColorCodes('&', Config.TEAM_DEATH_MESSAGES_FORMAT.get());

                final RawTextPart<?> teamTooltip = new RawText()
                        .then(team.getName()).style(team.getColorOrWhite().toChatColor(), ChatColor.BOLD);

                for (final OfflinePlayer player : team.getPlayers()) {
                    teamTooltip.then("\n")
                            .then("- ").color(ChatColor.GRAY)
                            .then(player.getName()).color(ChatColor.WHITE);
                }

                RawMessage.broadcast(
                        new RawText(I.t("{0}The team {1} has fallen!", format, team.getDisplayName() + format))
                                .hover(teamTooltip)
                );
            }, 1L);
        }
    }

    /**
     * Colorizes each instance of the given player name in the string with its team color,
     * if the player is in a team.
     *
     * @param str    The string to colorize.
     * @param player The player to look for in the string.
     * @return The colorized string.
     */
    private String colorizePlayerInString(final String str, final OfflinePlayer player) {
        final QuartzTeam team = QuartzTeams.get().getTeamForPlayer(player);
        if (team == null) {
            return str;
        }

        // We split the name to recompose the string with the colored name in each hole
        final String[] strParts = str.split(player.getName());
        final ChatColor color = team.getColorOrWhite().toChatColor();
        final StringBuilder colorizedStr = new StringBuilder();

        for (int i = 0; i < strParts.length; i++) {
            colorizedStr.append(strParts[i]);

            if (i != strParts.length - 1) // If not the last one
            {
                final String previousStyles = ChatColor.getLastColors(colorizedStr.toString());
                colorizedStr.append(color).append(player.getName()).append(previousStyles);
            }
        }

        return colorizedStr.toString();
    }
}
