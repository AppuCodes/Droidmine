package net.minecraft.optifine;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resources.I18n;

public class GuiAnimationSettingsOF extends GuiScreen
{
    private GuiScreen prevScreen;
    protected String title;
    private GameOptions settings;
    private static GameOptions.Options[] enumOptions = new GameOptions.Options[] {GameOptions.Options.ANIMATED_WATER, GameOptions.Options.ANIMATED_LAVA, GameOptions.Options.ANIMATED_FIRE, GameOptions.Options.ANIMATED_PORTAL, GameOptions.Options.ANIMATED_REDSTONE, GameOptions.Options.ANIMATED_EXPLOSION, GameOptions.Options.ANIMATED_FLAME, GameOptions.Options.ANIMATED_SMOKE, GameOptions.Options.VOID_PARTICLES, GameOptions.Options.WATER_PARTICLES, GameOptions.Options.RAIN_SPLASH, GameOptions.Options.PORTAL_PARTICLES, GameOptions.Options.POTION_PARTICLES, GameOptions.Options.DRIPPING_WATER_LAVA, GameOptions.Options.ANIMATED_TERRAIN, GameOptions.Options.ANIMATED_TEXTURES, GameOptions.Options.FIREWORK_PARTICLES, GameOptions.Options.PARTICLES};

    public GuiAnimationSettingsOF(GuiScreen p_i46_1_, GameOptions p_i46_2_)
    {
        this.prevScreen = p_i46_1_;
        this.settings = p_i46_2_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        this.title = I18n.format("of.options.animationsTitle", new Object[0]);
        this.buttonList.clear();

        for (int i = 0; i < enumOptions.length; ++i)
        {
            GameOptions.Options options$options = enumOptions[i];
            int j = this.width / 2 - 155 + i % 2 * 160;
            int k = this.height / 6 + 21 * (i / 2) - 12;

            if (!options$options.getEnumFloat())
            {
                this.buttonList.add(new GuiOptionButtonOF(options$options.returnEnumOrdinal(), j, k, options$options, this.settings.getKeyBinding(options$options)));
            }
            else
            {
                this.buttonList.add(new GuiOptionSliderOF(options$options.returnEnumOrdinal(), j, k, options$options, mc));
            }
        }

        this.buttonList.add(new GuiButton(210, this.width / 2 - 155, this.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOn")));
        this.buttonList.add(new GuiButton(211, this.width / 2 - 155 + 80, this.height / 6 + 168 + 11, 70, 20, Lang.get("of.options.animation.allOff")));
        this.buttonList.add(new GuiOptionButton(200, this.width / 2 + 5, this.height / 6 + 168 + 11, I18n.format("gui.done", new Object[0])));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id < 200 && button instanceof GuiOptionButton)
            {
                this.settings.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
                button.displayString = this.settings.getKeyBinding(GameOptions.Options.getEnumOptions(button.id));
            }

            if (button.id == 200)
            {
                this.mc.options.saveOptions();
                this.mc.displayGuiScreen(this.prevScreen);
            }

            if (button.id == 210)
            {
                this.mc.options.setAllAnimations(true);
            }

            if (button.id == 211)
            {
                this.mc.options.setAllAnimations(false);
            }

            ScaledResolution scaledresolution = ScaledResolution.get();
            this.setWorldAndResolution(this.mc, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
