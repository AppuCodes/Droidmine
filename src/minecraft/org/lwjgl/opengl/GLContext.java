package org.lwjgl.opengl;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

public class GLContext {

    public static GLCapabilities getCapabilities(){
        return GL.getCapabilities();
    }
}
