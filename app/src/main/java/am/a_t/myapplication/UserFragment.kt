package am.a_t.myapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.io.File
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val DATE_FORMAT = "EEE, MMM, dd"

class UserFragment : Fragment(), DatePickerFragment.Callbacks {

    private lateinit var user: User
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var nameField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private val userDetailViewModel:UserDetailViewModel by lazy {
        ViewModelProviders.of(this).get(UserDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = User()
        val userId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        userDetailViewModel.loadUser(userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        nameField = view.findViewById(R.id.user_name) as EditText
        dateButton = view.findViewById(R.id.user_date) as Button
        solvedCheckBox = view.findViewById(R.id.user_solved) as CheckBox
        reportButton = view.findViewById(R.id.user_report) as Button
        suspectButton = view.findViewById(R.id.user_suspect) as Button
        photoButton = view.findViewById(R.id.user_camera)  as ImageButton
        photoView = view.findViewById(R.id.user_photo) as ImageView

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userDetailViewModel.userLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { user ->
                user?.let {
                    this.user = user
                    photoFile = userDetailViewModel.getPhotoFile(user)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "am.a_t.myapplication.fileprovider",
                        photoFile)
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                user.name = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }

        nameField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                user.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(user.date).apply {
                setTargetFragment(this@UserFragment, REQUEST_DATE)
                show(this@UserFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getUserReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.user_report_subject)
                )
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                PackageManager.MATCH_DEFAULT_ONLY)
            if(resolvedActivity == null) {
                isEnabled = false
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        userDetailViewModel.saveUser(user)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    private fun updateUI() {
        nameField.setText(user.name)
        dateButton.text = DateFormat.format("EEEE, MMM dd, yyyy", user.date)
        solvedCheckBox.apply {
            isChecked = user.isSolved
            jumpDrawablesToCurrentState()
        }
        if(user.suspect.isNotEmpty()) {
            suspectButton.text = user.suspect
        }
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = requireActivity().contentResolver
                    .query(contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }

                    it.moveToFirst()
                    val suspect = it.getString(0)
                    user.suspect = suspect
                    userDetailViewModel.saveUser(user)
                    suspectButton.text = suspect
                }
            }

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }

    private fun getUserReport(): String {

        val solvedString = if (user.isSolved) {
            getString(R.string.user_report_solved)
        } else {
            getString(R.string.user_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, user.date).toString()
        var suspect = if (user.suspect.isBlank()) {
            getString(R.string.user_report_suspect)
        } else {
            getString(R.string.user_report_no_suspect)
        }

        return getString(R.string.user_report_text, user.name, dateString, solvedString, suspect)

    }

    companion object {

        fun newInstance(userId: UUID): UserFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, userId)
            }
            return UserFragment().apply {
                arguments = args
            }
        }
    }

    override fun onDateSelected(date: Date) {
        user.date = date
        updateUI()
    }
}