package org.lwjgl.display;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.LWJGLException;
import org.lwjgl.glfw.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import net.minecraft.util.ResourceLocation;

public class Display {
    private static HashMap<String, Display> displays = new HashMap<>();
    private final DisplayMode initial_mode;
    private DisplayMode current_mode;

    private String title = "Game";
    private final long window;

    private boolean created;
    private boolean resized;
    private boolean focused;
    private boolean vsync;
    private boolean closeRequested;
    
    static
    {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        if ( !glfwInit() )
            throw new RuntimeException("Failed to initialize GLFW");
    }

    public Display()
    {
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        int bpp = vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits();
        current_mode = initial_mode = new DisplayMode(vidMode.width(), vidMode.height(), bpp, vidMode.refreshRate());

        //INITIAL SETUP
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        //glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE);
        glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);

        window = glfwCreateWindow(current_mode.getWidth(), current_mode.getHeight(), title, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(window);

        GL.createCapabilities();
    }

    public void setDisplayMode(DisplayMode displayMode) {
        current_mode = displayMode;
        glfwSetWindowSize(window, displayMode.getWidth(), displayMode.getHeight());
        glfwSetWindowPos(
                window,
                (initial_mode.getWidth() - displayMode.getWidth()) / 2,
                (initial_mode.getHeight() - displayMode.getHeight()) / 2
        );
    }

    public DisplayMode getDisplayMode() {
        return current_mode;
    }

    public void setFullscreen(boolean fullscreen) {
        if(fullscreen) glfwSetWindowSize(window, initial_mode.getWidth(), initial_mode.getHeight());
        else glfwSetWindowSize(window, current_mode.getWidth(), current_mode.getHeight());
        glfwWindowHint(GLFW_DECORATED, fullscreen ? GLFW_FALSE : GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, fullscreen ? GLFW_FALSE : GLFW_TRUE);
    }

    public void setResizable(boolean resizable){
        glfwWindowHint(GLFW_RESIZABLE, resizable ? 1 : 0);
    }

    public void setTitle(String titleIn) {
        title = titleIn;
        glfwSetWindowTitle(window, titleIn);
    }

    public void setIcon(ResourceLocation[] path){}

    public void setVSyncEnabled(boolean vsyncIn) {
        if(glfwGetCurrentContext() != 0){
            glfwSwapInterval(vsyncIn ? GLFW_TRUE : GLFW_FALSE);
        }
        vsync = vsyncIn;
    }

    public void destroy() {
        glfwDestroyWindow(window);
        created = false;
    }

    public DisplayMode[] getAvailableDisplayModes(){
            GLFWVidMode.Buffer m = glfwGetVideoModes(glfwGetPrimaryMonitor());
            ArrayList<DisplayMode> modes = new ArrayList<>();
            while(m.hasRemaining()){
                GLFWVidMode vidMode =  m.get();
                int bpp = vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits();
                modes.add(new DisplayMode(vidMode.width(), vidMode.height(), bpp, vidMode.refreshRate()));
            }
            return modes.toArray(new DisplayMode[modes.size()]);
    }

    public DisplayMode getDesktopDisplayMode(){
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            int bpp = vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits();
            return new DisplayMode(vidMode.width(), vidMode.height(), bpp, vidMode.refreshRate());
    }

    public boolean isCreated(){
        return created;
    }

    public boolean isCloseRequested(){
        return glfwWindowShouldClose(window);
    }

    public void sync(int fps){
        Sync.sync(fps);
    }

    public void update(){
            if(glfwGetWindowAttrib(window, GLFW_VISIBLE) == 1) {
                resized = false;
                glfwSwapBuffers(window);
                Mouse.get().poll_scrollY = 0;
                glfwPollEvents();
                Mouse.get().createEvent();
            }
    }

    public boolean wasResized(){
        return resized;
    }

    public int getWidth(){
        return current_mode.getWidth();
    }

    public int getHeight(){
        return current_mode.getHeight();
    }

    public boolean isActive(){
        return focused;
    }

    public long getWindow() {
        return window;
    }

    public void releaseContext() {
        glfwSetWindowShouldClose(window, true);
    }

    public void create(boolean headless) throws LWJGLException {
        create(new PixelFormat(), new ContextAttribs(3, 3).withProfileCore(true), headless);
    }

    public void create(PixelFormat pf, boolean headless) throws LWJGLException {
        create(pf,  new ContextAttribs(3, 3).withProfileCore(true), headless);
    }

    public void create(PixelFormat pf, ContextAttribs attributes, boolean headless) throws LWJGLException {
        created = true;
        try ( MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Center the window
            glfwSetWindowPos(
                    window,
                    (initial_mode.getWidth() - pWidth.get(0)) / 2,
                    (initial_mode.getHeight() - pHeight.get(0)) / 2
            );
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, attributes.getVersion_major());
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, attributes.getVersion_minor());
        if(attributes.isProfileCore()) glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                current_mode = new DisplayMode(width, height);
                resized = true;
            }
        });
        glfwSetWindowFocusCallback(window, new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long l, boolean focused) {
                Display.this.focused = focused;
                Mouse.get().poll_xPos = Mouse.get().last_x;
                Mouse.get().poll_yPos = Mouse.get().last_y;
            }
        });

        Keyboard.get(headless).create();
        Keyboard.get().pollGLFW();
        Mouse.get(headless).create();
        Mouse.get().pollGLFW();

        if (glfwRawMouseMotionSupported() && !headless)
            glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);

        glfwSwapInterval(vsync ? GLFW_TRUE : GLFW_FALSE);
        if (!headless) glfwShowWindow(window);
    }

    public static Display get()
    {
        String thread = Thread.currentThread().getName();

        if (displays.containsKey(thread))
        {
            return displays.get(thread);
        }
        
        Display display = new Display();
        displays.put(thread, display);
        return display;
    }
}
