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

package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.warning;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.events.BorderChangedEvent;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimeDelta;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.Timer;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimersModule;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

@ModuleInfo(
        name = "Border Warning",
        description = "Warns players about the future border size.",
        when = ModuleLoadTime.ON_GAME_START,
        category = ModuleCategory.UTILITIES,
        icon = Material.NETHER_BRICK_FENCE,
        settings = Config.class
)
public class WarningModule extends QSGModule {

    private Integer warningSize = 0;
    private BukkitRunnable warningTask = null;

    private final Boolean warningFinalTimeEnabled = false;
    private Timer warningTimer = null;
    private String warningTimerName = null;
    private CommandSender warningSender = null;


    @Override
    protected void onEnable() {
        /// The name of the warning timer displaying the time left before the next border
        warningTimerName = I.t("Border shrinking");
    }

    @Override
    protected void onDisable() {
        cancelWarning();
    }

    @Override
    public List<Class<? extends Command>> getCommands() {
        return Collections.singletonList(WarningCommand.class);
    }

    /**
     * Returns the size of the future border, used in the warning messages sent to the
     * players out of this future border.
     *
     * @return the future border diameter.
     */
    public int getWarningSize() {
        return this.warningSize;
    }

    /**
     * Sets the size of the future border, used in the warning messages sent to the
     * players out of this future border.
     * <p>
     * This also starts the display of the warning messages, every 90 seconds by default
     * (configurable, see config.yml, map.border.warningInterval).
     *
     * @param diameter The diameter of the future border.
     */
    public void setWarningSize(final int diameter) {
        setWarningSize(diameter, null, null);
    }

    /**
     * @return true if there is currently a warning with a time left displayed.
     */
    public boolean getWarningFinalTimeEnabled() {
        return this.warningFinalTimeEnabled;
    }

    /**
     * @return the sender of the last warning configured.
     */
    public CommandSender getWarningSender() {
        return this.warningSender;
    }

    /**
     * Sets the size of the future border, used in the warning messages sent to the
     * players out of this future border.
     * <p>
     * This also starts the display of the warning messages, every 90 seconds by default
     * (configurable, see config.yml, map.border.warningInterval).
     * <p>
     * If timeLeft is not null, the time available for the players to go inside the future
     * border is displayed in the warning message.
     *
     * @param diameter The future diameter.
     * @param timeLeft The time available for the players to go inside the future border.
     * @param sender   The user who requested this change.
     */
    public void setWarningSize(final int diameter, final TimeDelta timeLeft, final CommandSender sender) {
        cancelWarning();

        this.warningSize = diameter;

        if (timeLeft != null) {
            warningTimer = new Timer(this.warningTimerName);
            warningTimer.setDuration((int) timeLeft.getSeconds());

            QSG.module(TimersModule.class).registerTimer(warningTimer);

            warningTimer.start();
        }

        if (sender != null) {
            this.warningSender = sender;
        }

        RunTask.timer(
                warningTask = new BorderWarningTask(),
                20L,
                20L * Config.WARNING_INTERVAL.get().getSeconds()
        );
    }

    /**
     * Stops the display of the warning messages.
     */
    public void cancelWarning() {
        if (warningTask != null) {
            try {
                warningTask.cancel();
            }
            catch (IllegalStateException ignored) {
            }
        }

        if (warningTimer != null) {
            warningTimer.stop();
            QSG.module(TimersModule.class).unregisterTimer(warningTimer);
        }
    }


    @EventHandler
    public void onBorderChanged(final BorderChangedEvent ev) {
        cancelWarning();
    }
}
