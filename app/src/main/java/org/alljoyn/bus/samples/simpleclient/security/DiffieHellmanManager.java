package org.alljoyn.bus.samples.simpleclient.security;

import android.util.Log;

import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;

/**
*       // generar llaves
     DiffieHellmanManager dh1 = new DiffieHellmanManager();
     byte[] pk1 = dh1.generatePublicKey();
     DiffieHellmanManager dh2 = new DiffieHellmanManager();
     byte[] pk2 = dh2.generatePublicKey();

     // intercambio de llaves
     dh1.generateSecretKey(pk2);
     dh2.generateSecretKey(pk1);

     try {

     String clear1 = "Hola 2";
     Log.d(TAG, "clear1 = " + clear1);
     String cypher1 = dh1.encryptBTData(clear1);
     Log.d(TAG, "cypher1 = " + cypher1);

     String descypher1 = dh2.decryptBTData(cypher1);
     Log.d(TAG, "descypher1 = " + descypher1);

     String clear2 = "Hola 1";
     Log.d(TAG, "clear2 = " + clear2);
     String cypher2 = dh2.encryptBTData(clear2);
     Log.d(TAG, "cypher2 = " + cypher2);

     String descypher2 = dh1.decryptBTData(cypher2);
     Log.d(TAG, "descypher2 = " + descypher2);

     } catch (Exception e) {
     e.printStackTrace();
     }
 *
 * Created by sgcweb on 3/09/14.
 */
public class DiffieHellmanManager {

    public static String TAG = "DiffieHellmanManager";

    // llave secreta
    private SecretKey DHSecretKey;

    // referencia a la instancia actual del RegisterManager
    private static DiffieHellmanManager dh_man;

    // valores de p y g
    private static BigInteger pDH = new BigInteger("150879384131882827232891861270691459811627502202705785478255965874615631753724419176772861963928645387092779809797128233793576706063216556844864749624535138899030719447156952061915457941436583628538256408000305676562762848192450277730153137788065936581681595643052465713108317519387605864583387527286039641179");
    private static BigInteger gDH = new BigInteger("119452215646183611487599688422073547594674389013455669698276831515679292151009409184010980478905717259784301125204061272729335614342688842246511246971655624154928823079787757115612117964489215036031365964151802667676586137089223319047541653850658437351305772507902909536417222276922291722521384356047372532626");

    // valores del algoritmo
    private KeyPairGenerator keyGen;
    private KeyAgreement keyAgree;

    // variable que indica que esta lista para cifrar
    public boolean isReady = false;

    /**
     * Método encargado de crear una nueva instancia de DiffieHellmanManager
     * @return
     */
    public static synchronized DiffieHellmanManager createNewInstance() {
        dh_man = new DiffieHellmanManager();
        return dh_man;
    }

    /**
     * Método encargado de obtener la instancia actual del DiffieHellmanManager
     * @return
     */
    public static synchronized DiffieHellmanManager getInstance() {
        return dh_man;
    }

