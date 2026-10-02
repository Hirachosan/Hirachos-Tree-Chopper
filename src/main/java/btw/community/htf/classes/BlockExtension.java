package btw.community.htf.classes;

import net.minecraft.src.IBlockAccess;

public interface BlockExtension {
    boolean htf$isHarvestableLog(IBlockAccess blockAccess, int x, int y, int z);
}
