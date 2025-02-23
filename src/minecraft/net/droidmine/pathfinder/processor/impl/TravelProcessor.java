package net.droidmine.pathfinder.processor.impl;

import java.util.ArrayList;
import java.util.List;

import net.droidmine.pathfinder.astar.AStarNode;
import net.droidmine.pathfinder.path.PathElm;
import net.droidmine.pathfinder.path.impl.*;
import net.droidmine.pathfinder.processor.Processor;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class TravelProcessor extends Processor {

    // Here we detect paths on the same y level with ray trace to shorten the route (travel nodes)
    @Override
    public void process(List<PathElm> elms, World world) {
        List<PathElm> newPath = new ArrayList<>();

        PathIter:
        for(int a = 0 ; a < elms.size() ; a++) {
            PathElm elm = elms.get(a);

            if(!(elm instanceof TravelNode)) {
                newPath.add(elm);
                continue;
            }

            TravelNode start = (TravelNode)elms.get(a);

            for(int b = elms.size() - 1 ; b > a ; b--) {
                if(!(elms.get(b) instanceof TravelNode)) {
                    continue;
                }

                TravelNode end = (TravelNode)elms.get(b);

                if(shouldOptimise(start, end, world)) {
                    a = b;
                    newPath.add(new TravelVector(start, end));
                    continue PathIter;
                }
            }

            newPath.add(elm);
        }

        elms.clear();
        elms.addAll(newPath);
    }

    // instead of getting surrounding blocks we could just shoot 2 vectors at an offset to each other
    public boolean shouldOptimise(TravelNode start, TravelNode end, World world) {
        if(start.getY() != end.getY())
            return false;

        Vec3 startVec = new Vec3(start.getBlockPos());
        Vec3 endVec = new Vec3(end.getBlockPos());

        Vec3 differenceVector = endVec.subtract(startVec);
        Vec3 normalDelta = differenceVector.normalize();

        List<BlockPos> blocksWithinVector = new ArrayList<>();

        // populate the blocks in the path in the vector
        for(int scale = 0 ; scale < endVec.distanceTo(startVec) ; scale++) {
            Vec3 blockVec = startVec.add(normalDelta.scale(scale));
            BlockPos blockPos = FallNode.toBlockPos(blockVec);

            if(!blocksWithinVector.contains(blockPos))
                blocksWithinVector.add(blockPos);
        }
        if(!blocksWithinVector.contains(FallNode.toBlockPos(endVec)))
            blocksWithinVector.add(FallNode.toBlockPos(endVec));

        blocksWithinVector.remove(FallNode.toBlockPos(startVec));

        for(BlockPos block : blocksWithinVector) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            // can optimise this by caching state
            BlockPos[] surroundings = new BlockPos[] {
                    new BlockPos(x+1, y, z+1),
                    new BlockPos(x, y, z+1),
                    new BlockPos(x-1, y, z+1),

                    new BlockPos(x+1, y, z),
                    new BlockPos(x, y, z),
                    new BlockPos(x-1, y, z),

                    new BlockPos(x+1, y, z-1),
                    new BlockPos(x, y, z-1),
                    new BlockPos(x-1, y, z-1),

                    new BlockPos(x+1, y+1, z+1),
                    new BlockPos(x, y+1, z+1),
                    new BlockPos(x-1, y+1, z+1),

                    new BlockPos(x+1, y+1, z),
                    new BlockPos(x, y+1, z),
                    new BlockPos(x-1, y+1, z),

                    new BlockPos(x+1, y+1, z-1),
                    new BlockPos(x, y+1, z-1),
                    new BlockPos(x-1, y+1, z-1),
            };

            for(BlockPos surroundingBlock : surroundings) {
                if(AStarNode.isBlockSolid(surroundingBlock, world)) {
                    return false;
                }
            }

            if(!AStarNode.isBlockSolid(block.subtract(new Vec3i(0, 1, 0)), world))
                return false;

        }

        return true;
    }


}
