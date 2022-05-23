package am.a_t.myapplication

import androidx.lifecycle.ViewModel

class UserListViewModel : ViewModel() {

    private val userRepository = UserRepository.get()
    val userListLiveData = userRepository.getUsers()

    fun addUser(user: User) {
        userRepository.addUser(user)
    }

}