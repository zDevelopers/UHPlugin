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
package eu.carrade.amaury.UHCReloaded.modules.core.timers.commands;

import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimeDelta;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.Timer;
import eu.carrade.amaury.UHCReloaded.modules.core.timers.TimersModule;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import eu.carrade.amaury.UHCReloaded.utils.UHUtils;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.commands.WithFlags;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.components.rawtext.RawTextPart;
import fr.zcraft.zlib.tools.text.MessageSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;


@CommandInfo (name = "timers", usageParameters = "[add|display|hide|list|pause|resume|remove|set|start|stop|help]", aliases = {"timer"})
@WithFlags
public class TimersCommand extends Command
{
    private final TimersModule timersModule = UR.module(TimersModule.class);

    @Override
    protected void run() throws CommandException
    {
        if (args.length == 0)
        {
            list();
            return;
        }

        switch (args[0].toLowerCase())
        {
            case "add":
                add(); break;

            case "display":
                display(); break;

            case "hide":
                hide(); break;

            case "list":
                list(); break;

            case "pause":
                pause(); break;

            case "resume":
                resume(); break;

            case "remove":
                remove(); break;

            case "set":
                set(); break;

            case "start":
                start(); break;

            case "stop":
                stop(); break;

            case "help":
            default:
                help();
        }
    }

    private void help()
    {
        info(I.t("{blue}{bold}Command help for {cc}{bold}/uh timers"));
        info(I.t("{cc}/uh timers add <duration> <title ...> {ci}: adds a timer."));
        info(I.t("{cc}/uh timers display <title ...> [--without-name] {ci}: displays a timer in the scoreboard. Automatic when a timer is started."));
        info(I.t("{cc}/uh timers hide <title ...> {ci}: removes a timer from the scoreboard. Don't stops the timer."));
        info(I.t("{cc}/uh timers list {ci}: lists the registered timers."));
        info(I.t("{cc}/uh timers pause <title ...> {ci}: pauses a timer."));
        info(I.t("{cc}/uh timers resume <title ...> {ci}: resumes a timer."));
        info(I.t("{cc}/uh timers remove <title ...> {ci}: deletes a timer."));
        info(I.t("{cc}/uh timers set <duration> <title ...> {ci}: sets the duration of a timer."));
        info(I.t("{cc}/uh timers start <title ...> {ci}: starts a timer."));
        info(I.t("{cc}/uh timers stop <title ...> {ci}: stops a timer. The timer will be removed from the scoreboard."));
    }

    private void add() throws CommandException
    {
        if (args.length < 3) throwInvalidArgument(I.t("You must specify both a duration and a name."));

        final String name = UHUtils.getStringFromCommandArguments(args, 2);

        if (timersModule.getTimer(name) != null)
            throwInvalidArgument(I.t("{ce}A timer called {0}{ce} already exists; please choose another name.", name));

        final Timer timer = new Timer(name, getTimeDeltaParameter(1));
        timersModule.registerTimer(timer);

        success(I.t("{cs}The timer {0}{cs} (duration {1}) has been registered.", timer.getDisplayName(), args[1]));
    }

    private void display() throws CommandException
    {
        final Timer timer = getTimerParameter(1);

        timer.setDisplayed(true);
        timer.setNameDisplayed(!hasFlag("without-name"));

        reply(I.t("{cs}The timer {0}{cs} is now displayed.", timer.getDisplayName()));
    }

    private void hide() throws CommandException
    {
        final Timer timer = getTimerParameter(1);

        timer.setDisplayed(false);

        reply(I.t("{cs}The timer {0}{cs} is now hidden.", timer.getDisplayName()));
    }

