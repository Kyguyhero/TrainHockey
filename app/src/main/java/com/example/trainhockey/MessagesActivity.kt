package com.example.trainhockey

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Snackbar
import androidx.core.content.ContextCompat
import com.example.trainhockey.data.LocalUserDao

class MessagesActivity : AppCompatActivity() {

    private lateinit var messageDao: MessagesDao

    private lateinit var messageListView: ListView
    private lateinit var itemsList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var noMessagesText: TextView
    private var isCoach: Boolean = false

    // Sample placeholder data — you can replace with real data later
    // private val messages = listOf<String>() // or listOf("Welcome to TrainHockey!", "Don't forget your practice today!")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.messages)

        //Get user name
        val firstName = intent.getStringExtra("name") + " "
        val lastName = intent.getStringExtra("lastName")
        val fullName = firstName + lastName

        // Check for coach user type
        val userType = intent.getStringExtra("userType") ?: "Player"
        isCoach = userType == "Coach"

        //
        messageDao = MessagesDao(this)

        // Get variables for send message items
        val message: EditText = findViewById(R.id.createMessageText)
        val sendMessageButton: Button = findViewById(R.id.sendMessageButton)

        // If the user is a coach then make send message items visible
        if (isCoach) {
            message.visibility = View.VISIBLE
            sendMessageButton.visibility = View.VISIBLE
        }

        // Get List view on messages layout for all sent messages
        messageListView = findViewById(R.id.messageListView)
        // Get text for no new messages
        noMessagesText = findViewById(R.id.noMessagesText)


        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        // Load messages from database
        itemsList = messageDao.getAllMessages().toMutableList()
        //adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemsList)
        adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemsList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(ContextCompat.getColor(context, R.color.white)) // << Your custom color
                return view
            }
        }

        messageListView.adapter = adapter

        if (itemsList.isEmpty()) {
            noMessagesText.visibility = TextView.VISIBLE
            messageListView.visibility = ListView.GONE
        } else {
            noMessagesText.visibility = TextView.GONE
            messageListView.visibility = ListView.VISIBLE
        }


        // When send message button hit add message to database and update list view
        findViewById<Button>(R.id.sendMessageButton).setOnClickListener {
            if (message.text.toString().isNotEmpty()) {
                //itemsList.add(0, inputText)
                messageDao.insertMessage(fullName, message.text.toString())
                itemsList = messageDao.getAllMessages().toMutableList()
                adapter.notifyDataSetChanged()
                message.text.clear()
                Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Message failed to send", Toast.LENGTH_SHORT).show()
            }

        }

    }

}