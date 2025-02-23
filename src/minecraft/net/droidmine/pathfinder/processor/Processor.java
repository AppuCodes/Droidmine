package net.droidmine.pathfinder.processor;

import java.util.List;

import net.droidmine.pathfinder.path.PathElm;
import net.minecraft.world.World;

public abstract class Processor {
    public abstract void process(List<PathElm> elms, World world);
}
