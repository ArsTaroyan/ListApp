package am.a_t.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*

class UserDetailViewModel : ViewModel() {

    private val userRepository = UserRepository.get()
    private val userIdLiveData = MutableLiveData<UUID>()

    var userLiveData: LiveData<User?> =
        Transformations.switchMap(userIdLiveData) { userId ->
             userRepository.getUser(userId)
        }

    fun loadUser(userId: UUID) {
        userIdLiveData.value = userId
    }

    fun saveUser(user: User) {
        userRepository.updateUser(user)
    }

    fun getPhotoFile(user: User): File {
        return userRepository.getPhotoFile(user)
    }
}