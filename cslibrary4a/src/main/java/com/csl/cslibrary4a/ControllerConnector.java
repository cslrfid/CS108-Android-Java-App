package com.csl.cslibrary4a;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

public class ControllerConnector {
    boolean userDebugEnableDefault = false, userDebugEnable = userDebugEnableDefault;

    Context context; Utility utility;
    public ControllerConnector(Context context, Utility utility) {
        this.context = context;
        this.utility = utility;
    }
    private String byteArrayToString(byte[] packet) { return utility.byteArrayToString(packet); }
    private boolean compareArray(byte[] array1, byte[] array2, int length) { return utility.compareByteArray(array1, array2, length); }

    private int icsModel = -1;
    int getCsModel() {
        Logger.trace("icsModel = {}", icsModel);
        return icsModel;
    }

    public enum ControllerPayloadEvents {
        CONTROLLER_GET_VERSION, CONTROLLER_GET_SERIALNUMBER, CONTROLLER_GET_MODELNAME, CONTROLLER_RESET
    }

    class ControllerReadData {
        ControllerPayloadEvents controllerPayloadEvents;
        byte[] dataValues;
    }

    private byte[] controllerVersion = new byte[]{-1, -1, -1};

    public String getVersion() {
        if (controllerVersion[0] == -1) {
            boolean repeatRequest = false;
            if (controllerToWrite.size() != 0) {
                if (controllerToWrite.get(controllerToWrite.size() - 1) == ControllerPayloadEvents.CONTROLLER_GET_VERSION) {
                    repeatRequest = true;
                }
            }
            if (repeatRequest == false) {
                controllerToWrite.add(ControllerPayloadEvents.CONTROLLER_GET_VERSION);
                Logger.pkData("PkData: add GET_VERSION to controllerWrite with length = {}", controllerToWrite.size());
            }
            return "";
        } else {
            Logger.debug("controllerVersion = {}", byteArrayToString(controllerVersion));
            String string = String.valueOf(controllerVersion[0]) + "." + String.valueOf(controllerVersion[1]) + "." + String.valueOf(controllerVersion[2]);
            Logger.debug("controllerVersion string = {}", string);
            return string;
        }
    }

    private byte[] serialNumber = null;
    public String getSerialNumber() {
        if (serialNumber == null) {
            boolean repeatRequest = false;
            if (controllerToWrite.size() != 0) {
                if (controllerToWrite.get(controllerToWrite.size() - 1) == ControllerPayloadEvents.CONTROLLER_GET_SERIALNUMBER) {
                    repeatRequest = true;
                }
            }
            if (repeatRequest == false) {
                controllerToWrite.add(ControllerPayloadEvents.CONTROLLER_GET_SERIALNUMBER);
                Logger.pkData("PkData: add GET_SERIALNUMBER to controllerToWrite with length = {}", controllerToWrite.size());
            }
            return "";
        } else {
            byte[] bytes = new byte[serialNumber.length];
            System.arraycopy(serialNumber, 0, bytes, 0, serialNumber.length);
            if (bytes.length == 16) {
                if (bytes[15] == 0) {
                    bytes[15] = serialNumber[14];
                    bytes[14] = serialNumber[13];
                    bytes[13] = 0;
                }
                for (int i = 13; i < 16; i++) {
                    if (bytes[i] == 0) bytes[i] = 0x30;
                }
            }
            Logger.trace("serialNumber = {}, revised = {}", byteArrayToString(serialNumber), byteArrayToString(bytes));
            String string = utility.byteArray2DisplayString(bytes);
            if (string == null || string.isEmpty()) {
                string = byteArrayToString(bytes);
                if (string.length() > 16) string = string.substring(0, 16);
            }
            Logger.trace("string = {} from serial {}, revised = {}", string, byteArrayToString(serialNumber), byteArrayToString(bytes));
            return string;
        }
    }

    private byte[] modelName = null;
    public String getModelName() {
        Logger.trace("modelName = {}", byteArrayToString(modelName));
        String strValue = null;
        if (modelName == null) {
            boolean repeatRequest = false;
            if (controllerToWrite.size() != 0) {
                if (controllerToWrite.get(controllerToWrite.size() - 1) == ControllerPayloadEvents.CONTROLLER_GET_MODELNAME) {
                    repeatRequest = true;
                }
            }
            if (repeatRequest == false) {
                controllerToWrite.add(ControllerPayloadEvents.CONTROLLER_GET_MODELNAME);
                Logger.trace("PkData: add GET_MODELNAME to controllerWrite with length = {}", controllerToWrite.size());
            }
        } else {
            strValue = utility.byteArray2DisplayString(modelName);
            Logger.trace("strValue 0 = {}", strValue);
            if (strValue == null || strValue.length() == 0) {
                strValue = byteArrayToString(modelName).substring(0, 5);
            }
        }
        Logger.trace("strValue = {}", strValue);
        return strValue;
    }

    boolean resetSiliconLab() {
        boolean bRetValue = false;
        bRetValue = controllerToWrite.add(ControllerConnector.ControllerPayloadEvents.CONTROLLER_RESET);
        Logger.info("add RESET to mSiliconLabIcWrite with length = {}", controllerToWrite.size());
        //mRfidDevice.setInventoring(false);
        return bRetValue;
    }

    public ArrayList<ControllerPayloadEvents> controllerToWrite = new ArrayList<>();

