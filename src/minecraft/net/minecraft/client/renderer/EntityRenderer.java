package net.minecraft.client.renderer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.display.Display;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import com.google.common.base.Predicates;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientEngine;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.optifine.*;
import net.minecraft.optifine.shadersmod.client.Shaders;
import net.minecraft.optifine.shadersmod.client.ShadersRender;
import net.minecraft.potion.Potion;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;

public class EntityRenderer implements IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation locationSnowPng = new ResourceLocation("textures/environment/snow.png");
    public static boolean anaglyphEnable;

    /** Anaglyph field (0=R, 1=GB) */
    public static int anaglyphField;

    /** A reference to the Minecraft object. */
    private ClientEngine mc;
    private final IResourceManager resourceManager;
    private Random random = new Random();
    private float farPlaneDistance;
    public ItemRenderer itemRenderer;
    private final MapItemRenderer theMapItemRenderer;

    /** Entity renderer update count */
    private int rendererUpdateCount;

    /** Pointed entity */
    private Entity pointedEntity;
    private MouseFilter mouseFilterXAxis = new MouseFilter();
    private MouseFilter mouseFilterYAxis = new MouseFilter();
    private float thirdPersonDistance = 4.0F;

    /** Third person distance temp */
    private float thirdPersonDistanceTemp = 4.0F;

    /** Smooth cam yaw */
    public float smoothCamYaw;

    /** Smooth cam pitch */
    public float smoothCamPitch;

    /** Smooth cam filter X */
    public float smoothCamFilterX;

    /** Smooth cam filter Y */
    public float smoothCamFilterY;

    /** Smooth cam partial ticks */
    public float smoothCamPartialTicks;

    /** FOV modifier hand */
    private float fovModifierHand;

    /** FOV modifier hand prev */
    private float fovModifierHandPrev;
    private float bossColorModifier;
    private float bossColorModifierPrev;

    /** Cloud fog mode */
    private boolean cloudFog;
    private boolean renderHand = true;
    private boolean drawBlockOutline = true;

    /** Previous frame time in milliseconds */
    private long prevFrameTime = ClientEngine.getSystemTime();

    /** End time of last render (ns) */
    private long renderEndNanoTime;

    /**
     * The texture id of the blocklight/skylight texture used for lighting effects
     */
    private final DynamicTexture lightmapTexture;

    /**
     * Colors computed in updateLightmap() and loaded into the lightmap emptyTexture
     */
    private final int[] lightmapColors;
    private final ResourceLocation locationLightMap;

    /**
     * Is set, updateCameraAndRender() calls updateLightmap(); set by updateTorchFlicker()
     */
    private boolean lightmapUpdateNeeded;

    /** Torch flicker X */
    private float torchFlickerX;
    private float torchFlickerDX;

    /** Rain sound counter */
    private int rainSoundCounter;
    private float[] rainXCoords = new float[1024];
    private float[] rainYCoords = new float[1024];

    /** Fog color buffer */
    private FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
    public float fogColorRed;
    public float fogColorGreen;
    public float fogColorBlue;

    /** Fog color 2 */
    private float fogColor2;

    /** Fog color 1 */
    private float fogColor1;
    private int debugViewDirection = 0;
    private boolean debugView = false;
    private double cameraZoom = 1.0D;
    private double cameraYaw;
    private double cameraPitch;
    private ShaderGroup theShaderGroup;
    private static final ResourceLocation[] shaderResourceLocations = new ResourceLocation[] {new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
    public static final int shaderCount = shaderResourceLocations.length;
    private int shaderIndex;
    private boolean useShader;
    public int frameCount;
    private static final String __OBFID = "CL_00000947";
    private boolean initialized = false;
    private World updatedWorld = null;
    private boolean showDebugInfo = false;
    public boolean fogStandard = false;
    private float clipDistance = 128.0F;
    private long lastServerTime = 0L;
    private int lastServerTicks = 0;
    private int serverWaitTime = 0;
    private int serverWaitTimeCurrent = 0;
    private float avgServerTimeDiff = 0.0F;
    private float avgServerTickDiff = 0.0F;
    private long lastErrorCheckTimeMs = 0L;
    private ShaderGroup[] fxaaShaders = new ShaderGroup[10];
    private int glDebugCrosshairList = -1;

    public EntityRenderer(ClientEngine mcIn, IResourceManager resourceManagerIn)
    {
        this.shaderIndex = shaderCount;
        this.useShader = false;
        this.frameCount = 0;
        this.mc = mcIn;
        this.resourceManager = resourceManagerIn;
        this.itemRenderer = mcIn.getItemRenderer();
        this.theMapItemRenderer = new MapItemRenderer(mcIn.getTextureManager());
        this.lightmapTexture = new DynamicTexture(16, 16);
        this.locationLightMap = mcIn.getTextureManager().getDynamicTextureLocation("lightMap", this.lightmapTexture);
        this.lightmapColors = this.lightmapTexture.getTextureData();
        this.theShaderGroup = null;

        for (int i = 0; i < 32; ++i)
        {
            for (int j = 0; j < 32; ++j)
            {
                float f = (float)(j - 16);
                float f1 = (float)(i - 16);
                float f2 = MathHelper.sqrt_float(f * f + f1 * f1);
                this.rainXCoords[i << 5 | j] = -f1 / f2;
                this.rainYCoords[i << 5 | j] = f / f2;
            }
        }
    }

    public boolean isShaderActive()
    {
        return OpenGlHelper.shadersSupported && this.theShaderGroup != null;
    }

    public void func_181022_b()
    {
        if (this.theShaderGroup != null)
        {
            this.theShaderGroup.deleteShaderGroup();
        }

        this.theShaderGroup = null;
        this.shaderIndex = shaderCount;
    }

    public void switchUseShader()
    {
        this.useShader = !this.useShader;
    }

    /**
     * What shader to use when spectating this entity
     */
    public void loadEntityShader(Entity entityIn)
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (this.theShaderGroup != null)
            {
                this.theShaderGroup.deleteShaderGroup();
            }

            this.theShaderGroup = null;

            if (entityIn instanceof EntityCreeper)
            {
                this.loadShader(new ResourceLocation("shaders/post/creeper.json"));
            }
            else if (entityIn instanceof EntitySpider)
            {
                this.loadShader(new ResourceLocation("shaders/post/spider.json"));
            }
            else if (entityIn instanceof EntityEnderman)
            {
                this.loadShader(new ResourceLocation("shaders/post/invert.json"));
            }
            else if (Reflector.ForgeHooksClient_loadEntityShader.exists())
            {
                Reflector.call(Reflector.ForgeHooksClient_loadEntityShader, new Object[] {entityIn, this});
            }
        }
    }

    public void activateNextShader()
    {
        if (OpenGlHelper.shadersSupported && this.mc.getRenderViewEntity() instanceof EntityPlayer)
        {
            if (this.theShaderGroup != null)
            {
                this.theShaderGroup.deleteShaderGroup();
            }

            this.shaderIndex = (this.shaderIndex + 1) % (shaderResourceLocations.length + 1);

            if (this.shaderIndex != shaderCount)
            {
                this.loadShader(shaderResourceLocations[this.shaderIndex]);
            }
            else
            {
                this.theShaderGroup = null;
            }
        }
    }

    private void loadShader(ResourceLocation resourceLocationIn)
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            try
            {
                this.theShaderGroup = new ShaderGroup(this.mc.getTextureManager(), this.resourceManager, this.mc.getFramebuffer(), resourceLocationIn);
                this.theShaderGroup.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
                this.useShader = true;
            }
            catch (IOException ioexception)
            {
                this.shaderIndex = shaderCount;
                this.useShader = false;
            }
            catch (JsonSyntaxException jsonsyntaxexception)
            {
                this.shaderIndex = shaderCount;
                this.useShader = false;
            }
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        if (this.theShaderGroup != null)
        {
            this.theShaderGroup.deleteShaderGroup();
        }

        this.theShaderGroup = null;

        if (this.shaderIndex != shaderCount)
        {
            this.loadShader(shaderResourceLocations[this.shaderIndex]);
        }
        else
        {
            this.loadEntityShader(this.mc.getRenderViewEntity());
        }
    }

    /**
     * Updates the entity renderer
     */
    public void updateRenderer()
    {
        if (OpenGlHelper.shadersSupported && ShaderLinkHelper.getStaticShaderLinkHelper() == null)
        {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
        }

        this.updateFovModifierHand();
        this.updateTorchFlicker();
        this.fogColor2 = this.fogColor1;
        this.thirdPersonDistanceTemp = this.thirdPersonDistance;

        if (this.mc.options.smoothCamera)
        {
            float f = this.mc.options.mouseSensitivity * 0.6F + 0.2F;
            float f1 = f * f * f * 8.0F;
            this.smoothCamFilterX = this.mouseFilterXAxis.smooth(this.smoothCamYaw, 0.05F * f1);
            this.smoothCamFilterY = this.mouseFilterYAxis.smooth(this.smoothCamPitch, 0.05F * f1);
            this.smoothCamPartialTicks = 0.0F;
            this.smoothCamYaw = 0.0F;
            this.smoothCamPitch = 0.0F;
        }
        else
        {
            this.smoothCamFilterX = 0.0F;
            this.smoothCamFilterY = 0.0F;
            this.mouseFilterXAxis.reset();
            this.mouseFilterYAxis.reset();
        }

        if (this.mc.getRenderViewEntity() == null)
        {
            this.mc.setRenderViewEntity(this.mc.player);
        }

        Entity entity = this.mc.getRenderViewEntity();
        double d0 = entity.posX;
        double d1 = entity.posY + (double)entity.getEyeHeight();
        double d2 = entity.posZ;
        float f3 = this.mc.world.getLightBrightness(new BlockPos(d0, d1, d2));
        float f4 = (float)this.mc.options.renderDistanceChunks / 16.0F;
        f4 = MathHelper.clamp_float(f4, 0.0F, 1.0F);
        float f2 = f3 * (1.0F - f4) + f4;
        this.fogColor1 += (f2 - this.fogColor1) * 0.1F;
        ++this.rendererUpdateCount;
        this.itemRenderer.updateEquippedItem();
        this.addRainParticles();
        this.bossColorModifierPrev = this.bossColorModifier;

        if (BossStatus.hasColorModifier)
        {
            this.bossColorModifier += 0.05F;

            if (this.bossColorModifier > 1.0F)
            {
                this.bossColorModifier = 1.0F;
            }

            BossStatus.hasColorModifier = false;
        }
        else if (this.bossColorModifier > 0.0F)
        {
            this.bossColorModifier -= 0.0125F;
        }
    }

    public ShaderGroup getShaderGroup()
    {
        return this.theShaderGroup;
    }

    public void updateShaderGroupSize(int width, int height)
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (this.theShaderGroup != null)
            {
                this.theShaderGroup.createBindFramebuffers(width, height);
            }

            this.mc.renderGlobal.createBindEntityOutlineFbs(width, height);
        }
    }

    /**
     * Finds what block or object the mouse is over at the specified partial tick time. Args: partialTickTime
     */
    public void getMouseOver(float partialTicks)
    {
        Entity entity = this.mc.getRenderViewEntity();

        if (entity != null && this.mc.world != null)
        {
            this.mc.pointedEntity = null;
            double d0 = (double)this.mc.playerController.getBlockReachDistance();
            this.mc.hitResult = entity.rayTrace(d0, partialTicks);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            boolean flag1 = true;

            if (this.mc.playerController.extendedReach())
            {
                d0 = 6.0D;
                d1 = 6.0D;
            }
            else
            {
                if (d0 > 3.0D)
                {
                    flag = true;
                }
            }

            if (this.mc.hitResult != null)
            {
                d1 = this.mc.hitResult.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = entity.getLook(partialTicks);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            this.pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List list = this.mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double)f, (double)f, (double)f), Predicates.and(EntitySelectors.NOT_SPECTATING, new EntityRenderer1(this)));
            double d2 = d1;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = (Entity)list.get(i);
                float f1 = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3))
                {
                    if (d2 >= 0.0D)
                    {
                        this.pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                }
                else if (movingobjectposition != null)
                {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D)
                    {
                        boolean flag2 = false;

                        if (Reflector.ForgeEntity_canRiderInteract.exists())
                        {
                            flag2 = Reflector.callBoolean(entity1, Reflector.ForgeEntity_canRiderInteract, new Object[0]);
                        }

                        if (entity1 == entity.ridingEntity && !flag2)
                        {
                            if (d2 == 0.0D)
                            {
                                this.pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                            }
                        }
                        else
                        {
                            this.pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (this.pointedEntity != null && flag && vec3.distanceTo(vec33) > 3.0D)
            {
                this.pointedEntity = null;
                this.mc.hitResult = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, (EnumFacing)null, new BlockPos(vec33));
            }

            if (this.pointedEntity != null && (d2 < d1 || this.mc.hitResult == null))
            {
                this.mc.hitResult = new MovingObjectPosition(this.pointedEntity, vec33);

                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame)
                {
                    this.mc.pointedEntity = this.pointedEntity;
                }
            }
        }
    }

    /**
     * Update FOV modifier hand
     */
    private void updateFovModifierHand()
    {
        float f = 1.0F;

        if (this.mc.getRenderViewEntity() instanceof AbstractClientPlayer)
        {
            AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)this.mc.getRenderViewEntity();
            f = abstractclientplayer.getFovModifier();
        }

        this.fovModifierHandPrev = this.fovModifierHand;
        this.fovModifierHand += (f - this.fovModifierHand) * 0.5F;

        if (this.fovModifierHand > 1.5F)
        {
            this.fovModifierHand = 1.5F;
        }

        if (this.fovModifierHand < 0.1F)
        {
            this.fovModifierHand = 0.1F;
        }
    }

    /**
     * Changes the field of view of the player depending on if they are underwater or not
     */
    private float getFOVModifier(float partialTicks, boolean p_78481_2_)
    {
        if (this.debugView)
        {
            return 90.0F;
        }
        else
        {
            Entity entity = this.mc.getRenderViewEntity();
            float f = 70.0F;

            if (p_78481_2_)
            {
                f = this.mc.options.fovSetting;

                if (Config.get().isDynamicFov())
                {
                    f *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * partialTicks;
                }
            }

            boolean flag = false;

            if (this.mc.currentScreen == null)
            {
                GameOptions options = this.mc.options;
                flag = GameOptions.isKeyDown(this.mc.options.ofKeyBindZoom);
            }

            if (flag)
            {
                if (!Config.get().zoomMode)
                {
                    Config.get().zoomMode = true;
                    this.mc.options.smoothCamera = true;
                }

                if (Config.get().zoomMode)
                {
                    f /= 4.0F;
                }
            }
            else if (Config.get().zoomMode)
            {
                Config.get().zoomMode = false;
                this.mc.options.smoothCamera = false;
                this.mouseFilterXAxis = new MouseFilter();
                this.mouseFilterYAxis = new MouseFilter();
                this.mc.renderGlobal.displayListEntitiesDirty = true;
            }

            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHealth() <= 0.0F)
            {
                float f1 = (float)((EntityLivingBase)entity).deathTime + partialTicks;
                f /= (1.0F - 500.0F / (f1 + 500.0F)) * 2.0F + 1.0F;
            }

            Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.world, entity, partialTicks);

            if (block.getMaterial() == Material.water)
            {
                f = f * 60.0F / 70.0F;
            }

            return f;
        }
    }

    private void hurtCameraEffect(float partialTicks)
    {
        if (this.mc.getRenderViewEntity() instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)this.mc.getRenderViewEntity();
            float f = (float)entitylivingbase.hurtTime - partialTicks;

            if (entitylivingbase.getHealth() <= 0.0F)
            {
                float f1 = (float)entitylivingbase.deathTime + partialTicks;
                GlStateManager.get().rotate(40.0F - 8000.0F / (f1 + 200.0F), 0.0F, 0.0F, 1.0F);
            }

            if (f < 0.0F)
            {
                return;
            }

            f = f / (float)entitylivingbase.maxHurtTime;
            f = MathHelper.sin(f * f * f * f * (float)Math.PI);
            float f2 = entitylivingbase.attackedAtYaw;
            GlStateManager.get().rotate(-f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.get().rotate(-f * 14.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.get().rotate(f2, 0.0F, 1.0F, 0.0F);
        }
    }

    /**
     * Setups all the GL settings for view bobbing. Args: partialTickTime
     */
    private void setupViewBobbing(float partialTicks)
    {
        if (this.mc.getRenderViewEntity() instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)this.mc.getRenderViewEntity();
            float f = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float f1 = -(entityplayer.distanceWalkedModified + f * partialTicks);
            float f2 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
            float f3 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;
            GlStateManager.get().translate(MathHelper.sin(f1 * (float)Math.PI) * f2 * 0.5F, -Math.abs(MathHelper.cos(f1 * (float)Math.PI) * f2), 0.0F);
            GlStateManager.get().rotate(MathHelper.sin(f1 * (float)Math.PI) * f2 * 3.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.get().rotate(Math.abs(MathHelper.cos(f1 * (float)Math.PI - 0.2F) * f2) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.get().rotate(f3, 1.0F, 0.0F, 0.0F);
        }
    }

    /**
     * sets up player's eye (or camera in third person mode)
     */
    private void orientCamera(float partialTicks)
    {
        Entity entity = this.mc.getRenderViewEntity();
        float f = entity instanceof EntityPlayerSP ? ((EntityPlayerSP) entity).getEyeHeight(partialTicks) : entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPlayerSleeping())
        {
            f = (float)((double)f + 1.0D);
            GlStateManager.get().translate(0.0F, 0.3F, 0.0F);

            if (!this.mc.options.debugCamEnable)
            {
                BlockPos blockpos = new BlockPos(entity);
                IBlockState iblockstate = this.mc.world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (Reflector.ForgeHooksClient_orientBedCamera.exists())
                {
                    Reflector.callVoid(Reflector.ForgeHooksClient_orientBedCamera, new Object[] {this.mc.world, blockpos, iblockstate, entity});
                }
                else if (block == Blocks.bed)
                {
                    int j = ((EnumFacing)iblockstate.getValue(BlockBed.FACING)).getHorizontalIndex();
                    GlStateManager.get().rotate((float)(j * 90), 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.get().rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.get().rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
            }
        }
        else if (this.mc.options.thirdPersonView > 0)
        {
            double d3 = (double)(this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * partialTicks);

            if (this.mc.options.debugCamEnable)
            {
                GlStateManager.get().translate(0.0F, 0.0F, (float)(-d3));
            }
            else
            {
                float f1 = entity.rotationYaw;
                float f2 = entity.rotationPitch;

                if (this.mc.options.thirdPersonView == 2)
                {
                    f2 += 180.0F;
                }

                double d4 = (double)(-MathHelper.sin(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d3;
                double d5 = (double)(MathHelper.cos(f1 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d3;
                double d6 = (double)(-MathHelper.sin(f2 / 180.0F * (float)Math.PI)) * d3;

                for (int i = 0; i < 8; ++i)
                {
                    float f3 = (float)((i & 1) * 2 - 1);
                    float f4 = (float)((i >> 1 & 1) * 2 - 1);
                    float f5 = (float)((i >> 2 & 1) * 2 - 1);
                    f3 = f3 * 0.1F;
                    f4 = f4 * 0.1F;
                    f5 = f5 * 0.1F;
                    MovingObjectPosition movingobjectposition = this.mc.world.rayTraceBlocks(new Vec3(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), new Vec3(d0 - d4 + (double)f3 + (double)f5, d1 - d6 + (double)f4, d2 - d5 + (double)f5), false, true, false);

                    if (movingobjectposition != null)
                    {
                        double d7 = movingobjectposition.hitVec.distanceTo(new Vec3(d0, d1, d2));

                        if (d7 < d3)
                        {
                            d3 = d7;
                        }
                    }
                }

                if (this.mc.options.thirdPersonView == 2)
                {
                    GlStateManager.get().rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.get().rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                GlStateManager.get().rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                GlStateManager.get().translate(0.0F, 0.0F, (float)(-d3));
                GlStateManager.get().rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.get().rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        }

        if (Reflector.EntityViewRenderEvent_CameraSetup_Constructor.exists())
        {
            if (!this.mc.options.debugCamEnable)
            {
                float f6 = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
                float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                float f8 = 0.0F;

                if (entity instanceof EntityAnimal)
                {
                    EntityAnimal entityanimal = (EntityAnimal)entity;
                    f6 = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
                }

                Block block1 = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.world, entity, partialTicks);
                Object object = Reflector.newInstance(Reflector.EntityViewRenderEvent_CameraSetup_Constructor, new Object[] {this, entity, block1, Float.valueOf(partialTicks), Float.valueOf(f6), Float.valueOf(f7), Float.valueOf(f8)});
                Reflector.postForgeBusEvent(object);
                f8 = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_CameraSetup_roll, f8);
                f7 = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_CameraSetup_pitch, f7);
                f6 = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_CameraSetup_yaw, f6);
                GlStateManager.get().rotate(f8, 0.0F, 0.0F, 1.0F);
                GlStateManager.get().rotate(f7, 1.0F, 0.0F, 0.0F);
                GlStateManager.get().rotate(f6, 0.0F, 1.0F, 0.0F);
            }
        }
        else if (!this.mc.options.debugCamEnable)
        {
            float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            GlStateManager.get().rotate(pitch, 1.0F, 0.0F, 0.0F);

            if (entity instanceof EntityAnimal)
            {
                EntityAnimal entityanimal1 = (EntityAnimal)entity;
                GlStateManager.get().rotate(entityanimal1.prevRotationYawHead + (entityanimal1.rotationYawHead - entityanimal1.prevRotationYawHead) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
                GlStateManager.get().rotate(yaw + 180.0F, 0.0F, 1.0F, 0.0F);
            }
        }

        GlStateManager.get().translate(0.0F, -f, 0.0F);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
    }

    /**
     * sets up projection, view effects, camera position/rotation
     */
    public void setupCameraTransform(float partialTicks, int pass)
    {
        this.farPlaneDistance = (float)(this.mc.options.renderDistanceChunks * 16);

        if (Config.get().isFogFancy())
        {
            this.farPlaneDistance *= 0.95F;
        }

        if (Config.get().isFogFast())
        {
            this.farPlaneDistance *= 0.83F;
        }

        GlStateManager.get().matrixMode(5889);
        GlStateManager.get().loadIdentity();
        float f = 0.07F;

        if (this.mc.options.anaglyph)
        {
            GlStateManager.get().translate((float)(-(pass * 2 - 1)) * f, 0.0F, 0.0F);
        }

        this.clipDistance = this.farPlaneDistance * 2.0F;

        if (this.clipDistance < 173.0F)
        {
            this.clipDistance = 173.0F;
        }

        if (this.mc.world.provider.getDimensionId() == 1)
        {
            this.clipDistance = 256.0F;
        }

        if (this.cameraZoom != 1.0D)
        {
            GlStateManager.get().translate((float)this.cameraYaw, (float)(-this.cameraPitch), 0.0F);
            GlStateManager.get().scale(this.cameraZoom, this.cameraZoom, 1.0D);
        }

        Project.get().gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance);
        GlStateManager.get().matrixMode(5888);
        GlStateManager.get().loadIdentity();

        if (this.mc.options.anaglyph)
        {
            GlStateManager.get().translate((float)(pass * 2 - 1) * 0.1F, 0.0F, 0.0F);
        }

        this.hurtCameraEffect(partialTicks);

        if (this.mc.options.viewBobbing)
        {
            this.setupViewBobbing(partialTicks);
        }

        float f1 = this.mc.player.prevTimeInPortal + (this.mc.player.timeInPortal - this.mc.player.prevTimeInPortal) * partialTicks;

        if (f1 > 0.0F)
        {
            byte b0 = 20;

            if (this.mc.player.isPotionActive(Potion.confusion))
            {
                b0 = 7;
            }

            float f2 = 5.0F / (f1 * f1 + 5.0F) - f1 * 0.04F;
            f2 = f2 * f2;
            GlStateManager.get().rotate(((float)this.rendererUpdateCount + partialTicks) * (float)b0, 0.0F, 1.0F, 1.0F);
            GlStateManager.get().scale(1.0F / f2, 1.0F, 1.0F);
            GlStateManager.get().rotate(-((float)this.rendererUpdateCount + partialTicks) * (float)b0, 0.0F, 1.0F, 1.0F);
        }

        this.orientCamera(partialTicks);

        if (this.debugView)
        {
            switch (this.debugViewDirection)
            {
                case 0:
                    GlStateManager.get().rotate(90.0F, 0.0F, 1.0F, 0.0F);
                    break;

                case 1:
                    GlStateManager.get().rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    break;

                case 2:
                    GlStateManager.get().rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                    break;

                case 3:
                    GlStateManager.get().rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    break;

                case 4:
                    GlStateManager.get().rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }
        }
    }

    /**
     * Render player hand
     */
    private void renderHand(float partialTicks, int xOffset)
    {
        this.renderHand(partialTicks, xOffset, true, true, false);
    }

    public void renderHand(float p_renderHand_1_, int p_renderHand_2_, boolean p_renderHand_3_, boolean p_renderHand_4_, boolean p_renderHand_5_)
    {
        if (!this.debugView)
        {
            GlStateManager.get().matrixMode(5889);
            GlStateManager.get().loadIdentity();
            float f = 0.07F;

            if (this.mc.options.anaglyph)
            {
                GlStateManager.get().translate((float)(-(p_renderHand_2_ * 2 - 1)) * f, 0.0F, 0.0F);
            }

            if (Config.get().isShaders())
            {
                Shaders.applyHandDepth();
            }

            Project.get().gluPerspective(this.getFOVModifier(p_renderHand_1_, false), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
            GlStateManager.get().matrixMode(5888);
            GlStateManager.get().loadIdentity();

            if (this.mc.options.anaglyph)
            {
                GlStateManager.get().translate((float)(p_renderHand_2_ * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }

            boolean flag = false;

            if (p_renderHand_3_)
            {
                GlStateManager.get().pushMatrix();
                this.hurtCameraEffect(p_renderHand_1_);

                if (this.mc.options.viewBobbing)
                {
                    this.setupViewBobbing(p_renderHand_1_);
                }

                flag = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase)this.mc.getRenderViewEntity()).isPlayerSleeping();
                boolean flag1 = !ReflectorForge.renderFirstPersonHand(this.mc.renderGlobal, p_renderHand_1_, p_renderHand_2_);

                if (flag1 && this.mc.options.thirdPersonView == 0 && !flag && !this.mc.options.hideGUI && !this.mc.playerController.isSpectator())
                {
                    this.enableLightmap();

                    if (Config.get().isShaders())
                    {
                        ShadersRender.renderItemFP(this.itemRenderer, p_renderHand_1_, p_renderHand_5_);
                    }
                    else
                    {
                        this.itemRenderer.renderItemInFirstPerson(p_renderHand_1_);
                    }

                    this.disableLightmap();
                }

                GlStateManager.get().popMatrix();
            }

            if (!p_renderHand_4_)
            {
                return;
            }

            this.disableLightmap();

            if (this.mc.options.thirdPersonView == 0 && !flag)
            {
                this.itemRenderer.renderOverlays(p_renderHand_1_);
                this.hurtCameraEffect(p_renderHand_1_);
            }

            if (this.mc.options.viewBobbing)
            {
                this.setupViewBobbing(p_renderHand_1_);
            }
        }
    }

    public void disableLightmap()
    {
        GlStateManager.get().setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.get().disableTexture2D();
        GlStateManager.get().setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.get().isShaders())
        {
            Shaders.disableLightmap();
        }
    }

    public void enableLightmap()
    {
        GlStateManager.get().setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.get().matrixMode(5890);
        GlStateManager.get().loadIdentity();
        float f = 0.00390625F;
        GlStateManager.get().scale(f, f, f);
        GlStateManager.get().translate(8.0F, 8.0F, 8.0F);
        GlStateManager.get().matrixMode(5888);
        this.mc.getTextureManager().bindTexture(this.locationLightMap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.get().enableTexture2D();
        GlStateManager.get().setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.get().isShaders())
        {
            Shaders.enableLightmap();
        }
    }

    /**
     * Recompute a random value that is applied to block color in updateLightmap()
     */
    private void updateTorchFlicker()
    {
        this.torchFlickerDX = (float)((double)this.torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
        this.torchFlickerDX = (float)((double)this.torchFlickerDX * 0.9D);
        this.torchFlickerX += (this.torchFlickerDX - this.torchFlickerX) * 1.0F;
        this.lightmapUpdateNeeded = true;
    }

    private void updateLightmap(float partialTicks)
    {
        if (this.lightmapUpdateNeeded)
        {
            WorldClient worldclient = this.mc.world;

            if (worldclient != null)
            {
                if (Config.get().isCustomColors() && CustomColors.updateLightmap(worldclient, this.torchFlickerX, this.lightmapColors, this.mc.player.isPotionActive(Potion.nightVision)))
                {
                    this.lightmapTexture.updateDynamicTexture();
                    this.lightmapUpdateNeeded = false;
                    return;
                }

                for (int i = 0; i < 256; ++i)
                {
                    this.lightmapColors[i] = -1;
                }

                this.lightmapTexture.updateDynamicTexture();
                this.lightmapUpdateNeeded = false;
            }
        }
    }

    public float getNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks)
    {
        int i = entitylivingbaseIn.getActivePotionEffect(Potion.nightVision).getDuration();
        return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float)i - partialTicks) * (float)Math.PI * 0.2F) * 0.3F;
    }

    public void updateCameraAndRender(float partialTicks, long p_181560_2_)
    {
        this.frameInit();
        this.prevFrameTime = ClientEngine.getSystemTime();

        if (Display.get().isActive() && ClientEngine.isRunningOnMac && this.mc.inGameHasFocus && !Mouse.get().isInsideWindow())
        {
            Mouse.get().setGrabbed(false);
            Mouse.get().setCursorPosition(Display.get().getWidth() / 2, Display.get().getHeight() / 2);
            Mouse.get().setGrabbed(true);
        }

        if (!this.mc.skipRenderWorld)
        {
            anaglyphEnable = this.mc.options.anaglyph;
            final ScaledResolution scaledresolution = ScaledResolution.get();
            int l = scaledresolution.getScaledWidth();
            int i1 = scaledresolution.getScaledHeight();
            final int j1 = Mouse.get().getX() * l / this.mc.displayWidth;
            final int k1 = i1 - Mouse.get().getY() * i1 / this.mc.displayHeight - 1;

            if (this.mc.world != null)
            {
                int i = Math.max(mc.getDebugFPS(), 60);
                long j = System.nanoTime() - p_181560_2_;
                long k = Math.max((long)(1000000000 / i / 4) - j, 0L);
                this.renderWorld(partialTicks, System.nanoTime() + k);

                if (OpenGlHelper.shadersSupported)
                {
                    this.mc.renderGlobal.renderEntityOutlineFramebuffer();

                    if (this.theShaderGroup != null && this.useShader)
                    {
                        GlStateManager.get().matrixMode(5890);
                        GlStateManager.get().pushMatrix();
                        GlStateManager.get().loadIdentity();
                        this.theShaderGroup.loadShaderGroup(partialTicks);
                        GlStateManager.get().popMatrix();
                    }

                    this.mc.getFramebuffer().bindFramebuffer(true);
                }

                this.renderEndNanoTime = System.nanoTime();

                if (!this.mc.options.hideGUI || this.mc.currentScreen != null)
                {
                    GlStateManager.get().alphaFunc(516, 0.0F);
                    if (!mc.isHeadless()) this.mc.ingameGUI.renderGameOverlay(partialTicks);

                    if (this.mc.options.ofShowFps && !this.mc.options.showDebugInfo)
                    {
                        Config.get().drawFps();
                    }

                    if (this.mc.options.showDebugInfo)
                    {
                        Lagometer.showLagometer(scaledresolution);
                    }
                }
            }
            else
            {
                GlStateManager.get().viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
                GlStateManager.get().matrixMode(5889);
                GlStateManager.get().loadIdentity();
                GlStateManager.get().matrixMode(5888);
                GlStateManager.get().loadIdentity();
                this.setupOverlayRendering();
                this.renderEndNanoTime = System.nanoTime();
                
                if (TileEntityRendererDispatcher.instance == null)
                {
                    TileEntityRendererDispatcher.init(mc); 
                }
                
                TileEntityRendererDispatcher.instance.renderEngine = this.mc.getTextureManager();
            }

            if (this.mc.currentScreen != null)
            {
                GlStateManager.get().clear(256);

                try
                {
                    if (Reflector.ForgeHooksClient_drawScreen.exists())
                    {
                        Reflector.callVoid(Reflector.ForgeHooksClient_drawScreen, new Object[] {this.mc.currentScreen, Integer.valueOf(j1), Integer.valueOf(k1), Float.valueOf(partialTicks)});
                    }
                    else
                    {
                        if (!mc.isHeadless()) this.mc.currentScreen.drawScreen(j1, k1, partialTicks);
                    }
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering screen");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Screen render details");
                    crashreportcategory.addCrashSectionCallable("Screen name", new EntityRenderer2(this));
                    crashreportcategory.addCrashSectionCallable("Mouse location", new Callable()
                    {
                        private static final String __OBFID = "CL_00000950";
                        public String call() throws Exception
                        {
                            return String.format("Scaled: (%d, %d). Absolute: (%d, %d)", new Object[] {Integer.valueOf(j1), Integer.valueOf(k1), Integer.valueOf(Mouse.get().getX()), Integer.valueOf(Mouse.get().getY())});
                        }
                    });
                    crashreportcategory.addCrashSectionCallable("Screen size", new Callable()
                    {
                        private static final String __OBFID = "CL_00000951";
                        public String call() throws Exception
                        {
                            return String.format("Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %d", new Object[] {Integer.valueOf(scaledresolution.getScaledWidth()), Integer.valueOf(scaledresolution.getScaledHeight()), Integer.valueOf(EntityRenderer.this.mc.displayWidth), Integer.valueOf(EntityRenderer.this.mc.displayHeight), Integer.valueOf(scaledresolution.getScaleFactor())});
                        }
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }

        this.frameFinish();
        this.waitForServerThread();
        Lagometer.updateLagometer(mc);

        if (this.mc.options.ofProfiler)
        {
            this.mc.options.showDebugProfilerChart = true;
        }
    }

    private boolean isDrawBlockOutline()
    {
        if (!this.drawBlockOutline)
        {
            return false;
        }
        else
        {
            Entity entity = this.mc.getRenderViewEntity();
            boolean flag = entity instanceof EntityPlayer && !this.mc.options.hideGUI;

            if (flag && !((EntityPlayer)entity).capabilities.allowEdit)
            {
                ItemStack itemstack = ((EntityPlayer)entity).getCurrentEquippedItem();

                if (this.mc.hitResult != null && this.mc.hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                {
                    BlockPos blockpos = this.mc.hitResult.getBlockPos();
                    IBlockState iblockstate = this.mc.world.getBlockState(blockpos);
                    Block block = iblockstate.getBlock();

                    if (this.mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR)
                    {
                        flag = ReflectorForge.blockHasTileEntity(iblockstate) && this.mc.world.getTileEntity(blockpos) instanceof IInventory;
                    }
                    else
                    {
                        flag = itemstack != null && (itemstack.canDestroy(block) || itemstack.canPlaceOn(block));
                    }
                }
            }

            return flag;
        }
    }

    private void renderWorldDirections(float partialTicks)
    {
        if (this.mc.options.showDebugInfo && !this.mc.options.hideGUI && !this.mc.player.hasReducedDebug() && !this.mc.options.reducedDebugInfo)
        {
            Entity entity = this.mc.getRenderViewEntity();
            GlStateManager.get().enableBlend();
            GlStateManager.get().tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.get().disableTexture2D();
            GlStateManager.get().depthMask(false);
            GlStateManager.get().pushMatrix();
            GlStateManager.get().matrixMode(5888);
            GL11.glLineWidth(2.0F);
            GlStateManager.get().loadIdentity();
            GlStateManager.get().translate(0, 0, -0.1D);
            this.orientCamera(partialTicks);
            GlStateManager.get().translate(0.0F, entity.getEyeHeight(), 0.0F);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            
            if (glDebugCrosshairList == -1)
            {
                glDebugCrosshairList = GL11.glGenLists(1);
                GL11.glNewList(glDebugCrosshairList, GL11.GL_COMPILE);
                // Shadow
                AxisAlignedBB x = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.004D, 0.00001, 0.00001),
                        y = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.00001, 0.00001, 0.004D),
                        z = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.00001, 0.004D, 0.00001);
                RenderGlobal.drawBox(x.expand(0.0001, 0.0001, 0.0001), 0, 0, 0, 255);
                RenderGlobal.drawBox(y.expand(0.0001, 0.0001, 0.0001), 0, 0, 0, 255);
                RenderGlobal.drawBox(z.expand(0.0001, 0.0001, 0.0001), 0, 0, 0, 255);
                // Main
                RenderGlobal.drawBox(x, 255, 0, 0, 255);
                RenderGlobal.drawBox(y, 0, 0, 255, 255);
                RenderGlobal.drawBox(z, 0, 255, 0, 255);
                GL11.glEndList();
            }
            
            GL11.glCallList(glDebugCrosshairList);
            GL11.glLineWidth(1.0F);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GlStateManager.get().popMatrix();
            GlStateManager.get().depthMask(true);
            GlStateManager.get().enableTexture2D();
            GlStateManager.get().disableBlend();
        }
    }

    public void renderWorld(float partialTicks, long finishTimeNano)
    {
        this.updateLightmap(partialTicks);

        if (this.mc.getRenderViewEntity() == null)
        {
            this.mc.setRenderViewEntity(this.mc.player);
        }

        this.getMouseOver(partialTicks);

        if (Config.get().isShaders())
        {
            Shaders.beginRender(this.mc, partialTicks, finishTimeNano);
        }

        GlStateManager.get().enableDepth();
        GlStateManager.get().enableAlpha();
        GlStateManager.get().alphaFunc(516, 0.1F);

        if (this.mc.options.anaglyph)
        {
            anaglyphField = 0;
            GlStateManager.get().colorMask(false, true, true, false);
            this.renderWorldPass(0, partialTicks, finishTimeNano);
            anaglyphField = 1;
            GlStateManager.get().colorMask(true, false, false, false);
            this.renderWorldPass(1, partialTicks, finishTimeNano);
            GlStateManager.get().colorMask(true, true, true, false);
        }
        else
        {
            this.renderWorldPass(2, partialTicks, finishTimeNano);
        }
    }

    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano)
    {
        boolean flag = Config.get().isShaders();

        if (flag)
        {
            Shaders.beginRenderPass(pass, partialTicks, finishTimeNano);
        }

        RenderGlobal renderglobal = this.mc.renderGlobal;
        EffectRenderer effectrenderer = this.mc.effectRenderer;
        boolean flag1 = this.isDrawBlockOutline();
        GlStateManager.get().enableCull();

        if (flag)
        {
            Shaders.setViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        }
        else
        {
            GlStateManager.get().viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        }

        this.updateFogColor(partialTicks);
        GlStateManager.get().clear(16640);

        if (flag)
        {
            Shaders.clearRenderBuffer();
        }

        this.setupCameraTransform(partialTicks, pass);

        if (flag)
        {
            Shaders.setCamera(partialTicks);
        }

        ActiveRenderInfo.updateRenderInfo(this.mc.player, this.mc.options.thirdPersonView == 2);
        ClippingHelperImpl.getInstance();
        Frustum frustum = new Frustum();
        Entity entity = this.mc.getRenderViewEntity();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;

        if (flag)
        {
            ShadersRender.setFrustrumPosition(frustum, d0, d1, d2);
        }
        else
        {
            frustum.setPosition(d0, d1, d2);
        }

        if ((Config.get().isSkyEnabled() || Config.get().isSunMoonEnabled() || Config.get().isStarsEnabled()) && !Shaders.isShadowPass)
        {
            this.setupFog(-1, partialTicks);
            GlStateManager.get().matrixMode(5889);
            GlStateManager.get().loadIdentity();
            Project.get().gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance);
            GlStateManager.get().matrixMode(5888);

            if (flag)
            {
                Shaders.beginSky();
            }

            renderglobal.renderSky(partialTicks, pass);

            if (flag)
            {
                Shaders.endSky();
            }

            GlStateManager.get().matrixMode(5889);
            GlStateManager.get().loadIdentity();
            Project.get().gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance);
            GlStateManager.get().matrixMode(5888);
        }
        else
        {
            GlStateManager.get().disableBlend();
        }

        this.setupFog(0, partialTicks);
        GlStateManager.get().shadeModel(7425);

        if (entity.posY + (double)entity.getEyeHeight() < 128.0D + (double)(this.mc.options.ofCloudsHeight * 128.0F))
        {
            this.renderCloudsCheck(renderglobal, partialTicks, pass);
        }

        this.setupFog(0, partialTicks);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        RenderHelper.get().disableStandardItemLighting();

        if (flag)
        {
            ShadersRender.setupTerrain(renderglobal, entity, (double)partialTicks, frustum, this.frameCount++, this.mc.player.isSpectator());
        }
        else
        {
            renderglobal.setupTerrain(entity, (double)partialTicks, frustum, this.frameCount++, this.mc.player.isSpectator());
        }

        if (pass == 0 || pass == 2)
        {
            Lagometer.timerChunkUpload.start();
            this.mc.renderGlobal.updateChunks(finishTimeNano);
            Lagometer.timerChunkUpload.end();
        }

        Lagometer.timerTerrain.start();

        if ((this.mc.options.ofSmoothFps || !Display.get().isActive()) && pass > 0)
        {
            GL11.glFinish();
        }

        GlStateManager.get().matrixMode(5888);
        GlStateManager.get().pushMatrix();
        GlStateManager.get().disableAlpha();

        if (flag)
        {
            ShadersRender.beginTerrainSolid();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.SOLID, (double)partialTicks, pass, entity);
        GlStateManager.get().enableAlpha();

        if (flag)
        {
            ShadersRender.beginTerrainCutoutMipped();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.CUTOUT_MIPPED, (double)partialTicks, pass, entity);
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);

        if (flag)
        {
            ShadersRender.beginTerrainCutout();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.CUTOUT, (double)partialTicks, pass, entity);
       // this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();

        if (flag)
        {
            ShadersRender.endTerrain();
        }

        Lagometer.timerTerrain.end();
        GlStateManager.get().shadeModel(7424);
        GlStateManager.get().alphaFunc(516, 0.0F);

        if (!this.debugView)
        {
            GlStateManager.get().matrixMode(5888);
            GlStateManager.get().popMatrix();
            GlStateManager.get().pushMatrix();
            RenderHelper.get().enableStandardItemLighting();

            if (Reflector.ForgeHooksClient_setRenderPass.exists())
            {
                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[] {Integer.valueOf(0)});
            }

            renderglobal.renderEntities(entity, frustum, partialTicks);

            if (Reflector.ForgeHooksClient_setRenderPass.exists())
            {
                Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[] {Integer.valueOf(-1)});
            }

            RenderHelper.get().disableStandardItemLighting();
            this.disableLightmap();
            GlStateManager.get().matrixMode(5888);
            GlStateManager.get().popMatrix();
            GlStateManager.get().pushMatrix();

            if (this.mc.hitResult != null && entity.isInsideOfMaterial(Material.water) && flag1)
            {
                EntityPlayer entityplayer = (EntityPlayer)entity;
                GlStateManager.get().disableAlpha();

                if ((!Reflector.ForgeHooksClient_onDrawBlockHighlight.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawBlockHighlight, new Object[] {renderglobal, entityplayer, this.mc.hitResult, Integer.valueOf(0), entityplayer.getHeldItem(), Float.valueOf(partialTicks)})) && !this.mc.options.hideGUI)
                {
                    renderglobal.drawSelectionBox(entityplayer, this.mc.hitResult, 0, partialTicks);
                }

                GlStateManager.get().enableAlpha();
            }
        }

        GlStateManager.get().matrixMode(5888);
        GlStateManager.get().popMatrix();

        if (!renderglobal.damagedBlocks.isEmpty())
        {
            GlStateManager.get().enableBlend();
            GlStateManager.get().tryBlendFuncSeparate(770, 1, 1, 0);
            this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
            renderglobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getWorldRenderer(), entity, partialTicks);
            this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
            GlStateManager.get().disableBlend();
        }

        GlStateManager.get().tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.get().disableBlend();

        if (!this.debugView)
        {
            this.enableLightmap();

            if (flag)
            {
                Shaders.beginLitParticles();
            }

            effectrenderer.renderLitParticles(entity, partialTicks);
            RenderHelper.get().disableStandardItemLighting();
            this.setupFog(0, partialTicks);

            if (flag)
            {
                Shaders.beginParticles();
            }

            effectrenderer.renderParticles(entity, partialTicks);

            if (flag)
            {
                Shaders.endParticles();
            }

            this.disableLightmap();
        }

        GlStateManager.get().depthMask(false);
        GlStateManager.get().enableCull();

        if (flag)
        {
            Shaders.beginWeather();
        }

        this.renderRainSnow(partialTicks);

        if (flag)
        {
            Shaders.endWeather();
        }

        GlStateManager.get().depthMask(true);
        renderglobal.renderWorldBorder(entity, partialTicks);

        if (flag)
        {
            ShadersRender.renderHand0(this, partialTicks, pass);
            Shaders.preWater();
        }

        GlStateManager.get().disableBlend();
        GlStateManager.get().enableCull();
        GlStateManager.get().tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.get().alphaFunc(516, 0.0F);
        this.setupFog(0, partialTicks);
        GlStateManager.get().enableBlend();
        GlStateManager.get().depthMask(false);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GlStateManager.get().shadeModel(7425);

        if (flag)
        {
            Shaders.beginWater();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.TRANSLUCENT, (double)partialTicks, pass, entity);

        if (flag)
        {
            Shaders.endWater();
        }

        if (flag1 && this.mc.hitResult != null && !entity.isInsideOfMaterial(Material.water))
        {
            EntityPlayer entityplayer1 = (EntityPlayer)entity;
            GlStateManager.get().disableAlpha();

            if ((!Reflector.ForgeHooksClient_onDrawBlockHighlight.exists() || !Reflector.callBoolean(Reflector.ForgeHooksClient_onDrawBlockHighlight, new Object[] {renderglobal, entityplayer1, this.mc.hitResult, Integer.valueOf(0), entityplayer1.getHeldItem(), Float.valueOf(partialTicks)})) && !this.mc.options.hideGUI)
            {
                renderglobal.drawSelectionBox(entityplayer1, this.mc.hitResult, 0, partialTicks);
            }

            GlStateManager.get().enableAlpha();
        }

        if (Reflector.ForgeHooksClient_setRenderPass.exists() && !this.debugView)
        {
            RenderHelper.get().enableStandardItemLighting();
            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[] {Integer.valueOf(1)});
            this.mc.renderGlobal.renderEntities(entity, frustum, partialTicks);
            GlStateManager.get().tryBlendFuncSeparate(770, 771, 1, 0);
            Reflector.callVoid(Reflector.ForgeHooksClient_setRenderPass, new Object[] {Integer.valueOf(-1)});
            RenderHelper.get().disableStandardItemLighting();
        }

        GlStateManager.get().shadeModel(7424);
        GlStateManager.get().depthMask(true);
        GlStateManager.get().enableCull();
        GlStateManager.get().disableBlend();
        GlStateManager.get().disableFog();

        if (entity.posY + (double)entity.getEyeHeight() >= 128.0D + (double)(this.mc.options.ofCloudsHeight * 128.0F))
        {
            this.renderCloudsCheck(renderglobal, partialTicks, pass);
        }

        if (Reflector.ForgeHooksClient_dispatchRenderLast.exists())
        {
            Reflector.callVoid(Reflector.ForgeHooksClient_dispatchRenderLast, new Object[] {renderglobal, Float.valueOf(partialTicks)});
        }

        boolean flag2 = ReflectorForge.renderFirstPersonHand(this.mc.renderGlobal, partialTicks, pass);

        if (!flag2 && this.renderHand && !Shaders.isShadowPass)
        {
            if (flag)
            {
                ShadersRender.renderHand1(this, partialTicks, pass);
                Shaders.renderCompositeFinal();
            }

            GlStateManager.get().clear(256);

            if (flag)
            {
                ShadersRender.renderFPOverlay(this, partialTicks, pass);
            }
            else
            {
                this.renderHand(partialTicks, pass);
            }

            this.renderWorldDirections(partialTicks);
        }

        if (flag)
        {
            Shaders.endRender();
        }
    }

    private void renderCloudsCheck(RenderGlobal renderGlobalIn, float partialTicks, int pass)
    {
        if (!Config.get().isCloudsOff() && Shaders.shouldRenderClouds(this.mc.options))
        {
            GlStateManager.get().matrixMode(5889);
            GlStateManager.get().loadIdentity();
            Project.get().gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance * 4.0F);
            GlStateManager.get().matrixMode(5888);
            GlStateManager.get().pushMatrix();
            this.setupFog(0, partialTicks);
            renderGlobalIn.renderClouds(partialTicks, pass);
            GlStateManager.get().disableFog();
            GlStateManager.get().popMatrix();
            GlStateManager.get().matrixMode(5889);
            GlStateManager.get().loadIdentity();
            Project.get().gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance);
            GlStateManager.get().matrixMode(5888);
        }
    }

    private void addRainParticles()
    {
        float f = this.mc.world.getRainStrength(1.0F);

        if (!Config.get().isRainFancy())
        {
            f /= 2.0F;
        }

        if (f != 0.0F && Config.get().isRainSplash())
        {
            this.random.setSeed((long)this.rendererUpdateCount * 312987231L);
            Entity entity = this.mc.getRenderViewEntity();
            WorldClient worldclient = this.mc.world;
            BlockPos blockpos = new BlockPos(entity);
            byte b0 = 10;
            double d0 = 0.0D;
            double d1 = 0.0D;
            double d2 = 0.0D;
            int i = 0;
            int j = (int)(100.0F * f * f);

            if (this.mc.options.particleSetting == 1)
            {
                j >>= 1;
            }
            else if (this.mc.options.particleSetting == 2)
            {
                j = 0;
            }

            for (int k = 0; k < j; ++k)
            {
                BlockPos blockpos1 = worldclient.getPrecipitationHeight(blockpos.add(this.random.nextInt(b0) - this.random.nextInt(b0), 0, this.random.nextInt(b0) - this.random.nextInt(b0)));
                BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(blockpos1);
                BlockPos blockpos2 = blockpos1.down();
                Block block = worldclient.getBlockState(blockpos2).getBlock();

                if (blockpos1.getY() <= blockpos.getY() + b0 && blockpos1.getY() >= blockpos.getY() - b0 && biomegenbase.canSpawnLightningBolt() && biomegenbase.getFloatTemperature(blockpos1) >= 0.15F)
                {
                    double d3 = this.random.nextDouble();
                    double d4 = this.random.nextDouble();

                    if (block.getMaterial() == Material.lava)
                    {
                        this.mc.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)blockpos1.getX() + d3, (double)((float)blockpos1.getY() + 0.1F) - block.getBlockBoundsMinY(), (double)blockpos1.getZ() + d4, 0.0D, 0.0D, 0.0D, new int[0]);
                    }
                    else if (block.getMaterial() != Material.air)
                    {
                        block.setBlockBoundsBasedOnState(worldclient, blockpos2);
                        ++i;

                        if (this.random.nextInt(i) == 0)
                        {
                            d0 = (double)blockpos2.getX() + d3;
                            d1 = (double)((float)blockpos2.getY() + 0.1F) + block.getBlockBoundsMaxY() - 1.0D;
                            d2 = (double)blockpos2.getZ() + d4;
                        }

                        this.mc.world.spawnParticle(EnumParticleTypes.WATER_DROP, (double)blockpos2.getX() + d3, (double)((float)blockpos2.getY() + 0.1F) + block.getBlockBoundsMaxY(), (double)blockpos2.getZ() + d4, 0.0D, 0.0D, 0.0D, new int[0]);
                    }
                }
            }

            if (i > 0 && this.random.nextInt(3) < this.rainSoundCounter++)
            {
                this.rainSoundCounter = 0;

                if (d1 > (double)(blockpos.getY() + 1) && worldclient.getPrecipitationHeight(blockpos).getY() > MathHelper.floor_float((float)blockpos.getY()))
                {
                    this.mc.world.playSound(d0, d1, d2, "ambient.weather.rain", 0.1F, 0.5F, false);
                }
                else
                {
                    this.mc.world.playSound(d0, d1, d2, "ambient.weather.rain", 0.2F, 1.0F, false);
                }
            }
        }
    }

    /**
     * Render rain and snow
     */
    protected void renderRainSnow(float partialTicks)
    {
        if (Reflector.ForgeWorldProvider_getWeatherRenderer.exists())
        {
            WorldProvider worldprovider = this.mc.world.provider;
            Object object = Reflector.call(worldprovider, Reflector.ForgeWorldProvider_getWeatherRenderer, new Object[0]);

            if (object != null)
            {
                Reflector.callVoid(object, Reflector.IRenderHandler_render, new Object[] {Float.valueOf(partialTicks), this.mc.world, this.mc});
                return;
            }
        }

        float f5 = this.mc.world.getRainStrength(partialTicks);

        if (f5 > 0.0F)
        {
            if (Config.get().isRainOff())
            {
                return;
            }

            this.enableLightmap();
            Entity entity = this.mc.getRenderViewEntity();
            WorldClient worldclient = this.mc.world;
            int i = MathHelper.floor_double(entity.posX);
            int j = MathHelper.floor_double(entity.posY);
            int k = MathHelper.floor_double(entity.posZ);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.get().disableCull();
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.get().enableBlend();
            GlStateManager.get().tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.get().alphaFunc(516, 0.0F);
            double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
            double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
            double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
            int l = MathHelper.floor_double(d1);
            byte b0 = 5;

            if (Config.get().isRainFancy())
            {
                b0 = 10;
            }

            byte b1 = -1;
            float f = (float)this.rendererUpdateCount + partialTicks;
            worldrenderer.setTranslation(-d0, -d1, -d2);

            if (Config.get().isRainFancy())
            {
                b0 = 10;
            }

            GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int i1 = k - b0; i1 <= k + b0; ++i1)
            {
                for (int j1 = i - b0; j1 <= i + b0; ++j1)
                {
                    int k1 = (i1 - k + 16) * 32 + j1 - i + 16;
                    double d3 = (double)this.rainXCoords[k1] * 0.5D;
                    double d4 = (double)this.rainYCoords[k1] * 0.5D;
                    blockpos$mutableblockpos.func_181079_c(j1, 0, i1);
                    BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(blockpos$mutableblockpos);

                    if (biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow())
                    {
                        int l1 = worldclient.getPrecipitationHeight(blockpos$mutableblockpos).getY();
                        int i2 = j - b0;
                        int j2 = j + b0;

                        if (i2 < l1)
                        {
                            i2 = l1;
                        }

                        if (j2 < l1)
                        {
                            j2 = l1;
                        }

                        int k2 = l1;

                        if (l1 < l)
                        {
                            k2 = l;
                        }

                        if (i2 != j2)
                        {
                            this.random.setSeed((long)(j1 * j1 * 3121 + j1 * 45238971 ^ i1 * i1 * 418711 + i1 * 13761));
                            blockpos$mutableblockpos.func_181079_c(j1, i2, i1);
                            float f1 = biomegenbase.getFloatTemperature(blockpos$mutableblockpos);

                            if (worldclient.getWorldChunkManager().getTemperatureAtHeight(f1, l1) >= 0.15F)
                            {
                                if (b1 != 0)
                                {
                                    if (b1 >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    b1 = 0;
                                    this.mc.getTextureManager().bindTexture(locationRainPng);
                                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double d5 = ((double)(this.rendererUpdateCount + j1 * j1 * 3121 + j1 * 45238971 + i1 * i1 * 418711 + i1 * 13761 & 31) + (double)partialTicks) / 32.0D * (3.0D + this.random.nextDouble());
                                double d6 = (double)((float)j1 + 0.5F) - entity.posX;
                                double d7 = (double)((float)i1 + 0.5F) - entity.posZ;
                                float f2 = MathHelper.sqrt_double(d6 * d6 + d7 * d7) / (float)b0;
                                float f3 = ((1.0F - f2 * f2) * 0.5F + 0.5F) * f5;
                                blockpos$mutableblockpos.func_181079_c(j1, k2, i1);
                                int l2 = worldclient.getCombinedLight(blockpos$mutableblockpos, 0);
                                int i3 = l2 >> 16 & 65535;
                                int j3 = l2 & 65535;
                                worldrenderer.pos((double)j1 - d3 + 0.5D, (double)i2, (double)i1 - d4 + 0.5D).tex(0.0D, (double)i2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f3).lightmap(i3, j3).endVertex();
                                worldrenderer.pos((double)j1 + d3 + 0.5D, (double)i2, (double)i1 + d4 + 0.5D).tex(1.0D, (double)i2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f3).lightmap(i3, j3).endVertex();
                                worldrenderer.pos((double)j1 + d3 + 0.5D, (double)j2, (double)i1 + d4 + 0.5D).tex(1.0D, (double)j2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f3).lightmap(i3, j3).endVertex();
                                worldrenderer.pos((double)j1 - d3 + 0.5D, (double)j2, (double)i1 - d4 + 0.5D).tex(0.0D, (double)j2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f3).lightmap(i3, j3).endVertex();
                            }
                            else
                            {
                                if (b1 != 1)
                                {
                                    if (b1 >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    b1 = 1;
                                    this.mc.getTextureManager().bindTexture(locationSnowPng);
                                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double d8 = (double)(((float)(this.rendererUpdateCount & 511) + partialTicks) / 512.0F);
                                double d9 = this.random.nextDouble() + (double)f * 0.01D * (double)((float)this.random.nextGaussian());
                                double d10 = this.random.nextDouble() + (double)(f * (float)this.random.nextGaussian()) * 0.001D;
                                double d11 = (double)((float)j1 + 0.5F) - entity.posX;
                                double d12 = (double)((float)i1 + 0.5F) - entity.posZ;
                                float f6 = MathHelper.sqrt_double(d11 * d11 + d12 * d12) / (float)b0;
                                float f4 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f5;
                                blockpos$mutableblockpos.func_181079_c(j1, k2, i1);
                                int k3 = (worldclient.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
                                int l3 = k3 >> 16 & 65535;
                                int i4 = k3 & 65535;
                                worldrenderer.pos((double)j1 - d3 + 0.5D, (double)i2, (double)i1 - d4 + 0.5D).tex(0.0D + d9, (double)i2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f4).lightmap(l3, i4).endVertex();
                                worldrenderer.pos((double)j1 + d3 + 0.5D, (double)i2, (double)i1 + d4 + 0.5D).tex(1.0D + d9, (double)i2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f4).lightmap(l3, i4).endVertex();
                                worldrenderer.pos((double)j1 + d3 + 0.5D, (double)j2, (double)i1 + d4 + 0.5D).tex(1.0D + d9, (double)j2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f4).lightmap(l3, i4).endVertex();
                                worldrenderer.pos((double)j1 - d3 + 0.5D, (double)j2, (double)i1 - d4 + 0.5D).tex(0.0D + d9, (double)j2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f4).lightmap(l3, i4).endVertex();
                            }
                        }
                    }
                }
            }

            if (b1 >= 0)
            {
                tessellator.draw();
            }

            worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.get().enableCull();
            GlStateManager.get().disableBlend();
            GlStateManager.get().alphaFunc(516, 0.0F);
            this.disableLightmap();
        }
    }

    /**
     * Setup orthogonal projection for rendering GUI screen overlays
     */
    public void setupOverlayRendering()
    {
        ScaledResolution scaledresolution = ScaledResolution.get();
        GlStateManager.get().clear(256);
        GlStateManager.get().matrixMode(5889);
        GlStateManager.get().loadIdentity();
        GlStateManager.get().ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.get().matrixMode(5888);
        GlStateManager.get().loadIdentity();
        GlStateManager.get().translate(0.0F, 0.0F, -2000.0F);
    }

    /**
     * calculates fog and calls glClearColor
     */
    private void updateFogColor(float partialTicks)
    {
        WorldClient worldclient = this.mc.world;
        Entity entity = this.mc.getRenderViewEntity();
        float f = 0.25F + 0.75F * (float)this.mc.options.renderDistanceChunks / 32.0F;
        f = 1.0F - (float)Math.pow((double)f, 0.25D);
        Vec3 vec3 = worldclient.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
        vec3 = CustomColors.getWorldSkyColor(vec3, worldclient, this.mc.getRenderViewEntity(), partialTicks, mc);
        float f1 = (float)vec3.xCoord;
        float f2 = (float)vec3.yCoord;
        float f3 = (float)vec3.zCoord;
        Vec3 vec31 = worldclient.getFogColor(partialTicks);
        vec31 = CustomColors.getWorldFogColor(vec31, worldclient, this.mc.getRenderViewEntity(), partialTicks, mc);
        this.fogColorRed = (float)vec31.xCoord;
        this.fogColorGreen = (float)vec31.yCoord;
        this.fogColorBlue = (float)vec31.zCoord;

//        if (this.mc.options.renderDistanceChunks >= 4)
//        {
//            double d0 = -1.0D;
//            Vec3 vec32 = MathHelper.sin(worldclient.getCelestialAngleRadians(partialTicks)) > 0.0F ? new Vec3(d0, 0.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
//            float f4 = (float)entity.getLook(partialTicks).dotProduct(vec32);
//
//            if (f4 < 0.0F)
//            {
//                f4 = 0.0F;
//            }
//
//            if (f4 > 0.0F)
//            {
//                float[] afloat = worldclient.provider.calcSunriseSunsetColors(worldclient.getCelestialAngle(partialTicks), partialTicks);
//
//                if (afloat != null)
//                {
//                    f4 = f4 * afloat[3];
//                    this.fogColorRed = this.fogColorRed * (1.0F - f4) + afloat[0] * f4;
//                    this.fogColorGreen = this.fogColorGreen * (1.0F - f4) + afloat[1] * f4;
//                    this.fogColorBlue = this.fogColorBlue * (1.0F - f4) + afloat[2] * f4;
//                }
//            }
//        }

        this.fogColorRed += (f1 - this.fogColorRed) * f;
        this.fogColorGreen += (f2 - this.fogColorGreen) * f;
        this.fogColorBlue += (f3 - this.fogColorBlue) * f;
        float f10 = worldclient.getRainStrength(partialTicks);

        if (f10 > 0.0F)
        {
            float f5 = 1.0F - f10 * 0.5F;
            float f12 = 1.0F - f10 * 0.4F;
            this.fogColorRed *= f5;
            this.fogColorGreen *= f5;
            this.fogColorBlue *= f12;
        }

        float f11 = worldclient.getThunderStrength(partialTicks);

        if (f11 > 0.0F)
        {
            float f13 = 1.0F - f11 * 0.5F;
            this.fogColorRed *= f13;
            this.fogColorGreen *= f13;
            this.fogColorBlue *= f13;
        }

        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.world, entity, partialTicks);

        if (this.cloudFog)
        {
            Vec3 vec33 = worldclient.getCloudColour(partialTicks);
            this.fogColorRed = (float)vec33.xCoord;
            this.fogColorGreen = (float)vec33.yCoord;
            this.fogColorBlue = (float)vec33.zCoord;
        }
        else if (block.getMaterial() == Material.water)
        {
            float f8 = (float)EnchantmentHelper.getRespiration(entity) * 0.2F;

            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.waterBreathing))
            {
                f8 = f8 * 0.3F + 0.6F;
            }

            this.fogColorRed = 0.02F + f8;
            this.fogColorGreen = 0.02F + f8;
            this.fogColorBlue = 0.2F + f8;
            Vec3 vec34 = CustomColors.getUnderwaterColor(this.mc.world, this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY + 1.0D, this.mc.getRenderViewEntity().posZ);

            if (vec34 != null)
            {
                this.fogColorRed = (float)vec34.xCoord;
                this.fogColorGreen = (float)vec34.yCoord;
                this.fogColorBlue = (float)vec34.zCoord;
            }
        }
        else if (block.getMaterial() == Material.lava)
        {
            this.fogColorRed = 0.6F;
            this.fogColorGreen = 0.1F;
            this.fogColorBlue = 0.0F;
        }

        float f9 = this.fogColor2 + (this.fogColor1 - this.fogColor2) * partialTicks;
        this.fogColorRed *= f9;
        this.fogColorGreen *= f9;
        this.fogColorBlue *= f9;
        double d2 = worldclient.provider.getVoidFogYFactor();
        double d1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks) * d2;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.blindness))
        {
            int i = ((EntityLivingBase)entity).getActivePotionEffect(Potion.blindness).getDuration();

            if (i < 20)
            {
                d1 *= (double)(1.0F - (float)i / 20.0F);
            }
            else
            {
                d1 = 0.0D;
            }
        }

        if (d1 < 1.0D)
        {
            if (d1 < 0.0D)
            {
                d1 = 0.0D;
            }

            d1 = d1 * d1;
            this.fogColorRed = (float)((double)this.fogColorRed * d1);
            this.fogColorGreen = (float)((double)this.fogColorGreen * d1);
            this.fogColorBlue = (float)((double)this.fogColorBlue * d1);
        }

