package am.a_t.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class User(@PrimaryKey val id: UUID = UUID.randomUUID(),
                var name: String = "",
                var date: Date = Date(),
                var isSolved: Boolean = false,
                var suspect: String = "") {

    val photoFileName
        get() = "IMG_$id.jpg"
}