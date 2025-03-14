package net.minecraft.client.renderer;

import java.util.HashMap;

public class Tessellator
{
    private WorldRenderer worldRenderer;
    private WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();

    /** The static instance of the Tessellator. */
    private static final HashMap<String, Tessellator> instances = new HashMap<>();

    public static Tessellator getInstance()
    {
        String thread = Thread.currentThread().getName();
        
        if (instances.containsKey(thread))
        {
            return instances.get(thread);
        }
        
        Tessellator tessellator = new Tessellator(2097152);
        instances.put(thread, tessellator);
        return tessellator;
    }

    public Tessellator(int bufferSize)
    {
        this.worldRenderer = new WorldRenderer(bufferSize);
    }

    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     */
    public void draw()
    {
        this.worldRenderer.finishDrawing();
        this.vboUploader.func_181679_a(this.worldRenderer);
    }

    public WorldRenderer getWorldRenderer()
    {
        return this.worldRenderer;
    }
}
