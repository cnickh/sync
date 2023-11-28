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
import android.os.Handler
import android.util.Log
import daemon.dev.field.*
import daemon.dev.field.cereal.objects.HandShake
import daemon.dev.field.cereal.objects.KeyBundle
import daemon.dev.field.network.Socket
import kotlinx.coroutines.runBlocking
import daemon.dev.field.network.util.NetworkEventDefinition.*
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.GATT
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.CONNECT
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.DISCONNECT
import daemon.dev.field.network.util.NetworkEventDefinition.Companion.PACKET
import daemon.dev.field.nypt.Session
import daemon.dev.field.nypt.Signature
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.util.Base64


@SuppressLint("MissingPermission")
class Gatt(val app: Application, val bluetoothManager : BluetoothManager, val adapter: BluetoothAdapter, val handler : Handler,  val shake: HandShake) {

    var gattServer: BluetoothGattServer? = null
    private var gattServerCallback: BluetoothGattServerCallback? = null

    private var profileCharacteristic: BluetoothGattCharacteristic? = null
    private var requestCharacteristic: BluetoothGattCharacteristic? = null

    private var sessionMap = mutableMapOf<String, Session?>()
    private var socketMap = mutableMapOf<String, Socket?>()


    fun forget(device : String){
        socketMap[device]?.let{
            sendEvent(DISCONNECT,null, it)
        }
        sessionMap[device] = null
        socketMap[device] = null
    }

    private fun sendEvent(type :Int,
                          bytes : ByteArray?,
                          socket : Socket
                        ){
        handler.obtainMessage(
            GATT,
            GattEvent(type,bytes,socket)).sendToTarget()
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

            if(!isSuccess || !isConnected){
                forget(device.address)
            }

        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {

            Log.v(GATT_TAG, "onCharacteristicRead $device")

            device?.let {
                Log.v(GATT_TAG, "have device ${device.address}")

                val session = Session()
                sessionMap[device.address] = session
                socketMap[device.address] = null

                //create session & map & send
                shake.keyBundle = session.createSecret()
                val json = Json.encodeToString(shake)

                Log.v(GATT_TAG,"Sending shake : $json")

                gattServer?.sendResponse(
                    device, requestId, 0, 0, json.toByteArray(CHARSET))
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

            if (device != null) {
                Log.i(GATT_TAG, "Gatt have write from ${device.address} gatt $gattServer")
            }

            value?.let { bytes->

                gattServer?.sendResponse(
                    device!!, requestId, 200, 0, null)

                var sock = socketMap[device!!.address]

                if(sock == null){

                    val json = bytes.toString(CHARSET)

                    Log.v(GATT_TAG,"Have shake : $json")

                    val shake = try{
                        Json.decodeFromString<HandShake>(json)
                    }catch(e : Exception){
                        null
                    }

                    shake?.let {
                        sock =
                            sessionMap[device.address]?.let { session ->
                                val key = session.computeSharedKey(it.keyBundle!!,it.me.key)
                                Log.i(GATT_TAG, "Gatt establishing key ${key!!.toBase64()}")

                                Socket(
                                    it.me,
                                    Socket.BLUETOOTH_DEVICE,
                                    null,
                                    null,
                                    device,
                                    gattServer,
                                    key
                                )
                            }
                        socketMap[device.address] = sock!!

                        sendEvent(CONNECT,null, sock!!)

                    }


                }else{
                    sendEvent(PACKET,bytes, sock!!)
                }

            }

        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)

            device?.let{ dev->
                Log.d(GATT_TAG, "OnNotificationSent(): exe-thread["+Thread.currentThread().name +"] from device[${device.address}]")
                socketMap[dev.address]?.response?.open()
            }

        }

    }

}