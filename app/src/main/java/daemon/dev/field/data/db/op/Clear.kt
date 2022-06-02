package daemon.dev.field.data.db.op

import android.os.Build
import androidx.annotation.RequiresApi
import daemon.dev.field.data.PostRAM

class Clear : Thread() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun run(){
        PostRAM.postDao.clear()
    }
}