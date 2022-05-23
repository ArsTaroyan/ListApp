package am.a_t.myapplication.db

import am.a_t.myapplication.User
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.util.*

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getUsers(): LiveData<List<User>>
    @Query("SELECT * FROM user WHERE id=(:id)")
    fun getUser(id: UUID): LiveData<User?>

    @Update
    fun updateUser(user: User)

    @Insert
    fun addUser(user: User)
}