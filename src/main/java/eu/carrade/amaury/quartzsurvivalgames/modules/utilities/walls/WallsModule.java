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

package eu.carrade.amaury.quartzsurvivalgames.modules.utilities.walls;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.border.BorderModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.utilities.walls.exceptions.CannotGenerateWallsException;
import eu.carrade.amaury.quartzsurvivalgames.modules.utilities.walls.exceptions.UnknownWallGenerator;
import eu.carrade.amaury.quartzsurvivalgames.modules.utilities.walls.generators.WallGenerator;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.World;


@ModuleInfo(
        name = "Walls Generator",
        description = "Provides a command to generate a solid wall around the arena.",
        when = ModuleLoadTime.POST_WORLD,
        category = ModuleCategory.UTILITIES,
        icon = Material.BARRIER,
        settings = Config.class
)
public class WallsModule extends QSGModule {
    private BorderModule borderModule;

    @Override
    protected void onEnable() {
        borderModule = QSG.module(BorderModule.class);
    }

    @Override
    public List<Class<? extends Command>> getCommands() {
        return Collections.singletonList(WallsCommand.class);
    }

    /**
     * Generates the walls in the given world, following the current border configuration.
     *
     * @param world The world were the walls will be built in.
     * @throws CannotGenerateWallsException If an error occurred while generating the wall.
     */
    public void generateWalls(final World world) throws CannotGenerateWallsException {
        final Integer wallHeight = Config.HEIGHT.get();

        final Material wallBlockAir = Config.BLOCK.REPLACE_AIR.get();
        final Material wallBlockSolid = Config.BLOCK.REPLACE_SOLID.get();

        if (wallBlockAir == null || !wallBlockAir.isSolid() || wallBlockSolid == null || !wallBlockSolid.isSolid()) {
            throw new CannotGenerateWallsException("Cannot generate the walls: invalid blocks set in the config");
        }

        final WallGenerator generator =
                WallGenerator.fromShape(borderModule.getMapShape(), wallBlockAir, wallBlockSolid);

        if (generator != null) {
            generator.build(world, borderModule.getCurrentBorderDiameter(), wallHeight);
        } else {
            throw new UnknownWallGenerator("Unable to load walls generator.");
        }
    }
}
