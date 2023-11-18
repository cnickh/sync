/*
 * Copyright (c) 2012, AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package daemon.dev.field.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Automatic approval of push-button WiFi Direct group formation.
 *
 * While this object is intercepting approval requests and this application
 * is in the foreground, the user will not see dialogs from the WifiP2p subsystem.
 * It's recommended to only intercept requests when the application is expecting
 * incoming group requests.  Group requests with PIN values are ignored and
 * no user prompt will appear.
 *
 * This class will build for Android API 15 and later, but the
 * DialogListener interface was added in API 16.  It is safe to create
 * objects of this class and call its public methods in API 15, but no
 * dialogs will be intercepted in that case.
 */
@SuppressLint("PrivateApi")
class WifiDirectAutoAccept private constructor(context: Context?, m: WifiP2pManager?, c: WifiP2pManager.Channel?) {
    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private val dialogListener: Any?
    private var dialogInterface: Class<*>? = null
    private var dialogListenerMethod: Method? = null

    /**
     * Construct a WifiDirectAutoAccept object using the application's existing
     * manager and channel.
     */
    constructor(manager: WifiP2pManager?, channel: WifiP2pManager.Channel?) : this(null, manager, channel) {}

    /**
     * Construct a WifiDirectAutoAccept object with automatically created manager and
     * channel objects for the given context.
     */
    constructor(context: Context?) : this(context, null, null) {}

    /**
     * Enable or disable interception of Wifi Direct group formation requests.
     *
     * A user of this object should make sure to call intercept(false) before
     * deleting the object, otherwise the internal dialog listener object will
     * remain registered until the app goes in to the background.
     */
    fun intercept(enable: Boolean) {
        if (enable) {
            setDialogListener(dialogListener)
        } else {
            setDialogListener(null)
        }
    }

    /**
     * Creating a new DialogListenerProxy object that also inherits the
     * DialogListener interface.
     *
     * The caller does not need to have any awareness of the
     * DialogListener interface.
     *
     * Returns null if the object could not be created.  This can
     * happen if the DialogListener interface is not available at
     * runtime.
     */
    private fun newDialogListener(): Any? {
        val dialogListener: Any
        if (manager == null || channel == null || dialogInterface == null) {
            return null
        }
        val `object`: Any = DialogListenerProxy(manager!!, channel!!)
        val handler = InvocationHandler { proxy, method, args -> /* Look up method based only on method
                       * name.  This assumes that there are not
                       * multiple methods with the same name but
                       * varying in their parameter types.
                       */
            var proxyMethod: Method? = null
            for (m in DialogListenerProxy::class.java.methods) {
                if (m.name == method.name) {
                    proxyMethod = m
                    break
                }
            }
            if (proxyMethod != null) {
                try {
                    proxyMethod.invoke(`object`, *args)
                } catch (ex: IllegalArgumentException) {
                    Log.d(TAG, ex.javaClass.name)
                }
            } else {
                Log.d(TAG, "No method found: " + method.name)
            }
            null
        }
        dialogListener = Proxy.newProxyInstance(DialogListenerProxy::class.java.classLoader,
            arrayOf<Class<*>>(dialogInterface!!),
            handler)
        return dialogListener
    }

    /**
     * Call WifiP2pManager.setDialogListener() using reflection.
     *
     * Passing null for the listener parameter will deregister any
     * previous listener and cause the WiFi P2P framework to revert to
     * system handling of P2P group formation.
     *
     * If the method is not available at runtime, no action is taken.
     *
     * WifiP2pManager.setDialogListener() exists in Android API 16, but
     * not in earlier APIs.
     */
    private fun setDialogListener(listener: Any?) {
        if (dialogListenerMethod == null) {
            return
        }
        try {
            dialogListenerMethod!!.invoke(manager, channel, listener)
        } catch (ex: IllegalAccessException) {
            // Ignore
            Log.d(TAG, ex.javaClass.name)
        } catch (ex: InvocationTargetException) {
            // Ignore
            Log.d(TAG, ex.javaClass.name)
        }
    }

    /**
     * A DialogListenerProxy provides access to the DialogListener
     * interface and associated functionality.
     *
     * DialogListener is used to handle WiFi P2P group formation events in
     * Android API 16 and later.
     */
    private inner class DialogListenerProxy
    /**
     * Construct a DialogListenerProxy for the requested manager and channel.
     */(private val manager: WifiP2pManager, private val channel: WifiP2pManager.Channel) {
        /**
         * Callback that provides the system-generated PIN.
         */
        fun onShowPinRequested(pin: String) {
            Log.d(TAG, "onShowPinRequested: $pin")
        }

        /**
         * Callback that reports connection attempts.
         *
         * The device parameter provides information about the remote
         * device that is trying to form a P2P group.  The config
         * parameter describes the type of connection being made.
         *
         * To accept a connection request, call manager.connect.
         * config.wps.pin should be assigned within this method for
         * PIN-based group formation before passing the config to
         * manager.connect.
         */
        @SuppressLint("MissingPermission")
        fun onConnectionRequested(device: WifiP2pDevice, config: WifiP2pConfig) {
            Log.d(TAG, "onConnectionRequested")
            Log.d(TAG, "    device: " + device.deviceAddress + " " + device.deviceName)
            Log.d(TAG, "    config: " + config.wps.setup + " " + config.wps.pin)
            manager.connect(channel, config, object : ActionListener {
                override fun onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                    Log.i(TAG, "Connect success")
                }

                override fun onFailure(reason: Int) {
                    Log.e(TAG, "Connect failed")
                }
            })
        }

        /**
         * Callback to report successful registration of this object.
         */
        fun onAttached() {
            Log.d(TAG, "onAttached")
        }

        /**
         * Callback to report deregistration of this object.
         *
         * The object will be deregistered automatically if a group
         * formation event occurs when the application is in the
         * background.
         */
        fun onDetached(reason: Int) {
            Log.d(TAG, "onDetached: $reason")
        }
    }

    companion object {
        const val TAG = "WifiDirectAutoAccept"
    }

    /**
     * Construct a WifiDirectAutoAccept object using either an existing context or
     * an existing manager and channel.
     */
    init {
        Log.d(TAG, "context: " + context.hashCode())
        manager = if (context != null && m == null) {
            context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        } else {
            m
        }
        channel = if (context != null && c == null) {
            manager!!.initialize(context, context.mainLooper, null)
        } else {
            c
        }
        Log.d(TAG, "manager: " + manager.hashCode())
        Log.d(TAG, "channel: " + channel.hashCode())
        try {
            dialogInterface = Class.forName("android.net.wifi.p2p.WifiP2pManager\$DialogListener")
            dialogListenerMethod = manager!!.javaClass.getMethod("setDialogListener",
                WifiP2pManager.Channel::class.java,
                dialogInterface)
        } catch (ex: NoSuchMethodException) {
            // Ignore
            Log.d(TAG, "NoSuchMethod")
        } catch (ex: ClassNotFoundException) {
            // Ignore
            Log.d(TAG, "ClassNotFound")
        }
        dialogListener = newDialogListener()
    }
}