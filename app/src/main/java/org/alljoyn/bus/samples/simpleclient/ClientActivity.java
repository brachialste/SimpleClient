/*
 * Copyright AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package org.alljoyn.bus.samples.simpleclient;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.samples.simpleclient.bstp.BSTProtocolCommands;
import org.alljoyn.bus.samples.simpleclient.bstp.BSTProtocolMessage;
import org.alljoyn.bus.samples.simpleclient.security.DiffieHellmanManager;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ClientActivity extends Activity implements NetworkListDialogFragment.Callbacks {
    private static final int MESSAGE_PING = 1;
    private static final int MESSAGE_PING_REPLY = 2;
    private static final int MESSAGE_POST_TOAST = 3;
    private static final int MESSAGE_START_PROGRESS_DIALOG = 4;
    private static final int MESSAGE_STOP_PROGRESS_DIALOG = 5;
    private static final int MESSAGE_CONN = 6;
    private static final int MESSAGE_CONN_REPLY = 7;
    private static final int MESSAGE_RQST = 8;
    private static final int MESSAGE_RQST_REPLY = 9;
    private static final int MESSAGE_CLSE = 10;
    private static final int MESSAGE_CLSE_REPLY = 11;
    private static final String TAG = "SimpleClient";
    private static BusAttachment mBus = null;
    private boolean mIsConnected = false;

    /* Load the native alljoyn_java library. */
    static {
        System.loadLibrary("alljoyn_java");
    }

    //    private EditText mEditText;
    private Button mButton;
    private Button mConnect;
    private Button mDissconnect;
    private ArrayAdapter<String> mListViewArrayAdapter;
    private ListView mListView;
    private Menu menu;
    private WifiConfiguration wifiConfiguration;
    private WifiManager wifiManager;
    // seguridad
    private DiffieHellmanManager diffieHellmanManager;

    /* Handler used to make calls to AllJoyn methods. See onCreate(). */
    private BusHandler mBusHandler;
    private static boolean isJoined = false;

    private ProgressDialog mDialog;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PING:
                    String ping = (String) msg.obj;
                    mListViewArrayAdapter.add("Ping:  " + ping);
                    break;
                case MESSAGE_PING_REPLY:
                    String ret = (String) msg.obj;
                    mListViewArrayAdapter.add("Reply:  " + ret);
//                mEditText.setText("");
                    break;
                case MESSAGE_POST_TOAST:
                    Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                case MESSAGE_START_PROGRESS_DIALOG:
                    mDialog = ProgressDialog.show(ClientActivity.this,
                            "",
                            "Buscando servicio.\nFavor de esperar...",
                            true,
                            true);
                    break;
                case MESSAGE_STOP_PROGRESS_DIALOG:
                    mDialog.dismiss();
                    break;
                case MESSAGE_CONN:
                    byte[] bstp_message = (byte[]) msg.obj;
                    mListViewArrayAdapter.add("CONN:  " + new String(bstp_message));
                    break;
                case MESSAGE_CONN_REPLY:
                    byte[] bstp_response = (byte[]) msg.obj;
                    mListViewArrayAdapter.add("CONN Response:  " + new String(bstp_response));
                    break;
                case MESSAGE_RQST:
                    byte[] rqst_message = (byte[]) msg.obj;
                    mListViewArrayAdapter.add("RQST:  " + new String(rqst_message));
                    break;
                case MESSAGE_RQST_REPLY:
                    byte[] rqst_response = (byte[]) msg.obj;
                    mListViewArrayAdapter.add("RQST Response:  " + new String(rqst_response));
                    break;
                case MESSAGE_CLSE:
                    byte[] clse_message = (byte[]) msg.obj;
                    mListViewArrayAdapter.add("CLSE:  " + new String(clse_message));
                    break;
                case MESSAGE_CLSE_REPLY:
                    byte[] clse_response = (byte[]) msg.obj;
                    mListViewArrayAdapter.add("CLSE Response:  " + new String(clse_response));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mListViewArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mListView = (ListView) findViewById(R.id.ListView);
        mListView.setAdapter(mListViewArrayAdapter);

