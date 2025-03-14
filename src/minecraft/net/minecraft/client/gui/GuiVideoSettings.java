package net.minecraft.client.gui;

import java.io.IOException;

import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resources.I18n;
import net.minecraft.optifine.*;
import net.minecraft.optifine.shadersmod.client.GuiShaders;

public class GuiVideoSettings extends GuiScreen
{
    private GuiScreen parentGuiScreen;
    protected String screenTitle = "Video Settings";
    private GameOptions guioptions;

    /** An array of all of options.Options's video options. */
    private static GameOptions.Options[] videoOptions = new GameOptions.Options[] {GameOptions.Options.GRAPHICS, GameOptions.Options.RENDER_DISTANCE, GameOptions.Options.AMBIENT_OCCLUSION, GameOptions.Options.FRAMERATE_LIMIT, GameOptions.Options.AO_LEVEL, GameOptions.Options.VIEW_BOBBING, GameOptions.Options.GUI_SCALE, GameOptions.Options.USE_VBO, GameOptions.Options.GAMMA, GameOptions.Options.BLOCK_ALTERNATIVES, GameOptions.Options.FOG_FANCY, GameOptions.Options.FOG_START};
    private static final String __OBFID = "CL_00000718";
    private TooltipManager tooltipManager = new TooltipManager(this);

