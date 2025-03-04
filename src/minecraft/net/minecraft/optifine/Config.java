package net.minecraft.optifine;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.display.*;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.ResourcePackRepository.Entry;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.optifine.shadersmod.client.Shaders;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.*;

public class Config
{
    public String openGlVersion = null;
    public String openGlRenderer = null;
    public String openGlVendor = null;
    public String[] openGlExtensions = null;
    public GlVersion glVersion = null;
    public GlVersion glslVersion = null;
    public int minecraftVersionInt = -1;
    public boolean fancyFogAvailable = false;
    public boolean occlusionAvailable = false;
    private GameOptions options = null;
    private ClientEngine minecraft;
    private boolean initialized = false;
    private Thread minecraftThread = null;
    private DisplayMode desktopDisplayMode = null;
    private DisplayMode[] displayModes = null;
    private int antialiasingLevel = 0;
    private int availableProcessors = 0;
    public boolean zoomMode = false;
    private int texturePackClouds = 0;
    public boolean waterOpacityChanged = false;
    private boolean fullscreenModeChecked = false;
    private boolean desktopModeChecked = false;
    private DefaultResourcePack defaultResourcePackLazy = null;
    public final Float DEF_ALPHA_FUNC_LEVEL = Float.valueOf(0.1F);
    private static final Logger LOGGER = LogManager.getLogger();
    public static HashMap<String, Config> configs = new HashMap<>();

    public String getVersion()
    {
        return "OptiFine_1.8.9_HD_U_H8";
    }

    public String getVersionDebug()
    {
        StringBuffer stringbuffer = new StringBuffer(32);

        if (isDynamicLights())
        {
            stringbuffer.append("DL: ");
            stringbuffer.append(String.valueOf(DynamicLights.getCount()));
            stringbuffer.append(", ");
        }

        stringbuffer.append("OptiFine_1.8.9_HD_U_H8");
        String s = Shaders.getShaderPackName();

        if (s != null)
        {
            stringbuffer.append(", ");
            stringbuffer.append(s);
        }

        return stringbuffer.toString();
    }

    public void initoptions(GameOptions p_initoptions_0_)
    {
        if (options == null)
        {
            options = p_initoptions_0_;
            desktopDisplayMode = Display.get().getDesktopDisplayMode();
            updateAvailableProcessors();
            ReflectorForge.putLaunchBlackboard("optifine.ForgeSplashCompatible", Boolean.TRUE);
        }
    }

    public void initDisplay()
    {
        minecraftThread = Thread.currentThread();
        updateThreadPriorities();
    }

    public void checkInitialized()
    {
        if (!initialized)
        {
            if (Display.get().isCreated())
            {
                initialized = true;
                checkOpenGlCaps();
                startVersionCheckThread();
            }
        }
    }

    private void checkOpenGlCaps()
    {
        openGlVersion = GL11.glGetString(GL11.GL_VERSION);
        openGlRenderer = GL11.glGetString(GL11.GL_RENDERER);
        openGlVendor = GL11.glGetString(GL11.GL_VENDOR);
        fancyFogAvailable = GLContext.getCapabilities().GL_NV_fog_distance;
        occlusionAvailable = GLContext.getCapabilities().GL_ARB_occlusion_query;
    }

    private String getBuild()
    {
        try
        {
            InputStream inputstream = Config.class.getResourceAsStream("/buildof.txt");

            if (inputstream == null)
            {
                return null;
            }
            else
            {
                String s = readLines(inputstream)[0];
                return s;
            }
        }
        catch (Exception exception)
        {
            warn("" + exception.getClass().getName() + ": " + exception.getMessage());
            return null;
        }
    }

    public boolean isFancyFogAvailable()
    {
        return fancyFogAvailable;
    }

    public boolean isOcclusionAvailable()
    {
        return occlusionAvailable;
    }

    public int getMinecraftVersionInt()
    {
        if (minecraftVersionInt < 0)
        {
            String[] astring = tokenize("1.8.9", ".");
            int i = 0;

            if (astring.length > 0)
            {
                i += 10000 * parseInt(astring[0], 0);
            }

            if (astring.length > 1)
            {
                i += 100 * parseInt(astring[1], 0);
            }

            if (astring.length > 2)
            {
                i += 1 * parseInt(astring[2], 0);
            }

            minecraftVersionInt = i;
        }

        return minecraftVersionInt;
    }

    public String getOpenGlVersionString()
    {
        GlVersion glversion = getGlVersion();
        String s = "" + glversion.getMajor() + "." + glversion.getMinor() + "." + glversion.getRelease();
        return s;
    }

    private GlVersion getGlVersionLwjgl()
    {
        return GLContext.getCapabilities().OpenGL44 ? new GlVersion(4, 4) : (GLContext.getCapabilities().OpenGL43 ? new GlVersion(4, 3) : (GLContext.getCapabilities().OpenGL42 ? new GlVersion(4, 2) : (GLContext.getCapabilities().OpenGL41 ? new GlVersion(4, 1) : (GLContext.getCapabilities().OpenGL40 ? new GlVersion(4, 0) : (GLContext.getCapabilities().OpenGL33 ? new GlVersion(3, 3) : (GLContext.getCapabilities().OpenGL32 ? new GlVersion(3, 2) : (GLContext.getCapabilities().OpenGL31 ? new GlVersion(3, 1) : (GLContext.getCapabilities().OpenGL30 ? new GlVersion(3, 0) : (GLContext.getCapabilities().OpenGL21 ? new GlVersion(2, 1) : (GLContext.getCapabilities().OpenGL20 ? new GlVersion(2, 0) : (GLContext.getCapabilities().OpenGL15 ? new GlVersion(1, 5) : (GLContext.getCapabilities().OpenGL14 ? new GlVersion(1, 4) : (GLContext.getCapabilities().OpenGL13 ? new GlVersion(1, 3) : (GLContext.getCapabilities().OpenGL12 ? new GlVersion(1, 2) : (GLContext.getCapabilities().OpenGL11 ? new GlVersion(1, 1) : new GlVersion(1, 0))))))))))))))));
    }

    public GlVersion getGlVersion()
    {
        if (glVersion == null)
        {
            String s = GL11.glGetString(GL11.GL_VERSION);
            glVersion = parseGlVersion(s, (GlVersion)null);

            if (glVersion == null)
            {
                glVersion = getGlVersionLwjgl();
            }

            if (glVersion == null)
            {
                glVersion = new GlVersion(1, 0);
            }
        }

        return glVersion;
    }