    private boolean arrayTypeSet(byte[] dataBuf, int pos, ControllerPayloadEvents event) {
        boolean validEvent = false;
        switch (event) {
            case CONTROLLER_GET_VERSION:
                validEvent = true;
                break;
            case CONTROLLER_GET_SERIALNUMBER:
                dataBuf[pos] = 4;
                validEvent = true;
                break;
            case CONTROLLER_GET_MODELNAME:
                dataBuf[pos] = 6;
                validEvent = true;
                break;
            case CONTROLLER_RESET:
                dataBuf[pos] = 12;
                validEvent = true;
                break;
        }
        return validEvent;
    }

    private byte[] writeController(ControllerPayloadEvents event) {
        byte[] dataOut = null;
        if (event == ControllerPayloadEvents.CONTROLLER_GET_VERSION) {
            dataOut = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xE8, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xB0, 0};
        } else if (event == ControllerPayloadEvents.CONTROLLER_GET_SERIALNUMBER) {
            dataOut = new byte[]{(byte) 0xA7, (byte) 0xB3, 3, (byte) 0xE8, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xB0, 4, 0};
        } else if (event == ControllerPayloadEvents.CONTROLLER_GET_MODELNAME) {
            dataOut = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xE8, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xB0, 6};
        } else if (event == ControllerPayloadEvents.CONTROLLER_RESET) {
            dataOut = new byte[]{(byte) 0xA7, (byte) 0xB3, 2, (byte) 0xE8, (byte) 0x82, (byte) 0x37, 0, 0, (byte) 0xB0, 12};
        }
        Logger.debug("{} for {}", byteArrayToString(dataOut), event.toString());
        return dataOut;
    }

    public boolean isMatchControllerToWrite(ConnectorData connectorData) {
        boolean match = false;
        if (controllerToWrite.size() != 0 && connectorData.dataValues[0] == (byte)0xB0) {
            byte[] dataInCompare = new byte[]{(byte) 0xB0, 0};
            if (arrayTypeSet(dataInCompare, 1, controllerToWrite.get(0)) && (connectorData.dataValues.length >= dataInCompare.length + 1)) {
                if (match = compareArray(connectorData.dataValues, dataInCompare, dataInCompare.length)) {
                    Logger.pkData("PkData: matched Controller.Reply with payload = {} for writeData.Controller.{}", byteArrayToString(connectorData.dataValues), controllerToWrite.get(0));
                    if (controllerToWrite.get(0) == ControllerPayloadEvents.CONTROLLER_GET_VERSION) {
                        if (connectorData.dataValues.length >= 2 + controllerVersion.length) {
                            System.arraycopy(connectorData.dataValues, 2, controllerVersion, 0, controllerVersion.length);
                            Logger.pkData("PkData: matched Controller.Reply.GetVersion with version = {}", byteArrayToString(controllerVersion));
                        }
                    } else if (controllerToWrite.get(0) == ControllerPayloadEvents.CONTROLLER_GET_SERIALNUMBER) {
                        int length = connectorData.dataValues.length - 2;
                        serialNumber = new byte[length];
                        System.arraycopy(connectorData.dataValues, 2, serialNumber, 0, length);
                        Logger.pkData("PkData: matched Controller.Reply.GetSerialNumber with serialNumber = {}", byteArrayToString(serialNumber));
                    } else if (controllerToWrite.get(0) == ControllerPayloadEvents.CONTROLLER_GET_MODELNAME) {
                        int length = connectorData.dataValues.length - 2;
                        modelName = new byte[length];
                        System.arraycopy(connectorData.dataValues, 2, modelName, 0, length);
                        Logger.pkData("PkData: matched controller.GetModelName.reply with modelName = {}", byteArrayToString(modelName));
                    } else if (controllerToWrite.get(0) == ControllerPayloadEvents.CONTROLLER_RESET) {
                        if (connectorData.dataValues[2] != 0) {
                            Logger.info("Controller RESET is found with error");
                        } else Logger.info("matched Controller.reply data is found");
                    } else {
                        Logger.info("matched controller.Other.reply data is found.");
                    }
                    controllerToWrite.remove(0); sendDataToWriteSent = 0;
                    Logger.pkData("PkData: new controllerToWrite size = {}", controllerToWrite.size());

                }
            }
        }
        return match;
    }

    public int sendDataToWriteSent = 0;
    boolean controllerFailure = false;
    public byte[] sendControllerToWrite() {
        if (controllerFailure) {
            controllerToWrite.remove(0); sendDataToWriteSent = 0;
        } else if (sendDataToWriteSent >= 5) {
            int oldSize = controllerToWrite.size();
            controllerToWrite.remove(0); sendDataToWriteSent = 0;
            Logger.debug("Removed after sending count-out with oldSize = {}, updated controllerToWrite.size() = {}", oldSize, controllerToWrite.size());
            Logger.debug("Removed after sending count-out.");
            String string = "Problem in sending data to Controller Module. Removed data sending after count-out";
            if (userDebugEnable) Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
            else Logger.toLogView(string).trace();
            controllerFailure = true; // disconnect(false);
        } else {
            Logger.debug("size = {}", controllerToWrite.size());
            sendDataToWriteSent++;
            return writeController(controllerToWrite.get(0));
        }
        return null;
    }
}
