package com.thekirankumar.youtubeauto;

import android.webkit.WebView;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by kiran.kumar on 06/01/18.
 */

class WebviewUtils {
    public static final String NIGHT_CSS_PATH = "https://cdn.rawgit.com/thekirankumar/youtube-android-auto/8bfdac63/night_css/";

    public static void injectNightModeCss(WebView webView, boolean isNightMode) {
        String domainName = null;
        try {
            domainName = getDomainName(webView.getUrl());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (domainName != null && isNightMode) {
            String injection = "var cssId = 'nightModeCss';\n" +
                    "if (!document.getElementById(cssId))\n" +
                    "{\n" +
                    "    var head  = document.getElementsByTagName('head')[0];\n" +
                    "    var link  = document.createElement('link');\n" +
                    "    link.id   = cssId;\n" +
                    "    link.rel  = 'stylesheet';\n" +
                    "    link.type = 'text/css';\n" +
                    "    link.href = '" + NIGHT_CSS_PATH + domainName + ".css';\n" +
                    "    link.media = 'all';\n" +
                    "    head.appendChild(link);\n" +
                    "}";
            webView.loadUrl("javascript:" + injection);
        } else {
            String injection = "var cssId = 'nightModeCss'; var element = document.getElementById(cssId);\n" +
                    "if (element)\n" +
                    "{\n" +
                    "    var head  = document.getElementsByTagName('head')[0];\n" +
                    "    head.removeChild(element);\n" +
                    "}";
            webView.loadUrl("javascript:" + injection);
        }

    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain != null) {
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } else {
            return null;
        }
    }

    public static void injectHashChangeListener(WebView webView, String javascriptInterface) {
        webView.loadUrl("javascript:window.onhashchange = function() { " +
                "console.log('onhashchange');\n" +
                "window."+javascriptInterface+".onUrlChange(document.location);" +
                "};");
    }
}
