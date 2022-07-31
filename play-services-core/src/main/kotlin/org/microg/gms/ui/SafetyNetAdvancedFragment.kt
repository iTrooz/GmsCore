/*
 * SPDX-FileCopyrightText: 2021 microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.gms.ui

//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import androidx.preference.Preference
//import com.google.android.gms.R
//
//import org.microg.gms.safetynet.SafetyNetPrefs.PREF_SNET_OFFICIAL
//import org.microg.gms.safetynet.SafetyNetPrefs.PREF_SNET_SELF_SIGNED
//import org.microg.gms.safetynet.SafetyNetPrefs.PREF_SNET_THIRD_PARTY
//import org.microg.tools.ui.AbstractSettingsActivity
//import org.microg.tools.ui.RadioButtonPreference
import org.microg.tools.ui.ResourceSettingsFragment


class SafetyNetAdvancedFragment : ResourceSettingsFragment() {
//    private var radioOfficial: RadioButtonPreference? = null
//    private var radioSelfSigned: RadioButtonPreference? = null
//    private var radioThirdParty: RadioButtonPreference? = null
//    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String) {
//        super.onCreatePreferences(savedInstanceState, rootKey)
//        radioOfficial = findPreference<Preference>(PREF_SNET_OFFICIAL) as RadioButtonPreference?
//        radioSelfSigned = findPreference<Preference>(PREF_SNET_SELF_SIGNED) as RadioButtonPreference?
//        radioThirdParty = findPreference<Preference>(PREF_SNET_THIRD_PARTY) as RadioButtonPreference?
//    }
//
//    override fun onPreferenceTreeClick(preference: Preference): Boolean {
//        if (preference === radioOfficial) {
//            radioOfficial!!.isChecked = true
//            radioSelfSigned!!.isChecked = false
//            radioThirdParty!!.isChecked = false
//            return true
//        } else if (preference === radioSelfSigned) {
//            radioOfficial!!.isChecked = false
//            radioSelfSigned!!.isChecked = true
//            radioThirdParty!!.isChecked = false
//            return true
//        } else if (preference === radioThirdParty) {
//            radioOfficial!!.isChecked = false
//            radioSelfSigned!!.isChecked = false
//            radioThirdParty!!.isChecked = true
//            return true
//        }
//        return super.onPreferenceTreeClick(preference)
//    }
//
//    class AsActivity : AbstractSettingsActivity() {
//        override fun getFragment(): Fragment {
//            return SafetyNetAdvancedFragment()
//        }
//
//        init {
//            showHomeAsUp = true
//        }
//    }
//
//    init {
//        preferencesResource = R.xml.preferences_snet_advanced
//    }
}
