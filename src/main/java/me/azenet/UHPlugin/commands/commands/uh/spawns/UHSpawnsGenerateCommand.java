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
package me.azenet.UHPlugin.commands.commands.uh.spawns;

import me.azenet.UHPlugin.UHPlugin;
import me.azenet.UHPlugin.commands.core.annotations.Command;
import me.azenet.UHPlugin.commands.core.commands.UHCommand;
import me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException;
import me.azenet.UHPlugin.i18n.I18n;
import me.azenet.UHPlugin.spawns.Generator;
import me.azenet.UHPlugin.spawns.exceptions.CannotGenerateSpawnPointsException;
import me.azenet.UHPlugin.spawns.exceptions.UnknownGeneratorException;
import me.azenet.UHPlugin.teams.UHTeam;
import me.azenet.UHPlugin.utils.CommandUtils;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Command(name = "generate")
public class UHSpawnsGenerateCommand extends UHCommand {

	private UHPlugin p;
	private final I18n i;

	public UHSpawnsGenerateCommand(UHPlugin plugin) {
		p = plugin;
		i = p.getI18n();
	}

	/**
	 * Runs the command.
	 *
	 * @param sender The sender of the command.
	 * @param args   The arguments passed to the command.
	 *
	 * @throws me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException If the command cannot be executed.
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException {

		if(args.length == 0) { // Help
			throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
		}

		String generationMethod = args[0];

		// Default values
		Integer size = p.getBorderManager().getCurrentBorderDiameter() - 25; // Avoid spawn points being too close to the border
		Integer distanceMinBetweenTwoPoints = 250;
		World world = p.getServer().getWorlds().get(0);
		Double xCenter = world.getSpawnLocation().getX();
		Double zCenter = world.getSpawnLocation().getZ();

		Integer spawnsCount = 0;
		for(UHTeam team : p.getTeamManager().getTeams()) {
			if(!team.isEmpty()) spawnsCount++;
		}

		if(args.length < 7) {
			if(sender instanceof Player) {
				world = ((Player) sender).getWorld();
			}
			else if(sender instanceof BlockCommandSender) {
				world = ((BlockCommandSender) sender).getBlock().getWorld();
			}

			xCenter = world.getSpawnLocation().getX();
			zCenter = world.getSpawnLocation().getZ();
		}

		// What if the game is in solo, or some players are out of all team?
		// Only if the spawn count is not provided of course. Else, we don't care, this count
		// will be overwritten.
		if(args.length < 5) {
			if(spawnsCount == 0) { // Solo mode?
				sender.sendMessage(i.t("spawns.assumptions.solo"));
				spawnsCount = p.getServer().getOnlinePlayers().size() - p.getGameManager().getStartupSpectators().size();
			}
			else {
				// Trying to find players without team
				int playersWithoutTeam = 0;
				for(Player player : p.getServer().getOnlinePlayers()) {
					if(p.getTeamManager().getTeamForPlayer(player) == null) {
						playersWithoutTeam++;
					}
				}

				if(playersWithoutTeam != 0) {
					sender.sendMessage(i.t("spawns.assumptions.partialSolo"));
					spawnsCount += playersWithoutTeam;
				}
			}
		}

		try {
			if(args.length >= 2) { // size included
				size = Integer.valueOf(args[1]);

				if(args.length >= 3) { // distance minimal included
					distanceMinBetweenTwoPoints = Integer.valueOf(args[2]);

					if(args.length >= 4) { // spawn count included
						spawnsCount = Integer.valueOf(args[3]);

						if(args.length >= 5) { // xCenter included
							xCenter = Double.parseDouble(args[4]);

							if(args.length >= 6) { // zCenter included
								zCenter = Double.parseDouble(args[5]);

								if(args.length >= 7) { // world included
									World inputWorld = p.getServer().getWorld(args[6]);

									if(inputWorld != null) {
										world = inputWorld;
									}
									else {
										sender.sendMessage(i.t("spawns.generate.unknownWorld", args[6]));
										return;
									}
								}
							}
						}
					}
				}
			}
		} catch(NumberFormatException e) {
			sender.sendMessage(i.t("spawns.NaN"));
			return;
		}


		if(spawnsCount <= 0) {
			sender.sendMessage(i.t("spawns.generate.nothingToDo"));
			return;
		}


		try {
			p.getSpawnsManager().generateSpawnPoints(generationMethod, world, spawnsCount, size, distanceMinBetweenTwoPoints, xCenter, zCenter);

		} catch (UnknownGeneratorException e) {
			sender.sendMessage(i.t("spawns.generate.unsupportedMethod", generationMethod));
			return;

		} catch (CannotGenerateSpawnPointsException e) {
			sender.sendMessage(i.t("spawns.generate.impossible"));
			return;
		}

		sender.sendMessage(i.t("spawns.generate.success"));
	}

	/**
	 * Tab-completes this command.
	 *
	 * @param sender The sender.
	 * @param args   The arguments passed to the command.
	 *
	 * @return A list of suggestions.
	 */
	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		// Generation methods - /uh spawns generate <?>
		if(args.length == 1) {
			ArrayList<String> suggested = new ArrayList<String>();

			for(Generator generator : Generator.values()) {
				suggested.add(generator.name().toLowerCase());
			}

			return CommandUtils.getAutocompleteSuggestions(args[0], suggested);
		}

		// Worlds - /uh spawns generate - - - - - - <?>
		else if(args.length == 7) {
			ArrayList<String> suggested = new ArrayList<String>();
			for(World world : p.getServer().getWorlds()) {
				suggested.add(world.getName());
			}

			return CommandUtils.getAutocompleteSuggestions(args[6], suggested);
		}

		else return null;
	}

	/**
	 * Returns the help of this command.
	 * <p/>
	 * <p>
	 * The first line should describe briefly the command, as this line is displayed as
	 * a line of the help of the parent command.
	 * </p>
	 * <p>
	 * The other lines will only be displayed if the {@link me.azenet.UHPlugin.commands.core.exceptions.CannotExecuteCommandException}
	 * is caught by the command executor.
	 * </p>
	 *
	 * @param sender The sender.
	 *
	 * @return The help. One line per entry in the list.
	 */
	@Override
	public List<String> help(CommandSender sender) {
		return Arrays.asList(i.t("cmd.spawnsHelpGenerate"),
				i.t("cmd.spawnsHelpGenerateDetailsCmdTitle"),
				i.t("cmd.spawnsHelpGenerateDetailsCmd"),
				i.t("cmd.spawnsHelpGenerateDetailsShapesTitle"),
				i.t("cmd.spawnsHelpGenerateDetailsShapesRandom"),
				i.t("cmd.spawnsHelpGenerateDetailsShapesGrid"),
				i.t("cmd.spawnsHelpGenerateDetailsShapesCircular"),
				i.t("cmd.spawnsHelpGenerateDetailsArgsTitle"),
				i.t("cmd.spawnsHelpGenerateDetailsArgsSize"),
				i.t("cmd.spawnsHelpGenerateDetailsArgsDistanceMin"),
				i.t("cmd.spawnsHelpGenerateDetailsArgsCount"),
				i.t("cmd.spawnsHelpGenerateDetailsArgsCenter"),
				i.t("cmd.spawnsHelpGenerateDetailsArgsWorld"));
	}
}
