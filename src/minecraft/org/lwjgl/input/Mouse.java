package org.lwjgl.input;

import static org.lwjgl.glfw.GLFW.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.display.Display;
import org.lwjgl.glfw.*;

public class Mouse {

    protected boolean created;

    protected String[] buttonName;
    protected final Map<String, Integer> buttonMap = new HashMap<>(8);
    protected ByteBuffer readBuffer;
    protected ByteBuffer buttons;
    protected final int BUFFER_SIZE = 50;
    //Max Nummber of buttons glfw supports https://www.glfw.org/docs/3.3/group__buttons.html
    protected final int BUTTON_COUNT = 8;
    //int = 4 boolean = 1, long = 8
    // 4 + 1 + 4 + 4 + 4 + 4 + 4
    public final int EVENT_SIZE = 4 + 1 + 4 + 4 + 4 + 4 + 4;
    protected MouseEvent current_Event;
    protected boolean isGrabbed;

    protected int last_event_raw_x;
    protected int last_event_raw_y;
    protected boolean clipMouseCoordinatesToWindow;

    protected int poll_button = 0;
    protected int poll_action = 0;
    public int poll_xPos = 0;
    public int poll_yPos = 0;
    public int poll_scrollY = 0;
    protected int current_dx = 0;
    protected int current_dy = 0;
    public int last_x = 0;
    public int last_y = 0;
    protected boolean poll_outside;
    protected boolean pollNeed;
    
    protected static HashMap<String, Mouse> mouses = new HashMap<>();

