package org.microg.gms.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.microg.gms.safetynet.SafetyNetDatabase
import org.microg.gms.safetynet.SafetyNetSummary

class SafetyNetRecentFullFragment : PreferenceFragmentCompat() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SafetyNetRecentFullFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_push_notifications)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val summary = arguments!!
            .get("summary") as SafetyNetSummary

        Log.e("MYTEST", "GOT DATA :")
        Log.e("MYTEST", summary.toString())

        val pm = context!!.packageManager
        binding.appIcon.setImageDrawable(pm.getApplicationInfoIfExists(summary.packageName)?.loadIcon(pm))

        binding.appName.text = "Hey"



    }

    lateinit var binding: SafetyNetRecentFullFragmentBinding

}
