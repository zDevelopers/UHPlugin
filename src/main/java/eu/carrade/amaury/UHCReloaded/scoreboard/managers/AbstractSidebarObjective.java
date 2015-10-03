/**
 * Plugin UltraHardcore Reloaded (UHPlugin)
 * Copyright (C) 2013 azenet
 * Copyright (C) 2014-2015 Amaury Carrade
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see [http://www.gnu.org/licenses/].
 */

package eu.carrade.amaury.UHCReloaded.scoreboard.managers;

import org.bukkit.entity.Player;

import java.util.ArrayList;


/**
 * This class represents a sidebar objective used to display informations
 * (aka lines of text).
 * <p>
 * It manages automatically the scores of the lines (entries), to have the
 * smallest positive numbers possible.
 * <p>
 * <strong>WARNING</strong>: in this class, the <em>index</em> of an entry is
 * 0 for the first one (at the top of the sidebar), and <tt>the number of entries - 1</tt>
 * for the last one.<br />
 * It is NOT the index used as a score in the objective.
 *
 * <h3>About separators</h3>
 * <em>(<tt>so</tt> is a SidebarObjective object.)</em>
 * <p>
 * To add a separator (blank line on the sidebar), use
 * <tt>so.{@link #addEntry(String) addEntry}(SidebarObjective.SEPARATOR);</tt>.<br />
 * This class will automatically use a different number of spaces to allow you to use
 * multiple separators (hard vanilla limit: 17 separators per scoreboard).
 * <p>
 * To remove a separator, it's a bit more complicated, because separators are not unique.
 * <ul>
 * 	<li>If there is only one separator in the scoreboard, you can safely use
 *      <tt>so.{@link #removeEntry(String) removeEntry}(SidebarObjective.SEPARATOR);</tt>.</li>
 * 	<li>Else, this method will remove the <em>first separator</em> only.<br />
 *      To remove a specific separator, use {@link #removeEntryAtIndex(int)}, using the index
 *      of an entry just before or after the space you want to remove as a reference.<br />
 *      this index can be retrieved using
 *      <tt>so.{@link #getEntryIndex(String) getEntryIndex}("the entry before/after")</tt>.<br />
 *      Just add or remove <tt>1</tt> to get the index of the separator.
 *  </li>
 * </ul>
 *
 * @author Amaury Carrade
 */
public abstract class AbstractSidebarObjective
{
	/**
	 * Represents an entry used as a separator.
	 */
	public static String SEPARATOR = "";


	protected ArrayList<String> entries = new ArrayList<>();
	protected String displayName = null;


	/**
	 * Sets the display name of this objective (aka the title of the displayed table).
	 *
	 * @param displayName The display name.
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
		setDisplayName();
	}

	/**
	 * Ensures the given player will use the good scoreboard.
	 *
	 * @param player The player.
	 */
	public abstract void setScoreboardForPlayer(Player player);

	/**
	 * Sets the display name of this objective (aka the title of the displayed table), using the
	 * stored display name.
	 *
	 * Used when the scoreboard is reconstructed.
	 */
	protected abstract void setDisplayName();

	/**
	 * Displays the objective on the sidebar.
	 *
	 * @throws IllegalStateException If the objective cannot be displayed.
	 */
	public abstract void display();


	/**
	 * Adds an entry at the bottom of the sidebar.
	 * <p>
	 * An entry is a line in the sidebar.<br />
	 * The entry is truncated at 16 characters (Minecraft limitation).
	 * <p>
	 * If an entry with this text is already registered, nothing is done.
	 *
	 * @param entry The entry to add.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */

	public boolean addEntry(String entry, boolean dontReconstruct)
	{
		entry = truncateEntry(entry);

		if (!entries.contains(entry) || entry.equals(SEPARATOR))
		{ // Multiple separators are allowed.
			this.entries.add(entry);

			if (!dontReconstruct) reconstruct();

			return true;
		}

		return false;
	}

