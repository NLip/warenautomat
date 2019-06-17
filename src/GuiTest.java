//==============================================================================
// Project   : Master of Advanced Studies in Software-Engineering 2019
// Modul     : Projektarbeit OO Softwareentwicklung "Warenautomat"
//             Teil: Design&Implementation
// Title     : GUI-Test-Applikation
// Author    : Thomas Letsch
// Tab-Width : 2
/*///===========================================================================
* Description: Demo-Programm zur Anwendung des GUI's des Warenautomaten. 
$Revision    : 1.12 $  $Date: 2019/06/11 14:17:15 $ 
/*///===========================================================================
//       1         2         3         4         5         6         7         8
//345678901234567890123456789012345678901234567890123456789012345678901234567890
//==============================================================================

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import warenautomat.*;

public class GuiTest {
  
  static final DateTimeFormatter FORMATTER = 
      DateTimeFormatter.ofPattern("dd.MM.yyyy");
  
  static public void main(String pArgs[]) throws ParseException {

    Automat automat = new Automat();
    Kasse kasse = automat.gibKasse();
    
    SystemSoftware.erzeugeGUI(automat);
    
    automat.neueWareVonBarcodeLeser(1, "Mars", 2.00, 
                                    LocalDate.parse("01.01.2009", FORMATTER));
    
    for(int i = 0; i < 16; i++) {
      String day = String.format("%02d", i+1);
      automat.neueWareVonBarcodeLeser(1, "Mars"+(i+1), i+1, 
                                      LocalDate.parse(day+".01.2100", FORMATTER));
      automat.drehen();
    }
    
  }

}


