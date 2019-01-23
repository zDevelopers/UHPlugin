package eu.carrade.amaury.UHCReloaded;

import fr.zcraft.zlib.components.configuration.*;
import org.bukkit.ChatColor;

import java.util.Locale;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.*;


public class UHConfig extends Configuration
{
    static public final ConfigurationItem<Locale> LANG = item("lang", Locale.class);

    static public final ConfigurationItem<String> TITLE = item("title", ChatColor.GREEN + "" + ChatColor.BOLD + "UHC Reloaded");

    static public final ConfigurationItem<Boolean> BUILT_IN_MODULES = item("built-in-modules", true);
    static public final ConfigurationMap<String, Boolean> MODULES = map("modules", String.class, Boolean.class);

    static public final WorldsSection WORLDS = section("worlds", WorldsSection.class);
    static public class WorldsSection extends ConfigurationSection
    {

        public final ConfigurationItem<String> OVERWORLD = item("overworld", "world");
        public final ConfigurationItem<String> NETHER = item("nether", "world_nether");
        public final ConfigurationItem<String> THE_END = item("the-end", "world_the_end");

    }
}
