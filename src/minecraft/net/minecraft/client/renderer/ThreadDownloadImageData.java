package net.minecraft.client.renderer;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.ClientEngine;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.optifine.*;
import net.minecraft.util.ResourceLocation;

public class ThreadDownloadImageData extends SimpleTexture
{
    private static final Logger logger = LogManager.getLogger();
    private static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);
    private final File cacheFile;
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    private BufferedImage bufferedImage;
    private Thread imageThread;
    private boolean textureUploaded;
    private static final String __OBFID = "CL_00001049";
    public Boolean imageFound = null;
    public boolean pipeline = false;

    public ThreadDownloadImageData(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, IImageBuffer imageBufferIn)
    {
        super(textureResourceLocation);
        this.cacheFile = cacheFileIn;
        this.imageUrl = imageUrlIn;
        this.imageBuffer = imageBufferIn;
    }

    private void checkTextureUploaded()
    {
        if (!this.textureUploaded && this.bufferedImage != null)
        {
            this.textureUploaded = true;

            if (this.textureLocation != null)
            {
                this.deleteGlTexture();
            }

            TextureUtil.get().uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
        }
    }

    public int getGlTextureId()
    {
        this.checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage bufferedImageIn)
    {
        this.bufferedImage = bufferedImageIn;

        if (this.imageBuffer != null)
        {
            this.imageBuffer.skinAvailable();
        }

        this.imageFound = Boolean.valueOf(this.bufferedImage != null);
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        if (this.bufferedImage == null && this.textureLocation != null)
        {
            super.loadTexture(resourceManager);
        }

        if (this.imageThread == null)
        {
            if (this.cacheFile != null && this.cacheFile.isFile())
            {
                try
                {
                    this.bufferedImage = ImageIO.read(this.cacheFile);

                    if (this.imageBuffer != null)
                    {
                        this.setBufferedImage(this.imageBuffer.parseUserSkin(this.bufferedImage));
                    }

                    this.imageFound = Boolean.valueOf(this.bufferedImage != null);
                }
                catch (IOException ioexception)
                {
                    logger.error((String)("Couldn\'t load skin " + this.cacheFile), (Throwable)ioexception);
                    this.loadTextureFromServer();
                }
            }
            else
            {
                this.loadTextureFromServer();
            }
        }
    }

    protected void loadTextureFromServer()
    {
        this.imageThread = new Thread("Texture Downloader #" + threadDownloadCounter.incrementAndGet())
        {
            private static final String __OBFID = "CL_00001050";
            public void run()
            {
                HttpURLConnection connection = null;

                if (shouldPipeline())
                {
                    loadPipelined();
                }

                else
                {
                    try
                    {
                        connection = (HttpURLConnection) new URI(ThreadDownloadImageData.this.imageUrl).toURL().openConnection();
                        connection.setDoInput(true);
                        connection.setDoOutput(false);
                        connection.connect();

                        if (connection.getResponseCode() != 200)
                        {
                            if (connection.getErrorStream() != null)
                            {
                                Config.get().readAll(connection.getErrorStream());
                            }

                            return;
                        }

                        BufferedImage bufferedimage;

                        if (cacheFile != null)
                        {
                            FileUtils.copyInputStreamToFile(connection.getInputStream(), cacheFile);
                            bufferedimage = ImageIO.read(cacheFile);
                        }

                        else
                        {
                            bufferedimage = TextureUtil.get().readBufferedImage(connection.getInputStream());
                        }

                        if (imageBuffer != null)
                        {
                            bufferedimage = imageBuffer.parseUserSkin(bufferedimage);
                        }

                        setBufferedImage(bufferedimage);
                    }

                    catch (Exception exception)
                    {
                        return;
                    }

                    finally
                    {
                        if (connection != null)
                        {
                            connection.disconnect();
                        }

                        imageFound = Boolean.valueOf(bufferedImage != null);
                    }
                }
            }
        };

        this.imageThread.setDaemon(true);
        this.imageThread.start();
    }

    private boolean shouldPipeline()
    {
        if (!this.pipeline)
        {
            return false;
        }

        else
        {
            return this.imageUrl.startsWith("http://");
        }
    }

    private void loadPipelined()
    {
        try
        {
            HttpRequest httprequest = HttpPipeline.makeRequest(this.imageUrl, Proxy.NO_PROXY);
            HttpResponse httpresponse = HttpPipeline.executeRequest(httprequest);

            if (httpresponse.getStatus() != 200)
            {
                return;
            }

            byte[] abyte = httpresponse.getBody();
            ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte);
            BufferedImage bufferedimage;

            if (this.cacheFile != null)
            {
                FileUtils.copyInputStreamToFile(bytearrayinputstream, this.cacheFile);
                bufferedimage = ImageIO.read(this.cacheFile);
            }
            else
            {
                bufferedimage = TextureUtil.get().readBufferedImage(bytearrayinputstream);
            }

            if (this.imageBuffer != null)
            {
                bufferedimage = this.imageBuffer.parseUserSkin(bufferedimage);
            }

            this.setBufferedImage(bufferedimage);
        }
        catch (Exception exception)
        {
            logger.error("Couldn\'t download http texture: " + exception.getClass().getName() + ": " + exception.getMessage());
            return;
        }
        finally
        {
            this.imageFound = Boolean.valueOf(this.bufferedImage != null);
        }
    }
}
