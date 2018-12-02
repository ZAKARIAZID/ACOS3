/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package classicapplet1;

import javacard.framework.*;

/**
 *
 * @author ZIDOUH
 */
public class ACOSimul extends Applet {

    /**
     * Installs this applet.
     * 
     * @param bArray
     *            the array containing installation parameters
     * @param bOffset
     *            the starting offset in bArray
     * @param bLength
     *            the length in bytes of the parameter data in bArray
     */
    //*************************************//
    
    final static byte[] Files_Sys = {(byte) 0xFF, (byte) 0x02, (byte) 0xFF, (byte) 0x03, (byte) 0xFF, (byte) 0x04 };
    public byte[] User_Fils = new byte[60];
    public byte[] User_Data = new byte[255];
    private final byte[] FF02 = new byte[1]; // Le N_OF_FILE
    private final byte[] FF03 = new byte[2]; // Le PIN et le IC
    private final byte[] FF04 = new byte[6]; 
    
    //*************************************//
    
    byte[] id_new = new byte[2];
    byte rows_new = 0x00;
    byte len_new = 0x00;
    byte nb_new = 0x00;
    
    //*************************************//
    
    private boolean upd2 = false;
    private boolean upd4 = false;
    int myindex = 00;
    int offset_of_user_fils = 0;
    int inf = 0;
    int offset_data_user = 0;
    private final static byte CLA = (byte) 0x80;
    private final static byte SELECT_FILE = (byte) 0xA4;
    private final static byte WRITE_FILE = (byte) 0xB0;
    private final static byte READ_FILE = (byte) 0xD0;
    
    //*************************************//
    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new ACOSimul();
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected ACOSimul() {
        register();
    }

    /**
     * Processes an incoming APDU.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU
     */
    public void process(APDU apdu) throws ISOException {
        //Insert your code here
        if (selectingApplet()) {
            return;
        }
        byte[] buff = apdu.getBuffer();
        if (buff[ISO7816.OFFSET_CLA] != CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
        switch (buff[ISO7816.OFFSET_INS])
        {
            case SELECT_FILE :
                if (buff[ISO7816.OFFSET_CDATA] == Files_Sys[0] && buff[ISO7816.OFFSET_CDATA + 1] == Files_Sys[1]){
                    myindex = 02;
                    break;
                }
                if (buff[ISO7816.OFFSET_CDATA] == Files_Sys[2] && buff[ISO7816.OFFSET_CDATA + 1] == Files_Sys[3]){
                    myindex = 03;
                    break;
                }
                if (buff[ISO7816.OFFSET_CDATA] == Files_Sys[4] && buff[ISO7816.OFFSET_CDATA + 1] == Files_Sys[5]){
                    myindex = 04;
                    break;
                }
                for(byte i = 0; i<59; i++){
                  if (buff[ISO7816.OFFSET_CDATA] == User_Fils[i] && buff[ISO7816.OFFSET_CDATA + 1] == User_Fils[i+1]){
                    myindex = 04;
                    break;
                }
                  else {
                    ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                    
                }
                }
                
                
                
                break;
            case WRITE_FILE:
                switch (myindex) {
                    case 02 :
                        byte len = buff[ISO7816.OFFSET_LC];
                        for (byte i = 0; i < len; i++) {
                            FF02[i] = buff[ISO7816.OFFSET_CDATA + i];
                        }
                        nb_new = FF02[0];
                        upd2 = true;
                        break;
                    case 03 : 
                        len = buff[ISO7816.OFFSET_LC];
                        for (byte i = 0; i < len; i++) {
                            FF03[i] = buff[ISO7816.OFFSET_CDATA + i];
                        }
                        break;
                    case 04 : 
                        len = buff[ISO7816.OFFSET_LC];
                        for (byte i = 0; i < len; i++) {
                            FF04[i] = buff[ISO7816.OFFSET_CDATA + i];
                        }
                        rows_new = FF04[0];
                        len_new = FF04[1];
                        id_new[0] = FF04[4];
                        id_new[1] = FF04[5];
                        upd4 = true;
                        break;    
                    default :
                        if (myindex == offset_of_user_fils){
                            len = buff[ISO7816.OFFSET_LC];                          
                            for (byte i = 0; i < len; i++) {
                            User_Data[(byte)myindex+i] = buff[ISO7816.OFFSET_CDATA + i];
                        }
                        }
                }
                if (nb_new !=0x00 && rows_new!=0x00 && len_new!=0x00 && id_new[0]!=0x00 && id_new[0]!=0x00 && upd2 && upd4 ){
                    User_Fils[(byte)offset_of_user_fils] = id_new[0];
                    User_Fils[(byte)offset_of_user_fils+1] = id_new[1];
                    offset_of_user_fils = offset_of_user_fils + 2; 
                    offset_data_user += rows_new + len_new;
                }
                break;
            
            case READ_FILE : 
                if (myindex != 0) // and pinCOde submit
                {
                       	byte offset = buff[ISO7816.OFFSET_P1];
                        byte len = buff[ISO7816.OFFSET_P2];
                        if (myindex == 02){
                         for (byte i = 0; i < len; i++) {
                            buff[i] = FF02[offset + i];
                            }   
                         
                        }
                         if (myindex == 03){
                         for (byte i = 0; i < len; i++) {
                            buff[i] = FF03[offset + i];
                            }   
                        }
                         if (myindex == 04){
                         for (byte i = 0; i < len; i++) {
                            buff[i] = FF04[offset + i];
                            }   
                         
                        }
                         
                        apdu.setOutgoingAndSend((byte) 0, len);} // -->> OK
                	 
              	else {
                    ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
                }
                
        }
    }
}
