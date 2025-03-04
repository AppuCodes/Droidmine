package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import net.minecraft.optifine.Config;

public class GlStateManager
{
    private GlStateManager.AlphaState alphaState = new GlStateManager.AlphaState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.BooleanState lightingState = new GlStateManager.BooleanState(2896);
    private GlStateManager.BooleanState[] lightState = new GlStateManager.BooleanState[8];
    private GlStateManager.ColorMaterialState colorMaterialState = new GlStateManager.ColorMaterialState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.BlendState blendState = new GlStateManager.BlendState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.DepthState depthState = new GlStateManager.DepthState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.FogState fogState = new GlStateManager.FogState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.CullState cullState = new GlStateManager.CullState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.PolygonOffsetState polygonOffsetState = new GlStateManager.PolygonOffsetState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.ColorLogicState colorLogicState = new GlStateManager.ColorLogicState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.TexGenState texGenState = new GlStateManager.TexGenState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.ClearState clearState = new GlStateManager.ClearState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.StencilState stencilState = new GlStateManager.StencilState((GlStateManager.GlStateManager$1)null);
    private GlStateManager.BooleanState normalizeState = new GlStateManager.BooleanState(2977);
    private int activeTextureUnit = 0;
    private GlStateManager.TextureState[] textureState = new GlStateManager.TextureState[32];
    private int activeShadeModel = 7425;
    private GlStateManager.BooleanState rescaleNormalState = new GlStateManager.BooleanState(32826);
    private GlStateManager.ColorMask colorMaskState = new GlStateManager.ColorMask((GlStateManager.GlStateManager$1)null);
    public GlStateManager.Color colorState = new GlStateManager.Color();
    private static HashMap<String, GlStateManager> states = new HashMap<>();
    public boolean clearEnabled = true;

    public void pushAttrib()
    {
        GL11.glPushAttrib(8256);
    }

    public void popAttrib()
    {
        GL11.glPopAttrib();
    }

    public void disableAlpha()
    {
        alphaState.field_179208_a.setDisabled();
    }

    public void enableAlpha()
    {
        alphaState.field_179208_a.setEnabled();
    }

    public void alphaFunc(int func, float ref)
    {
        if (func != alphaState.func || ref != alphaState.ref)
        {
            alphaState.func = func;
            alphaState.ref = ref;
            GL11.glAlphaFunc(func, ref);
        }
    }

    public void enableLighting()
    {
        lightingState.setEnabled();
    }

    public void disableLighting()
    {
        lightingState.setDisabled();
    }

    public void enableLight(int light)
    {
        lightState[light].setEnabled();
    }

    public void disableLight(int light)
    {
        lightState[light].setDisabled();
    }

    public void enableColorMaterial()
    {
        colorMaterialState.field_179191_a.setEnabled();
    }

    public void disableColorMaterial()
    {
        colorMaterialState.field_179191_a.setDisabled();
    }

    public void colorMaterial(int face, int mode)
    {
        if (face != colorMaterialState.field_179189_b || mode != colorMaterialState.field_179190_c)
        {
            colorMaterialState.field_179189_b = face;
            colorMaterialState.field_179190_c = mode;
            GL11.glColorMaterial(face, mode);
        }
    }

    public void disableDepth()
    {
        depthState.depthTest.setDisabled();
    }

    public void enableDepth()
    {
        depthState.depthTest.setEnabled();
    }

    public void depthFunc(int depthFunc)
    {
        if (depthFunc != depthState.depthFunc)
        {
            depthState.depthFunc = depthFunc;
            GL11.glDepthFunc(depthFunc);
        }
    }

    public void depthMask(boolean flagIn)
    {
        if (flagIn != depthState.maskEnabled)
        {
            depthState.maskEnabled = flagIn;
            GL11.glDepthMask(flagIn);
        }
    }

    public void disableBlend()
    {
        blendState.field_179213_a.setDisabled();
    }

    public void enableBlend()
    {
        blendState.field_179213_a.setEnabled();
    }

    public void blendFunc(int srcFactor, int dstFactor)
    {
        if (srcFactor != blendState.srcFactor || dstFactor != blendState.dstFactor)
        {
            blendState.srcFactor = srcFactor;
            blendState.dstFactor = dstFactor;
            GL11.glBlendFunc(srcFactor, dstFactor);
        }
    }

