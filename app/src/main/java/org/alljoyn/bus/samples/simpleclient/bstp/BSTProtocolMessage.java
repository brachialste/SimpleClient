package org.alljoyn.bus.samples.simpleclient.bstp;

import android.util.Log;


import org.alljoyn.bus.samples.simpleclient.security.BlowfishManager;
import org.alljoyn.bus.samples.simpleclient.security.DiffieHellmanManager;
import org.alljoyn.bus.samples.simpleclient.utils.Utilities;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by rcarventepc on 4/09/14.
 */
public class BSTProtocolMessage {

    // variables del protocolo
    public static final char SOF = (char) 0x0e;
    public static final char ENCRYPTED = (char) 0x1e;
    public static final char OBFUSCATED = (char) 0x1f;
    public static final char EOF = (char) 0x0f;
    // Debug
    private static final String TAG = "BSTProtocolMessage";
    // Contiene los datos de la trama.
    private byte[] tramaByte;
    // Comando que se envia.
    private String cmd;
    // Bloque de datos de la trama.
    private JSONObject data;
    // Manejador del cifrado y descifrado
    private DiffieHellmanManager diffieHellmanManager;
    // Manejador del ofuscado Blowfish iRED
    private BlowfishManager blowfishManager;

    /**
     * Formato de la trama para enviar al dispositivo indicado.
     *
     * @param cmd
     * @param data
     */
    public BSTProtocolMessage(String cmd, JSONObject data) {
        diffieHellmanManager = DiffieHellmanManager.getInstance();
        blowfishManager = BlowfishManager.getInstance();
        this.cmd = cmd;
        this.data = data;
        // construimos el mensaje a partir de los datos
        this.tramaByte = buildMessageSecureTransmissionProtocol();
    }

    /**
     * Reconstruye la información de la trama.
     *
     * @param tramaByte
     */
    public BSTProtocolMessage(byte[] tramaByte) {
        diffieHellmanManager = DiffieHellmanManager.getInstance();
        blowfishManager = BlowfishManager.getInstance();
        this.tramaByte = tramaByte;
        // procesamos el mensaje recibido
        getMessageSecureTransmissionProtocol();
    }

    public byte[] getTramaByte() {
        return tramaByte;
    }

    public String getCmd() {
        return cmd;
    }

    public JSONObject getData() {
        return data;
    }

