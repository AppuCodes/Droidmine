package net.minecraft.client.renderer.entity.layers;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StringUtils;

public class LayerCustomHead implements LayerRenderer<EntityLivingBase>
{
    private final ModelRenderer field_177209_a;
    private ClientEngine mc;

    public LayerCustomHead(ModelRenderer p_i46120_1_, ClientEngine mc)
    {
        this.field_177209_a = p_i46120_1_;
        this.mc = mc;
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        ItemStack itemstack = entitylivingbaseIn.getCurrentArmor(3);

        if (itemstack != null && itemstack.getItem() != null)
        {
            Item item = itemstack.getItem();
            ClientEngine minecraft = mc;
            GlStateManager.get().pushMatrix();

            if (entitylivingbaseIn.isSneaking())
            {
                GlStateManager.get().translate(0.0F, 0.2F, 0.0F);
            }

            boolean flag = entitylivingbaseIn instanceof EntityVillager || entitylivingbaseIn instanceof EntityZombie && ((EntityZombie)entitylivingbaseIn).isVillager();

            if (!flag && entitylivingbaseIn.isChild())
            {
                float f = 2.0F;
                float f1 = 1.4F;
                GlStateManager.get().scale(f1 / f, f1 / f, f1 / f);
                GlStateManager.get().translate(0.0F, 16.0F * scale, 0.0F);
            }

            this.field_177209_a.postRender(0.0625F);
            GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);

            if (item instanceof ItemBlock)
            {
                float f2 = 0.625F;
                GlStateManager.get().translate(0.0F, -0.25F, 0.0F);
                GlStateManager.get().rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.get().scale(f2, -f2, -f2);

                if (flag)
                {
                    GlStateManager.get().translate(0.0F, 0.1875F, 0.0F);
                }

                minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.HEAD);
            }
            else if (item == Items.skull)
            {
                float f3 = 1.1875F;
                GlStateManager.get().scale(f3, -f3, -f3);

                if (flag)
                {
                    GlStateManager.get().translate(0.0F, 0.0625F, 0.0F);
                }

                GameProfile gameprofile = null;

                if (itemstack.hasTagCompound())
                {
                    NBTTagCompound nbttagcompound = itemstack.getTagCompound();

                    if (nbttagcompound.hasKey("SkullOwner", 10))
                    {
                        gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                    }
                    else if (nbttagcompound.hasKey("SkullOwner", 8))
                    {
                        String s = nbttagcompound.getString("SkullOwner");

                        if (!StringUtils.isNullOrEmpty(s))
                        {
                            gameprofile = TileEntitySkull.updateGameprofile(new GameProfile((UUID)null, s));
                            nbttagcompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
                        }
                    }
                }

                TileEntitySkullRenderer.instance.renderSkull(-0.5F, 0.0F, -0.5F, EnumFacing.UP, 180.0F, itemstack.getMetadata(), gameprofile, -1);
            }

            GlStateManager.get().popMatrix();
        }
    }

    public boolean shouldCombineTextures()
    {
        return true;
    }
}
