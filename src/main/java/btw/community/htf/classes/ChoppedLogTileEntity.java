package btw.community.htf.classes;

import api.world.ImmutableBlockPos;
import net.minecraft.src.Block;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ChoppedLogTileEntity extends TileEntity {
    public final int MAX_CONNECTED_LOGS = 255;
    public Set<ImmutableBlockPos> connectedLogs;
    public int totalRequiredBreaks;
    public int requiredBreaksLeft;
    private int logBlockID;
    private int logBlockMetadata;

    public ChoppedLogTileEntity() {
        super();
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        totalRequiredBreaks = tag.getInteger("requiredBreaks");
        requiredBreaksLeft = tag.getInteger("breaksLeft");
        logBlockID = tag.getInteger("logBlockID");
        logBlockMetadata = tag.getInteger("logBlockMetadata");

    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        tag.setInteger("requiredBreaks", totalRequiredBreaks);
        tag.setInteger("breaksLeft", requiredBreaksLeft);
        tag.setInteger("logBlockID", logBlockID);
        tag.setInteger("logBlockMetadata", logBlockMetadata);
    }

    @Override
    public void updateEntity() {

    }

    //------------- Class Specific Methods ------------//

    public void scanAndSaveConnectedLogs() {
        ImmutableBlockPos currentPos = new ImmutableBlockPos(xCoord, yCoord, zCoord);
        Queue<ImmutableBlockPos> queue = new LinkedList<>();
        Set<ImmutableBlockPos> visited = new HashSet<>();

        queue.add(currentPos);
        visited.add(currentPos);

        while (!queue.isEmpty() && visited.size() <= MAX_CONNECTED_LOGS) {
            currentPos = queue.remove();

            for (ImmutableBlockPos neighbor : get26Directions(currentPos)) {
                if (!visited.contains(neighbor)) {
                    Block  neighborBlock = Block.blocksList[ worldObj.getBlockId(neighbor.getX(), neighbor.getY(), neighbor.getZ())];

                    if (neighborBlock instanceof BlockExtension log && log.htf$isHarvestableLog(worldObj, neighbor.getX(), neighbor.getY(), neighbor.getZ())) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }

            this.connectedLogs = visited;
            this.totalRequiredBreaks = visited.size() - 1;
        }
    }

    ImmutableBlockPos[] get6Directions(ImmutableBlockPos pos) {

        return new ImmutableBlockPos[] {pos.up(), pos.down(), pos.north(), pos.south(), pos.east(), pos.west()};
    }

    Set<ImmutableBlockPos> get26Directions(ImmutableBlockPos pos) {
        Set<ImmutableBlockPos> neighbors = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    neighbors.add(pos.add(dx, dy, dz));
                }
            }
        }

        return neighbors;
    }

    public void setLogType(int blockID, int metadata) {
        this.logBlockID = blockID;
        this.logBlockMetadata = metadata;
    }

    public int getLogBlockID() {
        return logBlockID;
    }

    public int getLogBlockMetadata() {
        return logBlockMetadata;
    }

    public void resetRequiredBreaks() {
        requiredBreaksLeft = totalRequiredBreaks;
    }
}

