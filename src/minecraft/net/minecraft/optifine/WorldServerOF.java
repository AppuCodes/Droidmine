package net.minecraft.optifine;

import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

public class WorldServerOF extends WorldServer
{
    private MinecraftServer mcServer;

    public WorldServerOF(MinecraftServer p_i98_1_, ISaveHandler p_i98_2_, WorldInfo p_i98_3_, int p_i98_4_)
    {
        super(p_i98_1_, p_i98_2_, p_i98_3_, p_i98_4_);
        this.mcServer = p_i98_1_;
    }

    /**
     * Runs a single tick for the world
     */
    public void tick()
    {
        super.tick();

        if (!Config.get().isTimeDefault())
        {
            this.fixWorldTime();
        }

        if (Config.get().waterOpacityChanged)
        {
            Config.get().waterOpacityChanged = false;
            ClearWater.updateWaterOpacity(Config.get().getoptions(), this);
        }
    }

    /**
     * Updates all weather states.
     */
    protected void updateWeather()
    {
        super.updateWeather();
    }

    private void fixWorldWeather()
    {
        if (this.worldInfo.isRaining() || this.worldInfo.isThundering())
        {
            this.worldInfo.setRainTime(0);
            this.worldInfo.setRaining(false);
            this.setRainStrength(0.0F);
            this.worldInfo.setThunderTime(0);
            this.worldInfo.setThundering(false);
            this.setThunderStrength(0.0F);
            this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(2, 0.0F));
            this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(7, 0.0F));
            this.mcServer.getConfigurationManager().sendPacketToAllPlayers(new S2BPacketChangeGameState(8, 0.0F));
        }
    }

    private void fixWorldTime()
    {
        if (this.worldInfo.getGameType().getID() == 1)
        {
            long i = this.getWorldTime();
            long j = i % 24000L;

            if (Config.get().isTimeDayOnly())
            {
                if (j <= 1000L)
                {
                    this.setWorldTime(i - j + 1001L);
                }

                if (j >= 11000L)
                {
                    this.setWorldTime(i - j + 24001L);
                }
            }

            if (Config.get().isTimeNightOnly())
            {
                if (j <= 14000L)
                {
                    this.setWorldTime(i - j + 14001L);
                }

                if (j >= 22000L)
                {
                    this.setWorldTime(i - j + 24000L + 14001L);
                }
            }
        }
    }
}
