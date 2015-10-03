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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;


public class SynchronizedSidebarObjective extends AbstractSidebarObjective
{
	private Scoreboard scoreboard = null;
	private String objectiveName = null;
	private Objective objective = null;

	public SynchronizedSidebarObjective(Scoreboard scoreboard, String objectiveName)
	{
		this.scoreboard = scoreboard;
		this.objectiveName = objectiveName;

		setDisplayName(objectiveName);
	}

	@Override
	public void setScoreboardForPlayer(Player player)
	{
		player.setScoreboard(scoreboard);
	}

	@Override
	protected void setDisplayName()
	{
		objective.setDisplayName(displayName.substring(0, Math.min(displayName.length(), 32)));
	}

	@Override
	public void display()
	{
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	@Override
	public boolean updateEntry(String oldEntry, String newEntry)
	{
		int index = entries.indexOf(oldEntry);
		if (index != -1)
		{
			entries.set(index, newEntry);

			int score = objective.getScore(oldEntry).getScore();
			scoreboard.resetScores(oldEntry);
			objective.getScore(newEntry).setScore(score);

			return true;
		}

		return false;
	}

	@Override
	public void reconstruct()
	{
		// First: the objective is removed, then recreated, to reset it.
		this.unregisterObjective();
		this.registerObjective();

		// We don't want a "0" score, because these scores need a special
		// initialization (set to 1, and to 0 one tick later).
		int score = entries.size();
		int maxScore = score;

		// We use this to generate a different number of spaces for each separator.
		int separatorSpacesCount = 0;

		for (int i = 0; i < maxScore; i++)
		{
			String entry = entries.get(i);

			if (entry == null) continue;

			if (entry.equals(SEPARATOR))
			{
				objective.getScore(generateSpaces(separatorSpacesCount)).setScore(score);
				separatorSpacesCount++;
			}
			else
			{
				objective.getScore(entries.get(i)).setScore(score);
			}

			score--;
		}
	}

	/**
	 * Unregisters the objective.
	 */
	public void unregisterObjective()
	{
		objective.unregister();
	}

	/**
	 * Registers the objective.
	 */
	public void registerObjective()
	{
		this.objective = scoreboard.registerNewObjective(objectiveName, "dummy");
		setDisplayName();
		display();
	}
}
