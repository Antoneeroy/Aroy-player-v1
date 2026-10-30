package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlaybackHistoryEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        SavedStreamEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AroyDatabase : RoomDatabase() {

    abstract fun aroyDao(): AroyDao

    companion object {
        @Volatile
        private var INSTANCE: AroyDatabase? = null

        fun getInstance(context: Context): AroyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AroyDatabase::class.java,
                    "aroy_player.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate default playlist and online sample streams
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).aroyDao()
                                dao.insertPlaylist(
                                    PlaylistEntity(
                                        name = "Favorites",
                                        description = "Your favorite videos and songs"
                                    )
                                )
                                dao.insertPlaylist(
                                    PlaylistEntity(
                                        name = "Chill Vibes",
                                        description = "Relaxing tracks and clips"
                                    )
                                )
                                // Prepopulate legal curated streaming URLs
                                dao.insertSavedStream(
                                    SavedStreamEntity(
                                        title = "Big Buck Bunny (HLS Stream)",
                                        streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                                        streamType = "HLS",
                                        category = "Online Video"
                                    )
                                )
                                dao.insertSavedStream(
                                    SavedStreamEntity(
                                        title = "Tears of Steel (Sci-Fi HD)",
                                        streamUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                                        streamType = "MP4",
                                        category = "Online Video"
                                    )
                                )
                                dao.insertSavedStream(
                                    SavedStreamEntity(
                                        title = "Chill Lo-Fi Groove Stream",
                                        streamUrl = "https://ice1.somafm.com/groovesalad-128-mp3",
                                        streamType = "AUDIO",
                                        category = "Online Radio"
                                    )
                                )
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
