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
package eu.carrade.amaury.UHCReloaded.protips;

import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.entity.Player;

import java.util.UUID;


public enum ProTips
{
    LOCK_CHAT(new ProTip("teamchat.lock",            I.tc("protip", "{gray}You can lock and unlock the team chat with {cc}/togglechat{gray}."))),
    USE_G_COMMAND(new ProTip("teamchat.useGCommand", I.tc("protip", "{gray}You can send a global message using {cc}/g <message>{gray}."))),
    USE_T_COMMAND(new ProTip("teamchat.useTCommand", I.tc("protip", "{gray}You can send a team-chat message with {cc}/t <message>{gray}."))),

    CRAFT_GOLDEN_HEAD(new ProTip("crafts.goldenHead",           I.tc("protip", "{gray}You can craft golden apples with heads (same recipe with a head instead of an apple)."))),
    CRAFT_COMPASS_EASY(new ProTip("crafts.compassEasy",         I.tc("protip", "{gray}The compass is crafted with, in the corners, a bone, a rotten flesh, a spider eye and a gunpowder."))),
    CRAFT_COMPASS_MEDIUM(new ProTip("crafts.compassMedium",     I.tc("protip", "{gray}The compass is crafted with, in the corners, a bone, a rotten flesh, a spider eye and a gunpowder; in the center, an ender pearl."))),
    CRAFT_COMPASS_HARD(new ProTip("crafts.compassHard",         I.tc("protip", "{gray}The compass is crafted with, in the corners, a bone, a rotten flesh, a spider eye and a gunpowder; in the center, an Eye of Ender."))),
    CRAFT_GLISTERING_MELON(new ProTip("crafts.glisteringMelon", I.tc("protip", "{gray}The glistering melon is crafted with a melon and a gold block."))),

    CRAFT_NO_ENCHANTED_GOLDEN_APPLE(new ProTip("crafts.noEnchGoldenApple", I.tc("protip", "{gray}The enchanted golden apple is disabled for this game."))),

    STARTUP_INVINCIBILITY(new ProTip("start.invincibility", I.tc("protip", "{gray}Fallen on a tree? Jump, you have a few seconds left to remain invincible.")));


    private final ProTip proTip;

    ProTips(ProTip proTip)
    {
        this.proTip = proTip;
    }

    public ProTip get()
    {
        return proTip;
    }


    /**
     * Sends this ProTip, if it wasn't sent before to this player.
     *
     * @param player The receiver of this ProTip.
     */
    public void sendTo(Player player)
    {
        proTip.sendTo(player);
    }

    /**
     * Sends this ProTip, if it wasn't sent before to this player and this player is online.
     *
     * @param id The receiver of this ProTip.
     */
    public void sendTo(UUID id)
    {
        proTip.sendTo(id);
    }
}
