package com.csl.cslibrary4a

class ConnectorData {
    enum class ConnectorTypes {
        RFID, BARCODE, NOTIFICATION, SILICONLAB, BLUETOOTH, OTHER
    }

    @JvmField
    var connectorTypes: ConnectorTypes? = null
    lateinit var dataValues: ByteArray
    @JvmField
    var invalidSequence: Boolean = false
    @JvmField
    var milliseconds: Long = 0
}
