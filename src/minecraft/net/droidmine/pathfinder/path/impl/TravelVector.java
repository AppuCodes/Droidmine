package net.droidmine.pathfinder.path.impl;

import net.droidmine.pathfinder.path.Node;
import net.droidmine.pathfinder.path.PathElm;
import net.minecraft.util.Vec3;

public class TravelVector implements PathElm {

    private final Node from;
    private final Node to;

    public TravelVector(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public static double calculateAngleVec2D(Vec3 one, Vec3 two) {
        one = new Vec3(one.xCoord, 0, one.zCoord);
        two = new Vec3(two.xCoord, 0, two.zCoord);

        double oneMagnitude = one.distanceTo(new Vec3(0, 0, 0));
        double twoMagnitude = two.distanceTo(new Vec3(0, 0, 0));

        double deg = Math.toDegrees(Math.acos(one.dotProduct(two)/(oneMagnitude*twoMagnitude)));
        if(Double.isNaN(deg)) {

            //vector should be going in the opposite direction
            return 180;
        }
        return deg;
    }

    @Override
    public boolean playerOn(Vec3 playerPos) {
        Vec3 fromVec = new Vec3(from.getBlockPos()).add(new Vec3(0.5, 0, 0.5));
        Vec3 toVec = new Vec3(to.getBlockPos()).add(new Vec3(0.5, 0, 0.5));

        Vec3 travelVec = toVec.subtract(fromVec);
        Vec3 playerVecFrom = playerPos.subtract(fromVec);

        double playerMagnitude = playerVecFrom.distanceTo(new Vec3(0, 0, 0));
        double destMagnitude = travelVec.distanceTo(new Vec3(0, 0, 0));
        double angle = calculateAngleVec2D(travelVec, playerVecFrom);

        // if the magnitude of the vector of the base to the player is greater than the magnitude of the vector of the base to the dest, it is clear that it has already been exceeded.
        // Then if the magnitude is less and the two vectors are more or less in the same direction we can tell the player is on the path element.

        if(playerMagnitude <= destMagnitude && angle < 20) {
            return true;
        }
        return false;
    }
}
