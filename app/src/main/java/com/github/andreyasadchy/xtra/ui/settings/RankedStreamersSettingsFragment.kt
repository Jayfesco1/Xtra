package com.github.andreyasadchy.xtra.ui.settings

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.andreyasadchy.xtra.R

class RankedStreamersSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.ranked_streamers_preferences, rootKey)

        findPreference<Preference>("ranked_streamers_list_editor")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_rankedStreamersSettingsFragment_to_rankedStreamersEditorFragment)
            true
        }
    }
}
