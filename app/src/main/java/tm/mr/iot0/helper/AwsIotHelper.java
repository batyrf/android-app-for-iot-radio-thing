package tm.mr.iot0.helper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.UUID;

/**
 * Created by viridis on 17.04.2018.
 */

public class AwsIotHelper implements AWSIotMqttClientStatusCallback, AWSIotMqttNewMessageCallback {

    private static final String KEYSTORE_NAME = "iot_keystore";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String CERTIFICATE_ID = "default";

    private static final String LOG_TAG = "ANDNERD";

    private String customer_specific_endpoint;
    private String cognito_pool_id;
    private String aws_iot_policy_name;
    private Regions my_region;
    private AWSIotMqttManager mqttManager;
    private KeyStore keyStore;
    private Context context;
    private Listener listener;
    private String sTopic;

    private static AwsIotHelper INSTANCE;

    public static AwsIotHelper getInstance(String customer_specific_endpoint, String cognito_pool_id, String aws_iot_policy_name, Regions my_region) {
        if (INSTANCE == null)
            INSTANCE = new AwsIotHelper(customer_specific_endpoint, cognito_pool_id, aws_iot_policy_name, my_region);
        return INSTANCE;
    }

    private AwsIotHelper(String customer_specific_endpoint, String cognito_pool_id, String aws_iot_policy_name, Regions my_region) {
        this.customer_specific_endpoint = customer_specific_endpoint;
        this.cognito_pool_id = cognito_pool_id;
        this.aws_iot_policy_name = aws_iot_policy_name;
        this.my_region = my_region;
    }

    private AWSIotMqttManager createMqttManager() {
        String clientId = UUID.randomUUID().toString();

        AWSIotMqttManager mqttManager = new AWSIotMqttManager(clientId, customer_specific_endpoint);
        mqttManager.setKeepAlive(10);

        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic", "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);
        return mqttManager;
    }

    private KeyStore loadKeystore(Context context, String keystorePath, String keystoreName, String keystorePassword, String certificateId) {
        if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
            if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath, keystoreName, keystorePassword)) {
                Log.i(LOG_TAG, "Certificate " + certificateId + " found in keystore - using for MQTT.");
                return AWSIotKeystoreHelper.getIotKeystore(certificateId, keystorePath, keystoreName, keystorePassword);
            } else {
                Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
                return createAndSaveKeystore(context, keystorePath, keystoreName, keystorePassword, certificateId);
            }
        } else {
            Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            return createAndSaveKeystore(context, keystorePath, keystoreName, keystorePassword, certificateId);
        }
    }

    private KeyStore createAndSaveKeystore(Context context, String keystorePath, String keystoreName, String keystorePassword, String certificateId) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(context, cognito_pool_id, my_region);

        Region region = Region.getRegion(my_region);

        AWSIotClient mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        CreateKeysAndCertificateRequest createKeysAndCertificateRequest = new CreateKeysAndCertificateRequest();
        createKeysAndCertificateRequest.setSetAsActive(true);
        final CreateKeysAndCertificateResult createKeysAndCertificateResult = mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
        Log.i(LOG_TAG, "Cert ID: " + createKeysAndCertificateResult.getCertificateId() + " created.");

        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId, createKeysAndCertificateResult.getCertificatePem(), createKeysAndCertificateResult.getKeyPair().getPrivateKey(), keystorePath, keystoreName, keystorePassword);

        KeyStore clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId, keystorePath, keystoreName, keystorePassword);

        AttachPrincipalPolicyRequest policyAttachRequest = new AttachPrincipalPolicyRequest();
        policyAttachRequest.setPolicyName(aws_iot_policy_name);
        policyAttachRequest.setPrincipal(createKeysAndCertificateResult.getCertificateArn());
        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

        return clientKeyStore;
    }

    private boolean checkIfConnected() {
        // TODO: 18.04.2018 check if mqttManager is connected
        return true;
    }

    public void setup(final Context context) {
        this.context = context;
        if (context instanceof Listener) {
            this.listener = (Listener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AwsIotHelper.Listener");
        }

        listener.onSetUpStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mqttManager = createMqttManager();
                    keyStore = loadKeystore(context, context.getFilesDir().getPath(), KEYSTORE_NAME, KEYSTORE_PASSWORD, CERTIFICATE_ID);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSetUp();
                        }
                    });
                } catch (final Exception e) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public void connect() {
        if (mqttManager != null)
            mqttManager.connect(keyStore, this);
    }

    public void subscribe(String topic) {
        if (mqttManager != null && checkIfConnected()) {
            this.sTopic = topic;
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0, this);
        }
    }


    public void publish(String msg) {
        if (mqttManager != null && checkIfConnected() && getsTopic() != null)
            mqttManager.publishString(msg, getsTopic(), AWSIotMqttQos.QOS0);
    }

    public boolean disconnect() {
        if (mqttManager != null && checkIfConnected())
            return mqttManager.disconnect();
        return false;
    }

    public String getsTopic() {
        return sTopic;
    }

    @Override
    public void onStatusChanged(final AWSIotMqttClientStatus status, Throwable throwable) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listener.onStatusChanged(status);
            }
        });
    }

    @Override
    public void onMessageArrived(String topic, byte[] data) {
        try {
            final JSONObject jsonObj = new JSONObject(new String(data, "UTF-8"));
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onMessageArrived(jsonObj);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public interface Listener {
        void onSetUpStart();

        void onSetUp();

        void onStatusChanged(AWSIotMqttClientStatus status);

        void onMessageArrived(JSONObject json);

        void onError(String sErrorMessage);
    }

}





















