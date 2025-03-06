package dev.sterner.api

import com.sammy.malum.client.VoidRevelationHandler
import com.sammy.malum.common.container.WeaversWorkbenchContainer.component
import com.sammy.malum.core.systems.recipe.SpiritWithCount
import dev.sterner.api.item.ItemAbility
import dev.sterner.api.item.ItemAbilityWithLevel
import dev.sterner.listener.EnchantSpiritDataReloadListener
import dev.sterner.registry.VoidBoundComponentRegistry
import dev.sterner.registry.VoidBoundItemRegistry
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.GlobalPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.Level
import team.lodestar.lodestone.helpers.TrinketsHelper

object VoidBoundApi {

    /**
     * Returns true if a client player has the hallowed goggles or monocle equipped
     */
    fun hasGoggles(): Boolean {
        val player = Minecraft.getInstance().player
        if (player != null) {
            val bl = TrinketsHelper.hasTrinketEquipped(player, VoidBoundItemRegistry.HALLOWED_MONOCLE.get())
            val bl2 = Minecraft.getInstance().player!!.getItemBySlot(EquipmentSlot.HEAD)
                .`is`(
                    VoidBoundItemRegistry.HALLOWED_GOGGLES.get()
                )
            return bl || bl2
        }
        return false
    }

    /**
     * Returns true if a player has the hallowed goggles or monocle equipped
     */
    fun hasGoggles(player: Player): Boolean {
        val bl = TrinketsHelper.hasTrinketEquipped(player, VoidBoundItemRegistry.HALLOWED_MONOCLE.get())
        val bl2 = Minecraft.getInstance().player!!.getItemBySlot(EquipmentSlot.HEAD)
            .`is`(
                VoidBoundItemRegistry.HALLOWED_GOGGLES.get()
            )
        return bl || bl2
    }

    /**
     * Returns how many spirits of each kind a enchantment is worth for the osmotic enchanter
     */
    fun getSpiritFromEnchant(enchantment: Enchantment, level: Int): List<SpiritWithCount> {

        val reg = BuiltInRegistries.ENCHANTMENT.getKey(enchantment)
        val list = EnchantSpiritDataReloadListener.ENCHANTING_DATA[reg]
        val out = mutableListOf<SpiritWithCount>()
        if (list != null) {
            for (spiritIn in list.spirits) {
                out.add(SpiritWithCount(spiritIn.type, spiritIn.count * level))
            }
        }

        return out
    }

    /**
     * Returns false if the block being broken is warded by another player
     */
    fun canPlayerBreakBlock(level: Level, player: Player, blockPos: BlockPos): Boolean {
        val comp = VoidBoundComponentRegistry.VOID_BOUND_WORLD_COMPONENT.get(level)
        if (comp.isEmpty()) {
            return true
        }

        return !comp.isPosBoundToAnotherPlayer(player, GlobalPos.of(player.level().dimension(), blockPos))
    }

    /**
     * Returns false if the block being broken is warded by any player
     */
    fun canBlockBreak(level: Level, blockPos: BlockPos): Boolean {
        val comp = VoidBoundComponentRegistry.VOID_BOUND_WORLD_COMPONENT.get(level)
        if (comp.isEmpty()) {
            return true
        }

        if (comp.hasBlockPos(GlobalPos.of(level.dimension(), blockPos))) {
            return false
        }
        return true
    }

    fun hasTearKnowledgeClient(): Boolean {
        val player = Minecraft.getInstance().player
        if (player != null) {
            val comp = VoidBoundComponentRegistry.VOID_BOUND_REVELATION_COMPONENT.get(player)
            return comp.isTearKnowledgeComplete()
        }
        return false
    }

    fun addThought(player: Player, text: Component, duration: Int = 20 * 5){
        VoidBoundComponentRegistry.VOID_BOUND_REVELATION_COMPONENT.maybeGet(player).ifPresent {
            it.addThought(text, duration, 20 * 5)
        }
    }

    fun getItemAbility(stack: ItemStack): List<ItemAbilityWithLevel> {
        val abilities = mutableListOf<ItemAbilityWithLevel>()
        val tag = stack.tag ?: return abilities // Return empty if no NBT

        val abilitiesTag = tag.getList("Abilities", 10) // 10 is the NBT type for CompoundTag
        for (i in 0 until abilitiesTag.size) {
            val abilityTag = abilitiesTag.getCompound(i)
            val ability = ItemAbilityWithLevel.readNbt(abilityTag)
            abilities.add(ability)
        }
        return abilities
    }

    // Function to add an ItemAbilityWithLevel to an ItemStack's NBT
    fun addItemAbility(stack: ItemStack, abilityWithLevel: ItemAbilityWithLevel) {
        val tag = stack.orCreateTag // Ensures the stack has NBT
        val abilitiesTag = tag.getList("Abilities", 10) // Fetch or create list

        // Check if ability already exists, if so, skip adding a duplicate
        for (i in 0 until abilitiesTag.size) {
            val abilityTag = abilitiesTag.getCompound(i)
            val existingAbility = ItemAbilityWithLevel.readNbt(abilityTag)
            if (existingAbility.itemAbility == abilityWithLevel.itemAbility) {
                return // Ability already exists, exit without adding
            }
        }

        // Add new ability
        abilitiesTag.add(abilityWithLevel.writeNbt())
        tag.put("Abilities", abilitiesTag)
    }

    // Function to modify the level of an existing ItemAbility in NBT
    fun modifyItemAbilityLevel(stack: ItemStack, itemAbility: ItemAbility, newLevel: Int) {
        val tag = stack.tag ?: return // No NBT, nothing to modify
        val abilitiesTag = tag.getList("Abilities", 10)

        // Find the ability and modify its level
        for (i in 0 until abilitiesTag.size) {
            val abilityTag = abilitiesTag.getCompound(i)
            val ability = ItemAbilityWithLevel.readNbt(abilityTag)
            if (ability.itemAbility == itemAbility) {
                // Modify the level and update the NBT
                abilityTag.putInt("Level", newLevel)
                abilitiesTag[i] = abilityTag // Replace the modified ability in the list
                tag.put("Abilities", abilitiesTag)
                return
            }
        }
    }

    fun hasItemAbility(stack: ItemStack, ability: ItemAbility): Boolean {
        return !getItemAbility(stack).none { it.itemAbility == ability }
    }
}