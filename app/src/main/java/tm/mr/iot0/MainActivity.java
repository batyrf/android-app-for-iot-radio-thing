package tm.mr.iot0;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.regions.Regions;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import tm.mr.iot0.helper.AwsIotHelper;

/**
 * Created by viridis on 29.04.2018.
 */

public class MainActivity extends AppCompatActivity implements AwsIotHelper.Listener, RadioDialogFragment.OnSendCmdListener {

    public static final String TAG = "ANDNERD";
    AwsIotHelper helper;
    @BindView(R.id.tvStatus)
    AppCompatTextView tvStatus;
    @BindView(R.id.btnConnect)
    AppCompatButton btnConnect;
    RadioDialogFragment newFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        helper = AwsIotHelper.getInstance(
                getResources().getString(R.string.endpoint),
                getResources().getString(R.string.cognito_pool_id),
                getResources().getString(R.string.policy_name),
                Regions.US_EAST_2);

        helper.setup(this);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.connect();

            }
        });

        newFragment = new RadioDialogFragment();
        newFragment.setCancelable(false);
    }

    @Override
    public void onSetUpStart() {
        tvStatus.setText("setting up...");
        btnConnect.setEnabled(false);
    }

    @Override
    public void onSetUp() {
        tvStatus.setText("setting up is done");
        btnConnect.setEnabled(true);
    }

    @Override
    public void onStatusChanged(AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus status) {
        Log.d(TAG, "onStatusChanged: " + status);
        tvStatus.setText(status.toString());
        switch (status) {
            case Connected:
                helper.subscribe("sdk/test/Python");
                newFragment.show(getSupportFragmentManager(), "");
                break;
            case ConnectionLost:
                newFragment.dismiss();
                break;
            case Connecting:
            case Reconnecting:
            default:
                break;
        }
    }

    @Override
    public void onMessageArrived(JSONObject json) {
        Log.d(TAG, "onMessageArrived: " + json.toString());
    }

    @Override
    public void onSendCmd(String sJson) {
        helper.publish(sJson);
    }

}
