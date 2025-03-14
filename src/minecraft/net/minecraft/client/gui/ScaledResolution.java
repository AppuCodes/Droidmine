package net.minecraft.client.gui;

import java.util.HashMap;

import net.minecraft.client.ClientEngine;
import net.minecraft.util.MathHelper;

public class ScaledResolution
{
    /**
     * Cache ScaledResolution
     */
    private static HashMap<String, ScaledResolution> instances = new HashMap<>();
    private int scaledWidth, scaledHeight, scaleFactor;
    private double scaledWidthD, scaledHeightD;

    private ScaledResolution(ClientEngine mc)
    {
        update(mc);
    }

    public void update(ClientEngine mc)
    {
        if (mc == null)
        {
            return;
        }

        this.scaledWidth = mc.displayWidth;
        this.scaledHeight = mc.displayHeight;
        this.scaleFactor = 1;
        boolean flag = mc.isUnicode();
        int i = 1000;

        while (this.scaleFactor < i && this.scaledWidth / (this.scaleFactor + 1) >= 320 && this.scaledHeight / (this.scaleFactor + 1) >= 240)
        {
            ++this.scaleFactor;
        }

        if (flag && this.scaleFactor % 2 != 0 && this.scaleFactor != 1)
        {
            --this.scaleFactor;
        }

        this.scaledWidthD = (double)this.scaledWidth / (double)this.scaleFactor;
        this.scaledHeightD = (double)this.scaledHeight / (double)this.scaleFactor;
        this.scaledWidth = MathHelper.ceiling_double_int(this.scaledWidthD);
        this.scaledHeight = MathHelper.ceiling_double_int(this.scaledHeightD);
    }

    public int getScaledWidth()
    {
        return this.scaledWidth;
    }

    public int getScaledHeight()
    {
        return this.scaledHeight;
    }

    public double getScaledWidth_double()
    {
        return this.scaledWidthD;
    }

    public double getScaledHeight_double()
    {
        return this.scaledHeightD;
    }

    public int getScaleFactor()
    {
        return this.scaleFactor;
    }

    public static ScaledResolution init(ClientEngine mc)
    {
        ScaledResolution scaled = new ScaledResolution(mc);
        instances.put(Thread.currentThread().getName(), scaled);
        return scaled;
    }

    public static ScaledResolution get()
    {
        ScaledResolution ins = instances.get(Thread.currentThread().getName());
        return ins == null ? ScaledResolution.init(null) : ins;
    }
}
