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
package eu.carrade.amaury.UHCReloaded.commands.commands;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHAboutCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHBorderCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHFeedAllCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHFeedCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHFinishCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHFreezeCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHGenerateWallsCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHHealAllCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHHealCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHInfosCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHKillCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHResurrectCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHRulesCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHShiftCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHSpawnsCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHSpectatorsCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHStartCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHTPBackCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHTPCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHTPSpawnCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHTeamCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.UHTimersCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import org.bukkit.command.CommandSender;

import java.util.List;


@Command (name = "uh")
public class UHRootCommand extends AbstractCommand
{
    private UHCReloaded p;

    public UHRootCommand(UHCReloaded plugin)
    {
        p = plugin;

        // Game
        registerSubCommand(new UHStartCommand(p));
        registerSubCommand(new UHShiftCommand(p));
        registerSubCommand(new UHSpawnsCommand(p));
        registerSubCommand(new UHTeamCommand(p));
        registerSubCommand(new UHBorderCommand(p));
        registerSubCommand(new UHSpectatorsCommand(p));
        registerSubCommand(new UHGenerateWallsCommand(p));

        // Bugs
        registerSubCommand(new UHHealCommand(p));
        registerSubCommand(new UHHealAllCommand(p));
        registerSubCommand(new UHFeedCommand(p));
        registerSubCommand(new UHFeedAllCommand(p));
        registerSubCommand(new UHKillCommand(p));
        registerSubCommand(new UHResurrectCommand(p));
        registerSubCommand(new UHTPBackCommand(p));
        registerSubCommand(new UHTPSpawnCommand(p));

        // Misc
        registerSubCommand(new UHFinishCommand(p));
        registerSubCommand(new UHFreezeCommand(p));
        registerSubCommand(new UHTimersCommand(p));
        registerSubCommand(new UHTPCommand(p));
        registerSubCommand(new UHInfosCommand(p));
        registerSubCommand(new UHRulesCommand(p));
        registerSubCommand(new UHAboutCommand(p));
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return null;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        return null;
    }
}
