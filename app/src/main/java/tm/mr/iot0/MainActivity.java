package tm.mr.iot0;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.regions.Regions;

import tm.mr.iot0.helper.Helper;

public class MainActivity extends AppCompatActivity implements Helper.Listener, AWSIotMqttClientStatusCallback, AWSIotMqttNewMessageCallback {

    Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = Helper.getInstance(
                getResources().getString(R.string.endpoint),
                getResources().getString(R.string.cognito_pool_id),
                getResources().getString(R.string.policy_name),
                Regions.US_EAST_2);

        helper.setup(this, this);
        helper.connect(this);
        helper.subscribe("", this);
        helper.publish("","");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.disconnect();
    }

    //Helper.Listener
    @Override
    public void settingUp() {

    }

    @Override
    public void setUp() {

    }

    //AWSIotMqttClientStatusCallback
    @Override
    public void onStatusChanged(AWSIotMqttClientStatus status, Throwable throwable) {

    }

    //AWSIotMqttNewMessageCallback
    @Override
    public void onMessageArrived(String topic, byte[] data) {

    }
}
