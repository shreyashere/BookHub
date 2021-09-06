package com.internshala.bookhub.adapter

import com.internshala.bookhub.activity.DescriptionActivity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.internshala.bookhub.R
import com.squareup.picasso.Picasso
import com.internshala.bookhub.model.Book


class DashboardRecyclerAdapter(
    val context: Context,
    val itemList: ArrayList<Book>): RecyclerView.Adapter<DashboardRecyclerAdapter.DashboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_dashboard_single_row, parent, false)
        return DashboardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val book = itemList[position]
        holder.txtBookName.text = book.bookName
        holder.txtAuthor.text = book.bookAuthor
        holder.txtCost.text = book.bookPrice
        holder.txtRating.text = book.bookRating
        //holder.imgBook.setImageResource(book.bookImage)
        Picasso.get().load(book.bookImage).error(R.drawable.default_book_cover).into(holder.imgBook)

        holder.llContent.setOnClickListener{
            val intent = Intent(context, DescriptionActivity::class.java)
            intent.putExtra("book_id", book.bookId)
            context.startActivity(intent)
        }
    }

    class DashboardViewHolder(view:View): RecyclerView.ViewHolder(view) {
        val txtBookName: TextView = view.findViewById(R.id.txtBookName)
        val txtAuthor: TextView = view.findViewById(R.id.txtAuthor)
        val txtCost: TextView = view.findViewById(R.id.txtCost)
        val txtRating: TextView = view.findViewById(R.id.txtRating)
        val imgBook: ImageView = view.findViewById(R.id.imgBook)
        val llContent: LinearLayout = view.findViewById(R.id.llContent)
    }

}
