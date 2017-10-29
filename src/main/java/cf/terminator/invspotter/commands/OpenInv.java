package cf.terminator.invspotter.commands;

import cf.terminator.invspotter.InvspotInventory;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.UsernameCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OpenInv extends CommandBase{

    @Override
    public String getCommandName() {
        return "openinv";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "openinv <player>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        boolean senderIsConsole = sender.getCommandSenderName().equalsIgnoreCase("Server") || sender.getCommandSenderName().equalsIgnoreCase("Rcon");

        if(args.length != 1){
            throw new CommandException("Argument: <player name>");
        }
        String player = args[0];

        HashMap<String, UUID> reverse = new HashMap<String, UUID>();
        for(Map.Entry<UUID, String> entry : UsernameCache.getMap().entrySet()) {
            reverse.put(entry.getValue().toLowerCase(), entry.getKey());
        }

        UUID uuid;
        String name;
        if(reverse.containsKey(player.toLowerCase())){
            uuid = reverse.get(player.toLowerCase());
            name = UsernameCache.getLastKnownUsername(uuid);
        }else{
            throw new CommandException("Can't find the UUID for: " + player + ". Did this player ever log in before?");
        }


        EntityPlayerMP admin = null;
        for (EntityPlayerMP potential : (List<EntityPlayerMP>) MinecraftServer.getServer().getConfigurationManager().playerEntityList){
            if(sender.getCommandSenderName().equals(potential.getCommandSenderName())){
                admin = potential;
                break;
            }
        }
        assert (admin != null);


        InvspotInventory inventory = new InvspotInventory(uuid, name);
        if(senderIsConsole == false) {
            admin.displayGUIChest(inventory);
        }else{
            inventory.printData(sender);
        }
    }
}
