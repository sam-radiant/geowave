/*******************************************************************************
 * Copyright (c) 2013-2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License,
 * Version 2.0 which accompanies this distribution and is available at
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 ******************************************************************************/
package mil.nga.giat.geowave.cli.geoserver;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import mil.nga.giat.geowave.core.cli.annotations.GeowaveOperation;
import mil.nga.giat.geowave.core.cli.api.OperationParams;
import mil.nga.giat.geowave.core.cli.api.ServiceStatus;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@GeowaveOperation(name = "listcs", parentOperation = GeoServerSection.class)
@Parameters(commandDescription = "List GeoServer coverage stores")
public class GeoServerListCoverageStoresCommand extends
		GeoServerCommand<String>
{
	@Parameter(names = {
		"-ws",
		"--workspace"
	}, required = false, description = "workspace name")
	private String workspace;

	private ServiceStatus status = ServiceStatus.OK;

	@Override
	public void execute(
			final OperationParams params )
			throws Exception {
		JCommander.getConsole().println(
				computeResults(params));
	}

	public void setStatus(
			ServiceStatus status ) {
		this.status = status;
	}

	@Override
	public Pair<ServiceStatus, String> executeService(
			OperationParams params )
			throws Exception {
		String ret = computeResults(params);
		return ImmutablePair.of(
				status,
				ret);
	}

	@Override
	public String computeResults(
			final OperationParams params )
			throws Exception {
		if ((workspace == null) || workspace.isEmpty()) {
			workspace = geoserverClient.getConfig().getWorkspace();
		}

		final Response listCvgStoresResponse = geoserverClient.getCoverageStores(workspace);

		if (listCvgStoresResponse.getStatus() == Status.OK.getStatusCode()) {
			final JSONObject jsonResponse = JSONObject.fromObject(listCvgStoresResponse.getEntity());
			final JSONArray cvgStores = jsonResponse.getJSONArray("coverageStores");
			setStatus(ServiceStatus.OK);
			return "\nGeoServer coverage stores list for '" + workspace + "': " + cvgStores.toString(2);
		}
		switch (listCvgStoresResponse.getStatus()) {
			case 404:
				setStatus(ServiceStatus.NOT_FOUND);
				break;
			default:
				setStatus(ServiceStatus.INTERNAL_ERROR);
				break;
		}
		return "Error getting GeoServer coverage stores list for '" + workspace + "'; code = "
				+ listCvgStoresResponse.getStatus();
	}
}
