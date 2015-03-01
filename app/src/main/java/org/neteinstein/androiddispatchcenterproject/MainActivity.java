package org.neteinstein.androiddispatchcenterproject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private Button mMainButton;
    private TextView mTestResult;
    private UITestClass testClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainButton = (Button) findViewById(R.id.main_button);
        mMainButton.setOnClickListener(onMainButtonClickListener);
        mTestResult = (TextView) findViewById(R.id.main_test_result);

        testClass = new UITestClass(mTestResult);
    }

    private View.OnClickListener onMainButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            testClass.testDispatch();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
