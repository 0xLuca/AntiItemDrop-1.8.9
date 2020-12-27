package at.luca.antiitemdrop;

import net.labymod.api.LabyModAddon;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.mojang.inventory.GuiInventoryCustom;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0EPacketClickWindow;

import java.lang.reflect.Field;
import java.util.List;

public class AntiItemDrop extends LabyModAddon {

    private static Minecraft minecraft = Minecraft.getMinecraft();
    private static Field lowerInventoryField;

    static {
        try {
            lowerInventoryField = GuiChest.class.getDeclaredField(LabyModCoreMod.isObfuscated() ?  LabyModCoreMod.isForge() ? "field_147015_w" : "w" : "lowerChestInventory");
            lowerInventoryField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void loadConfig() {

    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {

    }

    private static C0EPacketClickWindow lastSentPacket = null;

    public static boolean shouldSendPacket(Packet<?> packet) {
        if (!(packet instanceof C0EPacketClickWindow) || !(minecraft.currentScreen instanceof GuiContainer)) {
            return true;
        }
        C0EPacketClickWindow windowClickPacket = (C0EPacketClickWindow) packet;
        if (windowClickPacket.getMode() != 0 /* ClickType Pickup, so that shifting is supported.*/) {
            return true;
        }
        GuiContainer container = (GuiContainer) minecraft.currentScreen;
        if (container instanceof GuiChest || container instanceof GuiInventoryCustom) {
            if (container instanceof GuiChest) {
                GuiChest chest = (GuiChest) container;
                String title = "";
                try {
                    title = ((IInventory) lowerInventoryField.get(chest)).getDisplayName().getUnformattedText();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (!title.equals("Ender Chest")) {
                    return true;
                }
            }

            Slot slot = container.inventorySlots.getSlot(windowClickPacket.getSlotId());
            if (windowClickPacket.getSlotId() >= 0 && (slot.getStack() == null || slot.getStack().getItem() == Item.getItemById(0))) {
                lastSentPacket = windowClickPacket;
                return false;
            } else if (lastSentPacket != null) {
                Minecraft.getMinecraft().thePlayer.sendQueue.getNetworkManager().sendPacket(lastSentPacket);
            }
        }

        return true;
    }
}