//        mEditText = (EditText) findViewById(R.id.EditText);
//        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_NULL
//                        && event.getAction() == KeyEvent.ACTION_UP) {
//                    Log.d(TAG, "Test = " + view.getText().toString() );
//                    /* Call the remote object's Ping method. */
//                    Message msg = mBusHandler.obtainMessage(BusHandler.PING,
//                                                            view.getText().toString());
//                    mBusHandler.sendMessage(msg);
//                }
//                return true;
//            }
//        });

        mConnect = (Button) findViewById(R.id.buttonConnect);
        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkListDialogFragment networkListDialogFragment = NetworkListDialogFragment.newInstance();

                if (networkListDialogFragment != null) {
                    networkListDialogFragment.show(ClientActivity.this.getFragmentManager(), "NetworkListDialogFragment");
                    networkListDialogFragment.setCancelable(true);
                }
            }
        });

        mDissconnect = (Button) findViewById(R.id.buttonClose);
        mDissconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // enviamos el mensaje de cerrar
                BSTProtocolMessage bstProtocolMessage = new BSTProtocolMessage(BSTProtocolCommands.close, new JSONObject());
                if(bstProtocolMessage.getTramaByte() != null) {
                    Message msg = mBusHandler.obtainMessage(BusHandler.CLSE, bstProtocolMessage.getTramaByte());
                    mBusHandler.sendMessage(msg);
                }

            }
        });

        mButton = (Button) findViewById(R.id.buttonSend);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "Mensaje a enviar = " + mEditText.getText().toString() );
