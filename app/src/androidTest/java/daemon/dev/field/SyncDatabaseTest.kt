/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package daemon.dev.field


import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.data.db.*
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException


/**
 * This is not meant to be a full set of tests. For simplicity, most of your samples do not
 * include tests. However, when building the Room, it is helpful to make sure it works before
 * adding the UI.
 */

//@RunWith(AndroidJUnit4::class)
class SyncDatabaseTest {

    private lateinit var sync: PostDao
    private lateinit var uSync: UserDao

    private lateinit var db: SyncDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, SyncDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        sync = db.postDao
        uSync = db.userDao
        assertNotNull(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetRecent() {
        val title = "title"
        val body = "body"
        val time: Long = 0

        var post = Post(PUBLIC_KEY,time,title,body,"null",0)

        val obj = runBlocking {
            sync.insert(post)
            sync.getMostRecentItem()
        }

        Assert.assertNotNull(obj)

        obj?.let{
            Assert.assertEquals(title, obj.title)
            Assert.assertEquals(body, obj.body)
            Assert.assertEquals(time, obj.time)
            Assert.assertEquals(PUBLIC_KEY, obj.key)
        }

    }
    @Test
    fun insertAndGetAt(){
        val title = "title"
        val body = "body"
        val time: Long = 0

        var post = Post(PUBLIC_KEY,time,title,body,"null",0)

        val obj = runBlocking {
            sync.insert(post)
            sync.getAt(post.key,post.time)
        }


        Assert.assertNotNull(obj)

        obj?.let{
            Assert.assertEquals(title, obj.title)
            Assert.assertEquals(body, obj.body)
            Assert.assertEquals(time, obj.time)
            Assert.assertEquals(PUBLIC_KEY, obj.key)
        }

    }

    @Test
    fun insertAndGetIndex(){
        val title = "title"
        val body = "body"
        val time: Long = 0

        var post = Post(PUBLIC_KEY,time,title,body,"null",0)

        val obj = runBlocking {
            sync.insert(post)
            sync.getMostRecentItem()
        }


        Assert.assertNotNull(obj)

        obj?.let{

            val obj0 = runBlocking{
                sync.get(obj.index)
            }

            Assert.assertNotNull(obj0)

            obj0?.let{
                Assert.assertEquals(title, obj0.title)
                Assert.assertEquals(body, obj0.body)
                Assert.assertEquals(time, obj0.time)
                Assert.assertEquals(PUBLIC_KEY, obj0.key)
            }

        }

    }

    @Test
    fun userPutAndGet(){

    }
}
