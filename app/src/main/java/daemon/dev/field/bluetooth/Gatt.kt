/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package daemon.dev.field.bluetooth

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.*
import daemon.dev.field.network.Async
import kotlinx.coroutines.runBlocking
import daemon.dev.field.network.handler.event.*
import daemon.dev.field.nypt.Session


@SuppressLint("MissingPermission")
class Gatt(val app: Application, val bluetoothManager : BluetoothManager, val adapter: BluetoothAdapter, val handler : Handler) {

    var gattServer: BluetoothGattServer? = null
    private var gattServerCallback: BluetoothGattServerCallback? = null

    private var profileCharacteristic: BluetoothGattCharacteristic? = null
    private var requestCharacteristic: BluetoothGattCharacteristic? = null

    private var sessionMap = mutableMapOf<String, Session>()

    private fun sendEvent(type :Int,
                          device: BluetoothDevice,
                          bytes : ByteArray?,
                          gattServer: BluetoothGattServer?,
                          req : Int?,
                          session: Session?){
        handler.obtainMessage(
            GATT,
            GattEvent(type,device,bytes,gattServer,req,session)).sendToTarget()
    }

    fun start() {

        setupGattServer(app)

    }

    fun stopServer() {
        gattServer?.close()
    }

    private fun setupGattServer(app: Application) {
        gattServerCallback = GattServerCallback()

        gattServer = bluetoothManager.openGattServer(
            app,
            gattServerCallback
        ).apply {
            addService(setupGattService())
        }
        Log.d(GATT_TAG, "Server starting")

    }

    private fun setupGattService(): BluetoothGattService {

        // Setup gatt service
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        profileCharacteristic = BluetoothGattCharacteristic(
            PROFILE_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(profileCharacteristic)

        requestCharacteristic = BluetoothGattCharacteristic(
            REQUEST_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(requestCharacteristic)

        return service

    }

    private inner class GattServerCallback : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.v(
                GATT_TAG,
                "onConnectionStateChange: Server $device ${device.name} success: $isSuccess connected: $isConnected"
            )
            if(!isConnected){
                device.let{
                    Log.e(GATT_TAG,"onConnectionState was bad am gatt")
                    sendEvent(DISCONNECT,it,null,gattServer!!,null,null)
                }
            }

        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {

//            Log.v(GATT_TAG, "onCharacteristicRead")

            device?.let {
                //create session & map & send
                val session = Session()
                sessionMap[device.address] = session
                sendEvent(HANDSHAKE,device,null,gattServer!!,requestId,session)
            }

        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value)

            value?.let {

                //gattServer?.sendResponse(device!!, requestId, 0, 0, null)
                //Got handshake send original session from map
                sendEvent(PACKET,device!!,it,gattServer,requestId,sessionMap[device.address])

            }



        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)

//            Log.d(GATT_TAG, "OnNotificationSent(): exe-thread["+Thread.currentThread().name +"]")
            device?.let{
                    dev -> val socket = runBlocking { Async.getSocket(dev) }
                socket?.response?.open()
            }

        }

    }

}