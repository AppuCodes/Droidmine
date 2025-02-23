package net.droidmine.pathfinder.walk.target.impl;

import net.droidmine.pathfinder.path.PathElm;
import net.droidmine.pathfinder.path.impl.FallNode;
import net.droidmine.pathfinder.path.impl.TravelNode;
import net.droidmine.pathfinder.walk.target.WalkTarget;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TravelTarget extends WalkTarget {

    TravelNode node;
    public TravelTarget(TravelNode node) {
        this.node = node;
    }

    @Override
    public boolean tick(Vec3 predictedMotionOnStop, Vec3 playerPos, World world) {
        setCurrentTarget(node.getBlockPos());

        Vec3 dest = new Vec3(node.getBlockPos()).addVector(0.5d, 0d, 0.5d);
        double predicatedPositionDistance = playerPos.distanceTo(playerPos.add(predictedMotionOnStop));
        double destPositionDistance = playerPos.distanceTo(dest);

        double angle = calculateAnglePredictionDest(predictedMotionOnStop, dest.subtract(playerPos));

        return (predicatedPositionDistance > destPositionDistance && angle < PREDICTED_MOTION_ANGLE) || new BlockPos(Math.floor(playerPos.xCoord), Math.floor(playerPos.yCoord), Math.floor(playerPos.zCoord)).equals(FallNode.toBlockPos(dest));
    }

    public BlockPos getNodeBlockPos() {
        return node.getBlockPos();
    }

    public PathElm getElm() {
        return node;
    }
}