    public void pollGLFW(){
        if(!created) return;
        glfwSetMouseButtonCallback(Display.get().getWindow(), new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long l, int i, int i1, int i2) {
                poll_button = i;
                poll_action = i1;
                buttons.put(i, (byte)i1);
                pollNeed = true;
            }
        });
        glfwSetCursorPosCallback(Display.get().getWindow(), new GLFWCursorPosCallback() {
            @Override
            public void invoke(long l, double v, double v1) {
                if(Display.get().isActive()){
                    last_x = poll_xPos;
                    last_y = poll_yPos;
                    poll_xPos = (int) v;
                    poll_yPos = (int) (Display.get().getHeight() - v1);
                    current_dx = (int) ((poll_xPos - last_x) * 6.2);
                    current_dy = (int) ((poll_yPos - last_y) * 6.2);
                    pollNeed = true;
                }
            }
        });
        glfwSetScrollCallback(Display.get().getWindow(), new GLFWScrollCallback() {
            @Override
            public void invoke(long l, double v, double v1) {
                poll_scrollY = (int) v1;
                pollNeed = true;
            }
        });
        glfwSetCursorEnterCallback(Display.get().getWindow(), new GLFWCursorEnterCallback() {
            @Override
            public void invoke(long l, boolean entered) {
                poll_outside = entered;
            }
        });
    }
    
    public void createEvent(){
        if(!created || !pollNeed) return;
        readBuffer.compact();
        pollNeed = false;
        readBuffer.putInt(poll_button);
        readBuffer.put((byte)poll_action);
        if(isGrabbed){
            readBuffer.putInt(normalize(poll_xPos - last_x));
            readBuffer.putInt(normalize(poll_yPos - last_y));
        }else {
            readBuffer.putInt(poll_xPos);
            readBuffer.putInt(poll_yPos);
        }
        readBuffer.putInt(poll_scrollY);
        readBuffer.flip();
        //To prevent doubleclick when moving
        poll_button = -1;
        poll_action = 0;
    }

    public void create(){
        if(created) return;
        buttons = BufferUtils.createByteBuffer(BUTTON_COUNT);
        readBuffer = ByteBuffer.allocate(EVENT_SIZE * BUFFER_SIZE);
        readBuffer.limit(0);
        clipMouseCoordinatesToWindow = true;
        created = true;
        buttonName = new String[8];

        for (int i = 0; i < 8; i++) {
            buttonName[i] = "BUTTON" + i;
            buttonMap.put(buttonName[i], i);
        }
    }
    
    public boolean next(){
        if(!created) throw new IllegalStateException("Mouse must be created before you can read events");
        if(readBuffer.hasRemaining()){
            MouseEvent event = new MouseEvent();
            event.eventButton = readBuffer.getInt();
            event.eventState = readBuffer.get() != 0;

            if(isGrabbed){
                event.dx = readBuffer.getInt();
                event.dy = readBuffer.getInt();
                event.x += event.dx;
                event.y += event.dy;
                last_event_raw_x = event.x;
                last_event_raw_y = event.y;
            }else{
                int new_event_x = readBuffer.getInt();
                int new_event_y = readBuffer.getInt();
                event.dx = new_event_x - last_event_raw_x;
                event.dy = new_event_y - last_event_raw_y;
                event.x = new_event_x;
                event.y = new_event_y;
                last_event_raw_x = new_event_x;
                last_event_raw_y = new_event_y;
            }
            if(clipMouseCoordinatesToWindow) {
                event.x = Math.min(Display.get().getWidth() - 1, Math.max(0, event.x));
                event.y = Math.min(Display.get().getHeight() - 1, Math.max(0, event.y));
            }
            event.event_dwheel = readBuffer.getInt();
            current_Event = event;
            return true;
        }
        
        return false;
    }

    public int getButtonIndex(String buttonName) {
        Integer ret = buttonMap.get(buttonName);
        if (ret == null)
            return -1;
        else
            return ret;
    }

    public String getButtonName(int button) {
        if (button >= buttonName.length || button < 0)
            return null;
        else
            return buttonName[button];
    }

    public boolean isInsideWindow() {
        return poll_outside;
    }
    
    public int getEventButton(){
        return current_Event.eventButton;
    }
    
    public boolean getEventButtonState(){
        return current_Event.eventState;
    }
    
    public int getEventDWheel(){
        return current_Event.event_dwheel;
    }
    
    public int getEventX(){
        return current_Event.x;
    }
    
    public int getEventY(){
        return current_Event.y;
    }
    
    public boolean isCreated(){
        return created;
    }
    
    public int getX(){
        return Math.min(Math.max(poll_xPos, 0), Display.get().getWidth() - 1);
    }
    
    public int getY(){
        return Math.min(Math.max(poll_yPos, 0), Display.get().getHeight() - 1);
    }
    
    public void setClipMouseCoordinatesToWindow(boolean clip) {
        clipMouseCoordinatesToWindow = clip;
    }
    
    public boolean isButtonDown(int button){
        if (!created) throw new IllegalStateException("Mouse must be created before you can poll the button state");
        if (button >= BUTTON_COUNT || button < 0)
            return false;
        else
            return buttons.get(button) == 1;
    }
    
    public void setGrabbed(boolean grab){
        if (!created) throw new IllegalStateException("Mouse must be created before you can poll the button state");
        isGrabbed = grab;
        if (!grab) glfwSetInputMode(Display.get().getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        else glfwSetInputMode(Display.get().getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetCursorPos(Display.get().getWindow(), poll_xPos, poll_yPos);
    }
    
    public void setCursorPosition(int x, int y){
        if (!isCreated()) throw new IllegalStateException("Mouse is not created");
        
        if (current_Event != null)
        {
            poll_xPos = current_Event.x = x;
            poll_yPos = current_Event.y = y;
            glfwSetCursorPos(Display.get().getWindow(), x, y);
        }
    }
    
    public int getDX(){
        int result = current_dx;
        current_dx = 0;
        return result;
    }
    
    public int getDY(){
        int result = current_dy;
        current_dy = 0;
        return result;
    }
    
    private int normalize(int input){
        if(input < 0) return input /-1;
        return input;
    }

    public int getDWheel() {
        return poll_scrollY;
    }

    private final class MouseEvent {

        private int eventButton;
        private boolean eventState;

        private int dx;
        private int dy;
        private int x;
        private int y;
        private int event_dwheel;

    }
    
    public static Mouse get()
    {
        return get(false);
    }
    
    public static Mouse get(boolean headless)
    {
        String thread = Thread.currentThread().getName();

        if (mouses.containsKey(thread))
        {
            return mouses.get(thread);
        }
        
        Mouse mouse = headless ? new HeadlessMouse() : new Mouse();
        mouses.put(thread, mouse);
        return mouse;
    }
}
