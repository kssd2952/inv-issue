package io.github.kssd2952

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityDropItemEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Main : JavaPlugin(), Listener {
    companion object {
        var itemCount = 1
        val serverItems = mutableListOf<Item>()
    }

    override fun onEnable() {
        logger.info("inv-issue plugin v" + pluginMeta.version)
        Bukkit.getServer().pluginManager.registerEvents(this, this)
    }

    fun multiplyItem(world: World, items: MutableList<Item>, location: Location) {
        if (itemCount < 1) {
            itemCount = 1
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clear @a")

            for (world in Bukkit.getWorlds()) {
                for (entity in world.entities) {
                    if (entity is Player) {
                        for (effect in entity.activePotionEffects) {
                            entity.removePotionEffect(effect.type)
                        }
                        entity.kick(Component.text("당신은 지구를 소중히 생각하지 않았지"))
                    } else {
                        entity.remove()
                    }
                }
            }
        } else {
            for (item in items) {
                repeat(itemCount) {
                    world.dropItem(location, item.itemStack.clone())
                }
            }

            itemCount *= 2
        }
    }

    @EventHandler
    fun onBlockDropItem(event: BlockDropItemEvent) {
        val world = event.block.world
        val items = event.items
        val location = event.block.location
        event.isCancelled = true

        multiplyItem(world, items, location)
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val world = event.entity.world
        val location = event.entity.location
        val drops = event.drops
        val items = mutableListOf<Item>()
        for (drop in drops) {
            items.add(event.entity.world.dropItem(event.entity.location, drop))
        }

        multiplyItem(world, items, location)
    }

    @EventHandler
    fun onItemSpawn(event: ItemSpawnEvent) {
        val item = event.entity
        item.isGlowing = true
        serverItems.add(item)

        for (player in Bukkit.getOnlinePlayers()) {
            player.addPotionEffect(
                PotionEffect(
                    PotionEffectType.POISON, PotionEffect.INFINITE_DURATION, 0, false, true
                )
            )
        }
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        if (!serverItems.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(this, Runnable {
                event.player.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.POISON, PotionEffect.INFINITE_DURATION, 0, false, true
                    )
                )
            }, 1L)
        }
    }

    @EventHandler
    fun onItemDespawn(event: EntityRemoveFromWorldEvent) {
        if (event.entity is Item) {
            serverItems.remove(event.entity)

//            logger.info((event.entity as Item).itemStack.amount.toString())
//            logger.info(itemCount.toString())

            if (serverItems.isEmpty()) {
                for (player in Bukkit.getOnlinePlayers()) {
                    player.removePotionEffect(PotionEffectType.POISON)
                }
            }
        }
    }
}