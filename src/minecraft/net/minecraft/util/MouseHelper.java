package net.minecraft.util;

import org.lwjgl.display.Display;
import org.lwjgl.input.Mouse;

public class MouseHelper
{
    /** Mouse delta X this frame */
    public float deltaX;

    /** Mouse delta Y this frame */
    public float deltaY;

    /**
     * Grabs the mouse cursor it doesn't move and isn't seen.
     */
    public void grabMouseCursor()
    {
        Mouse.get().setGrabbed(true);
        this.deltaX = 0;
        this.deltaY = 0;
    }

    /**
     * Ungrabs the mouse cursor so it can be moved and set it to the center of the screen
     */
    public void ungrabMouseCursor()
    {
        Mouse.get().setCursorPosition(Display.get().getWidth() / 2, Display.get().getHeight() / 2);
        Mouse.get().setGrabbed(false);
    }

    public void mouseXYChange()
    {
        this.deltaX = Mouse.get().getDX();
        this.deltaY = Mouse.get().getDY();
    }
}
