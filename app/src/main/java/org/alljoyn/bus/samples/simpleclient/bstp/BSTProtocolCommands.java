package org.alljoyn.bus.samples.simpleclient.bstp;

/**
 * Comandos del protocolo BSTP
 * Created by rcarventepc on 4/09/14.
 */
public class BSTProtocolCommands {

    public static final String connect = "CONN";
    public static final String accept = "ACPT";
    public static final String reject = "REJT";
    public static final String request = "RQST";
    public static final String close = "CLSE";

    public static final String[] comandos = {connect, accept, reject, request, close};

    /**
     * Método que indica si un comando recibido se encuentra en la lista de comandos permitidos
     * @param command
     * @return
     */
    public static boolean isMember(String command) {
        for (String com : comandos)
            if (com.equals(command))
                return true;
        return false;
    }

}
