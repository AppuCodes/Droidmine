package net.droidmine.pathfinder.processor;

import java.util.ArrayList;
import java.util.List;

import net.droidmine.pathfinder.astar.AStarNode;
import net.droidmine.pathfinder.path.PathElm;
import net.droidmine.pathfinder.path.impl.*;
import net.droidmine.pathfinder.processor.impl.*;
import net.minecraft.world.World;

public class ProcessorManager {
    public static List<PathElm> process(List<AStarNode> aStarNodes, World world) {
        List<PathElm> pathElms = convertRepresentation(aStarNodes);

        List<Processor> processors = new ArrayList<>();
        processors.add(new TravelProcessor());
        processors.add(new FallProcessor());
        processors.add(new JumpProcessor());

        for (Processor processor : processors) {
            processor.process(pathElms, world);
        }

        return pathElms;
    }

    // convert from base form of AStarNode to normal nodes
    private static List<PathElm> convertRepresentation(List<AStarNode> aStarNodes) {
        List<PathElm> pathElms = new ArrayList<>();

        for(int i = 0 ; i < aStarNodes.size() ; i++) {
            AStarNode node = aStarNodes.get(i);

            if(node.isJumpNode()) {
                pathElms.add(new JumpNode(node.getX(), node.getY(), node.getZ()));
                continue;
            }

            if(node.isFallNode()) {
                pathElms.add(new FallNode(node.getX(), node.getY(), node.getZ()));

                // skip directly in front of fall, unless if its the last node
                if(i != aStarNodes.size() - 1)
                    i+=1;
                continue;
            }

            pathElms.add(new TravelNode(node.getX(), node.getY(), node.getZ()));
        }

        return pathElms;
    }
}
