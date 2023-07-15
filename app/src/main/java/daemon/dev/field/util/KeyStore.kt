package daemon.dev.field.util

import android.content.Context
import android.util.Log
import daemon.dev.field.PRIVATE_KEY
import daemon.dev.field.PUBLIC_KEY
import java.io.*
import java.util.*

class KeyStore(val context : Context) {

    val KEY_STORE = "sampleFile.txt"

    var keyFile : File = File(context.filesDir, KEY_STORE)

    fun clear(){
        keyFile.delete()
    }

    fun checkKey() : Boolean {
        return !keyFile.createNewFile()
    }

    fun privateKey() : ByteArray?{

        var bytes : ByteArray? = null

        try {
            val inputStream: InputStream = keyFile.inputStream()
            val lineList = mutableListOf<String>()

            inputStream.bufferedReader().forEachLine {
                lineList.add(it)

                val key_array = it.split(":")
                if(key_array[0] == "PRIVATE"){
                    bytes = key_array[1].toByteArray()
                }

            }
            lineList.forEach{println(">  " + it)}
        } catch(e: FileNotFoundException){
            Log.d("Main","File not found")
        }

        return bytes
    }


    fun publicKey() : ByteArray?{

        var bytes : ByteArray? = null

        try {

          val inputStream: InputStream = keyFile.inputStream()
          val lineList = mutableListOf<String>()

          inputStream.bufferedReader().forEachLine {
              lineList.add(it)

              val key_array = it.split(":")
              if(key_array[0] == "PUBLIC"){
                  bytes = key_array[1].toByteArray()
              }

          }
          lineList.forEach{println(">  " + it)}

        } catch(e: FileNotFoundException){
            Log.d("Main","File not found")
        }

        return bytes
    }

    fun storeKeys(){
        val privateIn = PRIVATE_KEY.toBase64()
        val publicIn = PUBLIC_KEY.toBase64()

        val output = BufferedWriter(FileWriter(keyFile, true))
        output.append("PUBLIC:$publicIn\nPRIVATE:$privateIn\n");
        output.close();

        val publicOut = publicKey()!!.toBase64()
        val privateOut = privateKey()!!.toBase64()


        Log.d("Main","publicIn{$publicIn} publicOut{$publicOut} \n privateIn{$privateIn} privateOut{$privateOut}")

    }

    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }

    private fun String.toByteArray() : ByteArray {
        return Base64.getDecoder().decode(this)
    }
}