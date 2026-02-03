package fr.iutfbleau.sae32_2025;


import fr.iutfbleau.sae32_2025.controllers.Converter;
import fr.iutfbleau.sae32_2025.controllers.Decoder;
import fr.iutfbleau.sae32_2025.views.AppTheme;
import fr.iutfbleau.sae32_2025.views.HomeFrame;

/**
 * Main entry point for the PIF application.
 *
 * @version 1.1
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class Main {

    public static void main(String[] args) {
        AppTheme.setupLookAndFeel();

        // CASE 1: No args -> Launch Graphical Menu (GUI)
        if (args.length == 0) {
            HomeFrame home = new HomeFrame();
            home.setVisible(true);
        } 
        // CASE 2: Args present -> CLI Mode
        else {
            String firstArg = args[0].toLowerCase();
            
            if (firstArg.endsWith(".pif")) {
                System.out.println("[Main] PIF file detected -> Launching Decoder...");
                Decoder.main(args);
            } else {
                System.out.println("[Main] Image file detected -> Launching Converter...");
                Converter.main(args);
            }
        }
    }
}