    public void tryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha)
    {
        if (srcFactor != blendState.srcFactor || dstFactor != blendState.dstFactor || srcFactorAlpha != blendState.srcFactorAlpha || dstFactorAlpha != blendState.dstFactorAlpha)
        {
            blendState.srcFactor = srcFactor;
            blendState.dstFactor = dstFactor;
            blendState.srcFactorAlpha = srcFactorAlpha;
            blendState.dstFactorAlpha = dstFactorAlpha;
            OpenGlHelper.glBlendFunc(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
        }
    }

    public void enableFog()
    {
        fogState.field_179049_a.setEnabled();
    }

    public void disableFog()
    {
        fogState.field_179049_a.setDisabled();
    }

    public void setFog(int param)
    {
        if (param != fogState.field_179047_b)
        {
            fogState.field_179047_b = param;
            GL11.glFogi(GL11.GL_FOG_MODE, param);
        }
    }

    public void setFogDensity(float param)
    {
        if (param != fogState.field_179048_c)
        {
            fogState.field_179048_c = param;
            GL11.glFogf(GL11.GL_FOG_DENSITY, param);
        }
    }

    public void setFogStart(float param)
    {
        if (param != fogState.fogStart)
        {
            fogState.fogStart = param;
            GL11.glFogf(GL11.GL_FOG_START, param);
        }
    }

    public void setFogEnd(float param)
    {
        if (param != fogState.fogEnd)
        {
            fogState.fogEnd = param;
            GL11.glFogf(GL11.GL_FOG_END, param);
        }
    }

    public void enableCull()
    {
        cullState.field_179054_a.setEnabled();
    }

    public void disableCull()
    {
        cullState.field_179054_a.setDisabled();
    }

    public void cullFace(int mode)
    {
        if (mode != cullState.field_179053_b)
        {
            cullState.field_179053_b = mode;
            GL11.glCullFace(mode);
        }
    }

    public void enablePolygonOffset()
    {
        polygonOffsetState.field_179044_a.setEnabled();
    }

    public void disablePolygonOffset()
    {
        polygonOffsetState.field_179044_a.setDisabled();
    }

    public void doPolygonOffset(float factor, float units)
    {
        if (factor != polygonOffsetState.field_179043_c || units != polygonOffsetState.field_179041_d)
        {
            polygonOffsetState.field_179043_c = factor;
            polygonOffsetState.field_179041_d = units;
            GL11.glPolygonOffset(factor, units);
        }
    }

    public void enableColorLogic()
    {
        colorLogicState.field_179197_a.setEnabled();
    }

    public void disableColorLogic()
    {
        colorLogicState.field_179197_a.setDisabled();
    }

    public void colorLogicOp(int opcode)
    {
        if (opcode != colorLogicState.field_179196_b)
        {
            colorLogicState.field_179196_b = opcode;
            GL11.glLogicOp(opcode);
        }
    }

    public void enableTexGenCoord(GlStateManager.TexGen p_179087_0_)
    {
        texGenCoord(p_179087_0_).field_179067_a.setEnabled();
    }

    public void disableTexGenCoord(GlStateManager.TexGen p_179100_0_)
    {
        texGenCoord(p_179100_0_).field_179067_a.setDisabled();
    }

    public void texGen(GlStateManager.TexGen p_179149_0_, int p_179149_1_)
    {
        GlStateManager.TexGenCoord glstatemanager$texgencoord = texGenCoord(p_179149_0_);

        if (p_179149_1_ != glstatemanager$texgencoord.field_179066_c)
        {
            glstatemanager$texgencoord.field_179066_c = p_179149_1_;
            GL11.glTexGeni(glstatemanager$texgencoord.field_179065_b, GL11.GL_TEXTURE_GEN_MODE, p_179149_1_);
        }
    }

    public void func_179105_a(GlStateManager.TexGen p_179105_0_, int pname, FloatBuffer params)
    {
        GL11.glTexGenfv(texGenCoord(p_179105_0_).field_179065_b, pname, params);
    }

    private GlStateManager.TexGenCoord texGenCoord(GlStateManager.TexGen p_179125_0_)
    {
        switch (GlStateManager.GlStateManager$1.field_179175_a[p_179125_0_.ordinal()])
        {
            case 1:
                return texGenState.field_179064_a;

            case 2:
                return texGenState.field_179062_b;

            case 3:
                return texGenState.field_179063_c;

            case 4:
                return texGenState.field_179061_d;

            default:
                return texGenState.field_179064_a;
        }
    }

    public void setActiveTexture(int texture)
    {
        if (activeTextureUnit != texture - OpenGlHelper.defaultTexUnit)
        {
            activeTextureUnit = texture - OpenGlHelper.defaultTexUnit;
            OpenGlHelper.setActiveTexture(texture);
        }
    }

    public void enableTexture2D()
    {
        textureState[activeTextureUnit].texture2DState.setEnabled();
    }

    public void disableTexture2D()
    {
        textureState[activeTextureUnit].texture2DState.setDisabled();
    }

    public int generateTexture()
    {
        return GL11.glGenTextures();
    }

    public void deleteTexture(int texture)
    {
        if (texture != 0)
        {
            GL11.glDeleteTextures(texture);

            for (GlStateManager.TextureState glstatemanager$texturestate : textureState)
            {
                if (glstatemanager$texturestate.textureName == texture)
                {
                    glstatemanager$texturestate.textureName = 0;
                }
            }
        }
    }

    public void bindTexture(int texture)
    {   
        if (texture != textureState[activeTextureUnit].textureName)
        {
            textureState[activeTextureUnit].textureName = texture;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        }
    }

    public void bindCurrentTexture()
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureState[activeTextureUnit].textureName);
    }

    public void enableNormalize()
    {
        normalizeState.setEnabled();
    }

    public void disableNormalize()
    {
        normalizeState.setDisabled();
    }

    public void shadeModel(int mode)
    {
        if (mode != activeShadeModel)
        {
            activeShadeModel = mode;
            GL11.glShadeModel(mode);
        }
    }

    public void enableRescaleNormal()
    {
        rescaleNormalState.setEnabled();
    }

    public void disableRescaleNormal()
    {
        rescaleNormalState.setDisabled();
    }

    public void viewport(int x, int y, int width, int height)
    {
        GL11.glViewport(x, y, width, height);
    }

    public void colorMask(boolean red, boolean green, boolean blue, boolean alpha)
    {
        if (red != colorMaskState.red || green != colorMaskState.green || blue != colorMaskState.blue || alpha != colorMaskState.alpha)
        {
            colorMaskState.red = red;
            colorMaskState.green = green;
            colorMaskState.blue = blue;
            colorMaskState.alpha = alpha;
            GL11.glColorMask(red, green, blue, alpha);
        }
    }

    public void clearDepth(double depth)
    {
        if (depth != clearState.field_179205_a)
        {
            clearState.field_179205_a = depth;
            GL11.glClearDepth(depth);
        }
    }

    public void clearColor(float red, float green, float blue, float alpha)
    {
        if (red != clearState.field_179203_b.red || green != clearState.field_179203_b.green || blue != clearState.field_179203_b.blue || alpha != clearState.field_179203_b.alpha)
        {
            clearState.field_179203_b.red = red;
            clearState.field_179203_b.green = green;
            clearState.field_179203_b.blue = blue;
            clearState.field_179203_b.alpha = alpha;
            GL11.glClearColor(red, green, blue, alpha);
        }
    }

    public void clear(int mask)
    {
        if (clearEnabled)
        {
            GL11.glClear(mask);
        }
    }

    public static void matrixMode(int mode)
    {
        GL11.glMatrixMode(mode);
    }

    public static void loadIdentity()
    {
        GL11.glLoadIdentity();
    }

    public static void pushMatrix()
    {
        GL11.glPushMatrix();
    }

    public static void popMatrix()
    {
        GL11.glPopMatrix();
    }

    public static void getFloat(int pname, FloatBuffer params)
    {
        GL11.glGetFloatv(pname, params);
    }

    public static void ortho(double left, double right, double bottom, double top, double zNear, double zFar)
    {
        GL11.glOrtho(left, right, bottom, top, zNear, zFar);
    }

    public static void rotate(float angle, float x, float y, float z)
    {
        GL11.glRotatef(angle, x, y, z);
    }

    public static void scale(float x, float y, float z)
    {
        GL11.glScalef(x, y, z);
    }

    public static void scale(double x, double y, double z)
    {
        GL11.glScaled(x, y, z);
    }

    public static void translate(float x, float y, float z)
    {
        GL11.glTranslatef(x, y, z);
    }

    public static void translate(double x, double y, double z)
    {
        GL11.glTranslated(x, y, z);
    }

    public static void multMatrix(FloatBuffer matrix)
    {
        GL11.glMultMatrixf(matrix);
    }

    public void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha)
    {
        if (colorRed != colorState.red || colorGreen != colorState.green || colorBlue != colorState.blue || colorAlpha != colorState.alpha)
        {
            colorState.red = colorRed;
            colorState.green = colorGreen;
            colorState.blue = colorBlue;
            colorState.alpha = colorAlpha;
            GL11.glColor4f(colorRed, colorGreen, colorBlue, colorAlpha);
        }
    }

    public void color(float colorRed, float colorGreen, float colorBlue)
    {
        color(colorRed, colorGreen, colorBlue, 1.0F);
    }

    public void resetColor()
    {
        colorState.red = colorState.green = colorState.blue = colorState.alpha = -1.0F;
    }

    public static void callList(int list)
    {
        GL11.glCallList(list);
    }

    public int getActiveTextureUnit()
    {
        return OpenGlHelper.defaultTexUnit + activeTextureUnit;
    }

    public int getBoundTexture()
    {
        return textureState[activeTextureUnit].textureName;
    }

    public void checkBoundTexture()
    {
        if (Config.get().isMinecraftThread())
        {
            int i = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            int j = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            int k = getActiveTextureUnit();
            int l = getBoundTexture();
        }
    }

    public void deleteTextures(IntBuffer p_deleteTextures_0_)
    {
        p_deleteTextures_0_.rewind();

        while (p_deleteTextures_0_.position() < p_deleteTextures_0_.limit())
        {
            int i = p_deleteTextures_0_.get();
            deleteTexture(i);
        }

        p_deleteTextures_0_.rewind();
    }

    public GlStateManager()
    {
        for (int i = 0; i < 8; ++i)
        {
            lightState[i] = new GlStateManager.BooleanState(16384 + i);
        }

        for (int j = 0; j < textureState.length; ++j)
        {
            textureState[j] = new GlStateManager.TextureState((GlStateManager.GlStateManager$1)null);
        }
    }

    final static class GlStateManager$1
    {
        private static final int[] field_179175_a = new int[GlStateManager.TexGen.values().length];
        private final String __OBFID = "CL_00002557";

        static
        {
            try
            {
                field_179175_a[GlStateManager.TexGen.S.ordinal()] = 1;
            }
            catch (NoSuchFieldError var4)
            {
                ;
            }

            try
            {
                field_179175_a[GlStateManager.TexGen.T.ordinal()] = 2;
            }
            catch (NoSuchFieldError var3)
            {
                ;
            }

            try
            {
                field_179175_a[GlStateManager.TexGen.R.ordinal()] = 3;
            }
            catch (NoSuchFieldError var2)
            {
                ;
            }

            try
            {
                field_179175_a[GlStateManager.TexGen.Q.ordinal()] = 4;
            }
            catch (NoSuchFieldError var1)
            {
                ;
            }
        }
    }

    class AlphaState
    {
        public GlStateManager.BooleanState field_179208_a;
        public int func;
        public float ref;
        private final String __OBFID = "CL_00002556";

        private AlphaState()
        {
            this.field_179208_a = new GlStateManager.BooleanState(3008);
            this.func = 519;
            this.ref = -1.0F;
        }

        AlphaState(GlStateManager.GlStateManager$1 p_i46489_1_)
        {
            this();
        }
    }

    class BlendState
    {
        public GlStateManager.BooleanState field_179213_a;
        public int srcFactor;
        public int dstFactor;
        public int srcFactorAlpha;
        public int dstFactorAlpha;
        private final String __OBFID = "CL_00002555";

        private BlendState()
        {
            this.field_179213_a = new GlStateManager.BooleanState(3042);
            this.srcFactor = 1;
            this.dstFactor = 0;
            this.srcFactorAlpha = 1;
            this.dstFactorAlpha = 0;
        }

        BlendState(GlStateManager.GlStateManager$1 p_i46488_1_)
        {
            this();
        }
    }

    class BooleanState
    {
        private final int capability;
        private boolean currentState = false;
        private final String __OBFID = "CL_00002554";

        public BooleanState(int capabilityIn)
        {
            this.capability = capabilityIn;
        }

        public void setDisabled()
        {
            this.setState(false);
        }

        public void setEnabled()
        {
            this.setState(true);
        }

        public void setState(boolean state)
        {
            if (state != this.currentState)
            {
                this.currentState = state;

                if (state)
                {
                    GL11.glEnable(this.capability);
                }
                else
                {
                    GL11.glDisable(this.capability);
                }
            }
        }
    }

    class ClearState
    {
        public double field_179205_a;
        public GlStateManager.Color field_179203_b;
        public int field_179204_c;
        private final String __OBFID = "CL_00002553";

        private ClearState()
        {
            this.field_179205_a = 1.0D;
            this.field_179203_b = new GlStateManager.Color(0.0F, 0.0F, 0.0F, 0.0F);
            this.field_179204_c = 0;
        }

        ClearState(GlStateManager.GlStateManager$1 p_i46487_1_)
        {
            this();
        }
    }

    public class Color
    {
        public float red = 1.0F;
        public float green = 1.0F;
        public float blue = 1.0F;
        public float alpha = 1.0F;
        private final String __OBFID = "CL_00002552";

        public Color()
        {
        }

        public Color(float redIn, float greenIn, float blueIn, float alphaIn)
        {
            this.red = redIn;
            this.green = greenIn;
            this.blue = blueIn;
            this.alpha = alphaIn;
        }
    }

    class ColorLogicState
    {
        public GlStateManager.BooleanState field_179197_a;
        public int field_179196_b;
        private final String __OBFID = "CL_00002551";

        private ColorLogicState()
        {
            this.field_179197_a = new GlStateManager.BooleanState(3058);
            this.field_179196_b = 5379;
        }

        ColorLogicState(GlStateManager.GlStateManager$1 p_i46486_1_)
        {
            this();
        }
    }

    class ColorMask
    {
        public boolean red;
        public boolean green;
        public boolean blue;
        public boolean alpha;
        private final String __OBFID = "CL_00002550";

        private ColorMask()
        {
            this.red = true;
            this.green = true;
            this.blue = true;
            this.alpha = true;
        }

        ColorMask(GlStateManager.GlStateManager$1 p_i46485_1_)
        {
            this();
        }
    }

    class ColorMaterialState
    {
        public GlStateManager.BooleanState field_179191_a;
        public int field_179189_b;
        public int field_179190_c;
        private final String __OBFID = "CL_00002549";

        private ColorMaterialState()
        {
            this.field_179191_a = new GlStateManager.BooleanState(2903);
            this.field_179189_b = 1032;
            this.field_179190_c = 5634;
        }

        ColorMaterialState(GlStateManager.GlStateManager$1 p_i46484_1_)
        {
            this();
        }
    }

    class CullState
    {
        public GlStateManager.BooleanState field_179054_a;
        public int field_179053_b;
        private final String __OBFID = "CL_00002548";

        private CullState()
        {
            this.field_179054_a = new GlStateManager.BooleanState(2884);
            this.field_179053_b = 1029;
        }

        CullState(GlStateManager.GlStateManager$1 p_i46483_1_)
        {
            this();
        }
    }

    class DepthState
    {
        public GlStateManager.BooleanState depthTest;
        public boolean maskEnabled;
        public int depthFunc;
        private final String __OBFID = "CL_00002547";

        private DepthState()
        {
            this.depthTest = new GlStateManager.BooleanState(2929);
            this.maskEnabled = true;
            this.depthFunc = 513;
        }

        DepthState(GlStateManager.GlStateManager$1 p_i46482_1_)
        {
            this();
        }
    }

    class FogState
    {
        public GlStateManager.BooleanState field_179049_a;
        public int field_179047_b;
        public float field_179048_c;
        public float fogStart;
        public float fogEnd;
        private final String __OBFID = "CL_00002546";

        private FogState()
        {
            this.field_179049_a = new GlStateManager.BooleanState(2912);
            this.field_179047_b = 2048;
            this.field_179048_c = 1.0F;
            this.fogStart = 0.0F;
            this.fogEnd = 1.0F;
        }

        FogState(GlStateManager.GlStateManager$1 p_i46481_1_)
        {
            this();
        }
    }

    class PolygonOffsetState
    {
        public GlStateManager.BooleanState field_179044_a;
        public GlStateManager.BooleanState field_179042_b;
        public float field_179043_c;
        public float field_179041_d;
        private final String __OBFID = "CL_00002545";

        private PolygonOffsetState()
        {
            this.field_179044_a = new GlStateManager.BooleanState(32823);
            this.field_179042_b = new GlStateManager.BooleanState(10754);
            this.field_179043_c = 0.0F;
            this.field_179041_d = 0.0F;
        }

        PolygonOffsetState(GlStateManager.GlStateManager$1 p_i46480_1_)
        {
            this();
        }
    }

    class StencilFunc
    {
        public int field_179081_a;
        public int field_179079_b;
        public int field_179080_c;
        private final String __OBFID = "CL_00002544";

        private StencilFunc()
        {
            this.field_179081_a = 519;
            this.field_179079_b = 0;
            this.field_179080_c = -1;
        }

        StencilFunc(GlStateManager.GlStateManager$1 p_i46479_1_)
        {
            this();
        }
    }

    class StencilState
    {
        public GlStateManager.StencilFunc field_179078_a;
        public int field_179076_b;
        public int field_179077_c;
        public int field_179074_d;
        public int field_179075_e;
        private final String __OBFID = "CL_00002543";

        private StencilState()
        {
            this.field_179078_a = new GlStateManager.StencilFunc((GlStateManager.GlStateManager$1)null);
            this.field_179076_b = -1;
            this.field_179077_c = 7680;
            this.field_179074_d = 7680;
            this.field_179075_e = 7680;
        }

        StencilState(GlStateManager.GlStateManager$1 p_i46478_1_)
        {
            this();
        }
    }

    public enum TexGen
    {
        S("S", 0),
        T("T", 1),
        R("R", 2),
        Q("Q", 3);

        private static final GlStateManager.TexGen[] $VALUES = new GlStateManager.TexGen[]{S, T, R, Q};

        private TexGen(String p_i3_3_, int p_i3_4_)
        {
        }
    }

    class TexGenCoord
    {
        public GlStateManager.BooleanState field_179067_a;
        public int field_179065_b;
        public int field_179066_c = -1;
        private final String __OBFID = "CL_00002541";

        public TexGenCoord(int p_i46254_1_, int p_i46254_2_)
        {
            this.field_179065_b = p_i46254_1_;
            this.field_179067_a = new GlStateManager.BooleanState(p_i46254_2_);
        }
    }

    class TexGenState
    {
        public GlStateManager.TexGenCoord field_179064_a;
        public GlStateManager.TexGenCoord field_179062_b;
        public GlStateManager.TexGenCoord field_179063_c;
        public GlStateManager.TexGenCoord field_179061_d;
        private final String __OBFID = "CL_00002540";

        private TexGenState()
        {
            this.field_179064_a = new GlStateManager.TexGenCoord(8192, 3168);
            this.field_179062_b = new GlStateManager.TexGenCoord(8193, 3169);
            this.field_179063_c = new GlStateManager.TexGenCoord(8194, 3170);
            this.field_179061_d = new GlStateManager.TexGenCoord(8195, 3171);
        }

        TexGenState(GlStateManager.GlStateManager$1 p_i46477_1_)
        {
            this();
        }
    }

    class TextureState
    {
        public GlStateManager.BooleanState texture2DState;
        public int textureName;

        private TextureState()
        {
            this.texture2DState = new GlStateManager.BooleanState(3553);
            this.textureName = 0;
        }

        TextureState(GlStateManager.GlStateManager$1 p_i46476_1_)
        {
            this();
        }
    }
    
    public static GlStateManager get()
    {
        String name = Thread.currentThread().getName();
        
        if (states.containsKey(name))
        {
            return states.get(name);
        }
        
        GlStateManager state = new GlStateManager();
        states.put(name, state);
        return state;
    }
}
