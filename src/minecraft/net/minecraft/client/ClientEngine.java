package net.minecraft.client;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.display.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.*;
import com.google.common.util.concurrent.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import net.droidmine.MineBot;
import net.droidmine.Session;
import net.droidmine.pathfinder.PathWalker;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.optifine.Config;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.stats.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Timer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.*;

public class ClientEngine implements IThreadListener
{
    private final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationMojangPng = new ResourceLocation("textures/gui/title/mojang.png");
    public static final boolean isRunningOnMac = Util.getOSType() == Util.EnumOS.OSX;
    private static final List<DisplayMode> macDisplayModes = Lists.newArrayList(new DisplayMode[] {new DisplayMode(2560, 1600), new DisplayMode(2880, 1800)});
    public Runnable gameLoop = null, tickEvent = null, joinEvent = null;
    public Function packetReceiveEvent = null, packetSendEvent = null;
    public Consumer chatEvent = null;
    private final File fileResourcepacks;
    private final PropertyMap twitchDetails;
    private final PropertyMap field_181038_N;
    private ServerData currentServerData;
    public PathWalker pathWalker;

    /** The RenderEngine instance used by Minecraft */
    private TextureManager renderEngine;

    public PlayerControllerMP playerController;
    public boolean disconnect = false;
    private boolean fullscreen;
    private boolean hasCrashed;

    /** Instance of CrashReport. */
    private CrashReport crashReporter;
    public int displayWidth;
    public int displayHeight;
    private boolean field_181541_X = false;
    public Timer timer = new Timer(20.0F);
    public WorldClient world;
    public RenderGlobal renderGlobal;
    private RenderManager renderManager;
    private RenderItem renderItem;
    private ItemRenderer itemRenderer;
    public EntityPlayerSP player;
    private Entity renderViewEntity;
    public Entity pointedEntity;
    public EffectRenderer effectRenderer;
    private final Session session;
    private boolean isGamePaused;
    public boolean exitGame = false;

    /** The font renderer used for displaying and measuring text */
    public FontRenderer fontRendererObj;
    public FontRenderer standardGalacticFontRenderer;

    /** The GuiScreen that's being displayed at the moment. */
    public GuiScreen currentScreen;
    public LoadingScreenRenderer loadingScreen;
    public EntityRenderer entityRenderer;

    /** Mouse left click counter */
    private int leftClickCounter;

    /** Display width */
    private int tempDisplayWidth;

    /** Display height */
    private int tempDisplayHeight;

    /** Instance of IntegratedServer. */
    private IntegratedServer theIntegratedServer;

    /** Gui achievement */
    public GuiAchievement guiAchievement;
    public GuiIngame ingameGUI;

    /** Skip render world */
    public boolean skipRenderWorld;

    /** The ray trace hit that the mouse is over. */
    public MovingObjectPosition hitResult;

    /** The game settings that currently hold effect. */
    public GameOptions options;

    /** Mouse helper instance. */
    public MouseHelper mouseHelper;
    public final File mcDataDir;
    private final File fileAssets;
    private final String launchedVersion;
    private final Proxy proxy;
    private ISaveFormat saveLoader;

    /**
     * This is set to fpsCounter every debug screen update, and is shown on the debug screen. It's also sent as part of
     * the usage snooping.
     */
    private int debugFPS;

    /**
     * When you place a block, it's set to 6, decremented once per tick, when it's 0, you can place another block.
     */
    private int rightClickDelayTimer;
    private String serverName;
    private int serverPort;

    /**
     * Does the actual gameplay have focus. If so then mouse and keys will effect the player instead of menus.
     */
    public boolean inGameHasFocus;
    long systemTime = getSystemTime();

    /** Join player counter */
    private int joinPlayerCounter;
    public final FrameTimer field_181542_y = new FrameTimer();
    long field_181543_z = System.nanoTime();
    private final boolean jvm64bit;
    private final boolean isDemo;
    private NetworkManager myNetworkManager;
    private boolean integratedServerIsRunning;

    private IReloadableResourceManager mcResourceManager;
    private final IMetadataSerializer metadataSerializer_ = new IMetadataSerializer();
    private final List<IResourcePack> defaultResourcePacks = Lists.<IResourcePack>newArrayList();
    private final DefaultResourcePack mcDefaultResourcePack;
    private ResourcePackRepository mcResourcePackRepository;
    private LanguageManager mcLanguageManager;
    private Framebuffer framebufferMc;
    private TextureMap textureMapBlocks;
    private ResourceLocation mojangLogo;
    private final MinecraftSessionService sessionService;
    private SkinManager skinManager;
    private final Queue < FutureTask<? >> scheduledTasks = Queues. < FutureTask<? >> newArrayDeque();
    private long field_175615_aJ = 0L;
    private final Thread mcThread = Thread.currentThread();
    private ModelManager modelManager;

    /**
     * The BlockRenderDispatcher instance that will be used based off options
     */
    private BlockRendererDispatcher blockRenderDispatcher;

    /**
     * Set to true to keep the game loop running. Set to false by shutdown() to allow the game loop to exit cleanly.
     */
    volatile boolean running = true;

    /** String that shows the debug information */
    public String debug = "";
    public boolean field_175613_B = false;
    public boolean field_175614_C = false;
    public boolean field_175611_D = false;
    public boolean renderChunksMany = true;

    /** Approximate time (in ms) of last update to debug string */
    long debugUpdateTime = getSystemTime();

    /** holds the current fps */
    int fpsCounter;
    long prevFrameTime = -1L;

    /** Profiler currently displayed in the debug screen pie chart */
    private String debugProfilerName = "root";
    private boolean headless;

    public ClientEngine(GameConfiguration gameConfig, boolean headless)
    {
        this.headless = headless;
        this.mcDataDir = gameConfig.folderInfo.mcDataDir;
        this.fileAssets = gameConfig.folderInfo.assetsDir;
        this.fileResourcepacks = gameConfig.folderInfo.resourcePacksDir;
        this.launchedVersion = gameConfig.gameInfo.version;
        this.twitchDetails = gameConfig.userInfo.userProperties;
        this.field_181038_N = gameConfig.userInfo.field_181172_c;
        this.mcDefaultResourcePack = new DefaultResourcePack((new ResourceIndex(gameConfig.folderInfo.assetsDir, gameConfig.folderInfo.assetIndex)).getResourceMap());
        this.proxy = gameConfig.userInfo.proxy == null ? Proxy.NO_PROXY : gameConfig.userInfo.proxy;
        this.sessionService = (new YggdrasilAuthenticationService(gameConfig.userInfo.proxy, UUID.randomUUID().toString())).createMinecraftSessionService();
        this.session = gameConfig.userInfo.session;
        logger.info("Booting up as " + this.session.name + ".");
        this.isDemo = gameConfig.gameInfo.isDemo;
        this.displayWidth = gameConfig.displayInfo.width > 0 ? gameConfig.displayInfo.width : 1;
        this.displayHeight = gameConfig.displayInfo.height > 0 ? gameConfig.displayInfo.height : 1;
        this.tempDisplayWidth = gameConfig.displayInfo.width;
        this.tempDisplayHeight = gameConfig.displayInfo.height;
        this.fullscreen = gameConfig.displayInfo.fullscreen;
        this.jvm64bit = isJvm64bit();
        this.theIntegratedServer = new IntegratedServer(this);

        if (gameConfig.serverInfo.serverName != null)
        {
            this.serverName = gameConfig.serverInfo.serverName;
            this.serverPort = gameConfig.serverInfo.serverPort;
        }

        ImageIO.setUseCache(false);
        Bootstrap.register();
    }

    public void run()
    {
        this.running = true;

        try
        {
            this.startGame();
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Initializing game");
            crashreport.makeCategory("Initialization");
            this.displayCrashReport(this.addGraphicsAndWorldToCrashReport(crashreport));
            return;
        }

        try
        {
            while (this.running)
            {
                if (!this.hasCrashed || this.crashReporter == null)
                {
                    try
                    {
                        this.runGameLoop();
                    }
                    catch (OutOfMemoryError var10)
                    {
                        this.freeMemory();
                        this.displayGuiScreen(new GuiMemoryErrorScreen());
                    }
                }
                else
                {
                    this.displayCrashReport(this.crashReporter);
                }
            }
        }
        catch (MinecraftError var12)
        {
        }

        catch (ReportedException reportedexception)
        {
            this.addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
            this.freeMemory();
            logger.error((String)"Reported exception thrown!", (Throwable)reportedexception);
            this.displayCrashReport(reportedexception.getCrashReport());
        }
        catch (Throwable throwable1)
        {
            CrashReport crashreport1 = this.addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
            this.freeMemory();
            logger.error("Unreported exception thrown!", throwable1);
            this.displayCrashReport(crashreport1);
        }

        finally
        {
            this.shutdownMinecraftApplet();
        }
    }

