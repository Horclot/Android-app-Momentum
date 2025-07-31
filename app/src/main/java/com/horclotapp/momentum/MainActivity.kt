package com.horclotapp.momentum

import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var db: SQLiteDatabase
    private lateinit var adapter: HabitAdapter
    private val habits = mutableListOf<Habit>()
    private val emojis = listOf("😊", "🏃", "📚", "💧", "🍎", "🛌", "🧘", "✍️")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация базы данных
        initDatabase()

        // Настройка RecyclerView
        val habitsGrid = findViewById<RecyclerView>(R.id.habitsGrid)
        habitsGrid.layoutManager = GridLayoutManager(this, 2)
        adapter = HabitAdapter(habits, this::onHabitClick, this::onHabitLongClick)
        habitsGrid.adapter = adapter

        // Загрузка данных
        loadHabits()

        // Кнопка добавления
        findViewById<FloatingActionButton>(R.id.addButton).setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun initDatabase() {
        db = openOrCreateDatabase("HabitsDB", MODE_PRIVATE, null)
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS habits(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT, emoji TEXT, currentStreak INTEGER)"
        )
    }

    private fun loadHabits() {
        habits.clear()
        val cursor = db.rawQuery("SELECT * FROM habits", null)
        with(cursor) {
            while (moveToNext()) {
                habits.add(
                    Habit(
                        getLong(getColumnIndexOrThrow("id")),
                        getString(getColumnIndexOrThrow("name")),
                        getString(getColumnIndexOrThrow("emoji")),
                        getInt(getColumnIndexOrThrow("currentStreak"))
                    )
                )
            }
            close()
        }
        adapter.notifyDataSetChanged()
    }

    private fun onHabitClick(habit: Habit) {
        db.execSQL(
            "UPDATE habits SET currentStreak = ${habit.currentStreak + 1} WHERE id = ${habit.id}"
        )
        habit.currentStreak += 1
        adapter.notifyItemChanged(habits.indexOf(habit))
    }

    private fun onHabitLongClick(habit: Habit): Boolean {
        val options = arrayOf("Изменить", "Удалить")
        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditHabitDialog(habit)
                    1 -> deleteHabit(habit)
                }
            }
            .show()
        return true
    }

    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_habit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val emojiGrid = dialogView.findViewById<GridView>(R.id.emojiGrid)

        emojiGrid.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emojis)
        var selectedEmoji = "😊"

        emojiGrid.setOnItemClickListener { _, _, position, _ ->
            selectedEmoji = emojis[position]
        }

        AlertDialog.Builder(this)
            .setTitle("Добавить привычку")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val name = nameInput.text.toString()
                if (name.isNotEmpty()) {
                    db.execSQL(
                        "INSERT INTO habits(name, emoji, currentStreak) " +
                                "VALUES('$name', '$selectedEmoji', 0)"
                    )
                    loadHabits()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_habit, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val emojiGrid = dialogView.findViewById<GridView>(R.id.emojiGrid)

        nameInput.setText(habit.name)
        emojiGrid.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emojis)
        var selectedEmoji = habit.emoji

        emojiGrid.setOnItemClickListener { _, _, position, _ ->
            selectedEmoji = emojis[position]
        }

        AlertDialog.Builder(this)
            .setTitle("Редактировать привычку")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = nameInput.text.toString()
                if (name.isNotEmpty()) {
                    db.execSQL(
                        "UPDATE habits SET name = '$name', emoji = '$selectedEmoji' " +
                                "WHERE id = ${habit.id}"
                    )
                    habit.name = name
                    habit.emoji = selectedEmoji
                    adapter.notifyItemChanged(habits.indexOf(habit))
                }
            }
            .show()
    }

    private fun deleteHabit(habit: Habit) {
        db.execSQL("DELETE FROM habits WHERE id = ${habit.id}")
        val position = habits.indexOf(habit)
        habits.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    data class Habit(
        val id: Long,
        var name: String,
        var emoji: String,
        var currentStreak: Int
    )

    inner class HabitAdapter(
        private val habits: List<Habit>,
        private val onItemClick: (Habit) -> Unit,
        private val onItemLongClick: (Habit) -> Boolean
    ) : RecyclerView.Adapter<HabitAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.habitName)
            val streak: TextView = view.findViewById(R.id.streakText)
            val progressView: CircleProgressView = view.findViewById(R.id.progressCircle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_habit, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val habit = habits[position]
            holder.name.text = habit.name
            holder.streak.text = "${habit.currentStreak}/60 дней"
            holder.progressView.progress = habit.currentStreak / 60f
            holder.progressView.emoji = habit.emoji

            holder.itemView.setOnClickListener { onItemClick(habit) }
            holder.itemView.setOnLongClickListener { onItemLongClick(habit) }
        }

        override fun getItemCount() = habits.size
    }
}