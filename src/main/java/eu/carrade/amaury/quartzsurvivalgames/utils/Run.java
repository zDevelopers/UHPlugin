/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package eu.carrade.amaury.quartzsurvivalgames.utils;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimeDelta;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import fr.zcraft.quartzlib.tools.text.ActionBar;
import fr.zcraft.quartzlib.tools.text.MessageSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;
import java.util.function.Supplier;

public class Run
{
    private final static String ACTION_BAR_SEPARATOR = "   " + ChatColor.GRAY + "\u2758" + ChatColor.RESET + "   ";

    /**
     * Executes a task after a delay, displaying a countdown in the players'
     * action bar at the end.
     *
     * @param title A title displayed in the left of the action bar.
     * @param subtitle A subtitle displayed during the whole countdown in
     *                 the right of the action bar. If {@code null}, the
     *                 action bar will only display something when the
     *                 countdown will be about to end.
     * @param subtitleDuringCountdown A subtitle displayed during the last
     *                                seconds of the cooldown. Receives the
     *                                current second (will be decreasing) and
     *                                returns a subtitle.
     * @param subtitleAfter A subtitle displayed a few seconds after the task
     *                      has been run, e.g. to display a message stating that
     *                      the task is done. If {@code null}, the last countdown
     *                      message wil simply fade out.
     * @param receivers The players who will receive the countdown in their
     *                  action bar.
     * @param task The task to execute after the delay.
     * @param delay The delay.
     */
    public static void withCountdown(
            final String title,
            final String subtitle,
            final Function<Short, String> subtitleDuringCountdown,
            final String subtitleAfter,
            final Supplier<Iterable<Player>> receivers,
            final Runnable task,
            final TimeDelta delay)
    {
        withCountdown(title, subtitle, subtitleDuringCountdown, subtitleAfter, (short) 5, receivers, task, delay);
    }

    /**
     * Executes a task after a delay, displaying a countdown in the players'
     * action bar at the end.
     *
     * @param title A title displayed in the left of the action bar.
     * @param subtitle A subtitle displayed during the whole countdown in
     *                 the right of the action bar. If {@code null}, the
     *                 action bar will only display something when the
     *                 countdown will be about to end.
     * @param subtitleDuringCountdown A subtitle displayed during the last
     *                                seconds of the cooldown. Receives the
     *                                current second (will be decreasing) and
     *                                returns a subtitle.
     * @param visibleCountdownBefore When there will be less than this number of
     *                               seconds before the end of the countdown,
     *                               the subtitle will switch from the defined
     *                               static subtitle to the visible countdown.
     * @param receivers The players who will receive the countdown in their
     *                  action bar.
     * @param task The task to execute after the delay.
     * @param delay The delay.
     */
    public static void withCountdown(
            final String title,
            final String subtitle,
            final Function<Short, String> subtitleDuringCountdown,
            final String subtitleAfter,
            final short visibleCountdownBefore,
            final Supplier<Iterable<Player>> receivers,
            final Runnable task,
            final TimeDelta delay)
    {
        // The countdown is not visible at the beginning
        if (visibleCountdownBefore < delay.getSeconds())
        {
            if (subtitle != null)
            {
                final Iterable<Player> initialReceivers = receivers.get();
                initialReceivers.forEach(receiver -> ActionBar.sendPermanentMessage(receiver, title + ACTION_BAR_SEPARATOR + subtitle));

                // As the receivers list is recalculated each time (to allow for new players), this ensures
                // players who received the initial message will not have it kept during the whole game
                // if they were disconnected at the end.
                RunTask.later(() -> initialReceivers.forEach(ActionBar::removeMessage), (delay.getSeconds() - visibleCountdownBefore) * 20L);
            }

            RunTask.timer(
                    new CountdownRunnable(
                            title,
                            subtitleDuringCountdown, subtitleAfter,
                            receivers,
                            visibleCountdownBefore,
                            task
                    ),
                    (delay.getSeconds() - visibleCountdownBefore) * 20L, 20L
            );
        }

        // The countdown is immediately visible.
        else
        {
            RunTask.timer(
                    new CountdownRunnable(
                            title,
                            subtitleDuringCountdown, subtitleAfter,
                            receivers,
                            (short) delay.getSeconds(),
                            task
                    ),
                    0L, 20L
            );
        }
    }

    private static class CountdownRunnable extends BukkitRunnable
    {
        private final String title;
        private final Function<Short, String> subtitleDuringCountdown;
        private final String subtitleAfter;
        private final Supplier<Iterable<Player>> receivers;
        private short secondsLeft;
        private final Runnable task;

        private CountdownRunnable(
                final String title,
                final Function<Short, String> subtitleDuringCountdown,
                final String subtitleAfter,
                final Supplier<Iterable<Player>> receivers,
                final short secondsLeft,
                final Runnable task)
        {
            this.title = title;
            this.subtitleDuringCountdown = subtitleDuringCountdown;
            this.subtitleAfter = subtitleAfter;
            this.receivers = receivers;
            this.secondsLeft = secondsLeft;
            this.task = task;
        }

        @Override
        public void run()
        {
            if (secondsLeft == 0)
            {
                if (subtitleAfter != null)
                {
                    final Iterable<Player> finalReceivers = receivers.get();
                    finalReceivers.forEach(receiver -> ActionBar.sendPermanentMessage(receiver, title + ACTION_BAR_SEPARATOR + subtitleAfter));

                    RunTask.later(() -> finalReceivers.forEach(ActionBar::removeMessage), 60L);
                }
                else
                {
                    receivers.get()
                        .forEach(receiver -> MessageSender.sendActionBarMessage(
                            receiver,
                            title + ACTION_BAR_SEPARATOR + subtitleDuringCountdown.apply(secondsLeft)
                        ));
                }

                task.run();
                cancel();
            }
            else
            {
                receivers.get()
                    .forEach(receiver -> MessageSender.sendActionBarMessage(
                        receiver,
                        title + ACTION_BAR_SEPARATOR + subtitleDuringCountdown.apply(secondsLeft)
                    ));

                secondsLeft--;
            }
        }
    }
}