    private void list()
    {
        final Collection<Timer> timers = timersModule.getTimers();

        info("");
        info(I.tn("{ci}{0} timer is registered.", "{ci}{0} timers are registered.", timers.size()));

        for (final Timer timer : timers)
        {
            final RawTextPart timerText = new RawText()
                .then(" •   ").color(timer.isRunning() ? (timer.isPaused() ? ChatColor.YELLOW : ChatColor.GREEN) : ChatColor.RED)
                .then(timer.getDisplayName())
                    .hover(
                        new RawText()
                            .then(timer.getDisplayName())
                            .then("\n")
                            .then(timer.isRunning() ? (timer.isPaused() ? I.t("Paused") : I.t("Running")) : I.t("Not started"))
                            .then("\n\n")
                            .then(timer.toString() + (timer.isRunning() ? " " + I.t("{gray}(total: {0}{gray})", Timer.formatTime(timer.getDuration())) : ""))
                            .then(timer.isSystem() ? "\n\n" + I.t("{gray}{bold}System timer") : "")
                            .then(timer.isSystem() ? "\n" + I.t("{gray}This timer is a system timer, it cannot be modified (you can still display or hide it to/from the sidebar).") : "")
                    )
                .then("   ")
                .then("[ \u272F ]")
                    .color(timer.isDisplayed() ? ChatColor.DARK_PURPLE : ChatColor.LIGHT_PURPLE)
                    .hover(
                        new RawText()
                            .then(timer.getDisplayName())
                            .then("\n").then(timer.isDisplayed() ? I.t("{white}Hide this timer from the sidebar") : I.t("{white}Show this timer in the sidebar"))
                    )
                    .command(TimersCommand.class, timer.isDisplayed() ? "hide" : "display", timer.getName(), "--from-list-command")
                .then(" ");

            if (!timer.isSystem())
            {
                if (!timer.isRunning())
                {
                    timerText
                        .then("[ \u25B6 ]")
                        .style(ChatColor.GREEN)
                        .hover(
                            new RawText()
                                .then(timer.getDisplayName())
                                .then("\n").then(I.t("{white}Start this timer"))
                        )
                        .command(TimersCommand.class, "start", timer.getName(), "--from-list-command");
                }
                else
                {
                    timerText
                        .then("[ \u2B1B ]")
                        .style(ChatColor.RED)
                        .hover(
                            new RawText()
                                .then(timer.getDisplayName())
                                .then("\n").then(I.t("{white}Stop this timer"))
                        )
                        .command(TimersCommand.class, "stop", timer.getName(), "--from-list-command");
                }

                timerText.then(" ");

                if (timer.isRunning())
                {
                    timerText
                        .then("[ \u2759 \u2759 ]")
                        .style(timer.isPaused() ? ChatColor.GOLD : ChatColor.YELLOW)
                        .hover(
                            new RawText()
                                .then(timer.getDisplayName())
                                .then("\n").then(timer.isPaused() ? I.t("{white}Resume this timer") : I.t("{white}Pause this timer"))
                        )
                        .command(TimersCommand.class, timer.isPaused() ? "resume" : "pause", timer.getName(), "--from-list-command")
                        .then(" ");
                }

                timerText
                    .then("[ × ]")
                        .style(ChatColor.DARK_RED)
                        .hover(
                            new RawText()
                                .then(timer.getDisplayName())
                                .then("\n").then(I.t("{white}Delete this timer"))
                        )
                        .command(TimersCommand.class, "remove", timer.getName(), "--from-list-command");
            }

            send(timerText.build());
        }

        if (sender instanceof Player)
        {
            send(
                /// Button in /uh timers
                new RawText(I.t("[ Create a new timer ]"))
                    .color(ChatColor.GREEN)
                    .hover(I.t("{white}Click here to create a timer\n{gray}/uh timers add mm:ss <name>"))
                    .suggest(TimersCommand.class, "add", "")

                /// Button in /uh timers
                .then(" ").then(I.t("[ Display Help ]"))
                    .color(ChatColor.YELLOW)
                    .hover(I.t("{white}Get some help about the commands\n{gray}/uh timers help"))
                    .command(TimersCommand.class, "help")
                .build()
            );
        }
    }

    private void pause() throws CommandException
    {
        final Timer timer = getTimerParameter(1);

        if (timer.isSystem()) throwNotAuthorized();

        timer.setPaused(true);

        reply(I.t("{cs}The timer {0}{cs} is now paused.", timer.getDisplayName()));
    }

    private void resume() throws CommandException
    {
        final Timer timer = getTimerParameter(1);

        if (timer.isSystem()) throwNotAuthorized();

        timer.setPaused(false);

        reply(I.t("{cs}The timer {0}{cs} was resumed.", timer.getDisplayName()));
    }

    private void remove() throws CommandException
    {
        final Timer timer = getTimerParameter(1);

        if (timer.isSystem()) throwNotAuthorized();

        timer.stop();
        timersModule.unregisterTimer(timer);

        reply(I.t("{cs}The timer {0}{cs} has been deleted.", timer.getDisplayName()));
    }

    private void set() throws CommandException
    {
        if (args.length < 3) throwInvalidArgument(I.t("You must specify both a duration and a name."));

        final Timer timer = getTimerParameter(2);
        if (timer.isSystem()) throwNotAuthorized();

        timer.setDuration(getTimeDeltaParameter(1));

        success(I.t("{cs}The duration of the timer {0}{cs} is now {1}.", timer.getDisplayName(), args[1]));
    }

    private void start() throws CommandException
    {
        final Timer timer = getTimerParameter(1);

        if (timer.isSystem()) throwNotAuthorized();

        if (!hasFlag("--hidden"))
        {
            timer.setDisplayed(true);
            timer.setNameDisplayed(!hasFlag("without-name"));
        }

        if (timer.isRunning()) timer.stop();
        timer.start();

        reply(I.t("{cs}The timer {0}{cs} was started.", timer.getDisplayName()));
    }

    private void stop() throws CommandException
    {
        final Timer timer = getTimerParameter(1);

        if (timer.isSystem()) throwNotAuthorized();

        timer.stop();

        reply(I.t("{cs}The timer {0}{cs} was stopped.", timer.getDisplayName()));
    }


    private void reply(final String reply) throws CommandException
    {
        if (hasFlag("from-list-command") && sender instanceof Player)
        {
            MessageSender.sendActionBarMessage(playerSender(), reply);
            list();
        }

        else success(reply);
    }


    private Timer getTimerParameter(int index) throws CommandException
    {
        try
        {
            final String timerName = UHUtils.getStringFromCommandArguments(args, index);
            final Timer timer = timersModule.getTimer(timerName);

            if (timer == null)
                throwInvalidArgument(I.t("{ce}This timer is not registered.", timerName));

            return timer;
        }
        catch (IllegalArgumentException e)
        {
            throwInvalidArgument(I.t("A timer name is required as argument #{0}", index + 1));
            return null;
        }
    }

    private TimeDelta getTimeDeltaParameter(final int index) throws CommandException
    {
        try
        {
            return new TimeDelta(args[index]);
        }
        catch (final ArrayIndexOutOfBoundsException e)
        {
            throwInvalidArgument(I.t("A duration is required as argument #{0}. Format: “mm”, “mm:ss” or “hh:mm:ss”.", index + 1));
            return new TimeDelta(0); // Dummy value never reached to avoid “can be null” lint warnings
        }
        catch (final IllegalArgumentException e)
        {
            throwInvalidArgument(I.t("The duration provided as argument #{0} is invalid. Format: “mm”, “mm:ss” or “hh:mm:ss”.", index + 1));
            return new TimeDelta(0); // Dummy value never reached to avoid “can be null” lint warnings
        }
    }
}
