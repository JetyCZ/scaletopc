package cz.pardubicebezobalu.scaletopc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerSend {

    static long start = System.currentTimeMillis();

    int lastSendToServer = -1;
    long lastTimeSent = -1;
    boolean sendToServer(int digits) {
        StringBuffer response = null;
        try {
            long timeFromLastSend = System.currentTimeMillis() - lastTimeSent;
            boolean shouldRefresh
                    = (lastTimeSent==-1) ||
                    (timeFromLastSend >5000);
            if (!shouldRefresh && lastSendToServer==digits) {
                    // return "not sent, same request " + digits + " send " + ((int) timeFromLastSend/1000) + " seconds ago";
                    return false;
            }
            lastSendToServer = digits;
            lastTimeSent = System.currentTimeMillis();
             
            String url = "http://www.pardubicebezobalu.cz/admin313uriemy/vaha.php?vaha="+digits;
            // System.out.println(url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

// optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            throw new IllegalStateException("Error sending " + digits + " to server, please check if WIFI/internet is ON: " + e.getMessage());
        }
//print result
        return true;
    }

    public static String secondsFromStart() {
        return (System.currentTimeMillis() - start) / 1000 + " sec: ";
    }

}