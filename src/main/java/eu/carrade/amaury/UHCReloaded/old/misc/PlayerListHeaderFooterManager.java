/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.UHCReloaded.old.misc;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.UHConfig;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.text.ListHeaderFooter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class PlayerListHeaderFooterManager
{
    private final String WAITING_HEADER_PATTERN;
    private final String WAITING_FOOTER_PATTERN;
    private final String IN_GAME_HEADER_PATTERN;
    private final String IN_GAME_FOOTER_PATTERN;

    private String currentHeader = "";
    private String currentFooter = "";


    public PlayerListHeaderFooterManager()
    {
        WAITING_HEADER_PATTERN = UHConfig.PLAYERS_LIST.WAITING_TIME.HEADER.get();
        WAITING_FOOTER_PATTERN = UHConfig.PLAYERS_LIST.WAITING_TIME.FOOTER.get();
        IN_GAME_HEADER_PATTERN = UHConfig.PLAYERS_LIST.IN_GAME_TIME.HEADER.get();
        IN_GAME_FOOTER_PATTERN = UHConfig.PLAYERS_LIST.IN_GAME_TIME.FOOTER.get();

        updateHeadersFooters();
    }


    public void updateHeadersFooters()
    {
        computeHeadersFooter();
        send();
    }

    public void sendTo(Player player)
    {
        if (!currentHeader.isEmpty() || !currentFooter.isEmpty())
            ListHeaderFooter.sendListHeaderFooter(player, currentHeader, currentFooter);
    }


    private void computeHeadersFooter()
    {
        if (UHCReloaded.get().getGameManager().isGameStarted())
        {
            currentHeader = computeText(IN_GAME_HEADER_PATTERN);
            currentFooter = computeText(IN_GAME_FOOTER_PATTERN);
        }
        else
        {
            currentHeader = computeText(WAITING_HEADER_PATTERN);
            currentFooter = computeText(WAITING_FOOTER_PATTERN);
        }
    }

    private String computeText(String pattern)
    {
        return pattern.isEmpty() ? "" : ChatColor.translateAlternateColorCodes('&', replaceTags(pattern));
    }

    /**
     * Tags:
     * - {title}: contains the scoreboard title (key scoreboard.title).
     * - {episodeText}: contains the localized “Episode x” text.
     * - {playersText}: contains the localized “x players left” text.
     * - {teamsText}: contains the localized “x teams left” text.
     * - {episodeNumber}: contains the raw episode number (e.g. “2”).
     * - {playersCount}: contains the raw alive players count (e.g. “18”).
     * - {teamsCount}: contains the raw alive teams count (e.g. “6”).
     *
     * @param raw The raw text.
     * @return The text, with tags replaced.
     */
    private String replaceTags(String raw)
    {
        return raw
                .replace("{title}", UHConfig.SCOREBOARD.TITLE.get())

                /// Episode in the player list ({episodeText} replacement). {0} = current episode number.
                .replace("{episodeText}", I.t("Episode {0}", String.valueOf(UHCReloaded.get().getGameManager().getEpisode())))
                /// Players in the player list ({playersText} replacement). {0} = current alive players count.
                .replace("{playersText}", I.tn("{0} player", "{0} players", UHCReloaded.get().getGameManager().getAlivePlayersCount(), UHCReloaded.get().getGameManager().getAlivePlayersCount()))
                /// Teams in the player list ({teamsText} replacement). {0} = current alive teams count.
                .replace("{teamsText}", I.tn("{0} team", "{0} teams", UHCReloaded.get().getGameManager().getAliveTeamsCount(), UHCReloaded.get().getGameManager().getAliveTeamsCount()))

                .replace("{episodeNumber}", String.valueOf(UHCReloaded.get().getGameManager().getEpisode()))
                .replace("{playersCount}", String.valueOf(UHCReloaded.get().getGameManager().getAlivePlayersCount()))
                .replace("{teamsCount}", String.valueOf(UHCReloaded.get().getGameManager().getAliveTeamsCount()))
                ;
    }

    private void send()
    {
        if (!currentHeader.isEmpty() || !currentFooter.isEmpty())
            ListHeaderFooter.sendListHeaderFooter(currentHeader, currentFooter);
    }
}
