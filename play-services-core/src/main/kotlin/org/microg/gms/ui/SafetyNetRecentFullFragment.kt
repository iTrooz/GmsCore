package org.microg.gms.ui

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.R
import com.google.android.gms.databinding.PushNotificationAppFragmentBinding
import com.google.android.gms.databinding.PushNotificationFragmentBinding
import com.google.android.gms.databinding.SafetyNetRecentFullFragmentBinding
import org.microg.gms.base.core.ui.databinding.PreferenceSwitchBarBindingImpl
import org.microg.gms.common.Utils
import org.microg.gms.safetynet.SafetyNetDatabase
import org.microg.gms.safetynet.SafetyNetRequestType
import org.microg.gms.safetynet.SafetyNetSummary

class SafetyNetRecentFullFragment : Fragment(R.layout.safety_net_recent_full_fragment) {


    class MyListView(context: Context) : ListView(context) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SafetyNetRecentFullFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val summary = arguments!!.get("summary") as SafetyNetSummary

        val pm = context!!.packageManager
        val appInfo = pm.getApplicationInfoIfExists(summary.packageName)
        if(appInfo==null){
            Toast.makeText(context, "Application not installed", Toast.LENGTH_SHORT).show()
            return
        }

        binding.appIcon.setImageDrawable(appInfo.loadIcon(pm))
        binding.appName.text = appInfo.loadLabel(pm)
        binding.packageName.text = summary.packageName

        binding.requestType.setValueText(summary.requestType.name)

        binding.timestamp.setValueText(DateUtils.getRelativeDateTimeString(context, summary.timestamp, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_SHOW_TIME))

        binding.key.setValueText(summary.key)

        if(summary.requestType==SafetyNetRequestType.RECAPTCHA){
            binding.nonce.isVisible = false
        }else{
            summary.nonce?.toHexString().let {
                if(it==null){
                    binding.nonce.setValueText("None");
                    // 0xffa500 is orange
                    binding.nonce.setValueColor(0xffa500)
                }else {
                    binding.nonce.setValueText(it);
                }
            }
        }

        summary.getInfoMessage().let {
            binding.resultStatus.setValueColor(it.first)
            binding.resultStatus.setValueText(it.second)
        }

    }

    lateinit var binding: SafetyNetRecentFullFragmentBinding

}