	/**
	 * Adds an entry at the bottom of the sidebar.
	 * <p>
	 * An entry is a line in the sidebar.<br />
	 * The entry is truncated at 16 characters (Minecraft limitation).
	 * <p>
	 * If an entry with this text is already registered, nothing is done.
	 *
	 * @param entry The entry to add.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntry(String entry)
	{
		return addEntry(entry, false);
	}

	/**
	 * Adds an entry at the given index.
	 * <p>
	 * Shifts the element currently at that position
	 * (if any) and any subsequent elements to the bottom
	 * (adds one to their indexes).
	 * <p>
	 * The entry is truncated at 16 characters (Minecraft limitation).
	 *
	 * @param index Where
	 * @param entry What
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryAtIndex(int index, String entry, boolean dontReconstruct)
	{
		entry = truncateEntry(entry);

		if (!entries.contains(entry) || entry.equals(SEPARATOR))
		{
			ensureSize(entries, index + 1);
			entries.add(index, entry);

			if (!dontReconstruct)
			{
				reconstruct();
			}

			return true;
		}

		return false;
	}

	/**
	 * Adds an entry at the given index.
	 * <p>
	 * Shifts the element currently at that position
	 * (if any) and any subsequent elements to the right
	 * (adds one to their indices).
	 * <p>
	 * The entry is truncated at 16 characters (Minecraft limitation).
	 *
	 * @param index Where
	 * @param entry What
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryAtIndex(int index, String entry)
	{
		return addEntryAtIndex(index, entry, false);
	}

	/**
	 * Adds an entry after the specified entry.
	 * <p>
	 * If the string <tt>afterThis</tt> is not registered, the entry is added at the bottom
	 * of the sidebar.
	 * <p>
	 * The entries are truncated at 16 characters (Minecraft limitation).
	 *
	 * @param afterThis The entry will be added after this entry. Don't use a separator for this!
	 * @param entry The entry to add.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryAfter(String afterThis, String entry, boolean dontReconstruct)
	{
		entry = truncateEntry(entry);

		if (!entries.contains(entry) || entry.equals(SEPARATOR))
		{
			int beforeIndex = entries.indexOf(truncateEntry(afterThis));

			if (beforeIndex == -1 || beforeIndex == entries.size())
			{
				return addEntry(entry, dontReconstruct);
			}

			addEntryAtIndex(beforeIndex + 1, entry, dontReconstruct);

			return true;
		}

		return false;
	}

	/**
	 * Adds an entry after the specified entry.
	 * <p>
	 * If the string <tt>afterThis</tt> is not registered, the entry is added at the bottom
	 * of the sidebar.
	 * <p>
	 * The entries are truncated at 16 characters (Minecraft limitation).
	 *
	 * @param afterThis The entry will be added after this entry.
	 * @param entry The entry to add.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryAfter(String afterThis, String entry)
	{
		return addEntryAfter(afterThis, entry, false);
	}

	/**
	 * Adds an entry before the specified entry.
	 * <p>
	 * If the string <tt>beforeThis</tt> is not registered, the entry is added at the top
	 * of the sidebar.
	 * <p>
	 * The entries are truncated at 16 characters (Minecraft limitation).
	 *
	 * @param beforeThis The entry will be added before this entry.
	 * @param entry The entry to add.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryBefore(String beforeThis, String entry, boolean dontReconstruct)
	{
		entry = truncateEntry(entry);

		if (!entries.contains(entry) || entry.equals(SEPARATOR))
		{
			int afterIndex = entries.indexOf(truncateEntry(beforeThis));

			if (afterIndex == -1 || afterIndex == entries.size())
			{
				return addEntryAtIndex(0, entry, dontReconstruct);
			}

			addEntryAtIndex(afterIndex - 1, entry, dontReconstruct);

			return true;
		}

		return false;
	}

	/**
	 * Adds an entry before the specified entry.
	 * <p>
	 * If the string <tt>beforeThis</tt> is not registered, the entry is added at the top
	 * of the sidebar.
	 * <p>
	 * The entry are truncated at 16 characters (Minecraft limitation).
	 *
	 * @param beforeThis The entry will be added before this entry.
	 * @param entry The entry to add.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryBefore(String beforeThis, String entry)
	{
		return addEntryBefore(beforeThis, entry, false);
	}

	/**
	 * Updates the entry <tt>oldEntry</tt> with the text <tt>newEntry</tt>.
	 * <p>
	 * The entries are truncated at 16 characters (Minecraft limitation).
	 *
	 * @param oldEntry The text to be updated. Don't use a separator here!
	 * @param newEntry The updated text.
	 *
	 * @return <tt>True</tt> if the text was updated (aka <tt>true</tt> if an
	 * entry <tt>oldEntry</tt> is registered).
	 */
	public abstract boolean updateEntry(String oldEntry, String newEntry);


