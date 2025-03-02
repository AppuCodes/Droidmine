package net.droidmine.pathfinder.walk.target.impl;

import net.droidmine.pathfinder.path.PathElm;
import net.droidmine.pathfinder.path.impl.FallNode;
import net.droidmine.pathfinder.path.impl.TravelVector;
import net.droidmine.pathfinder.walk.target.WalkTarget;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TravelVectorTarget extends WalkTarget {
    private int stuck = 0;
    TravelVector node;
    boolean baseReached = false;

    public TravelVectorTarget(TravelVector node) {
        this.node = node;
    }

    @Override
    public boolean tick(Vec3 predictedMotionOnStop, Vec3 playerPos, World world) {
        stuck++;
        if(!baseReached) {
            // this means that the player is already on the vector and is suitable for skipping the base node.
            if(node.playerOn(playerPos)) {
                baseReached = true;
            }
        }

        BlockPos destBlockPos = baseReached ? node.getTo().getBlockPos() : node.getFrom().getBlockPos();

        setCurrentTarget(destBlockPos);
        Vec3 dest = new Vec3(destBlockPos).addVector(0.5d, 0d, 0.5d);

        double predicatedPositionDistance = playerPos.distanceTo(playerPos.add(predictedMotionOnStop));
        double destPositionDistance = playerPos.distanceTo(dest);
        double angle = calculateAnglePredictionDest(predictedMotionOnStop, dest.subtract(playerPos));

        if(((predicatedPositionDistance > destPositionDistance && angle < PREDICTED_MOTION_ANGLE) || new BlockPos(Math.floor(playerPos.xCoord), Math.floor(playerPos.yCoord), Math.floor(playerPos.zCoord)).equals(FallNode.toBlockPos(dest)))
                || playerPos.equals(dest) || stuck > 20) {
            if(!baseReached) {
                baseReached = true;
                setCurrentTarget(node.getTo().getBlockPos());
            }
            else
                return true;
        }

        return false;
    }

    public BlockPos getNodeBlockPos() {
        return node.getFrom().getBlockPos();
    }

    public PathElm getElm() {
        return node;
    }

}
