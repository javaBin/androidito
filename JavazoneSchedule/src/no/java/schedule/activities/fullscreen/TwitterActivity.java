/*
 * Copyright (C) 2009 Virgil Dobjanschi, Jeff Sharkey, Filip Maelbrancke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.java.schedule.activities.fullscreen;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * An activity which displays an expandable list
 */
public class TwitterActivity extends Activity {
    private static final String TAG = "TwitterActivity";

    WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setKeepScreenOn(true);

        setContentView(webview);

        webview.loadUrl("file:///android_asset/twitter.html");
        //TODO autodetect screensize for the twitter widget.
    }
}