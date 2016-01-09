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
package eu.carrade.amaury.UHCReloaded.commands.commands.uh;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.commands.categories.Category;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersAddCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersDisplayCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersHideCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersListCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersPauseCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersRemoveCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersResumeCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersSetCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersStartCommand;
import eu.carrade.amaury.UHCReloaded.commands.commands.uh.timers.UHTimersStopCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.AbstractCommand;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.i18n.I18n;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;


/**
 * This command manages timers.
 *
 * Usage: /uh timers < add | set | display | hide | start | pause | resume | stop | remove | list >
 */
@Command (name = "timers")
public class UHTimersCommand extends AbstractCommand
{
    UHCReloaded p;
    I18n i;

    public UHTimersCommand(UHCReloaded p)
    {
        this.p = p;
        this.i = p.getI18n();

        registerSubCommand(new UHTimersAddCommand(p));
        registerSubCommand(new UHTimersSetCommand(p));
        registerSubCommand(new UHTimersDisplayCommand(p));
        registerSubCommand(new UHTimersHideCommand(p));
        registerSubCommand(new UHTimersStartCommand(p));
        registerSubCommand(new UHTimersPauseCommand(p));
        registerSubCommand(new UHTimersResumeCommand(p));
        registerSubCommand(new UHTimersStopCommand(p));
        registerSubCommand(new UHTimersRemoveCommand(p));
        registerSubCommand(new UHTimersListCommand(p));
    }

    @Override
    public void run(CommandSender sender, String[] args) throws CannotExecuteCommandException
    {
        throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NEED_DOC, this);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public List<String> help(CommandSender sender)
    {
        return Collections.singletonList(i.t("cmd.timersHelpTitle"));
    }

    @Override
    public List<String> onListHelp(CommandSender sender)
    {
        return Collections.singletonList(i.t("cmd.helpTimers"));
    }

    @Override
    public String getCategory()
    {
        return Category.MISC.getTitle();
    }
}
