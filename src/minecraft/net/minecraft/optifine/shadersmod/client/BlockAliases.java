package net.minecraft.optifine.shadersmod.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.minecraft.optifine.*;

public class BlockAliases
{
    private static BlockAlias[][] blockAliases = (BlockAlias[][])null;

    public static int getMappedBlockId(int blockId, int metadata)
    {
        if (blockAliases == null)
        {
            return blockId;
        }
        else if (blockId >= 0 && blockId < blockAliases.length)
        {
            BlockAlias[] ablockalias = blockAliases[blockId];

            if (ablockalias == null)
            {
                return blockId;
            }
            else
            {
                for (int i = 0; i < ablockalias.length; ++i)
                {
                    BlockAlias blockalias = ablockalias[i];

                    if (blockalias.matches(blockId, metadata))
                    {
                        return blockalias.getBlockId();
                    }
                }

                return blockId;
            }
        }
        else
        {
            return blockId;
        }
    }

    public static void update(IShaderPack shaderPack)
    {
        reset();
        String s = "/shaders/block.properties";

        try
        {
            InputStream inputstream = shaderPack.getResourceAsStream(s);

            if (inputstream == null)
            {
                return;
            }

            Properties properties = new PropertiesOrdered();
            properties.load(inputstream);
            inputstream.close();
            List<List<BlockAlias>> list = new ArrayList();
            ConnectedParser connectedparser = new ConnectedParser("Shaders");

            for (Object s10 : properties.keySet())
            {
            	String s1 =(String) s10;
                String s2 = properties.getProperty(s1);
                String s3 = "block.";

                if (!s1.startsWith(s3))
                {
                    Config.get().warn("[Shaders] Invalid block ID: " + s1);
                }
                else
                {
                    String s4 = StrUtils.removePrefix(s1, s3);
                    int i = Config.get().parseInt(s4, -1);

                    if (i < 0)
                    {
                        Config.get().warn("[Shaders] Invalid block ID: " + s1);
                    }
                    else
                    {
                        MatchBlock[] amatchblock = connectedparser.parseMatchBlocks(s2);

                        if (amatchblock != null && amatchblock.length >= 1)
                        {
                            BlockAlias blockalias = new BlockAlias(i, amatchblock);
                            addToList(list, blockalias);
                        }
                        else
                        {
                            Config.get().warn("[Shaders] Invalid block ID mapping: " + s1 + "=" + s2);
                        }
                    }
                }
            }

            if (list.size() <= 0)
            {
                return;
            }

            blockAliases = toArrays(list);
        }
        catch (IOException var15)
        {
            Config.get().warn("[Shaders] Error reading: " + s);
        }
    }

    private static void addToList(List<List<BlockAlias>> blocksAliases, BlockAlias ba)
    {
        int[] aint = ba.getMatchBlockIds();

        for (int i = 0; i < aint.length; ++i)
        {
            int j = aint[i];

            while (j >= blocksAliases.size())
            {
                blocksAliases.add(null);
            }

            List<BlockAlias> list = (List)blocksAliases.get(j);

            if (list == null)
            {
                list = new ArrayList();
                blocksAliases.set(j, list);
            }

            list.add(ba);
        }
    }

    private static BlockAlias[][] toArrays(List<List<BlockAlias>> listBlocksAliases)
    {
        BlockAlias[][] ablockalias = new BlockAlias[listBlocksAliases.size()][];

        for (int i = 0; i < ablockalias.length; ++i)
        {
            List<BlockAlias> list = (List)listBlocksAliases.get(i);

            if (list != null)
            {
                ablockalias[i] = (BlockAlias[])((BlockAlias[])list.toArray(new BlockAlias[list.size()]));
            }
        }

        return ablockalias;
    }

    public static void reset()
    {
        blockAliases = (BlockAlias[][])null;
    }
}
