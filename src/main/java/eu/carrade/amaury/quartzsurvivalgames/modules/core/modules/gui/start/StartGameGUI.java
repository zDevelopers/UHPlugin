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

package eu.carrade.amaury.quartzsurvivalgames.modules.core.modules.gui.start;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.teleporter.TeleportationMode;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.modules.gui.MainConfigGUI;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import eu.carrade.amaury.quartzsurvivalgames.utils.QSGUtils;
import fr.zcraft.quartzlib.components.gui.ActionGui;
import fr.zcraft.quartzlib.components.gui.Gui;
import fr.zcraft.quartzlib.components.gui.GuiAction;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.items.ItemStackBuilder;
import fr.zcraft.quartzlib.tools.mojang.MojangHead;
import fr.zcraft.quartzteams.QuartzTeams;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class StartGameGUI extends ActionGui {
    private TeleportationMode mode = TeleportationMode.NORMAL;

    @Override
    protected void onUpdate() {
        setHeight(5);
        setTitle(I.tl(getPlayerLocale(), "{black}Ready to start the game?"));

        final String prefixActive = ChatColor.YELLOW + "» ";
        final String prefixInactive = ChatColor.DARK_GRAY + "» " + ChatColor.GRAY;

        action("fast", 11, new ItemStackBuilder(Material.MAGMA_CREAM)
                .title(ChatColor.DARK_GREEN, ChatColor.BOLD + I.tl(getPlayerLocale(), "Fast Start"))
                .loreSeparator()
                .longLore(ChatColor.GRAY, I.tl(getPlayerLocale(),
                        "Click here to start the game immediately. Players will be teleported at once (thus loading a lot of chunks in a few seconds) and the countdown will begin immediately after."),
                        52)
        );

        action("slow", 15, new ItemStackBuilder(Material.SLIME_BALL)
                .title(ChatColor.GREEN, ChatColor.BOLD + I.tl(getPlayerLocale(), "Slow Start"))
                .loreSeparator()
                .longLore(ChatColor.GRAY, I.tl(getPlayerLocale(),
                        "If your server is a little bit small, use this option to teleport players slowly, loading the chunks one player at a time. You'll have to confirm the game start using a link in the chat."),
                        52)
        );

        if (QuartzTeams.get().countTeams() > 0) {
            action("teleportation_mode", 13, new ItemStackBuilder()
                    .material(mode == TeleportationMode.NORMAL ? Material.SUGAR : Material.GLOWSTONE_DUST)
                    .title(ChatColor.YELLOW, ChatColor.BOLD + I.tl(getPlayerLocale(), "Teleportation Mode"))
                    .loreSeparator()
                    .loreLine(mode == TeleportationMode.NORMAL ? prefixActive : prefixInactive,
                            I.tl(getPlayerLocale(), "Teams together"))
                    .longLore(mode == TeleportationMode.NORMAL ? ChatColor.GRAY : ChatColor.DARK_GRAY,
                            I.tl(getPlayerLocale(),
                                    "Teams are teleported to a shared spawn point. Teammates start together."), 38)
                    .loreSeparator()
                    .loreLine(mode == TeleportationMode.IGNORE_TEAMS ? prefixActive : prefixInactive,
                            I.tl(getPlayerLocale(), "Ignoring teams"))
                    .longLore(mode == TeleportationMode.NORMAL ? ChatColor.GRAY : ChatColor.DARK_GRAY,
                            I.tl(getPlayerLocale(),
                                    "Players will be alone at the beginning, even if they are in a team."), 38)
            );
        }

        action("back", 31, MojangHead.ARROW_LEFT.asItemBuilder()
                .title(ChatColor.RED, ChatColor.BOLD + I.tl(getPlayerLocale(), "Go Back"))
                .loreSeparator()
                .longLore(ChatColor.GRAY, I.tl(getPlayerLocale(),
                        "Changed your mind? No problem, click here to go back without starting the game."), 38)
        );
    }

    @GuiAction
    protected void teleportation_mode() {
        mode = QSGUtils.getNextElement(mode, 1);
        update();
    }

    @GuiAction
    protected void fast() {
        start(false);
        close();
    }

    @GuiAction
    protected void slow() {
        start(true);
        close();
    }

    private void start(final boolean slow) {
        QSG.game().setTeleportationMode(mode);
        QSG.game().setSlowMode(slow);

        QSG.game().setPhase(GamePhase.STARTING);
    }

    @GuiAction
    protected void back() {
        Gui.open(getPlayer(), new MainConfigGUI());
    }
}