    public GlVersion getGlslVersion()
    {
        if (glslVersion == null)
        {
            String s = GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
            glslVersion = parseGlVersion(s, (GlVersion)null);

            if (glslVersion == null)
            {
                glslVersion = new GlVersion(1, 10);
            }
        }

        return glslVersion;
    }

    public GlVersion parseGlVersion(String p_parseGlVersion_0_, GlVersion p_parseGlVersion_1_)
    {
        try
        {
            if (p_parseGlVersion_0_ == null)
            {
                return p_parseGlVersion_1_;
            }
            else
            {
                Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)(\\.([0-9]+))?(.+)?");
                Matcher matcher = pattern.matcher(p_parseGlVersion_0_);

                if (!matcher.matches())
                {
                    return p_parseGlVersion_1_;
                }
                else
                {
                    int i = Integer.parseInt(matcher.group(1));
                    int j = Integer.parseInt(matcher.group(2));
                    int k = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 0;
                    String s = matcher.group(5);
                    return new GlVersion(i, j, k, s);
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return p_parseGlVersion_1_;
        }
    }

    public String[] getOpenGlExtensions()
    {
        if (openGlExtensions == null)
        {
            openGlExtensions = detectOpenGlExtensions();
        }

        return openGlExtensions;
    }

    private String[] detectOpenGlExtensions()
    {
        try
        {
            GlVersion glversion = getGlVersion();

            if (glversion.getMajor() >= 3)
            {
                int i = GL11.glGetInteger(33309);

                if (i > 0)
                {
                    String[] astring = new String[i];

                    for (int j = 0; j < i; ++j)
                    {
                        astring[j] = GL30.glGetStringi(7939, j);
                    }

                    return astring;
                }
            }
        }
        catch (Exception exception1)
        {
            exception1.printStackTrace();
        }

        try
        {
            String s = GL11.glGetString(GL11.GL_EXTENSIONS);
            String[] astring1 = s.split(" ");
            return astring1;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return new String[0];
        }
    }

    public void updateThreadPriorities()
    {
        updateAvailableProcessors();
        int i = 8;

        if (isSingleProcessor())
        {
            if (isSmoothWorld())
            {
                minecraftThread.setPriority(10);
                setThreadPriority("Server thread", 1);
            }
            else
            {
                minecraftThread.setPriority(5);
                setThreadPriority("Server thread", 5);
            }
        }
        else
        {
            minecraftThread.setPriority(10);
            setThreadPriority("Server thread", 5);
        }
    }

    private void setThreadPriority(String p_setThreadPriority_0_, int p_setThreadPriority_1_)
    {
        try
        {
            ThreadGroup threadgroup = Thread.currentThread().getThreadGroup();

            if (threadgroup == null)
            {
                return;
            }

            int i = (threadgroup.activeCount() + 10) * 2;
            Thread[] athread = new Thread[i];
            threadgroup.enumerate(athread, false);

            for (int j = 0; j < athread.length; ++j)
            {
                Thread thread = athread[j];

                if (thread != null && thread.getName().startsWith(p_setThreadPriority_0_))
                {
                    thread.setPriority(p_setThreadPriority_1_);
                }
            }
        }
        catch (Throwable throwable)
        {
            warn(throwable.getClass().getName() + ": " + throwable.getMessage());
        }
    }

    public boolean isMinecraftThread()
    {
        return Thread.currentThread() == minecraftThread;
    }

    private void startVersionCheckThread()
    {
        VersionCheckThread versioncheckthread = new VersionCheckThread();
        versioncheckthread.start();
    }

    public boolean isMipmaps()
    {
        return options.mipmapLevels > 0;
    }

    public int getMipmapLevels()
    {
        return 4;
    }

    public int getMipmapType()
    {
        return 9986;
    }

    public boolean isUseAlphaFunc()
    {
        float f = getAlphaFuncLevel();
        return f > DEF_ALPHA_FUNC_LEVEL.floatValue() + 1.0E-5F;
    }

    public float getAlphaFuncLevel()
    {
        return DEF_ALPHA_FUNC_LEVEL.floatValue();
    }

    public boolean isFogFancy()
    {
        return !isFancyFogAvailable() ? false : options.ofFogType == 2;
    }

    public boolean isFogFast()
    {
        return options.ofFogType == 1;
    }

    public boolean isFogOff()
    {
        return options.ofFogType == 3;
    }

    public float getFogStart()
    {
        return options.ofFogStart;
    }

    public void warn(String p_warn_0_)
    {
        LOGGER.warn(p_warn_0_);
    }

    public void error(String p_error_0_)
    {
        LOGGER.error(p_error_0_);
    }

    public int getUpdatesPerFrame()
    {
        return options.ofChunkUpdates;
    }

    public boolean isDynamicUpdates()
    {
        return options.ofChunkUpdatesDynamic;
    }

    public boolean isRainFancy()
    {
        return options.ofRain == 0 ? options.fancyGraphics : options.ofRain == 2;
    }

    public boolean isRainOff()
    {
        return options.ofRain == 3;
    }

    public boolean isCloudsFancy()
    {
        return options.ofClouds != 0 ? options.ofClouds == 2 : (isShaders() && !Shaders.shaderPackClouds.isDefault() ? Shaders.shaderPackClouds.isFancy() : (texturePackClouds != 0 ? texturePackClouds == 2 : options.fancyGraphics));
    }

    public boolean isCloudsOff()
    {
        return options.ofClouds != 0 ? options.ofClouds == 3 : (isShaders() && !Shaders.shaderPackClouds.isDefault() ? Shaders.shaderPackClouds.isOff() : (texturePackClouds != 0 ? texturePackClouds == 3 : false));
    }

    public void updateTexturePackClouds()
    {
        texturePackClouds = 0;
        IResourceManager iresourcemanager = getResourceManager();

        if (iresourcemanager != null)
        {
            try
            {
                InputStream inputstream = iresourcemanager.getResource(new ResourceLocation("mcpatcher/color.properties")).getInputStream();

                if (inputstream == null)
                {
                    return;
                }

                Properties properties = new Properties();
                properties.load(inputstream);
                inputstream.close();
                String s = properties.getProperty("clouds");

                if (s == null)
                {
                    return;
                }

                s = s.toLowerCase();

                if (s.equals("fast"))
                {
                    texturePackClouds = 1;
                }

                if (s.equals("fancy"))
                {
                    texturePackClouds = 2;
                }

                if (s.equals("off"))
                {
                    texturePackClouds = 3;
                }
            }
            catch (Exception var4)
            {
                ;
            }
        }
    }

    public ModelManager getModelManager()
    {
        return minecraft.getRenderItem().modelManager;
    }

    public boolean isTreesFancy()
    {
        return options.ofTrees == 0 ? options.fancyGraphics : options.ofTrees != 1;
    }

    public boolean isTreesSmart()
    {
        return options.ofTrees == 4;
    }

    public boolean isCullFacesLeaves()
    {
        return options.ofTrees == 0 ? !options.fancyGraphics : options.ofTrees == 4;
    }

    public boolean isDroppedItemsFancy()
    {
        return options.ofDroppedItems == 0 ? options.fancyGraphics : options.ofDroppedItems == 2;
    }

    public int limit(int p_limit_0_, int p_limit_1_, int p_limit_2_)
    {
        return p_limit_0_ < p_limit_1_ ? p_limit_1_ : (p_limit_0_ > p_limit_2_ ? p_limit_2_ : p_limit_0_);
    }

    public float limit(float p_limit_0_, float p_limit_1_, float p_limit_2_)
    {
        return p_limit_0_ < p_limit_1_ ? p_limit_1_ : (p_limit_0_ > p_limit_2_ ? p_limit_2_ : p_limit_0_);
    }

    public double limit(double p_limit_0_, double p_limit_2_, double p_limit_4_)
    {
        return p_limit_0_ < p_limit_2_ ? p_limit_2_ : (p_limit_0_ > p_limit_4_ ? p_limit_4_ : p_limit_0_);
    }

    public float limitTo1(float p_limitTo1_0_)
    {
        return p_limitTo1_0_ < 0.0F ? 0.0F : (p_limitTo1_0_ > 1.0F ? 1.0F : p_limitTo1_0_);
    }

    public boolean isAnimatedWater()
    {
        return options.ofAnimatedWater != 2;
    }

    public boolean isGeneratedWater()
    {
        return options.ofAnimatedWater == 1;
    }

    public boolean isAnimatedPortal()
    {
        return options.ofAnimatedPortal;
    }

    public boolean isAnimatedLava()
    {
        return options.ofAnimatedLava != 2;
    }

    public boolean isGeneratedLava()
    {
        return options.ofAnimatedLava == 1;
    }

    public boolean isAnimatedFire()
    {
        return options.ofAnimatedFire;
    }

    public boolean isAnimatedRedstone()
    {
        return options.ofAnimatedRedstone;
    }

    public boolean isAnimatedExplosion()
    {
        return options.ofAnimatedExplosion;
    }

    public boolean isAnimatedFlame()
    {
        return options.ofAnimatedFlame;
    }

    public boolean isAnimatedSmoke()
    {
        return options.ofAnimatedSmoke;
    }

    public boolean isVoidParticles()
    {
        return options.ofVoidParticles;
    }

    public boolean isWaterParticles()
    {
        return options.ofWaterParticles;
    }

    public boolean isRainSplash()
    {
        return options.ofRainSplash;
    }

    public boolean isPortalParticles()
    {
        return options.ofPortalParticles;
    }

    public boolean isPotionParticles()
    {
        return options.ofPotionParticles;
    }

    public boolean isFireworkParticles()
    {
        return options.ofFireworkParticles;
    }

    public float getAmbientOcclusionLevel()
    {
        return isShaders() && Shaders.aoLevel >= 0.0F ? Shaders.aoLevel : options.ofAoLevel;
    }

    public String arrayToString(Object[] p_arrayToString_0_)
    {
        if (p_arrayToString_0_ == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringbuffer = new StringBuffer(p_arrayToString_0_.length * 5);

            for (int i = 0; i < p_arrayToString_0_.length; ++i)
            {
                Object object = p_arrayToString_0_[i];

                if (i > 0)
                {
                    stringbuffer.append(", ");
                }

                stringbuffer.append(String.valueOf(object));
            }

            return stringbuffer.toString();
        }
    }

    public String arrayToString(int[] p_arrayToString_0_)
    {
        if (p_arrayToString_0_ == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringbuffer = new StringBuffer(p_arrayToString_0_.length * 5);

            for (int i = 0; i < p_arrayToString_0_.length; ++i)
            {
                int j = p_arrayToString_0_[i];

                if (i > 0)
                {
                    stringbuffer.append(", ");
                }

                stringbuffer.append(String.valueOf(j));
            }

            return stringbuffer.toString();
        }
    }

    public ClientEngine getMinecraft()
    {
        return minecraft;
    }

    public TextureManager getTextureManager()
    {
        return minecraft.getTextureManager();
    }

    public IResourceManager getResourceManager()
    {
        return minecraft.getResourceManager();
    }

    public InputStream getResourceStream(ResourceLocation p_getResourceStream_0_) throws IOException
    {
        return getResourceStream(minecraft.getResourceManager(), p_getResourceStream_0_);
    }

    public InputStream getResourceStream(IResourceManager p_getResourceStream_0_, ResourceLocation p_getResourceStream_1_) throws IOException
    {
        IResource iresource = p_getResourceStream_0_.getResource(p_getResourceStream_1_);
        return iresource == null ? null : iresource.getInputStream();
    }

    public IResource getResource(ResourceLocation p_getResource_0_) throws IOException
    {
        return minecraft.getResourceManager().getResource(p_getResource_0_);
    }

    public boolean hasResource(ResourceLocation p_hasResource_0_)
    {
        IResourcePack iresourcepack = getDefiningResourcePack(p_hasResource_0_);
        return iresourcepack != null;
    }

    public boolean hasResource(IResourceManager p_hasResource_0_, ResourceLocation p_hasResource_1_)
    {
        try
        {
            IResource iresource = p_hasResource_0_.getResource(p_hasResource_1_);
            return iresource != null;
        }
        catch (IOException var3)
        {
            return false;
        }
    }

    public IResourcePack[] getResourcePacks()
    {
        ResourcePackRepository resourcepackrepository = minecraft.getResourcePackRepository();
        List list = resourcepackrepository.getRepositoryEntries();
        List list1 = new ArrayList();

        for (Object resourcepackrepository$entry : list)
        {
            list1.add(((Entry) resourcepackrepository$entry).getResourcePack());
        }

        if (resourcepackrepository.getResourcePackInstance() != null)
        {
            list1.add(resourcepackrepository.getResourcePackInstance());
        }

        IResourcePack[] airesourcepack = (IResourcePack[])((IResourcePack[])list1.toArray(new IResourcePack[list1.size()]));
        return airesourcepack;
    }

    public String getResourcePackNames()
    {
        if (minecraft.getResourcePackRepository() == null)
        {
            return "";
        }
        else
        {
            IResourcePack[] airesourcepack = getResourcePacks();

            if (airesourcepack.length <= 0)
            {
                return getDefaultResourcePack().getPackName();
            }
            else
            {
                String[] astring = new String[airesourcepack.length];

                for (int i = 0; i < airesourcepack.length; ++i)
                {
                    astring[i] = airesourcepack[i].getPackName();
                }

                String s = arrayToString((Object[])astring);
                return s;
            }
        }
    }

    public DefaultResourcePack getDefaultResourcePack()
    {
        if (defaultResourcePackLazy == null)
        {
            defaultResourcePackLazy = (DefaultResourcePack)Reflector.getFieldValue(minecraft, Reflector.Minecraft_defaultResourcePack);

            if (defaultResourcePackLazy == null)
            {
                ResourcePackRepository resourcepackrepository = minecraft.getResourcePackRepository();

                if (resourcepackrepository != null)
                {
                    defaultResourcePackLazy = (DefaultResourcePack)resourcepackrepository.rprDefaultResourcePack;
                }
            }
        }

        return defaultResourcePackLazy;
    }

    public boolean isFromDefaultResourcePack(ResourceLocation p_isFromDefaultResourcePack_0_)
    {
        IResourcePack iresourcepack = getDefiningResourcePack(p_isFromDefaultResourcePack_0_);
        return iresourcepack == getDefaultResourcePack();
    }

    public IResourcePack getDefiningResourcePack(ResourceLocation p_getDefiningResourcePack_0_)
    {
        ResourcePackRepository resourcepackrepository = minecraft.getResourcePackRepository();
        IResourcePack iresourcepack = resourcepackrepository.getResourcePackInstance();

        if (iresourcepack != null && iresourcepack.resourceExists(p_getDefiningResourcePack_0_))
        {
            return iresourcepack;
        }
        else
        {
            List<ResourcePackRepository.Entry> list = (List)Reflector.getFieldValue(resourcepackrepository, Reflector.ResourcePackRepository_repositoryEntries);

            if (list != null)
            {
                for (int i = list.size() - 1; i >= 0; --i)
                {
                    ResourcePackRepository.Entry resourcepackrepository$entry = (ResourcePackRepository.Entry)list.get(i);
                    IResourcePack iresourcepack1 = resourcepackrepository$entry.getResourcePack();

                    if (iresourcepack1.resourceExists(p_getDefiningResourcePack_0_))
                    {
                        return iresourcepack1;
                    }
                }
            }

            return getDefaultResourcePack().resourceExists(p_getDefiningResourcePack_0_) ? getDefaultResourcePack() : null;
        }
    }

    public RenderGlobal getRenderGlobal()
    {
        return minecraft.renderGlobal;
    }

    public boolean isBetterGrass()
    {
        return options.ofBetterGrass != 3;
    }

    public boolean isBetterGrassFancy()
    {
        return options.ofBetterGrass == 2;
    }

    public boolean isWeatherEnabled()
    {
        return options.ofWeather;
    }

    public boolean isSkyEnabled()
    {
        return options.ofSky;
    }

    public boolean isSunMoonEnabled()
    {
        return options.ofSunMoon;
    }

    public boolean isSunTexture()
    {
        return !isSunMoonEnabled() ? false : !isShaders() || Shaders.isSun();
    }

    public boolean isMoonTexture()
    {
        return !isSunMoonEnabled() ? false : !isShaders() || Shaders.isMoon();
    }

    public boolean isVignetteEnabled()
    {
        return isShaders() && !Shaders.isVignette() ? false : (options.ofVignette == 0 ? options.fancyGraphics : options.ofVignette == 2);
    }

    public boolean isStarsEnabled()
    {
        return options.ofStars;
    }

    public void sleep(long p_sleep_0_)
    {
        try
        {
            Thread.sleep(p_sleep_0_);
        }
        catch (InterruptedException interruptedexception)
        {
            interruptedexception.printStackTrace();
        }
    }

    public boolean isTimeDayOnly()
    {
        return options.ofTime == 1;
    }

    public boolean isTimeDefault()
    {
        return options.ofTime == 0;
    }

    public boolean isTimeNightOnly()
    {
        return options.ofTime == 2;
    }

    public boolean isClearWater()
    {
        return options.ofClearWater;
    }

    public int getAnisotropicFilterLevel()
    {
        return options.ofAfLevel;
    }

    public boolean isAnisotropicFiltering()
    {
        return getAnisotropicFilterLevel() > 1;
    }

    public int getAntialiasingLevel()
    {
        return antialiasingLevel;
    }

    public boolean isAntialiasing()
    {
        return getAntialiasingLevel() > 0;
    }

    public boolean isAntialiasingConfigured()
    {
        return getoptions().ofAaLevel > 0;
    }

    public boolean isMultiTexture()
    {
        return getAnisotropicFilterLevel() > 1 ? true : getAntialiasingLevel() > 0;
    }

    public boolean between(int p_between_0_, int p_between_1_, int p_between_2_)
    {
        return p_between_0_ >= p_between_1_ && p_between_0_ <= p_between_2_;
    }

    public boolean isDrippingWaterLava()
    {
        return options.ofDrippingWaterLava;
    }

    public boolean isBetterSnow()
    {
        return options.ofBetterSnow;
    }

    public Dimension getFullscreenDimension()
    {
        if (desktopDisplayMode == null)
        {
            return null;
        }
        else if (options == null)
        {
            return new Dimension(desktopDisplayMode.getWidth(), desktopDisplayMode.getHeight());
        }
        else
        {
            String s = options.ofFullscreenMode;

            if (s.equals("Default"))
            {
                return new Dimension(desktopDisplayMode.getWidth(), desktopDisplayMode.getHeight());
            }
            else
            {
                String[] astring = tokenize(s, " x");
                return astring.length < 2 ? new Dimension(desktopDisplayMode.getWidth(), desktopDisplayMode.getHeight()) : new Dimension(parseInt(astring[0], -1), parseInt(astring[1], -1));
            }
        }
    }

    public int parseInt(String p_parseInt_0_, int p_parseInt_1_)
    {
        try
        {
            if (p_parseInt_0_ == null)
            {
                return p_parseInt_1_;
            }
            else
            {
                p_parseInt_0_ = p_parseInt_0_.trim();
                return Integer.parseInt(p_parseInt_0_);
            }
        }
        catch (NumberFormatException var3)
        {
            return p_parseInt_1_;
        }
    }

    public float parseFloat(String p_parseFloat_0_, float p_parseFloat_1_)
    {
        try
        {
            if (p_parseFloat_0_ == null)
            {
                return p_parseFloat_1_;
            }
            else
            {
                p_parseFloat_0_ = p_parseFloat_0_.trim();
                return Float.parseFloat(p_parseFloat_0_);
            }
        }
        catch (NumberFormatException var3)
        {
            return p_parseFloat_1_;
        }
    }

    public boolean parseBoolean(String p_parseBoolean_0_, boolean p_parseBoolean_1_)
    {
        try
        {
            if (p_parseBoolean_0_ == null)
            {
                return p_parseBoolean_1_;
            }
            else
            {
                p_parseBoolean_0_ = p_parseBoolean_0_.trim();
                return Boolean.parseBoolean(p_parseBoolean_0_);
            }
        }
        catch (NumberFormatException var3)
        {
            return p_parseBoolean_1_;
        }
    }

    public String[] tokenize(String p_tokenize_0_, String p_tokenize_1_)
    {
        StringTokenizer stringtokenizer = new StringTokenizer(p_tokenize_0_, p_tokenize_1_);
        List list = new ArrayList();

        while (stringtokenizer.hasMoreTokens())
        {
            String s = stringtokenizer.nextToken();
            list.add(s);
        }

        String[] astring = (String[])((String[])list.toArray(new String[list.size()]));
        return astring;
    }

    public DisplayMode getDesktopDisplayMode()
    {
        return desktopDisplayMode;
    }

    public DisplayMode[] getDisplayModes()
    {
        if (displayModes == null)
        {
            try
            {
                DisplayMode[] adisplaymode = Display.get().getAvailableDisplayModes();
                Set<Dimension> set = getDisplayModeDimensions(adisplaymode);
                List list = new ArrayList();

                for (Dimension dimension : set)
                {
                    DisplayMode[] adisplaymode1 = getDisplayModes(adisplaymode, dimension);
                    DisplayMode displaymode = getDisplayMode(adisplaymode1, desktopDisplayMode);

                    if (displaymode != null)
                    {
                        list.add(displaymode);
                    }
                }

                DisplayMode[] adisplaymode2 = (DisplayMode[])((DisplayMode[])list.toArray(new DisplayMode[list.size()]));
                Arrays.sort(adisplaymode2, new DisplayModeComparator());
                return adisplaymode2;
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                displayModes = new DisplayMode[] {desktopDisplayMode};
            }
        }

        return displayModes;
    }

    public DisplayMode getLargestDisplayMode()
    {
        DisplayMode[] adisplaymode = getDisplayModes();

        if (adisplaymode != null && adisplaymode.length >= 1)
        {
            DisplayMode displaymode = adisplaymode[adisplaymode.length - 1];
            return desktopDisplayMode.getWidth() > displaymode.getWidth() ? desktopDisplayMode : (desktopDisplayMode.getWidth() == displaymode.getWidth() && desktopDisplayMode.getHeight() > displaymode.getHeight() ? desktopDisplayMode : displaymode);
        }
        else
        {
            return desktopDisplayMode;
        }
    }

    private Set<Dimension> getDisplayModeDimensions(DisplayMode[] p_getDisplayModeDimensions_0_)
    {
        Set<Dimension> set = new HashSet();

        for (int i = 0; i < p_getDisplayModeDimensions_0_.length; ++i)
        {
            DisplayMode displaymode = p_getDisplayModeDimensions_0_[i];
            Dimension dimension = new Dimension(displaymode.getWidth(), displaymode.getHeight());
            set.add(dimension);
        }

        return set;
    }

    private DisplayMode[] getDisplayModes(DisplayMode[] p_getDisplayModes_0_, Dimension p_getDisplayModes_1_)
    {
        List list = new ArrayList();

        for (int i = 0; i < p_getDisplayModes_0_.length; ++i)
        {
            DisplayMode displaymode = p_getDisplayModes_0_[i];

            if ((double)displaymode.getWidth() == p_getDisplayModes_1_.getWidth() && (double)displaymode.getHeight() == p_getDisplayModes_1_.getHeight())
            {
                list.add(displaymode);
            }
        }

        DisplayMode[] adisplaymode = (DisplayMode[])((DisplayMode[])list.toArray(new DisplayMode[list.size()]));
        return adisplaymode;
    }

    private DisplayMode getDisplayMode(DisplayMode[] p_getDisplayMode_0_, DisplayMode p_getDisplayMode_1_)
    {
        if (p_getDisplayMode_1_ != null)
        {
            for (int i = 0; i < p_getDisplayMode_0_.length; ++i)
            {
                DisplayMode displaymode = p_getDisplayMode_0_[i];

                if (displaymode.getBitsPerPixel() == p_getDisplayMode_1_.getBitsPerPixel() && displaymode.getFrequency() == p_getDisplayMode_1_.getFrequency())
                {
                    return displaymode;
                }
            }
        }

        if (p_getDisplayMode_0_.length <= 0)
        {
            return null;
        }
        else
        {
            Arrays.sort(p_getDisplayMode_0_, new DisplayModeComparator());
            return p_getDisplayMode_0_[p_getDisplayMode_0_.length - 1];
        }
    }

    public String[] getDisplayModeNames()
    {
        DisplayMode[] adisplaymode = getDisplayModes();
        String[] astring = new String[adisplaymode.length];

        for (int i = 0; i < adisplaymode.length; ++i)
        {
            DisplayMode displaymode = adisplaymode[i];
            String s = "" + displaymode.getWidth() + "x" + displaymode.getHeight();
            astring[i] = s;
        }

        return astring;
    }

    public DisplayMode getDisplayMode(Dimension p_getDisplayMode_0_) throws LWJGLException
    {
        DisplayMode[] adisplaymode = getDisplayModes();

        for (int i = 0; i < adisplaymode.length; ++i)
        {
            DisplayMode displaymode = adisplaymode[i];

            if (displaymode.getWidth() == p_getDisplayMode_0_.width && displaymode.getHeight() == p_getDisplayMode_0_.height)
            {
                return displaymode;
            }
        }

        return desktopDisplayMode;
    }

    public boolean isAnimatedTerrain()
    {
        return options.ofAnimatedTerrain;
    }

    public boolean isAnimatedTextures()
    {
        return options.ofAnimatedTextures;
    }

    public boolean isSwampColors()
    {
        return options.ofSwampColors;
    }

    public boolean isRandomMobs()
    {
        return options.ofRandomMobs;
    }

    public void checkGlError(String p_checkGlError_0_)
    {
        int i = GL11.glGetError();

        if (i != 0)
        {
            String s = GLU.gluErrorString(i);
            error("OpenGlError: " + i + " (" + s + "), at: " + p_checkGlError_0_);
        }
    }

    public boolean isSmoothBiomes()
    {
        return options.ofSmoothBiomes;
    }

    public boolean isCustomColors()
    {
        return options.ofCustomColors;
    }

    public boolean isCustomSky()
    {
        return options.ofCustomSky;
    }

    public boolean isCustomFonts()
    {
        return options.ofCustomFonts;
    }

    public boolean isShowCapes()
    {
        return options.ofShowCapes;
    }

    public boolean isConnectedTextures()
    {
        return options.ofConnectedTextures != 3;
    }

    public boolean isNaturalTextures()
    {
        return options.ofNaturalTextures;
    }

    public boolean isConnectedTexturesFancy()
    {
        return options.ofConnectedTextures == 2;
    }

    public boolean isFastRender()
    {
        return options.ofFastRender;
    }

    public boolean isTranslucentBlocksFancy()
    {
        return options.ofTranslucentBlocks == 0 ? options.fancyGraphics : options.ofTranslucentBlocks == 2;
    }

    public boolean isShaders()
    {
        return Shaders.shaderPackLoaded;
    }

    public String[] readLines(File p_readLines_0_) throws IOException
    {
        FileInputStream fileinputstream = new FileInputStream(p_readLines_0_);
        return readLines((InputStream)fileinputstream);
    }

    public String[] readLines(InputStream p_readLines_0_) throws IOException
    {
        List list = new ArrayList();
        InputStreamReader inputstreamreader = new InputStreamReader(p_readLines_0_, "ASCII");
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

        while (true)
        {
            String s = bufferedreader.readLine();

            if (s == null)
            {
                String[] astring = (String[])((String[])list.toArray(new String[list.size()]));
                return astring;
            }

            list.add(s);
        }
    }

    public String readFile(File p_readFile_0_) throws IOException
    {
        FileInputStream fileinputstream = new FileInputStream(p_readFile_0_);
        return readInputStream(fileinputstream, "ASCII");
    }

    public String readInputStream(InputStream p_readInputStream_0_) throws IOException
    {
        return readInputStream(p_readInputStream_0_, "ASCII");
    }

    public String readInputStream(InputStream p_readInputStream_0_, String p_readInputStream_1_) throws IOException
    {
        InputStreamReader inputstreamreader = new InputStreamReader(p_readInputStream_0_, p_readInputStream_1_);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        StringBuffer stringbuffer = new StringBuffer();

        while (true)
        {
            String s = bufferedreader.readLine();

            if (s == null)
            {
                return stringbuffer.toString();
            }

            stringbuffer.append(s);
            stringbuffer.append("\n");
        }
    }

    public byte[] readAll(InputStream p_readAll_0_) throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        byte[] abyte = new byte[1024];

        while (true)
        {
            int i = p_readAll_0_.read(abyte);

            if (i < 0)
            {
                p_readAll_0_.close();
                byte[] abyte1 = bytearrayoutputstream.toByteArray();
                return abyte1;
            }

            bytearrayoutputstream.write(abyte, 0, i);
        }
    }

