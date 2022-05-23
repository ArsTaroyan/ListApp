package am.a_t.myapplication

import am.a_t.myapplication.db.UserDatabase
import am.a_t.myapplication.db.migration_1_2
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"

class UserRepository private constructor(context: Context){

    private val database : UserDatabase = Room.databaseBuilder(
        context.applicationContext,
        UserDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2).build()

    private val userDao = database.userDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    fun getUsers() : LiveData<List<User>> = userDao.getUsers()

    fun getUser(id: UUID): LiveData<User?> = userDao.getUser(id)

    fun updateUser(user: User) {
        executor.execute {
            userDao.updateUser(user)
        }
    }

    fun addUser(user: User) {
        executor.execute {
            userDao.addUser(user)
        }
    }

    fun getPhotoFile(user: User): File = File(filesDir, user.photoFileName)

    companion object {
        private var INSTANCE: UserRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = UserRepository(context)
            }
        }

        fun get() : UserRepository {
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}