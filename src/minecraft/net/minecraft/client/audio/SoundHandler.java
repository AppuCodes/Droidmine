package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import net.minecraft.client.options.GameOptions;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoundHandler implements IResourceManagerReloadListener, ITickable
{
    private static final Logger logger = LogManager.getLogger();
    private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(SoundList.class, new SoundListSerializer()).create();
    private static final ParameterizedType TYPE = new ParameterizedType()
    {
        public Type[] getActualTypeArguments()
        {
            return new Type[] {String.class, SoundList.class};
        }
        public Type getRawType()
        {
            return Map.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };
    public static final SoundPoolEntry missing_sound = new SoundPoolEntry(new ResourceLocation("meta:missing_sound"), 0.0D, 0.0D, false);
    private final SoundRegistry sndRegistry = new SoundRegistry();
    private final IResourceManager mcResourceManager;

    public SoundHandler(IResourceManager manager, GameOptions optionsIn)
    {
        this.mcResourceManager = manager;
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
    }

    protected Map<String, SoundList> getSoundMap(InputStream stream)
    {
        Map map;

        try
        {
            map = (Map)GSON.fromJson((Reader)(new InputStreamReader(stream)), TYPE);
        }
        finally
        {
            IOUtils.closeQuietly(stream);
        }

        return map;
    }

    private void loadSoundResource(ResourceLocation location, SoundList sounds)
    {
    }

    public SoundEventAccessorComposite getSound(ResourceLocation location)
    {
        return (SoundEventAccessorComposite)this.sndRegistry.getObject(location);
    }

    /**
     * Play a sound
     */
    public void playSound(ISound sound)
    {
    }

    /**
     * Plays the sound in n ticks
     */
    public void playDelayedSound(ISound sound, int delay)
    {
    }

    public void setListener(EntityPlayer player, float p_147691_2_)
    {
    }

    public void pauseSounds()
    {
    }

    public void stopSounds()
    {
    }

    public void unloadSounds()
    {
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update()
    {
    }

    public void resumeSounds()
    {
    }

    public void setSoundLevel(SoundCategory category, float volume)
    {
        if (category == SoundCategory.MASTER && volume <= 0.0F)
        {
            this.stopSounds();
        }
    }

    public void stopSound(ISound p_147683_1_)
    {
    }

    /**
     * Returns a random sound from one or more categories
     */
    public SoundEventAccessorComposite getRandomSoundFromCategories(SoundCategory... categories)
    {
        List<SoundEventAccessorComposite> list = Lists.<SoundEventAccessorComposite>newArrayList();

        for (ResourceLocation resourcelocation : this.sndRegistry.getKeys())
        {
            SoundEventAccessorComposite soundeventaccessorcomposite = (SoundEventAccessorComposite)this.sndRegistry.getObject(resourcelocation);

            if (ArrayUtils.contains(categories, soundeventaccessorcomposite.getSoundCategory()))
            {
                list.add(soundeventaccessorcomposite);
            }
        }

        if (list.isEmpty())
        {
            return null;
        }
        else
        {
            return (SoundEventAccessorComposite)list.get((new Random()).nextInt(list.size()));
        }
    }

    public boolean isSoundPlaying(ISound sound)
    {
        return true;
    }
}
