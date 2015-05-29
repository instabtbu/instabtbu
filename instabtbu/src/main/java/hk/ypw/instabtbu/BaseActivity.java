package hk.ypw.instabtbu;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class BaseActivity extends Activity {

    Activity thisActivity = this;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.btbu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Public_menu menu = new Public_menu();
        menu.thisActivity = thisActivity;
        menu.select(item);
        return false;
    }
}
