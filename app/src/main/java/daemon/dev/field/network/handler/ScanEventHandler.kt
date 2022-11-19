package daemon.dev.field.network.handler

import android.util.Log
import daemon.dev.field.NETLOOPER_TAG
import daemon.dev.field.bluetooth.GattResolver
import daemon.dev.field.network.Async

class ScanEventHandler {

    private suspend fun handleScanEvent(event : ScanEvent){
//        if (getDevice(event.device.address) && (Async.state() == Async.READY)) {
//            Log.i(NETLOOPER_TAG,"Connecting scanning ${event.device.address}")
////            switch.off()
//            var gattCallback =
//                GattResolver(event.device, getHandler())
//            event.device.connectGatt(context, false, gattCallback)//, TRANSPORT_BREDR, PHY_LE_CODED)
//        }
//
    }

}