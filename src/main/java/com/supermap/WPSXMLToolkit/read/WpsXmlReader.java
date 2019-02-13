/* (c) 2014 - 2015 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package com.supermap.WPSXMLToolkit.read;

import com.supermap.WPSXMLToolkit.WPSException;

import org.geotools.util.Version;
import org.geotools.util.logging.Logging;
import org.geotools.wps.WPSConfiguration;
import org.geotools.xml.Parser;
import org.geotools.xml.PreventLocalEntityResolver;

import javax.xml.namespace.QName;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * WPS XML parser
 *
 * @author Lucas Reed, Refractions Research Inc
 */
public class WpsXmlReader extends XmlRequestReader {
    public Logger LOGGER = Logging.getLogger("org.geoserver.wps");

    private WPSConfiguration configuration;

    //private EntityResolverProvider resolverProvider;

    public WpsXmlReader(String element, String version, WPSConfiguration configuration) {
        super(new QName(org.geotools.wps.WPS.NAMESPACE, element), new Version("1.0.0"), "wps");
        this.configuration = configuration;
        //this.resolverProvider = resolverProvider;
    }

    @SuppressWarnings("unchecked")
    public Object read(Object request, Reader reader, Map kvp) throws Exception {
        Parser parser = new Parser(configuration);
        parser.setValidating(true);
        parser.setEntityResolver(PreventLocalEntityResolver.INSTANCE); // todo 修改过

        Object parsed;
        try {
            parsed = parser.parse(reader);
        } catch(Exception e) {
            throw new WPSException("Could not parse XML request.", e);
        }

        if (!parser.getValidationErrors().isEmpty()) {
            WPSException exception = new WPSException("Invalid request", "InvalidParameterValue");

            for(Exception error : (List<Exception>)parser.getValidationErrors()) {
                LOGGER.warning( error.getLocalizedMessage() );
                exception.getExceptionText().add(error.getLocalizedMessage());
            }
        }

        return parsed;
    }
}
