package daemon.dev.field.network.handler.event

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import daemon.dev.field.bluetooth.GattResolver
import daemon.dev.field.network.Socket

const val SCANNER = 0
const val GATT = 1
const val RESOLVER = 2
const val APP = 3

const val HANDSHAKE = 4
const val PACKET = 5
const val DISCONNECT = 6
const val RETRY = 7

data class ScanEvent(val result : ScanResult)

data class GattEvent(val type : Int,
                         val device: BluetoothDevice, val bytes : ByteArray?, val gattServer: BluetoothGattServer?, val req : Int?)

data class ResolverEvent(val type : Int,
                             val socket: Socket?, val bytes : ByteArray?, val device: BluetoothDevice?, val gatt: BluetoothGatt?, val res : GattResolver?)

data class AppEvent(val socket : Socket)
