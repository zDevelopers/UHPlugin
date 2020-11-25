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
package eu.carrade.amaury.quartzsurvivalgames.modules.end;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import fr.zcraft.quartzlib.tools.text.Titles;
import fr.zcraft.zteams.ZTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

@ModuleInfo (
        name = "End Announcement",
        description = "Announces the winner's name to the whole server when the game ends.",
        when = ModuleLoadTime.ON_GAME_END,
        category = ModuleCategory.END,
        icon = Material.ITEM_FRAME
)
public class EndAnnouncementModule extends QSGModule
{
    @Override
    protected void onEnable()
    {
        RunTask.later(() ->
        {
            if (QSG.game().getPhase() != GamePhase.END) return;

            final ZTeam winnerTeam = QSG.game().getWinner();

            if (winnerTeam == null) return;  // No winner

            Bukkit.broadcastMessage("");

            if (QSG.game().isTeamsGame())
            {
                final StringBuilder winners = new StringBuilder();
                int j = 0;

                for (final OfflinePlayer winner : winnerTeam.getPlayers())
                {
                    if (j != 0)
                    {
                        if (j == winnerTeam.size() - 1)
                        {
                            /// The "and" in the winners players list (like "player1, player2 and player3").
                            winners.append(" ").append(I.tc("winners_list", "and")).append(" ");
                        }
                        else
                        {
                            winners.append(", ");
                        }
                    }

                    winners.append(winner.getName());
                    j++;
                }

                Bukkit.broadcastMessage(I.t("{darkgreen}{obfuscated}--{green} Congratulations to {0} (team {1}{green}) for their victory! {darkgreen}{obfuscated}--", winners.toString(), winnerTeam.getDisplayName()));
            }
            else
            {
                Bukkit.broadcastMessage(I.t("{darkgreen}{obfuscated}--{green} Congratulations to {0} for his victory! {darkgreen}{obfuscated}--", winnerTeam.getName()));
            }

            Bukkit.broadcastMessage("");


            final String title;
            final String subtitle;

            if (QSG.game().isTeamsGame())
            {
                /// The main title of the /title displayed when a team wins the game. {0} becomes the team display name (with colors).
                title = I.t("{darkgreen}{0}", winnerTeam.getDisplayName());
                /// The subtitle of the /title displayed when a team wins the game. {0} becomes the team display name (with colors).
                subtitle = I.t("{green}This team wins the game!", winnerTeam.getDisplayName());
            }
            else
            {
                /// The main title of the /title displayed when a player wins the game (in solo). {0} becomes the player display name (with colors).
                title = I.t("{darkgreen}{0}", winnerTeam.getDisplayName());
                /// The subtitle of the /title displayed when a player wins the game (in solo). {0} becomes the player display name (with colors).
                subtitle = I.t("{green}wins the game!", winnerTeam.getDisplayName());
            }

            Titles.broadcastTitle(5, 142, 21, title, subtitle);
        }, 6 * 20L);
    }
}
