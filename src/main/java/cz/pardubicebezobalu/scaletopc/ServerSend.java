package cz.pardubicebezobalu.scaletopc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerSend {

    static long start = System.currentTimeMillis();

    int lastSendToServer = -1;
    long lastTimeSent = -1;
    String sendToServer(int digits) {
        StringBuffer response = null;
        try {
            boolean shouldRefresh
                    = (lastTimeSent==-1) ||
                    ((System.currentTimeMillis()-lastTimeSent)>2000);
            if (!shouldRefresh && lastSendToServer==digits) {
                    return digits + " not sent";
            }
            lastSendToServer = digits;
            lastTimeSent = System.currentTimeMillis();
             
            String url = "http://www.pardubicebezobalu.cz/vaha.php?vaha="+digits;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

// optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\n"+ secondsFromStart() + "Sending 'GET' request to URL : " + url + " " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            System.out.println("Error sending " + digits + " to server, please check if WIFI/internet is ON: " + e.getMessage());
            return "-1";
        }
//print result
        return response.toString();
    }

    public static String secondsFromStart() {
        return (System.currentTimeMillis() - start) / 1000 + " sec: ";
    }

}