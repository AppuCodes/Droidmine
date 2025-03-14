package net.minecraft.client.resources;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceIndex
{
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, File> resourceMap = Maps.<String, File>newHashMap();

    public ResourceIndex(File p_i1047_1_, String p_i1047_2_) {}

    public Map<String, File> getResourceMap()
    {
        return this.resourceMap;
    }
}
