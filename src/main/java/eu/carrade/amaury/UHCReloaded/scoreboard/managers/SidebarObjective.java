/**
 *  Plugin UltraHardcore Reloaded (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014-2015 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
package eu.carrade.amaury.UHCReloaded.scoreboard.managers;

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
public interface SidebarObjective {

	/**
	 * Represents an entry used as a separator.
	 */
	String SEPARATOR = "";

	/**
	 * Sets the display name of this objective (aka the title of the displayed table).
	 *
	 * @param displayName The display name.
	 */
	void setDisplayName(String displayName);

	/**
	 * Displays the objective on the sidebar.
	 *
	 * @throws IllegalStateException If the objective has been unregistered
	 * with {@link #unregisterObjective()}.
	 */
	void display();

	/**
	 * Unregisters the objective.
	 */
	void unregisterObjective();

	/**
	 * Registers the objective.
	 */
	void registerObjective();

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
	boolean addEntry(String entry, boolean dontReconstruct);

	/**
	 * Adds an entry at the bottom of the sidebar.
	 * <p>
	 * An entry is a line in the sidebar.
	 * <p>
	 * If an entry with this text is already registered, nothing is done.
	 *
	 * @param entry The entry to add.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	boolean addEntry(String entry);

	/**
	 * Adds an entry at the given index.
	 * <p>
	 * Shifts the element currently at that position
	 * (if any) and any subsequent elements to the bottom
	 * (adds one to their indexes).
	 *
	 * @param index Where
	 * @param entry What
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	boolean addEntryAtIndex(int index, String entry, boolean dontReconstruct);

	/**
	 * Adds an entry at the given index.
	 * <p>
	 * Shifts the element currently at that position
	 * (if any) and any subsequent elements to the right
	 * (adds one to their indices).
	 *
	 * @param index Where
	 * @param entry What
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	boolean addEntryAtIndex(int index, String entry);

	/**
	 * Adds an entry after the specified entry.
	 * <p>
	 * If the string <tt>afterThis</tt> is not registered, the entry is added at the bottom
	 * of the sidebar.
	 *
	 * @param afterThis The entry will be added after this entry. Don't use a separator for this!
	 * @param entry The entry to add.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	boolean addEntryAfter(String afterThis, String entry, boolean dontReconstruct);

	/**
	 * Adds an entry after the specified entry.
	 * <p>
	 * If the string <tt>afterThis</tt> is not registered, the entry is added at the bottom
	 * of the sidebar.
	 *
	 * @param afterThis The entry will be added after this entry.
	 * @param entry The entry to add.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	boolean addEntryAfter(String afterThis, String entry);

	/**
	 * Adds an entry before the specified entry.
	 * <p>
	 * If the string <tt>beforeThis</tt> is not registered, the entry is added at the top
	 * of the sidebar.
	 *
	 * @param beforeThis The entry will be added before this entry.
	 * @param entry The entry to add.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	boolean addEntryBefore(String beforeThis, String entry, boolean dontReconstruct);

	/**
	 * Adds an entry before the specified entry.
	 * <p>
	 * If the string <tt>beforeThis</tt> is not registered, the entry is added at the top
	 * of the sidebar.
	 *
	 * @param beforeThis The entry will be added before this entry.
	 * @param entry The entry to add.
	 *
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	boolean addEntryBefore(String beforeThis, String entry);

	/**
	 * Updates the entry <tt>oldEntry</tt> with the text <tt>newEntry</tt>.
	 *
	 * @param oldEntry The text to be updated. Don't use a separator here!
	 * @param newEntry The updated text.
	 *
	 * @return <tt>True</tt> if the text was updated (aka <tt>true</tt> if an
	 * entry <tt>oldEntry</tt> is registered).
	 */
	boolean updateEntry(String oldEntry, String newEntry);

	/**
	 * Removes the entry at the given index from the sidebar.
	 *
	 * @param index The index of the entry to remove.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if an entry was removed.
	 */
	boolean removeEntryAtIndex(int index, boolean dontReconstruct);

	/**
	 * Removes the entry at the given index from the sidebar.
	 *
	 * @param index The index of the entry to remove.
	 *
	 * @return <tt>True</tt> if an entry was removed.
	 */
	boolean removeEntryAtIndex(int index);

	/**
	 * Removes the given entry from the sidebar.
	 *
	 * @param entry The entry to remove.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 *
	 * @return <tt>True</tt> if an entry was removed.
	 */
	boolean removeEntry(String entry, boolean dontReconstruct);

	/**
	 * Removes the given entry from the sidebar.
	 *
	 * @param entry The entry to remove.
	 *
	 * @return <tt>True</tt> if an entry was removed.
	 */
	boolean removeEntry(String entry);

	/**
	 * Resets the sidebar, removing all entries.
	 *
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstruct()} to do so.
	 */
	void reset(boolean dontReconstruct);

	/**
	 * Resets the sidebar, removing all entries.
	 */
	void reset();

	/**
	 * Returns the index of the given entry.
	 *
	 * @param entry The entry.
	 * @return The index, or {@code -1} if the given {@code entry} is not registered.
	 */
	int getEntryIndex(String entry);

	/**
	 * Returns the (truncated) entry at the given index.
	 *
	 * @param index The index.
	 * @return The entry.
	 */
	String getEntry(int index);

	/**
	 * Reconstructs the sidebar from scratch, with recalculated scores
	 * following the number of entries.
	 * Automatically called when an entry is added/removed, except if the reconstruction
	 * is explicitly disabled using the dontReconstruct parameter.
	 */
	void reconstruct();
}
