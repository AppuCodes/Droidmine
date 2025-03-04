package net.droidmine.pathfinder.walk.target.impl;

import net.droidmine.pathfinder.path.PathElm;
import net.droidmine.pathfinder.path.impl.FallNode;
import net.droidmine.pathfinder.walk.target.WalkTarget;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class FallTarget extends WalkTarget {
    private int stuck = 0;
    FallNode node;

    public FallTarget(FallNode node) {
        this.node = node;
    }
    @Override
    public boolean tick(Vec3 predictedMotionOnStop, Vec3 playerPos, World world) {
        setCurrentTarget(node.getBlockPos());

        playerPos = new Vec3(playerPos.xCoord, 0, playerPos.zCoord);
        Vec3 dest = new Vec3(node.getX(), 0, node.getZ()).addVector(0.5d, 0d, 0.5d);
        double predicatedPositionDistance = playerPos.distanceTo(playerPos.add(predictedMotionOnStop));
        double destPositionDistance = playerPos.distanceTo(dest);

        double angle = calculateAnglePredictionDest(predictedMotionOnStop, dest.subtract(playerPos));
        return (predicatedPositionDistance > destPositionDistance   && angle < PREDICTED_MOTION_ANGLE) || FallNode.toBlockPos(playerPos).equals(FallNode.toBlockPos(dest)) || playerPos.equals(dest) || stuck > 20;
    }

    public BlockPos getNodeBlockPos() {
        return node.getBlockPos();
    }

    public PathElm getElm() {
        return node;
    }
}