    /**
     * Método encargado de construir la trama para comunicación con el
     * dispositivo
     *
     * @return
     */
    public byte[] buildMessageSecureTransmissionProtocol() {

        Log.d(TAG, "-> buildMessageSecureTransmissionProtocol()");

        String strDatos;
        byte[] trama = null;

        // Datos
        strDatos = new String(cmd);    // COMMAND
        strDatos += data.toString(); // DATA

        Log.d(TAG, "strDatos [en claro] = " + strDatos);


        // ciframos / ofuscamos las información
        String strCiphData = "";
        try {
            if (!cmd.equals(BSTProtocolCommands.connect)) {
                /** CIFRADO **/
                if (diffieHellmanManager != null && diffieHellmanManager.isReady) {
                    strCiphData = diffieHellmanManager.encryptBTData(strDatos);

                    Log.d(TAG, "strDatos [cifrados] = " + strCiphData);

                    // Se construye el mensaje
                    trama = BSTProtocolUtils.stringToByte(Character.toString(SOF));
                    trama = Utilities.appendByteArray(trama,
                            BSTProtocolUtils.stringToByte(Character.toString(ENCRYPTED) + strCiphData));
                    trama = Utilities.appendByteArray(trama,
                            BSTProtocolUtils.stringToByte(Character.toString(EOF)));
                } else {

                    Log.e(TAG, "imposible cifrar, el manejador no esta listo");

                }
            } else {
                /** OFUSCADO **/
                if (blowfishManager != null) {
                    strCiphData = blowfishManager.encryptBF(strDatos);

                    Log.d(TAG, "strDatos [ofuscados] = " + strCiphData);

                    // Se construye el mensaje
                    trama = BSTProtocolUtils.stringToByte(Character.toString(SOF));
                    trama = Utilities.appendByteArray(trama,
                            BSTProtocolUtils.stringToByte(Character.toString(OBFUSCATED) + strCiphData));
                    trama = Utilities.appendByteArray(trama,
                            BSTProtocolUtils.stringToByte(Character.toString(EOF)));
                } else {

                    Log.e(TAG, "imposible ofuscar, el manejador no esta listo");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (trama != null)
            Log.d(TAG, "Mensaje = " + new String(trama));

        return trama;
    }


    /**
     * Separa por bloques la trama recibida.
     */
    public void getMessageSecureTransmissionProtocol() {

        Log.d(TAG, "-> getMessageSecureTransmissionProtocol()");

        String comando;
        String datos;
        String datos_cifrados;
        String respuesta;

        try {
            respuesta = new String(tramaByte);

            Log.d(TAG, "respuesta = " + respuesta);


            // obtenemos los datos cifrados
            datos_cifrados = new String(Arrays.copyOfRange(tramaByte, 1, tramaByte.length - 1));

            Log.d(TAG, "datos_cifrados = " + datos_cifrados);


            if (datos_cifrados != null && !datos_cifrados.isEmpty()) {
                String datos_descifrados = "";
                // obtenemos el primer caracter para ssaber si esta cifrado u ofuscado
                if (datos_cifrados.charAt(0) == ENCRYPTED) {
                    /** DESCIFRADO **/
                    if (diffieHellmanManager != null && diffieHellmanManager.isReady) {
                        datos_descifrados = diffieHellmanManager.decryptBTData(datos_cifrados.substring(1));

                        Log.d(TAG, "datos_descifrados = " + datos_descifrados);

                    } else {

                        Log.e(TAG, "imposible descifrar, el manejador no esta listo");

                    }
                } else if (datos_cifrados.charAt(0) == OBFUSCATED) {
                    /** DESOFUSCADO **/
                    if (blowfishManager != null) {
                        datos_descifrados = blowfishManager.decryptBF(datos_cifrados.substring(1));

                        Log.d(TAG, "datos_descifrados = " + datos_descifrados);

                    } else {

                        Log.e(TAG, "imposible desofuscar, el manejador no esta listo");

                    }
                } else {

                    Log.e(TAG, "Trama recibida incorrecta");

                    return;
                }

                // procesamos los datos descifrados
                if (datos_descifrados != null && !datos_descifrados.isEmpty()) {
                    // obtenemos el comando
                    comando = datos_descifrados.substring(0, 4);

                    Log.i(TAG, "comando = " + comando);

                    if (BSTProtocolCommands.isMember(comando)) {
                        cmd = comando;
                    } else {

                        Log.e(TAG, "Trama recibida incorrecta");

                        return;
                    }

                    // obtenemos los datos
                    datos = datos_descifrados.substring(4, datos_descifrados.length());

                    Log.i(TAG, "datos = " + datos);

                    if (datos != null) {
                        if (!datos.isEmpty()) {
                            data = new JSONObject(datos);
                        } else {
                            data = new JSONObject();
                        }
                    } else {

                        Log.e(TAG, "Trama recibida incorrecta");

                        return;
                    }
                } else {

                    Log.e(TAG, "Trama recibida incorrecta");

                    return;
                }
            } else {

                Log.e(TAG, "Trama recibida incorrecta");

                return;
            }
        } catch (NumberFormatException e) {

            Log.e(TAG, "Trama recibida incorrecta - NumberFormatException");

        } catch (ArrayIndexOutOfBoundsException e) {

            Log.e(TAG, "Trama recibida incorrecta - ArrayIndexOutOfBoundsException");

        } catch (Exception e) {

            Log.e(TAG, "Trama recibida incorrecta - Exception");

        }

    }

}
