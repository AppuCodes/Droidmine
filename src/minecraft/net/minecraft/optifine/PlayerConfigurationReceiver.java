package net.minecraft.optifine;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.minecraft.client.ClientEngine;

public class PlayerConfigurationReceiver implements IFileDownloadListener
{
    private String player = null;
    private ClientEngine mc;

    public PlayerConfigurationReceiver(String p_i72_1_, ClientEngine mc)
    {
        this.player = p_i72_1_;
        this.mc = mc;
    }

    public void fileDownloadFinished(String p_fileDownloadFinished_1_, byte[] p_fileDownloadFinished_2_, Throwable p_fileDownloadFinished_3_)
    {
        if (p_fileDownloadFinished_2_ != null)
        {
            try
            {
                String s = new String(p_fileDownloadFinished_2_, "ASCII");
                JsonParser jsonparser = new JsonParser();
                JsonElement jsonelement = jsonparser.parse(s);
                PlayerConfigurationParser playerconfigurationparser = new PlayerConfigurationParser(this.player, mc);
                PlayerConfiguration playerconfiguration = playerconfigurationparser.parsePlayerConfiguration(jsonelement);

                if (playerconfiguration != null)
                {
                    playerconfiguration.setInitialized(true);
                    PlayerConfigurations.setPlayerConfiguration(this.player, playerconfiguration);
                }
            }
            catch (Exception exception)
            {
            }
        }
    }
}
