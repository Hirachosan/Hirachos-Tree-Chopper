package btw.community.htf;

import api.AddonHandler;
import api.BTWAddon;
import btw.block.BTWBlockIDs;
//import btw.community.htf.classes.ChoppedLogBlock;
import btw.community.htf.classes.ChoppedLogBlock;
import net.fabricmc.api.ModInitializer;
import net.minecraft.src.Block;

import static btw.block.BTWBlocks.oakChewedLog;

public class TreeFellerAddon extends BTWAddon implements ModInitializer {
    private static TreeFellerAddon instance;
    public static Block choppedLogBlock;
    public static final int CHOPPED_LOG_BLOCK_ID = 2050;


    public TreeFellerAddon() {
        super();
    }

    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
        addBlocks();
    }

    private void addBlocks() {
        choppedLogBlock = new ChoppedLogBlock(CHOPPED_LOG_BLOCK_ID, oakChewedLog);

    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
    }
}