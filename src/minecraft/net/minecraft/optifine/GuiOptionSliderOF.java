package net.minecraft.optifine;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.options.GameOptions;

public class GuiOptionSliderOF extends GuiOptionSlider implements IOptionControl
{
    private GameOptions.Options option = null;

    public GuiOptionSliderOF(int p_i50_1_, int p_i50_2_, int p_i50_3_, GameOptions.Options p_i50_4_, ClientEngine mc)
    {
        super(p_i50_1_, p_i50_2_, p_i50_3_, p_i50_4_, mc);
        this.option = p_i50_4_;
    }

    public GameOptions.Options getOption()
    {
        return this.option;
    }
}