    public GameOptions getoptions()
    {
        return options;
    }

    public int compareRelease(String p_compareRelease_0_, String p_compareRelease_1_)
    {
        String[] astring = splitRelease(p_compareRelease_0_);
        String[] astring1 = splitRelease(p_compareRelease_1_);
        String s = astring[0];
        String s1 = astring1[0];

        if (!s.equals(s1))
        {
            return s.compareTo(s1);
        }
        else
        {
            int i = parseInt(astring[1], -1);
            int j = parseInt(astring1[1], -1);

            if (i != j)
            {
                return i - j;
            }
            else
            {
                String s2 = astring[2];
                String s3 = astring1[2];

                if (!s2.equals(s3))
                {
                    if (s2.isEmpty())
                    {
                        return 1;
                    }

                    if (s3.isEmpty())
                    {
                        return -1;
                    }
                }

                return s2.compareTo(s3);
            }
        }
    }

    private String[] splitRelease(String p_splitRelease_0_)
    {
        if (p_splitRelease_0_ != null && p_splitRelease_0_.length() > 0)
        {
            Pattern pattern = Pattern.compile("([A-Z])([0-9]+)(.*)");
            Matcher matcher = pattern.matcher(p_splitRelease_0_);

            if (!matcher.matches())
            {
                return new String[] {"", "", ""};
            }
            else
            {
                String s = normalize(matcher.group(1));
                String s1 = normalize(matcher.group(2));
                String s2 = normalize(matcher.group(3));
                return new String[] {s, s1, s2};
            }
        }
        else
        {
            return new String[] {"", "", ""};
        }
    }