	/**
	 * Removes the entry at the given index from the sidebar.
	 *
	 * @param index The index of the entry to remove.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if an entry was removed.
	 */

	public boolean removeEntryAtIndex(int index, boolean dontReconstruct)
	{
		try
		{
			if (entries.get(index) != null)
			{
				entries.remove(index);
				if (!dontReconstruct) reconstruct();
				return true;
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// Entry not defined
		}

		return false;
	}

	/**
	 * Removes the entry at the given index from the sidebar.
	 *
	 * @param index The index of the entry to remove.
	 *
	 * @return <tt>True</tt> if an entry was removed.
	 */

	public boolean removeEntryAtIndex(int index)
	{
		return removeEntryAtIndex(index, false);
	}

	/**
	 * Removes the given entry from the sidebar.
	 * <p>
	 * The entry is truncated at 16 characters (Minecraft limitation).
	 *
	 * @param entry The entry to remove.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if an entry was removed.
	 */

	public boolean removeEntry(String entry, boolean dontReconstruct)
	{
		return removeEntryAtIndex(entries.indexOf(entry), dontReconstruct);
	}

	/**
	 * Removes the given entry from the sidebar.
	 * <p>
	 * The entry is truncated at 16 characters (Minecraft limitation).
	 *
	 * @param entry The entry to remove.
	 *
	 * @return <tt>True</tt> if an entry was removed.
	 */

	public boolean removeEntry(String entry)
	{
		return removeEntry(entry, false);
	}


	/**
	 * Resets the sidebar, removing all entries.
	 *
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 */

	public void reset(boolean dontReconstruct)
	{
		entries.clear();

		if (!dontReconstruct) reconstruct();
	}

	/**
	 * Resets the sidebar, removing all entries.
	 */

	public void reset()
	{
		reset(false);
	}


	/**
	 * Returns the index of the given entry.
	 * <p>
	 * The entry is truncated at 16 characters (Minecraft limitation).
	 *
	 * @param entry The entry.
	 * @return The index, or {@code -1} if the given {@code entry} is not registered.
	 */

	public int getEntryIndex(String entry)
	{
		return entries.indexOf(truncateEntry(entry));
	}

	/**
	 * Returns the (truncated) entry at the given index.
	 *
	 * @param index The index.
	 * @return The entry.
	 */

	public String getEntry(int index)
	{
		return entries.get(index);
	}


	/**
	 * Reconstructs the sidebar from scratch, with recalculated scores
	 * following the number of entries.
	 * Automatically called when an entry is added/removed, except if the reconstruction
	 * is explicitly disabled using the dontReconstruct parameter.
	 */
	public abstract void reconstruct();


	/**
	 * Truncates the given entry at 16 characters, the maximal size allowed by
	 * Minecraft.
	 *
	 * @param entry The entry to truncate.
	 * @return The truncated entry.
	 */
	private String truncateEntry(String entry)
	{
		return entry.substring(0, Math.min(entry.length(), 16));
	}

	/**
	 * Generates a string containing {@code spaces} spaces.
	 *
	 * @param spaces The number of spaces in the string.
	 *
	 * @return The string.
	 */
	protected String generateSpaces(int spaces)
	{
		String space = "";

		for (int i = 0; i < spaces; i++)
		{
			space += " ";
		}

		return space;
	}

	/**
	 * Increases the size of the given {@code ArrayList} if needed.
	 *
	 * @param list The list.
	 * @param size The size.
	 */
	private void ensureSize(ArrayList<?> list, int size)
	{
		if (list.size() < size)
		{
			// Prevent excessive copying while we're adding
			list.ensureCapacity(size);

			while (list.size() < size)
			{
				list.add(null);
			}
		}
	}
}
