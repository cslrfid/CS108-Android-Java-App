package com.csl.cs108ademoapp;

import static android.content.Context.WIFI_SERVICE;
import static com.csl.cs108ademoapp.MainActivity.csLibrary4A;
import static com.csl.cs108ademoapp.MainActivity.mContext;

import android.bluetooth.BluetoothAdapter;
import android.media.MediaScannerConnection;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.csl.cslibrary4a.ReaderDevice;
import com.csl.cslibrary4a.RfidReaderChipData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SaveList2ExternalTask extends AsyncTask<Void,Void,String> {
    public String messageStr;
    String resultDisplay = "";
    public String errorDisplay;
    ArrayList<ReaderDevice> tagsList; ReaderDevice tagDevice1;
    CustomPopupWindow customPopupWindow;
    boolean savedFile = false;
    int fileFormat = 0;

    public String url = null, strBearer = null;
    HttpURLConnection con;
    String stringBluetoothMAC, stringWifiMac;

    public SaveList2ExternalTask(ArrayList<ReaderDevice> tagsList) {
        this.tagsList = tagsList;

        stringBluetoothMAC = BluetoothAdapter.getDefaultAdapter().getAddress().replaceAll(":", "");
        csLibrary4A.appendToLog("stringBluetoothMac from getMacAddress = " + stringBluetoothMAC);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) { }
        else if (stringBluetoothMAC.contains("020000000000")) {
            final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
            String macAddress = Settings.Secure.getString(mContext.getContentResolver(), SECURE_SETTINGS_BLUETOOTH_ADDRESS); //Not OK in android 8, >= 32
            csLibrary4A.appendToLog("stringBluetoothMac from Settings.Secure.getString = " + macAddress);
            stringBluetoothMAC = macAddress;
        }

        stringWifiMac = ((WifiManager) MainActivity.mContext.getSystemService(WIFI_SERVICE)).getConnectionInfo().getMacAddress().replaceAll(":", "");
        csLibrary4A.appendToLog("stringWifMac from getMacAddress = " + stringWifiMac);
        if (stringWifiMac.contains("020000000000")) {
            try {
                List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : all) {
                    csLibrary4A.appendToLog("nif.getName = " + nif.getName() + ", macByts = " + csLibrary4A.byteArrayToString(nif.getHardwareAddress()));
                }

                WifiManager wifiMan = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
                int wifiState = wifiMan.getWifiState();
                //wifiMan.setWifiEnabled(true);
                File fl = new File("/sys/class/net/wlan0/address");
                FileInputStream fin = new FileInputStream(fl);  //Not Ok in Amdroid 11
                StringBuilder builder = new StringBuilder();
                int ch;
                while ((ch = fin.read()) != -1) {
                    builder.append((char) ch);
                }
                String fileMAC = builder.toString();
                csLibrary4A.appendToLog("getName: file content = " + fileMAC);
                fin.close();

                stringWifiMac = fileMAC;
                //boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
                //wifiMan.setWifiEnabled(enabled);
            } catch (Exception ex) {
                csLibrary4A.appendToLog("Exception : " + ex.getCause());
            }
        }
    }
    boolean bImpinjServer = false;
    public SaveList2ExternalTask(boolean bImpinjServer) {
        csLibrary4A.appendToLog("bImpinjServer = " + bImpinjServer);
        this.bImpinjServer = bImpinjServer;
    }

    @Override
    protected void onPreExecute() {
        if (!bImpinjServer) {
            if (tagsList == null) cancel(true);
            if (MainActivity.csLibrary4A.getSavingFormatSetting() == 0)
                messageStr = createJSON(tagsList, null).toString();
            else messageStr = createCSV(tagsList, null);
            resultDisplay = save2File(messageStr, true);
        }
        customPopupWindow = new CustomPopupWindow(mContext);
        csLibrary4A.appendToLog("SaveList2ExternalTask: resultDisplay = " + resultDisplay);
        if (resultDisplay == null) resultDisplay = "";
        else {
            resultDisplay += "\n";
            savedFile = true;
            customPopupWindow.popupStart(resultDisplay + "Connecting server. Please wait.", true);
            csLibrary4A.appendToLog("SaveList2ExternalTask: popupStart is done");
        }
    }

    protected String doInBackground(Void... params) {
        Log.i("Hello", "bImpinjServer: doInBackground starts");
        if (MainActivity.csLibrary4A.isBleConnected() == false) {
            resultDisplay += "Error in sending data to server as the reader is not connected";
            return null;
        } else if (MainActivity.csLibrary4A.getSaveCloudEnable() == false && bImpinjServer == false) {
            resultDisplay += "No saving to cloud as it is disabled";
            return null;
        }
        try {
            if (true) {
                csLibrary4A.appendToLog("bImpinjServer: doInBackground starts doing");
                openServer(bImpinjServer); csLibrary4A.appendToLog("bImpinjServer: doInBackground after openServer");
                write2Server(messageStr); csLibrary4A.appendToLog("bImpinjServer: doInBackground after write2Server");
                closeServer(); csLibrary4A.appendToLog("bImpinjServer: doInBackground after closeServer");
            } else if (true) {
                String serverAddress = "192.168.25.119:21";
                String addressPort[];
                InetAddress serverAddr = null;
                int serverPort = 0;
                boolean validLocation = false;
                if (serverAddress != null) {
                    addressPort = serverAddress.trim().split(":");
                    if (addressPort.length == 2) {
                        try {
                            serverAddr = InetAddress.getByName(addressPort[0]);
                            serverPort = Integer.decode(addressPort[1]);
                            validLocation = true;
                        } catch (Exception ex) { }
                    }
                }
                if (validLocation == false) resultDisplay += "Error in creating server location from " + csLibrary4A.getServerLocation() + " !!!";
                else {
                    DatagramSocket udpSocket = new DatagramSocket();
                    byte[] buf = messageStr.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, serverPort);
                    udpSocket.send(packet);
                    resultDisplay += "Success in sending data to " + csLibrary4A.getServerLocation();
                }
            }
        } catch (Exception e) {
            errorDisplay += ": " + e.getMessage() + "\n";
        }
        if (errorDisplay != null) resultDisplay += "Error in sending data to server " + (url != null ? url : "") + " with " + errorDisplay;
        while (MainActivity.permissionRequesting) { }
        return null;
    }
    protected void onProgressUpdate(Void... output) { }
    protected void onCancelled() { }
    protected void onPostExecute(String output) {
        if (savedFile == false) resultDisplay += "\n" + save2File(messageStr, false);
        customPopupWindow.popupWindow.dismiss();
        csLibrary4A.appendToLog("bImpinjServer = " + bImpinjServer + ", responseCode = " + responseCode + ", resultDisplay = " + resultDisplay);
        if (resultDisplay != null && resultDisplay.length() != 0) {
            if (!bImpinjServer || responseCode != 200) customPopupWindow.popupStart(resultDisplay, false);
        }
    }

    public String createStrEpcList() {
        String stringOutput = "";
        if (tagsList != null) {
            for (int i = 0; i < tagsList.size(); i++) {
                ReaderDevice tagDevice = tagsList.get(i);
                if (tagDevice.getAddress() != null) stringOutput += tagDevice.getAddress() + "\n";
            }
        }
        return stringOutput;
    }

    int sequenceNumber;
    public JSONObject createJSON(ArrayList<ReaderDevice> tagsList0, ReaderDevice tagDevice0) {
        JSONObject object = new JSONObject();
        try {
            object.put("sequenceNumber", sequenceNumber++);

            if (tagsList0 != null || tagDevice0 != null) {
                JSONArray jsonArray = new JSONArray();
                int i = 1; if (tagsList0 != null) i = tagsList0.size();
                object.put("numberOfTags", i);
                while (--i >= 0) {
                    ReaderDevice tagDevice;
                    if (tagsList0 != null) tagDevice = tagsList0.get(i);
                    else tagDevice = tagDevice0;

                    String accessPassword = null, killPassword = null, pcData = null, epcData = null, resBankData = null, epcBankData = null, tidBankData = null, userBankData = null;
                    String timeOfRead = null, timeZone = null, location = null, compass = null;
                    int phase = -1, channel = -1;
                    if (tagDevice != null) {
                        pcData = tagDevice.getPc();
                        epcData = tagDevice.getAddress();
                        resBankData = tagDevice.getRes();
                        epcBankData = tagDevice.getEpc();
                        tidBankData = tagDevice.getTid();
                        userBankData = tagDevice.getUser();
                        timeOfRead = tagDevice.getTimeOfRead();
                        timeZone = tagDevice.getTimeZone();
                        location = tagDevice.getLocation();
                        compass = tagDevice.getCompass();
                        phase = tagDevice.getPhase();
                        channel = tagDevice.getChannel();
                    }

                    JSONObject objectTag = new JSONObject();

                    objectTag.put("accessPassword", accessPassword);
                    objectTag.put("killPassword", killPassword);

                    objectTag.put("pc", pcData);
                    objectTag.put("epc", epcData);

                    objectTag.put("resBank", resBankData);
                    objectTag.put("epcBank", epcBankData);
                    objectTag.put("tidBank", tidBankData);
                    objectTag.put("userBank", userBankData);
                    if (phase != -1) objectTag.put("phase", phase);
                    if (channel != -1) objectTag.put("channel", channel);

                    objectTag.put("timeOfRead", timeOfRead);
                    objectTag.put("timeZone", timeZone);
                    objectTag.put("locationOfRead", location);
                    objectTag.put("eCompass", compass);

                    objectTag.put("antennaPort", "0");
                    jsonArray.put(objectTag);
                }
                object.put("tags", jsonArray);
            }

            object.put("userDescription","this is example tag data");

            object.put("rfidReaderName", MainActivity.csLibrary4A.getBluetoothICFirmwareName());
            object.put("rfidReaderSerialNumber", MainActivity.csLibrary4A.getHostProcessorICSerialNumber());
            object.put("rfidReaderInternalSerialNumber", MainActivity.csLibrary4A.getRadioSerial());

            object.put("smartPhoneName", Build.MODEL);
            String strPhoneSerial = Build.SERIAL;
            object.put("smartPhoneSerialNumber", strPhoneSerial);
            object.put("smartPhoneBluetoothMACAddress",  stringBluetoothMAC);
            object.put("smartPhoneWiFiMACAddress", stringWifiMac);

            object.put("smartPhoneUUID", null);
            object.put("pcName", null);
            object.put("pcEthernetMACAddress", null);
            object.put("pcWiFiMACAddress", null);

            object.put("operatorId","Generic Operator");
            object.put("operatorSiteId","Generic Site");
        } catch (Exception ex) { }
        return object;
    }

    public String createCSV(ArrayList<ReaderDevice> tagsList0, ReaderDevice tagDevice0) {
        String object = "";
        int csvColumnSelect = MainActivity.csLibrary4A.getCsvColumnSelectSetting();
        try {
            if (tagsList0 != null || tagDevice0 != null) {
                JSONArray jsonArray = new JSONArray();
                int i = 1; if (tagsList0 != null) i = tagsList0.size();
                if (true) {
                    String objectTag;
                    objectTag = "PC,";
                    objectTag += "EPC,";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.RESERVE_BANK.ordinal())) != 0) objectTag += "Reserve Bank,";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.EPC_BANK.ordinal())) != 0) objectTag += "EPC Bank,";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.TID_BANK.ordinal())) != 0) objectTag += "TID Bank,";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.USER_BANK.ordinal())) != 0) objectTag += "User Bank,";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.PHASE.ordinal())) != 0) objectTag += "Phase,";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.CHANNEL.ordinal())) != 0) objectTag += "Channel,";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.TIME.ordinal())) != 0) objectTag += "Time Of Read,";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.TIMEZONE.ordinal())) != 0) objectTag += "Time Zone,";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.LOCATION.ordinal())) != 0) objectTag += "location Of Read Latitude, Location of Read Longitude, ";
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.DIRECTION.ordinal())) != 0) objectTag += "eCompass";
                    objectTag += "\n";
                    object += objectTag;
                }

                while (--i >= 0) {
                    ReaderDevice tagDevice;
                    if (tagsList0 != null) tagDevice = tagsList0.get(i);
                    else tagDevice = tagDevice0;

                    String accessPassword = null, killPassword = null, pcData = null, epcData = null, resBankData = null, epcBankData = null, tidBankData = null, userBankData = null;
                    String timeOfRead = null, timeZone = null, location = null, compass = null;
                    int phase = -1, channel = -1;
                    if (tagDevice != null) {
                        pcData = tagDevice.getPc();
                        epcData = tagDevice.getAddress();
                        resBankData = tagDevice.getRes();
                        epcBankData = tagDevice.getEpc();
                        tidBankData = tagDevice.getTid();
                        userBankData = tagDevice.getUser();
                        timeOfRead = tagDevice.getTimeOfRead(); MainActivity.csLibrary4A.appendToLog("timeOfRead = " + timeOfRead );
                        if (false) {
                            int index = timeOfRead.indexOf(".");
                            if (index >= 0) {
                                String string1 = timeOfRead.substring(0, index);
                                timeOfRead = string1;
                                MainActivity.csLibrary4A.appendToLog("index = " + index + ", revised timeOfRead = " + timeOfRead );
                            }
                        }
                        timeZone = tagDevice.getTimeZone();
                        location = tagDevice.getLocation();
                        compass = tagDevice.getCompass();
                        phase = tagDevice.getPhase();
                        channel = tagDevice.getChannel();
                    }

                    String objectTag;
                    objectTag = String.format("=\"%s\",", pcData);
                    objectTag += String.format("=\"%s\",", epcData);

                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.RESERVE_BANK.ordinal())) != 0) objectTag += String.format("=\"%s\",", resBankData);
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.EPC_BANK.ordinal())) != 0) objectTag += String.format("=\"%s\",", epcBankData);
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.TID_BANK.ordinal())) != 0) objectTag += String.format("=\"%s\",", tidBankData);
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.USER_BANK.ordinal())) != 0) objectTag += String.format("=\"%s\",", userBankData);
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.PHASE.ordinal())) != 0) objectTag += String.format("%d,", phase);
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.CHANNEL.ordinal())) != 0) objectTag += String.format("%d,", channel);
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.TIME.ordinal())) != 0) objectTag += String.format("=\"%s\",", timeOfRead);
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.TIMEZONE.ordinal())) != 0) objectTag += String.format("%s,", timeZone);
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.LOCATION.ordinal())) != 0) objectTag += String.format("%s,", location);
                    if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.DIRECTION.ordinal())) != 0)objectTag += String.format("%s", compass);
                    objectTag += "\n";
                    object += objectTag;
                }

                if ((csvColumnSelect & (0x01 << RfidReaderChipData.CsvColumn.OTHERS.ordinal())) != 0) {
                    object += "\nUser Description,this is example tag data\n";

                    object += String.format("RFID Reader Name,=\"%s\"\n", MainActivity.csLibrary4A.getBluetoothICFirmwareName());
                    object += String.format("RFID Reader Serial Number,=\"%s\"\n", MainActivity.csLibrary4A.getHostProcessorICSerialNumber());
                    object += String.format("RFID Reader Radio Serial Number,=\"%s\"\n", MainActivity.csLibrary4A.getRadioSerial());
                    if (true) {
                        object += String.format("RFID Reader Barcode Serial Number,=\"%s\"\n", MainActivity.csLibrary4A.getBarcodeSerial());
                        object += String.format("RFID Reader Bluetooth MAC address,=\"%s\"\n", MainActivity.csLibrary4A.getBluetoothDeviceAddress());
                    }
                    object += String.format("Smart Phone Name,=\"%s\"\n", Build.MODEL);
                }
            }
        } catch (Exception ex) { }
        return object;
    }

    public String save2File(String messageStr, boolean requestPermission) {
        String resultDisplay = "";
        if (MainActivity.csLibrary4A.getSaveFileEnable() == false) return "No saving file as it is disabled";
        boolean writeExtPermission = true;
/*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                csLibrary4A.appendToLog("WRITE_EXTERNAL_STORAGE Permission is required !!!");
                writeExtPermission = false;
                if (requestPermission) {
                    csLibrary4A.appendToLog("requestPermissions WRITE_EXTERNAL_STORAGE 1");
                    MainActivity.permissionRequesting = true; requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    if (false) Toast.makeText(mContext, R.string.toast_permission_not_granted, Toast.LENGTH_SHORT).show();
                    return null;
                }
            } else csLibrary4A.appendToLog("WRITE_EXTERNAL_STORAGE Permission is GRANTED !!!");
        }
*/
        errorDisplay = null;
        if (writeExtPermission == false) {
            errorDisplay = "denied WRITE_EXTERNAL_STORAGE Permission !!!";
        } else if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) == false) errorDisplay = "Error in mouting external storage !!!";
        else {
            String strDir = "cs108Java";
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + strDir);
            if (path.exists() == false) path.mkdirs();
            if (path.exists() == false) errorDisplay = "Error in making directory !!!";
            else {
                String dateTime = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
                String fileName = strDir + "_" + dateTime + (csLibrary4A.getSavingFormatSetting() == 0 ? ".txt" : ".csv");
                File file = new File(path, fileName);
                if (file == null) errorDisplay = "Error in making directory !!!";
                else {
                    try {
                        errorDisplay = "Error in FileOutputStream()";
                        FileOutputStream outputStream = new FileOutputStream(file);
                        errorDisplay = "Error in write()";
                        outputStream.write(messageStr.getBytes());
                        errorDisplay = "Error in close()"; outputStream.close();
                        MediaScannerConnection.scanFile(mContext, new String[]{file.getAbsolutePath()}, null, null);
                        resultDisplay = "Success in saving data to Download/" + strDir + "/" + fileName;
                        errorDisplay = null;
                    } catch (Exception ex) {
                        errorDisplay += ex.getMessage();
                    }
                }
            }
        }
        if (errorDisplay != null) return "Error in saving file with " + errorDisplay;
        return resultDisplay;
    }

    public void openServer(boolean bImpinjServer) throws Exception {
        if (false) {
            url = "https://";
            url += "192.168.25.21:";
            url += "29090/WebServiceRESTs/1.0/req/";
        } else if (false) url = "http://ptsv2.com/t/10i1t-1519143332/post";
        else if (bImpinjServer) { } //url = "https://h9tqczg9-7275.asse.devtunnels.ms/api/Auth/login"; //"https://142.251.220.110"; //
        else url = csLibrary4A.getServerLocation();

        errorDisplay = "Error in SSLContext.getInstance()"; SSLContext sc = SSLContext.getInstance("TLS");
        errorDisplay = "Error in SSLContext.init()"; sc.init(null, new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        throw new UnsupportedOperationException("TrustManager.checkClientTrusted: Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        }, new SecureRandom());
        if (true) {
            errorDisplay = "Error in setDefaultSSLSocketFactory()"; HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            errorDisplay = "Error in setDefaultHostnameVerifier()"; HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                    return true;
                }
            });
        }
        HttpsURLConnection.setFollowRedirects(false);

        errorDisplay = "Error in URL()"; URL obj = new URL(url); MainActivity.csLibrary4A.appendToLog("obj is " + (obj != null ? "valid" : "NULL") + " with url = " + url);
        errorDisplay = "Error in openConnection()";
        boolean isHttps = false;
        if (url.length() >= 6) {
            if (url.substring(0, 6).matches("https:")) isHttps = true;
        }
        con = (HttpURLConnection) obj.openConnection();
        if (isHttps) {
            con = (HttpsURLConnection) obj.openConnection();
        }
        errorDisplay = "Error in setConnectTimeout()"; con.setConnectTimeout(MainActivity.csLibrary4A.getServerTimeout() * 1000);
        errorDisplay = "Error in setRequestMethod()"; con.setRequestMethod("POST");
        //errorDisplay = "Error in setRequestProperty(User-Agent)"; con.setRequestProperty("User-Agent", "Mozilla/5.0");
        //errorDisplay = "Error in setRequestProperty(text/plain)"; con.setRequestProperty("text/plain", "text/plain");
        //errorDisplay = "Error in setRequestProperty(Accept-Language)"; con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        MainActivity.csLibrary4A.appendToLog("authenticate in url position : " + url.indexOf("authenticate"));
        if (url.indexOf("authenticate") >= 0) {
            String string = strBearer;
            MainActivity.csLibrary4A.appendToLog("Authorization value = " + string);
            errorDisplay = "Error in setRequestProperty(Authorization)"; con.setRequestProperty("Authorization", string);
        }
        errorDisplay = "Error in setRequestProperty(Content)"; con.setRequestProperty("Content-Type", "application/json); //; charset=utf8");
        errorDisplay = "Error in setDoOutput()"; con.setDoOutput(true);
        errorDisplay = "Error in setDoInput()"; con.setDoInput(true);
    }
    boolean serverWritten = false;
    public void write2Server(String messageStr0) {
        csLibrary4A.appendToLog("messageStr0 = " + messageStr0);
        try {
            //InputStream is = con.getInputStream(); DataInputStream ir = new DataInputStream(is);
            errorDisplay = "Error in getOutputStream()"; OutputStream os = con.getOutputStream();
            errorDisplay = "Error in DataOutputStream()"; DataOutputStream wr = new DataOutputStream(os);

            errorDisplay = "Error in writeBytes()"; wr.writeBytes(messageStr0);

            errorDisplay = "Error in flush()"; wr.flush();
            errorDisplay = "Error in close(wr)"; wr.close();
            errorDisplay = "Error in close(os)"; os.close();
            serverWritten = true;
            //csLibrary4A.appendToLog("inputStream = " + ir.readUTF());
        } catch (Exception ex) {
            MainActivity.csLibrary4A.appendToLog("errorDisplay = " + errorDisplay + ", execpetion = " + ex.getMessage());
        }
    }
    public int responseCode; public String response = null;
    public void closeServer() throws Exception {
        if (serverWritten) {
            errorDisplay = "Error in getResponseCode()";
            responseCode = con.getResponseCode();
            //MainActivity.csLibrary4A.appendToLog("errorDisplay = " + errorDisplay);
            MainActivity.csLibrary4A.appendToLog("responseCode = " + responseCode);
            if (responseCode != 200)
                errorDisplay = "Error in response code = " + responseCode;
            else {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                //MainActivity.csLibrary4A.appendToLog("errorDisplay = " + errorDisplay);
                String inputLine;
                response = "";
                while ((inputLine = in.readLine()) != null) {
                    response += inputLine;
                }
                in.close();
                //MainActivity.csLibrary4A.appendToLog("errorDisplay = " + errorDisplay);
                resultDisplay += "Success in sending data to server with response = " + response;
                MainActivity.csLibrary4A.appendToLog("response = " + response);
                errorDisplay = null;
            }
        }
        con.disconnect();
    }
}