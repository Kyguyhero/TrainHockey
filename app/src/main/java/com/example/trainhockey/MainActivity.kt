package com.example.trainhockey

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.example.trainhockey.data.LocalUserDao
import com.example.trainhockey.data.User
import com.example.trainhockey.data.WorkoutDao
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek
import java.util.Date
import java.util.Locale
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.*
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.ui.DayBinder



class MainActivity : AppCompatActivity() {

    // Helper for calendar
    class DayViewContainer(view: View) : ViewContainer(view) {
        val dayText: TextView = view.findViewById(R.id.calendarDayText)
        val dot: View = view.findViewById(R.id.dotView)
    }

    private lateinit var greetingText: TextView
    private lateinit var newMessages: TextView
    private lateinit var userDao: LocalUserDao
    private var currentUser: User? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userDao = LocalUserDao(this)
        greetingText = findViewById(R.id.greetingText)
        newMessages = findViewById(R.id.newMessages)

        val userId = intent.getStringExtra("userUID")
        if (userId != null) {
            currentUser = userDao.getUserById(userId)
        }

        val workoutDao = WorkoutDao(this)
        val workoutContainer = findViewById<LinearLayout>(R.id.todaysWorkoutContainer)
        val todayDate = getTodayDate()

        if (currentUser != null) {
            val todayWorkout = workoutDao.getWorkoutForDate(todayDate, currentUser!!.id)

            if (todayWorkout != null) {
                val (goal, exercises) = todayWorkout

                if (goal.isNotEmpty()) {
                    val goalView = TextView(this).apply {
                        text = "Goal: $goal"
                        textSize = 16f
                        setPadding(0, 0, 0, 12)
                    }
                    workoutContainer.addView(goalView)
                }

                exercises.forEach { exercise ->
                    val exView = TextView(this).apply {
                        text = "• ${exercise.name} (${exercise.sets} sets x ${exercise.reps} reps)"
                        textSize = 15f
                        setPadding(0, 4, 0, 4)
                    }
                    workoutContainer.addView(exView)
                }
            } else {
                val noWorkoutView = TextView(this).apply {
                    text = "No workout found for today"
                    textSize = 15f
                }
                workoutContainer.addView(noWorkoutView)
            }
        }

        val workoutScroll = findViewById<ScrollView>(R.id.workoutScroll)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val calendarButton = findViewById<ImageButton>(R.id.calendarButton)

        val today = LocalDate.now()
        val startMonth = YearMonth.from(today.minusMonths(1))
        val endMonth = YearMonth.from(today.plusMonths(1))

        //calendarView.setup(startMonth, endMonth, DayOfWeek.MONDAY, )
        calendarView.setup(YearMonth.now(), YearMonth.now(), DayOfWeek.MONDAY)
        calendarView.maxRowCount = 4
        //calendarView.hasBoundedHeight = true
        calendarView.scrollToMonth(YearMonth.from(today))

        val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
        val allWorkoutDates = workoutDao.getAllWorkoutDates(currentUser?.id ?: "")

        calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.dayText.text = day.date.dayOfMonth.toString()

                val dateKey = formatter.format(day.date)
                val isToday = day.date == LocalDate.now()

                if (allWorkoutDates.contains(dateKey)) {

                    container.dot.visibility = View.VISIBLE
                    container.dot.setBackgroundColor(Color.BLUE)
                    //container.dayText.setTextColor(android.graphics.Color.WHITE)
                } else {
                    container.dot.visibility = View.GONE
                    //container.dayText.setTextColor(android.graphics.Color.LTGRAY)
                }
                container.dayText.apply{
                    if(isToday){
                        setBackgroundResource(R.drawable.gradient)
                        setTextColor(Color.WHITE)
                    } else{
                        setBackgroundColor(Color.WHITE)
                        setTextColor(Color.BLACK)
                    }
                }
            }
        }

        var showingCalendar = false
        calendarButton.setOnClickListener {
            showingCalendar = !showingCalendar
            calendarView.visibility = if (showingCalendar) View.VISIBLE else View.GONE
            workoutScroll.visibility = if (showingCalendar) View.GONE else View.VISIBLE

            val workoutCard = findViewById<androidx.cardview.widget.CardView>(R.id.workoutCard)
            val dateHeader = findViewById<TextView>(R.id.dateHeader)
            val weekdayLabels = findViewById<LinearLayout>(R.id.weekdayLabels)

            if (showingCalendar) {
                workoutCard.setCardBackgroundColor(Color.TRANSPARENT)
                calendarButton.setImageResource(R.drawable.back)
                dateHeader.visibility = View.VISIBLE
                weekdayLabels.visibility = View.VISIBLE

                val currentMonth = YearMonth.now()
                val formattedMonth = currentMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))
                dateHeader.text = formattedMonth
            } else {
                workoutCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                calendarButton.setImageResource(R.drawable.calendar)
                dateHeader.visibility = View.GONE
                weekdayLabels.visibility = View.GONE
            }
        }


        greetingText.text = currentUser?.let { "Hello, ${it.name}" } ?: "Hello, Guest"

        newMessages.setOnClickListener {
            val intent = Intent(this, MessagesActivity::class.java)
            intent.putExtra("userUID", currentUser?.id)
            intent.putExtra("userType", currentUser?.userType)
            startActivity(intent)
        }

        val homeButton: AppCompatImageButton = findViewById(R.id.homeButton)
        val workoutsButton: AppCompatImageButton = findViewById(R.id.workoutsButton)
        val profileButton: AppCompatImageButton = findViewById(R.id.profileButton)

        workoutsButton.setOnClickListener {
            val intent = Intent(this, WorkoutActivity::class.java)
            intent.putExtra("userUID", currentUser?.id)
            intent.putExtra("userType", currentUser?.userType)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userUID", currentUser?.id)
            intent.putExtra("userType", currentUser?.userType)
            startActivity(intent)
        }

        homeButton.setOnClickListener {
            // Already on home
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("d MMM", Locale.getDefault())
        return sdf.format(Date())
    }
}
