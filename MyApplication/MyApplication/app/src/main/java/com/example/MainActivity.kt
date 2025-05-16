package com.example.myapplication

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Scroller
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Message(
    val sender: String,
    val receiver: String,
    val timestamp: String,
    val content: String
)
class MainActivity : AppCompatActivity() {
    companion object {
        var currentUserEmail: String? = null
        var authToken: String? = null
        var currentUserId: Int? = null  // user_id global
        var nume: String? = null
        var rol: String? = null
        var bibliografie: String? = null
        var preferinte: String? = null
        var parola: String? = null
    }
    private val selectedAllergens = mutableListOf<String>()
    private val selectedMealTypes = mutableListOf<String>()
    private var selectedDietType: String? = null
    private var minimum_value_time: String? = null
    private var maximum_value_time: String? = null
    private lateinit var layout: LinearLayout
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "UserPrefs"
    private val USERS_KEY = "users"
    private lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        //resetAllData()
        showLoadingScreen()
        //Thread.sleep(1000)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


        // Așteaptă câteva secunde înainte de a ascunde loading screen
        Handler(Looper.getMainLooper()).postDelayed({
            // După 3 secunde, ascunde loading screen-ul
            hideLoadingScreen()

            // Aici poți adăuga codul pentru a continua logica aplicației tale
            // De exemplu, poți lansa un nou activitate sau iniția un alt proces
        }, 3000) // 3000 ms = 3 secunde
        layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(50, 100, 50, 100)
        }

        currentUserEmail?.let {
            showHomePage(it)  // Folosește email-ul pentru a arăta pagina principală
        } ?: showInitialMenu()
        //showInitialMenu()
        setContentView(layout)
    }
    fun showHistoryUI() {
        val context = this

        // Istoric cu datele asociate
        val fullHistory = listOf(
            ("Ce pot mânca dimineața pentru masă musculară?" to "Omletă cu avocado și pâine integrală.") to "2025-05-05",
            ("Cum arată un plan cardio eficient?" to "30 min alergare + 15 min HIIT de 3 ori pe săptămână.") to "2025-05-04",
            ("Există ciorbe low-carb gustoase?" to "Ciorbă de curcan cu legume verzi și fără cartofi.") to "2025-05-03",
            ("Idei pentru rețete fără gluten?" to "Tocăniță de quinoa cu legume.") to "2025-05-02",
            ("Plan vegan pentru întreaga săptămână?" to "Micul dejun: budincă de chia, Prânz: năut curry, Cină: linte cu legume.") to "2025-05-01"
        )
        // Scroll principal
        val scrollView = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 32, 32, 32)
        }

        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Back button
        val backButton = ImageButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(120, 120).apply {  // Dimensiuni mai mari pentru vizibilitate
                bottomMargin = 16
            }
            setImageResource(R.drawable.image3)  // Înlocuiește cu drawable-ul dorit
            background = null
            setPadding(0, 0, 0, 0)  // Elimina padding-ul (poate afecta dimensiunea)

            // Setează modul de scalare a imaginii pentru a se adapta perfect la dimensiunile butonului
            scaleType = ImageView.ScaleType.CENTER_CROP  // Imaginea va fi centrată și va umple complet butonul
            adjustViewBounds = true

            setOnClickListener {
                recreate()
                showHomePage(currentUserEmail.toString())  // Exemplu de home page
                Log.d("BACK_BUTTON", "Email: ${currentUserEmail.toString()}")  // Vezi în Logcat
            }
        }

        // Titlu
        val titleText = TextView(context).apply {
            text = "🕘 Search history"
            textSize = 24f
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, 24)
            gravity = Gravity.CENTER
        }

        // Layout căutare + buton X
        val searchLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 24)
        }

        val searchInput = EditText(context).apply {
            hint = "🔍 Find in history"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val clearButton = TextView(context).apply {
            text = "❌"
            textSize = 20f
            setPadding(16, 0, 0, 0)
            setOnClickListener {
                searchInput.setText("")
            }
        }

        searchLayout.addView(searchInput)
        searchLayout.addView(clearButton)

        // Layout care va conține dinamically întrebările/ răspunsurile
        val historyContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        // Funcție care populează containerul cu Q&A filtrate
        fun updateHistoryList(filter: String) {
            historyContainer.removeAllViews()

            val filtered = fullHistory.filter { (questionAnswer, date) ->
                val (question, answer) = questionAnswer // Destructurarea corectă a `Pair<String, String>`
                // Căutăm în întrebări, răspunsuri sau dată
                question.contains(filter, ignoreCase = true) ||
                        answer.contains(filter, ignoreCase = true) ||
                        date.contains(filter)  // Căutare în dată (ex: "2025-05-03")
            }

            filtered.forEach { (questionAnswer, date) ->
                val (question, answer) = questionAnswer // Destructurarea corectă a `Pair<String, String>`

                val questionText = TextView(context).apply {
                    text = "❓ $question"
                    textSize = 18f
                    setTextColor(Color.BLACK)
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, 16, 0, 4)
                }

                val answerText = TextView(context).apply {
                    text = "💡 $answer"
                    textSize = 17f
                    setTextColor(Color.DKGRAY)
                    setPadding(0, 0, 0, 16)
                    setBackgroundColor(Color.parseColor("#EFEFEF"))
                }

                val dateText = TextView(context).apply {
                    text = "📅 $date"
                    textSize = 14f
                    setTextColor(Color.GRAY)
                    setPadding(0, 4, 0, 16)
                }

                historyContainer.addView(questionText)
                historyContainer.addView(answerText)
                historyContainer.addView(dateText)

                // Spațiu între seturi Q&A
                val spacer = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        12
                    )
                }
                historyContainer.addView(spacer)
            }
        }

        // Inițial afișează tot
        updateHistoryList("") // Afișează toate datele inițiale

        // Căutare în timp real
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateHistoryList(s.toString()) // Actualizează lista în funcție de căutare
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        mainLayout.apply {
            addView(backButton)
            addView(titleText)
            addView(searchLayout)
            addView(historyContainer)
        }

        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }
    private fun showLoadingScreen() {
        loadingDialog = Dialog(this).apply {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(64, 64, 64, 64)
                setBackgroundColor(Color.TRANSPARENT)

                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 48f
                    setColor(Color.WHITE)
                    setStroke(2, Color.LTGRAY)
                }

                // ProgressBar cu spațiu dedesubt
                val progressBar = ProgressBar(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 40
                    }
                    isIndeterminate = true
                }

                // Text de loading
                val loadingText = TextView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = "🔄 Loading..."
                    textSize = 20f
                    setTextColor(Color.DKGRAY)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                }

                addView(progressBar)
                addView(loadingText)
            }

            setContentView(layout)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            window?.setGravity(Gravity.CENTER)
            setCancelable(false)
            show()
        }
    }



    private fun hideLoadingScreen() {
        // După finalizarea încărcării, închide dialogul
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
    fun getCircularBitmap(context: Context, drawableId: Int, size: Int = 200): Bitmap {
        val original = BitmapFactory.decodeResource(context.resources, drawableId)

        // Redimensionăm imaginea la dimensiunea butonului (ex: 200x200)
        val scaled = Bitmap.createScaledBitmap(original, size, size, true)

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)

        return output
    }


    private fun showInitialMenu() {
        layout.removeAllViews()
        val logoImageView = ImageView(this).apply {
            setImageResource(R.drawable.logo)  // Înlocuiește cu numele corect al fișierului
            layoutParams = LayoutParams(800, 800).apply {  // Lățime 400px și înălțime 200px
                setMargins(0, 0, 0, 50)  // Marja pentru logo între titlu
            }
        }

        val titleTextView = TextView(this).apply {
            text = "Welcome to Actify"
            textSize = 55f
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER

            // Setează un font cursiv
            typeface = Typeface.create("cursive", Typeface.NORMAL) // Folosește un font cursiv

            // Setează culoarea textului
            setTextColor(Color.parseColor("#096A09"))  // O culoare vibrantă (verde în acest caz)

            // Adăugarea unui efect de umbră pentru a face textul să iasă în evidență
            setShadowLayer(2f, 1f, 1f, Color.BLUE)  // (raza, offset pe X, offset pe Y, culoarea umbrei)

            // Adăugăm margini și setăm layout-ul
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 0, 0, 150)  // Marjă pentru a lăsa spațiu între titlu și butoane
            }
        }
        emailEditText = EditText(this).apply {
            hint = "Email"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        passwordEditText = EditText(this).apply {
            hint = "Password"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val registerButton = Button(this).apply {
            text = "Register"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        val alreadyHaveAccountText = TextView(this).apply {
            text = "Already have an account?"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 20, 0, 10)
            }
        }
        val loginLinkText = TextView(this).apply {
            text = "Login"
            textSize = 18f
            setTextColor(Color.parseColor("#0000EE")) // Albastru tipic pentru linkuri
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG // Sublinează textul
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setOnClickListener {
                showLoginUI()
            }
        }
        val loginLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 20, 0, 10)
            }

            val infoText = TextView(this@MainActivity).apply {
                text = "Already have an account? "
                textSize = 25f
                setTextColor(Color.DKGRAY)
            }

            val loginLink = TextView(this@MainActivity).apply {
                text = "Login"
                textSize = 25f
                setTextColor(Color.parseColor("#0000EE")) // Link-style albastru
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener {
                    showLoginUI()
                }
            }

            addView(infoText)
            addView(loginLink)
        }
        val exitButton = Button(this).apply {
            text = "Exit"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        //layout.setBackgroundColor(Color.BLACK)

        layout.addView(titleTextView)
        layout.addView(logoImageView)
        //loginButton.setOnClickListener { showLoginUI() }
        registerButton.setOnClickListener { showRegisterUI() }
        exitButton.setOnClickListener {
            finish()
        }
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER
            setPadding(16, 32, 16, 16)
        }
        inputLayout.addView(emailEditText)
        inputLayout.addView(passwordEditText)
        inputLayout.addView(registerButton)
        inputLayout.addView(loginLayout)
        inputLayout.addView(exitButton)
        val frameLayout = FrameLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        frameLayout.addView(inputLayout)
        val backLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        }
        //frameLayout.addView(backButton, backLayoutParams)
        layout.addView(frameLayout)
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            Thread {
                try {
                    val json = JSONObject()
                    json.put("email", email)
                    json.put("password_hash", password)
                    val url = URL("http://10.0.2.2:8000/api/auth/register") // corect pentru emulator
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true
                    val input = json.toString().toByteArray(Charsets.UTF_8)
                    conn.outputStream.use { os ->
                        os.write(input, 0, input.size)
                    }
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    Log.d("RegisterResult", "Mesaj: $message")
                    //Log.d("LoginResult", "Token: $token")
                    runOnUiThread {
                        Toast.makeText(this, "Răspuns: $response", Toast.LENGTH_LONG).show()
                    }
                    if (message == "registered") {
                        Log.d("Register Succesfull", "Login: $message")
                        runOnUiThread {
                            MainActivity.currentUserEmail = email
                            Toast.makeText(this, "Register Successful", Toast.LENGTH_SHORT).show()
                            showHomePage(email)
                        }
                        //am dat register, trebuie login si sa cer toate datele despre user
                        try {
                            val json = JSONObject()
                            json.put("email", email)
                            json.put("password_hash", password)
                            val url = URL("http://10.0.2.2:8000/api/auth/login") // corect pentru emulator
                            val conn = url.openConnection() as HttpURLConnection
                            conn.requestMethod = "POST"
                            conn.setRequestProperty("Content-Type", "application/json")
                            conn.doOutput = true
                            val input = json.toString().toByteArray(Charsets.UTF_8)
                            conn.outputStream.use { os ->
                                os.write(input, 0, input.size)
                            }
                            val response = conn.inputStream.bufferedReader().use { it.readText() }
                            val jsonResponse = JSONObject(response)
                            val message = jsonResponse.getString("message")
                            val token = jsonResponse.getString("token")
                            Log.d("LoginResult", "Mesaj: $message")
                            //Log.d("LoginResult", "Token: $token")
                            runOnUiThread {
                                Toast.makeText(this, "Răspuns: $response", Toast.LENGTH_LONG).show()
                            }
                            if (message == "logged in") {
                                Log.d("Login Succesfull", "Login: $message")
                                val url = URL("http://10.0.2.2:8000/api/users/email/$email") // corect pentru emulator
                                val conn = url.openConnection() as HttpURLConnection
                                conn.requestMethod = "GET"
                                conn.setRequestProperty("Content-Type", "application/json")
                                conn.setRequestProperty("Authorization", "Bearer $token")
                                val response = try {
                                    conn.inputStream.bufferedReader().use { it.readText() }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }

                                // Dacă răspunsul este valid
                                if (response != null) {
                                    val jsonResponse = JSONObject(response)
                                    Log.d("JSON Response", jsonResponse.toString())
                                    val userId = jsonResponse.getInt("user_id")  // Extrage user_id ca integer
                                    val email = jsonResponse.getString("email")  // Extrage email ca string
                                    val name = jsonResponse.optString("name", "Unknown")  // Extrage name, folosind optString pentru a evita null
                                    val role = jsonResponse.optString("role", "Unknown")  // Extrage role, folosind optString pentru a evita null
                                    val bio = jsonResponse.optString("bio", "No bio")  // Extrage bio, folosind optString pentru a evita null
                                    val preferences = jsonResponse.optString("preferences", "No preferences")  // Extrage preferences, folosind optString pentru a evita null
                                    Log.d("JSON Response", "user_id: $userId")
                                    runOnUiThread {
                                        MainActivity.authToken = token
                                        MainActivity.currentUserEmail = email
                                        MainActivity.currentUserId = userId
                                        MainActivity.nume = name
                                        MainActivity.rol = role
                                        MainActivity.bibliografie = bio
                                        MainActivity.preferinte = preferences
                                        MainActivity.parola = password
                                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                        showHomePage(email)
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                runOnUiThread {
                                    MainActivity.authToken = token;
                                    MainActivity.currentUserEmail = email
                                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                    showHomePage(email)
                                }
                            }
                            else
                            {
                                runOnUiThread {
                                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                Toast.makeText(this, "Eroare: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    else
                    {
                        runOnUiThread {
                            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Eroare: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }
    private fun markConversationAsRead(currentUser: String, otherUser: String) {
        val prefs = getSharedPreferences("chat_read_status", MODE_PRIVATE)
        val editor = prefs.edit()

        val messages = getChatMessages(currentUser, otherUser).split("\n")
        val lastMessageTimestamp = messages.lastOrNull()?.split("|")?.getOrNull(1)

        if (lastMessageTimestamp != null) {
            editor.putString("$currentUser-$otherUser", lastMessageTimestamp)
            editor.apply()
        }
    }
    private fun resizeDrawable(drawableResId: Int, width: Int, height: Int): Drawable {
        val original = ContextCompat.getDrawable(this, drawableResId)!!
        val bitmap = (original as BitmapDrawable).bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        return BitmapDrawable(resources, scaledBitmap)
    }
    private fun RoutePlanner() {
        layout.removeAllViews()
        layout.setPadding(32, 32, 32, 32)

        val titleText = TextView(this).apply {
            text = "🌍 Route Planner"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 32
            }
        }

        val startPointEditText = EditText(this).apply {
            hint = "Where you start"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 24
            }
            setPadding(24, 24, 24, 24)
            typeface = Typeface.DEFAULT_BOLD
            textSize = 22f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            background = resizeDrawable(R.drawable.edit_text_background, 800, 50)
        }

        val destinationEditText = EditText(this).apply {
            hint = "Your destination"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 24
            }
            setPadding(24, 24, 24, 24)
            typeface = Typeface.DEFAULT_BOLD
            textSize = 22f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            background = resizeDrawable(R.drawable.edit_text_background, 800, 50)
        }

        fun showPopupMenu(view: View, resultTextView: TextView) {
            val popupMenu = PopupMenu(this, view)
            val menu = popupMenu.menu

            menu.add("🚶‍♂️ Walking")
            menu.add("🚴‍♀️ With bike")

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "🚶‍♂️ Walking" -> {
                        resultTextView.text = "🚶‍♂️ Walking"
                        Toast.makeText(this, "Căutare rută pentru mers pe jos...", Toast.LENGTH_SHORT).show()
                    }
                    "🚴‍♀️ With bike" -> {
                        resultTextView.text = "🚴‍♀️ With bike"
                        Toast.makeText(this, "Căutare rută pentru bicicletă...", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }

            popupMenu.show()
        }

        val modeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 24
            }
        }

        val menuButton = Button(this).apply {
            text = "Choose Mode"
            layoutParams = LayoutParams(500, LayoutParams.WRAP_CONTENT).apply {
                marginEnd = 16
            }
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTextColor(Color.WHITE)
            textSize = 18f
            setPadding(16, 16, 16, 16)
        }

        val resultTextView = TextView(this).apply {
            text = "Select Mode"
            textSize = 18f
            setTextColor(Color.BLACK)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        modeLayout.addView(menuButton)
        modeLayout.addView(resultTextView)

        val mapPlaceholder = FrameLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0).apply {
                height = 0
                weight = 1f
            }
            setBackgroundColor(Color.LTGRAY)
            id = View.generateViewId()
            setPadding(8, 8, 8, 8)
        }

        // Butonul circular "Back"
        val backButton = Button(this).apply {
            text = "←"  // Poti folosi simbolul săgeată sau alt text pentru back
            layoutParams = LayoutParams(100, 100).apply {
                topMargin = 32
                leftMargin = 32
            }
            setBackgroundColor(Color.parseColor("#FF4081"))
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
            isAllCaps = false
            // Aplicăm un fundal rotund pentru buton
            background = resources.getDrawable(R.drawable.image3)
        }

        // Organizăm componentele în layout-ul principal
        layout.addView(backButton)
        layout.addView(titleText)
        layout.addView(startPointEditText)
        layout.addView(destinationEditText)
        layout.addView(modeLayout)
        layout.addView(mapPlaceholder)

        // Setăm listener-ul pentru butonul "Choose Mode"
        menuButton.setOnClickListener {
            showPopupMenu(menuButton, resultTextView)
        }
        backButton.setOnClickListener {
            showHomePage(MainActivity.currentUserEmail.toString())
        }
    }



    private fun showLoginUI() {
        layout.removeAllViews()
        val usersJson = sharedPreferences.getString(USERS_KEY, "{}")
        Log.d("Login", "Loaded users: $usersJson")

        emailEditText = EditText(this).apply {
            hint = "Email"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        passwordEditText = EditText(this).apply {
            hint = "Password"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }


        val loginButton = Button(this).apply {
            text = "Login"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        val backButton = Button(this).apply {
            text = "Back"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        backButton.setOnClickListener {
            showInitialMenu()
        }
        layout.addView(emailEditText)
        layout.addView(passwordEditText)
        layout.addView(loginButton)
        layout.addView(backButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            //val usersJson = sharedPreferences.getString(USERS_KEY, "{}")
            //val users = JSONObject(usersJson)
            Thread {
                try {
                    val json = JSONObject()
                    json.put("email", email)
                    json.put("password_hash", password)
                    val url = URL("http://10.0.2.2:8000/api/auth/login") // corect pentru emulator
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true
                    val input = json.toString().toByteArray(Charsets.UTF_8)
                    conn.outputStream.use { os ->
                        os.write(input, 0, input.size)
                    }
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("message")
                    val token = jsonResponse.getString("token")
                    Log.d("LoginResult", "Mesaj: $message")
                    //Log.d("LoginResult", "Token: $token")
                    runOnUiThread {
                        Toast.makeText(this, "Răspuns: $response", Toast.LENGTH_LONG).show()
                    }
                    if (message == "logged in") {
                        Log.d("Login Succesfull", "Login: $message")
                        val url = URL("http://10.0.2.2:8000/api/users/email/$email") // corect pentru emulator
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.setRequestProperty("Content-Type", "application/json")
                        conn.setRequestProperty("Authorization", "Bearer $token")
                        val response = try {
                            conn.inputStream.bufferedReader().use { it.readText() }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }

                        // Dacă răspunsul este valid
                        if (response != null) {
                            val jsonResponse = JSONObject(response)
                            Log.d("JSON Response", jsonResponse.toString())
                            val userId = jsonResponse.getInt("user_id")  // Extrage user_id ca integer
                            val email = jsonResponse.getString("email")  // Extrage email ca string
                            val name = jsonResponse.optString("name", "Unknown")  // Extrage name, folosind optString pentru a evita null
                            val role = jsonResponse.optString("role", "Unknown")  // Extrage role, folosind optString pentru a evita null
                            val bio = jsonResponse.optString("bio", "No bio")  // Extrage bio, folosind optString pentru a evita null
                            val preferences = jsonResponse.optString("preferences", "No preferences")  // Extrage preferences, folosind optString pentru a evita null
                            Log.d("JSON Response", "user_id: $userId")
                            runOnUiThread {
                                MainActivity.authToken = token
                                MainActivity.currentUserEmail = email
                                MainActivity.currentUserId = userId
                                MainActivity.nume = name
                                MainActivity.rol = role
                                MainActivity.bibliografie = bio
                                MainActivity.preferinte = preferences
                                MainActivity.parola = password
                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                showHomePage(email)
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                            }
                        }

                        runOnUiThread {
                            MainActivity.authToken = token;
                            MainActivity.currentUserEmail = email
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            showHomePage(email)
                        }
                    }
                    else
                    {
                        runOnUiThread {
                            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Eroare: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }
    private fun showSavedUI(email: String) {
        layout.removeAllViews()

        val parentLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5")) // Fundal general
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                bottomMargin = 150 // Spațiu pentru butonul fixat
            }
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 200, 32, 32)
        }

        fun createSavedSection(title: String, items: List<String>): CardView {
            val card = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 48) // Spațiu între secțiuni
                }
                radius = 24f
                cardElevation = 10f
                setContentPadding(40, 40, 40, 40)
                setCardBackgroundColor(Color.WHITE)
                minimumHeight = 600 // ✅ Face secțiunea vizibil mai mare
            }

            val sectionLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val titleView = TextView(this).apply {
                text = title
                textSize = 22f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#333333"))
            }

            sectionLayout.addView(titleView)

            if (items.isEmpty()) {
                val emptyText = TextView(this).apply {
                    text = "No saved items"
                    setPadding(0, 24, 0, 0)
                    textSize = 16f
                    setTextColor(Color.DKGRAY)
                }
                sectionLayout.addView(emptyText)
            } else {
                for (item in items) {
                    val itemView = TextView(this).apply {
                        text = "• $item"
                        setPadding(0, 16, 0, 16)
                        textSize = 17f
                        setTextColor(Color.DKGRAY)
                    }
                    sectionLayout.addView(itemView)
                }
            }

            card.addView(sectionLayout)
            return card
        }
        // Dummy data
        val savedMessages = listOf("Hi there!", "See you tomorrow.")
        val savedAIResponses = listOf("Here's the summary of your day.", "I recommend learning Kotlin.")
        val savedChallenges = listOf("30-Day Coding Challenge", "AI Art Contest")

        container.addView(createSavedSection("⭐ Saved messages from people", savedMessages))
        container.addView(createSavedSection("🤖 Saved responses from AI", savedAIResponses))
        container.addView(createSavedSection("🎯 Saved Challenges / Events", savedChallenges))

        scrollView.addView(container)
        parentLayout.addView(scrollView)

        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START // sau CENTER_HORIZONTAL dacă vrei centrare
            ).apply {
                setMargins(32, 32, 32, 0)
            }
            gravity = Gravity.CENTER_VERTICAL
        }

// ✅ Butonul "Back" cu dimensiuni și parametrii corecți
        val backButton = ImageButton(this).apply {
            setImageResource(R.drawable.image3)
            background = null
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams(200, 200) // Folosește LinearLayout.LayoutParams

            setOnClickListener {
                showHomePage(email)
            }
        }

// ✅ Titlul "⭐ Favorites"
        val titleText = TextView(this).apply {
            text = "⭐ Favorites"
            textSize = 30f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#333333"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(24, 0, 0, 0) // Spațiu între imagine și text
            }
        }

// ✅ Adăugare în header și adăugare header în layout
        headerLayout.addView(backButton)
        headerLayout.addView(titleText)
        parentLayout.addView(headerLayout)
        //parentLayout.addView(backButton)
        layout.addView(parentLayout)
    }


    private fun showRegisterUI() {
        layout.removeAllViews()
        emailEditText = EditText(this).apply {
            hint = "Email"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }

        passwordEditText = EditText(this).apply {
            hint = "Password"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val registerButton = Button(this).apply {
            text = "Register"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        val backButton = Button(this).apply {
            text = "Back"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        backButton.setOnClickListener {
            showInitialMenu()
        }
       val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER
            setPadding(16, 32, 16, 16)
        }
        inputLayout.addView(emailEditText)
        inputLayout.addView(passwordEditText)
        inputLayout.addView(registerButton)
        val frameLayout = FrameLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        frameLayout.addView(inputLayout)
        val backLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        }
        frameLayout.addView(backButton, backLayoutParams)
        layout.addView(frameLayout)
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val usersJson = sharedPreferences.getString(USERS_KEY, "{}")
                val users = JSONObject(usersJson)

                if (users.has(email)) {
                    Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show()
                } else {
                    users.put(email, password)
                    MainActivity.currentUserEmail = email
                    sharedPreferences.edit().putString(USERS_KEY, users.toString()).apply()
                    Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                    showHomePage(email)
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun transferKeys(oldEmail: String, newEmail: String) {
        val prefs = getSharedPreferences("messages", MODE_PRIVATE)
        val allMessages = prefs.all
        val editor = prefs.edit()

        for ((key, value) in allMessages) {
            Log.d("Mesaje","Mesaj: $key, $value")
            if (key.startsWith("chat_") && key.contains(oldEmail)) {
                val parts = key.removePrefix("chat_").split("_")
                if (parts.size != 2) {
                    val x= parts.size
                    Log.d("Mesaje","$x")
                    continue
                }

                var user1 = parts[0]
                var user2 = parts[1]
                user1 = if (user1 == oldEmail) newEmail else user1
                user2 = if (user2 == oldEmail) newEmail else user2
                Log.d("User","Useri: $user1, $user2")
                val newKey = "chat_${user1}_$user2"
                val oldMessages = value as? String ?: continue
                val updatedMessages = oldMessages.lines().joinToString("\n") { line ->
                    val msgParts = line.split("|")
                    if (msgParts.size == 3) {
                        Log.d("O ia pe aici","$msgParts[0], $msgParts[1]")
                        val sender = if (msgParts[0] == oldEmail) newEmail else msgParts[0]
                        "$sender|${msgParts[1]}|${msgParts[2]}"
                    } else {
                        line
                    }
                }
                editor.putString(newKey, updatedMessages)
                editor.remove(key)
            }
        }

        editor.apply()
    }
    fun showChallengesUI(email: String) {
        // Creăm un layout principal pentru ecran
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(16, 16, 16, 16)
        }

        // Creăm un ScrollView pentru a permite derularea listei de challenge-uri
        val scrollView = ScrollView(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // Creăm un LinearLayout care va conține lista de challenge-uri
        val challengeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(16, 16, 16, 16)
        }

        // Lista de challenge-uri
        val challenges = listOf(
            "Challenge 1: Complete your first task",
            "Challenge 2: Reach 100 points",
            "Challenge 3: Invite a friend",
            "Challenge 4: Achieve 5 achievements"
        )

        // Creăm un TextView pentru titlu
        val titleTextView = TextView(this).apply {
            text = "🏆 Your Challenges"
            textSize = 32f
            setTextColor(Color.BLACK)
            setPadding(300, 0, 20, 100)  // Padding pentru a adăuga spațiu între titlu și lista de challenge-uri
            gravity = Gravity.START
        }

        // Adăugăm fiecare challenge într-un CardView pentru a crea un chenar
        challenges.forEach { challenge ->
            val challengeCardView = CardView(this).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 10, 0, 10)
                }
                radius = 20f  // Colțuri rotunjite
                cardElevation = 8f  // Umbră subtilă
                setCardBackgroundColor(Color.parseColor("#F0F0F0"))  // Culoare de fundal deschisă
                val challengeTextView = TextView(this@MainActivity).apply {
                    text = challenge
                    textSize = 25f
                    setTextColor(Color.BLACK)
                    setPadding(20, 20, 20, 20)  // Padding consistent
                    gravity = Gravity.START
                }

                addView(challengeTextView)
            }

            challengeLayout.addView(challengeCardView)
        }

        // Adăugăm titlul și layout-ul cu challenge-uri la ScrollView
        scrollView.addView(challengeLayout)

        // Adăugăm scrollView la layout-ul principal
        mainLayout.addView(titleTextView)
        mainLayout.addView(scrollView)

        // Creăm un ImageButton pentru butonul rotund
        val roundButton = ImageButton(this).apply {
            setImageResource(R.drawable.image3)  // Imaginea dorită
            layoutParams = LayoutParams(200, 200).apply {
                setMargins(16, 16, 0, 0)  // Plasăm butonul în colțul din stânga sus
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = null  // Îndepărtează fundalul implicit
            setOnClickListener {
                showHomePage(email)
            }
        }

        // Creăm un container pentru butonul rotund și lista derulantă
        val containerLayout = FrameLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // Adăugăm butonul rotund și lista de challenge-uri la container
        containerLayout.addView(mainLayout)
        containerLayout.addView(roundButton)

        // Ștergem orice vizualizare existentă și adăugăm layout-ul final
        layout.removeAllViews()
        layout.addView(containerLayout)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            val uri = data.data ?: return
            currentUserEmail?.let {
                saveProfileImage(it, uri)
                showEditProfileUI(it)
            }
        }
    }

    private fun saveProfileImage(email: String, uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val file = File(filesDir, "profile_$email.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
    }

    private fun getProfileImageUri(email: String): Uri? {
        val file = File(filesDir, "profile_$email.jpg")
        return if (file.exists()) Uri.fromFile(file) else null
    }

    private fun renameProfileImage(oldEmail: String, newEmail: String) {
        val oldFile = File(filesDir, "profile_$oldEmail.jpg")
        val newFile = File(filesDir, "profile_$newEmail.jpg")
        if (oldFile.exists()) oldFile.renameTo(newFile)
    }

    private fun showEditProfileUI(email: String) {
        layout.removeAllViews()

        val usersJson = sharedPreferences.getString(USERS_KEY, "{}")
        val users = JSONObject(usersJson)

        val profilePrefs = getSharedPreferences("profile_data", MODE_PRIVATE)
        val currentName = profilePrefs.getString("name_$email", "")

        val title = TextView(this).apply {
            text = "Edit Profile"
            textSize = 22f
            setPadding(16, 16, 16, 16)
        }

        val profileImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(700, 700)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(16, 16, 16, 16)
            val uri = getProfileImageUri(email)
            if (uri != null) {
                setImageURI(uri)
            } else {
                setImageResource(R.drawable.poza_profil2) // imaginea default
            }
        }

        val uploadButton = Button(this).apply {
            text = "Choose Profile Picture"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1001)
                currentUserEmail = email // save context for onActivityResult
            }
        }

        val nameInput = EditText(this).apply {
            hint = "Display name"
            setText(MainActivity.nume ?: "Name: Unknown")
            setText(if (MainActivity.nume == "null") "Name: Unknown" else MainActivity.nume)
        }

        val emailInput = EditText(this).apply {
            hint = "New email (leave unchanged if not editing)"
            setText(MainActivity.currentUserEmail)
        }

        val passwordInput = EditText(this).apply {
            hint = "New password (leave blank if unchanged)"
            setText(MainActivity.parola)
        }
        val bioInput = EditText(this).apply {
            hint = "Bio (leave unchanged if not editing)"
            setText(if (MainActivity.bibliografie == "null") "Bio: Unknown" else MainActivity.bibliografie)
        }
        val preferencesInput = EditText(this).apply {
            hint = "Preferences (leave unchanged if not editing)"
            setText(if (MainActivity.preferinte == "null") "Preferences: Unknown" else MainActivity.preferinte)
        }
        val saveButton = Button(this).apply {
            text = "Save Changes"
        }

        val backButton = Button(this).apply {
            text = "Back"
        }

        saveButton.setOnClickListener {
            val newName = nameInput.text.toString().trim()
            val newEmail = emailInput.text.toString().trim()
            val newPassword = passwordInput.text.toString().trim()
            val new_bio = bioInput.text.toString().trim()
            val new_Preferences = preferencesInput.text.toString().trim()
            val userId = MainActivity.currentUserId
            val url = URL("http://10.0.2.2:8000/api/users/$userId")
            MainActivity.nume=newName
            MainActivity.currentUserEmail=newEmail
            MainActivity.parola=newPassword
            MainActivity.preferinte=new_Preferences
            MainActivity.bibliografie=new_bio
            val jsonBody = JSONObject().apply {
                put("email", newEmail)
                put("password_hash", newPassword)
                put("name", newName)
                put("role", null)
                put("bio", new_bio)
                put("preferences", new_Preferences)
            }

            Thread {
                try {
                    with(url.openConnection() as HttpURLConnection) {
                        requestMethod = "PUT"
                        val token = MainActivity.authToken
                        setRequestProperty("Content-Type", "application/json")
                        setRequestProperty("Authorization", "Bearer $token")
                        doOutput = true

                        outputStream.bufferedWriter().use { writer ->
                            writer.write(jsonBody.toString())
                            writer.flush()
                        }

                        val responseCode = responseCode
                        val responseMessage = inputStream.bufferedReader().readText()

                        Log.d("HTTP_PUT", "Response code: $responseCode")
                        Log.d("HTTP_PUT", "Response message: $responseMessage")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("HTTP_PUT", "Error: ${e.message}")
                }
            }.start()
            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Name and email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newEmail != email && users.has(newEmail)) {
                Toast.makeText(this, "This email is already in use", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passwordToStore = if (newPassword.isNotEmpty()) newPassword else users.getString(email)
            if (newEmail != email) {
                users.remove(email)
                renameProfileImage(email, newEmail)
            }

            users.put(newEmail, passwordToStore)
            sharedPreferences.edit().putString(USERS_KEY, users.toString()).apply()
            profilePrefs.edit().remove("name_$email").apply()
            profilePrefs.edit().putString("name_$newEmail", newName).apply()
            transferKeys(email, newEmail)

            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
            showHomePage(newEmail)
        }

        backButton.setOnClickListener {
            showHomePage(email)
        }

        layout.addView(title)
        layout.addView(profileImageView)
        layout.addView(uploadButton)
        layout.addView(nameInput)
        layout.addView(emailInput)
        layout.addView(passwordInput)
        layout.addView(bioInput)
        layout.addView(preferencesInput)
        layout.addView(saveButton)
        layout.addView(backButton)
    }

    private fun showHomePage(email: String) {
        layout.removeAllViews()
        val rootLayout = FrameLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // Top circular buttons (left and right)
        val topButtonsLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val leftCircularBitmap = getCircularBitmap(this, R.drawable.menu, 150)

// Creează butonul din stânga cu imagine rotundă
        val leftCircleButton = ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(150, 150, Gravity.START or Gravity.TOP).apply {
                marginStart = 32
                topMargin = 32
            }

            setImageBitmap(leftCircularBitmap)

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.TRANSPARENT)
            }

            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
            setPadding(0, 0, 0, 0)

            val rotationAnimator = ObjectAnimator.ofFloat(this, View.ROTATION, 0f, 360f).apply {
                duration = 1000
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.RESTART
            }

            /*setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Pornește animația la atingere
                        if (!rotationAnimator.isRunning) {
                            rotationAnimator.start()
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Oprește animația când utilizatorul ridică degetul
                        rotationAnimator.cancel()
                        this.rotation = 0f
                        false
                    }
                    else -> false
                }
            }*/
                setOnClickListener {
                    val dialog = Dialog(context)
                    dialog.window?.apply {
                        requestFeature(Window.FEATURE_NO_TITLE)
                        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        attributes.windowAnimations = android.R.style.Animation_Dialog
                        setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                    }

                    val layout = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(40, 40, 40, 40)
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 32f
                            setColor(Color.parseColor("#FFFFFF")) // Fundal alb
                            setStroke(2, Color.parseColor("#DDDDDD")) // Margine subtilă
                        }
                        elevation = 16f // Umbra
                    }

                    val menuItems = listOf(
                        "🍲  Recipe Recommender" to {
                            startRecipeChatAI()
                        },
                        "🌍 Route Planner" to {
                            RoutePlanner()
                        },
                        "📝  Add Notes" to {
                            showAddNotesUI(email) // Apelăm funcția pentru a adăuga note
                        },
                        "📖  View My Notes" to {
                            showViewNotesUI(email) // Apelăm funcția pentru a vizualiza notele
                        },
                        "💬  Discuss" to {
                            showUserListForChat(email) // Apelăm funcția pentru a discuta
                        },
                        "🕘  History" to {
                            showHistoryUI()
                        },
                        "⭐  Saved" to {
                            showSavedUI(email)
                        },
                        "🏆  Challenges" to {
                            showChallengesUI(email)
                        }
                    )
                    for ((text, action) in menuItems) {
                        val item = TextView(context).apply {
                            this.text = text
                            textSize = 25f
                            setTextColor(Color.BLACK)
                            setPadding(32, 32, 32, 32)
                            typeface = Typeface.DEFAULT_BOLD
                            setOnClickListener {
                                action()
                                dialog.dismiss()
                            }
                            background = ColorDrawable(Color.TRANSPARENT)
                        }
                        layout.addView(item)

                        // separator subțire între iteme
                        layout.addView(View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1
                            ).apply {
                                setMargins(32, 0, 32, 0)
                            }
                            setBackgroundColor(Color.parseColor("#EEEEEE"))
                        })
                    }

                    dialog.setContentView(layout)

                    // Poziționează sub top bar
                    dialog.window?.attributes = dialog.window?.attributes?.apply {
                        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                        y = 180
                    }
                    dialog.show()
                }
            }
        val circularBitmap = getCircularBitmap(this, R.drawable.poza)
        val rightCircleButton = ImageButton(this).apply {
            layoutParams = FrameLayout.LayoutParams(200, 200, Gravity.END or Gravity.TOP).apply {
                marginEnd = 32
                topMargin = 32
            }

            setImageBitmap(circularBitmap) // setează imaginea rotundă

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.TRANSPARENT)
            }

            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = true
            setPadding(0, 0, 0, 0)
            setOnClickListener {
                val dialog = Dialog(context)
                dialog.window?.apply {
                    requestFeature(Window.FEATURE_NO_TITLE)
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    attributes.windowAnimations = android.R.style.Animation_Dialog
                    setGravity(Gravity.TOP or Gravity.END)
                }

                val layout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(40, 40, 40, 40)
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 32f
                        setColor(Color.parseColor("#FFFFFF")) // Fundal alb
                        setStroke(2, Color.parseColor("#DDDDDD")) // Margine subtilă
                    }
                    elevation = 16f
                }

                val menuItems = listOf(
                    "👤  Edit Profile" to {
                        showEditProfileUI(email)  // Funcția ta existentă
                    },
                    "⚙️  Settings" to {
                        showSettingsUI()
                        //Toast.makeText(context, "Settings coming soon!", Toast.LENGTH_SHORT).show()
                        // Poți adăuga aici o funcție pentru setări, dacă ai una
                    },
                    "🚪  Logout" to {
                        currentUserEmail = null
                        showInitialMenu() // Revine la meniul inițial
                        dialog.dismiss()
                    }

                )


                for ((text, action) in menuItems) {
                    val item = TextView(context).apply {
                        this.text = text
                        textSize = 25f
                        setTextColor(Color.BLACK)
                        setPadding(32, 32, 32, 32)
                        typeface = Typeface.DEFAULT_BOLD
                        setOnClickListener {
                            action()
                            dialog.dismiss()
                        }
                        background = ColorDrawable(Color.TRANSPARENT)
                    }
                    layout.addView(item)

                    layout.addView(View(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                        ).apply {
                            setMargins(32, 0, 32, 0)
                        }
                        setBackgroundColor(Color.parseColor("#EEEEEE"))
                    })
                }

                dialog.setContentView(layout)

                dialog.window?.attributes = dialog.window?.attributes?.apply {
                    gravity = Gravity.TOP or Gravity.END
                    y = 180
                    x = 32
                }

                dialog.show()
            }
        }



        topButtonsLayout.addView(leftCircleButton)
        topButtonsLayout.addView(rightCircleButton)

        // Main center layout
        val centerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(32, 32, 32, 32)
        }

        val welcomeText = TextView(this).apply {
            text = "Welcome, $email!"
            textSize = 24f
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 40
            }
        }

        //val addNotesButton = Button(this).apply { text = "Add Notes for Myself" }
        //val viewNotesButton = Button(this).apply { text = "View My Notes" }
        //val discussButton = Button(this).apply { text = "Discuss" }
        val editProfileButton = Button(this).apply { text = "Edit Profile" }
        val toggleThemeButton = Button(this).apply { text = "Toggle Dark/Light Mode" }
        val logoutButton = Button(this).apply { text = "Logout" }

        // Button actions
        toggleThemeButton.setOnClickListener {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            AppCompatDelegate.setDefaultNightMode(
                if (currentMode == AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.MODE_NIGHT_NO
                else
                    AppCompatDelegate.MODE_NIGHT_YES
            )
            recreate()
        }

        editProfileButton.setOnClickListener { showEditProfileUI(email) }
        //discussButton.setOnClickListener { showUserListForChat(email) }
        logoutButton.setOnClickListener { showInitialMenu() }
        //addNotesButton.setOnClickListener { showAddNotesUI(email) }
        //viewNotesButton.setOnClickListener { showViewNotesUI(email) }

        // Add buttons to center layout
        centerLayout.addView(welcomeText)
        //centerLayout.addView(addNotesButton)
        //centerLayout.addView(viewNotesButton)
        //centerLayout.addView(discussButton)
        centerLayout.addView(editProfileButton)
        centerLayout.addView(toggleThemeButton)
        centerLayout.addView(logoutButton)

        // Add everything to root layout
        rootLayout.addView(centerLayout)
        rootLayout.addView(topButtonsLayout)

        layout.addView(rootLayout)
    }
    private fun showUserListForChat(email: String) {
        layout.removeAllViews()
        val usersJson = sharedPreferences.getString(USERS_KEY, "{}")
        val users = JSONObject(usersJson)
        val originalUserList = mutableListOf<String>()
        val lista_useri_necititi = getUsersWithUnreadMessages(email)
        Log.d("Lista useri necititi", "User List: $lista_useri_necititi")
        Thread {
            try {
                // URL-ul API-ului pentru a obține utilizatorii
                val url = URL("http://10.0.2.2:8000/api/users")
                val conn = url.openConnection() as HttpURLConnection
                val token = MainActivity.authToken
                conn.requestMethod = "GET"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $token") // Adaugă token dacă e necesar
                // Conectează-te la server și primește răspunsul
                conn.connect()

                // Dacă cererea a fost reușită (status code 200)
                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    // Citește răspunsul JSON
                    val response = conn.inputStream.bufferedReader().use { it.readText() }

                    // Actualizează UI-ul pe thread-ul principal
                    runOnUiThread {
                        try {
                            // Parsează răspunsul JSON
                            val jsonArray = JSONArray(response)
                            val userList = mutableListOf<String>()
                            val originalUserList = mutableListOf<String>()
                            val listaUseriNecititi = getUsersWithUnreadMessages(email)

                            Log.d("Lista useri necititi", "User List: $response")

                            // Parcurge fiecare utilizator din răspunsul JSON
                            for (i in 0 until jsonArray.length()) {
                                val user = jsonArray.getJSONObject(i)
                                val userName = user.getString("email")

                                // Adaugă utilizatorul în lista originală
                                originalUserList.add(userName)

                                // Verifică dacă utilizatorul are mesaje necitite
                                if (listaUseriNecititi.contains(userName)) {
                                    userList.add("$userName (nou)")
                                } else {
                                    userList.add(userName)
                                }
                            }

                            Log.d("showUserListForChat", "User List: $userList")

                            // Adaugă utilizatorii în UI (de exemplu, într-un ListView)
                            if (userList.isEmpty()) {
                                Toast.makeText(this, "No users available for chat", Toast.LENGTH_SHORT).show()
                            } else {
                                val userListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userList)
                                val userListView = ListView(this).apply {
                                    adapter = userListAdapter
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        0,
                                        1f
                                    )
                                }
                                userListView.setOnItemClickListener { _, _, position, _ ->
                                    val selectedUser = userList[position]
                                    showChatUI(email, selectedUser)  // Începe chat-ul cu utilizatorul selectat
                                }
                                layout.addView(userListView)

                                // Creează un buton "Back"
                                val bottomLayout = LinearLayout(this).apply {
                                    orientation = LinearLayout.VERTICAL
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                }

                                val backButton = Button(this).apply {
                                    text = "Back"
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                }

                                backButton.setOnClickListener {
                                    showHomePage(email)
                                }

                                bottomLayout.addView(backButton)
                                layout.addView(bottomLayout)
                            }

                        } catch (e: Exception) {
                            Log.e("Error", "Error parsing users response", e)
                            Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Error: ${conn.responseCode}", Toast.LENGTH_SHORT).show()
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                Log.e("Error", "Error fetching users", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()

        /*val userList = mutableListOf<String>()
        for (key in users.keys()) {
            if (key != email) {
                originalUserList.add(key)
                if (lista_useri_necititi.contains(key)) {
                    userList.add("$key (nou)")
                } else {
                    userList.add(key)
                }
            }
        }
        Log.d("showUserListForChat", "User List: $userList")
        if (userList.isEmpty()) {
            Toast.makeText(this, "No users available for chat", Toast.LENGTH_SHORT).show()
        } else {
            val userListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userList)
            val userListView = ListView(this).apply {
                adapter = userListAdapter
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
            userListView.setOnItemClickListener { _, _, position, _ ->
                val selectedUser = userList[position]
                showChatUI(email, selectedUser)  // Începe chat-ul cu utilizatorul selectat
            }
            layout.addView(userListView)
            val bottomLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            val backButton = Button(this).apply {
                text = "Back"
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            /*backButton.setOnClickListener {
                showHomePage(email)
            }*/
            bottomLayout.addView(backButton)
            layout.addView(bottomLayout)
        }*/
    }
    private fun showChatUI(email: String, selectedUser: String) {
        layout.removeAllViews()
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        }
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        val chatLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        val chatMessages = getChatMessages(email, selectedUser).split("\n")
        for (message in chatMessages) {
            val parts = message.split("|")
            if (parts.size < 3) continue

            val senderEmail = parts[0]
            val timestamp = parts[1]
            val content = parts[2]

            val isCurrentUser = senderEmail == email
            val messageLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 8)
                }
                gravity = if (isCurrentUser) Gravity.END else Gravity.START
            }
            val bubbleContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Mesajul propriu-zis
            val messageTextView = TextView(this).apply {
                text = content
                textSize = 16f
                setPadding(24, 16, 24, 16)
                background = GradientDrawable().apply {
                    cornerRadius = 40f
                    setColor(if (isCurrentUser) Color.parseColor("#DCF8C6") else Color.WHITE)
                }
                setTextColor(Color.BLACK)
            }

            // Textul de sub mesaj: ora + sender (daca e altcineva)
            val timestampTextView = TextView(this).apply {
                text = if (!isCurrentUser) "$senderEmail · $timestamp" else timestamp
                textSize = 12f
                setTextColor(Color.GRAY)
                setPadding(16, 4, 16, 8)
                gravity = Gravity.END
            }
            bubbleContainer.addView(messageTextView)
            bubbleContainer.addView(timestampTextView)
            messageLayout.addView(bubbleContainer)
            chatLayout.addView(messageLayout)
        }

        scrollView.addView(chatLayout)
        val messageInputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
        }
        val messageEditText = EditText(this).apply {
            hint = "Enter your message"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) // Lățimea este flexibilă, iar înălțimea se ajustează automat
            setPadding(16, 16, 16, 16)
        }

        val sendButton = Button(this).apply {
            //text = "Send"
            layoutParams = LinearLayout.LayoutParams(200, 100) // Dimensiune fixă pentru buton (lățime și înălțime)
            setBackgroundResource(R.drawable.sendbutton) // Setează imaginea butonului
            setPadding(16, 16, 16, 16) // Setează padding pentru buton
        }
        messageInputLayout.addView(messageEditText)
        messageInputLayout.addView(sendButton)
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if (message.isNotEmpty()) {
                saveChatMessage(email, selectedUser, message)
                messageEditText.text.clear()
                showChatUI(email, selectedUser)
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
        val backButton = Button(this).apply {
            text = "Back"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        backButton.setOnClickListener {
            showUserListForChat(email)
        }
        mainLayout.addView(scrollView)
        mainLayout.addView(messageInputLayout)
        mainLayout.addView(backButton)
        layout.addView(mainLayout)
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun getAllMessages(): Map<String, String> {
        val prefs = sharedPreferences
        val allMessages = prefs.all
        val chatMessages = mutableMapOf<String, String>()
        for ((key, value) in allMessages) {
                val messages = value as? String ?: continue
                chatMessages[key] = messages
        }
        return chatMessages
    }
    private fun generateChatKey(user1: String, user2: String): String {
        val (emailA, emailB) = if (user1 < user2) Pair(user1, user2) else Pair(user2, user1)
        return "chat_${emailA}_${emailB}"
    }
    private fun saveChatMessage(sender: String, receiver: String, message: String) {
        val chatKey = generateChatKey(sender, receiver)
        val currentChat = sharedPreferences.getString(chatKey, "")

        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val fullMessage = "$sender|$timestamp|$message"

        val updatedChat = if (currentChat.isNullOrEmpty()) fullMessage else "$currentChat\n$fullMessage"

        sharedPreferences.edit().putString(chatKey, updatedChat).apply()
    }

    private fun getChatMessages(user1: String, user2: String): String {
        val chatKey = generateChatKey(user1, user2)
        return sharedPreferences.getString(chatKey, "") ?: "No messages yet."
    }
    fun getUserEmail(): String {
        val prefs = getSharedPreferences("chat_prefs", MODE_PRIVATE)
        return prefs.getString("user_email", "default_email@example.com") ?: "default_email@example.com"
    }
    override fun onDestroy() {
        super.onDestroy()
        val userId = getUserEmail()
        saveLastLoginTime(userId)
    }
    fun saveLastLoginTime(userId: String) {
        val currentTimestamp = System.currentTimeMillis().toString()
        val prefs = getSharedPreferences("chat_prefs", MODE_PRIVATE)
        prefs.edit().putString("last_login_time_$userId", currentTimestamp).apply()
    }
    fun getUsersWithUnreadMessages(userId: String): List<String> {
        val prefs = getSharedPreferences("chat_prefs", MODE_PRIVATE)
        val usersWithUnreadMessages = mutableListOf<String>()

        val usersJson = prefs.getString("users", "{}")
        val users = JSONObject(usersJson)

        val lastLoginTime = prefs.getString("last_login_time_$userId", "0")?.toLong() ?: 0

        for (key in users.keys()) {
            if (key != userId) {
                val chatKey = "chat_${key}_$userId"
                val chatMessages = prefs.getString(chatKey, "") ?: ""

                val messages = chatMessages.split("\n")
                for (message in messages) {
                    val parts = message.split("|")
                    if (parts.size >= 3) {
                        val timestamp = parts[1].toLong()
                        if (timestamp > lastLoginTime) {
                            usersWithUnreadMessages.add(key)
                            break
                        }
                    }
                }
            }
        }

        return usersWithUnreadMessages
    }



    private fun showViewNotesUI(email: String) {
        layout.removeAllViews()

        val notesTextView = TextView(this).apply {
            text = "Your Notes:\n" + getSavedNotesForUser(email)
            textSize = 18f
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val backButton = Button(this).apply {
            text = "Back"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        backButton.setOnClickListener {
            showHomePage(email)
        }

        layout.addView(notesTextView)
        layout.addView(backButton)
    }
    private fun getSavedNotesForUser(email: String): String {
        val savedNotes = sharedPreferences.getString(email, "")
        return savedNotes ?: "No notes available."
    }
    private fun showAddNotesUI(email: String) {
        layout.removeAllViews()

        val noteEditText = EditText(this).apply {
            hint = "Enter your note here"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val saveButton = Button(this).apply {
            text = "Save Note"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val backButton = Button(this).apply {
            text = "Back"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        backButton.setOnClickListener {
            showHomePage(email)
        }

        layout.addView(noteEditText)
        layout.addView(saveButton)
        layout.addView(backButton)

        saveButton.setOnClickListener {
            val note = noteEditText.text.toString()

            if (note.isNotEmpty()) {
                val savedNotes = sharedPreferences.getString(email, "")
                val updatedNotes = if (savedNotes.isNullOrEmpty()) {
                    note
                } else {
                    "$savedNotes\n$note"
                }
                sharedPreferences.edit().putString(email, updatedNotes).apply()

                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
                showHomePage(email)
            } else {
                Toast.makeText(this, "Please write something!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showSettingsUI() {
        // Golește complet layout-ul principal
        layout.removeAllViews()

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5"))

            val settingsLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                setPadding(48, 64, 48, 48)
                setBackgroundColor(Color.parseColor("#F5F5F5"))

                // Titlu
                val settingsTitle = TextView(context).apply {
                    text = "⚙️ Settings"
                    textSize = 30f
                    setTextColor(Color.DKGRAY)
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setPadding(0, 0, 0, 48)
                }
                addView(settingsTitle)

                // Listă de setări
                val settingItems = listOf(
                    "🔔  Notifications" to { Toast.makeText(this@MainActivity, "Notificări", Toast.LENGTH_SHORT).show() },
                    "🌗  Dark/Light Mode" to {
                        val currentMode = AppCompatDelegate.getDefaultNightMode()
                        AppCompatDelegate.setDefaultNightMode(
                            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES)
                                AppCompatDelegate.MODE_NIGHT_NO
                            else
                                AppCompatDelegate.MODE_NIGHT_YES
                        )
                        recreate() // reîncarcă activitatea pentru a aplica tema
                    },
                    "🔒  Change Password" to { Toast.makeText(this@MainActivity, "Schimbă parola", Toast.LENGTH_SHORT).show() },
                    "📄  Privacy Policy" to { Toast.makeText(this@MainActivity, "Privacy Policy", Toast.LENGTH_SHORT).show() },
                    "⬅️  Back" to {
                        showHomePage(currentUserEmail.toString())
                    }
                )

                for ((text, action) in settingItems) {
                    val item = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        background = GradientDrawable().apply {
                            cornerRadius = 32f
                            setColor(Color.WHITE)
                            setStroke(2, Color.LTGRAY)
                        }
                        setPadding(40, 40, 40, 40)
                        setOnClickListener { action() }

                        val textView = TextView(context).apply {
                            this.text = text
                            textSize = 25f
                            setTextColor(Color.BLACK)
                            typeface = Typeface.SANS_SERIF
                        }

                        addView(textView)
                    }

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 24
                    }

                    addView(item, params)
                }
            }

            addView(settingsLayout)
        }
        // Adaugă totul în layout-ul principal deja existent
        layout.addView(scrollView)
    }
    fun createAllergenLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(40, 20, 40, 20)
            setTextColor(Color.WHITE)
            textSize = 20f
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 50f
                setColor(Color.parseColor("#FF7043")) // o nuanță de portocaliu
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(16, 16, 16, 16)
            layoutParams = params
        }
    }
    private fun startRecipeChatAI() {
        //val selectedAllergens = mutableListOf<String>()
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 80, 60, 60)
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val allergensContainer = FlowLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 20, 0, 20)
        }



        // Zona pentru întrebare
        val messageInput = EditText(this).apply {
            hint = "Ask recipe recommender for recipes..."
            textSize = 22f
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            )
            setSingleLine(false) // Permite linii multiple
            maxLines = 5         // Opțional, limitează înălțimea
            isVerticalScrollBarEnabled = true
            movementMethod = ScrollingMovementMethod()
            setScroller(Scroller(context))
        }


        val responseView = TextView(this).apply {
            text = "Your answer will be here"
            textSize = 22f
            setTextColor(Color.DKGRAY)
            setPadding(20, 30, 20, 30)
            isVerticalScrollBarEnabled = true
            movementMethod = ScrollingMovementMethod()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            )
        }

        //val selectedAllergens = mutableListOf<String>()

        // Funcție pentru creare etichetă
        fun createAllergenLabel(text: String): TextView {
            // Mapare între alergeni și culori
            val allergenColors = mapOf(
                "Gluten" to Color.parseColor("#FFEB3B"), // Galben Ocru
                "Eggs" to Color.parseColor("#2196F3"), // Albastru
                "Nuts" to Color.parseColor("#4CAF50"), // Verde
                "Soy" to Color.parseColor("#FF5722"), // Roșu
                "Fish" to Color.parseColor("#009688"), // Verde albastru
                "Shellfish" to Color.parseColor("#9C27B0"), // Mov
                "Sesame" to Color.parseColor("#FF9800")
            )

            // Obține culoarea alergenului din mapare (sau culoare implicită dacă nu există o corespondență)
            val color = allergenColors[text] ?: Color.GRAY

            return TextView(this).apply {
                this.text = text
                setPadding(40, 20, 40, 20)
                setTextColor(Color.WHITE)
                textSize = 20f
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 50f
                    setColor(color)  // Folosește culoarea specifică alergenului
                }
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(16, 16, 16, 16) // Margini între etichete
                layoutParams = params
            }
        }

