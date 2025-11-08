package com.github.andreyasadchy.xtra.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.github.andreyasadchy.xtra.R

class RankedStreamersSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.ranked_streamers_preferences, rootKey)
    }
}
