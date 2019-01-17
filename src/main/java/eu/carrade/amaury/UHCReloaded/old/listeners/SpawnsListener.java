/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.UHCReloaded.old.listeners;

import eu.carrade.amaury.UHCReloaded.UHConfig;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;


public class SpawnsListener implements Listener
{
    private final Random random = new Random();

    private final boolean RABBIT_SPAWN_ENABLED;
    private final double RABBIT_SPAWN_PROBABILITY;
    private final String RABBIT_NAME;

    public SpawnsListener()
    {
        RABBIT_SPAWN_ENABLED = UHConfig.GAMEPLAY_CHANGES.RABBIT.KILLER_RABBIT_SPAWN.get();
        RABBIT_SPAWN_PROBABILITY = UHConfig.GAMEPLAY_CHANGES.RABBIT.KILLER_RABBIT_SPAWN_PROBABILITY.get();

        RABBIT_NAME = UHConfig.GAMEPLAY_CHANGES.RABBIT.KILLER_RABBIT_NAME.get().trim();
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRabbitSpawn(CreatureSpawnEvent ev)
    {
        if (!RABBIT_SPAWN_ENABLED)
            return;

        if (ev.getEntity().getType() != EntityType.RABBIT)
            return;

        if (random.nextDouble() >= RABBIT_SPAWN_PROBABILITY)
            return;

        Rabbit rabbit = (Rabbit) ev.getEntity();
        rabbit.setRabbitType(Rabbit.Type.THE_KILLER_BUNNY);

        if (!RABBIT_NAME.isEmpty())
        {
            rabbit.setCustomName(RABBIT_NAME);
            rabbit.setCustomNameVisible(true);
        }
    }
}
