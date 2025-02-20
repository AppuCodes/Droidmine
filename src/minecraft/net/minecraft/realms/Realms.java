package net.minecraft.realms;

import java.net.Proxy;

import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;

import net.droidmine.Session;
import net.minecraft.client.ClientEngine;
import net.minecraft.world.WorldSettings;

public class Realms
{
    public static boolean isTouchScreen()
    {
        return false;
    }

    public static Proxy getProxy()
    {
        return null;
    }

    public static String sessionId()
    {
        return null;
    }

    public static String userName()
    {
        return null;
    }

    public static long currentTimeMillis()
    {
        return ClientEngine.getSystemTime();
    }

    public static String getSessionId()
    {
        return "";
    }

    public static String getUUID()
    {
        return "";
    }

    public static String getName()
    {
        return "";
    }

    public static String uuidToName(String p_uuidToName_0_)
    {
        return "";
    }

    public static void setScreen(RealmsScreen p_setScreen_0_)
    {
    }

    public static String getGameDirectoryPath()
    {
        return "";
    }

    public static int survivalId()
    {
        return WorldSettings.GameType.SURVIVAL.getID();
    }

    public static int creativeId()
    {
        return WorldSettings.GameType.CREATIVE.getID();
    }

    public static int adventureId()
    {
        return WorldSettings.GameType.ADVENTURE.getID();
    }

    public static int spectatorId()
    {
        return WorldSettings.GameType.SPECTATOR.getID();
    }

    public static void setConnectedToRealms(boolean p_setConnectedToRealms_0_)
    {
    }

    public static ListenableFuture<Object> downloadResourcePack(String p_downloadResourcePack_0_, String p_downloadResourcePack_1_)
    {
        return null;
    }

    public static void clearResourcePack()
    {
    }
}
