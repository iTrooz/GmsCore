package org.microg.gms.ui

import android.text.format.DateUtils
import org.microg.gms.safetynet.SafetyNetSummary
import org.microg.gms.ui.SafetyNetSummaryAdapter.SafetyNetSummaryViewHolder
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.R

class SafetyNetSummaryAdapter(recentRequests: List<SafetyNetSummary>) :
    ListAdapter<SafetyNetSummary, SafetyNetSummaryViewHolder>(DiffCallback) {

    init {
        submitList(recentRequests)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SafetyNetSummary>() {
        override fun areItemsTheSame(oldItem: SafetyNetSummary, newItem: SafetyNetSummary): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: SafetyNetSummary, newItem: SafetyNetSummary): Boolean {
            return oldItem==newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SafetyNetSummaryViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.safety_net_recent_card, parent, false)
        return SafetyNetSummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SafetyNetSummaryViewHolder, position: Int) {
        val summary = getItem(position)
        val context = holder.packageName.context
        val pm = context.packageManager

        holder.appIcon.setImageDrawable(pm.getApplicationInfoIfExists(summary!!.packageName)?.loadIcon(pm))

        holder.requestType.text = summary.requestType.name
        holder.date.text = DateUtils.getRelativeDateTimeString(context, summary.timestamp, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME)


        holder.packageName.text = summary.packageName
        val infoMsg = summary.getInfoMessage()
        holder.infoMsg.setTextColor(infoMsg.component1())
        holder.infoMsg.text = infoMsg.component2()

    }

    class SafetyNetSummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.snet_recent_appicon)
        val requestType: TextView = view.findViewById(R.id.snet_recent_type)
        val date: TextView = view.findViewById(R.id.snet_recent_date)
        val packageName: TextView = view.findViewById(R.id.snet_recent_package)
        val infoMsg: TextView = view.findViewById(R.id.snet_recent_infomsg)
    }

}