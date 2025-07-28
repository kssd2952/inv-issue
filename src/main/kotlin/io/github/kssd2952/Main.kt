package io.github.kssd2952

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.ItemSpawnEvent
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

    @EventHandler
    fun onBlockDropItem(event: BlockDropItemEvent) {
        val world = event.block.world
        val items = event.items
        val location = event.block.location
        event.isCancelled = true

        for (item in items) {
            repeat(itemCount) {
                world.dropItem(location, item.itemStack.clone())
            }
        }

        itemCount *= 2
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