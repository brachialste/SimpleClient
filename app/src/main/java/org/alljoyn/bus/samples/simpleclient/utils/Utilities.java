package org.alljoyn.bus.samples.simpleclient.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Clase con diferentes utilizades para el manejo de bytes
 * 
 * @author rcarventepc
 * 
 */
public class Utilities {
	
	private static final String TAG = "Utilities";
	
	/**
	 * Convierte un objeto a un arreglo de bytes
	 * 
	 * @param obj
	 *            objeto
	 * @return arreglo de bytes correspondiente al objeto
	 */
	public static byte[] getBytes(Object obj) {
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			bos.close();
			byte[] data = bos.toByteArray();
			return data;
		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
		} finally {
			try {
				oos.close();
			} catch (IOException ex) {
				Log.e(TAG, ex.getMessage());
			}
		}
		return null;
	}

	/**
	 * Convierte un arreglo de bytes a un objeto
	 * 
	 * @param bytes
	 *            arreglo de bytes
	 * @return objeto correspondiente al arreglo de bytes
	 */
	public static Object toObject(byte[] bytes) {
		Object object = null;
		try {
			object = new java.io.ObjectInputStream(
					new java.io.ByteArrayInputStream(bytes)).readObject();
		} catch (java.io.IOException ioe) {
			Log.e(TAG, ioe.getMessage());
		} catch (java.lang.ClassNotFoundException cnfe) {
			Log.e(TAG, cnfe.getMessage());
		}
		return object;
	}

	/**
	 * Método utilizado para sumar dos arreglos de bytes
	 * 
	 * @param a1
	 *            arreglo de bytes
	 * @param a2
	 *            arreglo de bytes
	 * @return suma de los arreglos de bytes
	 */
	public static byte[] appendByteArray(byte[] a1, byte[] a2) {
		byte[] result = new byte[a1.length + a2.length];
		System.arraycopy(a1, 0, result, 0, a1.length);
		System.arraycopy(a2, 0, result, a1.length, a2.length);
		return result;
	}

	/**
	 * Método que convierte de un arreglo de bytes a un entero
	 * 
	 * @param b
	 * @return
	 */
	public static int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
				| (b[0] & 0xFF) << 24;
	}

	/**
	 * Método que convierte de un entero a un arreglo de bytes
	 * 
	 * @param a
	 * @return
	 */
	public static byte[] intToByteArray(int a) {
		return new byte[] { (byte) ((a >> 24) & 0xFF),
				(byte) ((a >> 16) & 0xFF), (byte) ((a >> 8) & 0xFF),
				(byte) (a & 0xFF) };
	}

	/**
	 * Método que copia un stream de entrada a un stream de salida
	 * 
	 * @param is
	 * @param os
	 */
	public static void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	/**
	 * Método encargado de extraer el nombre de la aplicación Web a partir de
	 * una URL.
	 * 
	 * @param url
	 *            URL con formato http://webapp.com.mx/etc
	 * @return
	 */
	public static String getWebAppName(String url) {
		String domain = null;
		if (url != null && !url.isEmpty()) {
			// obtenemos el índice del separador
			int separator = url.indexOf("//") + 2;
			// obtenemos el dominio sin ningun otro valor
			domain = url.substring(separator, url.indexOf('.', separator));
			// convertimos el valor a mayusculas y le quitamos los espacios
			domain = domain.toUpperCase(Locale.getDefault()).trim();
		}
		return domain;
	}
	
	/**
	 * Método encargado de concatenar la información del Web Service al dominio de la URL
	 * @param url
	 * @return
	 */
	public static String addWebServiceInfo(String url){
        //return url + "/monederotest/ws/1094/v2/soap.php";     //TODO: TEST
		return url + "/ws/1094/v2/soap.php";
	}
	
	/**
	 * Método que obtiene el MD5 de una función dada
	 * 
	 * @param pass
	 * @return
	 */
	public static String Md5Hash(String pass) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			byte[] data = pass.getBytes("UTF-8");
			m.update(data, 0, data.length);
			BigInteger i = new BigInteger(1, m.digest());
			String hash = i.toString(16);
			hash = UtilsRi505.padLeft(hash, 32).replace(' ', '0');
			return hash;
		} catch (NoSuchAlgorithmException e1) {
			Log.e(TAG, e1.getMessage());
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		}
		return pass;
	}

    /**
     * Método encargado de obtener el nivel de la bateria actual
     * @return
     */
    public static float getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f;
    }


    /**
     * Método encargado de asignar el idioma de la aplicación
     * @param context
     */
    public static void checkLanguage(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        // seteamos el idioma seleccionado
        if (prefs.getString("language", "1").equals("2")) {
            Log.d(TAG, "English");
            Locale appLoc = new Locale("en");
            Locale.setDefault(appLoc);
            Configuration appConfig = new Configuration();
            appConfig.locale = appLoc;
            context.getResources().updateConfiguration(appConfig,
                    context.getResources().getDisplayMetrics());
        }
    }

    /**
     * Método encargado de hacer XOR de una cadena con una llave
     * @param clearData
     * @param key
     * @return
     */
    public static String stringXOR(String clearData, char key){
        StringBuilder encoded = new StringBuilder();
        char temp;
        for(int i=0; i<clearData.length(); i++){
            temp = (char)(clearData.charAt(i) ^ key);
            encoded.append(temp);
        }
        return encoded.toString();
    }
}
