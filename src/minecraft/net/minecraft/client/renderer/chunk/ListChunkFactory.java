package net.minecraft.client.renderer.chunk;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class ListChunkFactory implements IRenderChunkFactory
{
    public RenderChunk makeRenderChunk(World worldIn, RenderGlobal globalRenderer, BlockPos pos, int index, ClientEngine mc)
    {
        return new ListedRenderChunk(worldIn, globalRenderer, pos, index, mc);
    }
}
