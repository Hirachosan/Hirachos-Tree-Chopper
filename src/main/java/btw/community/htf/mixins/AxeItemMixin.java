package btw.community.htf.mixins;

import api.item.items.AxeItem;
import btw.community.htf.classes.LogBlockExtension;
import net.minecraft.src.Block;
import net.minecraft.src.BlockLog;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public class AxeItemMixin {

    @Inject(method = "canHarvestBlock", at = @At("HEAD"), cancellable = true)
    private void checkValidTreeChoppingLog(ItemStack stack, World world, Block block, int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (block instanceof LogBlockExtension log && log.htf$getIsValidTreeChoppingLog(world, i, j, k)){
            cir.setReturnValue(false);
        }
    }
}
