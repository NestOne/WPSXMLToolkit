package com.supermap.WPSXMLToolkit.xml;

import com.supermap.WPSXMLToolkit.read.WpsXmlReader;
import net.opengis.wps10.impl.ExecuteTypeImpl;
import org.geotools.feature.NameImpl;
import org.geotools.wps.WPSConfiguration;

import java.io.*;

public class ExecuteToXMLMain {
    public static void main(String[] args) throws Exception {

        ExecuteRequest execRequest = new ExecuteRequest();
        String processName = "geo:getX";
        execRequest.processName = processName;
        NameImpl name = new NameImpl("geo", ":","getX");
        InputParameterValues ipv = new InputParameterValues(name, "geom");
        ipv.values.add(new InputParameterValues.ParameterValue(InputParameterValues.ParameterType.TEXT,
                "application/wkt", "MULTIPOINT (1 1, 5 4, 7 9, 5 5, 2 2)"));
        execRequest.inputs.add(ipv);

//        ipv = new InputParameterValues(name, "distance");
//        ipv.values.add(new InputParameterValues.ParameterValue(InputParameterValues.ParameterType.LITERAL,
//                null, "2"));
//        execRequest.inputs.add(ipv);
//
//        ipv = new InputParameterValues(name, "quadrantSegments");
//        ipv.values.add(new InputParameterValues.ParameterValue(InputParameterValues.ParameterType.LITERAL,
//                null, null));
//        execRequest.inputs.add(ipv);
//
//        ipv = new InputParameterValues(name, "capStyle");
//        ipv.values.add(new InputParameterValues.ParameterValue(InputParameterValues.ParameterType.LITERAL,
//                null, null));
//        execRequest.inputs.add(ipv);


        OutputParameter op = new OutputParameter(name, "result");
        op.mimeType = null;
        execRequest.outputs.add(op);

        WPSXMLWriter writer = new WPSXMLWriter();
        String output = writer.write(execRequest);
        System.out.println(output);

        InputStream is = new ByteArrayInputStream( output.getBytes( "UTF-8" ) );
        InputStreamReader reader = new InputStreamReader(is);

        WpsXmlReader wpsXmlReader = new WpsXmlReader("Execute", "1.0", new WPSConfiguration());
        Object result = wpsXmlReader.read(null, reader, null);
        net.opengis.wps10.ExecuteType execute= (ExecuteTypeImpl)result;

        System.out.print("XML read sucess, process name: ");
        System.out.println(execute.getIdentifier().getValue());
    }
}
