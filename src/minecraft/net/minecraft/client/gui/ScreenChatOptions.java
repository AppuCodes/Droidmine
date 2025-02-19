package net.minecraft.client.gui;

import java.io.IOException;

import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resources.I18n;

public class ScreenChatOptions extends GuiScreen
{
    private static final GameOptions.Options[] field_146399_a = new GameOptions.Options[] {GameOptions.Options.CHAT_VISIBILITY, GameOptions.Options.CHAT_COLOR, GameOptions.Options.CHAT_LINKS, GameOptions.Options.CHAT_OPACITY, GameOptions.Options.CHAT_LINKS_PROMPT, GameOptions.Options.CHAT_SCALE, GameOptions.Options.CHAT_HEIGHT_FOCUSED, GameOptions.Options.CHAT_HEIGHT_UNFOCUSED, GameOptions.Options.CHAT_WIDTH, GameOptions.Options.REDUCED_DEBUG_INFO};
    private final GuiScreen parentScreen;
    private final GameOptions game_settings;
    private String field_146401_i;

    public ScreenChatOptions(GuiScreen parentScreenIn, GameOptions optionsIn)
    {
        this.parentScreen = parentScreenIn;
        this.game_settings = optionsIn;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        int i = 0;
        this.field_146401_i = I18n.format("options.chat.title", new Object[0]);

        for (GameOptions.Options options$options : field_146399_a)
        {
            if (options$options.getEnumFloat())
            {
                this.buttonList.add(new GuiOptionSlider(options$options.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), options$options));
            }
            else
            {
                this.buttonList.add(new GuiOptionButton(options$options.returnEnumOrdinal(), this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), options$options, this.game_settings.getKeyBinding(options$options)));
            }

            ++i;
        }

        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 120, I18n.format("gui.done", new Object[0])));
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id < 100 && button instanceof GuiOptionButton)
            {
                this.game_settings.setOptionValue(((GuiOptionButton)button).returnEnumOptions(), 1);
                button.displayString = this.game_settings.getKeyBinding(GameOptions.Options.getEnumOptions(button.id));
            }

            if (button.id == 200)
            {
                this.mc.options.saveOptions();
                this.mc.displayGuiScreen(this.parentScreen);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.field_146401_i, this.width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
