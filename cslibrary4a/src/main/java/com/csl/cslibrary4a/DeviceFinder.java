package com.csl.cslibrary4a;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class DeviceFinder{
    CsReaderConnector csReaderConnector;
    Utility utility;
    BluetoothGatt.CsScanData scanResultA = null;

    public DeviceFinder(CsReaderConnector csReaderConnector, Utility utility) {
        this.csReaderConnector = csReaderConnector;
        this.utility = utility;
        appendToLog("DeviceFinder.DeviceFinder");
    }

    void scanDevice(boolean enable) {
        if (enable) {
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            appendToLog("DeviceFinder.runnable");

            DatagramSocket udpSocket = null;
            Socket tcpSocket = null;
            ReaderSetting readerSetting = null;
            try {
                String strDestip = "255.255.255.255"; //"255.255.255.255", "192.168.25.197"

                udpSocket = new DatagramSocket();
                appendToLog("DeviceFinder.runnable: new udpSocket is " + (udpSocket == null ? "null" : "valid"));
//                    udpSocket.setBroadcast(true);
//                    appendToLog("DeviceFinder.runnable: done udpSocket.setBoardcast");
                udpSocket.setSoTimeout(1000);
                appendToLog("DeviceFinder.runnable: done udpSocket.setSoTimeout");

                byte[] sbuffer = null; InetAddress address = null; int portnum = -1; DatagramPacket packet = null;
                DatagramPacket p = null; byte[] message = null;
                if (true) {
                    sbuffer = getBytesUdpSearch();

                    address = InetAddress.getByName(strDestip);
                    portnum = 3040;
                    appendToLog("DeviceFinder.runnable: done InetAddress.getByName with strDestip as " + strDestip + ", port as " + portnum);
                    packet = new DatagramPacket(sbuffer, sbuffer.length, address, portnum);

                    //Exception here
                    udpSocket.send(packet);
                    appendToLog("DeviceFinder.runnable: done udpSocket.send with sent data as " + utility.byteArrayToString(sbuffer));

                    message = new byte[200];
                    p = new DatagramPacket(message, message.length);
                    udpSocket.receive(p); //keeps on waiting here but i am sending data back from server, but it never receives
                    appendToLog("DeviceFinder.runnable: done udpSocket.receive");

                    final byte[] rxbuffer = p.getData();
                    appendToLog("DeviceFinder.runnable: with received " + (rxbuffer == null ? "null data" : ("data length as " + rxbuffer.length)));

                    readerSetting = decodeSearchReply(sbuffer, rxbuffer);
                    if (readerSetting != null) {
                        BluetoothGatt.CsScanData scanResultA = new BluetoothGatt.CsScanData(null, 0, readerSetting.rxbuffer1);
                        StringBuilder sb = new StringBuilder(300);
                        for (int k = 0; k < readerSetting.mac.length; k++) {
                            if (k != 0) sb.append(":");
                            sb.append(String.format("%02X", readerSetting.mac[k]));
                        }
                        sb.append(" ");
                        for (int k = 0; k < readerSetting.ip.length; k++) {
                            if (k != 0) sb.append(".");
                            sb.append(String.valueOf(readerSetting.ip[k] & 0xFF));
                        }
                        scanResultA.address = sb.toString();
                        appendToLog("DeviceFinder.runnable: scanResultA.address is " + scanResultA.address);

                        sb = new StringBuilder(100);
                        for (int k = 0; k < 4; k++) {
                            if (readerSetting.device_name[k] != null) {
                                if (k != 0) sb.append(", ");
                                sb.append(readerSetting.device_name[k]);
                            }
                        }
                        scanResultA.name = sb.toString();
                        appendToLog("DeviceFinder.runnable: scanResultA.name is " + scanResultA.name);
                        scanResultA.serviceUUID2p2 = 4;

                        if (csReaderConnector.mScanResultList != null) {
                            csReaderConnector.mScanResultList.add(scanResultA);
                            //appendToLog("DeviceFinder.runnable: scanResultA.getAddress is " + csReaderConnector.mScanResultList.get(0).getAddress());
                        }
                    }
                } //3040 UdpSearch
                if (false && readerSetting != null) {
                    sbuffer = getBytesUdpAssignment(readerSetting);

                    address = InetAddress.getByAddress(readerSetting.ip);
                    portnum = 3040;
                    appendToLog("DeviceFinder.runnable: done InetAddress.getByName with ip as " + utility.byteArrayToString(readerSetting.ip) + ", port as " + portnum);
                    packet = new DatagramPacket(sbuffer, sbuffer.length, address, portnum);

                    udpSocket.send(packet);
                    appendToLog("DeviceFinder.runnable: done udpSocket.send with sent data as " + utility.byteArrayToString(sbuffer));

                    p = new DatagramPacket(message, message.length);
                    udpSocket.receive(p); //keeps on waiting here but i am sending data back from server, but it never receives
                    appendToLog("DeviceFinder.runnable: done udpSocket.receive");

                    final byte[] rxbuffer = p.getData(); //exception here as java.net.SocketTimeoutException: Poll timed out
                    appendToLog("DeviceFinder.runnable: with received " + (rxbuffer == null ? "null data" : ("data length as " + rxbuffer.length)));

                    if (decodeAssignmentReply(sbuffer, rxbuffer)) appendToLog("DeviceFinder.runnable: decodeAssignmentReply is Okay");
                    else appendToLog("DeviceFinder.runnable: decodeAssignmentReply is invalid");
                } //3040 UdpAssignment -- no reply
                if (false && readerSetting != null) {
                    sbuffer = getBytesUdpControl(readerSetting.ip, UpdControlCommands.CheckStatus);

                    address = InetAddress.getByAddress(readerSetting.ip);
                    portnum = 3041;
                    appendToLog("DeviceFinder.runnable: done InetAddress.getByName with ip as " + utility.byteArrayToString(readerSetting.ip) + ", port as " + portnum);
                    packet = new DatagramPacket(sbuffer, sbuffer.length, address, portnum);

                    udpSocket.send(packet);
                    appendToLog("DeviceFinder.runnable: done udpSocket.send with sent data as " + utility.byteArrayToString(sbuffer));

                    p = new DatagramPacket(message, message.length);
                    udpSocket.receive(p); //keeps on waiting here but i am sending data back from server, but it never receives
                    appendToLog("DeviceFinder.runnable: done udpSocket.receive");

                    final byte[] rxbuffer2 = p.getData(); //exception here as java.net.SocketTimeoutException: Poll timed out
                    appendToLog("DeviceFinder.runnable: with received " + (rxbuffer2 == null ? "null data" : ("data length as " + rxbuffer2.length)));

                    if (decodeUdpControlReply(sbuffer, rxbuffer2)) appendToLog("DeviceFinder.runnable: decodeUdpControlReply is Okay");
                    else appendToLog("DeviceFinder.runnable: decodeUdpControlReply is invalid");
                } //3041 UdpControl CheckStatus
                if (false && readerSetting != null) {
                    sbuffer = getBytesUdpControl(readerSetting.ip, UpdControlCommands.GetBootloaderVersion);

                    address = InetAddress.getByAddress(readerSetting.ip);
                    portnum = 3041;
                    appendToLog("DeviceFinder.runnable: done InetAddress.getByName with ip as " + utility.byteArrayToString(readerSetting.ip) + ", port as " + portnum);
                    packet = new DatagramPacket(sbuffer, sbuffer.length, address, portnum);

                    udpSocket.send(packet);
                    appendToLog("DeviceFinder.runnable: done udpSocket.send with sent data as " + utility.byteArrayToString(sbuffer));

                    p = new DatagramPacket(message, message.length);
                    udpSocket.receive(p); //keeps on waiting here but i am sending data back from server, but it never receives
                    appendToLog("DeviceFinder.runnable: done udpSocket.receive");

                    final byte[] rxbuffer2 = p.getData(); //exception here as java.net.SocketTimeoutException: Poll timed out
                    appendToLog("DeviceFinder.runnable: with received " + (rxbuffer2 == null ? "null data" : ("data length as " + rxbuffer2.length)));

                    if (decodeUdpControlReply(sbuffer, rxbuffer2)) appendToLog("DeviceFinder.runnable: decodeUdpControlReply is Okay");
                    else appendToLog("DeviceFinder.runnable: decodeUdpControlReply is invalid");
                } //3041 UdpControl GetBootloaderVersion
                if (false && readerSetting != null) {
                    sbuffer = getBytesUdpControl(readerSetting.ip, UpdControlCommands.GetImageVersion);

                    address = InetAddress.getByAddress(readerSetting.ip);
                    portnum = 3041;
                    appendToLog("DeviceFinder.runnable: done InetAddress.getByName with ip as " + utility.byteArrayToString(readerSetting.ip) + ", port as " + portnum);
                    packet = new DatagramPacket(sbuffer, sbuffer.length, address, portnum);

                    udpSocket.send(packet);
                    appendToLog("DeviceFinder.runnable: done udpSocket.send with sent data as " + utility.byteArrayToString(sbuffer));

                    p = new DatagramPacket(message, message.length);
                    udpSocket.receive(p); //keeps on waiting here but i am sending data back from server, but it never receives
                    appendToLog("DeviceFinder.runnable: done udpSocket.receive");

                    final byte[] rxbuffer2 = p.getData(); //exception here as java.net.SocketTimeoutException: Poll timed out
                    appendToLog("DeviceFinder.runnable: with received " + (rxbuffer2 == null ? "null data" : ("data length as " + rxbuffer2.length)));

                    if (decodeUdpControlReply(sbuffer, rxbuffer2)) appendToLog("DeviceFinder.runnable: decodeUdpControlReply is Okay");
                    else appendToLog("DeviceFinder.runnable: decodeUdpControlReply is invalid");
                } //3041 UdpControl GetImageVersion
            } catch (Exception ex) {
                appendToLog("DeviceFinder.runnable upd exception as " + ex.toString());
            }
            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
                appendToLog("DeviceFinder.runnable: done udpSocket.close");
            }

            if (false && readerSetting != null) {
                String stringIp = String.format("%d.%d.%d.%d", (readerSetting.ip[0] & 0xFF),
                        (readerSetting.ip[1] & 0xFF), (readerSetting.ip[2] & 0xFF), (readerSetting.ip[3] & 0xFF));
                appendToLog("DeviceFinder.runnable: stringIp is " + stringIp);
                byte[] data = null; InputStream input = null; byte[] dataIn = null; int iValue;
                try {
                    tcpSocket = new Socket(stringIp, 1515);
                    appendToLog("DeviceFinder.runnable: done new Socket with Ip " + stringIp + " and port 1515");
                    OutputStream output = tcpSocket.getOutputStream();
                    appendToLog("DeviceFinder.runnable: done socket.getOutputStream");

                    if (true) {
                        data = getBytesTcpReader(TcpReaderCommands.Reset);
                        output.write(data);
                        appendToLog("DeviceFinder.runnable: done outputStream.write with sent data as " + utility.byteArrayToString(data));

                        input = tcpSocket.getInputStream();
                        appendToLog("DeviceFinder.runnable: done socket.getInputStream");

                        dataIn = new byte[64];
                        iValue = input.read(dataIn);
                        appendToLog("DeviceFinder.runnable: done inputStream.read with output value as " + iValue);
                        if (iValue > 0) {
                            byte[] dataIn1 = new byte[iValue];
                            System.arraycopy(dataIn, 0, dataIn1, 0, dataIn1.length);
                            appendToLog("DeviceFinder.runnable: with data as " + utility.byteArrayToString(dataIn1));
                        }
                    } //1515 TcpReaderCommand.Reset
                    if (false) {
                        data = getBytesTcpReader(TcpReaderCommands.WriteCommandClearError);
                        output.write(data);
                        appendToLog("DeviceFinder.runnable: done outputStream.write with data as " + utility.byteArrayToString(data));

                        input = tcpSocket.getInputStream();
                        appendToLog("DeviceFinder.runnable: done socket.getInputStream");

                        dataIn = new byte[64];
                        iValue = input.read(dataIn);
                        appendToLog("DeviceFinder.runnable: done inputStream.read with output value as " + iValue);
                        if (iValue > 0) {
                            byte[] dataIn1 = new byte[iValue];
                            System.arraycopy(dataIn, 0, dataIn1, 0, dataIn1.length);
                            appendToLog("DeviceFinder.runnable: with data as " + utility.byteArrayToString(dataIn1));
                        }
                    } //1515 TcpReaderCommandClearError
                    if (false) {
                        data = getBytesTcpReader(TcpReaderCommands.WriteOemAddress);
                        output.write(data);
                        appendToLog("DeviceFinder.runnable: done outputStream.write with data as " + utility.byteArrayToString(data));
                        data = getBytesTcpReader(TcpReaderCommands.WriteCommandOemRead);
                        output.write(data);
                        appendToLog("DeviceFinder.runnable: done outputStream.write with data as " + utility.byteArrayToString(data));

                        input = tcpSocket.getInputStream();
                        appendToLog("DeviceFinder.runnable: done socket.getInputStream");

                        dataIn = new byte[64];
                        iValue = input.read(dataIn);
                        appendToLog("DeviceFinder.runnable: done inputStream.read with output value as " + iValue);
                        if (iValue > 0) {
                            byte[] dataIn1 = new byte[iValue];
                            System.arraycopy(dataIn, 0, dataIn1, 0, dataIn1.length);
                            appendToLog("DeviceFinder.runnable: with data as " + utility.byteArrayToString(dataIn1));
                        }
                    } //1515 TcpReaderCommandWriteOemAddress, WriteCommandOemRead
                    if (false) {
                        data = getBytesTcpReader(TcpReaderCommands.ReadMac705);
                        output.write(data);
                        appendToLog("DeviceFinder.runnable: done outputStream.write with data as " + utility.byteArrayToString(data));

                        input = tcpSocket.getInputStream();
                        appendToLog("DeviceFinder.runnable: done socket.getInputStream");

                        dataIn = new byte[64];
                        iValue = input.read(dataIn);
                        appendToLog("DeviceFinder.runnable: done inputStream.read with output value as " + iValue);
                        if (iValue > 0) {
                            byte[] dataIn1 = new byte[iValue];
                            System.arraycopy(dataIn, 0, dataIn1, 0, dataIn1.length);
                            appendToLog("DeviceFinder.runnable: with data as " + utility.byteArrayToString(dataIn1));
                        }
                    } //1515 TcpReaderCommandReadMac705
                } catch (Exception ex) {
                    appendToLog("DeviceFinder.runnable tcp exception as " + ex.toString());
                }
                if (tcpSocket != null && !tcpSocket.isClosed()) {
                    try {
                        tcpSocket.close();
                        appendToLog("DeviceFinder.runnable: done tcpSocket.close");
                    } catch (Exception ex) {
                        appendToLog("DeviceFinder.runnable tcp exception as " + ex.toString());
                    }
                }

                if (false) {
                    try {
                        tcpSocket = new Socket(stringIp, 1516); //failed to connect with port 1516
                        appendToLog("DeviceFinder.runnable: done new Socket with Ip " + stringIp + " and port 1515");
                        OutputStream output = tcpSocket.getOutputStream();
                        appendToLog("DeviceFinder.runnable: done socket.getOutputStream");

                        if (true) {
                            data = getBytesTcpControl();
                            output.write(data);
                            appendToLog("DeviceFinder.runnable: done outputStream.write with data as " + utility.byteArrayToString(data));

                            input = tcpSocket.getInputStream();
                            appendToLog("DeviceFinder.runnable: done socket.getInputStream");

                            dataIn = new byte[64];
                            iValue = input.read(dataIn);
                            appendToLog("DeviceFinder.runnable: done inputStream.read with output value as " + iValue);
                            if (iValue > 0) {
                                byte[] dataIn1 = new byte[iValue];
                                System.arraycopy(dataIn, 0, dataIn1, 0, dataIn1.length);
                                appendToLog("DeviceFinder.runnable: with data as " + utility.byteArrayToString(dataIn1));
                            }
                        } //1516 TcpControlCommand -- failed to connect with port 1516
                    } catch (Exception ex) {
                        appendToLog("DeviceFinder.runnable tcp exception as " + ex.toString());
                    }
                    if (tcpSocket != null && !tcpSocket.isClosed()) {
                        try {
                            tcpSocket.close();
                            appendToLog("DeviceFinder.runnable: done tcpSocket.close");
                        } catch (Exception ex) {
                            appendToLog("DeviceFinder.runnable tcp exception as " + ex.toString());
                        }
                    }
                } //1516 TcpCommand -- failed to connect with port 1516
            }
        }
    };

    static class ReaderSetting {
        byte[] rxbuffer1 = null;
        byte mode;                              //for search only
        byte connection_mode;                   //for search only
        byte dhcp_retry;                        //for search only
        //byte[] device_name1, device_name2, device_name3;    //for Search only
        byte[] ip = new byte[4];                //
        byte[] trusted_host_ip = new byte[4];   //
        byte tcp_timeout;
        byte ip_mode;                           //
        byte[] mac = new byte[6];               //
        byte trusted_host_mode;                 //
        //byte[] device_name = new byte[32];      //
        String[] device_name = new String[4];
        byte[] subnet = new byte[4];            //
        byte[] gateway = new byte[4];           //
        byte gateway_check;                     //
    }
    byte[] getBytesUdpSearch() {
        Random randomNumber = new Random();
        byte[] sbuffer = new byte[4];
        randomNumber.nextBytes(sbuffer);
        sbuffer[0] = 0;
        sbuffer[1] = 0;
        return sbuffer;
    }
    ReaderSetting decodeSearchReply(byte[] sbuffer, byte[] rxbuffer) {
        boolean DEBUG = false;
        ReaderSetting readerSetting = null;
        boolean bValue = false;
        int iLengthMin = 45;
        int iNull = 0, index = 40, i = index;
        if (rxbuffer != null && rxbuffer.length >= iLengthMin) {
            if (rxbuffer[0] == 1 && rxbuffer[1] == 0 && rxbuffer[2] == sbuffer[2] && rxbuffer[3] == sbuffer[3]) {
                for (; i < rxbuffer.length; i++) {
                    if (rxbuffer[i] == 0) {
                        if (DEBUG) appendToLog("DeviceFinder.decodeSearchReply: i = " + i);
                        if (++iNull == 4) break;
                    }
                }
                if (iNull == 4 && rxbuffer.length > i) bValue = true;
            }
        }
        if (bValue) {
            byte[] rxbuffer1 = new byte[i + 2];
            System.arraycopy(rxbuffer, 0, rxbuffer1, 0, rxbuffer1.length);

            readerSetting = new ReaderSetting();

            readerSetting.rxbuffer1 = rxbuffer1;
            appendToLog("DeviceFinder.decodeSearchReply: " + rxbuffer1.length + " data received " + utility.byteArrayToString(rxbuffer1));

            readerSetting.mode = rxbuffer1[1];

            System.arraycopy(rxbuffer1, 14, readerSetting.mac, 0, readerSetting.mac.length);
            if (DEBUG) appendToLog("DeviceFinder.decodeSearchReply: mac address is " + utility.byteArrayToString(readerSetting.mac));

            System.arraycopy(rxbuffer1, 20, readerSetting.ip, 0, readerSetting.ip.length);
            if (DEBUG) appendToLog("DeviceFinder.decodeSearchReply: ip address is " + utility.byteArrayToString(readerSetting.ip));

            System.arraycopy(rxbuffer1, 4, readerSetting.trusted_host_ip, 0, readerSetting.trusted_host_ip.length);
            readerSetting.trusted_host_mode = rxbuffer1[28];
            readerSetting.connection_mode = rxbuffer1[29];
            readerSetting.dhcp_retry = rxbuffer1[30];
            readerSetting.ip_mode = rxbuffer1[31];
            System.arraycopy(rxbuffer1, 32, readerSetting.subnet, 0, readerSetting.ip.length);
            System.arraycopy(rxbuffer1, 36, readerSetting.gateway, 0, readerSetting.ip.length);

            i = index;
            int indexString = 0;
            for (; i < rxbuffer1.length; i++) {
                if (rxbuffer1[i] == 0) {
                    if (i > index) {
                        byte[] bytesName = new byte[i - index];
                        System.arraycopy(rxbuffer1, index, bytesName, 0, bytesName.length);
                        if (DEBUG) appendToLog("DeviceFinder.decodeSearchReply: name is " + utility.byteArrayToString(bytesName) + " " + new String(bytesName, StandardCharsets.UTF_8));
                        readerSetting.device_name[indexString++] = new String(bytesName, StandardCharsets.UTF_8);
                    }
                    if (++iNull == 4) break;
                    index = i + 1;
                }
            }
            readerSetting.gateway_check = rxbuffer1[rxbuffer1.length-1];
        }
        return readerSetting;
    }
    byte[] getBytesUdpAssignment(ReaderSetting readerSetting) {
        Random randomNumber = new Random();
        byte[] sbuffer = new byte[65];
        sbuffer[0] = 2; sbuffer[1] = 0;

        byte[] sbuffer1 = new byte[2];
        randomNumber.nextBytes(sbuffer1);
        System.arraycopy(sbuffer1, 0, sbuffer, 2, sbuffer1.length);
        System.arraycopy(readerSetting.ip, 0, sbuffer, 4, readerSetting.ip.length);
        System.arraycopy(readerSetting.trusted_host_ip, 0, sbuffer, 8, readerSetting.trusted_host_ip.length);
        sbuffer[14] = readerSetting.tcp_timeout;
        sbuffer[15] = readerSetting.ip_mode;
        System.arraycopy(readerSetting.mac, 0, sbuffer, 16, readerSetting.mac.length);
        sbuffer[22] = readerSetting.trusted_host_mode;
        byte[] deviceName = readerSetting.device_name[0].getBytes();
        int length = deviceName.length;
        if (length > 32) length = 32;
        System.arraycopy(deviceName, 0, sbuffer, 24, length);
        System.arraycopy(readerSetting.subnet, 0, sbuffer, 56, readerSetting.subnet.length);
        System.arraycopy(readerSetting.gateway, 0, sbuffer, 60, readerSetting.gateway.length);
        sbuffer[64] = readerSetting.gateway_check;
        return sbuffer;
    }
    boolean decodeAssignmentReply(byte[] sbuffer, byte[] rxbuffer) {
        boolean bValue = false;;
        if (rxbuffer != null && rxbuffer.length >= 4) {
            if (rxbuffer[0] == 3 && rxbuffer[1] == 0 && rxbuffer[2] == sbuffer[2] && rxbuffer[3] == sbuffer[3]) {
                bValue = true;
            }
        }
        return bValue;
    }
    byte[] getBytesUdpControl(byte[] ip, UpdControlCommands updControlCommands) {
        byte[] bytes = new byte[7];
        int length = ip.length;
        appendToLog("DeviceFinder length is " + length);
        if (length == 4) {
            bytes[0] = (byte) 0x80;
            System.arraycopy(ip, 0, bytes, 1, length);
            bytes[5] = 0;
            if (updControlCommands == UpdControlCommands.CheckStatus) bytes[6] = 1;
            else if (updControlCommands == UpdControlCommands.GetBootloaderVersion) bytes[6] = 0x15;
            else if (updControlCommands == UpdControlCommands.GetImageVersion) bytes[6] = 0x16;
        } else bytes = null;
        return bytes;
    }
    public enum UpdControlCommands { CheckStatus, GetBootloaderVersion, GetImageVersion }
    boolean decodeUdpControlReply(byte[] sBuffer, byte[] rxbufferRaw) {
        boolean bValue = false;
        byte[] rxbuffer = new byte[4 + rxbufferRaw[2]];
        System.arraycopy(rxbufferRaw, 0, rxbuffer, 0, rxbuffer.length);
        appendToLog("DeviceFinder.decodeUdpControlReply: data received as " + utility.byteArrayToString(rxbuffer));
        if (rxbuffer[0] == (byte) 0x81 && rxbuffer[3] == sBuffer[6]) {
            if (rxbuffer[1] == 1) bValue = true;
        }
        return bValue;
    }
    enum TcpReaderCommands { Reset, WriteCommandClearError, WriteOemAddress, WriteCommandOemRead, ReadMac705 }
    byte[] getBytesTcpReader(TcpReaderCommands tcpReaderCommands) {
        byte[] bytes = null;
        if (tcpReaderCommands == TcpReaderCommands.Reset) bytes = new byte[] { 0x40, 0x03, 0, 0, 0, 0, 0, 0 };
        else if (tcpReaderCommands == TcpReaderCommands.WriteCommandClearError) bytes = new byte[] { 1, 0, 0, (byte) 0xf0, 0x15, 0, 0, 0 };
        else if (tcpReaderCommands == TcpReaderCommands.WriteOemAddress) bytes = new byte[] { 1, 0, 0, 5, (byte) 0xA4, 0, 0, 0 };
        else if (tcpReaderCommands == TcpReaderCommands.WriteCommandOemRead) bytes = new byte[] { 1, 0, 0, (byte) 0xf0, 3, 0, 0, 0 };
        else if (tcpReaderCommands == TcpReaderCommands.ReadMac705) bytes = new byte[] { 0, 0, 0x05, 0x07, 0, 0, 0, 0 };
        return bytes;
    }
    byte[] getBytesTcpControl() {
        return new byte[] { (byte) 0x80, 0, 0, 0x12 };
    }

    boolean deviceFinderConnectState = false;
    boolean connect(ReaderDevice readerDevice) {
        boolean DEBUG = true, bValue = true;
        if (DEBUG) appendToLog("DeviceFinder.connect: readerDevice is " + (readerDevice == null ? "null" : "valid"));
        if (readerDevice == null) {
            bValue = false;
            if (DEBUG) appendToLog("DeviceFinder.connect with NULL readerDevice");
        } else deviceFinderConnectState = true;
        return bValue;
    }
    //============ utility ============
    public void appendToLog(String s) {
        utility.appendToLog(s);
    }
}