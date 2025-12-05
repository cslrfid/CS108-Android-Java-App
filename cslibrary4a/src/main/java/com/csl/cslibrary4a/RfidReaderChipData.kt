package com.csl.cslibrary4a

class RfidReaderChipData {
    enum class OperationTypes {
        TAG_RDOEM,
        TAG_INVENTORY_COMPACT, TAG_INVENTORY, TAG_SEARCHING
    }
    enum class HostCommands {
        NULL, CMD_WROEM, CMD_RDOEM, CMD_ENGTEST, CMD_MBPRDREG, CMD_MBPWRREG,
        CMD_18K6CINV, CMD_18K6CREAD, CMD_18K6CWRITE, CMD_18K6CLOCK, CMD_18K6CKILL, CMD_SETPWRMGMTCFG, CMD_18K6CAUTHENTICATE, CMD_UNTRACEABLE,
        CMD_UPDATELINKPROFILE,
        CMD_18K6CBLOCKWRITE,
        CMD_CHANGEEAS, CMD_GETSENSORDATA,
        CMD_READBUFFER,
        CMD_FDM_RDMEM, CMD_FDM_WRMEM, CMD_FDM_AUTH, CMD_FDM_GET_TEMPERATURE, CMD_FDM_START_LOGGING, CMD_FDM_STOP_LOGGING,
        CMD_FDM_WRREG, CMD_FDM_RDREG, CMD_FDM_DEEP_SLEEP, CMD_FDM_OPMODE_CHECK, CMD_FDM_INIT_REGFILE, CMD_FDM_LED_CTRL,
        CMD_18K6CINV_SELECT,
        CMD_18K6CINV_COMPACT, CMD_18K6CINV_COMPACT_SELECT,
        CMD_18K6CINV_MB, CMD_18K6CINV_MB_SELECT
    }
    enum class HostCmdResponseTypes {
        NULL,
        TYPE_COMMAND_BEGIN,
        TYPE_COMMAND_END,
        TYPE_18K6C_INVENTORY, TYPE_18K6C_INVENTORY_COMPACT,
        TYPE_18K6C_TAG_ACCESS,
        TYPE_ANTENNA_CYCLE_END,
        TYPE_COMMAND_ACTIVE,
        TYPE_COMMAND_ABORT_RETURN
    }
    class Rx000pkgData {
        @JvmField
        var responseType: HostCmdResponseTypes? = null
        @JvmField
        var flags: Int = 0
        @JvmField
        var dataValues: ByteArray? = null

        @JvmField
        var decodedTime: Long = 0
        @JvmField
        var decodedRssi: Double = 0.0
        @JvmField
        var decodedPhase: Int = 0
        @JvmField
        var decodedChidx: Int = 0
        @JvmField
        var decodedPort: Int = 0
        @JvmField
        var decodedPc: ByteArray? = null

        @JvmField
        var decodedEpc: ByteArray? = null

        @JvmField
        var decodedCrc: ByteArray? = null

        @JvmField
        var decodedData1: ByteArray? = null

        @JvmField
        var decodedData2: ByteArray? = null

        @JvmField
        var decodedResult: String? = null
        @JvmField
        var decodedError: String? = null
    }
    enum class CsvColumn {
        RESERVE_BANK,
        EPC_BANK,
        TID_BANK,
        USER_BANK,
        PHASE,
        CHANNEL,
        TIME, TIMEZONE,
        LOCATION, DIRECTION,
        OTHERS
    }
}
