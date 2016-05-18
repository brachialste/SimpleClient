package org.alljoyn.bus.samples.simpleclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by imacmovil on 10/05/16.
 */
public class NetworkListDialogFragment extends DialogFragment {

    private static String TAG = "NetworkListDialogFragment";

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private static WifiManager wifiManager;
    private static WifiScanReceiver wifiReciever;

    private TextView title;
    private ProgressBar mProgress;

    private Button btnScan;

    private String info = "";
    private String address = "";
    private static String name = "";

    private static Dialog dialog_fragment = null;

    public static ArrayList<HashMap<String, String>> networkList;
    public static NetworkAdapter adapter_network;
    public static ArrayList<NetworkView> mNetworks;
    public static HashSet<String> hashNetworks;

    private Callbacks wifiDialogCallbacks = sWifiDialogCallbacks;

    /**
     * Interface de callback que debe ser implementada por las actividades que contengan a
     * este Fragment. Este mecanismo permite notificar a las actividades por eventos del fragment.
     */
    public interface Callbacks {
        public void connect(String bssid, String ssid);
    }


    /**
     * Implementación local del callback
     */
    private static Callbacks sWifiDialogCallbacks = new Callbacks() {
        @Override
        public void connect(String bssid, String ssid) {
        }
    };

    public static synchronized NetworkListDialogFragment newInstance() {
        Log.d(TAG, "-> newInstance()");
        return new NetworkListDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        dialog_fragment = new Dialog(getActivity());
        dialog_fragment.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog_fragment.setContentView(R.layout.networks_list);
        dialog_fragment.setCanceledOnTouchOutside(false);

        btnScan = (Button) dialog_fragment.findViewById(R.id.button_scan);

        title = (TextView) dialog_fragment.findViewById(R.id.dialog_title_scanner);
        mProgress = (ProgressBar) dialog_fragment.findViewById(R.id.progress_title);

        networkList = new ArrayList<HashMap<String, String>>();

        // conjunto de folios de servicio
        hashNetworks = new HashSet<String>();

        // lista de componentes de servicio
        mNetworks = new ArrayList<NetworkView>();

        // obtenemos el adaptador y le pasamos los datos de la lista de HashMaps
        adapter_network = new NetworkAdapter(getActivity(), mNetworks);

        // Find and set up the ListView for newly discovered devices
        ListView networksListView = (ListView) dialog_fragment.findViewById(R.id.new_networks);
        networksListView.setAdapter(adapter_network);
        networksListView.setOnItemClickListener(mDeviceClickListener);

        // obtenemos el manejador de redes
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();

        getActivity().registerReceiver(wifiReciever,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // botón escanear
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                adapter_network.clear();

                mProgress.setVisibility(View.VISIBLE);
                mProgress.setIndeterminate(true);

                title.setText("Escaneando redes...");

                wifiManager.startScan();
                v.setVisibility(View.GONE);
            }
        });

        return dialog_fragment;
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            mProgress.setIndeterminate(false);
            mProgress.setVisibility(View.GONE);
            title.setText("Seleccione la red a conectar");

            List<ScanResult> wifiScanList = wifiManager.getScanResults();
            for(int i = 0; i < wifiScanList.size(); i++){

                HashMap<String, String> map = new HashMap<String, String>();
                map.put(NetworkAdapter.BSSID_NETWORK, wifiScanList.get(i).BSSID);
                map.put(NetworkAdapter.SSID_NETWORK, wifiScanList.get(i).SSID);

                networkList.add(map);

                if(networkList.size() > 0){

                    for(HashMap<String, String> n : networkList){
                        NetworkView net = new NetworkView();
                        net.bssid = n.get(NetworkAdapter.BSSID_NETWORK);
                        net.ssid = n.get(NetworkAdapter.SSID_NETWORK);

                        // validamos que no se agregue el pedido duplicado a la lista
                        if(!net.ssid.isEmpty() &&
                                !net.bssid.isEmpty() &&
                                net.ssid.contains("iRED") && // solo los que inicien con iRED
                                !hashNetworks.contains(net.bssid)){
                            hashNetworks.add(net.bssid);
                            adapter_network.add(net);
                        }
                    }

                    if (networkList != null) {
                        // notificamos que se han modificado los datos
                        adapter_network.notifyDataSetChanged();
                    }
                }
            }

            if (adapter_network.getCount() == 0) {

                btnScan.setVisibility(View.VISIBLE);

                NetworkView nodev = new NetworkView();
                nodev.bssid = "";
                nodev.ssid = "No se encontraron redes";
                adapter_network.add(nodev);
            }

        }

    }

    // The on-click listener for all devices in the ListViews
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            final NetworkView item = (NetworkView) av.getItemAtPosition(arg2);

            String bssidNetwork = item.bssid;
            String ssidNetwork = item.ssid;

            Bundle data = new Bundle();
            data.putString("bssid", bssidNetwork);
            data.putString("ssid", ssidNetwork);

            name = ssidNetwork;

            if(!bssidNetwork.equals("")) {

                Log.d(TAG, "Conectar con " + ssidNetwork + " [" + bssidNetwork + "]");

                //Crear dialogo para mostrar pregunta si es el dispositivo al cual desea conectarse
                showDialog(data);
            }
        }
    };

    /**
     * Metodo encargado de crear un dialogo para confirmar la autorizacion
     * @param datos
     */
    void showDialog(Bundle datos) {
        DialogFragment newAlertDialogFragment = AuthConnectAlertDialogFragment
                .newInstance(datos, wifiDialogCallbacks);
        newAlertDialogFragment.show(getFragmentManager(), "AuthConnectAlertDialogFragment");
    }

    /**
     * Clase encargada de mostrar un diálogo de confirmación
     */
    public static class AuthConnectAlertDialogFragment extends DialogFragment {

        private static Callbacks wifiDialogCallbacksLocal;

        public static AuthConnectAlertDialogFragment newInstance(Bundle datos, Callbacks wifiDialogCallbacksAux) {
            AuthConnectAlertDialogFragment frag = new AuthConnectAlertDialogFragment();
            frag.setArguments(datos);
            wifiDialogCallbacksLocal = wifiDialogCallbacksAux;
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.icon)
                    .setTitle("Conexión WiFi")
                    .setMessage("Esta seguro de conectar con " + getArguments().getString("ssid"))
                    .setPositiveButton("Aceptar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    wifiDialogCallbacksLocal.connect(getArguments().getString("bssid"),
                                            getArguments().getString("ssid"));
                                    dialog.dismiss();
                                    dialog_fragment.dismiss();
                                }
                            })
                    .setNegativeButton("Cancelar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    dialog_fragment.dismiss();
                                }
                            }).create();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

            Log.d(TAG, "-> onViewCreated()");

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "-> onAttach()");

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        wifiDialogCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {

        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        wifiDialogCallbacks = sWifiDialogCallbacks;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        // Make sure we're not doing discovery anymore
        getActivity().unregisterReceiver(wifiReciever);

        super.onDismiss(dialog);
    }

    @Override
    public void onSaveInstanceState(Bundle arg0) {

        super.onSaveInstanceState(arg0);
    }
}
