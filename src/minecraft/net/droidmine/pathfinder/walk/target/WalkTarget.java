package net.droidmine.pathfinder.walk.target;

import net.droidmine.pathfinder.path.PathElm;
import net.droidmine.pathfinder.path.impl.TravelVector;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class WalkTarget {

    protected final int PREDICTED_MOTION_ANGLE = 20;
    private BlockPos currentTarget;
    public abstract boolean tick(Vec3 predictedMotionOnStop, Vec3 playerPos, World world);

    public BlockPos getCurrentTarget() {
        return currentTarget;
    }
    protected void setCurrentTarget(BlockPos target) {
        this.currentTarget = target;
    }

    protected double calculateAnglePredictionDest(Vec3 predictedVec, Vec3 destVec) {
        return TravelVector.calculateAngleVec2D(predictedVec, destVec);
    }

    public abstract BlockPos getNodeBlockPos();

    public abstract PathElm getElm();
}


