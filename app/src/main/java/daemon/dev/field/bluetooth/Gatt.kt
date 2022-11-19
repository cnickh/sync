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
import daemon.dev.field.network.handler.*


@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
class Gatt(val app : Application, val handler : Handler) {

    private val advertiser : BluetoothAdvertiser = BluetoothAdvertiser()
    private var bluetoothManager: BluetoothManager? = null
    var gattServer: BluetoothGattServer? = null
    private var gattServerCallback: BluetoothGattServerCallback? = null

    private var profileCharacteristic: BluetoothGattCharacteristic? = null
    private var requestCharacteristic: BluetoothGattCharacteristic? = null

    private fun sendEvent(type :Int, device: BluetoothDevice, bytes : ByteArray?, gattServer: BluetoothGattServer?, req : Int?){
        handler.obtainMessage(
            GATT,
            GattEvent(type,device,bytes,gattServer,req)).sendToTarget()
    }

    fun start() {

        if(bluetoothManager == null) {
            bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }

        setupGattServer(app)
        advertiser.startAdvertisement()

    }

    fun stopServer() {
        gattServer?.close()
        advertiser.stopAdvertising()
    }

    fun startAdvertising(){
        advertiser.startAdvertisement()
    }

    fun stopAdvertising(){
        advertiser.stopAdvertising()
    }

    private fun setupGattServer(app: Application) {
        gattServerCallback = GattServerCallback()

        gattServer = bluetoothManager!!.openGattServer(
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
                device.let{ device ->
                    Log.e(GATT_TAG,"onConnectionState was bad am gatt do nothing")
                    //sendEvent(DISCONNECT,device,null,gattServer!!,null)
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
                sendEvent(HANDSHAKE,device,null,gattServer!!,requestId)
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
                sendEvent(PACKET,device!!,it,gattServer,requestId)

            }



        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)

            device?.let{
                    dev -> val socket = runBlocking { Async.getSocket(dev) }
                socket?.let{ it.response.open()}
            }

        }

    }

}