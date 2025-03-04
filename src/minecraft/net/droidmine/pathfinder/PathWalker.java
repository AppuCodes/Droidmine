package net.droidmine.pathfinder;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.droidmine.pathfinder.astar.AStarNode;
import net.droidmine.pathfinder.astar.AStarPathFinder;
import net.droidmine.pathfinder.path.PathElm;
import net.droidmine.pathfinder.path.impl.*;
import net.droidmine.pathfinder.processor.ProcessorManager;
import net.droidmine.pathfinder.walk.target.WalkTarget;
import net.droidmine.pathfinder.walk.target.impl.*;
import net.minecraft.client.ClientEngine;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

public class PathWalker
{
    private Entity player, followEntity;
    private boolean active;
    private ClientEngine mc;
    private List<PathElm> path;
    private WalkTarget currentTarget;

    /** Makes the player walk to a specified destination. */
    public void goTo(BlockPos destination)
    {
        if (active) cancel();
        this.player = mc.player;
        active = true;
        int i = 128;

        while (i-- > 0)
        {
            if (player.worldObj.getBlockState(destination.add(0, -1, 0)).getBlock().equals(Blocks.air))
                destination = destination.add(0, -1, 0);
        }

        List<AStarNode> nodes = AStarPathFinder.compute(player.getPosition(), destination, 10000, player.worldObj);
        path = ProcessorManager.process(nodes, player.worldObj);

        if (path.size() == 0) {
            active = false;
            currentTarget = null;
            return;
        }

        currentTarget = null;
    }
    
    /** Makes the player follow an entity. */
    public void follow(Entity entity)
    {
        if (active) cancel();
        followEntity = entity;
        goTo(entity.getPosition());
    }
    
    public void tick()
    {
        if (!active || path == null || path.isEmpty())
            return;

        if (currentTarget == null)
            currentTarget = getCurrentTarget(path.get(0));

        WalkTarget playerOnTarget;

        if (!((playerOnTarget = onTarget(player)) == null))
            currentTarget = playerOnTarget;

        // while, so we don't skip ticks
        while (tick(currentTarget, player)) {
            // removes it
            path.remove(0);

            if (path.isEmpty())
            {
                active = false;
                currentTarget = null;
                KeyBinding.setKeyBindState(Keyboard.KEY_W, false);
                KeyBinding.setKeyBindState(Keyboard.KEY_A, false);
                KeyBinding.setKeyBindState(Keyboard.KEY_D, false);
                KeyBinding.setKeyBindState(Keyboard.KEY_S, false);

                if (followEntity != null)
                {
                    goTo(followEntity.getPosition());
                }

                return;
            }

            currentTarget = getCurrentTarget(path.get(0));
        }

        KeyBinding.setKeyBindState(Keyboard.KEY_LCONTROL, true);
        Tuple<Double, Double> angles = getAngles(player, currentTarget.getCurrentTarget());
        player.rotationYaw = angles.getFirst().floatValue();
        player.rotationPitch = currentTarget instanceof JumpTarget ? -10 : 10;
        pressKeys(angles.getFirst().floatValue(), player);
    }

    public static Tuple<Double, Double> getAngles(Entity player, BlockPos pos) {
        return getAngles(player.getPositionVector().addVector(0, player.getEyeHeight(), 0), new Vec3(pos).addVector(0.5f, 0.5f, 0.5f), player.rotationYaw);
    }

    public static Tuple<Double, Double> getAngles(Vec3 origin, Vec3 point, double currentYaw) {
        double dx = origin.xCoord - point.xCoord;
        double dy = origin.yCoord - point.yCoord;
        double dz = origin.zCoord - point.zCoord;
        double dist = Math.sqrt(dx*dx + dz*dz);

        if(dist == 0)
            return new Tuple<>(0.0, 0.0);

        double pitch = 90 - Math.toDegrees(Math.atan(dist/Math.abs(dy)));
        if (dy < 0)
            pitch = -pitch;

        double angle =  Math.toDegrees(Math.atan(Math.abs(dx/dz)));
        double yaw;
        if(dx > 0 && dz < 0)
            yaw = angle;
        else if(dx > 0 && dz > 0)
            yaw = 180 - angle;
        else if (dx < 0 && dz > 0)
            yaw = 180 + angle;
        else
            yaw = 360 - angle;

        double diff = yaw - currentYaw;

        while (diff > 180) {
            yaw -= 360;
            diff = yaw - currentYaw;
        }

        while (diff < -180) {
            yaw += 360;
            diff = yaw - currentYaw;
        }

        return new Tuple<>(yaw, pitch);
    }

