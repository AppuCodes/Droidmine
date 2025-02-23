package net.droidmine.pathfinder.path.impl;

import net.droidmine.pathfinder.path.Node;
import net.droidmine.pathfinder.path.PathElm;
import net.minecraft.util.Vec3;

public class TravelNode extends Node implements PathElm {

    public TravelNode(int x, int y, int z) {
        super(x, y, z);
    }

    @Override
    public boolean playerOn(Vec3 playerPos) {
        return FallNode.toBlockPos(playerPos).equals(getBlockPos());
    }
}
