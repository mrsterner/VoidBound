package dev.sterner.common.item.equipment.ichor

import com.sammy.malum.common.item.curiosities.weapons.scythe.MagicScytheItem
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class IchoriumEdge(tier: Tier?, attackDamageIn: Float, attackSpeedIn: Float, builderIn: Properties?) :
    MagicScytheItem(
        tier,
        attackDamageIn + 3 + tier!!.attackDamageBonus,
        attackSpeedIn - 1.2f,
        4f,
        builderIn
    ) {
    override fun appendHoverText(
        stack: ItemStack,
        level: Level?,
        tooltipComponents: MutableList<Component>,
        isAdvanced: TooltipFlag
    ) {
        tooltipComponents.add(Component.translatable("voidbound.hold_l_alt"))
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced)
    }
}