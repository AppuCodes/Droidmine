package net.minecraft.optifine;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resources.I18n;

public class GuiDetailSettingsOF extends GuiScreen
{
    private GuiScreen prevScreen;
    protected String title;
    private GameOptions settings;
    private static GameOptions.Options[] enumOptions = new GameOptions.Options[] {GameOptions.Options.CLOUDS, GameOptions.Options.CLOUD_HEIGHT, GameOptions.Options.TREES, GameOptions.Options.RAIN, GameOptions.Options.SKY, GameOptions.Options.STARS, GameOptions.Options.SUN_MOON, GameOptions.Options.SHOW_CAPES, GameOptions.Options.TRANSLUCENT_BLOCKS, GameOptions.Options.HELD_ITEM_TOOLTIPS, GameOptions.Options.DROPPED_ITEMS, GameOptions.Options.ENTITY_SHADOWS, GameOptions.Options.VIGNETTE, GameOptions.Options.DYNAMIC_FOV};
    private TooltipManager tooltipManager = new TooltipManager(this);

    public GuiDetailSettingsOF(GuiScreen p_i47_1_, GameOptions p_i47_2_)
    {
        this.prevScreen = p_i47_1_;
        this.settings = p_i47_2_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        this.title = I18n.format("of.options.detailsTitle", new Object[0]);
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
                this.buttonList.add(new GuiOptionSliderOF(options$options.returnEnumOrdinal(), j, k, options$options));
            }
        }

        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.format("gui.done", new Object[0])));
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
        this.tooltipManager.drawTooltips(mouseX, mouseY, this.buttonList);
    }
}