    public GuiVideoSettings(GuiScreen parentScreenIn, GameOptions optionsIn)
    {
        this.parentGuiScreen = parentScreenIn;
        this.guioptions = optionsIn;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        this.screenTitle = I18n.format("options.videoTitle", new Object[0]);
        this.buttonList.clear();

        for (int i = 0; i < videoOptions.length; ++i)
        {
            GameOptions.Options options$options = videoOptions[i];

            if (options$options != null)
            {
                int j = this.width / 2 - 155 + i % 2 * 160;
                int k = this.height / 6 + 21 * (i / 2) - 12;

                if (options$options.getEnumFloat())
                {
                    this.buttonList.add(new GuiOptionSliderOF(options$options.returnEnumOrdinal(), j, k, options$options, mc));
                }
                else
                {
                    this.buttonList.add(new GuiOptionButtonOF(options$options.returnEnumOrdinal(), j, k, options$options, this.guioptions.getKeyBinding(options$options)));
                }
            }
        }

        int l = this.height / 6 + 21 * (videoOptions.length / 2) - 12;
        int i1 = 0;
        i1 = this.width / 2 - 155 + 0;
        this.buttonList.add(new GuiOptionButton(231, i1, l, Lang.get("of.options.shaders")));
        i1 = this.width / 2 - 155 + 160;
        this.buttonList.add(new GuiOptionButton(202, i1, l, Lang.get("of.options.quality")));
        l = l + 21;
        i1 = this.width / 2 - 155 + 0;
        this.buttonList.add(new GuiOptionButton(201, i1, l, Lang.get("of.options.details")));
        i1 = this.width / 2 - 155 + 160;
        this.buttonList.add(new GuiOptionButton(212, i1, l, Lang.get("of.options.performance")));
        l = l + 21;
        i1 = this.width / 2 - 155 + 0;
        this.buttonList.add(new GuiOptionButton(211, i1, l, Lang.get("of.options.animations")));
        i1 = this.width / 2 - 155 + 160;
        this.buttonList.add(new GuiOptionButton(222, i1, l, Lang.get("of.options.other")));
        l = l + 21;
        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.format("gui.done", new Object[0])));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            int i = this.guioptions.guiScale;

            if (button.id < 200 && button instanceof GuiOptionButton)
            {
                this.guioptions.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
                button.displayString = this.guioptions.getKeyBinding(GameOptions.Options.getEnumOptions(button.id));
            }

            if (button.id == 200)
            {
                this.mc.options.saveOptions();
                this.mc.displayGuiScreen(this.parentGuiScreen);
            }

            if (this.guioptions.guiScale != i)
            {
                ScaledResolution scaledresolution = ScaledResolution.get();
                int j = scaledresolution.getScaledWidth();
                int k = scaledresolution.getScaledHeight();
                this.setWorldAndResolution(this.mc, j, k);
            }

            if (button.id == 201)
            {
                this.mc.options.saveOptions();
                GuiDetailSettingsOF guidetailsettingsof = new GuiDetailSettingsOF(this, this.guioptions);
                this.mc.displayGuiScreen(guidetailsettingsof);
            }

            if (button.id == 202)
            {
                this.mc.options.saveOptions();
                GuiQualitySettingsOF guiqualitysettingsof = new GuiQualitySettingsOF(this, this.guioptions);
                this.mc.displayGuiScreen(guiqualitysettingsof);
            }

            if (button.id == 211)
            {
                this.mc.options.saveOptions();
                GuiAnimationSettingsOF guianimationsettingsof = new GuiAnimationSettingsOF(this, this.guioptions);
                this.mc.displayGuiScreen(guianimationsettingsof);
            }

            if (button.id == 212)
            {
                this.mc.options.saveOptions();
                GuiPerformanceSettingsOF guiperformancesettingsof = new GuiPerformanceSettingsOF(this, this.guioptions);
                this.mc.displayGuiScreen(guiperformancesettingsof);
            }

            if (button.id == 222)
            {
                this.mc.options.saveOptions();
                GuiOtherSettingsOF guiothersettingsof = new GuiOtherSettingsOF(this, this.guioptions);
                this.mc.displayGuiScreen(guiothersettingsof);
            }

            if (button.id == 231)
            {
                if (Config.get().isAntialiasing() || Config.get().isAntialiasingConfigured())
                {
                    Config.get().showGuiMessage(Lang.get("of.message.shaders.aa1"), Lang.get("of.message.shaders.aa2"));
                    return;
                }

                if (Config.get().isAnisotropicFiltering())
                {
                    Config.get().showGuiMessage(Lang.get("of.message.shaders.af1"), Lang.get("of.message.shaders.af2"));
                    return;
                }

                if (Config.get().isFastRender())
                {
                    Config.get().showGuiMessage(Lang.get("of.message.shaders.fr1"), Lang.get("of.message.shaders.fr2"));
                    return;
                }

                this.mc.options.saveOptions();
                GuiShaders guishaders = new GuiShaders(this, this.guioptions);
                this.mc.displayGuiScreen(guishaders);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 15, 16777215);
        String s = Config.get().getVersion();
        String s1 = "HD_U";

        if (s1.equals("HD"))
        {
            s = "OptiFine HD H8";
        }

        if (s1.equals("HD_U"))
        {
            s = "OptiFine HD H8 Ultra";
        }

        if (s1.equals("L"))
        {
            s = "OptiFine H8 Light";
        }

        this.drawString(this.fontRendererObj, s, 2, this.height - 10, 8421504);
        String s2 = "Minecraft 1.8.9";
        int i = this.fontRendererObj.getStringWidth(s2);
        this.drawString(this.fontRendererObj, s2, this.width - i - 2, this.height - 10, 8421504);
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.tooltipManager.drawTooltips(mouseX, mouseY, this.buttonList);
    }

    public static int getButtonWidth(GuiButton p_getButtonWidth_0_)
    {
        return p_getButtonWidth_0_.width;
    }

    public static int getButtonHeight(GuiButton p_getButtonHeight_0_)
    {
        return p_getButtonHeight_0_.height;
    }

    public static void drawGradientRect(GuiScreen p_drawGradientRect_0_, int p_drawGradientRect_1_, int p_drawGradientRect_2_, int p_drawGradientRect_3_, int p_drawGradientRect_4_, int p_drawGradientRect_5_, int p_drawGradientRect_6_)
    {
        p_drawGradientRect_0_.drawGradientRect(p_drawGradientRect_1_, p_drawGradientRect_2_, p_drawGradientRect_3_, p_drawGradientRect_4_, p_drawGradientRect_5_, p_drawGradientRect_6_);
    }
}
