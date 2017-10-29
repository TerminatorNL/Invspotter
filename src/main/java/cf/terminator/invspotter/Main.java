package cf.terminator.invspotter;

import cf.terminator.invspotter.commands.OpenInv;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraftforge.common.UsernameCache;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.UUID;

@Mod(modid = Main.MODID, version = Main.VERSION, name = Main.MODID, acceptableRemoteVersions = "*")
public class Main {

    static final String MODID = "invspotter";
    static final String VERSION = "1.0";

    static File getPlayerDat(UUID uuid){
        return new File("./world/playerdata/" + uuid.toString() + ".dat");
    }
    static File getPlayerBaubles(UUID uuid){
        return new File("./world/playerdata/" + UsernameCache.getLastKnownUsername(uuid) + ".baub");
    }

    @EventHandler
    public void init(FMLServerStartingEvent event){
        event.registerServerCommand(new OpenInv());
    }
}
