package com.csl.cs108ademoapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import com.csl.cs108library4a.ReaderDevice;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.content.Context.WIFI_SERVICE;
import static android.support.v4.app.ActivityCompat.requestPermissions;
import static com.csl.cs108ademoapp.MainActivity.mContext;
import static com.csl.cs108ademoapp.MainActivity.mCs108Library4a;

public class SaveList2ExternalTask extends AsyncTask<Void,Void,String> {
    String messageStr;
    String resultDisplay = "", errorDisplay;
    ArrayList<ReaderDevice> tagsList; ReaderDevice tagDevice1;
    CustomPopupWindow customPopupWindow;
    boolean savedFile = false;

    String url = null;
    HttpURLConnection con;

    public SaveList2ExternalTask(ArrayList<ReaderDevice> tagsList) {
        this.tagsList = tagsList;
    }
    public SaveList2ExternalTask() { }

    @Override
    protected void onPreExecute() {
        if (tagsList == null) cancel(true);

        messageStr = createJSON(tagsList, null).toString();
        resultDisplay = save2File(messageStr, true);
        customPopupWindow = new CustomPopupWindow(mContext);
        if (resultDisplay == null) resultDisplay = "";
        else {
            resultDisplay += "\n";
            savedFile = true;
            customPopupWindow.popupStart(resultDisplay + "Connecting server. Please wait.", true);
        }
    }

    protected String doInBackground(Void... params) {
        if (MainActivity.mCs108Library4a.isBleConnected() == false) {
            resultDisplay += "Error in sending data to server as the reader is not connected";
            return null;
        } else if (MainActivity.mCs108Library4a.getSaveCloudEnable() == false) {
            resultDisplay += "No saving to cloud as it is disabled";
            return null;
        }
        try {
            if (true) {
                openServer();
                write2Server(messageStr);
                closeServer();
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
                if (validLocation == false) resultDisplay += "Error in creating server location from " + mCs108Library4a.getServerLocation() + " !!!";
                else {
                    DatagramSocket udpSocket = new DatagramSocket();
                    byte[] buf = messageStr.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, serverPort);
                    udpSocket.send(packet);
                    resultDisplay += "Success in sending data to " + mCs108Library4a.getServerLocation();
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
        else customPopupWindow.popupWindow.dismiss();
        customPopupWindow.popupStart(resultDisplay, false);
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

            object.put("rfidReaderName", MainActivity.mCs108Library4a.getBluetoothICFirmwareName());
            object.put("rfidReaderSerialNumber", MainActivity.mCs108Library4a.getHostProcessorICSerialNumber());
            object.put("rfidReaderInternalSerialNumber", MainActivity.mCs108Library4a.getRadioSerial());

            object.put("smartPhoneName", Build.MODEL);
            object.put("smartPhoneSerialNumber", Build.SERIAL);
            object.put("smartPhoneBluetoothMACAddress",  BluetoothAdapter.getDefaultAdapter().getAddress().replaceAll(":", ""));
            object.put("smartPhoneWiFiMACAddress", ((WifiManager) MainActivity.mContext.getSystemService(WIFI_SERVICE)).getConnectionInfo().getMacAddress().replaceAll(":", ""));

            object.put("smartPhoneUUID", null);
            object.put("pcName", null);
            object.put("pcEthernetMACAddress", null);
            object.put("pcWiFiMACAddress", null);

            object.put("operatorId","Generic Operator");
            object.put("operatorSiteId","Generic Site");
        } catch (Exception ex) { }
        return object;
    }

    String save2File(String messageStr, boolean requestPermission) {
        String resultDisplay = "";
        if (MainActivity.mCs108Library4a.getSaveFileEnable() == false) return "No saving file as it is disabled";
        boolean writeExtPermission = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                writeExtPermission = false;
                if (requestPermission) { MainActivity.permissionRequesting = true; requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return null; }
            }
        }
        errorDisplay = null;
        if (writeExtPermission == false) {
            errorDisplay = "denied WRITE_EXTERNAL_STORAGE Permission !!!";
        } else if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) == false) errorDisplay = "Error in mouting external storage !!!";
        else {
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/cs108Java");
            if (path.exists() == false) path.mkdirs();
            if (path.exists() == false) errorDisplay = "Error in making directory !!!";
            else {
                String dateTime = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
                String fileName = "cs108Java_" + dateTime + ".txt";
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
                        resultDisplay = "Success in saving data to Download/cs108Java/" + fileName;
                        errorDisplay = null;
                    } catch (Exception ex) {
                    }
                }
            }
        }
        if (errorDisplay != null) return "Error in saving file with " + errorDisplay;
        return resultDisplay;
    }

    public void openServer() throws Exception {
        if (false) {
            url = "https://";
            url += "192.168.25.21:";
            url += "29090/WebServiceRESTs/1.0/req/";
        } else if (false) url = "http://ptsv2.com/t/10i1t-1519143332/post";
        else url = mCs108Library4a.getServerLocation();

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

        errorDisplay = "Error in URL()"; URL obj = new URL(url);
        errorDisplay = "Error in openConnection()";
        boolean isHttps = false;
        if (url.length() >= 6) {
            if (url.substring(0, 6).matches("https:")) isHttps = true;
        }
        con = (HttpURLConnection) obj.openConnection();
        if (isHttps) {
            con = (HttpsURLConnection) obj.openConnection();
        }
        errorDisplay = "Error in setConnectTimeout()"; con.setConnectTimeout(MainActivity.mCs108Library4a.getServerTimeout() * 1000);
        errorDisplay = "Error in setRequestMethod()"; con.setRequestMethod("POST");
        errorDisplay = "Error in setRequestProperty(User-Agent)"; con.setRequestProperty("User-Agent", "Mozilla/5.0");
        errorDisplay = "Error in setRequestProperty(Accept-Languag)"; con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        errorDisplay = "Error in setDoOutput()"; con.setDoOutput(true);
    }
    boolean serverWritten = false;
    public void write2Server(String messageStr0) {
        try {
            errorDisplay = "Error in getOutputStream()"; OutputStream os = con.getOutputStream();
            errorDisplay = "Error in DataOutputStream()"; DataOutputStream wr = new DataOutputStream(os);

            errorDisplay = "Error in writeBytes()"; wr.writeBytes(messageStr0);

            errorDisplay = "Error in flush()"; wr.flush();
            errorDisplay = "Error in close(wr)"; wr.close();
            errorDisplay = "Error in close(os)"; os.close();
            serverWritten = true;
        } catch (Exception ex) {
            MainActivity.mCs108Library4a.appendToLog("errorDisplay = " + errorDisplay + ", execpetion = " + ex.getMessage());
        }
    }
    public void closeServer() throws Exception {
        if (serverWritten) {
            errorDisplay = "Error in getResponseCode()";
            int responseCode = con.getResponseCode();
            MainActivity.mCs108Library4a.appendToLog("errorDisplay = " + errorDisplay);
            MainActivity.mCs108Library4a.appendToLog("responseCode = " + responseCode);
            if (responseCode != 200)
                errorDisplay = "Error in response code = " + responseCode;
            else {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                MainActivity.mCs108Library4a.appendToLog("errorDisplay = " + errorDisplay);
                String inputLine;
                String response = "";
                while ((inputLine = in.readLine()) != null) {
                    response += inputLine;
                }
                in.close();
                MainActivity.mCs108Library4a.appendToLog("errorDisplay = " + errorDisplay);
                resultDisplay += "Success in sending data to server with response = " + response;
                errorDisplay = null;
            }
        }
        con.disconnect();
    }
}