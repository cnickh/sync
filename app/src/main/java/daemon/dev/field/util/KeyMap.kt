package daemon.dev.field.util

import android.util.Log
import daemon.dev.field.nypt.Key

class KeyMap() {

    private val keys = mutableListOf<Key>()
    private val map = mutableListOf<Any>()

    fun put(key: Key, item : Any) : Int{
        for(k in keys.indices) {
            if (keys[k] == key) {
                keys.removeAt(k)
                map.removeAt(k)
            }
        }
        keys.add(key)
        map.add(item)
        return keys.indexOf(key)
    }

    fun get(key : Key) : Any?{
        for(k in keys.indices){
            if(keys[k] == key){
                return map[k]
            }
        }
        return null
    }

    fun getAt(i : Int) : Any? {
        return map.getOrNull(i)
    }

    fun getKeyIndex(key : Key) : Int? {
        for(k in keys.indices){
            if(keys[k] == key){
                return k
            }
        }
        return null
    }

    fun getKeyAt(i : Int) : Key? {
        return keys.getOrNull(i)
    }

    fun remove(key : Key) : Int{
        for(k in keys.indices){
            if(keys[k] == key){
                keys.removeAt(k)
                map.removeAt(k)
                return 0
            }
        }
        return -1
    }

    fun removeAt(sid : Int){
        keys.removeAt(sid)
        map.removeAt(sid)
    }

    fun getKeys() : List<Key>{
        return keys
    }

    fun getMap() : List<Any>{
        return map
    }

}