    /**
     * Constructor de la clase
     */
    public DiffieHellmanManager(){
        try {
            // Use the values to generate a key pair
            DHParameterSpec dhParams = new DHParameterSpec(pDH, gDH);
            keyGen = KeyPairGenerator.getInstance("DH", "BC");

            keyGen.initialize(dhParams, new SecureRandom());

            isReady = false;
        } catch (java.security.InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public void test() throws Exception {

        DHParameterSpec dhParams = new DHParameterSpec(pDH, gDH);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "BC");

        keyGen.initialize(dhParams, new SecureRandom());

        KeyAgreement aKeyAgree = KeyAgreement.getInstance("DH", "BC");
        KeyPair aPair = keyGen.generateKeyPair();
        aKeyAgree.init(aPair.getPrivate());

        KeyAgreement bKeyAgree = KeyAgreement.getInstance("DH", "BC");
        KeyPair bPair = keyGen.generateKeyPair();
        bKeyAgree.init(bPair.getPrivate());

        aKeyAgree.doPhase(bPair.getPublic(), true);
        bKeyAgree.doPhase(aPair.getPublic(), true);

        MessageDigest hash = MessageDigest.getInstance("SHA1", "BC");
        Log.i(TAG, new String(hash.digest(aKeyAgree.generateSecret())));
        Log.i(TAG, new String(hash.digest(bKeyAgree.generateSecret())));

    }

    // Returns a comma-separated string of 3 values.
    // The first number is the prime modulus P.
    // The second number is the base generator G.
    // The third number is bit size of the random exponent L.
    private String genDhParams() {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            // Create the parameter generator for a 1024-bit DH key pair
            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH", "BC");
            paramGen.init(1024, new SecureRandom());

            // Generate the parameters
            AlgorithmParameters params = paramGen.generateParameters();
            DHParameterSpec dhSpec = (DHParameterSpec) params.getParameterSpec(DHParameterSpec.class);

            /*BigInteger p = dhSpec.getP();
            BigInteger g = dhSpec.getG();

            Log.i(TAG, "P: " + p);
            Log.i(TAG, "G: " + g);*/

            // Return the three values in a string
            return ""+dhSpec.getP()+","+dhSpec.getG();

            /*String valuesInStr = genDhParams();
            // Retrieve the prime, base, and private value for generating the key pair.
            // If the values are encoded as in
            // Generating a Parameter Set for the Diffie-Hellman Key Agreement Algorithm,
            // the following code will extract the values.
            String[] values = valuesInStr.split(",");
            BigInteger p = new BigInteger(values[0]);
            BigInteger g = new BigInteger(values[1]);*/
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Método encargado de generar la llave pública del origen
     * @return
     */
    public String generatePublicKey(){
        byte[] publicKeyBytes = null;

        try {
            keyAgree = KeyAgreement.getInstance("DH", "BC");
            // generamos el par de llaves
            KeyPair keypair = keyGen.generateKeyPair();

            // inicamos el key Agree
            keyAgree.init(keypair.getPrivate());

            // Send the public key bytes to the other party...
            publicKeyBytes = keypair.getPublic().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }finally {
            return android.util.Base64.encodeToString(publicKeyBytes,
                    android.util.Base64.DEFAULT);
        }
    }


    /**
     * Método encargado de generar la llave secreta a partir de la llave pública de la otra entidad
     * @param publicKeyStr
     * @return
     */
    public boolean generateSecretKey(String publicKeyStr){
        boolean isSecretKeyGenerated = false;
        try {
            byte[] publicKeyBytes = android.util.Base64.decode(publicKeyStr,
                    android.util.Base64.DEFAULT);

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("DH", "BC");
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

            keyAgree.doPhase(pubKey, true);

            // Generate the secret key
            DHSecretKey = keyAgree.generateSecret("Blowfish");

            // Use the secret key to encrypt/decrypt data;
            isSecretKeyGenerated = true;

            isReady = isSecretKeyGenerated;
        } catch (java.security.InvalidKeyException e) {
            e.printStackTrace();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            return isSecretKeyGenerated;
        }
    }

    /**
     * Método encargado de cifrar un texto en claro
     * @param cleartext
     * @return
     * @throws Exception
     */
    public String encryptBTData(String cleartext) throws Exception {
        byte[] result = encrypt(cleartext.getBytes());
        return android.util.Base64.encodeToString(result, android.util.Base64.DEFAULT);
    }

    /**
     * Método encargado de descifrar un texto cifrado
     * @param encrypted
     * @return
     * @throws Exception
     */
    public String decryptBTData(String encrypted) throws Exception {
        byte[] enc = android.util.Base64.decode(encrypted, android.util.Base64.DEFAULT);
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
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, DHSecretKey);
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
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, DHSecretKey);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

}
