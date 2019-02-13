package com.supermap.WPSXMLToolkit;

import com.supermap.WPSXMLToolkit.process.GlobalProcessors;
import org.geotools.process.ProcessFactory;
import org.opengis.feature.type.Name;

import java.util.Set;

public class ListProcessesMain {
    public static void main(String[] args) {
        Set<ProcessFactory> pfs = GlobalProcessors.getProcessFactories();
        for (ProcessFactory pf : pfs) {

            for (Name name : pf.getNames()) {
                System.out.println("===================================");
                System.out.print("Process name:");
                System.out.println(name.toString());
                System.out.print("Version:");
                System.out.println(pf.getVersion(name));
                System.out.print("Title:");
                System.out.println(pf.getTitle(name));
                System.out.print("Description:");
                System.out.println(pf.getDescription(name));
                System.out.println();
            }
        }
    }
}