//        if (this.bossColorModifier > 0.0F)
//        {
//            float f14 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTicks;
//            this.fogColorRed = this.fogColorRed * (1.0F - f14) + this.fogColorRed * 0.7F * f14;
//            this.fogColorGreen = this.fogColorGreen * (1.0F - f14) + this.fogColorGreen * 0.6F * f14;
//            this.fogColorBlue = this.fogColorBlue * (1.0F - f14) + this.fogColorBlue * 0.6F * f14;
//        }

        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.nightVision))
        {
            float f15 = this.getNightVisionBrightness((EntityLivingBase)entity, partialTicks);
            float f6 = 1.0F / this.fogColorRed;

            if (f6 > 1.0F / this.fogColorGreen)
            {
                f6 = 1.0F / this.fogColorGreen;
            }

            if (f6 > 1.0F / this.fogColorBlue)
            {
                f6 = 1.0F / this.fogColorBlue;
            }

            this.fogColorRed = this.fogColorRed * (1.0F - f15) + this.fogColorRed * f6 * f15;
            this.fogColorGreen = this.fogColorGreen * (1.0F - f15) + this.fogColorGreen * f6 * f15;
            this.fogColorBlue = this.fogColorBlue * (1.0F - f15) + this.fogColorBlue * f6 * f15;
        }

        if (this.mc.options.anaglyph)
        {
            float f16 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
            float f17 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
            float f7 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
            this.fogColorRed = f16;
            this.fogColorGreen = f17;
            this.fogColorBlue = f7;
        }

        if (Reflector.EntityViewRenderEvent_FogColors_Constructor.exists())
        {
            Object object = Reflector.newInstance(Reflector.EntityViewRenderEvent_FogColors_Constructor, new Object[] {this, entity, block, Float.valueOf(partialTicks), Float.valueOf(this.fogColorRed), Float.valueOf(this.fogColorGreen), Float.valueOf(this.fogColorBlue)});
            Reflector.postForgeBusEvent(object);
            this.fogColorRed = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_FogColors_red, this.fogColorRed);
            this.fogColorGreen = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_FogColors_green, this.fogColorGreen);
            this.fogColorBlue = Reflector.getFieldValueFloat(object, Reflector.EntityViewRenderEvent_FogColors_blue, this.fogColorBlue);
        }

        Shaders.setClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
    }

    /**
     * Sets up the fog to be rendered. If the arg passed in is -1 the fog starts at 0 and goes to 80% of far plane
     * distance and is used for sky rendering.
     */
    private void setupFog(int p_78468_1_, float partialTicks)
    {
        Entity entity = this.mc.getRenderViewEntity();
        boolean flag = false;
        this.fogStandard = false;

        if (entity instanceof EntityPlayer)
        {
            flag = ((EntityPlayer)entity).capabilities.isCreativeMode;
        }

        GL11.glFogfv(GL11.GL_FOG_COLOR, (FloatBuffer)this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.world, entity, partialTicks);
        float f1 = -1.0F;

        if (Reflector.ForgeHooksClient_getFogDensity.exists())
        {
            f1 = Reflector.callFloat(Reflector.ForgeHooksClient_getFogDensity, new Object[] {this, entity, block, Float.valueOf(partialTicks), Float.valueOf(0.1F)});
        }

        if (f1 >= 0.0F)
        {
            GlStateManager.get().setFogDensity(f1);
        }
        else if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.blindness))
        {
            float f2 = 5.0F;
            int i = ((EntityLivingBase)entity).getActivePotionEffect(Potion.blindness).getDuration();

            if (i < 20)
            {
                f2 = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - (float)i / 20.0F);
            }

            if (Config.get().isShaders())
            {
                Shaders.setFog(9729);
            }
            else
            {
                GlStateManager.get().setFog(9729);
            }

            if (p_78468_1_ == -1)
            {
                GlStateManager.get().setFogStart(0.0F);
                GlStateManager.get().setFogEnd(f2 * 0.8F);
            }
            else
            {
                GlStateManager.get().setFogStart(f2 * 0.25F);
                GlStateManager.get().setFogEnd(f2);
            }

            if (GLContext.getCapabilities().GL_NV_fog_distance && Config.get().isFogFancy())
            {
                GL11.glFogi(34138, 34139);
            }
        }
        else if (this.cloudFog)
        {
            if (Config.get().isShaders())
            {
                Shaders.setFog(2048);
            }
            else
            {
                GlStateManager.get().setFog(2048);
            }

            GlStateManager.get().setFogDensity(0.1F);
        }
        else if (block.getMaterial() == Material.water)
        {
            if (Config.get().isShaders())
            {
                Shaders.setFog(2048);
            }
            else
            {
                GlStateManager.get().setFog(2048);
            }

            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.waterBreathing))
            {
                GlStateManager.get().setFogDensity(0.01F);
            }
            else
            {
                GlStateManager.get().setFogDensity(0.1F - (float)EnchantmentHelper.getRespiration(entity) * 0.03F);
            }

            if (Config.get().isClearWater())
            {
                GlStateManager.get().setFogDensity(0.02F);
            }
        }
        else if (block.getMaterial() == Material.lava)
        {
            if (Config.get().isShaders())
            {
                Shaders.setFog(2048);
            }
            else
            {
                GlStateManager.get().setFog(2048);
            }

            GlStateManager.get().setFogDensity(2.0F);
        }
        else
        {
            float f = this.farPlaneDistance;
            this.fogStandard = true;

            if (Config.get().isShaders())
            {
                Shaders.setFog(9729);
            }
            else
            {
                GlStateManager.get().setFog(9729);
            }

            if (p_78468_1_ == -1)
            {
                GlStateManager.get().setFogStart(0.0F);
                GlStateManager.get().setFogEnd(f);
            }
            else
            {
                GlStateManager.get().setFogStart(f * Config.get().getFogStart());
                GlStateManager.get().setFogEnd(f);
            }

            if (GLContext.getCapabilities().GL_NV_fog_distance)
            {
                if (Config.get().isFogFancy())
                {
                    GL11.glFogi(34138, 34139);
                }

                if (Config.get().isFogFast())
                {
                    GL11.glFogi(34138, 34140);
                }
            }

            if (this.mc.world.provider.doesXZShowFog((int)entity.posX, (int)entity.posZ))
            {
                GlStateManager.get().setFogStart(f * 0.05F);
                GlStateManager.get().setFogEnd(f);
            }

            if (Reflector.ForgeHooksClient_onFogRender.exists())
            {
                Reflector.callVoid(Reflector.ForgeHooksClient_onFogRender, new Object[] {this, entity, block, Float.valueOf(partialTicks), Integer.valueOf(p_78468_1_), Float.valueOf(f)});
            }
        }

        GlStateManager.get().enableColorMaterial();
        GlStateManager.get().enableFog();
        GlStateManager.get().colorMaterial(1028, 4608);
    }

    /**
     * Update and return fogColorBuffer with the RGBA values passed as arguments
     */
    private FloatBuffer setFogColorBuffer(float red, float green, float blue, float alpha)
    {
        if (Config.get().isShaders())
        {
            Shaders.setFogColor(red, green, blue);
        }

        this.fogColorBuffer.clear();
        this.fogColorBuffer.put(red).put(green).put(blue).put(alpha);
        this.fogColorBuffer.flip();
        return this.fogColorBuffer;
    }

    public MapItemRenderer getMapItemRenderer()
    {
        return this.theMapItemRenderer;
    }

    private void waitForServerThread()
    {
        this.serverWaitTimeCurrent = 0;

        if (Config.get().isSmoothWorld() && Config.get().isSingleProcessor())
        {
            if (this.mc.isIntegratedServerRunning())
            {
                IntegratedServer integratedserver = this.mc.getIntegratedServer();

                if (integratedserver != null)
                {
                    boolean flag = this.mc.isGamePaused();

                    if (!flag && !(this.mc.currentScreen instanceof GuiDownloadTerrain))
                    {
                        if (this.serverWaitTime > 0)
                        {
                            Lagometer.timerServer.start();
                            Config.get().sleep((long)this.serverWaitTime);
                            Lagometer.timerServer.end();
                            this.serverWaitTimeCurrent = this.serverWaitTime;
                        }

                        long i = System.nanoTime() / 1000000L;

                        if (this.lastServerTime != 0L && this.lastServerTicks != 0)
                        {
                            long j = i - this.lastServerTime;

                            if (j < 0L)
                            {
                                this.lastServerTime = i;
                                j = 0L;
                            }

                            if (j >= 50L)
                            {
                                this.lastServerTime = i;
                                int k = integratedserver.getTickCounter();
                                int l = k - this.lastServerTicks;

                                if (l < 0)
                                {
                                    this.lastServerTicks = k;
                                    l = 0;
                                }

                                if (l < 1 && this.serverWaitTime < 100)
                                {
                                    this.serverWaitTime += 2;
                                }

                                if (l > 1 && this.serverWaitTime > 0)
                                {
                                    --this.serverWaitTime;
                                }

                                this.lastServerTicks = k;
                            }
                        }
                        else
                        {
                            this.lastServerTime = i;
                            this.lastServerTicks = integratedserver.getTickCounter();
                            this.avgServerTickDiff = 1.0F;
                            this.avgServerTimeDiff = 50.0F;
                        }
                    }
                    else
                    {
                        if (this.mc.currentScreen instanceof GuiDownloadTerrain)
                        {
                            Config.get().sleep(20L);
                        }

                        this.lastServerTime = 0L;
                        this.lastServerTicks = 0;
                    }
                }
            }
        }
        else
        {
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
        }
    }

    private void frameInit()
    {
        World world = this.mc.world;

        if (this.mc.currentScreen instanceof GuiMainMenu)
        {
            this.updateMainMenu((GuiMainMenu)this.mc.currentScreen);
        }

        if (this.updatedWorld != world)
        {
            RandomMobs.worldChanged(this.updatedWorld, world);
            Config.get().updateThreadPriorities();
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
            this.updatedWorld = world;
        }

        if (!this.setFxaaShader(Shaders.configAntialiasingLevel))
        {
            Shaders.configAntialiasingLevel = 0;
        }
    }

    private void frameFinish()
    {
//        if (this.mc.world != null)
//        {
//            long i = System.currentTimeMillis();
//
//            if (i > this.lastErrorCheckTimeMs + 10000L)
//            {
//                this.lastErrorCheckTimeMs = i;
//                int j = GL11.glGetError();
//
//                if (j != 0)
//                {
//                    String s = GLU.gluErrorString(j);
//                    ChatComponentText chatcomponenttext = new ChatComponentText(I18n.format("of.message.openglError", new Object[] {Integer.valueOf(j), s}));
//                    this.mc.ingameGUI.getChatGUI().printChatMessage(chatcomponenttext);
//                }
//            }
//        }
    }

    private void updateMainMenu(GuiMainMenu p_updateMainMenu_1_)
    {
        try
        {
            String s = null;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int i = calendar.get(5);
            int j = calendar.get(2) + 1;

            if (i == 8 && j == 4)
            {
                s = "Happy birthday, OptiFine!";
            }

            if (i == 14 && j == 8)
            {
                s = "Happy birthday, sp614x!";
            }

            if (s == null)
            {
                return;
            }

            Field[] afield = GuiMainMenu.class.getDeclaredFields();

            for (int k = 0; k < afield.length; ++k)
            {
                if (afield[k].getType() == String.class)
                {
                    afield[k].setAccessible(true);
                    afield[k].set(p_updateMainMenu_1_, s);
                    break;
                }
            }
        }
        catch (Throwable var8)
        {
            ;
        }
    }

    public boolean setFxaaShader(int p_setFxaaShader_1_)
    {
        if (!OpenGlHelper.isFramebufferEnabled())
        {
            return false;
        }
        else if (this.theShaderGroup != null && this.theShaderGroup != this.fxaaShaders[2] && this.theShaderGroup != this.fxaaShaders[4])
        {
            return true;
        }
        else if (p_setFxaaShader_1_ != 2 && p_setFxaaShader_1_ != 4)
        {
            if (this.theShaderGroup == null)
            {
                return true;
            }
            else
            {
                this.theShaderGroup.deleteShaderGroup();
                this.theShaderGroup = null;
                return true;
            }
        }
        else if (this.theShaderGroup != null && this.theShaderGroup == this.fxaaShaders[p_setFxaaShader_1_])
        {
            return true;
        }
        else if (this.mc.world == null)
        {
            return true;
        }
        else
        {
            this.loadShader(new ResourceLocation("shaders/post/fxaa_of_" + p_setFxaaShader_1_ + "x.json"));
            this.fxaaShaders[p_setFxaaShader_1_] = this.theShaderGroup;
            return this.useShader;
        }
    }
}
