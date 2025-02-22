package net.minecraft.util;

import net.minecraft.client.options.GameOptions;

public class MovementInputFromOptions extends MovementInput
{
    private final GameOptions options;

    public MovementInputFromOptions(GameOptions optionsIn)
    {
        this.options = optionsIn;
    }

    public void updatePlayerMoveState()
    {
    }
}
