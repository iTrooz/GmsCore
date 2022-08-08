package org.microg.gms.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.R

class SafetyNetRecentFullPreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_snet_recent_full)
    }

}