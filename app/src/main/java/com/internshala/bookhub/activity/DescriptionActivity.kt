package com.internshala.bookhub.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.bookhub.R
import com.internshala.bookhub.database.BookDatabase
import com.internshala.bookhub.database.BookEntity
import com.internshala.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {
    lateinit var imgBook: ImageView
    lateinit var txtBookName: TextView
    lateinit var txtAuthor: TextView
    lateinit var txtCost: TextView
    lateinit var txtRating: TextView
    lateinit var txtAbout: TextView
    lateinit var btnAddToFav: Button
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var toolbar: Toolbar

    var bookId: String? = "100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName)
        txtAuthor = findViewById(R.id.txtAuthor)
        txtCost = findViewById(R.id.txtCost)
        txtRating = findViewById(R.id.txtRating)
        txtAbout = findViewById(R.id.txtAbout)
        btnAddToFav = findViewById(R.id.btnAddToFav)
        imgBook = findViewById(R.id.imgBook)
        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.VISIBLE
        progressLayout.visibility = View.VISIBLE

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book details"

        if (intent != null){
            bookId = intent.getStringExtra("book_id")
        } else {
            finish()
            Toast.makeText(this@DescriptionActivity, "Some unexpected error occurred", Toast.LENGTH_SHORT).show()
        }
        if (bookId == "100"){
            finish()
            Toast.makeText(this@DescriptionActivity, "Some unexpected error occurred", Toast.LENGTH_SHORT).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id", bookId)

        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)!!){
            val jsonRequest = object: JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try {
                    val success = it.getBoolean("success")
                    if (success){
                        val bookJsonObject = it.getJSONObject("book_data")
                        progressLayout.visibility = View.GONE

                        Picasso.get().load(bookJsonObject.getString("image")).error(R.drawable.default_book_cover).into(imgBook)
                        txtBookName.text = bookJsonObject.getString("name")
                        txtAuthor.text = bookJsonObject.getString("author")
                        txtCost.text = bookJsonObject.getString("price")
                        txtRating.text = bookJsonObject.getString("rating")
                        txtAbout.text = bookJsonObject.getString("description")

                        val bookImageUrl = bookJsonObject.getString("image")
                        val bookEntity = BookEntity(
                            bookId?. toInt() as Int,
                            txtBookName.text.toString(),
                            txtAuthor.text.toString(),
                            txtCost.text.toString(),
                            txtRating.text.toString(),
                            txtAbout.text.toString(),
                            bookImageUrl
                        )
                        val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                        val isFav = checkFav.get()

                        if (isFav){
                            btnAddToFav.text = "Remove from Favourites"
                            val favColor = ContextCompat.getColor(applicationContext, R.color.darkBlue)
                            btnAddToFav.setBackgroundColor(favColor)
                        }

                        btnAddToFav.setOnClickListener{
                            if (!DBAsyncTask(applicationContext, bookEntity, 1).execute().get()){
                                val async = DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                val result = async.get()
                                if (result){
                                    Toast.makeText(this@DescriptionActivity, "Book added to Favourites", Toast.LENGTH_SHORT).show()
                                    btnAddToFav.text = "Remove from Favourites"
                                    val favColor = ContextCompat.getColor(applicationContext, R.color.darkBlue)
                                    btnAddToFav.setBackgroundColor(favColor)
                                } else {
                                    Toast.makeText(this@DescriptionActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val remove = DBAsyncTask(applicationContext, bookEntity, 3).execute().get()
                                if (remove){
                                    Toast.makeText(this@DescriptionActivity, "Book removed from Favourites", Toast.LENGTH_SHORT).show()
                                    btnAddToFav.text = "Add to Favourites"
                                    val noFavColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                    btnAddToFav.setBackgroundColor(noFavColor)
                                } else {
                                    Toast.makeText(
                                        this@DescriptionActivity,
                                        "Some error occurred",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                    } else {
                        Toast.makeText(this@DescriptionActivity, "Some error occurred!, Please try again", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@DescriptionActivity, "Some error occurred!, Please try again", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener {
                Toast.makeText(this@DescriptionActivity, "Please try again", Toast.LENGTH_SHORT).show()
                finish()
            })

            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "bc7d33ad852f66"
                    return headers
                }
            }
            queue.add(jsonRequest)
        } else {
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Your device is not connected to the internet")
            dialog.setPositiveButton("Open Settings") {text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") {text, listener ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }
    }

    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int): AsyncTask<Void, Void, Boolean>() {

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when(mode){
                1 -> {
                    //checking for the book in favourites
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2 -> {
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }
}
