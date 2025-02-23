package net.droidmine.pathfinder.path.impl;

import net.droidmine.pathfinder.path.Node;
import net.droidmine.pathfinder.path.PathElm;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class FallNode extends Node implements PathElm {
    public FallNode(int x, int y, int z) {
        super(x, y, z);
    }

    @Override
    public boolean playerOn(Vec3 playerPos) {
        return toBlockPos(playerPos).equals(getBlockPos());
    }

    public static BlockPos toBlockPos(Vec3 vec) {
        return new BlockPos(Math.floor(vec.xCoord), Math.floor(vec.yCoord), Math.floor(vec.zCoord));
    }
}
