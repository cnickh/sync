package daemon.dev.field

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import java.util.*

var PUBLIC_KEY: ByteArray = ByteArray(0)

var PRIVATE_KEY: ByteArray = ByteArray(0)

var UNIVERSAL_KEY = "Hello uni-key"

/**Character Encoding*/
//val CHARSET = Charsets.US_ASCII
val CHARSET = Charsets.UTF_8

const val HEX = 16

const val CONNECTION_PERM = Manifest.permission.BLUETOOTH_CONNECT
const val LOCATION_FINE_PERM = Manifest.permission.ACCESS_FINE_LOCATION
const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
const val PERMISSION_REQUEST_STORAGE = 102
const val PERMISSION_REQUEST_LOCATION = 101
const val PERMISSION_REQUEST_CONNECTION = 103
/*Class TAGs*/
const val OPERATOR_TAG = "SyncOperator.kt"
const val DB_OP = "PostDatabase.kt"
const val AD_TAG = "BluetoothAdvertiser.kt"
const val GATT_TAG = "Gatt.kt"
const val MAIN_TAG = "MainActivity.kt"
const val MESH_TAG = "MeshService.kt"
const val SCANNER_TAG = "BluetoothScanner.kt"
const val GATT_RESOLVER_TAG = "GattResolver.kt"
const val SOCKET_TAG = "Socket.kt"
const val NETLOOPER_TAG = "NetworkLooper.kt"
const val ASYNC_TAG = "Async.kt"
const val PEER_NET = "PeerNetwork.kt"
const val PROFILE_TAG = "ProfileFragment.kt"
const val INBOX_TAG = "InboxFragment.kt"
const val PACKER_TAG = "Packer.kt"
const val MERGE_TAG = "CommentMerge.kt"
const val SYNC_TAG = "Sync.kt"
const val MODEL_TAG = "SyncModel.kt"
const val MF_TAG = "MessengerFragment.kt"

/*Channel Connection States*/
const val NOTIFICATION_CHANNEL = "MeshServiceChannel"
const val NOTIFICATION_ID = 1

const val MAX_PEERS = 3

/*Handshake length max size of 512*/
const val MTU = 512

const val BLE_INTERVAL = 1000L //1 second

const val SYNC_INTERVAL = 1000L //1 second

const val CONFIRMATION_TIMEOUT = 3000L //5 seconds

const val SERVER_PORT = 8888

const val DEVICE_NAME = ""
/**
 * service UUID, should be removed
 */
val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
/**
 * UUID for the profile data
 */
val PROFILE_UUID: UUID = UUID.fromString("d3f751bc-12a1-4939-a91a-3fd976867a5b")
/**
 * UUID for requests to be made
 */
val REQUEST_UUID = UUID.fromString("61163941-2a11-459e-a447-53b3332004f3")

val WIFI_UUID = UUID.fromString("ad22b5f6-19f4-4f07-9388-ce4a75b464e1")

/**
 * UUIDs for 1-1 data transfers
 */
val TRANSFER_UUIDS : Array<UUID> = arrayOf(
    UUID.fromString("a7c1a110-88eb-4709-ac11-99fa824bb3cc"),
UUID.fromString("ad22b5f6-19f4-4f07-9388-ce4a75b464e1"),
UUID.fromString("a4ab7967-93d5-479d-ae15-6a32eb25e6ed"),
UUID.fromString("23dabe19-26ec-4b3d-8730-4d6a07787972"),
UUID.fromString("a725b56a-5f1e-41ab-8d8e-abd7fd7530d2"),
UUID.fromString("d8b8229b-a0ea-4432-9f3f-72361ccfab7d"),
UUID.fromString("cb730beb-7c70-407a-bebd-65437f61265d"),
UUID.fromString("379c5e48-8155-446f-bc3c-c7569b262766"),
UUID.fromString("2fa86928-8410-47e0-9152-1c045f1eabe9"),
UUID.fromString("cb4fe060-9b18-4829-8b64-9211d998f7a7"),
UUID.fromString("e70b6254-d368-422b-8743-cb1a4def8c2d"),
UUID.fromString("c38ab577-e0cc-4353-92c8-00e5729df04f"),
UUID.fromString("29bd5b24-a47f-4d04-adaa-31af0239997c"),
UUID.fromString("31ae8a40-2bca-456c-8a17-22e2a6881f56"),
UUID.fromString("0dc6fa4e-7c24-4125-ba9a-b6b37b9a40f8"),
UUID.fromString("7ec2290c-5e53-4414-a116-3f9974cec8cd")
)
/**
 * UUID can be made here:
 * https://www.guidgenerator.com/
 *
 */

/**
 * Global extension functions
 */
fun getTime() : Long{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        SystemClock.currentNetworkTimeClock().millis()
    } else {
        System.currentTimeMillis()
    }
}

fun ByteArray.toBase64() : String {
    return Base64.getEncoder().encodeToString(this)
}

fun String.toByteArray() : ByteArray {
    return Base64.getDecoder().decode(this)
}

fun String.AdKey() : String {
    return this.toByteArray().slice(0..19).toByteArray().toBase64()
}