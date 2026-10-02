package btw.community.htf.classes;


import api.block.util.Flammability;
import api.item.util.ItemUtils;
import api.world.BlockPos;
import api.world.ImmutableBlockPos;
import btw.block.BTWBlocks;
import btw.block.blocks.ChewedLogBlock;
import btw.block.model.BlockModel;
import btw.client.fx.BTWEffectManager;
import btw.community.htf.TreeFellerAddon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockLog;
import net.minecraft.src.Explosion;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ITileEntityProvider;
import net.minecraft.src.Icon;
import net.minecraft.src.IconRegister;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;

import java.util.Random;

public class ChoppedLogBlock extends Block implements ITileEntityProvider {
    private BlockModel[] blockModels;
    private AxisAlignedBB[] boxSelectionArray;

    private BlockModel currentModel;
    private ChewedLogBlock referenceBlock;


    public ChoppedLogBlock(int blockID, ChewedLogBlock chewedReference) {
        super(blockID, BTWBlocks.plankMaterial);

        setHardness(1.25F); // vanilla 2
        setResistance(3.33F);  // odd value to match vanilla resistance set through hardness of 2

        setAxesEffectiveOn();

        setBuoyant();

        setFireProperties(Flammability.LOGS);

        Block.useNeighborBrightness[blockID] = true;
        setLightOpacity(8);

        setStepSound(soundWoodFootstep);

        setUnlocalizedName("fcBlockChoppedTree");

        this.referenceBlock = chewedReference;

        initModels();
    }

    //TODO needs some way to reference a parent block id for the wood chip texture

    @Override
    public int quantityDropped(Random par1Random) {
        return 0;
    }

    @Override
    public int idDropped(int par1, Random par2Random, int par3) {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return new ChoppedLogTileEntity();
    }

    @Override
    public int getHarvestToolLevel(IBlockAccess blockAccess, int x, int y, int z) {
        return 1000; // always convert
    }

    @Override
    public boolean canConvertBlock(ItemStack stack, World world, int i, int j, int k) {
        return true;
    }

    @Override
    public boolean convertBlock(ItemStack stack, World world, int x, int y, int z, int fromSide) {
        if (!world.isRemote && world.getBlockTileEntity(x, y, z) instanceof ChoppedLogTileEntity logTileEntity) {
            logTileEntity.scanAndSaveConnectedLogs();

            if(logTileEntity.requiredBreaksLeft > logTileEntity.totalRequiredBreaks) {
                logTileEntity.requiredBreaksLeft = logTileEntity.totalRequiredBreaks;
            }

            if(logTileEntity.requiredBreaksLeft > 1) {
                logTileEntity.requiredBreaksLeft--;

                world.playAuxSFX(BTWEffectManager.LOG_STRIP_EFFECT_ID, x, y, z, 0);

                int newDamage = calculateLogLeft(logTileEntity.requiredBreaksLeft, logTileEntity.totalRequiredBreaks);

                int currentDamage = world.getBlockMetadata(x, y, z) & 3;

                if(newDamage != currentDamage) {
                    world.setBlockMetadataWithNotify(x, y, z, newDamage);
                }

            }
            else {
                return false;
            }
        }

        return true;
    }

    private int calculateLogLeft(int requiredBreaksLeft, int totalRequiredBreaks) {
        if (totalRequiredBreaks < 1) {
            totalRequiredBreaks = requiredBreaksLeft;
        }
        double  percentage = (requiredBreaksLeft / (double) totalRequiredBreaks);

        int bucket = (int) ((1 - percentage) * 4);

        return Math.min(3, Math.max(0, bucket));
    }

    @Override
    public void breakBlock( World world, int x, int y, int z, int iBlockID, int iMetadata ) {
        super.breakBlock(world, x, y, z, iBlockID, iMetadata);

        if (!world.isRemote && world.getBlockTileEntity(x, y, z) instanceof ChoppedLogTileEntity logTileEntity) {
            ItemUtils.dropSingleItemAsIfBlockHarvested(world, x, y, z, logTileEntity.getLogBlockID(), logTileEntity.getLogBlockMetadata());

            logTileEntity.scanAndSaveConnectedLogs();

            for (ImmutableBlockPos pos : logTileEntity.connectedLogs) {
                int connectedBlockID = world.getBlockId(pos.getX(), pos.getY(), pos.getZ());
                int connectedMetadata = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());

                if (Block.blocksList[connectedBlockID] instanceof BlockLog logBlock) {
                    world.playAuxSFX(BTWEffectManager.BLOCK_BREAK_EFFECT_ID, pos.getX(), pos.getY(), pos.getZ(), 0);

                    logBlock.dropBlockAsItem(world, pos.getX(), pos.getY(), pos.getZ(), connectedMetadata, 0);

                    world.setBlockToAir(pos.getX(), pos.getY(), pos.getZ());
                }
            }

        }

        world.removeBlockTileEntity(x, y, z);
    }


    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosion) {
        return false;
    }


    //	public void onBlockPlacedBy( World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack ) {
