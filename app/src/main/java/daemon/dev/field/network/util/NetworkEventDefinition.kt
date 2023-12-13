package daemon.dev.field.network.util

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.network.Socket
import kotlinx.serialization.Serializable

class NetworkEventDefinition {

    companion object {
        const val SCANNER = 0
        const val GATT = 1
        const val RESOLVER = 2
        const val APP = 3

        const val CONNECT = 4
        const val PACKET = 5
        const val DISCONNECT = 6
        const val RETRY = 7

        const val STATE = 8

    }
    data class ScanEvent(val result : ScanResult)

    data class GattEvent(val type : Int,
                         val bytes : ByteArray?,
                         val socket: Socket?,)
    data class ResolverEvent(val type : Int,
                             val bytes : ByteArray?,
                             val socket: Socket?,
                             val device: BluetoothDevice?,

                             )
    data class AppEvent(val key : String, val raw : MeshRaw?)

    fun code2String(code : Int) : String{
        return when(code){
            SCANNER->{"SCANNER"}
            GATT->{"GATT"}
            RESOLVER->{"RESOLVER"}
            APP->{"APP"}
            CONNECT->{"CONNECT"}
            PACKET->{"PACKET"}
            DISCONNECT->{"DISCONNECT"}
            RETRY->{"RETRY"}
            STATE->{"STATE"}
            else->{"NONE"}
        }
    }

}