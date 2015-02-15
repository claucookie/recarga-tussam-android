package es.claucookie.recarga.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

import es.claucookie.recarga.R;

/**
 * Created by claucookie on 15/02/15.
 */
@EActivity(R.layout.activity_settings)
public class SettingsActivity extends ActionBarActivity {

    @AfterViews
    void initViews() {
        
        initActionBar();
    }

    private void initActionBar() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @OptionsItem
    void homeSelected() {
        finish();
    }
}