//                    /* Call the remote object's Ping method. */
//                Message msg = mBusHandler.obtainMessage(BusHandler.PING,
//                        mEditText.getText().toString());

                logInfo("Enviando mensaje... ");
                JSONObject test = new JSONObject();
                try {
                    test.put("param1", "param1");
                    test.put("param2", "param2");
                    test.put("param3", "param3");
                    test.put("param4", "param4");
                    test.put("param5", "param5");
                    test.put("param6", "param6");
                    test.put("param7", "param7");
                    test.put("param8", "param8");
                    test.put("param9", "param9");
                    test.put("param10", "param10");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // enviamos el mensaje de prueba
                BSTProtocolMessage bstProtocolMessage = new BSTProtocolMessage(BSTProtocolCommands.request, test);
                if(bstProtocolMessage.getTramaByte() != null) {
                    Message msg = mBusHandler.obtainMessage(BusHandler.RQST, bstProtocolMessage.getTramaByte());
                    mBusHandler.sendMessage(msg);
                }
            }
        });

        // hacemos invisibles los componentes de la lista y el boton de enviar
        mConnect.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        mButton.setVisibility(View.GONE);
        mDissconnect.setVisibility(View.GONE);

        // creamos una nueva instancia del manejador de WiFI
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // inicializamos la variable a false
        isJoined = false;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.quit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // validamos si esta conectado
        if(mIsConnected) {
            /* Disconnect to prevent resource leaks. */
            mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
        }
    }

    @Override
    public void connect(String bssid, String ssid) {
        logInfo("-> connect()");

        // creamos una nuevo configuración de WiFi
        wifiConfiguration = new WifiConfiguration();
        // seteamos los valores de la red
        wifiConfiguration.SSID = "\"" + ssid + "\"";
        wifiConfiguration.BSSID = bssid;
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;

        // asignamos el tipo de conexión
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        // agregamos la configuración al manager
        int netId = wifiManager.addNetwork(wifiConfiguration);

        // nos conectamos a la red
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.setWifiEnabled(true);

        if (wifiManager.reconnect()) {
            logInfo("-> reconnect()");

            // hacemos visibles los componentes de la lista y el boton de enviar
            mConnect.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mButton.setVisibility(View.VISIBLE);
            mDissconnect.setVisibility(View.VISIBLE);

            /* Make all AllJoyn calls through a separate handler thread to prevent blocking the UI. */
            HandlerThread busThread = new HandlerThread("BusHandler");
            busThread.start();
            mBusHandler = new BusHandler(busThread.getLooper());

            /* Connect to an AllJoyn object. */
            mBusHandler.sendEmptyMessage(BusHandler.CONNECT);
            mHandler.sendEmptyMessage(MESSAGE_START_PROGRESS_DIALOG);
        }

    }

    private void logStatus(String msg, Status status) {
        String log = String.format("%s: %s", msg, status);
        if (status == Status.OK) {
            Log.i(TAG, log);
        } else {
            Message toastMsg = mHandler.obtainMessage(MESSAGE_POST_TOAST, log);
            mHandler.sendMessage(toastMsg);
            Log.e(TAG, log);
        }
    }

    private void logException(String msg, BusException ex) {
        String log = String.format("%s: %s", msg, ex);
        Message toastMsg = mHandler.obtainMessage(MESSAGE_POST_TOAST, log);
        mHandler.sendMessage(toastMsg);
        Log.e(TAG, log, ex);
    }

    /*
     * print the status or result to the Android log. If the result is the expected
     * result only print it to the log.  Otherwise print it to the error log and
     * Sent a Toast to the users screen.
     */
    private void logInfo(String msg) {
        Log.i(TAG, msg);
    }

    /**
     * Enviamos un mensage de conexion a estacion encriptado para iniciar de manera segura
     * si el dispositivo al que intentamos conectarnos no puede desifrar el mensage no se establece
     * la comunicación, ya que probablemente no sea un dispositivo estacion.
     **/
    private void exchangeKey() {
        logInfo("-> exchangeKey()");
        // iniciamos el canal seguro usando BSTP
        diffieHellmanManager = DiffieHellmanManager.createNewInstance();
        String pk = diffieHellmanManager.generatePublicKey();
        JSONObject data = new JSONObject();
        try {
            data.put("pk", pk);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BSTProtocolMessage bstProtocolMessage = new BSTProtocolMessage(BSTProtocolCommands.connect, data);
        Message msg = mBusHandler.obtainMessage(BusHandler.CONN, bstProtocolMessage.getTramaByte());
        mBusHandler.sendMessage(msg);
    }

    /* This class will handle all AllJoyn calls. See onCreate(). */
    class BusHandler extends Handler {
        /* These are the messages sent to the BusHandler from the UI. */
        public static final int CONNECT = 1;
        public static final int JOIN_SESSION = 2;
        public static final int DISCONNECT = 3;
        public static final int PING = 4;
        public static final int CONN = 5;
        public static final int RQST = 6;
        public static final int CLSE = 7;

        /*
         * Name used as the well-known name and the advertised name of the service this client is
         * interested in.  This name must be a unique name both to the bus and to the network as a
         * whole.
         *
         * The name uses reverse URL style of naming, and matches the name used by the service.
         */
        private static final String SERVICE_NAME = "mx.ired.Bus.estacion";
        private static final short CONTACT_PORT = 42;
        private ProxyBusObject mProxyObj;
        private SimpleInterface mSimpleInterface;
        private int mSessionId;
        private boolean mIsInASession;
        private boolean mIsStoppingDiscovery;

        public BusHandler(Looper looper) {
            super(looper);

            mIsInASession = false;
            mIsConnected = false;
            mIsStoppingDiscovery = false;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /* Connect to a remote instance of an object implementing the SimpleInterface. */
                case CONNECT: {
                    logInfo("CONNECT");
                    org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(getApplicationContext());
                    if (mBus == null) {
                        /*
                         * All communication through AllJoyn begins with a BusAttachment.
                         *
                         * A BusAttachment needs a name. The actual name is unimportant except for internal
                         * security. As a default we use the class name as the name.
                         *
                         * By default AllJoyn does not allow communication between devices (i.e. bus to bus
                         * communication). The second argument must be set to Receive to allow communication
                         * between devices.
                         */
                        mBus = new BusAttachment(getPackageName(), BusAttachment.RemoteMessage.Receive);

                        /*
                         * Create a bus listener class
                         */
                        mBus.registerBusListener(new BusListener() {
                            @Override
                            public void foundAdvertisedName(String name, short transport, String namePrefix) {
                                logInfo(String.format("MyBusListener.foundAdvertisedName(%s, 0x%04x, %s)", name, transport, namePrefix));
                            /*
                             * This client will only join the first service that it sees advertising
                             * the indicated well-known name.  If the program is already a member of
                             * a session (i.e. connected to a service) we will not attempt to join
                             * another session.
                             * It is possible to join multiple session however joining multiple
                             * sessions is not shown in this sample.
                             */
                            if (!mIsConnected) {
                                Message msg = obtainMessage(JOIN_SESSION);
                                msg.arg1 = transport;
                                msg.obj = name;
                                sendMessage(msg);
                            }
                            }
                        });

                        /* To communicate with AllJoyn objects, we must connect the BusAttachment to the bus. */
                        Status status = mBus.connect();
                        logStatus("BusAttachment.connect()", status);
                        if (Status.OK != status) {
                            finish();
                            return;
                        }

                        /*
                         * Now find an instance of the AllJoyn object we want to call.  We start by looking for
                         * a name, then connecting to the device that is advertising that name.
                         *
                         * In this case, we are looking for the well-known SERVICE_NAME.
                         */
                        status = mBus.findAdvertisedName(SERVICE_NAME);
                        logStatus(String.format("BusAttachement.findAdvertisedName(%s)", SERVICE_NAME), status);
                        if (Status.OK != status) {
                            finish();
                            return;
                        }
                    }

                    break;
                }
                case (JOIN_SESSION): {
                    logInfo("JOIN_SESSION");
                    /*
                     * If discovery is currently being stopped don't join to any other sessions.
                     */
                    if (mIsStoppingDiscovery) {
                        break;
                    }
                    if(!isJoined){
                        // ponemos true al primer caso
                        isJoined = true;
                    }else{
                        break; // evitamos que otra sesión sea creada
                    }

                    /*
                     * In order to join the session, we need to provide the well-known
                     * contact port.  This is pre-arranged between both sides as part
                     * of the definition of the chat service.  As a result of joining
                     * the session, we get a session identifier which we must use to
                     * identify the created session communication channel whenever we
                     * talk to the remote side.
                     */
                    short contactPort = CONTACT_PORT;
                    SessionOpts sessionOpts = new SessionOpts();
                    sessionOpts.transports = (short) msg.arg1;
                    Mutable.IntegerValue sessionId = new Mutable.IntegerValue();

                    Status status = mBus.joinSession((String) msg.obj, contactPort, sessionId, sessionOpts, new SessionListener() {
                        @Override
                        public void sessionLost(int sessionId, int reason) {
                            mIsConnected = false;
                            logInfo(String.format("MyBusListener.sessionLost(sessionId = %d, reason = %d)", sessionId, reason));
                            // nos desconectamos del servicio
//                            mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
//                            mHandler.sendEmptyMessage(MESSAGE_START_PROGRESS_DIALOG);

                        }
                    });
                    logStatus("BusAttachment.joinSession() - sessionId: " + sessionId.value, status);

                    if (status == Status.OK) {
                        /*
                         * To communicate with an AllJoyn object, we create a ProxyBusObject.
                         * A ProxyBusObject is composed of a name, path, sessionID and interfaces.
                         *
                         * This ProxyBusObject is located at the well-known SERVICE_NAME, under path
                         * "/SimpleService", uses sessionID of CONTACT_PORT, and implements the SimpleInterface.
                         */
                        mProxyObj = mBus.getProxyBusObject(SERVICE_NAME,
                                "/ired",
                                sessionId.value,
                                new Class<?>[]{SimpleInterface.class});

                        /* We make calls to the methods of the AllJoyn object through one of its interfaces. */
                        mSimpleInterface = mProxyObj.getInterface(SimpleInterface.class);

                        mSessionId = sessionId.value;
                        mIsConnected = true;
                        mHandler.sendEmptyMessage(MESSAGE_STOP_PROGRESS_DIALOG);

                        /** Iniciamos el canal seguro **/
                        exchangeKey();

                    }
                    break;
                }

                /* Release all resources acquired in the connect. */
                case DISCONNECT: {
                    logInfo("DISCONNECT");
                    mIsStoppingDiscovery = true;
                    if (mIsConnected) {
                        Status status = mBus.leaveSession(mSessionId);
                        logStatus("BusAttachment.leaveSession()", status);
                    }
                    isJoined = false;   // inicializamos la variable de las sesiones
                    mBus.disconnect();
                    mBus = null;
                    getLooper().quit();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // limpiamos la vista
                            mListViewArrayAdapter.clear();
                            mListViewArrayAdapter.notifyDataSetChanged();

                            // mostramos la vista original
                            mConnect.setVisibility(View.VISIBLE);
                            mListView.setVisibility(View.GONE);
                            mButton.setVisibility(View.GONE);
                            mDissconnect.setVisibility(View.GONE);
                        }
                    });
                    break;
                }
                /*
                 * Call the service's Ping method through the ProxyBusObject.
                 *
                 * This will also print the String that was sent to the service and the String that was
                 * received from the service to the user interface.
                 */
                case PING: {
                    logInfo("PING");
                    try {
                        if (mSimpleInterface != null) {
                            sendUiMessage(MESSAGE_PING, msg.obj);
                            String reply = mSimpleInterface.Ping((String) msg.obj);
                            sendUiMessage(MESSAGE_PING_REPLY, reply);
                        }
                    } catch (BusException ex) {
                        logException("SimpleInterface.Ping()", ex);
                    }
                    break;
                }
                case CONN: {
                    logInfo("CONN");
                    try {
                        if (mSimpleInterface != null) {
                            sendUiMessage(MESSAGE_CONN, msg.obj);
                            logInfo("request = " + msg.obj);
                            // enviamos el mensaje pk y esperamos el pk de la estación
                            byte[] response = mSimpleInterface.CONN((byte[]) msg.obj);
                            logInfo("response = " + response);
                            sendUiMessage(MESSAGE_CONN_REPLY, response);
                            // creamos un mensaje con los datos recibidos
                            BSTProtocolMessage readMsg = new BSTProtocolMessage(response);
                            if (readMsg.getCmd() != null && readMsg.getCmd().equals(BSTProtocolCommands.connect)) {
                                logInfo("connect. ");
                                try {
                                    if (diffieHellmanManager.generateSecretKey(readMsg.getData().getString("pk"))) {

                                        // hacemos vibrar el dispositivo
                                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrar por 500 mils
                                        v.vibrate(500);
                                        //sound
                                        // reproducimos un sonido para la conexión Bluetooth
                                        MediaPlayer mp = MediaPlayer.create(ClientActivity.this, R.raw.tone);
                                        mp.start();

                                        logInfo("Se ha iniciado sesión segura con el servidor");
                                        Toast.makeText(getApplicationContext(), "Conectado de manera segura",
                                                Toast.LENGTH_LONG).show();

                                    } else { //Si no se conecta de modo seguro desconectar a bajo nivel
                                        Toast.makeText(ClientActivity.this, "No se pudo conectar de manera segura", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (BusException ex) {
                        logException("SimpleInterface.CONN()", ex);
                    }
                    break;
                }
                case RQST: {
                    logInfo("RQST");
                    try {
                        if (mSimpleInterface != null) {
                            sendUiMessage(MESSAGE_RQST, msg.obj);
                            logInfo("request = " + msg.obj);
                            // enviamos el mensaje pk y esperamos el pk de la estación
                            byte[] response = mSimpleInterface.RQST((byte[]) msg.obj);
                            logInfo("response = " + response);
                            sendUiMessage(MESSAGE_RQST_REPLY, response);
                            // creamos un mensaje con los datos recibidos
                            BSTProtocolMessage readMsg = new BSTProtocolMessage(response);
                            if (readMsg.getCmd() != null && readMsg.getCmd().equals(BSTProtocolCommands.accept)) {
                                logInfo("accept. ");
//                                try {
                                    Toast.makeText(ClientActivity.this, "Aceptado", Toast.LENGTH_SHORT).show();
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
                            }else{
                                logInfo("reject. ");
//                                try {
                                Toast.makeText(ClientActivity.this, "Rechazado", Toast.LENGTH_SHORT).show();
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }

                            }
                        }
                    } catch (BusException ex) {
                        logException("SimpleInterface.RQST()", ex);
                    }
                    break;
                }
                case CLSE: {
                    logInfo("CLSE");
                    try {
                        if (mSimpleInterface != null) {
                            sendUiMessage(MESSAGE_CLSE, msg.obj);
                            logInfo("request = " + msg.obj);
                            // enviamos el mensajede cerrado de sesión
                            byte[] response = mSimpleInterface.CLSE((byte[]) msg.obj);
                            logInfo("response = " + msg.obj);

                            // nos desconectamos del servicio
                            mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
                        }
                    } catch (BusException ex) {
                        logException("SimpleInterface.CLSE()", ex);
                    }
                    break;
                }
                default:
                    break;
            }
        }

        /* Helper function to send a message to the UI thread. */
        private void sendUiMessage(int what, Object obj) {
            mHandler.sendMessage(mHandler.obtainMessage(what, obj));
        }
    }

}
