//package daemon.dev.field.util
//
//import android.os.Build
//import android.util.Log
//import androidx.annotation.RequiresApi
//import daemon.dev.field.CHARSET
//import daemon.dev.field.FILE_IO
//import daemon.dev.field.MANAGER_TAG
//import daemon.dev.field.data.PostRAM
//import daemon.dev.field.cereal.objects.UserProfile
//import daemon.dev.field.nypt.Key
//import daemon.dev.field.nypt.USER_KEY_SIZE
//import java.io.*
//import java.nio.file.Files
//import java.nio.file.Path
//import java.nio.file.Paths
//
//
//@RequiresApi(Build.VERSION_CODES.O)
//class AppDataIO(private val binData : File) {
//
//    fun addBin(name:String){
//
//        val output = BufferedWriter(FileWriter(binData, true))
//        output.append("$name:\n");
//        output.close();
//
//    }
//
//    fun removeBin(name:String){
//
//    }
//
//    /**Should always be called before createChannels!!*/
//    fun writeOfficialAppUserToSecureFile(user : UserProfile) {
//
//        //if they don't create them and initialize the data
//        val fos = FileOutputStream(binData);
//
//        var userInfo = user.alias + ":" + user.uid.get().toString(CHARSET)
//
//        // Write a line to the file
//        fos.write(userInfo.toByteArray(CHARSET));
//
//        // Close the file output stream
//        fos.close();
//
//    }
//
//    fun getOfficialAppUserStoredOnThisDevice() : UserProfile {
//        val fis = FileInputStream(binData);
//        val line : String
//
//        fis.bufferedReader(CHARSET).use { line = it.readLine() }
//
//        //val value = ByteArray(USER_KEY_SIZE)
//        //fis.read(value)
//        val userInfo = line.split(":")//value.toString(CHARSET).split(":")
//        val alias = userInfo[0]
//        val key = userInfo[1].toByteArray(CHARSET)
//        return UserProfile(alias,Key(byteArrayOf()))
//    }
//
//    fun addToFile(name:String,uid :ULong){
//
//        val path: Path = Paths.get(binData.path)
//        var lines: MutableList<String> = Files.readAllLines(path, CHARSET)
//        for (l in lines.indices){
//            if(lines[l].split(":")[0]==name){
//                lines[l] = "${lines[l].substringBefore('\n')}$uid,"
//            }
//        }
//        Files.write(path, lines, CHARSET)
//
//        lines = Files.readAllLines(path, CHARSET)
//        Log.i(FILE_IO,"Wrote $uid to $name")
//        for (l in lines.indices){
//            Log.i(FILE_IO, lines[l])
//        }
//    }
//
//    fun createChannels(channelList : HashMap<String,PostRAM.Channel>){
//
//        //if they don't create them and initialize the data
//        val fos = FileOutputStream(binData);
//
//        // Write a line to the file
//        for(b in channelList.keys){
//            fos.write("$b:\n".toByteArray(CHARSET));
//        }
//
//        // Close the file output stream
//        fos.close();
//    }
//
//    fun getChannels() : HashMap<String,PostRAM.Channel>{
//        val binList = hashMapOf<String,PostRAM.Channel>()
//
//        try {
//            BufferedReader(FileReader(binData)).use { br ->
//                var line: String?
//                br.readLine()
//                while (br.readLine().also { line = it } != null) {
//
//                    Log.i(FILE_IO,"Read line $line")
//
//                    // process the line.
//                    val serial = line!!.split(":")
//
//                    val name = serial[0]
//                    val bin = PostRAM.Channel(name)
//                    if(serial.size > 1) {
//                        for (p in serial[1].split(",")) {
//                            if (p != "")
//                                bin.postList.add(p.toULong())
//                        }
//                    }
//
//                    binList[name] = bin
//                }
//            }
//        } catch(e: FileNotFoundException){
//
//            Log.i(FILE_IO,"File not found")
//
//        }
//
//        return binList
//    }
//
//    fun removeFromFile(name: String,uid:ULong){
//        val path: Path = Paths.get(binData.path)
//        var lines: MutableList<String> = Files.readAllLines(path, CHARSET)
//        for (l in lines.indices){
//            if(lines[l].split(":")[0]==name){
//                lines[l] = lines[l].replace("$uid,","")
//            }
//        }
//        Files.write(path, lines, CHARSET)
//
//        Log.i(FILE_IO,"Removed $uid from $name")
//        lines = Files.readAllLines(path, CHARSET)
//        for (l in lines.indices){
//            Log.i(FILE_IO, lines[l])
//        }
//    }
//
//}