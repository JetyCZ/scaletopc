import com.fazecast.jSerialComm.SerialPort;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PortReader {
    public static void main(String[] args) {
        SerialPort comPort = getSerialPort(args);

        InputStream inputStream = null;

        InputStream finalInputStream = inputStream;
        Signal.handle(new Signal("INT"), new SignalHandler() {
            // Signal handler method
            public void handle(Signal signal) {
                try {
                    System.out.println("Closing input stream...");
                    finalInputStream.close();
                    System.out.println("Closing input stream OK...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                comPort.closePort();
            }
        });

        try
        {
            try {
                comPort.openPort();
                inputStream = comPort.getInputStream();
                while(true) {
                    readBytes(inputStream);
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SerialPort getSerialPort(String[] args) {
        SerialPort[] commPorts = SerialPort.getCommPorts();
        int idx = 0;
        for (SerialPort commPort : commPorts) {
            System.out.println((idx++)+ " PORT: " + commPort.getDescriptivePortName() +", " + commPort.getSystemPortName());
        }
        SerialPort comPort = commPorts[1];
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        comPort.setBaudRate(9600);
        comPort.setNumDataBits(7);
        comPort.setNumStopBits(1);
        comPort.setParity(SerialPort.EVEN_PARITY);
        return comPort;
    }


    private static boolean readBytes(InputStream inputStream) throws IOException, InterruptedException {
        try {
            byte[] readBuffer = inputStream.readNBytes(38*2);
            int byteIdx = -1;
            for (byte one : readBuffer) {
                byteIdx++;
                if (one == 0xA) {
                    // @C␍000.000␍400.000␍U0000.0␍T00000.0␍␊
                    // ￀ￃﾍ000.000ﾍﾴ00.000ﾍU0000.0ﾍￔ00000.0ﾍ
                    byte[] read = subArray(readBuffer, byteIdx + 1, byteIdx + 37);

/*
40 42 	@B
0d 30 30 32 2e 33 36 36   0d 34 30 30 2e 30 30 30 	␍<tab>002.366<tab>400.000
0d 55 30 30 30 30 2e 30   0d 54 30 30 30 30 30 2e 	␍<tab>U0000.0<tab>T00000.
30 0d 0a                                          	0␍␊␊
[07:10:55:099] @B␍002.366␍400.000␍U0000.0␍T00000.0␍␊
*/

                    List a = new ArrayList<>();
                    int bIdx = 0;
                    for (byte b : read) {
                        a.add(bIdx + ":  " + String.format("%2x",b) + " ... "  + " ... " + Integer.toString(b,2) + " ... " + b + " ... " + (char)(b));
                        bIdx++;
                    }
/*
‘0’ Net Weight 0x30
‘4’ Tare Weight 0x34
‘U’ Unit Price 0x55
‘T’ Total Price 0x54
*/
                    List r = new ArrayList();
                    byte statusFlag = read[0];
                    byte weightCondition = read[1];

                    int netWeight = toWeight(read, 4);
                    int tareWeight = toWeight(read, 12);
                    int unitPrice = toUnitPrice(read, 20);
                    int totalPrice = toTotalPrice(read, 28);
                    System.out.println("NET\t"+netWeight + "\tTare\t"+tareWeight+"\tUnit"+unitPrice+"\tTOT\t"+totalPrice);
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return false;
        }

    }

    private static int toWeight(byte[] read, int startIdx) {
        try {
            String firstDigit = firstDigit(read[startIdx]);
            String numberStr = firstDigit +
                    String.format("%2x", read[startIdx+1]).substring(1) +
                    String.format("%2x", read[startIdx+3]).substring(1) +
                    String.format("%2x", read[startIdx+4]).substring(1) +
                    String.format("%2x", read[startIdx+5]).substring(1);
            int weight = Integer.valueOf(
                    numberStr
            );
            return weight;
        } catch (Exception e) {
            return -1;
        }
    }

    private static String firstDigit(byte b) {
        String firstDigitHex = String.format("%2x", b);
        String firstDigit;
        if ("2d".equals(firstDigitHex)) {
            firstDigit = "-";
        } else {
            firstDigit = firstDigitHex.substring(1);
        }
        return firstDigit;
    }

    private static int toUnitPrice(byte[] read, int startIdx) {
        try {
            String format = "%2x";
            String numberStr = String.format(format, read[startIdx]).substring(1) +
                    String.format(format, read[startIdx+1]).substring(1) +
                    String.format(format, read[startIdx+2]).substring(1) +
                    String.format(format, read[startIdx+3]).substring(1) +
                    String.format(format, read[startIdx+5]).substring(1);

            int price = Integer.valueOf(
                    numberStr
            );
            return price;
        } catch (Exception e) {
            return -1;
        }
    }

    private static int toTotalPrice(byte[] read, int startIdx) {
        try {
            String format = "%2x";
            String numberStr = String.format(format, read[startIdx]).substring(1) +
                    String.format(format, read[startIdx+1]).substring(1) +
                    String.format(format, read[startIdx+2]).substring(1) +
                    String.format(format, read[startIdx+3]).substring(1) +
                    String.format(format, read[startIdx+4]).substring(1) +
                    String.format(format, read[startIdx+6]).substring(1);

            int price = Integer.valueOf(
                    numberStr
            );
            return price;
        } catch (Exception e) {
            return -1;
        }
    }

    public static byte[] subArray(byte[] array, int beg, int end) {
        return Arrays.copyOfRange(array, beg, end + 1);
    }
}
