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

package eu.carrade.amaury.quartzsurvivalgames.modules.worldgen.creatures;

import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleLoadTime;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import fr.zcraft.quartzlib.components.configuration.ConfigurationParseException;
import fr.zcraft.quartzlib.components.configuration.ConfigurationValueHandlers;
import fr.zcraft.quartzlib.exceptions.IncompatibleMinecraftVersionException;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.quartzlib.tools.reflection.Reflection;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

@ModuleInfo(
        name = "Creatures Spawn Control",
        description = "Alters creatures spawn rules for all or some biomes. " +
                "This include disabling spawn of some creatures completely.\n\n" +
                "For best results, enable before generating the worlds.",
        when = ModuleLoadTime.STARTUP,
        category = ModuleCategory.WORLD_GENERATION,
        icon = Material.SPAWNER,
        settings = Config.class
)
public class CreaturesModule extends QSGModule {
    /**
     * Map {@link EntityType} -> {@code Class<? extends net.minecraft.server.Entity>}
     */
    private final static Map<EntityType, Class<?>> NMS_ENTITY_CLASSES = new HashMap<>();
    /**
     * Map {@link EntityType} -> {@code net.minecraft.server.EntityTypes}
     */
    private final static Map<EntityType, Object> NMS_ENTITY_TYPES = new HashMap<>();
    /**
     * Map {@link Biome} -> {@code net.minecraft.server.BiomeBase}
     */
    private final static Map<Biome, Object> NMS_BIOMES = new HashMap<>();
    /**
     * {@code true} if successfully hooked into NMS.
     */
    private static boolean hooked;
    private static Class<?> WEIGHTED_RANDOM_CHOICE_CLASS;
    private static Class<?> CREATURE_TYPE_ENUM;
    private static Field CREATURE_TYPE_ENUM_CLASS_FIELD;
    private static Constructor<?> BIOME_META_CONSTRUCTOR;

