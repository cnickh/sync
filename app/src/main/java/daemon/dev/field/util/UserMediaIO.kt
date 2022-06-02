package daemon.dev.field.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getContentUri
import androidx.annotation.RequiresApi

class UserMediaIO(val context : Context) {

    // Container for information about each video.
    data class Image(
        val id: Long,
        val uri: Uri,
        val name: String,
        val size: Int,
    )

    fun getUserImages() : MutableList<Image>{


        val imageList = mutableListOf<Image>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
        )

        //optional search constraints
        val selection = null
        val selectionArgs = null

        val sortOrder = "${MediaStore.Images.Media.DEFAULT_SORT_ORDER}"

        //make query
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                // Get values of columns
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val size = cursor.getInt(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                imageList += Image(id,contentUri,name,size)
            }
        }

        return imageList
    }


//    // Load thumbnail of a specific media item.
//    val thumbnail: Bitmap =
//        applicationContext.contentResolver.loadThumbnail(
//            content-uri, Size(640, 480), null)

}