package net.minecraft.optifine;

import net.minecraft.client.ClientEngine;

public class FileDownloadThread extends Thread
{
    private String urlString = null;
    private IFileDownloadListener listener = null;
    private ClientEngine mc;

    public FileDownloadThread(String p_i41_1_, IFileDownloadListener p_i41_2_, ClientEngine mc)
    {
        this.urlString = p_i41_1_;
        this.listener = p_i41_2_;
        this.mc = mc;
    }

    public void run()
    {
        try
        {
            byte[] abyte = HttpPipeline.get(this.urlString, mc.getProxy());
            this.listener.fileDownloadFinished(this.urlString, abyte, (Throwable)null);
        }
        catch (Exception exception)
        {
            this.listener.fileDownloadFinished(this.urlString, (byte[])null, exception);
        }
    }

    public String getUrlString()
    {
        return this.urlString;
    }

    public IFileDownloadListener getListener()
    {
        return this.listener;
    }
}
