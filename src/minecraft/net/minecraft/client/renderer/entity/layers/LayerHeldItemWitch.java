package net.minecraft.client.renderer.entity.layers;

import net.minecraft.block.Block;
import net.minecraft.client.ClientEngine;
import net.minecraft.client.model.ModelWitch;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderWitch;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.init.Items;
import net.minecraft.item.*;

public class LayerHeldItemWitch implements LayerRenderer<EntityWitch>
{
    private final RenderWitch witchRenderer;
    private ClientEngine mc;

    public LayerHeldItemWitch(RenderWitch witchRendererIn, ClientEngine mc)
    {
        this.witchRenderer = witchRendererIn;
        this.mc = mc;
    }

    public void doRenderLayer(EntityWitch entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        ItemStack itemstack = entitylivingbaseIn.getHeldItem();

        if (itemstack != null)
        {
            GlStateManager.get().color(1.0F, 1.0F, 1.0F);
            GlStateManager.get().pushMatrix();

            if (this.witchRenderer.getMainModel().isChild)
            {
                GlStateManager.get().translate(0.0F, 0.625F, 0.0F);
                GlStateManager.get().rotate(-20.0F, -1.0F, 0.0F, 0.0F);
                float f = 0.5F;
                GlStateManager.get().scale(f, f, f);
            }

            ((ModelWitch)this.witchRenderer.getMainModel()).villagerNose.postRender(0.0625F);
            GlStateManager.get().translate(-0.0625F, 0.53125F, 0.21875F);
            Item item = itemstack.getItem();
            ClientEngine minecraft = mc;

            if (item instanceof ItemBlock && minecraft.getBlockRendererDispatcher().isRenderTypeChest(Block.getBlockFromItem(item), itemstack.getMetadata()))
            {
                GlStateManager.get().translate(0.0F, 0.0625F, -0.25F);
                GlStateManager.get().rotate(30.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.get().rotate(-5.0F, 0.0F, 1.0F, 0.0F);
                float f4 = 0.375F;
                GlStateManager.get().scale(f4, -f4, f4);
            }
            else if (item == Items.bow)
            {
                GlStateManager.get().translate(0.0F, 0.125F, -0.125F);
                GlStateManager.get().rotate(-45.0F, 0.0F, 1.0F, 0.0F);
                float f1 = 0.625F;
                GlStateManager.get().scale(f1, -f1, f1);
                GlStateManager.get().rotate(-100.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.get().rotate(-20.0F, 0.0F, 1.0F, 0.0F);
            }
            else if (item.isFull3D())
            {
                if (item.shouldRotateAroundWhenRendering())
                {
                    GlStateManager.get().rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.get().translate(0.0F, -0.0625F, 0.0F);
                }

                this.witchRenderer.transformHeldFull3DItemLayer();
                GlStateManager.get().translate(0.0625F, -0.125F, 0.0F);
                float f2 = 0.625F;
                GlStateManager.get().scale(f2, -f2, f2);
                GlStateManager.get().rotate(0.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.get().rotate(0.0F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                GlStateManager.get().translate(0.1875F, 0.1875F, 0.0F);
                float f3 = 0.875F;
                GlStateManager.get().scale(f3, f3, f3);
                GlStateManager.get().rotate(-20.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.get().rotate(-60.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.get().rotate(-30.0F, 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.get().rotate(-15.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.get().rotate(40.0F, 0.0F, 0.0F, 1.0F);
            minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
            GlStateManager.get().popMatrix();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