    public int intHash(int p_intHash_0_)
    {
        p_intHash_0_ = p_intHash_0_ ^ 61 ^ p_intHash_0_ >> 16;
        p_intHash_0_ = p_intHash_0_ + (p_intHash_0_ << 3);
        p_intHash_0_ = p_intHash_0_ ^ p_intHash_0_ >> 4;
        p_intHash_0_ = p_intHash_0_ * 668265261;
        p_intHash_0_ = p_intHash_0_ ^ p_intHash_0_ >> 15;
        return p_intHash_0_;
    }

    public int getRandom(BlockPos p_getRandom_0_, int p_getRandom_1_)
    {
        int i = intHash(p_getRandom_1_ + 37);
        i = intHash(i + p_getRandom_0_.getX());
        i = intHash(i + p_getRandom_0_.getZ());
        i = intHash(i + p_getRandom_0_.getY());
        return i;
    }

    public WorldServer getWorldServer()
    {
        World world = minecraft.world;

        if (world == null)
        {
            return null;
        }
        else if (!minecraft.isIntegratedServerRunning())
        {
            return null;
        }
        else
        {
            IntegratedServer integratedserver = minecraft.getIntegratedServer();

            if (integratedserver == null)
            {
                return null;
            }
            else
            {
                WorldProvider worldprovider = world.provider;

                if (worldprovider == null)
                {
                    return null;
                }
                else
                {
                    int i = worldprovider.getDimensionId();

                    try
                    {
                        WorldServer worldserver = integratedserver.worldServerForDimension(i);
                        return worldserver;
                    }
                    catch (NullPointerException var5)
                    {
                        return null;
                    }
                }
            }
        }
    }

