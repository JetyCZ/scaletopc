package cz.pardubicebezobalu.scaletopc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerSend {

    static long start = System.currentTimeMillis();

    int lastSendToServer = -1;
    long lastTimeSent = -1;
    boolean sendToServer(int digits) {
        StringBuffer response = null;
        BufferedReader in = null;
        try {
            long thisTimeSent = System.currentTimeMillis();
            long timeFromLastSend = thisTimeSent - lastTimeSent;
            boolean dataDifferFromLast = digits != lastSendToServer;
            boolean shouldSendNewData
                    = (lastTimeSent==-1) ||
                    (timeFromLastSend >5000) ||
                    dataDifferFromLast;

            if (!dataDifferFromLast) {
                if (!shouldSendNewData && timeFromLastSend<1000) {
                    return false;
                }
                if (!shouldSendNewData && lastSendToServer==digits) {
                    // return "not sent, same request " + digits + " send " + ((int) timeFromLastSend/1000) + " seconds ago";
                    return false;
                }
            }

            lastSendToServer = digits;

            String url = "http://www.pardubicebezobalu.cz/admin313uriemy/vaha.php?vaha="+digits + "&scale_date="+thisTimeSent
                    + "&date="+thisTimeSent;
            lastTimeSent = thisTimeSent;
            // System.out.println(url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

// optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();

            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            if (!"OK".equals(response.toString())) {
                System.out.println("ERROR updating on server: " + digits);
                System.out.println(response);
                return false;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error sending " + digits + " to server, please check if WIFI/internet is ON: " + e.getMessage());
        } finally {
            if (in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ok
                }
            }
        }
//print result
        return true;
    }

    public static String secondsFromStart() {
        return (System.currentTimeMillis() - start) / 1000 + " sec: ";
    }

}