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
import eu.carrade.amaury.UHCReloaded.utils.UHSound;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Sends a ProTip to a player.
 *
 * All ProTips are sent only once.
 *
 * The name of a protip (the value of the static attribute representing it) is:
 *  - an identifier;
 *  - the name of the key in the translation files (protips.{name});
 *  - the name of the key to disable it in the config file (protips.{name} too).
 *
 * @author Amaury Carrade
 *
 */
public class ProTipsSender
{
    private UHCReloaded p = null;

    private Map<String, ArrayList<UUID>> protipsGiven = new HashMap<>();

    private UHSound proTipsSound = null;

    public static final String PROTIP_LOCK_CHAT = "teamchat.lock";
    public static final String PROTIP_USE_G_COMMAND = "teamchat.useGCommand";
    public static final String PROTIP_USE_T_COMMAND = "teamchat.useTCommand";

    public static final String PROTIP_CRAFT_GOLDEN_HEAD = "crafts.goldenHead";
    public static final String PROTIP_CRAFT_COMPASS_EASY = "crafts.compassEasy";
    public static final String PROTIP_CRAFT_COMPASS_MEDIUM = "crafts.compassMedium";
    public static final String PROTIP_CRAFT_COMPASS_HARD = "crafts.compassHard";
    public static final String PROTIP_CRAFT_GLISTERING_MELON = "crafts.glisteringMelon";
    public static final String PROTIP_CRAFT_NO_ENCHANTED_GOLDEN_APPLE = "crafts.noEnchGoldenApple";

    public static final String PROTIP_STARTUP_INVINCIBILITY = "start.invincibility";

    public ProTipsSender(UHCReloaded p)
    {
        this.p = p;

        // Initialization of the "protips" map
        protipsGiven.put(PROTIP_LOCK_CHAT, new ArrayList<UUID>());
        protipsGiven.put(PROTIP_USE_G_COMMAND, new ArrayList<UUID>());
        protipsGiven.put(PROTIP_USE_T_COMMAND, new ArrayList<UUID>());

        protipsGiven.put(PROTIP_CRAFT_GOLDEN_HEAD, new ArrayList<UUID>());
        protipsGiven.put(PROTIP_CRAFT_COMPASS_EASY, new ArrayList<UUID>());
        protipsGiven.put(PROTIP_CRAFT_COMPASS_MEDIUM, new ArrayList<UUID>());
        protipsGiven.put(PROTIP_CRAFT_COMPASS_HARD, new ArrayList<UUID>());
        protipsGiven.put(PROTIP_CRAFT_GLISTERING_MELON, new ArrayList<UUID>());
        protipsGiven.put(PROTIP_CRAFT_NO_ENCHANTED_GOLDEN_APPLE, new ArrayList<UUID>());

        protipsGiven.put(PROTIP_STARTUP_INVINCIBILITY, new ArrayList<UUID>());


        // Sound
        proTipsSound = new UHSound(p.getConfig().getConfigurationSection("protips.sound"));
    }


    /**
     * Sends a ProTip to a player.
     * A ProTip is only given one time to a given player.
     *
     * @param player The player
     * @param protip The ProTip to send to this player.
     * @return true if the ProTip was sent, false else (already sent or disabled by the config).
     *
     * @throws IllegalArgumentException if the ProTip is not registered.
     */
    public boolean sendProtip(Player player, String protip)
    {

        if (!protipsGiven.containsKey(protip))
        {
            throw new IllegalArgumentException("Unknown ProTip");
        }

        if (!isProtipEnabled(protip))
        {
            return false;
        }

        if (wasProtipSent(player, protip))
        {
            return false;
        }

        protipsGiven.get(protip).add(player.getUniqueId());

        /// ProTip invite, displayed before a ProTip.
        player.sendMessage(I.t("{darkpurple}ProTip!") + " " + ChatColor.RESET + I.t("protips." + protip));
        proTipsSound.play(player);

        return false;
    }

    /**
     * Checks if a ProTip was already sent to a player.
     *
     * @param player The player.
     * @param protip The ProTip.
     * @return true if the ProTip was sent; false else.
     *
     * @throws IllegalArgumentException if the ProTip is not registered.
     */
    public boolean wasProtipSent(Player player, String protip)
    {
        if (!protipsGiven.containsKey(protip))
        {
            throw new IllegalArgumentException("Unknown ProTip");
        }

        return protipsGiven.get(protip).contains(player.getUniqueId());
    }

    /**
     * Checks if the given ProTip is enabled in the config.
     *
     * @param protip The ProTip to check.
     *
     * @return true if the ProTip is enabled.
     */
    public boolean isProtipEnabled(String protip)
    {
        return p.getConfig().getBoolean("protips." + protip);
    }
}