    public int getAvailableProcessors()
    {
        return availableProcessors;
    }

    public void updateAvailableProcessors()
    {
        availableProcessors = Runtime.getRuntime().availableProcessors();
    }

    public boolean isSingleProcessor()
    {
        return getAvailableProcessors() <= 1;
    }

    public boolean isSmoothWorld()
    {
        return options.ofSmoothWorld;
    }

    public boolean isLazyChunkLoading()
    {
        return !isSingleProcessor() ? false : options.ofLazyChunkLoading;
    }

    public boolean isDynamicFov()
    {
        return options.ofDynamicFov;
    }

    public boolean isAlternateBlocks()
    {
        return options.allowBlockAlternatives;
    }

    public int getChunkViewDistance()
    {
        if (options == null)
        {
            return 10;
        }
        else
        {
            int i = options.renderDistanceChunks;
            return i;
        }
    }

    public boolean equals(Object p_equals_0_, Object p_equals_1_)
    {
        return p_equals_0_ == p_equals_1_ ? true : (p_equals_0_ == null ? false : p_equals_0_.equals(p_equals_1_));
    }

    public boolean equalsOne(Object p_equalsOne_0_, Object[] p_equalsOne_1_)
    {
        if (p_equalsOne_1_ == null)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < p_equalsOne_1_.length; ++i)
            {
                Object object = p_equalsOne_1_[i];

                if (equals(p_equalsOne_0_, object))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isSameOne(Object p_isSameOne_0_, Object[] p_isSameOne_1_)
    {
        if (p_isSameOne_1_ == null)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < p_isSameOne_1_.length; ++i)
            {
                Object object = p_isSameOne_1_[i];

                if (p_isSameOne_0_ == object)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public String normalize(String p_normalize_0_)
    {
        return p_normalize_0_ == null ? "" : p_normalize_0_;
    }

    public void checkDisplaySettings()
    {
        int i = getAntialiasingLevel();

        if (i > 0)
        {
            DisplayMode displaymode = Display.get().getDisplayMode();

            try
            {
                Display.get().destroy();
                Display.get().setDisplayMode(displaymode);
                Display.get().create((new PixelFormat()).withDepthBits(24).withSamples(i), minecraft.isHeadless());
                Display.get().setResizable(false);
                Display.get().setResizable(true);
            }
            catch (LWJGLException lwjglexception2)
            {
                warn("Error setting FSAA: " + i + "x");
                lwjglexception2.printStackTrace();

                try
                {
                    Display.get().setDisplayMode(displaymode);
                    Display.get().create((new PixelFormat()).withDepthBits(24), minecraft.isHeadless());
                    Display.get().setResizable(false);
                    Display.get().setResizable(true);
                }
                catch (LWJGLException lwjglexception1)
                {
                    lwjglexception1.printStackTrace();

                    try
                    {
                        Display.get().setDisplayMode(displaymode);
                        Display.get().create(minecraft.isHeadless());
                        Display.get().setResizable(false);
                        Display.get().setResizable(true);
                    }
                    catch (LWJGLException lwjglexception)
                    {
                        lwjglexception.printStackTrace();
                    }
                }
            }
        }
    }

    private ByteBuffer readIconImage(InputStream p_readIconImage_0_) throws IOException
    {
        BufferedImage bufferedimage = ImageIO.read(p_readIconImage_0_);
        int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), (int[])null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);

        for (int i : aint)
        {
            bytebuffer.putInt(i << 8 | i >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }

    public void checkDisplayMode()
    {
        try
        {
            if (minecraft.isFullScreen())
            {
                if (fullscreenModeChecked)
                {
                    return;
                }

                fullscreenModeChecked = true;
                desktopModeChecked = false;
                DisplayMode displaymode = Display.get().getDisplayMode();
                Dimension dimension = getFullscreenDimension();

                if (dimension == null)
                {
                    return;
                }

                if (displaymode.getWidth() == dimension.width && displaymode.getHeight() == dimension.height)
                {
                    return;
                }

                DisplayMode displaymode1 = getDisplayMode(dimension);

                if (displaymode1 == null)
                {
                    return;
                }

                Display.get().setDisplayMode(displaymode1);
                minecraft.displayWidth = Display.get().getDisplayMode().getWidth();
                minecraft.displayHeight = Display.get().getDisplayMode().getHeight();

                if (minecraft.displayWidth <= 0)
                {
                    minecraft.displayWidth = 1;
                }

                if (minecraft.displayHeight <= 0)
                {
                    minecraft.displayHeight = 1;
                }

                if (minecraft.currentScreen != null)
                {
                    ScaledResolution scaledresolution = ScaledResolution.get();
                    int i = scaledresolution.getScaledWidth();
                    int j = scaledresolution.getScaledHeight();
                    minecraft.currentScreen.setWorldAndResolution(minecraft, i, j);
                }

                minecraft.loadingScreen = new LoadingScreenRenderer(minecraft);
                updateFramebufferSize();
                System.out.println(true);
                Display.get().setFullscreen(true);
                Display.get().setVSyncEnabled(true);
                GlStateManager.get().enableTexture2D();
            }
            else
            {
                if (desktopModeChecked)
                {
                    return;
                }

                desktopModeChecked = true;
                fullscreenModeChecked = false;
                Display.get().setVSyncEnabled(true);
                Display.get().update();
                GlStateManager.get().enableTexture2D();
                Display.get().setResizable(false);
                Display.get().setResizable(true);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            options.ofFullscreenMode = "Default";
            options.saveOfOptions();
        }
    }

    public void updateFramebufferSize()
    {
        minecraft.getFramebuffer().createBindFramebuffer(minecraft.displayWidth, minecraft.displayHeight);

        if (minecraft.entityRenderer != null)
        {
            minecraft.entityRenderer.updateShaderGroupSize(minecraft.displayWidth, minecraft.displayHeight);
        }
    }

    public Object[] addObjectToArray(Object[] p_addObjectToArray_0_, Object p_addObjectToArray_1_)
    {
        if (p_addObjectToArray_0_ == null)
        {
            throw new NullPointerException("The given array is NULL");
        }
        else
        {
            int i = p_addObjectToArray_0_.length;
            int j = i + 1;
            Object[] aobject = (Object[])((Object[])Array.newInstance(p_addObjectToArray_0_.getClass().getComponentType(), j));
            System.arraycopy(p_addObjectToArray_0_, 0, aobject, 0, i);
            aobject[i] = p_addObjectToArray_1_;
            return aobject;
        }
    }

    public Object[] addObjectToArray(Object[] p_addObjectToArray_0_, Object p_addObjectToArray_1_, int p_addObjectToArray_2_)
    {
        List list = new ArrayList(Arrays.asList(p_addObjectToArray_0_));
        list.add(p_addObjectToArray_2_, p_addObjectToArray_1_);
        Object[] aobject = (Object[])((Object[])Array.newInstance(p_addObjectToArray_0_.getClass().getComponentType(), list.size()));
        return list.toArray(aobject);
    }

    public Object[] addObjectsToArray(Object[] p_addObjectsToArray_0_, Object[] p_addObjectsToArray_1_)
    {
        if (p_addObjectsToArray_0_ == null)
        {
            throw new NullPointerException("The given array is NULL");
        }
        else if (p_addObjectsToArray_1_.length == 0)
        {
            return p_addObjectsToArray_0_;
        }
        else
        {
            int i = p_addObjectsToArray_0_.length;
            int j = i + p_addObjectsToArray_1_.length;
            Object[] aobject = (Object[])((Object[])Array.newInstance(p_addObjectsToArray_0_.getClass().getComponentType(), j));
            System.arraycopy(p_addObjectsToArray_0_, 0, aobject, 0, i);
            System.arraycopy(p_addObjectsToArray_1_, 0, aobject, i, p_addObjectsToArray_1_.length);
            return aobject;
        }
    }

    public boolean isCustomItems()
    {
        return options.ofCustomItems;
    }

    public void drawFps()
    {
    }

    private String getUpdates(String p_getUpdates_0_)
    {
        int i = p_getUpdates_0_.indexOf(40);

        if (i < 0)
        {
            return "";
        }
        else
        {
            int j = p_getUpdates_0_.indexOf(32, i);
            return j < 0 ? "" : p_getUpdates_0_.substring(i + 1, j);
        }
    }

    public int getBitsOs()
    {
        String s = System.getenv("ProgramFiles(X86)");
        return s != null ? 64 : 32;
    }

    public int getBitsJre()
    {
        String[] astring = new String[] {"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

        for (int i = 0; i < astring.length; ++i)
        {
            String s = astring[i];
            String s1 = System.getProperty(s);

            if (s1 != null && s1.contains("64"))
            {
                return 64;
            }
        }

        return 32;
    }

    public boolean isConnectedModels()
    {
        return false;
    }

    public void showGuiMessage(String p_showGuiMessage_0_, String p_showGuiMessage_1_)
    {
        GuiMessage guimessage = new GuiMessage(minecraft.currentScreen, p_showGuiMessage_0_, p_showGuiMessage_1_);
        minecraft.displayGuiScreen(guimessage);
    }

    public int[] addIntToArray(int[] p_addIntToArray_0_, int p_addIntToArray_1_)
    {
        return addIntsToArray(p_addIntToArray_0_, new int[] {p_addIntToArray_1_});
    }

    public int[] addIntsToArray(int[] p_addIntsToArray_0_, int[] p_addIntsToArray_1_)
    {
        if (p_addIntsToArray_0_ != null && p_addIntsToArray_1_ != null)
        {
            int i = p_addIntsToArray_0_.length;
            int j = i + p_addIntsToArray_1_.length;
            int[] aint = new int[j];
            System.arraycopy(p_addIntsToArray_0_, 0, aint, 0, i);

            for (int k = 0; k < p_addIntsToArray_1_.length; ++k)
            {
                aint[k + i] = p_addIntsToArray_1_[k];
            }

            return aint;
        }
        else
        {
            throw new NullPointerException("The given array is NULL");
        }
    }

    public DynamicTexture getMojangLogoTexture(DynamicTexture p_getMojangLogoTexture_0_)
    {
        try
        {
            ResourceLocation resourcelocation = new ResourceLocation("textures/gui/title/mojang.png");
            InputStream inputstream = getResourceStream(resourcelocation);

            if (inputstream == null)
            {
                return p_getMojangLogoTexture_0_;
            }
            else
            {
                BufferedImage bufferedimage = ImageIO.read(inputstream);

                if (bufferedimage == null)
                {
                    return p_getMojangLogoTexture_0_;
                }
                else
                {
                    DynamicTexture dynamictexture = new DynamicTexture(bufferedimage);
                    return dynamictexture;
                }
            }
        }
        catch (Exception exception)
        {
            warn(exception.getClass().getName() + ": " + exception.getMessage());
            return p_getMojangLogoTexture_0_;
        }
    }

    public void writeFile(File p_writeFile_0_, String p_writeFile_1_) throws IOException
    {
        FileOutputStream fileoutputstream = new FileOutputStream(p_writeFile_0_);
        byte[] abyte = p_writeFile_1_.getBytes("ASCII");
        fileoutputstream.write(abyte);
        fileoutputstream.close();
    }

    public TextureMap getTextureMap()
    {
        return getMinecraft().getTextureMapBlocks();
    }

    public boolean isDynamicLights()
    {
        return options.ofDynamicLights != 3;
    }

    public boolean isDynamicLightsFast()
    {
        return options.ofDynamicLights == 1;
    }

    public void setMinecraft(ClientEngine mc)
    {
        this.minecraft = mc;
        this.options = mc.options;
    }

    public boolean isDynamicHandLight()
    {
        return !isDynamicLights() ? false : (isShaders() ? Shaders.isDynamicHandLight() : true);
    }
    
    public static Config get()
    {
        String name = Thread.currentThread().getName();
        
        if (configs.containsKey(name))
        {
            return configs.get(name);
        }
        
        else if (configs.containsKey(name.split(" ")[0]))
        {
            return configs.get(name.split(" ")[0]);
        }
        
        Config config = new Config();
        configs.put(name, config);
        config.minecraftThread = Thread.currentThread();
        return config;
    }
}
