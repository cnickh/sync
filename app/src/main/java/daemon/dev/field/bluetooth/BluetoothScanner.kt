package daemon.dev.field.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.SCANNER_TAG
import daemon.dev.field.SERVICE_UUID
import daemon.dev.field.network.PeerRAM
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@RequiresApi(Build.VERSION_CODES.O)
class BluetoothScanner(val context : Context) {

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothLeScanner : BluetoothLeScanner? = null
    private lateinit var scanCallback: DaemonScanCallback
    private val ScanFilterServiceUUID : ParcelUuid = ParcelUuid(SERVICE_UUID)

    val deviceLock = Mutex()
    val scannedDevices = mutableListOf<String>()

    private suspend fun getDevice(device: String) : Boolean{
        deviceLock.withLock {
            return if (scannedDevices.contains(device)) {
                false
            } else {
                scannedDevices.add(device)
                true
            }
        }
    }

    suspend fun removeDev(device : String){
        deviceLock.withLock {
            if (scannedDevices.contains(device)) {
                scannedDevices.remove(device)
            }
        }
    }

    fun startScanning() {
        Log.d(SCANNER_TAG, "startScanning")

        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = adapter.bluetoothLeScanner
        }
        scanCallback = DaemonScanCallback()
        bluetoothLeScanner?.startScan(buildScanFilters(), buildScanSettings(), scanCallback)
    }

    fun stopScanning() {
        Log.d(SCANNER_TAG, "stopScanning")
        bluetoothLeScanner?.stopScan(scanCallback)
    }

    private fun buildScanFilters(): List<ScanFilter> {
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ScanFilterServiceUUID)
            .build()
        Log.d(SCANNER_TAG, "buildScanFilters")
        return listOf(scanFilter)
    }

    private fun buildScanSettings() = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .build()

    inner class DaemonScanCallback() : ScanCallback() {

        /*onScanResults passes one result to our viewModel via addSingleItems method*/
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            var gattCallback =
                GattResolver(result.device.address,this@BluetoothScanner,
                    PeerRAM.getResolver())
            //send message to resolver
            runBlocking {
                if (getDevice(result.device.address)) {
                    Log.d(SCANNER_TAG, "Calling connect...")
                    result.device.connectGatt(context, false, gattCallback)
                }
            }
        }

        /*onScanFailed logs an error if the scan was a failure*/
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(SCANNER_TAG, "onScanFailed: errorCode $errorCode")
        }

    }

}