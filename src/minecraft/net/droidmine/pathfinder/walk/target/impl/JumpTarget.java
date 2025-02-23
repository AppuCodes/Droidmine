package net.droidmine.pathfinder.walk.target.impl;

import org.lwjgl.input.Keyboard;

import net.droidmine.pathfinder.astar.AStarNode;
import net.droidmine.pathfinder.path.PathElm;
import net.droidmine.pathfinder.path.impl.JumpNode;
import net.droidmine.pathfinder.walk.target.WalkTarget;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class JumpTarget extends WalkTarget {

    JumpNode node;
    WalkTarget next;

    boolean originalYSet;
    int originalY;
    int wait = 0;

    public JumpTarget(JumpNode node, WalkTarget next) {
        this.node = node;
        this.next = next;
    }

    @Override
    public boolean tick(Vec3 predictedMotionOnStop, Vec3 playerPos, World world) {
        if(!originalYSet) {
            originalYSet = true;
            originalY = (int)playerPos.yCoord;
        }
        // last one
        if(next == null)
            return true;

        setCurrentTarget(next.getNodeBlockPos());

        wait++;

        // change value and stuff
        if(wait < 2)
            return false;

        KeyBinding.setKeyBindState(Keyboard.KEY_SPACE, true);

        if((int)playerPos.yCoord - originalY == 1 && AStarNode.isBlockSolid(new BlockPos(playerPos).subtract(new Vec3i(0, 1, 0)), world)) {
            KeyBinding.setKeyBindState(Keyboard.KEY_SPACE, false);
            return true;
        }

        return false;
    }

    public BlockPos getNodeBlockPos() {
        return node.getBlockPos();
    }

    public PathElm getElm() {
        return node;
    }
}
