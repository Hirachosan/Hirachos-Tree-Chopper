package btw.community.htf.mixins;

import api.item.items.AxeItem;
import btw.client.fx.BTWEffectManager;
import btw.community.htf.TreeFellerAddon;
import btw.community.htf.classes.BlockExtension;
import btw.community.htf.classes.ChoppedLogTileEntity;
import btw.community.htf.classes.LogBlockExtension;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLog.class)
public abstract class BlockLogMixin implements BlockExtension, LogBlockExtension {
    @Shadow
    public abstract boolean getIsStump(IBlockAccess blockAccess, int x, int y, int z);

    @Override
    public boolean htf$isHarvestableLog(IBlockAccess blockAccess, int x, int y, int z) {
        return !getIsStump(blockAccess, x, y, z);
    }

    @Inject(method = "convertBlock", at = @At("HEAD"), cancellable = true)
    private void convertToChoppedLog(ItemStack stack, World world, int x, int y, int z, int iFromSide, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null) {
            return; // Don't convert without tool
        }

        if (stack.getItem() instanceof AxeItem && htf$getIsValidTreeChoppingLog(world, x, y, z)) {
            Block thisBlock = (Block) (Object) this;
            convertToTreeChoppingLog(world, x, y, z, thisBlock.blockID);
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean htf$getIsValidTreeChoppingLog(IBlockAccess blockAccess, int x, int y, int z) {
        if (Block.blocksList[blockAccess.getBlockId(x, y - 1, z)] instanceof BlockLog logBelow
                && logBelow.getIsStump(blockAccess, x, y - 1, z)) {
            return Block.blocksList[blockAccess.getBlockId(x, y + 1, z)] instanceof BlockLog;
        }

        return false;
    }

    @Unique
    private static void convertToTreeChoppingLog(World world, int x, int y, int z, int blockID) {
        if (!world.isRemote) {
            world.playAuxSFX(BTWEffectManager.LOG_STRIP_EFFECT_ID, x, y, z, 0);
        }
        int metadata = world.getBlockMetadata(x, y, z);

        world.setBlockWithNotify(x, y, z, TreeFellerAddon.choppedLogBlock.blockID);

        if (world.getBlockTileEntity(x, y, z) instanceof ChoppedLogTileEntity logTileEntity) {
            logTileEntity.scanAndSaveConnectedLogs();
            logTileEntity.setLogType(blockID, metadata);
            logTileEntity.resetRequiredBreaks();
        }
    }
}
