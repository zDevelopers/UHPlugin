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
package eu.carrade.amaury.UHCReloaded.commands.core;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.commands.core.annotations.Command;
import eu.carrade.amaury.UHCReloaded.commands.core.exceptions.CannotExecuteCommandException;
import eu.carrade.amaury.UHCReloaded.commands.core.utils.CommandUtils;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * The base of every command executor.
 *
 * <p>
 *     The command executors needs to extend this class and to register the commands in the constructor
 *     with the method {@link #registerCommand}.
 * </p>
 *
 * @version 1.0
 * @author Amaury Carrade
 */
public abstract class AbstractCommandExecutor implements TabExecutor
{
    private UHCReloaded p;

    /**
     * Stores the main commands, i.e. the commands registered in the {@code plugin.yml} file.
     */
    private Map<String, AbstractCommand> mainCommands = new LinkedHashMap<>();

    /**
     * Stores the base permissions of these commands.
     */
    private Map<String, String> mainCommandsPermissions = new LinkedHashMap<>();


    public AbstractCommandExecutor(UHCReloaded plugin)
    {
        p = plugin;
    }


    /**
     * Registers a main, root command. This command must be in the {@code plugin.yml}, or
     * it will never be called.
     *
     * @param command The command.
     *
     * @throws IllegalArgumentException If the command class doesn't have the @Command
     *                                  annotation.
     */
    public void registerCommand(AbstractCommand command)
    {
        Command commandAnnotation = command.getClass().getAnnotation(Command.class);
        if (commandAnnotation == null)
        {
            throw new IllegalArgumentException("Cannot register a command without @Command annotation. Class: " + command.getClass().getCanonicalName() + ".");
        }

        mainCommands.put(commandAnnotation.name(), command);

        String permission = commandAnnotation.permission();

        if (commandAnnotation.noPermission())
        {
            permission = null;
        }
        else if (permission != null && permission.isEmpty())
        {
            if (commandAnnotation.useParentPermission())
            {
                permission = null;
            }
            else
            {
                permission = commandAnnotation.name();
            }
        }

        mainCommandsPermissions.put(commandAnnotation.name(), permission);
    }


    /**
     * Displays the help of a command.
     *
     * <p>
     *     If the command is a complex command, this will display the help of the complex command,
     *     first line excepted, ath then the short help of all sub-commands.<br />
     *     Else, this will display the full help for the command.
     * </p>
     *
     * @param sender The sender.
     * @param command The command.
     * @param isAnError {@code true} if this is displayed due to an error.
     */
    public void displayHelp(CommandSender sender, AbstractCommand command, boolean isAnError)
    {
        if (command.hasSubCommands())
        {
            List<String> help = new LinkedList<>();

            // Root help
            List<String> rootHelp = command.help(sender);
            if (rootHelp != null)
            {
                help.addAll(rootHelp);
            }

            // Then, the help of the sub-commands sorted by category.
            // We first organize the commands per-category.
            Map<String, LinkedList<String>> helpPerCategory = new LinkedHashMap<>();

            for (Map.Entry<String, AbstractCommand> subCommand : command.getSubcommands().entrySet())
            {
                List<String> subHelp = subCommand.getValue().onListHelp(sender);
                String permission = command.getSubcommandsPermissions().get(subCommand.getKey());
                String category = command.getSubcommandsCategories().get(subCommand.getKey());

                if (category == null) category = "";

                if (subHelp != null && subHelp.size() > 0 && (permission == null || sender.hasPermission(permission)))
                {

                    LinkedList<String> helpForThisCategory = helpPerCategory.get(category);
                    if (helpForThisCategory != null)
                    {
                        helpForThisCategory.addAll(subHelp);
                    }
                    else
                    {
                        helpForThisCategory = new LinkedList<>();
                        helpForThisCategory.addAll(subHelp);
                        helpPerCategory.put(category, helpForThisCategory);
                    }
                }
            }

            // After, we add to the help to display these commands, with the titles of the
            // categories.
            for (Map.Entry<String, LinkedList<String>> category : helpPerCategory.entrySet())
            {
                help.add(category.getKey());
                help.addAll(category.getValue());
            }

            displayHelp(sender, help, isAnError);
        }
        else
        {
            List<String> help = command.help(sender);
            if (help == null) help = command.onListHelp(sender);

            displayHelp(sender, help, isAnError);
        }
    }

    /**
     * Displays the help of a command.
     *
     * @param sender The sender; this user will receive the help.
     * @param help The help to display (one line per entry; raw display).
     * @param isAnError {@code true} if this is displayed due to an error.
     */
    public void displayHelp(CommandSender sender, List<String> help, boolean isAnError)
    {
        CommandUtils.displaySeparator(sender);

        if (!isAnError)
        {
            sender.sendMessage(I.t("{yellow}{0} - version {1}", p.getDescription().getDescription(), p.getDescription().getVersion()));
            sender.sendMessage(I.t("{ci}Legend: {cc}/uh command <required> [optional=default] <spaces allowed ...>{ci}."));
        }

        if (help != null)
        {
            for (String line : help)
            {
                if (line != null && !line.isEmpty())
                    sender.sendMessage(line);
            }
        }

        CommandUtils.displaySeparator(sender);

        if (isAnError)
        {
            sender.sendMessage(I.t("{ce}{bold}You cannot execute this command this way."));
            sender.sendMessage(I.t("{ce}The help is displayed above."));
            CommandUtils.displaySeparator(sender);
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args)
    {
        AbstractCommand abstractCommand = mainCommands.get(command.getName());
        if (abstractCommand == null)
        {
            return false;
        }

        try
        {
            String permission = mainCommandsPermissions.get(command.getName());
            if (permission != null && !sender.hasPermission(permission))
            {
                throw new CannotExecuteCommandException(CannotExecuteCommandException.Reason.NOT_ALLOWED);
            }

            abstractCommand.routeCommand(sender, args);

        }
        catch (CannotExecuteCommandException e)
        {
            switch (e.getReason())
            {
                case NOT_ALLOWED:
                    sender.sendMessage(I.t("{ce}You are not allowed to execute this command."));
                    break;

                case ONLY_AS_A_PLAYER:
                    sender.sendMessage(I.t("{ce}This can only be executed as a player."));
                    break;

                case BAD_USE:
                case NEED_DOC:
                    displayHelp(sender, e.getOrigin() != null ? e.getOrigin() : abstractCommand, e.getReason() == CannotExecuteCommandException.Reason.BAD_USE);
                    break;

                case UNKNOWN:
                    break;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args)
    {
        AbstractCommand abstractCommand = mainCommands.get(command.getName());
        return abstractCommand.routeTabComplete(sender, args);
    }

    public Map<String, AbstractCommand> getMainCommands()
    {
        return mainCommands;
    }
}
