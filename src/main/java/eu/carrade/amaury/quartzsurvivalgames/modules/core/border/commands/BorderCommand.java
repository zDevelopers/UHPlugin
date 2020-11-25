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
package eu.carrade.amaury.quartzsurvivalgames.modules.core.border.commands;

import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.BorderModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.MapShape;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.timers.TimeDelta;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import org.bukkit.Bukkit;


@CommandInfo (name = "border", usageParameters = "[new diameter] [duration]", aliases = "b")
public class BorderCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        final BorderModule border = QSG.module(BorderModule.class);

        // No arguments: displays current size
        if (args.length == 0)
        {
            if (border.getMapShape() == MapShape.CIRCULAR)
            {
                sender.sendMessage(I.tn("{ci}The current diameter of the map is {0} block.", "{ci}The current diameter of the map is {0} blocks.", border.getCurrentBorderDiameter()));
            }
            else
            {
                sender.sendMessage(I.t("{ci}The current map size is {0}×{0}.", border.getCurrentBorderDiameter()));
            }
        }

        else
        {
            // /uh border <radius>
            if (args.length == 1)
            {
                try
                {
                    final int newDiameter = Integer.valueOf(args[0]);

                    // Some players are outside
                    if (border.getPlayersOutside(newDiameter).size() != 0)
                    {
                        sender.sendMessage(I.t("{ce}Some players are outside the future border, so this operation was cancelled."));
                        sender.sendMessage(I.t("{ci}Use {cc}/uh border set {0} force{ci} to resize the border regardless to this point.", args[0]));

                      if (!QSG.module(BorderModule.class).getWorldBorderDependency().isEnabled())
                      {
                          sender.sendMessage(I.t("{ce}WARNING: {ci}because WorldBorder is not installed, players out of the border will not be teleported!"));
                      }

                        border.sendCheckMessage(sender, newDiameter);
                    }
                    else
                    {
                        border.setCurrentBorderDiameter(newDiameter);

                        if (border.getMapShape() == MapShape.CIRCULAR)
                        {
                            Bukkit.getServer().broadcastMessage(I.tn("{lightpurple}The diameter of the map is now {0} block.", "{lightpurple}The diameter of the map is now {0} blocks.", newDiameter));
                        }
                        else
                        {
                            Bukkit.getServer().broadcastMessage(I.t("{lightpurple}The size of the map is now {0}×{0}.", newDiameter));
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    error(I.t("{ce}“{0}” is not a number...", args[0]));
                }
            }

            // /uh border <radius> force
            else if (args.length == 2 && args[1].equalsIgnoreCase("force"))
            {
                try
                {
                    final Integer newDiameter = Integer.valueOf(args[0]);

                    border.setCurrentBorderDiameter(newDiameter);

                    if (border.getMapShape() == MapShape.CIRCULAR)
                    {
                        Bukkit.getServer().broadcastMessage(I.tn("{lightpurple}The diameter of the map is now {0} block.", "{lightpurple}The diameter of the map is now {0} blocks.", newDiameter));
                    }
                    else
                    {
                        Bukkit.getServer().broadcastMessage(I.t("{lightpurple}The size of the map is now {0}×{0}.", newDiameter));
                    }
                }
                catch (NumberFormatException e)
                {
                    error(I.t("{ce}“{0}” is not a number...", args[0]));
                }
            }

            // /uh border <radius> <duration>
            else if (args.length == 2)
            {
                if (!border.getBorderProxy().supportsProgressiveResize())
                {
                    error(I.t("The border motor ({0}) does not supports progressive resizes.", border.getBorderProxy().getClass().getSimpleName()));
                }

                final Integer newDiameter;
                final TimeDelta delta;

                try
                {
                    newDiameter = Integer.valueOf(args[0]);
                }
                catch (NumberFormatException e)
                {
                    error(I.t("{ce}“{0}” is not a number...", args[0]));
                    return;
                }

                try
                {
                    delta = new TimeDelta(args[1]);
                }
                catch (IllegalArgumentException e)
                {
                    error(I.t("{ce}“{0}” is not a valid time delta... Accepted formats are mm, mm:ss or hh:mm:ss.", args[1]));
                    return;
                }

                border.getBorderProxy().setDiameter(newDiameter, delta);

                if (border.getMapShape() == MapShape.CIRCULAR)
                {
                    Bukkit.getServer().broadcastMessage(I.tn("{lightpurple}The diameter of the map will be set to {0} block over {1}.", "{lightpurple}The diameter of the map will be set to {0} blocks over {1}.", newDiameter, newDiameter, delta));
                }
                else
                {
                    Bukkit.getServer().broadcastMessage(I.t("{lightpurple}The size of the map will be set to {0}×{0} over {1}.", newDiameter, delta));
                }
            }
        }
    }
}
