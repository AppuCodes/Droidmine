package net.minecraft.client.renderer.tileentity;

import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileEntitySkullRenderer extends TileEntitySpecialRenderer<TileEntitySkull>
{
    public TileEntitySkullRenderer(ClientEngine mc) {
        super(mc);
        // TODO Auto-generated constructor stub
    }

    private static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    private static final ResourceLocation WITHER_SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
    private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/zombie.png");
    private static final ResourceLocation CREEPER_TEXTURES = new ResourceLocation("textures/entity/creeper/creeper.png");
    public static TileEntitySkullRenderer instance;
    private final ModelSkeletonHead skeletonHead = new ModelSkeletonHead(0, 0, 64, 32);
    private final ModelSkeletonHead humanoidHead = new ModelHumanoidHead();

    public void renderTileEntityAt(TileEntitySkull te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        EnumFacing enumfacing = EnumFacing.getFront(te.getBlockMetadata() & 7);
        this.renderSkull((float)x, (float)y, (float)z, enumfacing, (float)(te.getSkullRotation() * 360) / 16.0F, te.getSkullType(), te.getPlayerProfile(), destroyStage);
    }

    public void setRendererDispatcher(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super.setRendererDispatcher(rendererDispatcherIn);
        instance = this;
    }

    public void renderSkull(float p_180543_1_, float p_180543_2_, float p_180543_3_, EnumFacing p_180543_4_, float p_180543_5_, int p_180543_6_, GameProfile p_180543_7_, int p_180543_8_)
    {
        ModelBase modelbase = this.skeletonHead;

        if (p_180543_8_ >= 0)
        {
            this.bindTexture(DESTROY_STAGES[p_180543_8_]);
            GlStateManager.get().matrixMode(5890);
            GlStateManager.get().pushMatrix();
            GlStateManager.get().scale(4.0F, 2.0F, 1.0F);
            GlStateManager.get().translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.get().matrixMode(5888);
        }
        else
        {
            switch (p_180543_6_)
            {
                case 0:
                default:
                    this.bindTexture(SKELETON_TEXTURES);
                    break;

                case 1:
                    this.bindTexture(WITHER_SKELETON_TEXTURES);
                    break;

                case 2:
                    this.bindTexture(ZOMBIE_TEXTURES);
                    modelbase = this.humanoidHead;
                    break;

                case 3:
                    modelbase = this.humanoidHead;
                    ResourceLocation resourcelocation = DefaultPlayerSkin.getDefaultSkinLegacy();

                    if (p_180543_7_ != null)
                    {
                        ClientEngine minecraft = mc;
                        Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(p_180543_7_);

                        if (map.containsKey(Type.SKIN))
                        {
                            resourcelocation = minecraft.getSkinManager().loadSkin((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN);
                        }
                        else
                        {
                            UUID uuid = EntityPlayer.getUUID(p_180543_7_);
                            resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
                        }
                    }

                    this.bindTexture(resourcelocation);
                    break;

                case 4:
                    this.bindTexture(CREEPER_TEXTURES);
            }
        }

        GlStateManager.get().pushMatrix();
        GlStateManager.get().disableCull();

        if (p_180543_4_ != EnumFacing.UP)
        {
            switch (p_180543_4_)
            {
                case NORTH:
                    GlStateManager.get().translate(p_180543_1_ + 0.5F, p_180543_2_ + 0.25F, p_180543_3_ + 0.74F);
                    break;

                case SOUTH:
                    GlStateManager.get().translate(p_180543_1_ + 0.5F, p_180543_2_ + 0.25F, p_180543_3_ + 0.26F);
                    p_180543_5_ = 180.0F;
                    break;

                case WEST:
                    GlStateManager.get().translate(p_180543_1_ + 0.74F, p_180543_2_ + 0.25F, p_180543_3_ + 0.5F);
                    p_180543_5_ = 270.0F;
                    break;

                case EAST:
                default:
                    GlStateManager.get().translate(p_180543_1_ + 0.26F, p_180543_2_ + 0.25F, p_180543_3_ + 0.5F);
                    p_180543_5_ = 90.0F;
            }
        }
        else
        {
            GlStateManager.get().translate(p_180543_1_ + 0.5F, p_180543_2_, p_180543_3_ + 0.5F);
        }

        float f = 0.0625F;
        GlStateManager.get().enableRescaleNormal();
        GlStateManager.get().scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.get().enableAlpha();
        modelbase.render((Entity)null, 0.0F, 0.0F, 0.0F, p_180543_5_, 0.0F, f);
        GlStateManager.get().popMatrix();

        if (p_180543_8_ >= 0)
        {
            GlStateManager.get().matrixMode(5890);
            GlStateManager.get().popMatrix();
            GlStateManager.get().matrixMode(5888);
        }
    }
}
