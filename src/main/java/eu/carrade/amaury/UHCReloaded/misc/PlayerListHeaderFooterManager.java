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
package eu.carrade.amaury.UHCReloaded.misc;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import fr.zcraft.zlib.tools.text.ListHeaderFooter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
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
        WAITING_HEADER_PATTERN = UHCReloaded.get().getConfig().getString("playersList.waitingTime.header", "");
        WAITING_FOOTER_PATTERN = UHCReloaded.get().getConfig().getString("playersList.waitingTime.footer", "");
        IN_GAME_HEADER_PATTERN = UHCReloaded.get().getConfig().getString("playersList.inGameTime.header",  "");
        IN_GAME_FOOTER_PATTERN = UHCReloaded.get().getConfig().getString("playersList.inGameTime.footer",  "");

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
        Configuration config = UHCReloaded.get().getConfig();
        I18n i = UHCReloaded.i();

        return raw
                .replace("{title}", config.getString("scoreboard.title", ""))
                .replace("{episodeText}", i.t("playersList.episode", String.valueOf(UHCReloaded.get().getGameManager().getEpisode())))
                .replace("{playersText}", i.t("playersList.players", String.valueOf(UHCReloaded.get().getGameManager().getAlivePlayersCount())))
                .replace("{teamsText}", i.t("playersList.teams", String.valueOf(UHCReloaded.get().getGameManager().getAliveTeamsCount())))
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
