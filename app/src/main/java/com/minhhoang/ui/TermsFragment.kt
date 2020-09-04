package com.minhhoang.ui

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.minhhoang.MainApplication
import com.minhhoang.vpn.R

class TermsFragment : Fragment() {
    private lateinit var termsViewModel: TermsViewModel

    private lateinit var termsTextWiew: TextView
    private lateinit var termsAcceptButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_terms, container, false)

        termsViewModel = (requireActivity().application as MainApplication).appContainer.termsViewModel
        termsTextWiew = root.findViewById(R.id.terms_text_wiew)
        termsAcceptButton = root.findViewById(R.id.terms_accept_button)

        termsTextWiew.setText(Html.fromHtml(termsViewModel.termsText))

        termsAcceptButton.setOnClickListener {
            termsAcceptButton.isEnabled = false
            CoroutineScope(Dispatchers.Main).launch {
                termsViewModel.acceptCurrentTerms()
                root.findNavController().navigate(R.id.action_go_to_vpn_screen)
            }
        }

        onBackPress { emulateHomePress() }

        return root
    }
}
