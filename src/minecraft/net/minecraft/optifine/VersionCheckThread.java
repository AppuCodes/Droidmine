package net.minecraft.optifine;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import net.minecraft.client.ClientBrandRetriever;

public class VersionCheckThread extends Thread
{
    public void run()
    {
//        HttpURLConnection httpurlconnection = null;
//
//        try
//        {
//            URL url = new URI("http://optifine.net/version/1.8.9/HD_U.txt").toURL();
//            httpurlconnection = (HttpURLConnection)url.openConnection();
//
//            if (Config.get().getoptions().snooperEnabled)
//            {
//                httpurlconnection.setRequestProperty("OF-MC-Version", "1.8.9");
//                httpurlconnection.setRequestProperty("OF-MC-Brand", "" + ClientBrandRetriever.getClientModName());
//                httpurlconnection.setRequestProperty("OF-Edition", "HD_U");
//                httpurlconnection.setRequestProperty("OF-Release", "H8");
//                httpurlconnection.setRequestProperty("OF-Java-Version", "" + System.getProperty("java.version"));
//                httpurlconnection.setRequestProperty("OF-CpuCount", "" + Config.get().getAvailableProcessors());
//                httpurlconnection.setRequestProperty("OF-OpenGL-Version", "" + Config.get().openGlVersion);
//                httpurlconnection.setRequestProperty("OF-OpenGL-Vendor", "" + Config.get().openGlVendor);
//            }
//
//            httpurlconnection.setDoInput(true);
//            httpurlconnection.setDoOutput(false);
//            httpurlconnection.connect();
//
//            try
//            {
//                InputStream inputstream = httpurlconnection.getInputStream();
//                inputstream.close();
//            }
//            finally
//            {
//                if (httpurlconnection != null)
//                {
//                    httpurlconnection.disconnect();
//                }
//            }
//        }
//        catch (Exception exception)
//        {
//            Config.get().dbg(exception.getClass().getName() + ": " + exception.getMessage());
//        }
    }
}
