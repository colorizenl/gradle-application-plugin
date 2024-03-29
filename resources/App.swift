//-----------------------------------------------------------------------------
// File generated by Colorize Gradle application plugin
//-----------------------------------------------------------------------------

import SwiftUI
import WebKit

@main
struct Test_AppApp: App {
    var body: some Scene {
        WindowGroup {
            HybridWebView()
                .ignoresSafeArea()
        }
    }
}

struct HybridWebView: UIViewRepresentable {
    typealias UIViewType = WKWebView

    func makeUIView(context: Context) -> WKWebView {
        let preferences: WKPreferences = WKPreferences()
        preferences.javaScriptEnabled = true

        let config: WKWebViewConfiguration = WKWebViewConfiguration()
        config.preferences = preferences
        config.setValue(true, forKey: "_allowUniversalAccessFromFileURLs")

        let webView = WKWebView(frame: .zero, configuration: config)
        webView.scrollView.contentInsetAdjustmentBehavior = .never
        return webView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        DispatchQueue.main.async {
            let index = "HybridResources/index"
            let path = Bundle.main.path(forResource: index, ofType: "html")!
            let url = URL(fileURLWithPath: path)
            webView.loadFileURL(url, allowingReadAccessTo: url)
        }
    }
}
