package android.inflabnet.logisticainfnet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_file.view.*

class FilesAdapter(val file: Array<String>, private val itemClick: (String) -> Unit):
    RecyclerView.Adapter<FilesAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file,parent,false)
        return ViewHolder(view,itemClick)

    }

    override fun getItemCount(): Int {
        return file.size
    }

    override fun onBindViewHolder(holder: FilesAdapter.ViewHolder, position: Int) {
        holder.bindForecast(file[position])
    }

    class ViewHolder(view: View, val itemClick: (String) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bindForecast(filer: String) {
            with(filer) {
                itemView.textView.text = filer
                itemView.setOnClickListener { itemClick(this) }
            }
        }
    }
}

