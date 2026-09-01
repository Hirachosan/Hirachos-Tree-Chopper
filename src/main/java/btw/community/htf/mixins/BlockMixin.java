package btw.community.htf.mixins;

import btw.community.htf.classes.BlockExtension;
import net.minecraft.src.Block;
import net.minecraft.src.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Block.class)
public abstract class BlockMixin implements BlockExtension {

	@Shadow public abstract boolean isLog(IBlockAccess blockAccess, int x, int y, int z);

	@Override
	public boolean htf$isHarvestableLog(IBlockAccess blockAccess, int x, int y, int z) {
		return isLog(blockAccess, x, y, z);
	}
}
