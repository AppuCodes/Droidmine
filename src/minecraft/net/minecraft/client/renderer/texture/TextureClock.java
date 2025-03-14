package net.minecraft.client.renderer.texture;

import net.minecraft.client.ClientEngine;
import net.minecraft.optifine.Config;
import net.minecraft.optifine.shadersmod.client.ShadersTex;
import net.minecraft.util.MathHelper;

public class TextureClock extends TextureAtlasSprite
{
    private double field_94239_h;
    private double field_94240_i;
    private static final String __OBFID = "CL_00001070";

    public TextureClock(String iconName, ClientEngine mc)
    {
        super(iconName, mc);
    }

    public void updateAnimation()
    {
        if (!this.framesTextureData.isEmpty())
        {
            ClientEngine minecraft = mc;
            double d0 = 0.0D;

            if (minecraft.world != null && minecraft.player != null)
            {
                d0 = (double)minecraft.world.getCelestialAngle(1.0F);

                if (!minecraft.world.provider.isSurfaceWorld())
                {
                    d0 = Math.random();
                }
            }

            double d1;

            for (d1 = d0 - this.field_94239_h; d1 < -0.5D; ++d1)
            {
                ;
            }

            while (d1 >= 0.5D)
            {
                --d1;
            }

            d1 = MathHelper.clamp_double(d1, -1.0D, 1.0D);
            this.field_94240_i += d1 * 0.1D;
            this.field_94240_i *= 0.8D;
            this.field_94239_h += this.field_94240_i;
            int i;

            for (i = (int)((this.field_94239_h + 1.0D) * (double)this.framesTextureData.size()) % this.framesTextureData.size(); i < 0; i = (i + this.framesTextureData.size()) % this.framesTextureData.size())
            {
                ;
            }

            if (i != this.frameCounter)
            {
                this.frameCounter = i;

                if (Config.get().isShaders())
                {
                    ShadersTex.uploadTexSub((int[][])((int[][])this.framesTextureData.get(this.frameCounter)), this.width, this.height, this.originX, this.originY, false, false);
                }
                else
                {
                    TextureUtil.get().uploadTextureMipmap((int[][])((int[][])this.framesTextureData.get(this.frameCounter)), this.width, this.height, this.originX, this.originY, false, false);
                }
            }
        }
    }
}
