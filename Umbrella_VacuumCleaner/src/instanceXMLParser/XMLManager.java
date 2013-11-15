package instanceXMLParser;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author davide
 */
public class XMLManager {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
      Instance i = new Instance();
        try {
            i.buildINstanceJDom("instance.xml");
            i.toString();
        } catch (Exception ex) {
            Logger.getLogger(XMLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
