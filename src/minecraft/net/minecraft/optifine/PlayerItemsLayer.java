package net.minecraft.optifine;

import java.util.Map;
import java.util.Set;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

public class PlayerItemsLayer implements LayerRenderer
{
    private RenderPlayer renderPlayer = null;
    public ClientEngine mc;

    public PlayerItemsLayer(RenderPlayer p_i76_1_, ClientEngine mc)
    {
        this.renderPlayer = p_i76_1_;
        this.mc = mc;
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        this.renderEquippedItems(entitylivingbaseIn, scale, partialTicks);
    }

    protected void renderEquippedItems(EntityLivingBase p_renderEquippedItems_1_, float p_renderEquippedItems_2_, float p_renderEquippedItems_3_)
    {
        if (Config.get().isShowCapes())
        {
            if (p_renderEquippedItems_1_ instanceof AbstractClientPlayer)
            {
                AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)p_renderEquippedItems_1_;
                GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.get().disableRescaleNormal();
                GlStateManager.get().enableCull();
                ModelBiped modelbiped = (ModelBiped)this.renderPlayer.getMainModel();
                PlayerConfigurations.renderPlayerItems(modelbiped, abstractclientplayer, p_renderEquippedItems_2_, p_renderEquippedItems_3_, mc);
                GlStateManager.get().disableCull();
            }
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }

    public static void register(Map p_register_0_, ClientEngine mc)
    {
        Set set = p_register_0_.keySet();
        boolean flag = false;

        for (Object object : set)
        {
            Object object1 = p_register_0_.get(object);

            if (object1 instanceof RenderPlayer)
            {
                RenderPlayer renderplayer = (RenderPlayer)object1;
                renderplayer.addLayer(new PlayerItemsLayer(renderplayer, mc));
                flag = true;
            }
        }

        if (!flag)
        {
            Config.get().warn("PlayerItemsLayer not registered");
        }
    }
}
