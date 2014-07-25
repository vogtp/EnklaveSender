package ch.almana.android.enklave.sender;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.almana.android.enklave.sender.utils.Logger;
import ch.almana.android.enklave.sender.utils.Settings;

public class ChangelogActivity extends Activity {

    private static final String CHANGELOG = "CHANGELOG";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Settings.getInstance(this).hasHoloTheme()){
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.changelog));
        setContentView(R.layout.changelog);
        TextView tvChangelog = (TextView) findViewById(R.id.tvChangelog);

        try {
            InputStream is = getResources().getAssets().open(CHANGELOG);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            for (int i = 0; i < 7; i++) {
                reader.readLine();
            }
            StringBuffer sb = new StringBuffer();
            String line = reader.readLine();
            while (line != null /* && !line.startsWith("V 1.6.1") */) {
                sb.append(line).append("\n");
                line = reader.readLine();
            }
            tvChangelog.setText(sb.toString());
        } catch (IOException e) {
            Logger.w("Cannot read the changelog", e);
        }

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
