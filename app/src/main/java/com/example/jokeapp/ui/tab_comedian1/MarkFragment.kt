package com.example.jokeapp.ui.tab_comedian1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.jokeapp.R
import android.webkit.WebView
import android.webkit.WebViewClient

class MarkFragment(val mode:String = "") : Fragment() {

    private lateinit var markViewModel: MarkViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        markViewModel =
            ViewModelProvider(this).get(MarkViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_marknorm, container, false)

        val webview: WebView = root.findViewById(R.id.comedian1_webview)

        webview.webViewClient = WebViewClient()
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.overScrollMode = WebView.OVER_SCROLL_NEVER
        when (mode){
            "comedian1"->{
                webview.loadUrl(getString(R.string.comedian1URL)) //mark normand twitter
            }
            "comedian2"->{
                webview.loadUrl(getString(R.string.comedian2URL)) //ronny chieng twitter
            }
            "comedian3"->{
                webview.loadUrl(getString(R.string.comedian3URL))
            }
            else->{
                webview.loadUrl("about:blank") //empty page
            }
        }

        //val textView: TextView = root.findViewById(R.id.text_home)
        //homeViewModel.text.observe(viewLifecycleOwner, Observer {
        //    textView.text = it
        //})
        return root
    }
}