    static {
        try {
            final Class<?> ENTITY_TYPES_CLASS = Reflection.getMinecraftClassByName("EntityTypes");
            final Class<?> BIOME_BASE_BIOME_META_CLASS = Reflection.getMinecraftClassByName("BiomeBase$BiomeMeta");

            WEIGHTED_RANDOM_CHOICE_CLASS = Reflection.getMinecraftClassByName("WeightedRandom$WeightedRandomChoice");
            CREATURE_TYPE_ENUM = Reflection.getMinecraftClassByName("EnumCreatureType");

            CREATURE_TYPE_ENUM_CLASS_FIELD = Arrays.stream(CREATURE_TYPE_ENUM.getDeclaredFields())
                    .filter(field -> field.getType().equals(Class.class)).findFirst()
                    .orElseThrow(() -> new IncompatibleMinecraftVersionException(
                            new Exception("Cannot find the field containing the super class in EnumCreatureType")));

            CREATURE_TYPE_ENUM_CLASS_FIELD.setAccessible(true);

            try {
                BIOME_META_CONSTRUCTOR =
                        BIOME_BASE_BIOME_META_CLASS.getConstructor(Class.class, int.class, int.class, int.class);
            }
            catch (final NoSuchMethodException | SecurityException e) {
                BIOME_META_CONSTRUCTOR =
                        BIOME_BASE_BIOME_META_CLASS.getConstructor(ENTITY_TYPES_CLASS, int.class, int.class, int.class);
            }

            BIOME_META_CONSTRUCTOR.setAccessible(true);

            // We build the two above maps to access the classes & types faster in all patches.
            // We first loop over `EntityTypes` fields to detect the version we have.

            EntityTypesClassVersion version = null;
            boolean hadMaps = false;

            for (final Field field : ENTITY_TYPES_CLASS.getDeclaredFields()) {
                if (field.getType().equals(ENTITY_TYPES_CLASS)) {
                    version = EntityTypesClassVersion.STATIC_ATTRIBUTES;
                    break;
                } else if (field.getType().getSimpleName().equals("RegistryMaterials")) {
                    version = EntityTypesClassVersion.MINECRAFT_KEYS;
                    break;
                } else if (Map.class.isAssignableFrom(field.getType())) {
                    hadMaps = true;
                }
            }

            if (version == null && hadMaps) {
                version = EntityTypesClassVersion.MAPS;
            }
            if (version == null) {
                throw new IncompatibleMinecraftVersionException(new Exception("EntityTypes class version unsupported"));
            }

            switch (version) {
                // FIXME UNTESTED
                case MAPS:
                    // We loop over all maps to check the values inside.
                    // We want to find the map String -> Class<? extends net.minecraft.server.Entity>
                    for (final Field field : ENTITY_TYPES_CLASS.getDeclaredFields()) {
                        if (!Map.class.isAssignableFrom(field.getType())) {
                            continue;
                        }
                        field.setAccessible(true);

                        final Map<?, ?> map = (Map) field.get(null);
                        final Map.Entry entry = map.entrySet().stream().findFirst().orElse(null);

                        if (entry == null) {
                            continue;
                        }

                        if (entry.getKey().getClass().equals(String.class) &&
                                entry.getValue().getClass().equals(Class.class)) {
                            // We found the one, let's save all of this.
                            // We can't extract EntityTypes for this version because the instances doesn't even exist,
                            // and we don't need them anyway.
                            final Map<String, Class<?>> keyToClass = (Map<String, Class<?>>) map;

                            for (final EntityType entityType : EntityType.values()) {
                                final String name = (String) Reflection.getFieldValue(entityType, "name");
                                if (name == null) {
                                    continue;
                                }
                                NMS_ENTITY_CLASSES.put(entityType, keyToClass.get(name));
                            }

                            break;
                        }
                    }
                    break;

                case MINECRAFT_KEYS:
                    // Here we can load this class
                    final Class<?> REGISTRY_CLASS = Reflection.getMinecraftClassByName("RegistryMaterials");
                    final Method REGISTRY_METHOD_GET = REGISTRY_CLASS.getDeclaredMethod("get", Object.class);
                    final Class<?> MINECRAFT_KEY_CLASS = Reflection.getMinecraftClassByName("MinecraftKey");

                    Object registry = null;
                    for (final Field field : ENTITY_TYPES_CLASS.getDeclaredFields()) {
                        if (field.getType().equals(REGISTRY_CLASS)) {
                            registry = field.get(null);
                            break;
                        }
                    }

                    // Should be impossible
                    if (registry == null) {
                        throw new IncompatibleMinecraftVersionException(
                                new NoSuchFieldException("Cannot retrieve the EntityTypes registry."));
                    }

                    for (final EntityType entityType : EntityType.values()) {
                        // We can't extract EntityTypes for this version because the instances doesn't even exist,
                        // and we don't need them anyway.
                        final String name = (String) Reflection.getFieldValue(entityType, "name");
                        if (name == null) {
                            continue;
                        }

                        NMS_ENTITY_CLASSES.put(entityType, (Class<?>) REGISTRY_METHOD_GET
                                .invoke(registry, Reflection.instantiate(MINECRAFT_KEY_CLASS, name)));
                    }

                    break;

                // FIXME UNTESTED (can only be tested with 1.13 support)
                case STATIC_ATTRIBUTES:
                    // Intermediate map key -> bukkit entity type
                    final Map<String, EntityType> BY_KEY = Arrays.stream(EntityType.values())
                            .map(entityType -> {
                                try {
                                    return new AbstractMap.SimpleEntry<>(
                                            (String) Reflection.getFieldValue(entityType, "name"), entityType);
                                }
                                catch (NoSuchFieldException | IllegalAccessException e) {
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .filter(entry -> entry.getKey() != null)
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    for (Field field : ENTITY_TYPES_CLASS.getDeclaredFields()) {
                        if (!field.getType().equals(ENTITY_TYPES_CLASS)) {
                            continue;
                        }

                        final Object type = field.get(null);
                        final String key = (String) Reflection.call(type, "d");  // Returns the key (String)
                        final Class<?> clazz =
                                (Class<?>) Reflection.call(type, "c");  // Returns the class (Class<? extends Entity>)

                        final EntityType bukkitType = BY_KEY.get(key);
                        if (bukkitType == null) {
                            continue;
                        }

                        NMS_ENTITY_CLASSES.put(bukkitType, clazz);
                        NMS_ENTITY_TYPES.put(bukkitType, type);
                    }

                    break;
            }

            // We also build a map from Bukkit's to NMS' biomes.
            final Class<?> CRAFT_BLOCK_CLASS = Reflection.getBukkitClassByName("block.CraftBlock");

            for (final Biome biome : Biome.values()) {
                NMS_BIOMES.put(biome, Reflection.call(CRAFT_BLOCK_CLASS, "biomeToBiomeBase", biome));
            }

            hooked = true;
        }
        catch (final Exception e) {
            PluginLogger.error("Unable to hook into NMS to patch creatures spawn.", e);
            hooked = false;
        }
    }

    @Override
    protected void onEnable() {
        for (final Map rule : Config.SPAWN_RULES) {
            try {
                if (!rule.containsKey("entity")) {
                    log().warning("Skipping spawn rule without entity.");
                    continue;
                }

                final EntityType entity = ConfigurationValueHandlers.handleValue(rule.get("entity"), EntityType.class);

                final int weight = ConfigurationValueHandlers.handleIntValue(rule.getOrDefault("weight", -1));
                final int minPackSize =
                        ConfigurationValueHandlers.handleIntValue(rule.getOrDefault("minimal_pack_size", -1));
                final int maxPackSize =
                        ConfigurationValueHandlers.handleIntValue(rule.getOrDefault("maximal_pack_size", -1));

                final Biome[] biomes;
                final boolean onlyIfPresent;

                if (!rule.containsKey("biomes")) {
                    biomes = null;
                    onlyIfPresent = true;
                } else {
                    final Object rawBiomes = rule.get("biomes");

                    if (rawBiomes instanceof String && ((String) rawBiomes).trim().equalsIgnoreCase("ALL")) {
                        biomes = null;
                        onlyIfPresent = false;
                    } else if (rawBiomes instanceof List) {
                        final List<Biome> biomesList = new ArrayList<>(((List) rawBiomes).size());

                        for (final Object biome : ((List) rawBiomes)) {
                            try {
                                biomesList.add(ConfigurationValueHandlers.handleValue(biome, Biome.class));
                            }
                            catch (ConfigurationParseException e) {
                                log().warning("Ignoring unknown biome {0} in spawn rules.", e.getValue());
                            }
                        }

                        biomes = biomesList.toArray(new Biome[0]);
                        onlyIfPresent = false;
                    } else {
                        log().warning("Invalid `biomes` key in spawn rule (neither a string nor a list); ignoring.");

                        biomes = null;
                        onlyIfPresent = true;
                    }
                }

                patchAnimalsSpawn(entity, weight, minPackSize, maxPackSize, onlyIfPresent, biomes);
            }
            catch (ConfigurationParseException e) {
                log().warning("Invalid spawn rule, skipping. {0} (erroneous value: {1}).", e.getMessage(),
                        e.getValue());
            }
        }
    }

    /*
      CREATURES SPAWN PATCH METHOD

      1. final BiomeBase base = CraftBlock.biomeToBiomeBase(bukkitBiome);
      2. final Entity entity = craftEntity.getHandle();
      3. loop EnumCreatureType, check if entity type.a() is subclass of entity
          3b. If given EntityType,
      4. if so, we have ctype = the type
      5. List<BiomeBase.BiomeMeta> metas = base.getMobs(ctype);
      6. Loop over metas to find in meta.b: if it's a class, the entity class; if it's a `EntityTypes` (1.13+), a type
          6b. if it's  type, check the class with etypes.c()
      7. If good type found in the biome base metas, update the instance.
          meta.a = weight
          meta.c = spawnPackMin
          meta.d = spawnPackMax
          (meta.b = type or class)
      8. If not found, create the instance and att it to the list. Constructors:
          8a. BiomeMeta(Class<? extends Entity> entityClass, int weight, int spawnPackMin, int spawnPackMax);
          8b. BiomeMeta(EntityTypes entityType, int weight, int spawnPackMin, int spawnPackMax);
              In this case, loop over static fields of type `EntityTypes` in `EntityTypes` and check their `c()` method
              to get the class and compare it (maybe add this into a cache class -> type).
     */

    /**
     * Patches the Minecraft Server to update spawning rules for the given entity, in all biomes where the given entity
     * already spawn.
     * <p>
     * If integer values (weight, min & max) are negative, their values will be left untouched (excepted if a new rule
     * is created from scratch if {@code onlyIfPresent = false}, then 1 will be used for all of them).
     *
     * @param entity       The entity to alter the spawning rules of.
     * @param weight       The spawn weight. At each spawn tentative, the higher this number is, the higher the probability
     *                     of this entity to be selected for spawn is. This is also true while generating the chunks.
     * @param spawnPackMin While generating the chunks, the server spawns entities in them in groups. This is the
     *                     minimal size of these spawn groups.
     *                     During natural generation, entities are spawned alone, and these parameters are ignored.
     * @param spawnPackMax This is the maximal size of a group while generating the chunks (see spawnPackMin).
     */
    public void patchAnimalsSpawn(final EntityType entity, final int weight, final int spawnPackMin,
                                  final int spawnPackMax) {
        patchAnimalsSpawn(entity, weight, spawnPackMin, spawnPackMax, true, (Biome[]) null);
    }

    /**
     * Patches the Minecraft Server to update spawning rules for the given entity, in all biomes.
     *
     * @param entity        The entity to alter the spawning rules of.
     * @param weight        The spawn weight. At each spawn tentative, the higher this number is, the higher the probability
     *                      of this entity to be selected for spawn is. This is also true while generating the chunks.
     * @param spawnPackMin  While generating the chunks, the server spawns entities in them in groups. This is the
     *                      minimal size of these spawn groups.
     *                      During natural generation, entities are spawned alone, and these parameters are ignored.
     * @param spawnPackMax  This is the maximal size of a group while generating the chunks (see spawnPackMin).
     * @param onlyIfPresent If true, the entity spawning rule will only be altered if the creature spawns in the biome.
     *                      Else, if the creature doesn't already spawn in the given biome, it will be added to the
     *                      biome's entities. This is especially useful for all-biomes alterations.
     */
    public void patchAnimalsSpawn(final EntityType entity, final int weight, final int spawnPackMin,
                                  final int spawnPackMax, final boolean onlyIfPresent) {
        patchAnimalsSpawn(entity, weight, spawnPackMin, spawnPackMax, onlyIfPresent, (Biome[]) null);
    }

    /**
     * Patches the Minecraft Server to update spawning rules for the given entity, in the given biome(s).
     * <p>
     * If integer values (weight, min & max) are negative, their values will be left untouched (excepted if a new rule
     * is created from scratch if {@code onlyIfPresent = false}, then 1 will be used for all of them).
     *
     * @param entity        The entity to alter the spawning rules of.
     * @param weight        The spawn weight. At each spawn tentative, the higher this number is, the higher the probability
     *                      of this entity to be selected for spawn is. This is also true while generating the chunks.
     * @param spawnPackMin  While generating the chunks, the server spawns entities in them in groups. This is the
     *                      minimal size of these spawn groups.
     *                      During natural generation, entities are spawned alone, and these parameters are ignored.
     * @param spawnPackMax  This is the maximal size of a group while generating the chunks (see spawnPackMin).
     * @param onlyIfPresent If true, the entity spawning rule will only be altered if the creature spawns in the biome.
     *                      Else, if the creature doesn't already spawn in the given biome, it will be added to the
     *                      biome's entities. This is especially useful for all-biomes alterations.
     * @param biomes        The biomes to alter. Spawning rules are specific to biomes and only these biomes will be patched.
     *                      If {@code null} or empty, all biomes will be patched.
     */
    public void patchAnimalsSpawn(final EntityType entity, final int weight, final int spawnPackMin,
                                  final int spawnPackMax, final boolean onlyIfPresent, final Biome... biomes) {
        if (!hooked) {
            return;  // Incompatible Minecraft version
        }

        final Class<?> nmsEntityClass = NMS_ENTITY_CLASSES.get(entity);
        final Object nmsEntityType = NMS_ENTITY_TYPES.get(entity);

        if (nmsEntityClass == null) {
            return;  // Unsupported entity
        }

        final Enum creatureType = getCreatureType(nmsEntityClass);
        if (creatureType == null) {
            return;  // Non-naturally-spawnable entity
        }

        final Biome[] patchedBiomes = biomes != null && biomes.length > 0 ? biomes : Biome.values();

        for (final Biome biome : patchedBiomes) {
            final Object base = NMS_BIOMES.get(biome);
            if (base == null) {
                continue;
            }

            try {
                final List<Object> metas = (List<Object>) Reflection.call(base, "getMobs", creatureType);
                boolean found = false;

                for (final Object meta : metas) {
                    final Object entityClassOrType = Reflection.getFieldValue(meta, "b");
                    if (!Objects.equals(entityClassOrType, nmsEntityClass) &&
                            !Objects.equals(entityClassOrType, nmsEntityType)) {
                        continue;  // Not the meta we're looking for
                    }

                    if (weight >= 0) {
                        Reflection.setFieldValue(WEIGHTED_RANDOM_CHOICE_CLASS, meta, "a", weight);
                    }
                    if (spawnPackMin >= 0) {
                        Reflection.setFieldValue(meta, "c", spawnPackMin);
                    }
                    if (spawnPackMax >= 0) {
                        Reflection.setFieldValue(meta, "d", spawnPackMax);
                    }

                    found = true;
                }

                // If the entity meta was not present and we want it to be added in this case
                if (!found && !onlyIfPresent) {
                    Object meta;

                    try {
                        meta = BIOME_META_CONSTRUCTOR.newInstance(
                                nmsEntityClass,
                                weight >= 0 ? weight : 1,
                                spawnPackMin >= 0 ? spawnPackMin : 1,
                                spawnPackMax >= 0 ? spawnPackMax : 1
                        );
                    }
                    catch (final IllegalArgumentException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                        try {

                            if (nmsEntityType == null) {
                                throw new IncompatibleMinecraftVersionException(
                                        new Exception("Unknown entity type for " + entity));
                            }

                            meta = BIOME_META_CONSTRUCTOR
                                    .newInstance(nmsEntityType, weight, spawnPackMin, spawnPackMax);
                        }
                        catch (final IllegalArgumentException | InstantiationException | InvocationTargetException | IllegalAccessException e1) {
                            log().warning(
                                    "Unable to construct a new BiomeBase.BiomeMeta (tested two constructors). Nag UHCReloaded authors about this.",
                                    e1);
                            log().warning("Previous exception was:", e);
                            continue;
                        }
                    }

                    metas.add(meta);
                }
            }
            catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
                log().error("Unable to patch entities spawn rules for entity {0} and biome {1}", e, entity, biome);
            }
        }
    }

    private Enum getCreatureType(final Class<?> nmsEntityClass) {
        try {
            for (final Object creatureType : CREATURE_TYPE_ENUM.getEnumConstants()) {
                final Class<?> clazz = (Class<?>) CREATURE_TYPE_ENUM_CLASS_FIELD.get(creatureType);
                if (clazz.isAssignableFrom(nmsEntityClass)) {
                    return (Enum) creatureType;
                }
            }

            return null;
        }
        catch (IllegalAccessException e) {
            return null;
        }
    }


    /**
     * Represents the version of the class storing the entity types, used to access
     * the NMS' entity base class from a Bukkit's EntityType.
     */
    private enum EntityTypesClassVersion {
        /**
         * Entity types stored in maps in the `EntityTypes` class.
         * Covers 1.8 -> 1.10.
         */
        MAPS,

        /**
         * Entity types stored in a registry in `EntityTypes`.
         * Covers 1.11 -> 1.12.
         */
        MINECRAFT_KEYS,

        /**
         * Entity types stored as static attributes of `EntityTypes`.
         * Covers 1.13+.
         */
        STATIC_ATTRIBUTES
    }
}
