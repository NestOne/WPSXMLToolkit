package com.supermap.wpsxmltoolkit.writer;

import com.supermap.wpsxmltoolkit.read.WpsXmlReader;
import net.opengis.wps10.impl.ExecuteTypeImpl;
import org.apache.commons.io.FileUtils;
import org.geotools.feature.NameImpl;

import java.io.*;

public class WriteToXMLMain {
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

        String file = "D:/test.xml";
        FileUtils.writeStringToFile(new File(file), output);
    }
}
