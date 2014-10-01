/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014 Amaury Carrade
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

package me.azenet.UHPlugin;

import java.util.ArrayList;

import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * This class represents a sidebar objective used to display informations
 * (aka lines of text).
 * <p>
 * It manages automatically the scores of the lines (entries), to have the
 * smallest positive numbers possible.
 * <p>
 * <strong>WARNING</strong>: in this class, the <em>index</em> of an entry is
 * 0 for the first one (at the top of the sidebar), and the number of entries - 1
 * for the last one.<br />
 * It is NOT the index used as a score in the objective. 
 * 
 * @author Amaury Carrade
 */
public class UHSidebarObjective {
	
	private ArrayList<String> entries = new ArrayList<String>();
	
	private Scoreboard scoreboard = null;
	private String objectiveName = null;
	private Objective objective = null;
	
	public UHSidebarObjective(Scoreboard scoreboard, String objectiveName) {
		this.objectiveName = objectiveName;
		this.scoreboard = scoreboard;
		
		registerObjective();
	}
	
	/**
	 * Sets the display name of this objective (aka the title of the displayed table).
	 * 
	 * @param displayName The display name.
	 */
	public void setDisplayName(String displayName) {
		objective.setDisplayName(displayName);
	}
	
	/**
	 * Displays the objective on the sidebar.
	 * 
	 * @throws IllegalStateException If the objective has been unregistered
	 * with {@link #unregisterObjective()}.
	 */
	public void display() {
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	/**
	 * Unregisters the objective.
	 */
	public void unregisterObjective() {
		objective.unregister();
	}
	
	/**
	 * Registers the objective.
	 */
	public void registerObjective() {
		this.objective = scoreboard.registerNewObjective(objectiveName, "dummy");
		display();
	}
	
	
	/**
	 * Adds an entry at the bottom of the sidebar.
	 * <p>
	 * An entry is a line in the sidebar.
	 * <p>
	 * If an entry with this text is already registered, nothing is done.
	 * 
	 * @param entry The entry to add.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstructSidebar()} to do so.
	 * 
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntry(String entry, boolean dontReconstruct) {
		if(!entries.contains(entry)) {
			this.entries.add(entry);
			
			if(!dontReconstruct) reconstructSidebar();
			
			return true;
		}
		
		return false;
	}
	
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
	public boolean addEntry(String entry) {
		return addEntry(entry, false);
	}
	
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
	 * reconstructed. You will need to call {@link #reconstructSidebar()} to do so.
	 * 
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryAtIndex(int index, String entry, boolean dontReconstruct) {
		if(!entries.contains(entry)) {
			this.entries.add(index, entry);
			
			if(!dontReconstruct) {
				if(index == 0) { // Top - we just need to add a score higher than the other ones.
					objective.getScore(entry).setScore(entries.size());
				}
				else {
					reconstructSidebar();
				}
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
	 * 
	 * @param index Where
	 * @param entry What
	 * 
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryAtIndex(int index, String entry) {
		return addEntryAtIndex(index, entry, false);
	}
	
	/**
	 * Adds an entry after the specified entry.
	 * <p>
	 * If the string <tt>afterThis</tt> is not registered, the entry is added at the bottom
	 * of the sidebar.
	 * 
	 * @param afterThis The entry will be added after this entry.
	 * @param entry The entry to add.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstructSidebar()} to do so.
	 * 
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryAfter(String afterThis, String entry, boolean dontReconstruct) {
		if(!entries.contains(entry)) {
			int beforeIndex = entries.indexOf(afterThis);
			
			if(beforeIndex == -1 || beforeIndex == entries.size()) {
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
	 * 
	 * @param afterThis The entry will be added after this entry.
	 * @param entry The entry to add.
	 * 
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryAfter(String afterThis, String entry) {
		return addEntryAfter(afterThis, entry, false);
	}
	
	/**
	 * Adds an entry before the specified entry.
	 * <p>
	 * If the string <tt>beforeThis</tt> is not registered, the entry is added at the top
	 * of the sidebar.
	 * 
	 * @param beforeThis The entry will be added before this entry.
	 * @param entry The entry to add.
	 * @param dontReconstruct If true, the objective will not been automatically
	 * reconstructed. You will need to call {@link #reconstructSidebar()} to do so.
	 * 
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryBefore(String beforeThis, String entry, boolean dontReconstruct) {
		if(!entries.contains(entry)) {
			int afterIndex = entries.indexOf(beforeThis);
			
			if(afterIndex == -1 || afterIndex == entries.size()) {
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
	 * 
	 * @param beforeThis The entry will be added before this entry.
	 * @param entry The entry to add.
	 * 
	 * @return <tt>True</tt> if the entry was added (aka non already registered).
	 */
	public boolean addEntryBefore(String beforeThis, String entry) {
		return addEntryBefore(beforeThis, entry, false);
	}
	
	/**
	 * Updates the entry <tt>oldEntry</tt> with the text <tt>newEntry</tt>.
	 * 
	 * @param oldEntry The text to be updated.
	 * @param newEntry The updated text.
	 * 
	 * @return <tt>True</tt> if the text was updated (aka <tt>true</tt> if an
	 * entry <tt>oldEntry</tt> is registered).
	 */
	public boolean updateEntry(String oldEntry, String newEntry) {
		int index = entries.indexOf(oldEntry);
		if(index != -1) {
			entries.set(index, newEntry);
			
			int score = objective.getScore(oldEntry).getScore();
			scoreboard.resetScores(oldEntry);
			objective.getScore(newEntry).setScore(score);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Removes the given entry from the sidebar.
	 * 
	 * @param entry The entry to remove.
	 * @return
	 */
	public boolean removeEntry(String entry) {
		return entries.remove(entry);
	}
	
	
	/**
	 * Reconstructs the sidebar from scratch, with recalculated scores
	 * following the number of entries.
	 * Automatically called when an entry is added/removed, except if the reconstruction
	 * is explicitly disabled using the dontReconstruct parameter.
	 */
	public void reconstructSidebar() {
		// First: the objective is removed, then recreated, to reset it.
		this.unregisterObjective();
		this.registerObjective();
		
		// We don't want a "0" score, because these scores need a special
		// initialization (set to 1, and to 0 one tick later).
		int maxScore = entries.size();
		
		// entries.get(i) -> score maxScore - i
		for(int i = 0; i < maxScore; i++) {
			objective.getScore(entries.get(i)).setScore(maxScore - i);
		}
	}
}
