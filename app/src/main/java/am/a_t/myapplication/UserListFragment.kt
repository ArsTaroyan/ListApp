package am.a_t.myapplication

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class UserListFragment : Fragment() {

    private val userListViewModel: UserListViewModel by lazy {
        ViewModelProviders.of(this).get(UserListViewModel::class.java)
    }

    interface Callbacks {
        fun onUserSelected(userId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var userRecyclerView: RecyclerView
    private var adapter: UserAdapter? = UserAdapter(emptyList())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)

        userRecyclerView =
            view.findViewById(R.id.user_recycler_view) as RecyclerView
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        userRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userListViewModel.userListLiveData.observe(
            viewLifecycleOwner,
            Observer { users ->
                users?.let {
                    updateUI(users)
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_user_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_user -> {
                val user = User()
                userListViewModel.addUser(user)
                callbacks?.onUserSelected(user.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    //updateUI
    private fun updateUI(users: List<User>) {
        adapter = UserAdapter(users)
        userRecyclerView.adapter = adapter
    }

    //RecyclerViewHolder
    private inner class UserHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private lateinit var user: User

        private val titleTextView: TextView = itemView.findViewById(R.id.user_title_list)
        private val dateTextView: TextView = itemView.findViewById(R.id.user_date_list)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.user_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(user: User) {
            this.user = user
            titleTextView.text = this.user.name
            dateTextView.text = DateFormat.format("EEEE, MMM dd, yyyy", this.user.date)
            solvedImageView.visibility = if (user.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onUserSelected(user.id)
        }

    }

    //RecyclerViewAdapter
    private inner class UserAdapter(var users: List<User>) : RecyclerView.Adapter<UserHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
            val view = layoutInflater.inflate(R.layout.list_item_user, parent, false)
            return UserHolder(view)
        }

        override fun onBindViewHolder(holder: UserHolder, position: Int) {
            val user = users[position]
            holder.bind(user)
        }

        override fun getItemCount(): Int = users.size

    }

    //newInstance
    companion object {
        fun newInstance(): UserListFragment {
            return UserListFragment()
        }
    }

}