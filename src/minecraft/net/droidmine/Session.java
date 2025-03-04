package net.droidmine;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;

public class Session
{
    public final String name, playerID, token, sessionType;
    
    public static Session offline(String name)
    {
        return new Session(name, name, name, "offline");
    }
    
    /** opens an authentication window if not cached. */
    public static Session microsoft()
    {
        return new Session("Droidmine", "", "", "online");
    }
    
    public GameProfile profile()
    {
        try
        {
            UUID uuid = UUIDTypeAdapter.fromString(playerID);
            return new GameProfile(uuid, name);
        }
        
        catch (Throwable t)
        {
            return new GameProfile(null, name);
        }
    }
    
    private Session(String name, String playerID, String token, String sessionType) { this.name = name; this.playerID = playerID; this.token = token; this.sessionType = sessionType; }
}
