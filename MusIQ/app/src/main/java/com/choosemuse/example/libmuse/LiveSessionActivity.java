package com.choosemuse.example.libmuse;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/** Ameris Rudland
 * musIQ
 * created @ The Lady Hacks
 * York University, 2016-11-19, 2016-11-20.
 *
 * LiveSessionActivity occurs when a new session is started. It must
 *  - read incoming muse data
 *  - display data in a pretty graph form
 *  - change music depending on time/mood in the session
 *  - save data from the session
 */

public class LiveSessionActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
