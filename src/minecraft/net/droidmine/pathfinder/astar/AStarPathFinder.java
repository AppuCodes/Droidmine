package net.droidmine.pathfinder.astar;

import java.util.*;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class AStarPathFinder {
    public static List<AStarNode> compute(BlockPos start, BlockPos end, int depth, World world) {
        PriorityQueue<AStarNode> openQueue = new PriorityQueue<>(Comparator.comparingDouble(AStarNode::getTotalCost));
        List<AStarNode> closedList = new ArrayList<>();

        AStarNode endNode = new AStarNode(end, world);
        AStarNode startNode = new AStarNode(start, endNode, world);

        // add start node
        openQueue.add(startNode);

        for(int i = 0 ; i < depth ; i++) {

            if(openQueue.isEmpty())
                return new ArrayList<>();

            AStarNode currentNode = openQueue.poll();
            closedList.add(currentNode);

            if(currentNode.equals(endNode))
                return getPath(currentNode);

            populateNeighbours(openQueue, closedList, currentNode, startNode, endNode, world);
        }

        return new ArrayList<>();
    }

    private static void populateNeighbours(PriorityQueue<AStarNode> openQueue, List<AStarNode> closedList, AStarNode current, AStarNode startNode, AStarNode endNode, World world) {
        List<AStarNode> neighbours = new ArrayList<>();
        neighbours.add(new AStarNode(-1, 0, 0, current, endNode, world));
        neighbours.add(new AStarNode(0, 0, 1, current, endNode, world));
        neighbours.add(new AStarNode(0, 0, -1, current, endNode, world));
        neighbours.add(new AStarNode(1, 0, 0, current, endNode, world));
        neighbours.add(new AStarNode(0, 1, 0, current, endNode, world));
        neighbours.add(new AStarNode(0,-1,0, current, endNode, world));

        for(AStarNode neighbour : neighbours) {

            if(closedList.contains(neighbour))
                continue;

            if(neighbour.getTotalCost() < current.getTotalCost() || !openQueue.contains(neighbour)) {
                if(neighbour.canBeTraversed()) {
                    openQueue.remove(neighbour);
                    openQueue.add(neighbour);
                }
            }
        }
    }

    private static List<AStarNode> getPath(AStarNode currentNode) {
        List<AStarNode> path = new ArrayList<>();
        path.add(currentNode);
        AStarNode parent;
        while ((parent = currentNode.getParent()) != null) {
            path.add(0, parent);
            currentNode = parent;
        }
        return path;
    }
}