    private void pressKeys(double targetYaw, Entity player) {
        double difference = targetYaw - player.rotationYaw;
        KeyBinding.setKeyBindState(Keyboard.KEY_W, false);
        KeyBinding.setKeyBindState(Keyboard.KEY_A, false);
        KeyBinding.setKeyBindState(Keyboard.KEY_S, false);
        KeyBinding.setKeyBindState(Keyboard.KEY_D, false);

        if(22.5 > difference && difference > -22.5) {   // Forwards

            KeyBinding.setKeyBindState(Keyboard.KEY_W, true);
        } else if(-22.5 > difference && difference > -67.5) {   // Forwards+Right

            KeyBinding.setKeyBindState(Keyboard.KEY_W, true);
            KeyBinding.setKeyBindState(Keyboard.KEY_A, true);
        } else if(-67.5 > difference && difference > -112.5) { // Right

            KeyBinding.setKeyBindState(Keyboard.KEY_A, true);
        } else if(-112.5 > difference && difference > -157.5) { // Backwards + Right

            KeyBinding.setKeyBindState(Keyboard.KEY_A, true);
            KeyBinding.setKeyBindState(Keyboard.KEY_S, true);
        } else if((-157.5 > difference && difference > -180) || (180 > difference && difference > 157.5)) { // Backwards

            KeyBinding.setKeyBindState(Keyboard.KEY_S, true);
        } else if(67.5 > difference && difference > 22.5) { // Forwards + Left

            KeyBinding.setKeyBindState(Keyboard.KEY_W, true);
            KeyBinding.setKeyBindState(Keyboard.KEY_D, true);

        } else if(112.5 > difference && difference > 67.5) { // Left

            KeyBinding.setKeyBindState(Keyboard.KEY_D, true);
        } else if(157.5 > difference && difference > 112.5) {  // Backwards+Left

            KeyBinding.setKeyBindState(Keyboard.KEY_S, true);
            KeyBinding.setKeyBindState(Keyboard.KEY_D, true);
        }
    }

    // This checks if the player is on any nodes further in the queue, which means the player, due to probably high speed, has skipped some. Then
    // this removes the nodes behind it and sets it as the current target.
    private WalkTarget onTarget(Entity player) {
        for(int i = 0 ; i < path.size() ; i++) {
            PathElm elm = path.get(i);

            if(elm.playerOn(player.getPositionVector())) {
                if(elm == currentTarget.getElm())
                    return null;

                // Get the next one if the player is on it
                // if its travel vector, we don't get the next one, cos we need to go to the dest.
                // if its jump, we don't get the next one, cos we need to jump.
                if(path.size() > i + 1 && !(elm instanceof TravelVector) && !(elm instanceof JumpNode)) {
                    path.subList(0, i + 1).clear();
                } else {
                    path.subList(0, i).clear();
                }

                // cutting off might end jump target so stop jumping
                KeyBinding.setKeyBindState(Keyboard.KEY_SPACE, false);
                return getCurrentTarget(path.get(0));
            }
        }

        return null;
    }

    // The return value of this is if the node has been satisfied, and the next one should be polled.
    private boolean tick(WalkTarget current, Entity player)
    {
        Vec3 offset = new Vec3(player.motionX, 0, player.motionZ);
        Vec3 temp = offset;
        offset.add(temp);

        for(int i = 0 ; i < 12 ; i++) {
            // How much the motion stops after every tick after not moving.
            offset = offset.add(temp = temp.scale(0.54600006f));
        }

        return current.tick(offset, player.getPositionVector(), player.worldObj);
    }

    private WalkTarget getCurrentTarget(PathElm elm) {
        if(elm instanceof FallNode)
            return new FallTarget((FallNode) elm);
        if(elm instanceof TravelNode)
            return new TravelTarget((TravelNode) elm);
        if(elm instanceof TravelVector)
            return new TravelVectorTarget((TravelVector) elm);
        if(elm instanceof JumpNode) {
            if(path.size() > 1)
                return new JumpTarget((JumpNode) elm, getCurrentTarget(path.get(1)));
            return new JumpTarget((JumpNode) elm, null);
        }

        return null;
    }

    /** cancels any pathfinding action */
    public void cancel()
    {
        active = false;
        currentTarget = null;
        followEntity = null;
        path = null;
    }

    public boolean isActive()
    {
        return active;
    }

    public PathWalker(ClientEngine mc) { this.mc = mc; }
}
