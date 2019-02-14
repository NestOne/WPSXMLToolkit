/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package com.supermap.wpsxmltoolkit.executor;

import com.supermap.wpsxmltoolkit.Ows11Util;
import com.supermap.wpsxmltoolkit.WPSException;
import com.supermap.wpsxmltoolkit.executor.inputprovider.AbstractInputProvider;
import com.supermap.wpsxmltoolkit.executor.inputprovider.InputProvider;
import com.supermap.wpsxmltoolkit.executor.inputprovider.ListInputProvider;
import com.supermap.wpsxmltoolkit.executor.inputprovider.StringInputProvider;
import com.supermap.wpsxmltoolkit.ppio.ProcessParameterIO;
import com.supermap.wpsxmltoolkit.process.AbstractRawData;
import com.supermap.wpsxmltoolkit.process.GlobalProcessors;
import net.opengis.wps10.*;
import org.eclipse.emf.common.util.EList;
import org.geotools.data.Parameter;
import org.geotools.process.ProcessFactory;
import org.opengis.feature.type.Name;

import java.util.*;

/**
 * Centralizes some common request parsing activities
 *
 * @author Andrea Aime - GeoSolutions
 *
 */
public class ExecuteData {

    ExecuteType request;

    LazyInputMap inputs;

    public ExecuteData(ExecuteType request) {
        this.request = request;
    }

    /**
     * The wrapped WPS 1.0 request
     *
     *
     */
    public ExecuteType getRequest() {
        return request;
    }

    /**
     * True if the request is asynchronous
     *
     *
     */
    public boolean isAsynchronous() {
        return request.getResponseForm() != null
                && request.getResponseForm().getResponseDocument() != null
                && request.getResponseForm().getResponseDocument().isStoreExecuteResponse();
    }

    /**
     * Returns true if status update is requested
     *
     *
     */
    public boolean isStatusEnabled() {
        return isAsynchronous() && request.getResponseForm().getResponseDocument().isStatus();
    }

    /**
     * Returns the process name according to the GeoTools API
     *
     *
     */
    public Name getProcessName() {
        return Ows11Util.name(request.getIdentifier());
    }

    /**
     * Returns the process inputs according to the GeoTools API expectations
     *
     //* @param manager
     *
     */
    public LazyInputMap getProcessInputs(/*WPSExecutionManager manager*/) {
        if (inputs == null) {
            return getInputsInternal(/*manager*/);
        }
        return inputs;
    }

