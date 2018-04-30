package tm.mr.iot0;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
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
    private static final String AWS_ENDPOINT = "aws_endpoint";
    private static final String AWS_COGNITO_POOL_ID = "aws_cognito_pool_id";
    private static final String AWS_POLICY_NAME = "aws_policy_name";
    private static final String IOT_TOPIC = "iot_topic";
    AwsIotHelper helper;
    @BindView(R.id.tvStatus)
    AppCompatTextView tvStatus;
    @BindView(R.id.btnConnect)
    AppCompatButton btnConnect;

    @BindView(R.id.etEndpoint)
    AppCompatEditText etEndpoint;
    @BindView(R.id.etCognitoPoolId)
    AppCompatEditText etCognitoPoolId;
    @BindView(R.id.etPolicyName)
    AppCompatEditText etPolicyName;
    @BindView(R.id.etTopicName)
    AppCompatEditText etTopicName;

    RadioDialogFragment newFragment;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper = AwsIotHelper.getInstance(
                        etEndpoint.getText().toString(),
                        etCognitoPoolId.getText().toString(),
                        etPolicyName.getText().toString(),
                        Regions.US_EAST_2);

                helper.setup(MainActivity.this);
                saveConf();
            }
        });

        newFragment = new RadioDialogFragment();
        newFragment.setCancelable(false);

        loadConf();
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
        helper.connect();
    }

    @Override
    public void onStatusChanged(AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus status) {
        Log.d(TAG, "onStatusChanged: " + status);
        tvStatus.setText(status.toString());
        switch (status) {
            case Connected:
                helper.subscribe(etTopicName.getText().toString());
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
    public void onError(String sErrorMessage) {
        btnConnect.setEnabled(true);
        tvStatus.setText(sErrorMessage);
    }

    @Override
    public void onSendCmd(String sJson) {
        helper.publish(sJson);
    }

    public void saveConf() {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(AWS_ENDPOINT, etEndpoint.getText().toString());
        editor.putString(AWS_COGNITO_POOL_ID, etCognitoPoolId.getText().toString());
        editor.putString(AWS_POLICY_NAME, etPolicyName.getText().toString());
        editor.putString(IOT_TOPIC, etTopicName.getText().toString());
        editor.apply();
    }

    public void loadConf() {
        String sEndpoint = getPreferences(MODE_PRIVATE).getString(AWS_ENDPOINT, "");
        String sCognitoPoolId = getPreferences(MODE_PRIVATE).getString(AWS_COGNITO_POOL_ID, "");
        String sPolicyName = getPreferences(MODE_PRIVATE).getString(AWS_POLICY_NAME, "");
        String sTopic = getPreferences(MODE_PRIVATE).getString(IOT_TOPIC, "sdk/test/Python");

        etEndpoint.setText(sEndpoint);
        etCognitoPoolId.setText(sCognitoPoolId);
        etPolicyName.setText(sPolicyName);
        etTopicName.setText(sTopic);
    }
}
