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

package eu.carrade.amaury.UHCReloaded.utils;

import fr.zcraft.zlib.tools.Callback;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;


public class UHUtils
{
    /**
     * Extracts a string from a list of arguments, starting at the given index.
     *
     * @param args The raw arguments.
     * @param startIndex The index of the first item in the returned string (first argument given: 0).
     *
     * @return The extracted string.
     *
     * @throws IllegalArgumentException if the index of the first element is out of the bounds of the arguments' list.
     */
    public static String getStringFromCommandArguments(String[] args, int startIndex)
    {
        if (args.length < startIndex)
        {
            throw new IllegalArgumentException("The index of the first element is out of the bounds of the arguments' list.");
        }

        String text = "";

        for (int index = startIndex; index < args.length; index++)
        {
            if (index < args.length - 1)
            {
                text += args[index] + " ";
            }
            else
            {
                text += args[index];
            }
        }

        return text;
    }

    /**
     * Converts a string to a number of seconds.
     * <p>
     * Format:
     * <ul>
     *    <li><tt>mm</tt> – number of minutes;</li>
     *    <li><tt>mm:ss</tt> – minutes and seconds;</li>
     *    <li><tt>hh:mm:ss</tt> – hours, minutes and seconds.</li>
     * </ul>
     *
     *
     * @param text The text to be converted.
     * @return The number of seconds represented by this string.
     *
     * @throws IllegalArgumentException if the text is not formatted as above.
     * @throws NumberFormatException if the text between the colons cannot be converted in integers.
     */
    public static int string2Time(String text)
    {
        String[] split = text.split(":");

        if (text.isEmpty() || split.length > 3)
        {
            throw new IllegalArgumentException("Badly formatted string in string2time, formats allowed are mm, mm:ss or hh:mm:ss.");
        }

        if (split.length == 1)  // "mm"
        {
            return Integer.valueOf(split[0]) * 60;
        }
        else if (split.length == 2)  // "mm:ss"
        {
            return Integer.valueOf(split[0]) * 60 + Integer.valueOf(split[1]);
        }
        else  // "hh:mm:ss"
        {
            return Integer.valueOf(split[0]) * 3600 + Integer.valueOf(split[1]) * 60 + Integer.valueOf(split[2]);
        }
    }