//		if (world.getBlockTileEntity(x, y, z) instanceof ChoppedLogTileEntity logTileEntity) {
//			logTileEntity.scanAndSaveConnectedLogs();
//		}

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int i, int j, int k, Vec3 startRay, Vec3 endRay) {
        setCurrentModelForBlock(world, i, j, k);

        return currentModel.collisionRayTrace(world, i, j, k, startRay, endRay);
    }


    //------------- Class Specific Methods ------------//
    private final static float RIM_WIDTH = (1F / 16F);

    private final static float LAYER_HEIGHT = (2F / 16F);
    private final static float FIRST_LAYER_HEIGHT = (3F / 16F);
    private final static float LAYER_WIDTH_GAP = (1F / 16F);

    protected void initModels() {
        blockModels = new BlockModel[4];

        boxSelectionArray = new AxisAlignedBB[4];

        // center colum

        for (int iTempIndex = 0; iTempIndex < 4; iTempIndex++) {
            BlockModel tempModel = blockModels[iTempIndex] = new BlockModel();

            float fCenterColumnWidthGap = RIM_WIDTH + (LAYER_WIDTH_GAP * iTempIndex);
            float fCenterColumnHeightGap = 0F;

            if (iTempIndex > 0) {
                fCenterColumnHeightGap = FIRST_LAYER_HEIGHT + (LAYER_HEIGHT * (iTempIndex - 1));
            }

            tempModel.addBox(fCenterColumnWidthGap, fCenterColumnHeightGap, fCenterColumnWidthGap, 1F - fCenterColumnWidthGap, 1F - fCenterColumnHeightGap,
                    1F - fCenterColumnWidthGap);

            AxisAlignedBB tempSelection = boxSelectionArray[iTempIndex] =
                    new AxisAlignedBB(fCenterColumnWidthGap, 0, fCenterColumnWidthGap, 1F - fCenterColumnWidthGap, 1F, 1F - fCenterColumnWidthGap);
        }

        // first layer

        for (int iTempIndex = 1; iTempIndex < 4; iTempIndex++) {
            // bottom
            blockModels[iTempIndex].addBox(RIM_WIDTH, 0, RIM_WIDTH, 1F - RIM_WIDTH, FIRST_LAYER_HEIGHT, 1F - RIM_WIDTH);

            // top
            blockModels[iTempIndex].addBox(RIM_WIDTH, 1F - FIRST_LAYER_HEIGHT, RIM_WIDTH, 1F - RIM_WIDTH, 1F, 1F - RIM_WIDTH);

        }

        // second layer
        float fWidthGap = RIM_WIDTH + LAYER_WIDTH_GAP;
        float fHeightGap = FIRST_LAYER_HEIGHT;

        for (int iTempIndex = 2; iTempIndex < 4; iTempIndex++) {
            // second layer bottom
            blockModels[iTempIndex].addBox(fWidthGap, fHeightGap, fWidthGap, 1F - fWidthGap, fHeightGap + LAYER_HEIGHT, 1F - fWidthGap);

            // second layer top
            blockModels[iTempIndex].addBox(fWidthGap, 1F - fHeightGap - LAYER_HEIGHT, fWidthGap, 1F - fWidthGap, 1F - fHeightGap, 1F - fWidthGap);
        }

        // third layer bottom

        fWidthGap = RIM_WIDTH + (LAYER_WIDTH_GAP * 2);
        fHeightGap = FIRST_LAYER_HEIGHT + LAYER_HEIGHT;

        blockModels[3].addBox(fWidthGap, fHeightGap, fWidthGap, 1F - fWidthGap, fHeightGap + LAYER_HEIGHT, 1F - fWidthGap);

        // third layer top
        blockModels[3].addBox(fWidthGap, 1F - fHeightGap - LAYER_HEIGHT, fWidthGap, 1F - fWidthGap, 1F - LAYER_HEIGHT, 1F - fWidthGap);
    }

    public void setCurrentModelForBlock(IBlockAccess blockAccess, int i, int j, int k) {
        int iDamageLevel = getDamageLevel(blockAccess, i, j, k);

        currentModel = blockModels[iDamageLevel];
    }

    public int getDamageLevel(IBlockAccess blockAccess, int i, int j, int k) {
        return getDamageLevel(blockAccess.getBlockMetadata(i, j, k));
    }

    public int getDamageLevel(int iMetadata) {
        return iMetadata & 3;
    }

    //------------ Client Side Functionality ----------//

    @Environment(EnvType.CLIENT)
    private Icon iconSide;

    @Environment(EnvType.CLIENT)
    public void registerIcons(IconRegister register) {
        blockIcon = register.registerIcon("btw:stripped_oak_log_top");
        iconSide = register.registerIcon("btw:stripped_oak_log");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Icon getIcon(int iSide, int iMetadata) {
        return iSide >= 2 ? iconSide : blockIcon;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess blockAccess, int iNeighborI, int iNeighborJ, int iNeighborK, int iSide) {
        return currentBlockRenderer.shouldSideBeRenderedBasedOnCurrentBounds(iNeighborI, iNeighborJ, iNeighborK, iSide);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean renderBlock(RenderBlocks renderBlocks, int x, int y, int z) {
        setCurrentModelForBlock(renderBlocks.blockAccess, x, y, z);

        currentModel.renderAsBlock(renderBlocks, this, x, y, z);
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k) {
        int iDamageLevel = getDamageLevel(world, i, j, k);

        AxisAlignedBB tempSelectionBox = boxSelectionArray[iDamageLevel].makeTemporaryCopy();

        return tempSelectionBox.offset(i, j, k);
    }

}
