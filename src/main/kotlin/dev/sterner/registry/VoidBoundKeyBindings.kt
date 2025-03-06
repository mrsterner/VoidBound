package dev.sterner.registry

import com.mojang.blaze3d.platform.InputConstants
import dev.sterner.VoidBoundClient
import dev.sterner.client.screen.FocusSelectionScreen
import dev.sterner.common.item.WandItem
import io.github.fabricators_of_create.porting_lib.event.client.KeyInputCallback
import io.github.fabricators_of_create.porting_lib.event.client.MouseInputEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft

object VoidBoundKeyBindings {

    private var focusKeyBind: KeyMapping? = KeyBindingHelper.registerKeyBinding(
        KeyMapping("key.voidbound.focus_select", InputConstants.KEY_F, "category.voidbound")
    )


    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register(::listenFocusKey)
        KeyInputCallback.EVENT.register(::listenItemAbilitySelect)
        MouseInputEvents.BEFORE_SCROLL.register(::onMouseScrolled)
    }

    private fun onMouseScrolled(deltaX: Double, delta: Double): Boolean {
        if (Minecraft.getInstance().screen != null) return false

        val cancelled: Boolean = (VoidBoundClient.ITEM_ABILITY_HANDLER.mouseScrolled(delta))
        return cancelled
    }

    private fun listenItemAbilitySelect(key: Int, scancode: Int, action: Int, mods: Int) {
        if (Minecraft.getInstance().screen != null) return

        val pressed = action != 0

        VoidBoundClient.ITEM_ABILITY_HANDLER.onKeyInput(key, pressed)
    }

    private fun listenFocusKey(minecraft: Minecraft) {
        if (minecraft.player != null) {
            if (focusKeyBind?.isDown == true) {
                if (minecraft.screen == null && minecraft.player!!.mainHandItem.item is WandItem) {
                    minecraft.setScreen(FocusSelectionScreen(minecraft.player!!))
                }
            }
        }
    }
}