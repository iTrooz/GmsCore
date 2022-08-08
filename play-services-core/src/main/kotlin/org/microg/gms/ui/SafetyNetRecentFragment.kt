package org.microg.gms.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.R
import org.microg.gms.safetynet.SafetyNetDatabase

class SafetyNetRecentFragment : Fragment(R.layout.safety_net_recent_fragment){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = SafetyNetDatabase(context)
        val recentRequests = db.recentRequestsList

        val recyclerView: RecyclerView = view.findViewById(R.id.snet_recent_recyclerview)
        if(recentRequests.isEmpty()){
            recyclerView.isVisible = false
        }else{
            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = SafetyNetSummaryAdapter(recentRequests) {
                findNavController().navigate(requireContext(), R.id.openSafetyNetRecentFull, bundleOf(
                    "summary" to it
                ))
            }
            recyclerView.adapter = adapter
        }
    }

}
