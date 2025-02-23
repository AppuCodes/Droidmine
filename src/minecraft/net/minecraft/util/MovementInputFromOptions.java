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
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.options.keyBindForward.isKeyDown())
        {
            ++this.moveForward;
        }

        if (this.options.keyBindBack.isKeyDown())
        {
            --this.moveForward;
        }

        if (this.options.keyBindLeft.isKeyDown())
        {
            ++this.moveStrafe;
        }

        if (this.options.keyBindRight.isKeyDown())
        {
            --this.moveStrafe;
        }

        this.jump = this.options.keyBindJump.isKeyDown();
        this.sneak = this.options.keyBindSneak.isKeyDown();

        if (this.sneak)
        {
            this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
            this.moveForward = (float)((double)this.moveForward * 0.3D);
        }
    }
}
