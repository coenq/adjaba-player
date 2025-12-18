package com.adjaba.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adjaba.R

class NewsAdapter(var news:List<RssItem>) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.news_custom,parent,false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.header.text=news[position].title
        holder.desc.text= news[position].description
    }

    override fun getItemCount(): Int =3



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val header: TextView=itemView.findViewById(R.id.header)
        val desc: TextView=itemView.findViewById(R.id.desc)
    }

}