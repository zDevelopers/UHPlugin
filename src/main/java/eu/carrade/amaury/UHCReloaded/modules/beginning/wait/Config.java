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
package eu.carrade.amaury.UHCReloaded.modules.beginning.wait;

import fr.zcraft.zlib.components.configuration.ConfigurationInstance;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationSection;
import org.bukkit.Material;

import java.io.File;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.zlib.components.configuration.ConfigurationItem.section;

public class Config extends ConfigurationInstance
{
    public Config(File file)
    {
        super(file);
    }

    static public final ConfigurationItem<Boolean> TELEPORT_TO_SPAWN_IF_NOT_STARTED = item("teleport-to-spawn-if-not-started", true);

    public static final InventorySection INVENTORY = section("inventory", InventorySection.class);

    static public class InventorySection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> CLEAR = item("clear", true);
        public final ConfigurationItem<Boolean> PREVENT_USAGE = item("prevent-usage", true);
        public final ConfigurationItem<Boolean> ALLOW_FOR_BUILDERS = item("allow-for-builders", true);
    }

    public static final TeamSelectorSection TEAM_SELECTOR = section("teams-selector", TeamSelectorSection.class);

    static public class TeamSelectorSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
        public final ConfigurationItem<Material> ITEM = item("item", Material.NETHER_STAR);
    }

    public static final ConfigAccessorSection CONFIG_ACCESSOR = section("config-accessor", ConfigAccessorSection.class);

    static public class ConfigAccessorSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
        public final ConfigurationItem<Material> ITEM = item("item", Material.REDSTONE_COMPARATOR);
    }

    public static final ConfigurationItem<Boolean> TEAM_IN_ACTION_BAR = item("team-in-action-bar", true);

    public static final ConfigurationItem<Boolean> ENABLE_PVP = item("enable-pvp", false);
}
