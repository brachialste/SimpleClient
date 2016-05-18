package org.alljoyn.bus.samples.simpleclient.security;


import android.util.Base64;


import org.alljoyn.bus.samples.simpleclient.utils.Utilities;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by sgcweb on 29/08/14.
 */
public class BlowfishManager {

    public static String TAG = "BlowfishManager";

    private String dato = null;

    // referencia a la instancia actual del RegisterManager
    private static BlowfishManager bf_man;

    /**
     * Método encargado de obtener la instancia actual del BlowfishManager o una nueva
     * @return
     */
    public static synchronized BlowfishManager getInstance() {
        if (bf_man == null) {
            bf_man = new BlowfishManager();
        }
        return bf_man;
    }

    /**
     * Constructor de la clase
     */
    public BlowfishManager(){
        dato = Utilities.Md5Hash("b8736962d6e610af"); //TODO: SEED
    }

    /**
     * Método encargado de cifrar un texto en claro
     * @param cleartext
     * @return
     * @throws Exception
     */
    public String encryptBF(String cleartext) throws Exception {
        byte[] result = encrypt(cleartext.getBytes());
        return Base64.encodeToString(result, Base64.DEFAULT);
    }

    /**
     * Método encargado de descifrar un texto cifrado
     * @param encrypted
     * @return
     * @throws Exception
     */
    public String decryptBF(String encrypted) throws Exception {
        byte[] enc = Base64.decode(encrypted, Base64.DEFAULT);
        byte[] result = decrypt(enc);
        return new String(result);
    }

    /**
     * Método para cifrar contenido en claro
     * @param clear
     * @return
     * @throws Exception
     */
    private byte[] encrypt(byte[] clear) throws Exception {
        byte[] key_byte = dato.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(key_byte, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    /**
     * Método para descifrar un contenido cifrado
     * @param encrypted
     * @return
     * @throws Exception
     */
    private byte[] decrypt(byte[] encrypted) throws Exception {
        byte[] key_byte = dato.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(key_byte, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
}
