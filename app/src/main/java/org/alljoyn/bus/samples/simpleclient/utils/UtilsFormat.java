package org.alljoyn.bus.samples.simpleclient.utils;

/**
 * Created by sgcmovil on 14/01/16.
 */
public class UtilsFormat {
    // Debug
    private static final String TAG = "UtilsFormat";


    public static String formatTransNumber(String codigo){
        if (codigo!=null && codigo.length()>0){
            char[] codigoChar = codigo.toCharArray();
            char[] formatedcodigoChar = new char[codigoChar.length +1];

            int j = 0;
            for (int i = 0; i < codigoChar.length; i++) {
                if (i==4 ){
                    formatedcodigoChar[j] = '-';
                    j++;
                }
                formatedcodigoChar[j] = codigoChar[i];
                j++;
            }
            return new String(formatedcodigoChar);
        }
        return codigo;
    }

    public static String formatTransNumber1(String codigo){
        if (codigo!=null && codigo.length()>0){
            char[] codigoChar = codigo.toCharArray();
            char[] formatedcodigoChar = new char[codigoChar.length +4];

            int j = 0;
            for (int i = 0; i < codigoChar.length; i++) {
                if (i%4==0){
                    formatedcodigoChar[j] = ' ';
                    j++;
                }
                formatedcodigoChar[j] = codigoChar[i];
                j++;
            }
            return new String(formatedcodigoChar);
        }
        return codigo;
    }


}
