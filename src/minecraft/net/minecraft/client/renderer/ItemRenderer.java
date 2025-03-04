package net.minecraft.client.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientEngine;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.optifine.*;
import net.minecraft.optifine.shadersmod.client.Shaders;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;

public class ItemRenderer
{
    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");

    /** A reference to the Minecraft object. */
    private final ClientEngine mc;
    private ItemStack itemToRender;

    /**
     * How far the current item has been equipped (0 disequipped and 1 fully up)
     */
    private float equippedProgress;
    private float prevEquippedProgress;
    private final RenderManager renderManager;
    private final RenderItem itemRenderer;

    /** The index of the currently held item (0-8, or -1 if not yet updated) */
    private int equippedItemSlot = -1;
    private static final String __OBFID = "CL_00000953";

    public ItemRenderer(ClientEngine mcIn)
    {
        this.mc = mcIn;
        this.renderManager = mcIn.getRenderManager();
        this.itemRenderer = mcIn.getRenderItem();
    }

    public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform)
    {
        if (heldStack != null)
        {
            Item item = heldStack.getItem();
            Block block = Block.getBlockFromItem(item);
            GlStateManager.get().pushMatrix();

            if (this.itemRenderer.shouldRenderItemIn3D(heldStack))
            {
                GlStateManager.get().scale(2.0F, 2.0F, 2.0F);

                if (this.isBlockTranslucent(block) && (!Config.get().isShaders() || !Shaders.renderItemKeepDepthMask))
                {
                    GlStateManager.get().depthMask(false);
                }
            }
            
            this.itemRenderer.renderItemModelForEntity(heldStack, entityIn, transform);

            if (this.isBlockTranslucent(block))
            {
                GlStateManager.get().depthMask(true);
            }

            GlStateManager.get().popMatrix();
        }
    }

    /**
     * Returns true if given block is translucent
     */
    private boolean isBlockTranslucent(Block blockIn)
    {
        return blockIn != null && blockIn.getBlockLayer() == EnumWorldBlockLayer.TRANSLUCENT;
    }

    private void func_178101_a(float angle, float p_178101_2_)
    {
        GlStateManager.get().pushMatrix();
        GlStateManager.get().rotate(angle, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().rotate(p_178101_2_, 0.0F, 1.0F, 0.0F);
        RenderHelper.get().enableStandardItemLighting();
        GlStateManager.get().popMatrix();
    }

    private void func_178109_a(AbstractClientPlayer clientPlayer)
    {
        int i = this.mc.world.getCombinedLight(new BlockPos(clientPlayer.posX, clientPlayer.posY + (double)clientPlayer.getEyeHeight(), clientPlayer.posZ), 0);

        if (Config.get().isDynamicLights())
        {
            i = DynamicLights.getCombinedLight(this.mc.getRenderViewEntity(), i);
        }

        float f = (float)(i & 65535);
        float f1 = (float)(i >> 16);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
    }

    private void func_178110_a(EntityPlayerSP entityplayerspIn, float partialTicks)
    {
        float f = entityplayerspIn.prevRenderArmPitch + (entityplayerspIn.renderArmPitch - entityplayerspIn.prevRenderArmPitch) * partialTicks;
        float f1 = entityplayerspIn.prevRenderArmYaw + (entityplayerspIn.renderArmYaw - entityplayerspIn.prevRenderArmYaw) * partialTicks;
        GlStateManager.get().rotate((entityplayerspIn.rotationPitch - f) * 0.1F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().rotate((entityplayerspIn.rotationYaw - f1) * 0.1F, 0.0F, 1.0F, 0.0F);
    }

    private float func_178100_c(float p_178100_1_)
    {
        float f = 1.0F - p_178100_1_ / 45.0F + 0.1F;
        f = MathHelper.clamp_float(f, 0.0F, 1.0F);
        f = -MathHelper.cos(f * (float)Math.PI) * 0.5F + 0.5F;
        return f;
    }

    private void renderRightArm(RenderPlayer renderPlayerIn)
    {
        GlStateManager.get().pushMatrix();
        GlStateManager.get().rotate(54.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(64.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().rotate(-62.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.get().translate(0.25F, -0.85F, 0.75F);
        renderPlayerIn.renderRightArm(this.mc.player);
        GlStateManager.get().popMatrix();
    }

    private void renderLeftArm(RenderPlayer renderPlayerIn)
    {
        GlStateManager.get().pushMatrix();
        GlStateManager.get().rotate(92.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().rotate(41.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.get().translate(-0.3F, -1.1F, 0.45F);
        renderPlayerIn.renderLeftArm(this.mc.player);
        GlStateManager.get().popMatrix();
    }

    private void renderPlayerArms(AbstractClientPlayer clientPlayer)
    {
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        Render render = this.renderManager.getEntityRenderObject(this.mc.player);
        RenderPlayer renderplayer = (RenderPlayer)render;

        if (!clientPlayer.isInvisible())
        {
            GlStateManager.get().disableCull();
            this.renderRightArm(renderplayer);
            this.renderLeftArm(renderplayer);
            GlStateManager.get().enableCull();
        }
    }

    private void renderItemMap(AbstractClientPlayer clientPlayer, float p_178097_2_, float p_178097_3_, float p_178097_4_)
    {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float)Math.PI);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float)Math.PI * 2.0F);
        float f2 = -0.2F * MathHelper.sin(p_178097_4_ * (float)Math.PI);
        GlStateManager.get().translate(f, f1, f2);
        float f3 = this.func_178100_c(p_178097_2_);
        GlStateManager.get().translate(0.0F, 0.04F, -0.72F);
        GlStateManager.get().translate(0.0F, p_178097_3_ * -1.2F, 0.0F);
        GlStateManager.get().translate(0.0F, f3 * -0.5F, 0.0F);
        GlStateManager.get().rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(f3 * -85.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.get().rotate(0.0F, 1.0F, 0.0F, 0.0F);
        this.renderPlayerArms(clientPlayer);
        float f4 = MathHelper.sin(p_178097_4_ * p_178097_4_ * (float)Math.PI);
        float f5 = MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float)Math.PI);
        GlStateManager.get().rotate(f4 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.get().rotate(f5 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().scale(0.38F, 0.38F, 0.38F);
        GlStateManager.get().rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.get().rotate(0.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().translate(-1.0F, -1.0F, 0.0F);
        GlStateManager.get().scale(0.015625F, 0.015625F, 0.015625F);
        this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        MapData mapdata = Items.filled_map.getMapData(this.itemToRender, this.mc.world);

        if (mapdata != null)
        {
            this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
        }
    }

    private void func_178095_a(AbstractClientPlayer clientPlayer, float p_178095_2_, float p_178095_3_)
    {
        float f = -0.3F * MathHelper.sin(MathHelper.sqrt_float(p_178095_3_) * (float)Math.PI);
        float f1 = 0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178095_3_) * (float)Math.PI * 2.0F);
        float f2 = -0.4F * MathHelper.sin(p_178095_3_ * (float)Math.PI);
        GlStateManager.get().translate(f, f1, f2);
        GlStateManager.get().translate(0.64000005F, -0.6F, -0.71999997F);
        GlStateManager.get().translate(0.0F, p_178095_2_ * -0.6F, 0.0F);
        GlStateManager.get().rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f3 = MathHelper.sin(p_178095_3_ * p_178095_3_ * (float)Math.PI);
        float f4 = MathHelper.sin(MathHelper.sqrt_float(p_178095_3_) * (float)Math.PI);
        GlStateManager.get().rotate(f4 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(f3 * -20.0F, 0.0F, 0.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        GlStateManager.get().translate(-1.0F, 3.6F, 3.5F);
        GlStateManager.get().rotate(120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.get().rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().scale(1.0F, 1.0F, 1.0F);
        GlStateManager.get().translate(5.6F, 0.0F, 0.0F);
        Render render = this.renderManager.getEntityRenderObject(this.mc.player);
        GlStateManager.get().disableCull();
        RenderPlayer renderplayer = (RenderPlayer)render;
        renderplayer.renderRightArm(this.mc.player);
        GlStateManager.get().enableCull();
    }

    private void func_178105_d(float p_178105_1_)
    {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178105_1_) * (float)Math.PI);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(p_178105_1_) * (float)Math.PI * 2.0F);
        float f2 = -0.2F * MathHelper.sin(p_178105_1_ * (float)Math.PI);
        GlStateManager.get().translate(f, f1, f2);
    }

    private void func_178104_a(AbstractClientPlayer clientPlayer, float p_178104_2_)
    {
        float f = (float)clientPlayer.getItemInUseCount() - p_178104_2_ + 1.0F;
        float f1 = f / (float)this.itemToRender.getMaxItemUseDuration();
        float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * (float)Math.PI) * 0.1F);

        if (f1 >= 0.8F)
        {
            f2 = 0.0F;
        }

        GlStateManager.get().translate(0.0F, f2, 0.0F);
        float f3 = 1.0F - (float)Math.pow((double)f1, 27.0D);
        GlStateManager.get().translate(f3 * 0.6F, f3 * -0.5F, f3 * 0.0F);
        GlStateManager.get().rotate(f3 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().rotate(f3 * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    /**
     * Performs transformations prior to the rendering of a held item in first person.
     */
    private void transformFirstPersonItem(float equipProgress, float swingProgress)
    {
        GlStateManager.get().translate(0.56F, -0.52F, -0.72F);
        GlStateManager.get().translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.get().rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
        GlStateManager.get().rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.get().rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().scale(0.4F, 0.4F, 0.4F);
    }

    private void func_178098_a(float p_178098_1_, AbstractClientPlayer clientPlayer)
    {
        GlStateManager.get().rotate(-18.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.get().rotate(-12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(-8.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().translate(-0.9F, 0.2F, 0.0F);
        float f = (float)this.itemToRender.getMaxItemUseDuration() - ((float)clientPlayer.getItemInUseCount() - p_178098_1_ + 1.0F);
        float f1 = f / 20.0F;
        f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;

        if (f1 > 1.0F)
        {
            f1 = 1.0F;
        }

        if (f1 > 0.1F)
        {
            float f2 = MathHelper.sin((f - 0.1F) * 1.3F);
            float f3 = f1 - 0.1F;
            float f4 = f2 * f3;
            GlStateManager.get().translate(f4 * 0.0F, f4 * 0.01F, f4 * 0.0F);
        }

        GlStateManager.get().translate(f1 * 0.0F, f1 * 0.0F, f1 * 0.1F);
        GlStateManager.get().scale(1.0F, 1.0F, 1.0F + f1 * 0.2F);
    }

    private void func_178103_d()
    {
        GlStateManager.get().translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.get().rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.get().rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    /**
     * Renders the active item in the player's hand when in first person mode. Args: partialTickTime
     */
    public void renderItemInFirstPerson(float partialTicks)
    {
        float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        EntityPlayerSP entityplayersp = this.mc.player;
        float f1 = entityplayersp.getSwingProgress(partialTicks);
        float f2 = entityplayersp.prevRotationPitch + (entityplayersp.rotationPitch - entityplayersp.prevRotationPitch) * partialTicks;
        float f3 = entityplayersp.prevRotationYaw + (entityplayersp.rotationYaw - entityplayersp.prevRotationYaw) * partialTicks;
        this.func_178101_a(f2, f3);
        this.func_178109_a(entityplayersp);
        GlStateManager.get().enableRescaleNormal();
        GlStateManager.get().pushMatrix();

        if (this.itemToRender != null)
        {
            if (this.itemToRender.getItem() instanceof ItemMap)
            {
                this.renderItemMap(entityplayersp, f2, f, f1);
            }
            else if (entityplayersp.getItemInUseCount() > 0)
            {
                EnumAction enumaction = this.itemToRender.getItemUseAction();

                switch (ItemRenderer.ItemRenderer$1.field_178094_a[enumaction.ordinal()])
                {
                    case 1:
                        this.transformFirstPersonItem(f, f1);
                        break;

                    case 2:
                    case 3:
                        this.func_178104_a(entityplayersp, partialTicks);
                        this.transformFirstPersonItem(f, f1);
                        break;

                    case 4:
                        this.transformFirstPersonItem(f, f1);
                        this.func_178103_d();
                        break;

                    case 5:
                        this.transformFirstPersonItem(f, f1);
                        this.func_178098_a(partialTicks, entityplayersp);
                }
            }
            else
            {
                this.func_178105_d(f1);
                this.transformFirstPersonItem(f, f1);
            }
            
            this.renderItem(entityplayersp, this.itemToRender, transformItem() ? ItemCameraTransforms.TransformType.FIRST_PERSON : ItemCameraTransforms.TransformType.NONE);
        }
        else if (!entityplayersp.isInvisible())
        {
            this.func_178095_a(entityplayersp, f, f1);
        }

        GlStateManager.get().popMatrix();
        GlStateManager.get().disableRescaleNormal();
        RenderHelper.get().disableStandardItemLighting();
    }
    
    public boolean transformItem()
    {
        if (this.mc.getRenderItem().shouldRenderItemIn3D(this.itemToRender))
        {
            return true;
        }
        
        if (this.itemToRender.getItem() instanceof ItemFishingRod)
        {
            GlStateManager.get().translate(0.0F, 0.0F, -0.3F);
            return true;
        }
        
        GlStateManager.get().translate(0.588F, 0.36F, -0.77F);
        GlStateManager.get().translate(0, -0.3F, 0.0F);
        GlStateManager.get().scale(1.5F, 1.5F, 1.5F);
        GlStateManager.get().rotate(50.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.get().rotate(335.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.get().translate(-0.9375F, -0.0625F, 0.0F);
        GlStateManager.get().scale(-1, 1, -1);
        return false;
    }

    /**
     * Renders all the overlays that are in first person mode. Args: partialTickTime
     */
    public void renderOverlays(float partialTicks)
    {
        GlStateManager.get().disableAlpha();

        if (this.mc.player.isEntityInsideOpaqueBlock())
        {
            IBlockState iblockstate = this.mc.world.getBlockState(new BlockPos(this.mc.player));
            BlockPos blockpos = new BlockPos(this.mc.player);
            EntityPlayerSP entityplayersp = this.mc.player;

            for (int i = 0; i < 8; ++i)
            {
                double d0 = entityplayersp.posX + (double)(((float)((i >> 0) % 2) - 0.5F) * entityplayersp.width * 0.8F);
                double d1 = entityplayersp.posY + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
                double d2 = entityplayersp.posZ + (double)(((float)((i >> 2) % 2) - 0.5F) * entityplayersp.width * 0.8F);
                BlockPos blockpos1 = new BlockPos(d0, d1 + (double)entityplayersp.getEyeHeight(), d2);
                IBlockState iblockstate1 = this.mc.world.getBlockState(blockpos1);

                if (iblockstate1.getBlock().isVisuallyOpaque())
                {
                    iblockstate = iblockstate1;
                    blockpos = blockpos1;
                }
            }

            if (iblockstate.getBlock().getRenderType() != -1)
            {
                Object object = Reflector.getFieldValue(Reflector.RenderBlockOverlayEvent_OverlayType_BLOCK);

                if (!Reflector.callBoolean(Reflector.ForgeEventFactory_renderBlockOverlay, new Object[] {this.mc.player, Float.valueOf(partialTicks), object, iblockstate, blockpos}) && !mc.isHeadless())
                {
                    this.renderInsideOfBlock(partialTicks, this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(iblockstate));
                }
            }
        }

        if (!this.mc.player.isSpectator())
        {
            if (this.mc.player.isInsideOfMaterial(Material.water) && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderWaterOverlay, new Object[] {this.mc.player, Float.valueOf(partialTicks)}))
            {
                this.renderWaterOverlayTexture(partialTicks);
            }

            if (this.mc.player.isBurning() && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderFireOverlay, new Object[] {this.mc.player, Float.valueOf(partialTicks)}))
            {
                this.renderFireInFirstPerson(partialTicks);
            }
        }

        GlStateManager.get().enableAlpha();
    }

    private void renderInsideOfBlock(float p_178108_1_, TextureAtlasSprite p_178108_2_)
    {
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f = 0.1F;
        GlStateManager.get().color(0.1F, 0.1F, 0.1F, 0.5F);
        GlStateManager.get().pushMatrix();
        float f1 = -1.0F;
        float f2 = 1.0F;
        float f3 = -1.0F;
        float f4 = 1.0F;
        float f5 = -0.5F;
        float f6 = p_178108_2_.getMinU();
        float f7 = p_178108_2_.getMaxU();
        float f8 = p_178108_2_.getMinV();
        float f9 = p_178108_2_.getMaxV();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex((double)f7, (double)f9).endVertex();
        worldrenderer.pos(1.0D, -1.0D, -0.5D).tex((double)f6, (double)f9).endVertex();
        worldrenderer.pos(1.0D, 1.0D, -0.5D).tex((double)f6, (double)f8).endVertex();
        worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex((double)f7, (double)f8).endVertex();
        tessellator.draw();
        GlStateManager.get().popMatrix();
        GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Renders a texture that warps around based on the direction the player is looking. Texture needs to be bound
     * before being called. Used for the water overlay. Args: parialTickTime
     */
    private void renderWaterOverlayTexture(float p_78448_1_)
    {
        if (!Config.get().isShaders() || Shaders.isUnderwaterOverlay())
        {
            this.mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            float f = this.mc.player.getBrightness(p_78448_1_);
            GlStateManager.get().color(f, f, f, 0.5F);
            GlStateManager.get().enableBlend();
            GlStateManager.get().tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.get().pushMatrix();
            float f1 = 4.0F;
            float f2 = -1.0F;
            float f3 = 1.0F;
            float f4 = -1.0F;
            float f5 = 1.0F;
            float f6 = -0.5F;
            float f7 = -this.mc.player.rotationYaw / 64.0F;
            float f8 = this.mc.player.rotationPitch / 64.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex((double)(4.0F + f7), (double)(4.0F + f8)).endVertex();
            worldrenderer.pos(1.0D, -1.0D, -0.5D).tex((double)(0.0F + f7), (double)(4.0F + f8)).endVertex();
            worldrenderer.pos(1.0D, 1.0D, -0.5D).tex((double)(0.0F + f7), (double)(0.0F + f8)).endVertex();
            worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex((double)(4.0F + f7), (double)(0.0F + f8)).endVertex();
            tessellator.draw();
            GlStateManager.get().popMatrix();
            GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.get().disableBlend();
        }
    }

    /**
     * Renders the fire on the screen for first person mode. Arg: partialTickTime
     */
    private void renderFireInFirstPerson(float p_78442_1_)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.get().color(1.0F, 1.0F, 1.0F, 0.9F);
        GlStateManager.get().enableBlend();
        GlStateManager.get().tryBlendFuncSeparate(770, 771, 1, 0);
        float f = 1.0F;

        for (int i = 0; i < 2; ++i)
        {
            GlStateManager.get().pushMatrix();
            TextureAtlasSprite textureatlassprite = this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
            this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            float f1 = textureatlassprite.getMinU();
            float f2 = textureatlassprite.getMaxU();
            float f3 = textureatlassprite.getMinV();
            float f4 = textureatlassprite.getMaxV();
            float f5 = (0.0F - f) / 2.0F;
            float f6 = f5 + f;
            float f7 = 0.0F - f / 2.0F;
            float f8 = f7 + f;
            float f9 = -0.5F;
            GlStateManager.get().translate((float)(-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            GlStateManager.get().rotate((float)(i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos((double)f5, (double)f7, (double)f9).tex((double)f2, (double)f4).endVertex();
            worldrenderer.pos((double)f6, (double)f7, (double)f9).tex((double)f1, (double)f4).endVertex();
            worldrenderer.pos((double)f6, (double)f8, (double)f9).tex((double)f1, (double)f3).endVertex();
            worldrenderer.pos((double)f5, (double)f8, (double)f9).tex((double)f2, (double)f3).endVertex();
            tessellator.draw();
            GlStateManager.get().popMatrix();
        }

        GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.get().disableBlend();
    }

    public void updateEquippedItem()
    {
        this.prevEquippedProgress = this.equippedProgress;
        EntityPlayerSP var1 = this.mc.player;
        ItemStack var2 = var1.inventory.getCurrentItem();
        boolean var3 = this.equippedItemSlot == var1.inventory.currentItem && var2 == this.itemToRender;
        
        if (this.itemToRender == null && var2 == null)
        {
            var3 = true;
        }

        if (var2 != null && this.itemToRender != null && var2 != this.itemToRender && var2.getItem() == this.itemToRender.getItem() && var2.getItemDamage() == this.itemToRender.getItemDamage())
        {
            this.itemToRender = var2;
            var3 = true;
        }

        float var4 = 0.4F;
        float var5 = var3 ? 1.0F : 0.0F;
        float var6 = var5 - this.equippedProgress;
        
        if (var6 < -var4)
        {
            var6 = -var4;
        }

        if (var6 > var4)
        {
            var6 = var4;
        }
        
        this.equippedProgress += var6;

        if (this.equippedProgress < 0.1F)
        {
            if (Config.get().isShaders())
            {
                Shaders.setItemToRenderMain(var2);
            }

            this.itemToRender = var2;
            this.equippedItemSlot = var1.inventory.currentItem;
        }
    }

    /**
     * Resets equippedProgress
     */
    public void resetEquippedProgress()
    {
        this.equippedProgress = 0.0F;
    }

    /**
     * Resets equippedProgress
     */
    public void resetEquippedProgress2()
    {
        this.equippedProgress = 0.0F;
    }

    static final class ItemRenderer$1
    {
        static final int[] field_178094_a = new int[EnumAction.values().length];
        private static final String __OBFID = "CL_00002537";

        static
        {
            try
            {
                field_178094_a[EnumAction.NONE.ordinal()] = 1;
            }
            catch (NoSuchFieldError var5)
            {
                ;
            }

            try
            {
                field_178094_a[EnumAction.EAT.ordinal()] = 2;
            }
            catch (NoSuchFieldError var4)
            {
                ;
            }

            try
            {
                field_178094_a[EnumAction.DRINK.ordinal()] = 3;
            }
            catch (NoSuchFieldError var3)
            {
                ;
            }

            try
            {
                field_178094_a[EnumAction.BLOCK.ordinal()] = 4;
            }
            catch (NoSuchFieldError var2)
            {
                ;
            }

            try
            {
                field_178094_a[EnumAction.BOW.ordinal()] = 5;
            }
            catch (NoSuchFieldError var1)
            {
                ;
            }
        }
    }
}