// Butonul pentru alergeni
        val allergenButton = Button(this).apply {
            text = "🥜 Alergens"
            textSize = 20f
            setOnClickListener {
                val allergens = arrayOf("Gluten", "Eggs", "Nuts", "Soy", "Fish", "Shellfish", "Sesame")
                val checkedItems = BooleanArray(allergens.size) { i -> selectedAllergens.contains(allergens[i]) }

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose allergens:")
                    .setMultiChoiceItems(allergens, checkedItems) { _, which, isChecked ->
                        val allergen = allergens[which]
                        if (isChecked) {
                            if (!selectedAllergens.contains(allergen)) selectedAllergens.add(allergen)
                        } else {
                            selectedAllergens.remove(allergen)
                        }
                    }
                    .setPositiveButton("OK") { _, _ ->
                        allergensContainer.removeAllViews()
                        if (selectedAllergens.isEmpty()) {
                            allergensContainer.addView(TextView(this@MainActivity).apply {
                                text = "No selected allergens"
                                setTextColor(Color.GRAY)
                                textSize = 20f
                            })
                        } else {
                            selectedAllergens.forEach {
                                allergensContainer.addView(createAllergenLabel(it))
                            }
                        }

                        Toast.makeText(
                            this@MainActivity,
                            if (selectedAllergens.isEmpty()) "No selected allergens"
                            else "Selected: ${selectedAllergens.joinToString()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
        if (selectedAllergens.isEmpty()) {
            allergensContainer.addView(TextView(this).apply {
                text = "No selected allergens"
                setTextColor(Color.GRAY)
                textSize = 20f
            })
        } else {
            selectedAllergens.forEach {
                allergensContainer.addView(createAllergenLabel(it))
            }
        }



        //val selectedMealTypes = mutableListOf<String>()

// Containerul pentru tipurile de masă
        val mealTypeContainer = FlowLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 20, 0, 20)
        }

// Butonul pentru tipuri de masă
        val mealTypeColors = mapOf(
            "Main Course" to Color.parseColor("#FF5722"),        // Portocaliu vibrant
            "Breakfast" to Color.parseColor("#FFEB3B"),          // Galben deschis
            "Snacks" to Color.parseColor("#FFC107"),             // Galben mustar
            "Sides & Salads" to Color.parseColor("#8BC34A"),     // Verde lime
            "Desserts & Baked Goods" to Color.parseColor("#E91E63"), // Roz intens
            "Drinks" to Color.parseColor("#00BCD4"),             // Albastru deschis
            "Other" to Color.parseColor("#9E9E9E")               // Gri neutru
        )
// Butonul pentru tipuri de masă
        val mealTypeButton = Button(this).apply {
            text = "🍽️ What do you eat"
            textSize = 20f
            setOnClickListener {
                val mealTypes = arrayOf("Main Course",
                    "Breakfast", "Snacks", "Sides & Salads",
                    "Desserts & Baked Goods", "Drinks", "Other" )
                val checkedItems = BooleanArray(mealTypes.size) { i -> selectedMealTypes.contains(mealTypes[i]) }

                // Crearea unui dialog pentru alegerea tipurilor de masă
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose meal types:")
                    .setSingleChoiceItems(mealTypes, -1) { dialog, which ->
                        selectedMealTypes.clear()
                        selectedMealTypes.add(mealTypes[which])

                        // Închide dialogul imediat după selecție
                        dialog.dismiss()

                        // Reîmprospătarea containerului pentru tipuri de masă
                        mealTypeContainer.removeAllViews()
                        val color = mealTypeColors[mealTypes[which]] ?: Color.GRAY

                        val mealTypeLabel = TextView(this@MainActivity).apply {
                            text = mealTypes[which]
                            setPadding(40, 20, 40, 20)
                            setTextColor(Color.WHITE)
                            textSize = 20f
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.RECTANGLE
                                cornerRadius = 50f
                                setColor(color)
                            }
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(16, 16, 16, 16)
                            }
                        }

                        mealTypeContainer.addView(mealTypeLabel)

                        Toast.makeText(
                            this@MainActivity,
                            "Selected: ${mealTypes[which]}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

// Verificare inițială pentru tipurile de masă
        if (selectedMealTypes.isEmpty()) {
            mealTypeContainer.addView(TextView(this).apply {
                text = "No selected meal types"
                setTextColor(Color.GRAY)
                textSize = 20f
            })
        } else {
            selectedMealTypes.forEach {
                // Obține culoarea corespunzătoare tipului de masă
                val color = mealTypeColors[it] ?: Color.GRAY // Folosește o culoare implicită dacă nu există corespondență

                val mealTypeLabel = TextView(this).apply {
                    this.text = it
                    setPadding(40, 20, 40, 20)
                    setTextColor(Color.WHITE)
                    textSize = 20f
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 50f
                        setColor(color)
                    }
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(16, 16, 16, 16)
                    layoutParams = params
                }

                // Adăugăm eticheta la container
                mealTypeContainer.addView(mealTypeLabel)
            }
        }
        val dietTypeContainer = FlowLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 20, 0, 20)
        }
        val dietTypeColors = mapOf(
            "Vegan" to Color.parseColor("#4CAF50"),       // Verde
            "Vegetarian" to Color.parseColor("#FFC107"),  // Galben
            "Pescatarian" to Color.parseColor("#00BCD4"), // Albastru deschis
            "Omnivore" to Color.parseColor("#FF5722")     // Roșu
        )

// Butonul pentru alegerea dietei
        val dietTypeButton = Button(this).apply {
            text = "🥗 Your diet"
            textSize = 20f
            setOnClickListener {
                val dietTypes = arrayOf("Vegan", "Vegetarian", "Pescatarian", "Omnivore")
                val selectedIndex = selectedDietType?.let { dietTypes.indexOf(it) } ?: -1

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose your diet type:")
                    .setSingleChoiceItems(dietTypes, selectedIndex) { _, which ->
                        selectedDietType = dietTypes[which]
                    }
                    .setPositiveButton("OK") { _, _ ->
                        dietTypeContainer.removeAllViews()
                        if (selectedDietType == null) {
                            dietTypeContainer.addView(TextView(this@MainActivity).apply {
                                text = "No selected diet types"
                                setTextColor(Color.GRAY)
                                textSize = 20f
                            })
                        } else {
                            val color = dietTypeColors[selectedDietType] ?: Color.GRAY
                            val label = TextView(this@MainActivity).apply {
                                text = selectedDietType
                                setPadding(40, 20, 40, 20)
                                setTextColor(Color.WHITE)
                                textSize = 20f
                                background = GradientDrawable().apply {
                                    shape = GradientDrawable.RECTANGLE
                                    cornerRadius = 50f
                                    setColor(color)
                                }
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(16, 16, 16, 16)
                                }
                            }
                            dietTypeContainer.addView(label)
                        }

                        Toast.makeText(
                            this@MainActivity,
                            selectedDietType ?: "No selected diet types",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

// Inițializare UI pentru tipul de dietă
        if (selectedDietType == null) {
            dietTypeContainer.addView(TextView(this).apply {
                text = "No selected diet types"
                setTextColor(Color.GRAY)
                textSize = 20f
            })
        } else {
            val color = dietTypeColors[selectedDietType] ?: Color.GRAY
            val label = TextView(this).apply {
                text = selectedDietType
                setPadding(40, 20, 40, 20)
                setTextColor(Color.WHITE)
                textSize = 20f
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 50f
                    setColor(color)
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 16)
                }
            }
            dietTypeContainer.addView(label)
        }

        val sendButton = Button(this).apply {
            text = "📤 Send"
            textSize = 18f
            setOnClickListener {
                val userMessage = messageInput.text.toString()
                if (userMessage.isBlank()) {
                    Toast.makeText(this@MainActivity, "Write a question.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val json = """
        {
            "user_text": "${userMessage.replace("\"", "\\\"")}",
            "allergens": ["${selectedAllergens.joinToString("\", \"") { it.replace("\"", "\\\"") }}"],
            "diet": "${selectedDietType}",
            "dish_category": "${selectedMealTypes}"
        }
        """.trimIndent()
                Log.d("BACK_BUTTON", "JSON: $json")

                Thread  {
                    try {
                        val url = URL("http://10.0.2.2:8000/api/recipes/recommend")  // Corect: port 8000, cale /api/recipes/recommend
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        val token = MainActivity.authToken
                        conn.setRequestProperty("Content-Type", "application/json")
                        conn.setRequestProperty("Authorization", "Bearer $token") // Adaugă token dacă e necesar
                        conn.doOutput = true

                        // Creează JSON-ul de request (modifică în funcție de schema ta)
                        val json = JSONObject().apply {
                            put("ingredients", "chicken, rice, garlic")
                            put("preferences", "low-carb")
                        }.toString()

                        // Trimite JSON-ul
                        conn.outputStream.use { os ->
                            val input = json.toByteArray(Charsets.UTF_8)
                            os.write(input, 0, input.size)
                        }

                        // Primește răspunsul
                        val response = conn.inputStream.bufferedReader().use { it.readText() }

                        runOnUiThread {
                            try {
                                val jsonObject = JSONObject(response)

                                val formatted = buildString {
                                    append("🍽️ Recipe: ${jsonObject.getString("name")}\n\n")

                                    val rawIngredients = jsonObject.getString("ingredients")
                                    val cleaned = rawIngredients
                                        .replace("[", "")
                                        .replace("]", "")
                                        .replace("\"", "")
                                        .replace("'", "")
                                        .split(",")
                                        .map { it.trim() }

                                    append("📋 Ingredients:\n")
                                    for (ingredient in cleaned) {
                                        append("  • $ingredient\n")
                                    }

                                    append("\n📝 Directions:\n${jsonObject.getString("directions")}\n")

                                    append("\n🕒 Total Time: ${jsonObject.getInt("total_time")} minutes\n")
                                    append("⚠️ Allergens: ")
                                    val allergens = jsonObject.getJSONArray("allergens")
                                    if (allergens.length() == 0) {
                                        append("None\n")
                                    } else {
                                        for (i in 0 until allergens.length()) {
                                            append("${allergens.getString(i)}${if (i < allergens.length() - 1) ", " else "\n"}")
                                        }
                                    }

                                    append("🔥 Calories: ${jsonObject.getDouble("calories")}\n")
                                    append("\n🌐 URL: ${jsonObject.getString("site")}")
                                }

                                responseView.text = formatted

                            } catch (e: Exception) {
                                responseView.text = "❌ I couldn't find a good recipe for you. I am sorry."
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            responseView.text = "Request failed: ${e.message}"
                        }
                    }
                }.start()
            }
        }

        val spacer = Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        val prepTimeTitle = TextView(this).apply {
            text = "⏱️ Preparation time:"
            textSize = 25f
            setTextColor(Color.BLACK)
            setPadding(0, 30, 0, 10)
        }
        val prepTimeMin = EditText(this).apply {
            hint = "Min (10)"
            inputType = InputType.TYPE_CLASS_NUMBER // Permite doar cifre
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = 16
            }
            setText(minimum_value_time ?: "")
        }

        val prepTimeMax = EditText(this).apply {
            hint = "Max (500)"
            inputType = InputType.TYPE_CLASS_NUMBER // Permite doar cifre
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            setText(maximum_value_time ?: "")
        }

// Adăugăm TextWatcher pentru validarea valorilor
        prepTimeMin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()
                if (!input.isNullOrEmpty()) {
                    val num = input.toIntOrNull()
                    if (num != null) {
                        // Actualizăm minimum_value_time doar dacă inputul este valid
                        minimum_value_time = input

                        // Verificăm dacă min este mai mare decât max, caz în care le interschimbăm
                        val max = maximum_value_time?.toIntOrNull()
                        if (max != null && num > max) {
                            // Interschimbăm valorile
                            Toast.makeText(this@MainActivity, "Min value was greater than Max value. Values have been swapped.", Toast.LENGTH_SHORT).show()
                            minimum_value_time = max.toString()
                            maximum_value_time = input
                            prepTimeMin.setText(minimum_value_time)
                            prepTimeMax.setText(maximum_value_time)
                        }
                    } else {
                        // Dacă nu este număr, resetăm valoarea și arătăm un mesaj
                        prepTimeMin.setText(minimum_value_time)
                        Toast.makeText(this@MainActivity, "Please enter a valid number.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        prepTimeMax.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString()
                if (!input.isNullOrEmpty()) {
                    val num = input.toIntOrNull()
                    if (num != null) {
                        // Actualizăm maximum_value_time doar dacă inputul este valid
                        maximum_value_time = input

                        // Verificăm dacă max este mai mic decât min, caz în care le interschimbăm
                        val min = minimum_value_time?.toIntOrNull()
                        if (min != null && num < min) {
                            // Interschimbăm valorile
                            Toast.makeText(this@MainActivity, "Max value was smaller than Min value. Values have been swapped.", Toast.LENGTH_SHORT).show()
                            maximum_value_time = min.toString()
                            minimum_value_time = input
                            prepTimeMin.setText(minimum_value_time)
                            prepTimeMax.setText(maximum_value_time)
                        }
                    } else {
                        // Dacă nu este număr, resetăm valoarea și arătăm un mesaj
                        prepTimeMax.setText(maximum_value_time)
                        Toast.makeText(this@MainActivity, "Please enter a valid number.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        prepTimeMin.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)  // 18f reprezintă dimensiunea textului în unități SP (scale-independent pixels)
        prepTimeMax.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)  // Poți ajusta valoarea după preferințe

// Adăugăm câmpurile într-un layout orizontal
        val prepTimeLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(prepTimeMin)
            addView(prepTimeMax)
        }
        val backButton = Button(this).apply {
            text = "⬅️ Back"
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        backButton.setOnClickListener {
            dialog.dismiss()
            showHomePage(currentUserEmail.toString())
        }

        layout.apply {
            addView(messageInput)
            addView(responseView)
            addView(allergenButton)
            addView(allergensContainer) // <- aici se adaugă etichetele
            addView(mealTypeButton)
            addView(mealTypeContainer)
            addView(dietTypeButton)
            addView(dietTypeContainer)
            addView(prepTimeTitle)
            addView(prepTimeLayout)
            addView(backButton)
            addView(spacer)
            addView(sendButton)
        }

        scrollView.addView(layout)
        dialog.setContentView(scrollView)
        dialog.show()
    }
}
class FlowLayout(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    init {
        orientation = HORIZONTAL
        setWillNotDraw(false) // Permite redarea manuală a elementelor
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        var lineHeight = 0
        var x = paddingLeft
        var y = paddingTop

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight

                if (x + childWidth + paddingRight > width) {
                    // Dacă nu mai încap pe linia curentă, mutăm pe următoarea linie
                    x = paddingLeft
                    y += lineHeight
                    lineHeight = 0
                }

                child.layout(x, y, x + childWidth, y + childHeight)
                x += childWidth
                lineHeight = maxOf(lineHeight, childHeight)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var lineWidth = 0
        var lineHeight = 0
        var width = 0
        var height = paddingTop

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec)

                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight

                if (lineWidth + childWidth + paddingRight > measuredWidth) {
                    // Dacă nu mai încap pe linia curentă, mutăm pe următoarea linie
                    width = maxOf(width, lineWidth)
                    lineWidth = childWidth
                    height += lineHeight
                    lineHeight = childHeight
                } else {
                    lineWidth += childWidth
                    lineHeight = maxOf(lineHeight, childHeight)
                }
            }
        }
        width = maxOf(width, lineWidth)
        height += lineHeight + paddingBottom

        setMeasuredDimension(width, height)
    }
}

