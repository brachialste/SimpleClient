package org.alljoyn.bus.samples.simpleclient.bstp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by sgcweb on 4/09/14.
 */
public class BSTProtocolUtils {

    // Debug
    private static final String TAG = "UtilsRi505";

    /**
     * Limpia un archivo existente con datos
     *
     * @param path
     * @return
     */
    public static boolean clearFile(String path) {
        try {
            File file = new File(path);
            // si el archivo no existe, se crea
            if (!file.exists()) {
                file.createNewFile();
            }
            // se escribe vacio el archivo
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(new String());
            bw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convierte un arreglo de bytes a su representación en cadena.
     *
     * @param data
     *            dato tipo byte[] que sera convertido a string
     * @return dato byte[] convertido en String
     */
    public static String byteToString(byte[] data) {
        try {
            char[] cData = new char[data.length];

            for (int i = 0; i < cData.length; i++) {
                cData[i] = (char) data[i];
            }

            return new String(cData);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Obtiene los datos binarios de un hexadecimal
     *
     * @param strHex
     * @return
     */
    public static String hexToBin(String strHex) {
        try {
            return new BigInteger(strHex, 16).toString(2);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Convierte un valor entero a su representación hexadecimal en un arreglo
     * de bytes
     *
     * @param valor
     *            dato tipo entero que sera convertido a hexadecimal
     * @param size
     *            tamaño del dato
     * @return
     */
    public static byte[] intToHex(int valor, int size) {
        try {
            byte[] hex = initArray(size);
            int pos = size - 1;
            do {
                if ((valor % 16) < 10) {
                    hex[pos--] = (byte) ((valor % 16) + 48);
                } else
                    hex[pos--] = (byte) ((valor % 16) + 55);
            } while ((valor /= 16) != 0 && pos >= 0);
            return hex;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] initArray(int size) {
        byte[] array = new byte[size];
        for (int i = 0; i < size; i++) {
            array[i] = (byte) '0';
        }
        return array;
    }

    /**
     * Convierte un valor byte hexadecimal a entero
     *
     * @param data
     *            arrreglo de bytes hexadecimal que se convertira a entero
     * @return el byte hexadecimal
     */
    public static int byteHexToInt(byte[] data) {
        try {
            return new BigInteger(new String(data), 16).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Obtiene un subarreglo de bytes contenido en un arreglo de bytes
     *
     * @param array
     *            arreglo de bytes
     * @param index
     *            la posicion donde comienza
     * @param count
     *            la posicion donde termina
     * @return arreglo de byte con los arreglos a y b concatenados
     */
    public static byte[] subArray(byte[] array, int index, int count) {
        try {
            byte[] sub = new byte[count];

            for (int i = index, j = 0; j < count; j++, i++) {
                sub[j] = array[i];
            }

            return sub;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convierte una cadena a su representación en un arreglo de bytes
     *
     * @param str
     *            Cadena que sera convertida en byte[]
     * @return La cadena convertida en byte[]
     */
    public static byte[] stringToByte(String str) {
        try {
            byte[] byteArray = new byte[str.length()];

            for (int i = 0; i < str.length(); i++) {
                byteArray[i] = (byte) str.charAt(i);
            }
            return byteArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Valida si el dato es un digito
     *
     * @param dat
     * @return
     */
    public static boolean isDigit(String dat) {
        try {
            int cont = 0;
            for (int i = 0; i < dat.length(); i++) {
                if (Character.isDigit(dat.charAt(i))) {
                    cont++;
                }
            }
            if (cont == dat.length() - 1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Método utilizado para el padding a la derecha
     *
     * @param s
     * @param n
     * @return
     */
    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    /**
     * Método utilizado para el padding a la izquierda
     *
     * @param s
     * @param n
     * @return
     */
    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s);
    }


    /**
     * Calcula un crc de 16 bits.
     *
     * @param liCRC
     * @param lbData
     * @return
     */
    private static int crc16bits(int liCRC, byte lbData) {
        int lbIndex;
        liCRC ^= (int) ((int) lbData << 8);
        lbIndex = 8;
        do {
            if ((liCRC & 0x8000) != 0) {
                liCRC = (int) ((liCRC << 1) ^ 0x1021);
            } else {
                liCRC <<= 1;
            }
        } while (--lbIndex > 0);
        return liCRC;
    }

    /**
     * Método encargado de separar los valores de un byte en la parte mas significativa y la menos significativa y regresando su
     * representación en un arreglo de bytes
     * @param dato
     * @return
     */
    public static byte[] separaDato(byte dato){
        byte[] res = stringToByte(padLeft(Integer.toString((byte)(dato & 0xFF), 16).toUpperCase(), 2).replace(' ', '0'));
        return res;
    }
}
