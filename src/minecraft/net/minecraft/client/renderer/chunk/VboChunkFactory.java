package net.minecraft.client.renderer.chunk;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class VboChunkFactory implements IRenderChunkFactory
{
    public RenderChunk makeRenderChunk(World worldIn, RenderGlobal globalRenderer, BlockPos pos, int index, ClientEngine mc)
    {
        return new RenderChunk(worldIn, globalRenderer, pos, index, mc);
    }
}
