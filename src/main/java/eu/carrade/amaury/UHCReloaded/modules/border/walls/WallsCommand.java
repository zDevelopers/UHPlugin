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
package eu.carrade.amaury.UHCReloaded.modules.border.walls;

import eu.carrade.amaury.UHCReloaded.UHCReloaded;
import eu.carrade.amaury.UHCReloaded.modules.border.walls.exceptions.CannotGenerateWallsException;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name = "build-walls", aliases = {"buildwalls", "generate-walls", "generatewalls"})
public class WallsCommand extends Command
{
    @Override
    protected void run() throws CommandException
    {
        info(I.t("{cst}Generating the walls..."));

        final World world;

        if (sender instanceof Player)
        {
            world = ((Player) sender).getWorld();
        }
        else if (sender instanceof BlockCommandSender)
        {
            world = ((BlockCommandSender) sender).getBlock().getWorld();
        }
        else
        {
            world = UHCReloaded.get().getWorld(World.Environment.NORMAL);
            info(I.t("{ci}From the console, generating the walls of the default world, {0}", world.getName()));
        }

        try
        {
            UR.module(WallsModule.class).generateWalls(world);
        }
        catch (CannotGenerateWallsException e)
        {
            error(I.t("{ce}Unable to generate the wall: see logs for details. The blocks set in the config are probably invalid."));
            return;

        }
        catch (Exception e)
        {
            error(I.t("{ce}An error occurred, see console for details."));
            e.printStackTrace();
            return;
        }

        success(I.t("{cst}Generation done."));
    }
}