    LazyInputMap getInputsInternal(/*WPSExecutionManager manager*/) {
        // get the input descriptors
        Name processName = Ows11Util.name(request.getIdentifier());
        ProcessFactory pf = GlobalProcessors.createProcessFactory(processName, true);
        if(pf == null) {
            throw new WPSException("Unknown process " + processName);
        }

        final Map<String, Parameter<?>> parameters = pf.getParameterInfo(processName);
        Map<String, InputProvider> providers = new LinkedHashMap<String, InputProvider>();

        // see what output raw data we have that need the user chosen mime type to be
        // sent back to the process as an input
        Map<String, String> outputMimeParameters = AbstractRawData.getOutputMimeParameters(
                processName, pf);
        if (!outputMimeParameters.isEmpty()) {
            Map<String, String> requestedRawDataMimeTypes = getRequestedRawDataMimeTypes(outputMimeParameters.keySet(), processName, pf);
            for (Map.Entry<String, String> param : outputMimeParameters.entrySet()) {
                String outputName = param.getKey();
                String inputParameter = param.getValue();
                String mime = requestedRawDataMimeTypes.get(outputName);
                StringInputProvider provider = new StringInputProvider(mime, inputParameter);
                providers.put(inputParameter, provider);
            }
        }

        // turn them into a map of input providers
        for (Iterator i = request.getDataInputs().getInput().iterator(); i.hasNext();) {
            InputType input = (InputType) i.next();
            String inputId = input.getIdentifier().getValue();

            // locate the parameter for this request
            Parameter p = parameters.get(inputId);
            if (p == null) {
                throw new WPSException("No such parameter: " + inputId);
            }

            // find the ppio
            String mime = null;
            if (input.getData() != null && input.getData().getComplexData() != null) {
                mime = input.getData().getComplexData().getMimeType();
            } else if (input.getReference() != null) {
                mime = input.getReference().getMimeType();
            }
            ProcessParameterIO ppio = ProcessParameterIO.find(p, mime);
            if (ppio == null) {
                throw new WPSException("Unable to decode input: " + inputId);
            }

            // get the validators
//            Collection<Validator> validators = (Collection<Validator>) p.metadata
//                    .get(ProcessLimitsFilter.VALIDATORS_KEY);
//            // we handle multiplicity validation here, before the parsing even starts
//            List<Validator> filteredValidators = Validators.filterOutClasses(validators,
//                    MultiplicityValidator.class);

            // build the provider
            try {
                InputProvider provider = AbstractInputProvider.getInputProvider(input, ppio/*, manager*/);

                // store the input
                if (p.maxOccurs > 1) {
                    ListInputProvider lp = (ListInputProvider) providers.get(p.key);
                    if (lp == null) {
                        lp = new ListInputProvider(provider, p.getMaxOccurs());
                        providers.put(p.key, lp);
                    } else {
                        lp.add(provider);
                    }
                } else {
                    providers.put(p.key, provider);
                }
            } catch (Exception e) {
                throw new WPSException("Failed to parse process inputs", e);
            }
        }

        return new LazyInputMap(providers);
    }

    private Map<String, String> getRequestedRawDataMimeTypes(Collection<String> rawResults, Name name,
            ProcessFactory pf) {
        Map<String, String> result = new HashMap<String, String>();
        ResponseFormType form = request.getResponseForm();
        OutputDefinitionType raw = form.getRawDataOutput();
        ResponseDocumentType document = form.getResponseDocument();
		if (form == null || (raw == null && document == null)) {
            // all outputs using their default mime
        	for (String rawResult : rawResults) {
        		String mime = AbstractRawData.getDefaultMime(name, pf, rawResult);
        		result.put(rawResult, mime);
			}
        } else if (raw != null) {
            // just one output type
            String output = raw.getIdentifier().getValue();
            String mime;
            if (raw.getMimeType() != null) {
                mime = raw.getMimeType();
            } else {
                mime = AbstractRawData.getDefaultMime(name, pf, output);
            }
            result.put(output, mime);
        } else {
            // the response document form
        	for (Iterator it = document.getOutput().iterator(); it.hasNext();) {
				OutputDefinitionType out = (OutputDefinitionType) it.next();
				String outputName = out.getIdentifier().getValue();
				if(rawResults.contains(outputName)) {
					// was the output mime specified?
					String mime = out.getMimeType();
					if(mime == null || mime.trim().isEmpty()) {
						mime = AbstractRawData.getDefaultMime(name, pf, outputName);
					}
					result.put(outputName, mime);
				}
			}
        }

        return result;
    }

    public boolean isLineageRequested() {
        return request.getResponseForm() != null
                && request.getResponseForm().getResponseDocument() != null
                && request.getResponseForm().getResponseDocument().isLineage();
    }

    /**
     * Returns null if nothing specific was requested, the list otherwise
     *
     */
    public List<DocumentOutputDefinitionType> getRequestedOutputs() {
        // in case nothing specific was requested
        if (request.getResponseForm() == null
                || request.getResponseForm().getResponseDocument() == null
                || request.getResponseForm().getResponseDocument().getOutput() == null) {
            return null;
        }

        List<DocumentOutputDefinitionType> result = new ArrayList<DocumentOutputDefinitionType>();
        EList outputs = request.getResponseForm().getResponseDocument().getOutput();
        for (Object output : outputs) {
            result.add((DocumentOutputDefinitionType) output);
        }
        return result;
    }

}
