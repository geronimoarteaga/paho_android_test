package ve.org.mqtt.paho.android.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    private final static String DEFAULT_CLIENT_ID = MqttAsyncClient.generateClientId();

    private MqttAndroidClient client;
    private final MqttConnectOptions options = new MqttConnectOptions();


    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Lifecycle Activity Methods ...
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trace("onCreate");

        setContentView(R.layout.activity_main);
        create(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        trace("onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        trace("onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        trace("onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        trace("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        trace("onPause");
    }


    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // MQTT Methods ...
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    protected void create(View view) {

        final String clientId = DEFAULT_CLIENT_ID;

        trace("Created client... " + clientId);

        if (client == null) {
            client = new MqttAndroidClient(this.getApplicationContext(), "ssl://test.mosquitto.org:8883", DEFAULT_CLIENT_ID,
                    new MemoryPersistence(), MqttAndroidClient.Ack.AUTO_ACK);

            // Enabled reconnect ...
            options.setAutomaticReconnect(true);

            // Keep Alive & Connection Timeout ...
            options.setKeepAliveInterval(30);
            options.setConnectionTimeout(120);

            // Connect with Last Will Topic ...
            options.setWill("last/will", "bye".getBytes(), 1, false);

            // Secure connection ...
            try {
                options.setSocketFactory(client.getSSLSocketFactory(getApplicationContext()
                        .getResources().openRawResource(R.raw.mosquitto), "mosquitto"));
            } catch (MqttSecurityException ex) {
                trace("setSocketFactory()...", ex);
            }

            // Connect with Username / Password ...
//        options.setUserName("USER");
//        options.setPassword("PSW".toCharArray());

            client.setCallback(new MqttCallbackExtended() {

                @Override
                public void connectComplete(boolean reconnected, String s) {
                    trace((reconnected ? "Reconnect" : "Connect") + " complete... " + clientId + "\n" + s);
                }

                @Override
                public void connectionLost(Throwable cause) {
                    trace("Connection lost... " + clientId, cause);
//                if (cause != null)
//                    connect(null);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    trace("Message arrived... " + clientId + "\n" + topic + "\n" + new String(message.getPayload()));

                    Toast.makeText(MainActivity.this, topic + "\n" + new String(message.getPayload()), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    trace("Delivery complete... " + clientId + "\n" + token);
                }
            });

            // Trace / Debugging ...
            client.setTraceCallback(new MqttTraceHandler() {
                @Override
                public void traceDebug(String source, String message) {
                    Log.i(source, message);
                }

                @Override
                public void traceError(String source, String message) {
                    Log.i(source, message);
                }

                @Override
                public void traceException(String source, String message, Exception ex) {
                    Log.e(source, message, ex);
                }
            });
            client.setTraceEnabled(true);
        }
    }

    protected void connect(View v) {
        trace("Connect... ");
        if (!client.isConnected())
            try {
                IMqttToken token = client.connect(options);

                trace("Connecting...\n" + token.getClient().getClientId());
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        trace("Success connect...\n" + asyncActionToken);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        trace("Failure connect...\n" + asyncActionToken, exception);
                    }
                });
            } catch (MqttException ex) {   // | IOException
                ex.printStackTrace();
            }
    }

    protected void disconnect(View v) {
        if (client.isConnected())
            try {
                client.disconnect()
                        .setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                trace("Success disconnect...\n" + asyncActionToken);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                trace("Failure disconnect...\n" + asyncActionToken, exception);
                            }
                        });
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
    }

    protected void publish(View v) {
        if (client.isConnected())
            try {
                IMqttDeliveryToken token = client.publish("topic", new MqttMessage("payload".getBytes("UTF-8")));
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        trace("Success publish...\n" + asyncActionToken);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        trace("Failure publish...\n" + asyncActionToken, exception);
                    }
                });

            } catch (UnsupportedEncodingException | MqttException ex) {
                ex.printStackTrace();
            }
    }

    protected void subscribe(View v) {
        if (client.isConnected())
            try {
                final IMqttToken subToken = client.subscribe("retained", 1 /* qos */);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        trace("Success subscribe...\n" + asyncActionToken);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        trace("Failure subscribe...\n" + asyncActionToken, exception);
                    }
                });
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
    }

    protected void unsubscribe(View v) {
        if (client.isConnected())
            try {
                client.unsubscribe("retained").setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        trace("Success unsubscribe...\n" + asyncActionToken);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        trace("Failure unsubscribe...\n" + asyncActionToken, exception);
                    }
                });
            } catch (MqttException ex) {
                ex.printStackTrace();
            }
    }

    protected void retain(View v) {
        if (client.isConnected())
            try {
                client.publish("retained", "payload".getBytes("UTF-8"), 0 /* qos */, true /* retained*/)
                        .setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                trace("Success retain...\n" + asyncActionToken);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                trace("Failure retain...\n" + asyncActionToken, exception);
                            }
                        });

            } catch (UnsupportedEncodingException | MqttException ex) {
                ex.printStackTrace();
            }
    }

    protected void remove(View v) {
        if (client.isConnected())
            try {
                MqttMessage payloadRetained = new MqttMessage();
                payloadRetained.setRetained(true);
                client.publish("retained", payloadRetained)
                        .setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                trace("Success remove...\n" + asyncActionToken);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                trace("Failure remove...\n" + asyncActionToken, exception);
                            }
                        });

            } catch (MqttException ex) {
                ex.printStackTrace();
            }
    }


    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Trace Methods ...
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private void trace(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }

    private void trace(String message, Throwable throwable) {
        Log.e(this.getClass().getSimpleName(), message, throwable);
    }
}