    /**
     * Converts a string to a number of seconds.
     *
     * Prints a warning if the format is invalid.
     *
     * @param text The text to be converted.
     * @param defaultValue The default value returned if the format is invalid.
     *
     * @return The extracted seconds, or the default value if invalid.
     * @see #string2Time(String)
     */
    public static int string2Time(String text, Integer defaultValue)
    {
        try
        {
            return string2Time(text);
        }
        catch (IllegalArgumentException | NullPointerException e)
        {
            PluginLogger.warning("Invalid duration '{0}', using {1} seconds instead.", text, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Converts a string to a boolean.
     *
     * <p>
     *     {@code true, on, 1, yes} (case insensitive) -> {@code true}.<br />
     *     Anything else ({@code null} included) -> {@code false}.
     * </p>
     *
     * @param raw The raw string.
     * @return a boolean.
     */
    public static boolean stringToBoolean(String raw)
    {
        return raw != null
                && (
                raw.equalsIgnoreCase("true")
                        || raw.equalsIgnoreCase("on")
                        || raw.equalsIgnoreCase("1")
                        || raw.equalsIgnoreCase("yes")
        );
    }

    /**
     * @param integer An integer.
     * @return A string representation of this integer, with an explicit "+" if positive.
     */
    public static String integerToStringWithSign(int integer)
    {
        return (integer < 0 ? "" : "+") + String.valueOf(integer);
    }


    /**
     * Finds a safe spot where teleport the player, and teleport the player to that spot.
     * If a spot is not found, the player is not teleported, except if {@code force} is set to true.
     *
     * @param player
     * @param location
     * @param force If true the player will be teleported to the exact given location if there is no safe spot.
     * @return true if the player was effectively teleported.
     */
    public static boolean safeTP(Player player, Location location, boolean force)
    {
        // If the target is safe, let's go
        if (isSafeSpot(location))
        {
            player.teleport(location);
            return true;
        }

        // If the teleportation is forced, let's go
        if (force)
        {
            player.teleport(location);
            return true;
        }

        Location safeSpot = searchSafeSpot(location);

        // A spot was found, let's teleport.
        if (safeSpot != null)
        {
            player.teleport(safeSpot);
            return true;
        }
        // No spot found; the teleportation is cancelled.
        else
        {
            return false;
        }
    }

    /**
     * Searches a safe spot where teleport the player, and teleport the player to that spot.
     * If a spot is not found, the player is not teleported, except if {@code force} is set to true.
     *
     * @param player
     * @param location
     * @return true if the player was effectively teleported.
     */
    public static boolean safeTP(Player player, Location location)
    {
        return safeTP(player, location, false);
    }

    /**
     * Searches a safe spot in the given location.
     *
     * The spot is in the same X;Z coordinates.
     *
     * @param location The location where to find a safe spot.
     * @return A Location object representing the safe spot, or null if no safe spot is available.
     */
    public static Location searchSafeSpot(Location location)
    {
        // We try to find a spot above or below the target

        Location safeSpot = null;
        final int maxHeight = (location.getWorld().getEnvironment() == World.Environment.NETHER) ? 125 : location.getWorld().getMaxHeight() - 2; // (thx to WorldBorder)

        for (int yGrow = (int) location.getBlockY(), yDecr = (int) location.getBlockY(); yDecr >= 1 || yGrow <= maxHeight; yDecr--, yGrow++)
        {
            // Above?
            if (yGrow < maxHeight)
            {
                Location spot = new Location(location.getWorld(), location.getBlockX(), yGrow, location.getBlockZ());
                if (isSafeSpot(spot))
                {
                    safeSpot = spot;
                    break;
                }
            }

            // Below?
            if (yDecr > 1 && yDecr != yGrow)
            {
                Location spot = new Location(location.getWorld(), location.getX(), yDecr, location.getZ());
                if (isSafeSpot(spot))
                {
                    safeSpot = spot;
                    break;
                }
            }
        }

        // A spot was found, we changes the pitch & yaw according to the original location.
        if (safeSpot != null)
        {
            safeSpot.setPitch(location.getPitch());
            safeSpot.setYaw(location.getYaw());
        }

        return safeSpot;
    }

    /**
     * Checks if a given location is safe.
     * A safe location is a location with two breathable blocks (aka transparent block or water)
     * over something solid (or water).
     *
     * @param location
     * @return true if the location is safe.
     */
    public static boolean isSafeSpot(Location location)
    {
        Block blockCenter = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Block blockAbove = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());
        Block blockBelow = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());

        if ((blockCenter.getType().isTransparent() || (blockCenter.isLiquid() && !blockCenter.getType().equals(Material.LAVA) && !blockCenter.getType().equals(Material.STATIONARY_LAVA)))
                && (blockAbove.getType().isTransparent() || (blockAbove.isLiquid() && !blockAbove.getType().equals(Material.LAVA) && !blockCenter.getType().equals(Material.STATIONARY_LAVA))))
        {
            // two breathable blocks: ok

            if (blockBelow.getType().isSolid() || blockBelow.getType().equals(Material.WATER) || blockBelow.getType().equals(Material.STATIONARY_WATER))
            {
                // The block below is solid, or liquid (but not lava)
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }


    /**
     * Spawns a random firework at the given location.
     *
     * Please note: because the power of a firework is an integer, the min/max heights
     * are with a precision of ±5 blocks.
     *
     * @param location The location where the firework will be spawned.
     * @param heightMin The minimal height of the explosion.
     * @param heightMax The maximal height of the explosion.
     *
     * @return The random firework generated.
     */
    public static Firework generateRandomFirework(Location location, int heightMin, int heightMax)
    {
        Random rand = new Random();

        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();

        int effectsCount = rand.nextInt(3) + 1;

        for (int i = 0; i < effectsCount; i++)
        {
            meta.addEffect(generateRandomFireworkEffect());
        }

        // One level of power is half a second of flight time.
        // In half a second, a firework fly ~5 blocks.
        // So, one level of power = ~5 blocks.
        meta.setPower((int) Math.min(Math.floor((heightMin / 5) + rand.nextInt(heightMax / 5)), 128D));

        firework.setFireworkMeta(meta);

        return firework;
    }

    /**
     * Generates a random firework effect.
     *
     * @return The firework effect.
     */
    private static FireworkEffect generateRandomFireworkEffect()
    {
        Random rand = new Random();
        Builder fireworkBuilder = FireworkEffect.builder();

        int colorCount = rand.nextInt(3) + 1;
        int trailCount = rand.nextInt(3) + 1;

        fireworkBuilder.flicker(rand.nextInt(3) == 1);
        fireworkBuilder.trail(rand.nextInt(3) == 1);

        for (int i = 0; i < colorCount; i++)
        {
            fireworkBuilder.withColor(generateRandomColor());
        }

        for (int i = 0; i < trailCount; i++)
        {
            fireworkBuilder.withFade(generateRandomColor());
        }

        // Random shape
        FireworkEffect.Type[] types = FireworkEffect.Type.values();
        fireworkBuilder.with(types[rand.nextInt(types.length)]);

        return fireworkBuilder.build();
    }

    /**
     * Generates a random color.
     *
     * @return The color.
     */
    private static Color generateRandomColor()
    {
        Random rand = new Random();
        return Color.fromBGR(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    /**
     * Calls a callback if it is defined (non-{@code null}).
     *
     * @param callback The callback
     * @param argument The callback's argument.
     * @param <T> The callback's argument type.
     */
    public static <T> void callIfDefined(Callback<T> callback, T argument)
    {
        if (callback != null) callback.call(argument);
    }

    /**
     * Returns the overworld.
     *
     * @return the... overworld?
     */
    public static World getOverworld()
    {
        for (World world : Bukkit.getServer().getWorlds())
        {
            if (world.getEnvironment() != World.Environment.NETHER && world.getEnvironment() != World.Environment.THE_END)
            {
                return world;
            }
        }
        return null;
    }
}
