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
package eu.carrade.amaury.quartzsurvivalgames.modules.external.motd;

import eu.carrade.amaury.quartzsurvivalgames.QSGConfig;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzteams.QuartzTeam;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;


@ModuleInfo (
        name = "MOTD",
        description = "Updates the MOTD according to the current game state.",
        category = ModuleCategory.EXTERNAL,
        icon = Material.DARK_OAK_SIGN,
        settings = Config.class
)
public class MotdModule extends QSGModule
{
    private final GameModule game = QSG.module(GameModule.class);

    private String getMOTD()
    {
        final String matchName;

        if (Config.DISPLAY_MATCH_NAME.get())
        {
            matchName = ChatColor.translateAlternateColorCodes('&', Config.MATCH_NAME_PREFIX.get())
                + QSGConfig.TITLE.get()
                + ChatColor.RESET + "\n";
        }
        else
        {
            matchName = "";
        }

        switch (game.getPhase())
        {
            case WAIT:
                return matchName + I.t("Waiting for players...");

            case STARTING:
                return matchName + I.t("Starting in progress...");

            case IN_GAME:
                if (game.isTeamsGame())
                {
                    /// Teams game running MOTD. {0} = players alive count. {1} = teams alive count. Plural based on players count.
                    return matchName + I.tn("Game running! {0} player alive in {1} team.", "Game running! {0} players alive in {1} teams.", game.countAlivePlayers(), game.countAlivePlayers(), game.countAliveTeams());
                }
                else
                {
                    /// Solo game running MOTD. {0} = players alive count.
                    return matchName + I.tn("Game running! {0} player alive.", "Game running! {0} players alive.", game.countAlivePlayers());
                }

            case END:
            default:
                final QuartzTeam winner = game.getWinner();

                if (game.isTeamsGame())
                {
                    /// Game finished MOTD with team winner ({0} = team display name).
                    return matchName + I.t("Game finished; the team {0} wins this match!", winner != null ? winner.getDisplayName() : "??");
                }
                else
                {
                    /// Game finished MOTD with solo winner ({0} = winner raw name).
                    return matchName + I.t("Game finished; congratulation to {0} for his victory!", winner != null ? winner.getName() : "??");
                }
        }
    }

    @EventHandler
    public void onServerListPing(final ServerListPingEvent ev)
    {
        ev.setMotd(getMOTD());
    }
}
