package com.example.venueexplorer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.adsoyad.venueexplorer.data.local.entity.CategoryEntity
import com.adsoyad.venueexplorer.data.local.entity.VenueEntity

@Database(entities = [VenueEntity:: class, CategoryEntity::class], version = 1, exportSchema = false)
abstract class VenueAppDatabase : RoomDatabase() {
    abstract fun venueDao(): VenueDAO
    abstract fun categoryDao(): CategoryEntity

    companion object{
        @Volatile //Boyle yaparak veriler cache'lenmiyor direkt ram'e aktariliyor, bu sayede tum core'lar verileri updated halleriyle gorebiliyor.
        private var INSTANCE: VenueAppDatabase? = null

        fun getDatabase(context: Context): VenueAppDatabase{
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context = context, klass = VenueAppDatabase:: class.java, "item_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
            //synchronized olmasi exection zamaninda sadece 1 adet veritabani objesi olmasini sagliyor cunku sadece 1 adet olmali.
        }

    }
}