    /**
     * Starts the game: initializes the canvas, the title, the settings, etcetera.
     */
    private void startGame() throws LWJGLException, IOException
    {
        Config.get().setMinecraft(this);
        this.options = new GameOptions(this, this.mcDataDir);
        this.defaultResourcePacks.add(this.mcDefaultResourcePack);
        this.startTimerHackThread();

        if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0)
        {
            this.displayWidth = this.options.overrideWidth;
            this.displayHeight = this.options.overrideHeight;
        }

        this.setWindowIcon();
        this.setInitialDisplayMode();
        this.createDisplay();
        OpenGlHelper.init();
        this.framebufferMc = new Framebuffer(this.displayWidth, this.displayHeight, true);
        this.framebufferMc.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.registerMetadataSerializers();
        this.mcResourcePackRepository = new ResourcePackRepository(this.fileResourcepacks, new File(this.mcDataDir, "server-resource-packs"), this.mcDefaultResourcePack, this.metadataSerializer_, this.options, this);
        this.mcResourceManager = new SimpleReloadableResourceManager(this.metadataSerializer_);
        this.mcLanguageManager = new LanguageManager(this.metadataSerializer_, this.options.language);
        this.mcResourceManager.registerReloadListener(this.mcLanguageManager);
        ScaledResolution.init(this);
        this.refreshResources();
        this.renderEngine = new TextureManager(this.mcResourceManager);
        this.mcResourceManager.registerReloadListener(this.renderEngine);
        this.drawSplashScreen(this.renderEngine);
        this.skinManager = new SkinManager(this.renderEngine, new File(this.fileAssets, "skins"), this.sessionService, this);
        this.saveLoader = new AnvilSaveConverter(new File(this.mcDataDir, "saves"));
        this.fontRendererObj = new FontRenderer(this.options, new ResourceLocation("textures/font/ascii.png"), this.renderEngine, false, this);

        if (this.options.language != null)
        {
            this.fontRendererObj.setUnicodeFlag(this.isUnicode());
            this.fontRendererObj.setBidiFlag(this.mcLanguageManager.isCurrentLanguageBidirectional());
        }

        this.standardGalacticFontRenderer = new FontRenderer(this.options, new ResourceLocation("textures/font/ascii_sga.png"), this.renderEngine, false, this);
        this.mcResourceManager.registerReloadListener(this.fontRendererObj);
        this.mcResourceManager.registerReloadListener(this.standardGalacticFontRenderer);
        this.mcResourceManager.registerReloadListener(new GrassColorReloadListener());
        this.mcResourceManager.registerReloadListener(new FoliageColorReloadListener());
        AchievementList.openInventory.setStatStringFormatter(new IStatStringFormat()
        {
            public String formatString(String p_74535_1_)
            {
                try
                {
                    return String.format(p_74535_1_, new Object[] {GameOptions.getKeyDisplayString(ClientEngine.this.options.keyBindInventory.getKeyCode())});
                }
                catch (Exception exception)
                {
                    return "Error: " + exception.getLocalizedMessage();
                }
            }
        });
        this.mouseHelper = new MouseHelper();
        GlStateManager.get().enableTexture2D();
        GlStateManager.get().shadeModel(7425);
        GlStateManager.get().clearDepth(1.0D);
        GlStateManager.get().enableDepth();
        GlStateManager.get().depthFunc(515);
        GlStateManager.get().enableAlpha();
        GlStateManager.get().alphaFunc(516, 0.0F);
        GlStateManager.get().cullFace(1029);
        GlStateManager.get().matrixMode(5889);
        GlStateManager.get().loadIdentity();
        GlStateManager.get().matrixMode(5888);
        this.textureMapBlocks = new TextureMap("textures", this);
        this.textureMapBlocks.setMipmapLevels(this.options.mipmapLevels);
        this.renderEngine.loadTickableTexture(TextureMap.locationBlocksTexture, this.textureMapBlocks);
        this.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        this.textureMapBlocks.setBlurMipmapDirect(false, this.options.mipmapLevels > 0);
        this.modelManager = new ModelManager(this.textureMapBlocks, this);
        if (!isHeadless()) this.mcResourceManager.registerReloadListener(this.modelManager);
        this.renderItem = new RenderItem(this.renderEngine, this.modelManager, this);
        this.renderManager = new RenderManager(this.renderEngine, this.renderItem, this);
        this.itemRenderer = new ItemRenderer(this);
        this.mcResourceManager.registerReloadListener(this.renderItem);
        this.entityRenderer = new EntityRenderer(this, this.mcResourceManager);
        this.mcResourceManager.registerReloadListener(this.entityRenderer);
        this.blockRenderDispatcher = new BlockRendererDispatcher(this.modelManager.getBlockModelShapes(), this.options, this);
        this.mcResourceManager.registerReloadListener(this.blockRenderDispatcher);
        this.renderGlobal = new RenderGlobal(this);
        this.mcResourceManager.registerReloadListener(this.renderGlobal);
        this.guiAchievement = new GuiAchievement(this);
        GlStateManager.get().viewport(0, 0, this.displayWidth, this.displayHeight);
        this.effectRenderer = new EffectRenderer(this.world, this.renderEngine);
        this.ingameGUI = new GuiIngame(this);

        if (this.serverName != null)
        {
            this.displayGuiScreen(new GuiConnecting(new GuiMainMenu(), this, new ServerData(this.serverName, this.serverName, false)));
        }
        else
        {
            this.displayGuiScreen(new GuiMainMenu());
        }

        this.renderEngine.deleteTexture(this.mojangLogo);
        this.mojangLogo = null;
        this.loadingScreen = new LoadingScreenRenderer(this);

        if (this.options.fullScreen && !this.fullscreen)
        {
            this.toggleFullscreen();
        }

