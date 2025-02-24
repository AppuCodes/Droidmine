package net.droidmine.pathfinder.astar;

import net.minecraft.block.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class AStarNode {
    private double hCost;
    private int gCost;

    private final int x;
    private final int y;
    private final int z;

    private AStarNode parent;
    private BlockPos blockPos;

    private boolean isJumpNode;

    private boolean isFallNode;
    private World world;

    public AStarNode(BlockPos pos, AStarNode parentNode, AStarNode endNode, World world) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.blockPos = pos;
        this.world = world;

        calculateHeuristic(endNode);
        setParent(parentNode);
    }

    public AStarNode(int xRel, int yRel, int zRel, AStarNode parentNode, AStarNode endNode, World world) {
        this.x = xRel + parentNode.getX();
        this.y = yRel + parentNode.getY();
        this.z = zRel + parentNode.getZ();
        this.blockPos = new BlockPos(x, y, z);
        this.world = world;

        calculateHeuristic(endNode);
        setParent(parentNode);
    }

    public AStarNode(BlockPos pos, AStarNode endNode, World world) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.world = world;
        this.blockPos = pos;

        calculateHeuristic(endNode);
    }

    public AStarNode(BlockPos pos, World world) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.world = world;
    }

    public boolean canBeTraversed() {
        if(isBlockSolid(blockPos, world) || isBlockSolid(new BlockPos(x, y + 1, z), world))
            return false;

        // fall node and not falling return false
        if(parent.isFallNode() && parent.getY() == y)
            return false;

        if(isBlockSolid(new BlockPos(x, y - 1, z), world))
            return true;


        if(parent == null) {
            return false;
        }

        // jump
        if(parent.blockPos.getY() - 1 == y - 2 && isBlockSolid(new BlockPos(x, y - 2, z), world)) {
            setJumpNode(true);
            return true;
        }

        // Since we already know the block directly under this node is not solid due to the guard clause above, assume still falling
        if(parent.isFallNode() && y == parent.getY() - 1) {
            setFallNode(true);
            return true;
        }

        // fall origin
        if(parent.blockPos.getY() == y && isBlockSolid(new BlockPos(parent.blockPos.getX(), parent.blockPos.getY() - 1, parent.blockPos.getZ()), world)) {
            setFallNode(true);
            return true;
        }

        return false;
    }

    public static boolean isBlockSolid(BlockPos block, World world) {
        return world.getBlockState(block)
                .getBlock().isBlockSolid(world, block, null) ||
                world.getBlockState(block).getBlock() instanceof BlockSlab ||
                world.getBlockState(block).getBlock() instanceof BlockStainedGlass ||
                world.getBlockState(block).getBlock() instanceof BlockPane ||
                world.getBlockState(block).getBlock() instanceof BlockFence ||
                world.getBlockState(block).getBlock() instanceof BlockPistonExtension ||
                world.getBlockState(block).getBlock() instanceof BlockEnderChest ||
                world.getBlockState(block).getBlock() instanceof BlockTrapDoor ||
                world.getBlockState(block).getBlock() instanceof BlockPistonBase ||
                world.getBlockState(block).getBlock() instanceof BlockChest ||
                world.getBlockState(block).getBlock() instanceof BlockStairs ||
                world.getBlockState(block).getBlock() instanceof BlockCactus ||
                world.getBlockState(block).getBlock() instanceof BlockWall ||
                world.getBlockState(block).getBlock() instanceof BlockGlass ||
                world.getBlockState(block).getBlock() instanceof BlockSkull ||
                world.getBlockState(block).getBlock() instanceof BlockSand;
    }
    private void calculateHeuristic(AStarNode endNode) {
        this.hCost = (Math.abs(endNode.getX() - x) + Math.abs(endNode.getY() - y) + Math.abs(endNode.getZ() - z)) * 10;

    }

    public void setParent(AStarNode parent) {
        this.parent = parent;

        int xDiff = Math.abs(x - parent.getX());
        int yDiff = Math.abs(y - parent.getY());
        int zDiff = Math.abs(z - parent.getZ());


        this.gCost = parent.getGCost() + (xDiff + yDiff + zDiff) * 10;
    }

    public double getTotalCost() {
        return hCost + gCost;
    }




    public int getX() {
        return x;
    }

    public int getGCost() {
        return gCost;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public AStarNode getParent() {
        return parent;
    }

    public Vec3 asVec3(double xAdd, double yAdd, double zAdd) {
        return new Vec3(x + xAdd, y + yAdd, z + zAdd);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof AStarNode))
            return false;

        AStarNode other = (AStarNode)o;

        return x == other.getX() && y == other.getY() && z == other.getZ();
    }

    public void setJumpNode(boolean jumpNode) {
        isJumpNode = jumpNode;
    }

    public void setFallNode(boolean fallNode) {
        isFallNode = fallNode;
    }

    public boolean isFallNode() {
        return isFallNode;
    }

    public boolean isJumpNode() {
        return isJumpNode;
    }
}


