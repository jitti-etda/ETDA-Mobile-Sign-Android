package th.or.etda.teda.mobile.ui.cert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import th.or.etda.teda.mobile.R
import th.or.etda.teda.mobile.data.Certificate


class KeyCertAdapter() :
    RecyclerView.Adapter<KeyCertAdapter.ViewHolder>() {
    private  var dataSet= ArrayList<Certificate>()
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cert_title: TextView
        val cert_date: TextView

        init {
            // Define click listener for the ViewHolder's View.
            cert_title = view.findViewById(R.id.cert_title)
            cert_date = view.findViewById(R.id.cert_date)
        }
    }

    fun addAll(cert: ArrayList<Certificate>) {
        dataSet.addAll(cert)
        notifyDataSetChanged()
    }

    fun add(cert: Certificate) {
        dataSet.add(cert)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Certificate {
        return dataSet.get(position)
    }

    fun removeItem(cert: Certificate) {
        dataSet.remove(cert)
        notifyDataSetChanged()
    }
    fun clear() {
        dataSet.clear()
        notifyDataSetChanged()
    }
    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_cert, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.cert_date.setText(dataSet[position].date)
        var split = dataSet[position].certName.split("_")
        if(split.size>2){
            viewHolder.cert_title.setText(dataSet[position].certName.replace("_"+split[split.size-1],""))
        }else{
            viewHolder.cert_title.setText(split[0])
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}