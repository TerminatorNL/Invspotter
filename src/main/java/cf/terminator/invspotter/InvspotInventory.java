package cf.terminator.invspotter;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.Loader;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InvspotInventory implements IInventory{

    private final String title;
    private final HashMap<Integer, NBTTagCompound> contents = new HashMap<Integer, NBTTagCompound>();

    private static int inventoryToGUI(int slot) {
        if (slot < 9) {
            slot = slot + 27 + 18;
        } else if(slot < 36) {
            slot = slot - 9 + 18;
        } else if(slot >= 100 && slot <= 109){
            slot = slot - 100;
        }
        return slot;
    }

    public InvspotInventory(UUID targetUUID, String name) throws CommandException{
        title = name + ": Snapshot";

        EntityPlayerMP onlinePlayer = null;
        for (EntityPlayerMP potential : (List<EntityPlayerMP>) MinecraftServer.getServer().getConfigurationManager().playerEntityList){
            if(targetUUID.equals(potential.getUniqueID())){
                onlinePlayer = potential;
                break;
            }
        }

        NBTTagCompound PlayerNBT = new NBTTagCompound();
        if(onlinePlayer != null){
            onlinePlayer.writeEntityToNBT(PlayerNBT);
            if(Loader.isModLoaded("Baubles")) {
                IInventory inventory = BaublesApi.getBaubles(onlinePlayer);
                for(int i=0; i<inventory.getSizeInventory();i++){
                    ItemStack stack = inventory.getStackInSlot(i);
                    if(stack != null) {
                        NBTTagCompound compound = new NBTTagCompound();
                        stack.writeToNBT(compound);
                        contents.put(i + 9, compound);
                    }
                }
            }
        }else{
            try {
                PlayerNBT = CompressedStreamTools.readCompressed(new FileInputStream(Main.getPlayerDat(targetUUID)));
                NBTTagCompound BAUBLES_NBT = CompressedStreamTools.readCompressed(new FileInputStream(Main.getPlayerBaubles(targetUUID)));

                NBTTagList baublesInventory = (NBTTagList) BAUBLES_NBT.getTag("Baubles.Inventory");
                for(int i=0;i<baublesInventory.tagCount();i++){
                    NBTTagCompound item = baublesInventory.getCompoundTagAt(i);
                    int slot = item.getInteger("Slot");
                    contents.put(slot + 9, item);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new CommandException("Unable to open player file: " + Main.getPlayerDat(targetUUID));
            }
        }
        NBTTagList playerInventory = (NBTTagList) PlayerNBT.getTag("Inventory");
        for(int i=0;i<playerInventory.tagCount();i++){
            NBTTagCompound item = playerInventory.getCompoundTagAt(i);
            int slot = inventoryToGUI(item.getInteger("Slot"));
            contents.put(slot, item);
        }
    }

    public void printData(ICommandSender sender){
        sender.addChatMessage(new ChatComponentText(title));
        for (Map.Entry<Integer, NBTTagCompound> entry: contents.entrySet()){
            ItemStack stack = ItemStack.loadItemStackFromNBT(entry.getValue());
            StringBuilder builder = new StringBuilder();
            if(stack != null) {
                builder.append("[" + entry.getKey() + "] " + stack.getDisplayName());
                if(stack.getItemDamageForDisplay() != 0){
                    builder.append(":" + stack.getItemDamageForDisplay());
                }
                if(stack.stackSize != 1) {
                    builder.append(" (x" + stack.stackSize + ")");
                }
                sender.addChatMessage(new ChatComponentText(builder.toString()));
            }
        }
    }

    @Override
    public int getSizeInventory() {
        return 54;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if(contents.containsKey(slot) && contents.get(slot) != null){
            return ItemStack.loadItemStackFromNBT(contents.get(slot));
        }else {
            return null;
        }
    }


    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack saved = ItemStack.loadItemStackFromNBT(contents.get(slot));
        ItemStack result = saved.copy();

        saved.stackSize = saved.stackSize - amount;
        contents.put(slot, saved.getTagCompound());
        result.stackSize = amount;
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack item) {
    }

    @Override
    public String getInventoryName() {
        return EnumChatFormatting.DARK_RED + title;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack item) {
        return false;
    }
}
