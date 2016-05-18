package org.alljoyn.bus.samples.simpleclient;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by imacmovil on 11/05/16.
 */
public class NetworkAdapter extends ArrayAdapter<NetworkView> {
    public static final String BSSID_NETWORK = "bssid";
    public static final String SSID_NETWORK = "ssid";

    /**
     * Constructor de la clase
     * @param context contexto de la aplicación
     * @param devices  listado de ubicaciones
     */
    public NetworkAdapter(Context context, List<NetworkView> devices) {
        super(context, R.layout.row_network, R.id.bssidDevice, devices);
    }

    /**
     * Método encargado de obtener la vista del componente personalizado de servicios
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        if (v != convertView && v != null) {
            NetworkViewHolder holder = new NetworkViewHolder();

            TextView bssidTxt = (TextView) v.findViewById(R.id.bssidDevice);  // bssid de la red
            TextView ssidTxt = (TextView) v.findViewById(R.id.ssidDevice); // ssid de la red

            holder.bssidHolder = bssidTxt;
            holder.ssidHolder = ssidTxt;

            v.setTag(holder);
        }

        NetworkViewHolder holder = (NetworkViewHolder) v.getTag();
        String bssid = getItem(position).bssid;
        String ssid = getItem(position).ssid;

        holder.bssidHolder.setText(bssid);
        holder.ssidHolder.setText(ssid);

        return v;
    }

    /**
     * Clase encargada de representar la vista del contenedor
     */
    private class NetworkViewHolder {
        public TextView bssidHolder;
        public TextView ssidHolder;
    }
}
