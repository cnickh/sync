package daemon.dev.field.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.SCANNER_TAG
import daemon.dev.field.SERVICE_UUID
import daemon.dev.field.network.handler.SCANNER
import daemon.dev.field.network.handler.ScanEvent


@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.O)
class BluetoothScanner(val context : Context, val handler: Handler) {

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothLeScanner : BluetoothLeScanner? = null
    private lateinit var scanCallback: DaemonScanCallback
    private val ScanFilterServiceUUID : ParcelUuid = ParcelUuid(SERVICE_UUID)

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
            handler.obtainMessage(SCANNER,ScanEvent(result.device)).sendToTarget()
        }

        /*onScanFailed logs an error if the scan was a failure*/
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(SCANNER_TAG, "onScanFailed: errorCode $errorCode")
        }

    }

}