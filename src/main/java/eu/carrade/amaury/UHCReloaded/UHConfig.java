package eu.carrade.amaury.UHCReloaded;

import eu.carrade.amaury.UHCReloaded.borders.MapShape;
import eu.carrade.amaury.UHCReloaded.teams.TeamManager;
import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;
import fr.zcraft.zlib.components.configuration.ConfigurationList;
import fr.zcraft.zlib.components.configuration.ConfigurationSection;
import fr.zcraft.zlib.components.configuration.ConfigurationValueHandlers;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.zlib.components.configuration.ConfigurationItem.list;
import static fr.zcraft.zlib.components.configuration.ConfigurationItem.section;


public class UHConfig extends Configuration
{
    static public final ConfigurationItem<String> LANG = item("lang", "");
    static public final ConfigurationItem<Boolean> METRICS = item("metrics", true);

    static public final EpisodesSection EPISODES = section("episodes", EpisodesSection.class);

    static public class EpisodesSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
        public final ConfigurationItem<String> LENGTH = item("length", "20:00");
        public final ConfigurationItem<Boolean> TITLE = item("title", true);
    }

    static public final MapSection MAP = section("map", MapSection.class);

    static public class MapSection extends ConfigurationSection
    {
        public final ConfigurationItem<Integer> SIZE = item("size", 2000);
        public final ConfigurationItem<MapShape> SHAPE = item("shape", MapShape.SQUARED);

        public final WallSection WALL = section("wall", WallSection.class);

        static public class WallSection extends ConfigurationSection
        {
            public final ConfigurationItem<Integer> HEIGHT = item("height", 128);

            public final BlockSection BLOCK = section("block", BlockSection.class);

            static public class BlockSection extends ConfigurationSection
            {
                public final ConfigurationItem<Material> REPLACE_AIR = item("replaceAir", Material.GLASS);
                public final ConfigurationItem<Material> REPLACE_SOLID = item("replaceSolid", Material.BEDROCK);
            }
        }

        public final BorderSection BORDER = section("border", BorderSection.class);

        static public class BorderSection extends ConfigurationSection
        {
            public final ConfigurationItem<String> MOTOR = item("motor", "vanilla");
            public final ConfigurationItem<Double> DAMAGES_BUFFER = item("damagesBuffer", 5d);
            public final ConfigurationItem<Double> DAMAGES_AMOUNT = item("damagesAmount", 0.2);
            public final ConfigurationItem<Integer> WARNING_DISTANCE = item("warningDistance", 5);

            public final ShrinkingSection SHRINKING = section("shrinking", ShrinkingSection.class);

            static public class ShrinkingSection extends ConfigurationSection
            {
                public final ConfigurationItem<Boolean> ENABLED = item("enabled", false);
                public final ConfigurationItem<String> STARTS_AFTER = item("startsAfter", "30:00");
                public final ConfigurationItem<String> SHRINKS_DURING = item("shrinksDuring", "2:00:00");
                public final ConfigurationItem<Integer> DIAMETER_AFTER_SHRINK = item("diameterAfterShrink", 200);
            }

            public final ConfigurationItem<Integer> WARNING_INTERVAL = item("warningInterval", 90);
        }

        public final SpawnPointsSection SPAWN_POINTS = section("spawnPoints", SpawnPointsSection.class);

        static public class SpawnPointsSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DONT_GENERATE_ABOVE_WATER = item("dontGenerateAboveWater", true);
        }
    }

    static public final DaylightCycleSection DAYLIGHT_CYCLE = section("daylightCycle", DaylightCycleSection.class);

    static public class DaylightCycleSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> DO = item("do", false);
        public final ConfigurationItem<Long> TIME = item("time", 6000l);
    }

    static public final ScoreboardSection SCOREBOARD = section("scoreboard", ScoreboardSection.class);

    static public class ScoreboardSection extends ConfigurationSection
    {
        public final ConfigurationItem<String> TITLE = item("title", "Kill the Patrick");
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
        public final ConfigurationItem<Boolean> EPISODE = item("episode", true);
        public final ConfigurationItem<Boolean> PLAYERS = item("players", true);
        public final ConfigurationItem<Boolean> TEAMS = item("teams", true);

        public final OwnTeamSection OWN_TEAM = section("ownTeam", OwnTeamSection.class);

        static public class OwnTeamSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);

            public final TitleSection TITLE = section("title", TitleSection.class);

            static public class TitleSection extends ConfigurationSection
            {
                public final ConfigurationItem<String> COLOR = item("color", "");
                public final ConfigurationItem<Boolean> USE_TEAM_NAME = item("useTeamName", false);
            }

            public final ContentSection CONTENT = section("content", ContentSection.class);

            static public class ContentSection extends ConfigurationSection
            {
                public final ConfigurationItem<Boolean> DISPLAY_HEARTS = item("displayHearts", true);
                public final ConfigurationItem<Boolean> COLOR_NAME = item("colorName", false);
                public final ConfigurationItem<Boolean> STRIKE_DEAD_PLAYERS = item("strikeDeadPlayers", false);

                public final LoginStateSection LOGIN_STATE = section("loginState", LoginStateSection.class);

                static public class LoginStateSection extends ConfigurationSection
                {
                    public final ConfigurationItem<Boolean> ITALIC = item("italic", true);
                    public final ConfigurationItem<String> SUFFIX = item("suffix", "➥");
                }

                public final DisplayMetPlayersOnlySection DISPLAY_MET_PLAYERS_ONLY = section("displayMetPlayersOnly", DisplayMetPlayersOnlySection.class);

                static public class DisplayMetPlayersOnlySection extends ConfigurationSection
                {
                    public final ConfigurationItem<Boolean> ENABLED = item("enabled", false);
                    public final ConfigurationItem<Double> DISPLAYED_WHEN_CLOSER_THAN = item("displayedWhenCloserThan", 10d);
                }
            }
        }

        public final BorderSection BORDER = section("border", BorderSection.class);

        static public class BorderSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DISPLAYED = item("displayed", true);
            public final ConfigurationItem<Boolean> DISPLAY_DIAMETER = item("displayDiameter", false);
        }

        public final ConfigurationItem<Boolean> KILLS = item("kills", true);
        public final ConfigurationItem<Boolean> TIMER = item("timer", true);
        public final ConfigurationItem<Boolean> FREEZE_STATUS = item("freezeStatus", true);
        public final ConfigurationItem<Boolean> HEALTH = item("health", true);
    }

    static public final PlayersListSection PLAYERS_LIST = section("playersList", PlayersListSection.class);

    static public class PlayersListSection extends ConfigurationSection
    {
        public final WaitingTimeSection WAITING_TIME = section("waitingTime", WaitingTimeSection.class);

        static public class WaitingTimeSection extends ConfigurationSection
        {
            public final ConfigurationItem<String> HEADER = item("header", "{title}");
            public final ConfigurationItem<String> FOOTER = item("footer", "");
        }

        public final InGameTimeSection IN_GAME_TIME = section("inGameTime", InGameTimeSection.class);

        static public class InGameTimeSection extends ConfigurationSection
        {
            public final ConfigurationItem<String> HEADER = item("header", "{title}");
            public final ConfigurationItem<String> FOOTER = item("footer", "§a{episodeText} §7- §a{playersText} §7- §a{teamsText}");
        }
    }

    static public final MotdSection MOTD = section("motd", MotdSection.class);

    static public class MotdSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> ENABLED = item("enabled", false);
        public final ConfigurationItem<Boolean> DISPLAY_MATCH_NAME = item("displayMatchName", true);
        public final ConfigurationItem<String> MATCH_NAME_PREFIX = item("matchNamePrefix", "");
    }

    static public final ConfigurationItem<Boolean> TELEPORT_TO_SPAWN_IF_NOT_STARTED = item("teleportToSpawnIfNotStarted", true);

    static public final AchievementsSection ACHIEVEMENTS = section("achievements", AchievementsSection.class);

    static public class AchievementsSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> RESET_ACHIEVEMENTS_AT_STARTUP = item("resetAchievementsAtStartup", true);
        public final ConfigurationItem<Boolean> DISABLE_ACHIEVEMENTS_BEFORE_START = item("disableAchievementsBeforeStart", true);
    }

    static public final StatisticsSection STATISTICS = section("statistics", StatisticsSection.class);

    static public class StatisticsSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> DISABLE_STATISTICS_BEFORE_START = item("disableStatisticsBeforeStart", true);
    }

    static public final BeforeStartSection BEFORE_START = section("before-start", BeforeStartSection.class);

    static public class BeforeStartSection extends ConfigurationSection
    {
        public final InventorySection INVENTORY = section("inventory", InventorySection.class);

        static public class InventorySection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> CLEAR = item("clear", true);
            public final ConfigurationItem<Boolean> PREVENT_USAGE = item("preventUsage", true);
            public final ConfigurationItem<Boolean> ALLOW_FOR_BUILDERS = item("allowForBuilders", true);
        }

        public final TeamSelectorSection TEAM_SELECTOR = section("teamSelector", TeamSelectorSection.class);

        static public class TeamSelectorSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
            public final ConfigurationItem<Material> ITEM = item("item", Material.NETHER_STAR);
        }

        public final ConfigurationItem<Boolean> TEAM_IN_ACTION_BAR = item("teamInActionBar", true);
    }

    static public final StartSection START = section("start", StartSection.class);

    static public class StartSection extends ConfigurationSection
    {
        public final SlowSection SLOW = section("slow", SlowSection.class);

        static public class SlowSection extends ConfigurationSection
        {
            public final ConfigurationItem<Long> DELAY_BETWEEN_TP = item("delayBetweenTP", 3l);
            public final ConfigurationItem<Boolean> BROADCAST_PROGRESS = item("broadcastProgress", true);
        }

        public final SoundSection SOUND = section("sound", SoundSection.class);

        public final ConfigurationItem<Boolean> DISPLAY_TITLE = item("displayTitle", true);
        public final ConfigurationItem<String> GRACE_PERIOD = item("gracePeriod", "00:30");
        public final ConfigurationItem<String> PEACE_PERIOD = item("peacePeriod", "00");
        public final ConfigurationItem<String> SURFACE_MOBS_FREE_PERIOD = item("surfaceMobsFreePeriod", "15:00");
    }

    static public final DeathSection DEATH = section("death", DeathSection.class);

    static public class DeathSection extends ConfigurationSection
    {
        public final MessagesSection MESSAGES = section("messages", MessagesSection.class);

        static public class MessagesSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> NOTIFY_IF_TEAM_HAS_FALLEN = item("notifyIfTeamHasFallen", true);
            public final ConfigurationItem<String> DEATH_MESSAGES_FORMAT = item("deathMessagesFormat", "§6");
            public final ConfigurationItem<String> TEAM_DEATH_MESSAGES_FORMAT = item("teamDeathMessagesFormat", "§6");
        }

        public final KickSection KICK = section("kick", KickSection.class);

        static public class KickSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DO = item("do", false);
            public final ConfigurationItem<Integer> TIME = item("time", 30);
            public final ConfigurationItem<Boolean> ALLOW_RECONNECT = item("allow-reconnect", true);
        }

        public final HeadSection HEAD = section("head", HeadSection.class);

        static public class HeadSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DROP = item("drop", true);
            public final ConfigurationItem<Boolean> PVP_ONLY = item("pvpOnly", false);
        }

        public final GiveXpToKillerSection GIVE_XP_TO_KILLER = section("give-xp-to-killer", GiveXpToKillerSection.class);

        static public class GiveXpToKillerSection extends ConfigurationSection
        {
            public final ConfigurationItem<Integer> LEVELS = item("levels", 2);
            public final ConfigurationItem<Boolean> ONLY_OTHER_TEAM = item("onlyOtherTeam", true);
        }

        public final AnnouncementsSection ANNOUNCEMENTS = section("announcements", AnnouncementsSection.class);

        static public class AnnouncementsSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> LIGHTNING_STRIKE = item("lightning-strike", false);
            public final SoundSection SOUND = section("sound", SoundSection.class);
        }
    }

    static public final ConfigurationItem<Boolean> COLORIZE_CHAT = item("colorizeChat", true);

    static public final GameplayChangesSection GAMEPLAY_CHANGES = section("gameplay-changes", GameplayChangesSection.class);

    static public class GameplayChangesSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> NATURAL_REGENERATION = item("naturalRegeneration", false);
        public final ConfigurationItem<Boolean> WEATHER = item("weather", true);
        public final ConfigurationItem<Boolean> REPLACE_GHAST_TEARS_WITH_GOLD = item("replaceGhastTearsWithGold", true);
        public final ConfigurationItem<Boolean> CRAFT_GOLDEN_MELON_WITH_GOLD_BLOCK = item("craftGoldenMelonWithGoldBlock", true);

        public final CraftGoldenAppleFromHeadSection CRAFT_GOLDEN_APPLE_FROM_HEAD = section("craftGoldenAppleFromHead", CraftGoldenAppleFromHeadSection.class);

        static public class CraftGoldenAppleFromHeadSection extends ConfigurationSection
        {
            public final FromHumanSection FROM_HUMAN = section("fromHuman", FromHumanSection.class);

            static public class FromHumanSection extends ConfigurationSection
            {
                public final ConfigurationItem<Boolean> DO = item("do", true);
                public final ConfigurationItem<Integer> NUMBER_CRAFTED = item("numberCrafted", 2);
                public final ConfigurationItem<Boolean> ADD_LORE = item("addLore", true);
                public final ConfigurationItem<Boolean> CRAFT_NOTCH_APPLE = item("craftNotchApple", false);
            }


            public final FromWitherSection FROM_WITHER = section("fromWither", FromWitherSection.class);

            static public class FromWitherSection extends ConfigurationSection
            {
                public final ConfigurationItem<Boolean> DO = item("do", true);
                public final ConfigurationItem<Integer> NUMBER_CRAFTED = item("numberCrafted", 1);
                public final ConfigurationItem<Boolean> ADD_LORE = item("addLore", true);
                public final ConfigurationItem<Boolean> CRAFT_NOTCH_APPLE = item("craftNotchApple", false);
            }
        }

        public final GoldenAppleSection GOLDEN_APPLE = section("goldenApple", GoldenAppleSection.class);

        static public class GoldenAppleSection extends ConfigurationSection
        {
            public final RegenerationSection REGENERATION = section("regeneration", RegenerationSection.class);

            static public class RegenerationSection extends ConfigurationSection
            {
                public final ConfigurationItem<Integer> NORMAL = item("normal", 4);
                public final ConfigurationItem<Integer> NOTCH = item("notch", 180);
                public final ConfigurationItem<Integer> FROM_NORMAL_HEAD = item("fromNormalHead", 4);
                public final ConfigurationItem<Integer> FROM_NOTCH_HEAD = item("fromNotchHead", 180);
            }

            public final ConfigurationItem<Boolean> DISABLE_NOTCH_APPLES = item("disableNotchApples", false);
        }

        public final ConfigurationItem<Boolean> DISABLE_ENDERPEARLS_DAMAGES = item("disableEnderpearlsDamages", true);
        public final ConfigurationItem<Boolean> DISABLE_LEVEL_II_POTIONS = item("disableLevelIIPotions", false);

        public final WitchSection WITCH = section("witch", WitchSection.class);

        static public class WitchSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DISABLE_NATURAL_SPAWN = item("disableNaturalSpawn", false);
            public final ConfigurationItem<Boolean> DISABLE_LIGHTNING_SPAWN = item("disableLightningSpawn", false);
        }

        public final RabbitSection RABBIT = section("rabbit", RabbitSection.class);

        static public class RabbitSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> KILLER_RABBIT_SPAWN = item("killerRabbitSpawn", true);
            public final ConfigurationItem<Double> KILLER_RABBIT_SPAWN_PROBABILITY = item("killerRabbitSpawnProbability", 0.05);
            public final ConfigurationItem<String> KILLER_RABBIT_NAME = item("killerRabbitName", "The Killer Rabbit of Caerbannog");
        }

        public final CompassSection COMPASS = section("compass", CompassSection.class);

        static public class CompassSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
            public final ConfigurationItem<String> RECIPE = item("recipe", "medium");
        }
    }

    static public final TeamsOptionsSection TEAMS_OPTIONS = section("teams-options", TeamsOptionsSection.class);

    static public class TeamsOptionsSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> CAN_SEE_FRIENDLY_INVISIBLES = item("canSeeFriendlyInvisibles", true);
        public final ConfigurationItem<Boolean> ALLOW_FRIENDLY_FIRE = item("allowFriendlyFire", true);
        public final ConfigurationItem<Integer> MAX_PLAYERS_PER_TEAM = item("maxPlayersPerTeam", 0);
        public final ConfigurationItem<Boolean> RANDOM_COLORS = item("randomColors", true);

        public final BannerSection BANNER = section("banner", BannerSection.class);

        static public class BannerSection extends ConfigurationSection
        {
            public final ShapeSection SHAPE = section("shape", ShapeSection.class);

            static public class ShapeSection extends ConfigurationSection
            {
                public final ConfigurationItem<Boolean> WRITE_LETTER = item("writeLetter", true);
                public final ConfigurationItem<Boolean> ADD_BORDER = item("addBorder", true);
            }

            public final GiveSection GIVE = section("give", GiveSection.class);

            static public class GiveSection extends ConfigurationSection
            {
                public final ConfigurationItem<Boolean> PLACE_ON_SPAWN = item("placeOnSpawn", true);
                public final ConfigurationItem<Boolean> GIVE_IN_HOTBAR = item("giveInHotbar", false);
                public final ConfigurationItem<Boolean> GIVE_IN_HEAD = item("giveInHead", false);
            }

            public final ShieldSection SHIELDS = section("shields", ShieldSection.class);

            static public class ShieldSection extends ConfigurationSection
            {
                public final ConfigurationItem<Boolean> ADD_ON_SHIELDS = item("addOnShields", true);
            }
        }

        public final GuiSection GUI = section("gui", GuiSection.class);

        static public class GuiSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DISPLAY_PLAYERS_IN_TEAMS = item("displayPlayersInTeams", true);
            public final ConfigurationItem<Boolean> AUTO_DISPLAY = item("autoDisplay", true);
            public final ConfigurationItem<Integer> DELAY = item("delay", 4);
        }

        public final ChestGuiSection CHEST_GUI = section("chestGui", ChestGuiSection.class);

        static public class ChestGuiSection extends ConfigurationSection
        {
            public final DisplaySection DISPLAY = section("display", DisplaySection.class);

            static public class DisplaySection extends ConfigurationSection
            {
                public final ConfigurationItem<String> TEAM_ITEM = item("teamItem", "banner");
                public final ConfigurationItem<Boolean> GLOW_ON_SELECTED_TEAM = item("glowOnSelectedTeam", true);
            }
        }

        public final TeamChatSection TEAM_CHAT = section("teamChat", TeamChatSection.class);

        static public class TeamChatSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DISABLE_LOCK_ON_DEATH = item("disableLockOnDeath", true);
            public final ConfigurationItem<Boolean> LOG = item("log", false);
        }
    }

    static public final HardcoreHeartsSection HARDCORE_HEARTS = section("hardcore-hearts", HardcoreHeartsSection.class);

    static public class HardcoreHeartsSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> DISPLAY = item("display", true);
        public final ConfigurationItem<Boolean> RESPAWN_MESSAGE = item("respawnMessage", false);
    }

    static public final AutoRespawnSection AUTO_RESPAWN = section("auto-respawn", AutoRespawnSection.class);

    static public class AutoRespawnSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> DO = item("do", true);
        public final ConfigurationItem<Integer> DELAY = item("delay", 6);
    }

    static public final FinishSection FINISH = section("finish", FinishSection.class);

    static public class FinishSection extends ConfigurationSection
    {
        public final AutoSection AUTO = section("auto", AutoSection.class);

        static public class AutoSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> DO = item("do", true);
            public final ConfigurationItem<Integer> TIME_AFTER_LAST_DEATH = item("timeAfterLastDeath", 3);
        }

        public final ConfigurationItem<Boolean> MESSAGE = item("message", true);
        public final ConfigurationItem<Boolean> TITLE = item("title", true);

        public final FireworksSection FIREWORKS = section("fireworks", FireworksSection.class);

        static public class FireworksSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> ENABLED = item("enabled", true);
            public final ConfigurationItem<Long> DURATION = item("duration", 10l);
            public final ConfigurationItem<Double> AREA_SIZE = item("areaSize", 6d);
        }
    }

    static public final DynmapSection DYNMAP = section("dynmap", DynmapSection.class);

    static public class DynmapSection extends ConfigurationSection
    {
        public final ConfigurationItem<Boolean> SHOW_SPAWN_LOCATIONS = item("showSpawnLocations", true);
        public final ConfigurationItem<Boolean> SHOW_DEATH_LOCATIONS = item("showDeathLocations", true);
    }

    static public final ConfigurationItem<Boolean> SPECTATOR_MODE_WHEN_NEW_PLAYER_JOIN_AFTER_START = item("spectatorModeWhenNewPlayerJoinAfterStart", true);

    static public final RulesSection RULES = section("rules", RulesSection.class);

    static public class RulesSection extends ConfigurationSection
    {
        public final DisplaySection DISPLAY = section("display", DisplaySection.class);

        static public class DisplaySection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> ON_JOIN = item("onJoin", false);
            public final ConfigurationItem<Boolean> ON_START = item("onStart", true);
        }

        public final ConfigurationList<String> RULES = list("rules", String.class);
    }

    static public final CommandsSection COMMANDS = section("commands", CommandsSection.class);

    static public class CommandsSection extends ConfigurationSection
    {
        public final ConfigurationList<String> EXECUTE_SERVER_START = list("execute-server-start", String.class);
        public final ConfigurationList<String> EXECUTE_START = list("execute-start", String.class);
        public final ConfigurationList<String> EXECUTE_END = list("execute-end", String.class);
    }

    static public final ProtipsSection PROTIPS = section("protips", ProtipsSection.class);

    static public class ProtipsSection extends ConfigurationSection
    {
        public final SoundSection SOUND = section("sound", SoundSection.class);

        public final TeamchatSection TEAMCHAT = section("teamchat", TeamchatSection.class);

        static public class TeamchatSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> USE_T_COMMAND = item("useTCommand", true);
            public final ConfigurationItem<Boolean> LOCK = item("lock", true);
            public final ConfigurationItem<Boolean> USE_G_COMMAND = item("useGCommand", true);
        }

        public final CraftsSection CRAFTS = section("crafts", CraftsSection.class);

        static public class CraftsSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> GOLDEN_HEAD = item("goldenHead", true);
            public final ConfigurationItem<Boolean> COMPASS_EASY = item("compassEasy", true);
            public final ConfigurationItem<Boolean> COMPASS_MEDIUM = item("compassMedium", true);
            public final ConfigurationItem<Boolean> COMPASS_HARD = item("compassHard", true);
            public final ConfigurationItem<Boolean> GLISTERING_MELON = item("glisteringMelon", true);
            public final ConfigurationItem<Boolean> NO_ENCH_GOLDEN_APPLE = item("noEnchGoldenApple", true);
        }

        public final StartSection START = section("start", StartSection.class);

        static public class StartSection extends ConfigurationSection
        {
            public final ConfigurationItem<Boolean> INVINCIBILITY = item("invincibility", true);
        }
    }

    static public final ConfigurationList<Vector> SPAWN_POINTS = list("spawnpoints", Vector.class);
    static public final ConfigurationList<String> TEAMS = list("teams", String.class);


    /* ** Helper sub-sections ** */

    static public class SoundSection extends ConfigurationSection
    {
        public final ConfigurationItem<String> NAME = item("name", "");
        public final ConfigurationItem<Integer> VOLUME = item("volume", 1);
        public final ConfigurationItem<Integer> PITCH = item("pitch", 1);
    }
    
    /* ** Helper value handlers ** */
    
    static
    {
        ConfigurationValueHandlers.registerHandlers(TeamManager.class);
    }
}
