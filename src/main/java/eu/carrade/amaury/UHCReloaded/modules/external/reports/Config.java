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
package eu.carrade.amaury.UHCReloaded.modules.external.reports;

import fr.zcraft.zlib.components.configuration.ConfigurationInstance;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationList;
import fr.zcraft.zlib.components.configuration.ConfigurationSection;
import org.bukkit.Material;
import org.bukkit.Statistic;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.zlib.components.configuration.ConfigurationItem.section;

public class Config extends ConfigurationInstance
{
    public Config(File file)
    {
        super(file);
    }

    public static final ConfigurationItem<String> REPORTS_API_BASE_URL = item("reports-api-base-url", "");

    public static final ConfigurationItem<Boolean> PUBLISH_REPORT = item("publish-report", true);
    public static final ConfigurationItem<ReportBroadcastedTo> BROADCAST_REPORT_TO = item("broadcast-report-to", ReportBroadcastedTo.ALL);

    static public final ConfigurationItem<Boolean> DATE = item("date", true);
    static public final ConfigurationItem<Boolean> PLAYERS_COUNT = item("players-count", true);
    static public final ConfigurationItem<Boolean> WINNERS = item("winners", true);

    static public final SummarySection SUMMARY = section("summary", SummarySection.class);
    static public class SummarySection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
        public final ConfigurationItem<Boolean> HISTORY = item("history", true);
        public final ConfigurationItem<Boolean> PLAYERS = item("players", true);
        public final ConfigurationItem<Boolean> TEAMS = item("teams", true);
    }


    static public final DamagesSection DAMAGES = section("damages", DamagesSection.class);
    static public class DamagesSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
        public final ConfigurationItem<Boolean> DAMAGES_PER_PLAYERS = item("damages-per-players", true);
        public final ConfigurationItem<Boolean> DAMAGES_PER_TEAMS = item("damages-per-teams", true);
        public final ConfigurationItem<Boolean> DAMAGES_FROM_ENVIRONMENT = item("damages-from-environment", true);
        public final ConfigurationItem<Boolean> DISPLAY_KILLER = item("display-killer", true);
    }


    static public final PlayersSection PLAYERS = section("players", PlayersSection.class);
    static public class PlayersSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
        public final ConfigurationItem<Boolean> PLAY_TIME = item("play-time", true);

        public final ConfigurationItem<Boolean> GLOBAL_STATISTICS = item("global-statistics", true);
        public final ConfigurationList<Statistic> STATISTICS_WHITELIST = list("statistics-whitelist", Statistic.class);
        public final ConfigurationList<Statistic> STATISTICS_HIGHLIGHT = list("statistics-highlight", Statistic.class);

        public final ConfigurationItem<Boolean> USED = item("used", false);
        public final ConfigurationList<Material> USED_WHITELIST = list("used-whitelist", Material.class);
        public final ConfigurationList<Material> USED_HIGHLIGHT = list("used-highlight", Material.class);

        public final ConfigurationItem<Boolean> MINED = item("mined", true);
        public final ConfigurationList<Material> MINED_WHITELIST = list("mined-whitelist", Material.class);
        public final ConfigurationList<Material> MINED_HIGHLIGHT = list("mined-highlight", Material.class);

        public final ConfigurationItem<Boolean> PICKED_UP = item("picked-up", true);
        public final ConfigurationList<Material> PICKED_UP_WHITELIST = list("picked-up-whitelist", Material.class);
        public final ConfigurationList<Material> PICKED_UP_HIGHLIGHT = list("picked-up-highlight", Material.class);
    }

    static public List<Statistic> defaultStatsHighlight()
    {
        return Arrays.asList(
                Statistic.DAMAGE_DEALT,
                Statistic.CRAFT_ITEM,
                Statistic.ITEM_ENCHANTED,
                Statistic.BREWINGSTAND_INTERACTION,
                Statistic.SPRINT_ONE_CM
        );
    }

    static public List<Material> defaultUsedHighlight()
    {
        return Arrays.asList(
                Material.DIAMOND_SWORD, Material.DIAMOND_AXE, Material.DIAMOND_PICKAXE, Material.DIAMOND_HOE,
                Material.IRON_SWORD, Material.IRON_AXE, Material.IRON_PICKAXE, Material.IRON_HOE,
                Material.CAKE, Material.SADDLE, Material.GOLDEN_APPLE, Material.POTION,
                Material.GOLD_RECORD, Material.GREEN_RECORD, Material.RECORD_3, Material.RECORD_4,
                Material.RECORD_5, Material.RECORD_6, Material.RECORD_7, Material.RECORD_8,
                Material.RECORD_9, Material.RECORD_10, Material.RECORD_11, Material.RECORD_12
        );
    }

    static public List<Material> defaultMinedHighlight()
    {
        return Arrays.asList(
                Material.DIAMOND, Material.DIAMOND_ORE,
                Material.GOLD_INGOT, Material.GOLD_ORE,
                Material.IRON_INGOT, Material.IRON_ORE,
                Material.EMERALD, Material.EMERALD_ORE,
                Material.OBSIDIAN, Material.NETHER_WARTS,
                Material.MOB_SPAWNER, Material.STONE
        );
    }

    static public List<Material> defaultPickedUpHighlight()
    {
        return Arrays.asList(
                Material.APPLE,
                Material.BOW, Material.ENDER_PEARL, Material.GOLD_INGOT,
                Material.SKULL_ITEM
        );
    }

    public enum ReportBroadcastedTo
    {
        ALL,
        ADMINISTRATORS,
        CONSOLE
    }
}