        Display.get().setVSyncEnabled(true);
        this.renderGlobal.makeEntityOutlineShader();
    }

    private void registerMetadataSerializers()
    {
        this.metadataSerializer_.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
        this.metadataSerializer_.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
    }

    private void createDisplay() throws LWJGLException
    {
        Display.get().setResizable(true);
        Display.get().setTitle("Minecraft View");

        try
        {
            Display.get().create(new PixelFormat().withDepthBits(24), isHeadless());
        }
        catch (LWJGLException lwjglexception)
        {
            logger.error((String)"Couldn\'t set pixel format", (Throwable)lwjglexception);

            try
            {
                Thread.sleep(1000L);
            }
            catch (InterruptedException var3)
            {
                ;
            }

            if (this.fullscreen)
            {
                this.updateDisplayMode();
            }

            Display.get().create(isHeadless());
        }
    }

    private void setInitialDisplayMode() throws LWJGLException
    {
        if (this.fullscreen)
        {
            Display.get().setFullscreen(true);
            DisplayMode displaymode = Display.get().getDisplayMode();
            this.displayWidth = Math.max(1, displaymode.getWidth());
            this.displayHeight = Math.max(1, displaymode.getHeight());
        }
        else
        {
            Display.get().setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
        }
    }

    private void setWindowIcon()
    {
    }

    private static boolean isJvm64bit()
    {
        String[] astring = new String[] {"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

        for (String s : astring)
        {
            String s1 = System.getProperty(s);

            if (s1 != null && s1.contains("64"))
            {
                return true;
            }
        }

        return false;
    }

    public Framebuffer getFramebuffer()
    {
        return this.framebufferMc;
    }

    public String getVersion()
    {
        return this.launchedVersion;
    }

    private void startTimerHackThread()
    {
        Thread thread = new Thread("Timer hack thread")
        {
            public void run()
            {
                while (ClientEngine.this.running)
                {
                    try
                    {
                        Thread.sleep(2147483647L);
                    }
                    catch (InterruptedException var2)
                    {
                        ;
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void crashed(CrashReport crash)
    {
        this.hasCrashed = true;
        this.crashReporter = crash;
    }

    /**
     * Wrapper around displayCrashReportInternal
     */
    public void displayCrashReport(CrashReport crashReportIn)
    {
        Bootstrap.printToSYSOUT(crashReportIn.getCompleteReport());
    }

    public boolean isUnicode()
    {
        return this.mcLanguageManager.isCurrentLocaleUnicode() || this.options.forceUnicodeFont;
    }

    public void refreshResources()
    {
        List<IResourcePack> list = Lists.newArrayList(this.defaultResourcePacks);

        try
        {
            this.mcResourceManager.reloadResources(list);
        }
        catch (RuntimeException runtimeexception)
        {
            list.clear();
            list.addAll(this.defaultResourcePacks);
            this.mcResourcePackRepository.setRepositories(Collections.<ResourcePackRepository.Entry>emptyList());
            this.mcResourceManager.reloadResources(list);
            this.options.resourcePacks.clear();
            this.options.field_183018_l.clear();
            this.options.saveOptions();
        }

        this.mcLanguageManager.parseLanguageMetadata(list);

        if (this.renderGlobal != null)
        {
            this.renderGlobal.loadRenderers();
        }
    }

    private ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException
    {
        BufferedImage bufferedimage = ImageIO.read(imageStream);
        int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), (int[])null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);

        for (int i : aint)
        {
            bytebuffer.putInt(i << 8 | i >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }

    private void updateDisplayMode() throws LWJGLException
    {
        Set<DisplayMode> set = Sets.<DisplayMode>newHashSet();
        Collections.addAll(set, Display.get().getAvailableDisplayModes());
        DisplayMode displaymode = Display.get().getDesktopDisplayMode();

        if (!set.contains(displaymode) && Util.getOSType() == Util.EnumOS.OSX)
        {
            label53:

            for (DisplayMode displaymode1 : macDisplayModes)
            {
                boolean flag = true;

                for (DisplayMode displaymode2 : set)
                {
                    if (displaymode2.getBitsPerPixel() == 32 && displaymode2.getWidth() == displaymode1.getWidth() && displaymode2.getHeight() == displaymode1.getHeight())
                    {
                        flag = false;
                        break;
                    }
                }

                if (!flag)
                {
                    Iterator iterator = set.iterator();
                    DisplayMode displaymode3;

                    while (true)
                    {
                        if (!iterator.hasNext())
                        {
                            continue label53;
                        }

                        displaymode3 = (DisplayMode)iterator.next();

                        if (displaymode3.getBitsPerPixel() == 32 && displaymode3.getWidth() == displaymode1.getWidth() / 2 && displaymode3.getHeight() == displaymode1.getHeight() / 2)
                        {
                            break;
                        }
                    }

                    displaymode = displaymode3;
                }
            }
        }

        Display.get().setDisplayMode(displaymode);
        this.displayWidth = displaymode.getWidth();
        this.displayHeight = displaymode.getHeight();
    }

    private void drawSplashScreen(TextureManager textureManagerInstance) throws LWJGLException
    {
        ScaledResolution scaledresolution = ScaledResolution.get();
        int i = scaledresolution.getScaleFactor();
        Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i, true);
        framebuffer.bindFramebuffer(false);
        GlStateManager.get().matrixMode(5889);
        GlStateManager.get().loadIdentity();
        GlStateManager.get().ortho(0.0D, (double)scaledresolution.getScaledWidth(), (double)scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.get().matrixMode(5888);
        GlStateManager.get().loadIdentity();
        GlStateManager.get().translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.get().disableLighting();
        GlStateManager.get().disableFog();
        GlStateManager.get().disableDepth();
        GlStateManager.get().enableTexture2D();
        InputStream inputstream = null;

        try
        {
            inputstream = this.mcDefaultResourcePack.getInputStream(locationMojangPng);
            this.mojangLogo = textureManagerInstance.getDynamicTextureLocation("logo", new DynamicTexture(ImageIO.read(inputstream)));
            textureManagerInstance.bindTexture(this.mojangLogo);
        }
        catch (IOException ioexception)
        {
            logger.error((String)("Unable to load logo: " + locationMojangPng), (Throwable)ioexception);
        }
        finally
        {
            IOUtils.closeQuietly(inputstream);
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0D, (double)this.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos((double)this.displayWidth, (double)this.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos((double)this.displayWidth, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        GlStateManager.get().color(1.0F, 1.0F, 1.0F, 1.0F);
        int j = 256;
        int k = 256;
        this.func_181536_a((scaledresolution.getScaledWidth() - j) / 2, (scaledresolution.getScaledHeight() - k) / 2, 0, 0, j, k, 255, 255, 255, 255);
        GlStateManager.get().disableLighting();
        GlStateManager.get().disableFog();
        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i);
        GlStateManager.get().enableAlpha();
        GlStateManager.get().alphaFunc(516, 0.0F);
        this.updateDisplay();
    }

    public void func_181536_a(int p_181536_1_, int p_181536_2_, int p_181536_3_, int p_181536_4_, int p_181536_5_, int p_181536_6_, int p_181536_7_, int p_181536_8_, int p_181536_9_, int p_181536_10_)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos((double)p_181536_1_, (double)(p_181536_2_ + p_181536_6_), 0.0D).tex((double)((float)p_181536_3_ * f), (double)((float)(p_181536_4_ + p_181536_6_) * f1)).color(p_181536_7_, p_181536_8_, p_181536_9_, p_181536_10_).endVertex();
        worldrenderer.pos((double)(p_181536_1_ + p_181536_5_), (double)(p_181536_2_ + p_181536_6_), 0.0D).tex((double)((float)(p_181536_3_ + p_181536_5_) * f), (double)((float)(p_181536_4_ + p_181536_6_) * f1)).color(p_181536_7_, p_181536_8_, p_181536_9_, p_181536_10_).endVertex();
        worldrenderer.pos((double)(p_181536_1_ + p_181536_5_), (double)p_181536_2_, 0.0D).tex((double)((float)(p_181536_3_ + p_181536_5_) * f), (double)((float)p_181536_4_ * f1)).color(p_181536_7_, p_181536_8_, p_181536_9_, p_181536_10_).endVertex();
        worldrenderer.pos((double)p_181536_1_, (double)p_181536_2_, 0.0D).tex((double)((float)p_181536_3_ * f), (double)((float)p_181536_4_ * f1)).color(p_181536_7_, p_181536_8_, p_181536_9_, p_181536_10_).endVertex();
        Tessellator.getInstance().draw();
    }

    /**
     * Returns the save loader that is currently being used
     */
    public ISaveFormat getSaveLoader()
    {
        return this.saveLoader;
    }

    /**
     * Sets the argument GuiScreen as the main (topmost visible) screen.
     */
    public void displayGuiScreen(GuiScreen guiScreenIn)
    {
        if (guiScreenIn != null)
        {
            guiScreenIn.mc = this;
            guiScreenIn.fontRendererObj = fontRendererObj;
        }

        if (this.currentScreen != null)
        {
            this.currentScreen.onGuiClosed();
        }

        if (guiScreenIn == null && this.world == null)
        {
            guiScreenIn = new GuiMainMenu();
        }
        else if (guiScreenIn == null && this.player.getHealth() <= 0.0F)
        {
            guiScreenIn = new GuiGameOver();
        }

        if (guiScreenIn instanceof GuiMainMenu)
        {
            this.options.showDebugInfo = false;
            this.ingameGUI.getChatGUI().clearChatMessages();
        }

        this.currentScreen = (GuiScreen)guiScreenIn;

        if (guiScreenIn != null)
        {
            this.setIngameNotInFocus();
            ScaledResolution scaledresolution = ScaledResolution.get();
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            ((GuiScreen)guiScreenIn).setWorldAndResolution(this, i, j);
            this.skipRenderWorld = false;
        }
        else
        {
            this.setIngameFocus();
        }
    }

    /**
     * Shuts down the minecraft applet by stopping the resource downloads, and clearing up GL stuff; called when the
     * application (or web page) is exited.
     */
    public void shutdownMinecraftApplet()
    {
        try
        {
            logger.info("Shutting down.");

            try
            {
                this.loadWorld((WorldClient)null);
            }
            catch (Throwable var5)
            {
                ;
            }
        }

        finally
        {
            Display.get().destroy();
            MineBot.instances--;
            
            if (MineBot.instances == 0)
            {
                System.exit(0);
            }
        }
    }

    /**
     * Called repeatedly from run()
     */
    private void runGameLoop() throws IOException
    {
        long i = System.nanoTime();

        if (disconnect && getCurrentServerData() != null)
        {
            world.sendQuittingDisconnectingPacket();
            loadWorld(null);
            stopIntegratedServer();
            disconnect = false;
        }

        if ((Display.get().isCreated() && Display.get().isCloseRequested()) || exitGame)
        {
            this.shutdown();
            return;
        }

        if (gameLoop != null)
        {
            CompletableFuture.runAsync(() -> gameLoop.run());
        }

        if (this.isGamePaused && this.world != null)
        {
            float f = this.timer.renderPartialTicks;
            this.timer.updateTimer();
            this.timer.renderPartialTicks = f;
        }
        else
        {
            this.timer.updateTimer();
        }

        synchronized (this.scheduledTasks)
        {
            while (!this.scheduledTasks.isEmpty())
            {
                Util.func_181617_a((FutureTask)this.scheduledTasks.poll(), logger);
            }
        }

        long l = System.nanoTime();

        for (int j = 0; j < this.timer.elapsedTicks; ++j)
        {
            this.runTick();
        }

        long i1 = System.nanoTime() - l;
        GlStateManager.get().pushMatrix();
        GlStateManager.get().clear(16640);
        this.framebufferMc.bindFramebuffer(true);
        GlStateManager.get().enableTexture2D();

        if (this.player != null && this.player.isEntityInsideOpaqueBlock())
        {
            this.options.thirdPersonView = 0;
        }

        if (!this.skipRenderWorld && !headless)
        {
            this.entityRenderer.updateCameraAndRender(this.timer.renderPartialTicks, i);
        }

        this.prevFrameTime = System.nanoTime();
        if (!isHeadless()) this.guiAchievement.updateAchievementWindow();
        this.framebufferMc.unbindFramebuffer();
        GlStateManager.get().popMatrix();
        GlStateManager.get().pushMatrix();
        this.framebufferMc.framebufferRender(this.displayWidth, this.displayHeight);
        GlStateManager.get().popMatrix();
        this.updateDisplay();
        Thread.yield();
        ++this.fpsCounter;
        this.isGamePaused = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.theIntegratedServer.getPublic();
        long k = System.nanoTime();
        this.field_181542_y.func_181747_a(k - this.field_181543_z);
        this.field_181543_z = k;

        while (getSystemTime() >= this.debugUpdateTime + 1000L)
        {
            debugFPS = this.fpsCounter;
            this.debug = String.format("%d fps (%d chunk update%s) T: %s%s%s", new Object[] {Integer.valueOf(debugFPS), Integer.valueOf(RenderChunk.renderChunksUpdated), RenderChunk.renderChunksUpdated != 1 ? "s" : "", this.options.fancyGraphics ? "" : " fast", this.options.clouds == 0 ? "" : (this.options.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : ""});
            RenderChunk.renderChunksUpdated = 0;
            this.debugUpdateTime += 1000L;
            this.fpsCounter = 0;
        }

        if (this.world == null && !Display.get().isActive())
        {
            GL11.glFinish();
        }
    }

    public void updateDisplay()
    {
        Display.get().update();
        this.checkWindowResize();
    }

    protected void checkWindowResize()
    {
        if (!this.fullscreen && Display.get().wasResized())
        {
            int i = this.displayWidth;
            int j = this.displayHeight;
            this.displayWidth = Display.get().getWidth();
            this.displayHeight = Display.get().getHeight();

            if (this.displayWidth != i || this.displayHeight != j)
            {
                if (this.displayWidth <= 0)
                {
                    this.displayWidth = 1;
                }

                if (this.displayHeight <= 0)
                {
                    this.displayHeight = 1;
                }

                this.resize(this.displayWidth, this.displayHeight);
            }
        }
    }

    public void freeMemory()
    {
        try
        {
            this.renderGlobal.deleteAllDisplayLists();
        }
        catch (Throwable var3)
        {
            ;
        }

        try
        {
            this.loadWorld((WorldClient)null);
        }
        catch (Throwable var2)
        {
            ;
        }
    }
    
    /**
     * Called when the window is closing. Sets 'running' to false which allows the game loop to exit cleanly.
     */
    public void shutdown()
    {
        this.running = false;
    }

    /**
     * Will set the focus to ingame if the Minecraft window is the active with focus. Also clears any GUI screen
     * currently displayed
     */
    public void setIngameFocus()
    {
        if (Display.get().isActive())
        {
            if (!this.inGameHasFocus)
            {
                this.inGameHasFocus = true;
                this.mouseHelper.grabMouseCursor();
                this.displayGuiScreen((GuiScreen)null);
                this.updateKeyBindState();
                this.leftClickCounter = 10000;
            }
        }
    }

    /**
     * Resets the player keystate, disables the ingame focus, and ungrabs the mouse cursor.
     */
    public void setIngameNotInFocus()
    {
        if (this.inGameHasFocus)
        {
            KeyBinding.unPressAllKeys();
            this.inGameHasFocus = false;
            this.mouseHelper.ungrabMouseCursor();
        }
    }

    /**
     * Displays the ingame menu
     */
    public void displayInGameMenu()
    {
        if (this.currentScreen == null)
        {
            this.displayGuiScreen(new GuiIngameMenu());
        }
    }

    private void sendClickBlockToController(boolean leftClick)
    {
        if (!leftClick)
        {
            this.leftClickCounter = 0;
        }

        if (this.leftClickCounter <= 0 && leftClick && this.hitResult != null && this.hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            BlockPos blockpos = this.hitResult.getBlockPos();

            if (this.world.getBlockState(blockpos).getBlock().getMaterial() != Material.air)
            {
                if (!this.player.isUsingItem())
                {
                    if (this.playerController.onPlayerDamageBlock(blockpos, this.hitResult.sideHit))
                    {
                        this.effectRenderer.addBlockHitEffects(blockpos, this.hitResult.sideHit, this);
                        this.player.swingItem();
                    }
                }
            }
        }
        else
        {
            this.playerController.resetBlockRemoving();
        }
    }

    private void clickMouse()
    {
        if (this.leftClickCounter <= 0)
        {
            this.player.swingItem();
            
            if (this.hitResult != null)
            {
                switch (this.hitResult.typeOfHit)
                {
                    case ENTITY:
                        this.playerController.attackEntity(this.player, this.hitResult.entityHit);
                        break;

                    case BLOCK:
                        BlockPos blockpos = this.hitResult.getBlockPos();

                        if (this.world.getBlockState(blockpos).getBlock().getMaterial() != Material.air)
                        {
                            this.playerController.clickBlock(blockpos, this.hitResult.sideHit);
                            break;
                        }

                    case MISS:
                    default: {}
                }
            }
        }
    }

    /**
     * Called when user clicks their place button
     */
    private void rightClickMouse()
    {
        if (!this.playerController.hittingBlock())
        {
            this.rightClickDelayTimer = 4;
            boolean flag = true;
            ItemStack itemstack = this.player.inventory.getCurrentItem();

            if (this.hitResult == null)
            {
                logger.warn("Null returned as \'hitResult\', this shouldn\'t happen!");
            }
            else
            {
                switch (this.hitResult.typeOfHit)
                {
                    case ENTITY:
                        if (this.playerController.interactAt(this.player, this.hitResult.entityHit, this.hitResult))
                        {
                            flag = false;
                        }
                        else if (this.playerController.interactWithEntitySendPacket(this.player, this.hitResult.entityHit))
                        {
                            flag = false;
                        }

                        break;

                    case BLOCK:
                        BlockPos blockpos = this.hitResult.getBlockPos();

                        if (this.world.getBlockState(blockpos).getBlock().getMaterial() != Material.air)
                        {
                            int i = itemstack != null ? itemstack.stackSize : 0;

                            if (this.playerController.onPlayerRightClick(this.player, this.world, itemstack, blockpos, this.hitResult.sideHit, this.hitResult.hitVec))
                            {
                                flag = false;
                                this.player.swingItem();
                            }

                            if (itemstack == null)
                            {
                                return;
                            }

                            if (itemstack.stackSize == 0)
                            {
                                this.player.inventory.mainInventory[this.player.inventory.currentItem] = null;
                            }
                            else if (itemstack.stackSize != i || this.playerController.isInCreativeMode())
                            {
                                this.entityRenderer.itemRenderer.resetEquippedProgress();
                            }
                        }

                        break;

                    default: {}
                }
            }

            if (flag)
            {
                ItemStack itemstack1 = this.player.inventory.getCurrentItem();

                if (itemstack1 != null && this.playerController.sendUseItem(this.player, this.world, itemstack1))
                {
                    this.entityRenderer.itemRenderer.resetEquippedProgress2();
                }
            }
        }
    }

    /**
     * Toggles fullscreen mode.
     */
    public void toggleFullscreen()
    {
        try
        {
            this.fullscreen = !this.fullscreen;
            this.options.fullScreen = this.fullscreen;

            if (this.fullscreen)
            {
                this.updateDisplayMode();
                this.displayWidth = Display.get().getDisplayMode().getWidth();
                this.displayHeight = Display.get().getDisplayMode().getHeight();

                if (this.displayWidth <= 0)
                {
                    this.displayWidth = 1;
                }

                if (this.displayHeight <= 0)
                {
                    this.displayHeight = 1;
                }
            }
            else
            {
                Display.get().setDisplayMode(new DisplayMode(this.tempDisplayWidth, this.tempDisplayHeight));
                this.displayWidth = this.tempDisplayWidth;
                this.displayHeight = this.tempDisplayHeight;

                if (this.displayWidth <= 0)
                {
                    this.displayWidth = 1;
                }

                if (this.displayHeight <= 0)
                {
                    this.displayHeight = 1;
                }
            }

            if (this.currentScreen != null)
            {
                this.resize(this.displayWidth, this.displayHeight);
            }
            else
            {
                this.updateFramebufferSize();
            }

            Display.get().setFullscreen(this.fullscreen);
            Display.get().setVSyncEnabled(true);
            this.updateDisplay();
        }
        catch (Exception exception)
        {
            logger.error((String)"Couldn\'t toggle fullscreen", (Throwable)exception);
        }

        ScaledResolution.get().update(this);
    }

    /**
     * Called to resize the current screen.
     */
    private void resize(int width, int height)
    {
        this.displayWidth = Math.max(1, width);
        this.displayHeight = Math.max(1, height);
        ScaledResolution.get().update(this);

        if (this.currentScreen != null)
        {
            ScaledResolution scaledresolution = ScaledResolution.get();
            this.currentScreen.onResize(this, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
        }

        this.loadingScreen = new LoadingScreenRenderer(this);
        this.updateFramebufferSize();
    }

    private void updateFramebufferSize()
    {
        this.framebufferMc.createBindFramebuffer(this.displayWidth, this.displayHeight);

        if (this.entityRenderer != null)
        {
            this.entityRenderer.updateShaderGroupSize(this.displayWidth, this.displayHeight);
        }
    }

    /**
     * Runs the current tick.
     */
    public void runTick() throws IOException
    {
        if (this.rightClickDelayTimer > 0)
        {
            --this.rightClickDelayTimer;
        }

        if (!this.isGamePaused)
        {
            this.ingameGUI.updateTick();
        }

        this.entityRenderer.getMouseOver(1.0F);

        if (!this.isGamePaused && this.world != null)
        {
            this.playerController.updateController();
        }

        if (!this.isGamePaused)
        {
            this.renderEngine.tick();
        }

        if (this.currentScreen == null && this.player != null)
        {
            if (this.player.getHealth() <= 0.0F)
            {
                this.displayGuiScreen((GuiScreen)null);
            }
            else if (this.player.isPlayerSleeping() && this.world != null)
            {
                this.displayGuiScreen(new GuiSleepMP());
            }
        }
        else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.player.isPlayerSleeping())
        {
            this.displayGuiScreen((GuiScreen)null);
        }

        if (this.currentScreen != null)
        {
            this.leftClickCounter = 10000;
        }

        if (this.currentScreen != null)
        {
            try
            {
                this.currentScreen.handleInput();
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Affected screen");
                crashreportcategory.addCrashSectionCallable("Screen name", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return ClientEngine.this.currentScreen.getClass().getCanonicalName();
                    }
                });
                throw new ReportedException(crashreport);
            }

            if (this.currentScreen != null)
            {
                try
                {
                    this.currentScreen.updateScreen();
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
                    crashreportcategory1.addCrashSectionCallable("Screen name", new Callable<String>()
                    {
                        public String call() throws Exception
                        {
                            return ClientEngine.this.currentScreen.getClass().getCanonicalName();
                        }
                    });
                    throw new ReportedException(crashreport1);
                }
            }
        }

        if (this.currentScreen == null || this.currentScreen.allowUserInput)
        {
            while (Mouse.get().next())
            {
                int i = Mouse.get().getEventButton();
                KeyBinding.setKeyBindState(i - 100, Mouse.get().getEventButtonState());

                if (Mouse.get().getEventButtonState())
                {
                    if (this.player.isSpectator() && i == 2)
                    {
                        this.ingameGUI.getSpectatorGui().func_175261_b();
                    }
                    else
                    {
                        KeyBinding.onTick(i - 100);
                    }
                }

                long i1 = getSystemTime() - this.systemTime;

                if (i1 <= 200L)
                {
                    int j = Mouse.get().getEventDWheel();

//                    if (j != 0)
//                    {
//                        if (this.player.isSpectator())
//                        {
//                            j = j < 0 ? -1 : 1;
//
//                            if (this.ingameGUI.getSpectatorGui().func_175262_a())
//                            {
//                                this.ingameGUI.getSpectatorGui().func_175259_b(-j);
//                            }
//                            else
//                            {
//                                float f = MathHelper.clamp_float(this.player.capabilities.getFlySpeed() + (float)j * 0.005F, 0.0F, 0.2F);
//                                this.player.capabilities.setFlySpeed(f);
//                            }
//                        }
//                        else
//                        {
//                            this.player.inventory.changeCurrentItem(j);
//                        }
//                    }

                    if (this.currentScreen == null)
                    {
                        if (!this.inGameHasFocus && Mouse.get().getEventButtonState())
                        {
                            this.setIngameFocus();
                        }
                    }
                    else if (this.currentScreen != null)
                    {
                        this.currentScreen.handleMouseInput();
                    }
                }
            }

            if (this.leftClickCounter > 0)
            {
                --this.leftClickCounter;
            }

            while (Keyboard.get().next())
            {
                int k = Keyboard.get().getEventKey() == 0 ? Keyboard.get().getEventCharacter() + 256 : Keyboard.get().getEventKey();
                
                if (k != options.keyBindForward.getKeyCode() && k != options.keyBindLeft.getKeyCode() && k != options.keyBindBack.getKeyCode() && k != options.keyBindRight.getKeyCode() && k != options.keyBindJump.getKeyCode() && k != options.keyBindSneak.getKeyCode())
                {
                    KeyBinding.setKeyBindState(k, Keyboard.get().getEventKeyState());
                }

                if (Keyboard.get().getEventKeyState())
                {
                    KeyBinding.onTick(k);
                }

                this.dispatchKeypresses();

                if (Keyboard.get().getEventKeyState())
                {
                    if (k == 62 && this.entityRenderer != null)
                    {
                        this.entityRenderer.switchUseShader();
                    }

                    if (this.currentScreen != null)
                    {
                        this.currentScreen.handleKeyboardInput();
                    }
                    else
                    {
                        if (k == 1)
                        {
                            this.displayInGameMenu();
                        }

                        if (k == 32 && Keyboard.get().isKeyDown(61) && this.ingameGUI != null)
                        {
                            this.ingameGUI.getChatGUI().clearChatMessages();
                        }

                        if (k == 31 && Keyboard.get().isKeyDown(61))
                        {
                            this.refreshResources();
                        }

                        if (k == 17 && Keyboard.get().isKeyDown(61))
                        {
                            ;
                        }

                        if (k == 18 && Keyboard.get().isKeyDown(61))
                        {
                            ;
                        }

                        if (k == 47 && Keyboard.get().isKeyDown(61))
                        {
                            ;
                        }

                        if (k == 38 && Keyboard.get().isKeyDown(61))
                        {
                            ;
                        }

                        if (k == 22 && Keyboard.get().isKeyDown(61))
                        {
                            ;
                        }

                        if (k == 20 && Keyboard.get().isKeyDown(61))
                        {
                            this.refreshResources();
                        }

                        if (k == 33 && Keyboard.get().isKeyDown(61))
                        {
                            this.options.setOptionValue(GameOptions.Options.RENDER_DISTANCE, GuiScreen.isShiftKeyDown() ? -1 : 1);
                        }

                        if (k == 30 && Keyboard.get().isKeyDown(61))
                        {
                            this.renderGlobal.loadRenderers();
                        }

                        if (k == 35 && Keyboard.get().isKeyDown(61))
                        {
                            this.options.advancedItemTooltips = !this.options.advancedItemTooltips;
                            this.options.saveOptions();
                        }

                        if (k == 48 && Keyboard.get().isKeyDown(61))
                        {
                            this.renderManager.setDebugBoundingBox(!this.renderManager.isDebugBoundingBox());
                        }

                        if (k == 25 && Keyboard.get().isKeyDown(61))
                        {
                            this.options.pauseOnLostFocus = !this.options.pauseOnLostFocus;
                            this.options.saveOptions();
                        }

                        if (k == 59)
                        {
                            this.options.hideGUI = !this.options.hideGUI;
                        }

                        if (k == 61)
                        {
                            this.options.showDebugInfo = !this.options.showDebugInfo;
                            this.options.showDebugProfilerChart = GuiScreen.isShiftKeyDown();
                            this.options.field_181657_aC = GuiScreen.isAltKeyDown();
                        }

                        if (this.options.keyBindTogglePerspective.isPressed())
                        {
                            ++this.options.thirdPersonView;

                            if (this.options.thirdPersonView > 2)
                            {
                                this.options.thirdPersonView = 0;
                            }

                            if (this.options.thirdPersonView == 0)
                            {
                                this.entityRenderer.loadEntityShader(this.getRenderViewEntity());
                            }
                            else if (this.options.thirdPersonView == 1)
                            {
                                this.entityRenderer.loadEntityShader((Entity)null);
                            }

                            this.renderGlobal.setDisplayListEntitiesDirty();
                        }

                        if (this.options.keyBindSmoothCamera.isPressed())
                        {
                            this.options.smoothCamera = !this.options.smoothCamera;
                        }
                    }
                }
            }

//            for (int l = 0; l < 9; ++l)
//            {
//                if (this.options.keyBindsHotbar[l].isPressed())
//                {
//                    if (this.player.isSpectator())
//                    {
//                        this.ingameGUI.getSpectatorGui().func_175260_a(l);
//                    }
//                    else
//                    {
//                        this.player.inventory.currentItem = l;
//                    }
//                }
//            }

            boolean flag = this.options.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

//            while (this.options.keyBindInventory.isPressed())
//            {
//                if (this.playerController.isRidingHorse())
//                {
//                    this.player.sendHorseInventory();
//                }
//                else
//                {
//                    this.getNetHandler().sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
//                    this.displayGuiScreen(new GuiInventory(this.player));
//                }
//            }

//            while (this.options.keyBindDrop.isPressed())
//            {
//                if (!this.player.isSpectator())
//                {
//                    this.player.dropOneItem(GuiScreen.isCtrlKeyDown());
//                }
//            }

            while (this.options.keyBindChat.isPressed() && flag)
            {
                this.displayGuiScreen(new GuiChat());
            }

            if (this.currentScreen == null && this.options.keyBindCommand.isPressed() && flag)
            {
                this.displayGuiScreen(new GuiChat("/"));
            }

            if (this.player.isUsingItem())
            {
                if (!this.options.keyBindUseItem.isKeyDown())
                {
                    this.playerController.onStoppedUsingItem(this.player);
                }

                while (this.options.keyBindAttack.isPressed())
                {
                    ;
                }

                while (this.options.keyBindUseItem.isPressed())
                {
                    ;
                }

                while (this.options.keyBindPickBlock.isPressed())
                {
                    ;
                }
            }
            else
            {
                while (this.options.keyBindAttack.isPressed())
                {
                    this.clickMouse();
                }

                while (this.options.keyBindUseItem.isPressed())
                {
                    this.rightClickMouse();
                }

                while (this.options.keyBindPickBlock.isPressed())
                {
                    this.middleClickMouse();
                }
            }

            if (this.options.keyBindUseItem.isKeyDown() && this.rightClickDelayTimer == 0 && !this.player.isUsingItem())
            {
                this.rightClickMouse();
            }

            this.sendClickBlockToController(this.currentScreen == null && this.options.keyBindAttack.isKeyDown() && this.inGameHasFocus);
        }

        if (this.world != null)
        {
            if (this.player != null)
            {
                ++this.joinPlayerCounter;

                if (this.joinPlayerCounter == 30)
                {
                    this.joinPlayerCounter = 0;
                    this.world.joinEntityInSurroundings(this.player);
                }
            }

            if (!this.isGamePaused)
            {
                this.entityRenderer.updateRenderer();
            }

            if (!this.isGamePaused)
            {
                this.renderGlobal.updateClouds();
            }

            if (!this.isGamePaused)
            {
                if (this.world.getLastLightningBolt() > 0)
                {
                    this.world.setLastLightningBolt(this.world.getLastLightningBolt() - 1);
                }

                this.world.updateEntities();
            }
        }
        else if (this.entityRenderer.isShaderActive())
        {
            this.entityRenderer.func_181022_b();
        }

        if (this.world != null)
        {
            if (!this.isGamePaused)
            {
                this.world.setAllowedSpawnTypes(this.world.getDifficulty() != EnumDifficulty.PEACEFUL, true);

                try
                {
                    this.world.tick();
                }
                catch (Throwable throwable2)
                {
                    CrashReport crashreport2 = CrashReport.makeCrashReport(throwable2, "Exception in world tick");

                    if (this.world == null)
                    {
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Affected level");
                        crashreportcategory2.addCrashSection("Problem", "Level is null!");
                    }
                    else
                    {
                        this.world.addWorldInfoToCrashReport(crashreport2);
                    }

                    throw new ReportedException(crashreport2);
                }
            }

            if (!this.isGamePaused && this.world != null)
            {
                this.world.doVoidFogParticles(MathHelper.floor_double(this.player.posX), MathHelper.floor_double(this.player.posY), MathHelper.floor_double(this.player.posZ));
            }

            if (!this.isGamePaused)
            {
                this.effectRenderer.updateEffects();
            }
        }
        else if (this.myNetworkManager != null)
        {
            this.myNetworkManager.processReceivedPackets();
        }

        this.systemTime = getSystemTime();
    }

    /**
     * Arguments: World foldername,  World ingame name, WorldSettings
     */
    public void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettingsIn)
    {
        this.loadWorld((WorldClient)null);
        ISaveHandler isavehandler = this.saveLoader.getSaveLoader(folderName, false);
        WorldInfo worldinfo = isavehandler.loadWorldInfo();

        if (worldinfo == null && worldSettingsIn != null)
        {
            worldinfo = new WorldInfo(worldSettingsIn, folderName);
            isavehandler.saveWorldInfo(worldinfo);
        }

        if (worldSettingsIn == null)
        {
            worldSettingsIn = new WorldSettings(worldinfo);
        }

        try
        {
            this.theIntegratedServer = new IntegratedServer(this, folderName, worldName, worldSettingsIn);
            this.theIntegratedServer.startServerThread();
            this.integratedServerIsRunning = true;
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Starting integrated server");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Starting integrated server");
            crashreportcategory.addCrashSection("Level ID", folderName);
            crashreportcategory.addCrashSection("Level Name", worldName);
            throw new ReportedException(crashreport);
        }

        this.loadingScreen.displaySavingString(I18n.format("menu.loadingLevel", new Object[0]));

        while (!this.theIntegratedServer.serverIsInRunLoop())
        {
            String s = this.theIntegratedServer.getUserMessage();

            if (s != null)
            {
                this.loadingScreen.displayLoadingString(I18n.format(s, new Object[0]));
            }
            else
            {
                this.loadingScreen.displayLoadingString("");
            }

            try
            {
                Thread.sleep(200L);
            }
            catch (InterruptedException var9)
            {
                ;
            }
        }

        this.displayGuiScreen((GuiScreen)null);
        SocketAddress socketaddress = this.theIntegratedServer.getNetworkSystem().addLocalEndpoint();
        NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress, this);
        networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, this, (GuiScreen)null));
        networkmanager.sendPacket(new C00Handshake(47, socketaddress.toString(), 0, EnumConnectionState.LOGIN));
        networkmanager.sendPacket(new C00PacketLoginStart(this.getSession().profile()));
        this.myNetworkManager = networkmanager;
        
        if (joinEvent != null)
        {
            CompletableFuture.runAsync(() -> joinEvent.run());
        }
    }

    /**
     * unloads the current world first
     */
    public void loadWorld(WorldClient worldClientIn)
    {
        this.loadWorld(worldClientIn, "");
    }

    /**
     * par2Str is displayed on the loading screen to the user unloads the current world first
     */
    public void loadWorld(WorldClient worldClientIn, String loadingMessage)
    {
        if (worldClientIn == null)
        {
            NetHandlerPlayClient nethandlerplayclient = this.getNetHandler();

            if (nethandlerplayclient != null)
            {
                nethandlerplayclient.cleanup();
            }

            if (this.theIntegratedServer != null && this.theIntegratedServer.isAnvilFileSet())
            {
                this.theIntegratedServer.initiateShutdown();
                this.theIntegratedServer.setStaticInstance();
            }

            this.theIntegratedServer = null;
            this.guiAchievement.clearAchievements();
            this.entityRenderer.getMapItemRenderer().clearLoadedMaps();
        }

        this.renderViewEntity = null;
        this.myNetworkManager = null;

        if (this.loadingScreen != null && isCallingFromMinecraftThread())
        {
            this.loadingScreen.resetProgressAndMessage(loadingMessage);
            this.loadingScreen.displayLoadingString("");
        }

        if (worldClientIn == null && this.world != null)
        {
            this.mcResourcePackRepository.func_148529_f();
            this.ingameGUI.func_181029_i();
            this.setServerData((ServerData)null);
            this.integratedServerIsRunning = false;
        }

        // this.mcSoundHandler.stopSounds();
        this.world = worldClientIn;

        if (worldClientIn != null)
        {
            if (this.renderGlobal != null)
            {
                this.renderGlobal.setWorldAndLoadRenderers(worldClientIn);
            }

            if (this.effectRenderer != null)
            {
                this.effectRenderer.clearEffects(worldClientIn);
            }

            if (this.player == null)
            {
                this.player = this.playerController.create(worldClientIn, new StatFileWriter());
                this.playerController.flipPlayer(this.player);
            }

            this.player.preparePlayerToSpawn();
            worldClientIn.spawnEntityInWorld(this.player);
            this.player.movementInput = new MovementInputFromOptions(this.options);
            this.playerController.setPlayerCapabilities(this.player);
            this.renderViewEntity = this.player;
        }
        else
        {
            this.saveLoader.flushCache();
            this.player = null;
        }

        this.systemTime = 0L;
    }

    public void setDimensionAndSpawnPlayer(int dimension)
    {
        this.world.setInitialSpawnLocation();
        this.world.removeAllEntities();
        int i = 0;
        String s = null;

        if (this.player != null)
        {
            i = this.player.getEntityId();
            this.world.removeEntity(this.player);
            s = this.player.getClientBrand();
        }

        this.renderViewEntity = null;
        EntityPlayerSP entityplayersp = this.player;
        this.player = this.playerController.create(this.world, this.player == null ? new StatFileWriter() : this.player.getStatFileWriter());
        this.player.getDataWatcher().updateWatchedObjectsFromList(entityplayersp.getDataWatcher().getAllWatched());
        this.player.dimension = dimension;
        this.renderViewEntity = this.player;
        this.player.preparePlayerToSpawn();
        this.player.setClientBrand(s);
        this.world.spawnEntityInWorld(this.player);
        this.playerController.flipPlayer(this.player);
        this.player.movementInput = new MovementInputFromOptions(this.options);
        this.player.setEntityId(i);
        this.playerController.setPlayerCapabilities(this.player);
        this.player.setReducedDebug(entityplayersp.hasReducedDebug());

        if (this.currentScreen instanceof GuiGameOver)
        {
            this.displayGuiScreen((GuiScreen)null);
        }
    }

    /**
     * Gets whether this is a demo or not.
     */
    public final boolean isDemo()
    {
        return this.isDemo;
    }

    public NetHandlerPlayClient getNetHandler()
    {
        return this.player != null ? this.player.sendQueue : null;
    }

    public boolean isGuiEnabled()
    {
        return !options.hideGUI;
    }

    public boolean isFancyGraphicsEnabled()
    {
        return options.fancyGraphics;
    }

    public boolean isAmbientOcclusionEnabled()
    {
        return options.ambientOcclusion != 0;
    }

    /**
     * Called when user clicks their mouse's middle button (pick block)
     */
    private void middleClickMouse()
    {
        if (this.hitResult != null)
        {
            boolean flag = this.player.capabilities.isCreativeMode;
            int i = 0;
            boolean flag1 = false;
            TileEntity tileentity = null;
            Item item;

            if (this.hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                BlockPos blockpos = this.hitResult.getBlockPos();
                Block block = this.world.getBlockState(blockpos).getBlock();

                if (block.getMaterial() == Material.air)
                {
                    return;
                }

                item = block.getItem(this.world, blockpos);

                if (item == null)
                {
                    return;
                }

                if (flag && GuiScreen.isCtrlKeyDown())
                {
                    tileentity = this.world.getTileEntity(blockpos);
                }

                Block block1 = item instanceof ItemBlock && !block.isFlowerPot() ? Block.getBlockFromItem(item) : block;
                i = block1.getDamageValue(this.world, blockpos);
                flag1 = item.getHasSubtypes();
            }
            else
            {
                if (this.hitResult.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || this.hitResult.entityHit == null || !flag)
                {
                    return;
                }

                if (this.hitResult.entityHit instanceof EntityPainting)
                {
                    item = Items.painting;
                }
                else if (this.hitResult.entityHit instanceof EntityLeashKnot)
                {
                    item = Items.lead;
                }
                else if (this.hitResult.entityHit instanceof EntityItemFrame)
                {
                    EntityItemFrame entityitemframe = (EntityItemFrame)this.hitResult.entityHit;
                    ItemStack itemstack = entityitemframe.getDisplayedItem();

                    if (itemstack == null)
                    {
                        item = Items.item_frame;
                    }
                    else
                    {
                        item = itemstack.getItem();
                        i = itemstack.getMetadata();
                        flag1 = true;
                    }
                }
                else if (this.hitResult.entityHit instanceof EntityMinecart)
                {
                    EntityMinecart entityminecart = (EntityMinecart)this.hitResult.entityHit;

                    switch (entityminecart.getMinecartType())
                    {
                        case FURNACE:
                            item = Items.furnace_minecart;
                            break;

                        case CHEST:
                            item = Items.chest_minecart;
                            break;

                        case TNT:
                            item = Items.tnt_minecart;
                            break;

                        case HOPPER:
                            item = Items.hopper_minecart;
                            break;

                        case COMMAND_BLOCK:
                            item = Items.command_block_minecart;
                            break;

                        default:
                            item = Items.minecart;
                    }
                }
                else if (this.hitResult.entityHit instanceof EntityBoat)
                {
                    item = Items.boat;
                }
                else if (this.hitResult.entityHit instanceof EntityArmorStand)
                {
                    item = Items.armor_stand;
                }
                else
                {
                    item = Items.spawn_egg;
                    i = EntityList.getEntityID(this.hitResult.entityHit);
                    flag1 = true;

                    if (!EntityList.entityEggs.containsKey(Integer.valueOf(i)))
                    {
                        return;
                    }
                }
            }

            InventoryPlayer inventoryplayer = this.player.inventory;

            if (tileentity == null)
            {
                inventoryplayer.setCurrentItem(item, i, flag1, flag);
            }
            else
            {
                ItemStack itemstack1 = this.func_181036_a(item, i, tileentity);
                inventoryplayer.setInventorySlotContents(inventoryplayer.currentItem, itemstack1);
            }

            if (flag)
            {
                int j = this.player.inventoryContainer.inventorySlots.size() - 9 + inventoryplayer.currentItem;
                this.playerController.sendSlotPacket(inventoryplayer.getStackInSlot(inventoryplayer.currentItem), j);
            }
        }
    }

    private ItemStack func_181036_a(Item p_181036_1_, int p_181036_2_, TileEntity p_181036_3_)
    {
        ItemStack itemstack = new ItemStack(p_181036_1_, 1, p_181036_2_);
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        p_181036_3_.writeToNBT(nbttagcompound);

        if (p_181036_1_ == Items.skull && nbttagcompound.hasKey("Owner"))
        {
            NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("Owner");
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
            nbttagcompound3.setTag("SkullOwner", nbttagcompound2);
            itemstack.setTagCompound(nbttagcompound3);
            return itemstack;
        }
        else
        {
            itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            NBTTagList nbttaglist = new NBTTagList();
            nbttaglist.appendTag(new NBTTagString("(+NBT)"));
            nbttagcompound1.setTag("Lore", nbttaglist);
            itemstack.setTagInfo("display", nbttagcompound1);
            return itemstack;
        }
    }

    /**
     * adds core server Info (GL version , Texture pack, isModded, type), and the worldInfo to the crash report
     */
    public CrashReport addGraphicsAndWorldToCrashReport(CrashReport theCrash)
    {
        theCrash.getCategory().addCrashSectionCallable("Launched Version", new Callable<String>()
        {
            public String call() throws Exception
            {
                return ClientEngine.this.launchedVersion;
            }
        });
        theCrash.getCategory().addCrashSectionCallable("LWJGL", new Callable<String>()
        {
            public String call()
            {
                return Sys.getVersion();
            }
        });
        theCrash.getCategory().addCrashSectionCallable("OpenGL", new Callable<String>()
        {
            public String call()
            {
                return GL11.glGetString(GL11.GL_RENDERER) + " GL version " + GL11.glGetString(GL11.GL_VERSION) + ", " + GL11.glGetString(GL11.GL_VENDOR);
            }
        });
        theCrash.getCategory().addCrashSectionCallable("GL Caps", new Callable<String>()
        {
            public String call()
            {
                return OpenGlHelper.getLogText();
            }
        });
        theCrash.getCategory().addCrashSectionCallable("Using VBOs", new Callable<String>()
        {
            public String call()
            {
                return ClientEngine.this.options.useVbo ? "Yes" : "No";
            }
        });
        theCrash.getCategory().addCrashSectionCallable("Is Modded", new Callable<String>()
        {
            public String call() throws Exception
            {
                String s = ClientBrandRetriever.getClientModName();
                return !s.equals("vanilla") ? "Definitely; Client brand changed to \'" + s + "\'" : (ClientEngine.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and client brand is untouched.");
            }
        });
        theCrash.getCategory().addCrashSectionCallable("Type", new Callable<String>()
        {
            public String call() throws Exception
            {
                return "Client (map_client.txt)";
            }
        });
        theCrash.getCategory().addCrashSectionCallable("Resource Packs", new Callable<String>()
        {
            public String call() throws Exception
            {
                StringBuilder stringbuilder = new StringBuilder();

                for (Object s : ClientEngine.this.options.resourcePacks)
                {
                    if (stringbuilder.length() > 0)
                    {
                        stringbuilder.append(", ");
                    }

                    stringbuilder.append(s);

                    if (ClientEngine.this.options.field_183018_l.contains(s))
                    {
                        stringbuilder.append(" (incompatible)");
                    }
                }

                return stringbuilder.toString();
            }
        });
        theCrash.getCategory().addCrashSectionCallable("Current Language", new Callable<String>()
        {
            public String call() throws Exception
            {
                return ClientEngine.this.mcLanguageManager.getCurrentLanguage().toString();
            }
        });
        theCrash.getCategory().addCrashSectionCallable("CPU", new Callable<String>()
        {
            public String call()
            {
                return OpenGlHelper.func_183029_j();
            }
        });

        if (this.world != null)
        {
            this.world.addWorldInfoToCrashReport(theCrash);
        }

        return theCrash;
    }

    public ListenableFuture<Object> scheduleResourcesRefresh()
    {
        return this.addScheduledTask(new Runnable()
        {
            public void run()
            {
                ClientEngine.this.refreshResources();
            }
        });
    }
    
    private String func_181538_aA()
    {
        return this.theIntegratedServer != null ? (this.theIntegratedServer.getPublic() ? "hosting_lan" : "singleplayer") : (this.currentServerData != null ? (this.currentServerData.func_181041_d() ? "playing_lan" : "multiplayer") : "out_of_game");
    }
    
    /**
     * Used in the usage snooper.
     */
    public int getGLMaximumTextureSize()
    {
        for (int i = 16384; i > 0; i >>= 1)
        {
            GL11.glTexImage2D(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_RGBA, i, i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)((ByteBuffer)null));
            int j = GL11.glGetTexLevelParameteri(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);

            if (j != 0)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns whether snooping is enabled or not.
     */
    public boolean isSnooperEnabled()
    {
        return false;
    }

    /**
     * Set the current ServerData instance.
     */
    public void setServerData(ServerData serverDataIn)
    {
        this.currentServerData = serverDataIn;
    }

    public ServerData getCurrentServerData()
    {
        return this.currentServerData;
    }

    public boolean isIntegratedServerRunning()
    {
        return this.integratedServerIsRunning;
    }

    /**
     * Returns true if there is only one player playing, and the current server is the integrated one.
     */
    public boolean isSingleplayer()
    {
        return this.integratedServerIsRunning && this.theIntegratedServer != null;
    }

    /**
     * Returns the currently running integrated server
     */
    public IntegratedServer getIntegratedServer()
    {
        return this.theIntegratedServer;
    }

    public void stopIntegratedServer()
    {
        IntegratedServer integratedserver = getIntegratedServer();

        if (integratedserver != null)
        {
            integratedserver.stopServer();
        }
    }

    /**
     * Gets the system time in milliseconds.
     */
    public static long getSystemTime()
    {
        try
        {
            return Sys.getTime() * 1000L / Sys.getTimerResolution();
        }
        
        catch (Exception e)
        {
            return System.currentTimeMillis();
        }
    }

    /**
     * Returns whether we're in full screen or not.
     */
    public boolean isFullScreen()
    {
        return this.fullscreen;
    }

    public Session getSession()
    {
        return this.session;
    }

    public PropertyMap getTwitchDetails()
    {
        return this.twitchDetails;
    }

    public PropertyMap func_181037_M()
    {
        if (this.field_181038_N.isEmpty())
        {
            GameProfile gameprofile = this.getSessionService().fillProfileProperties(this.session.profile(), false);
            this.field_181038_N.putAll(gameprofile.getProperties());
        }

        return this.field_181038_N;
    }

    public Proxy getProxy()
    {
        return this.proxy;
    }

    public TextureManager getTextureManager()
    {
        return this.renderEngine;
    }

    public IResourceManager getResourceManager()
    {
        return this.mcResourceManager;
    }

    public ResourcePackRepository getResourcePackRepository()
    {
        return this.mcResourcePackRepository;
    }

    public LanguageManager getLanguageManager()
    {
        return this.mcLanguageManager;
    }

    public TextureMap getTextureMapBlocks()
    {
        return this.textureMapBlocks;
    }

    public boolean isJava64bit()
    {
        return this.jvm64bit;
    }

    public boolean isGamePaused()
    {
        return this.isGamePaused;
    }

    public void dispatchKeypresses()
    {
        int i = Keyboard.get().getEventKey() == 0 ? Keyboard.get().getEventCharacter() : Keyboard.get().getEventKey();

        if (i != 0 && !Keyboard.get().isRepeatEvent())
        {
            if (!(this.currentScreen instanceof GuiControls) || ((GuiControls)this.currentScreen).time <= getSystemTime() - 20L)
            {
                if (Keyboard.get().getEventKeyState())
                {
                    if (i == this.options.keyBindFullscreen.getKeyCode())
                    {
                        this.toggleFullscreen();
                    }
                    else if (i == this.options.keyBindScreenshot.getKeyCode())
                    {
                        this.ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(this.mcDataDir, this.displayWidth, this.displayHeight, this.framebufferMc));
                    }
                }
            }
        }
    }

    public MinecraftSessionService getSessionService()
    {
        return this.sessionService;
    }

    public SkinManager getSkinManager()
    {
        return this.skinManager;
    }

    public Entity getRenderViewEntity()
    {
        return this.renderViewEntity;
    }

    public void setRenderViewEntity(Entity viewingEntity)
    {
        this.renderViewEntity = viewingEntity;
        this.entityRenderer.loadEntityShader(viewingEntity);
    }

    public <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule)
    {
        Validate.notNull(callableToSchedule);

        if (!this.isCallingFromMinecraftThread())
        {
            ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.<V>create(callableToSchedule);

            synchronized (this.scheduledTasks)
            {
                this.scheduledTasks.add(listenablefuturetask);
                return listenablefuturetask;
            }
        }
        else
        {
            try
            {
                return Futures.<V>immediateFuture(callableToSchedule.call());
            }
            catch (Exception exception)
            {
                return Futures.immediateFailedCheckedFuture(exception);
            }
        }
    }

    public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule)
    {
        Validate.notNull(runnableToSchedule);
        return this.<Object>addScheduledTask(Executors.callable(runnableToSchedule));
    }

    public boolean isCallingFromMinecraftThread()
    {
        return Thread.currentThread() == this.mcThread;
    }

    public BlockRendererDispatcher getBlockRendererDispatcher()
    {
        return this.blockRenderDispatcher;
    }

    public RenderManager getRenderManager()
    {
        return this.renderManager;
    }

    public RenderItem getRenderItem()
    {
        return this.renderItem;
    }

    public ItemRenderer getItemRenderer()
    {
        return this.itemRenderer;
    }

    public int getDebugFPS()
    {
        return debugFPS;
    }

    public FrameTimer func_181539_aj()
    {
        return this.field_181542_y;
    }

    public Map<String, String> getSessionInfo()
    {
        Map<String, String> map = Maps.<String, String>newHashMap();
        map.put("X-Minecraft-Username", getSession().name);
        map.put("X-Minecraft-UUID", getSession().playerID);
        map.put("X-Minecraft-Version", "1.8.9");
        return map;
    }

    public boolean func_181540_al()
    {
        return this.field_181541_X;
    }

    public void func_181537_a(boolean p_181537_1_)
    {
        this.field_181541_X = p_181537_1_;
    }

    /* true if Droidmine is not specified with a window */
    public boolean isHeadless()
    {
        return headless;
    }

    private void updateKeyBindState()
    {
        for (KeyBinding keyBinding : KeyBinding.keybindArray)
        {
            try
            {
                keyBinding.setKeyBindState(keyBinding.getKeyCode(), keyBinding.getKeyCode() < 256 && Keyboard.get().isKeyDown(keyBinding.getKeyCode()));
            }
            
            catch (IndexOutOfBoundsException e) {}
        }
    }
}