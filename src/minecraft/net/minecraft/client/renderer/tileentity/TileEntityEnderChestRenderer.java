package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ResourceLocation;

public class TileEntityEnderChestRenderer extends TileEntitySpecialRenderer<TileEntityEnderChest>
{
    public TileEntityEnderChestRenderer(ClientEngine mc) {
        super(mc);
        // TODO Auto-generated constructor stub
    }

    private static final ResourceLocation ENDER_CHEST_TEXTURE = new ResourceLocation("textures/entity/chest/ender.png");
    private ModelChest field_147521_c = new ModelChest();

    public void renderTileEntityAt(TileEntityEnderChest te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        int i = 0;

        if (te.hasWorldObj())
        {
            i = te.getBlockMetadata();
        }

        if (destroyStage >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.get().matrixMode(5890);
            GlStateManager.get().pushMatrix();
            GlStateManager.get().scale(4.0F, 4.0F, 1.0F);
            GlStateManager.get().translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.get().matrixMode(5888);
        }
        else
        {
            this.bindTexture(ENDER_CHEST_TEXTURE);
        }

        GlStateManager.get().pushMatrix();
        GlStateManager.get().enableRescaleNormal();
        GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.get().translate((float)x, (float)y + 1.0F, (float)z + 1.0F);
        GlStateManager.get().scale(1.0F, -1.0F, -1.0F);
        GlStateManager.get().translate(0.5F, 0.5F, 0.5F);
        int j = 0;

        if (i == 2)
        {
            j = 180;
        }

        if (i == 3)
        {
            j = 0;
        }

        if (i == 4)
        {
            j = 90;
        }

        if (i == 5)
        {
            j = -90;
        }

        GlStateManager.get().rotate((float)j, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().translate(-0.5F, -0.5F, -0.5F);
        float f = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;
        f = 1.0F - f;
        f = 1.0F - f * f * f;
        this.field_147521_c.chestLid.rotateAngleX = -(f * (float)Math.PI / 2.0F);
        this.field_147521_c.renderAll();
        GlStateManager.get().disableRescaleNormal();
        GlStateManager.get().popMatrix();
        GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);

        if (destroyStage >= 0)
        {
            GlStateManager.get().matrixMode(5890);
            GlStateManager.get().popMatrix();
            GlStateManager.get().matrixMode(5888);
        }
    }
}
