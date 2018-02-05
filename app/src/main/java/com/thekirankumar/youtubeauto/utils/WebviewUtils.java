package com.thekirankumar.youtubeauto.utils;

import android.webkit.WebView;

import com.thekirankumar.youtubeauto.webview.VideoEnabledWebView;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by kiran.kumar on 06/01/18.
 */

public class WebviewUtils {
    public static final String NIGHT_CSS_PATH = "https://cdn.rawgit.com/thekirankumar/youtube-android-auto/8bfdac63/night_css/";
    private static final String FILE_BROWSER_SCRIPT_PATH = "file:///android_asset/filebrowser.js";
    private static final String FILE_BROWSER_CSS_PATH = "file:///android_asset/filebrowser.css";

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
        if(url == null) return null;
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain != null) {
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } else {
            return null;
        }
    }


    public static void injectFileListingHack(VideoEnabledWebView webView) {
        String url = webView.getUrl();
        if (url != null && url.startsWith("file:///") && url.endsWith("/")) {

            String js = ("javascript:var oldHead = document.head.innerHTML;" +
                    "    var head  = document.body;\n" +
                    "    var link  = document.createElement('link');\n" +
                    "    link.rel  = 'stylesheet';\n" +
                    "    link.type = 'text/css';\n" +
                    "    link.href = '" + FILE_BROWSER_CSS_PATH+"';\n" +
                    "    link.media = 'all';\n" +
                    "    head.appendChild(link);\n" +
                    "var script = document.createElement('script');" +
                    "script.src = '" + FILE_BROWSER_SCRIPT_PATH + "';" +
                    "head.appendChild(script);"+
                    "document.body.innerHTML+= '<h3 id=\"header\">LOCATION</h1><div id=\"parentDirLinkBox\" style=\"display:none\"><a id=\"parentDirLink\" class=\"icon up\"><span id=\"parentDirText\">[parent directory]</span></a> </div> <table> <thead> <tr class=\"header\" id=\"theader\"><th onclick=\"javascript:sortTable(0);\">Name</th><th class=\"detailsColumn\" onclick=\"javascript:sortTable(1);\">Size </th> <th class=\"detailsColumn\" onclick=\"javascript:sortTable(2);\">Date Modified </th> </tr> </thead> <tbody id=\"tbody\"> </tbody> </table>';" +
                    "document.body.innerHTML+= oldHead;");
            webView.loadUrl(js);
        }
    }

}
