/*******************************************************************************
 * PathVisio, a tool for data visualization and analysis using biological pathways
 * Copyright 2006-2022 BiGCaT Bioinformatics, WikiPathways
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.pathvisio.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.pathvisio.core.util.Resources;
import org.pathvisio.libgpml.debug.Logger;
import org.pathvisio.libgpml.debug.WorkerThreadOnly;
import org.pathvisio.libgpml.model.PathwayObject;
import org.pathvisio.libgpml.model.Xrefable;
import org.pathvisio.libgpml.model.type.ObjectType;

/**
 * BackpageTextProvider knows how to generate a html "backpage" for a given
 * PathwayElement. The backpage consists of a template, and several sections.
 * The sections are each generated by an implementation of @link{BackpageHook},
 * and plugins can register more backpage hooks to extend the information in the
 * backpage.
 * <p>
 * Two basic BackpageHooks are defined here: @link{BackpageAttributes} and
 * {@link DataXrefs}. However, these are not automatically registered, that is
 * the responsibility of the instantiator.
 */
public class DataPaneTextProvider {
	/**
	 * Hook into the backpage text provider, use this to generate a fragment of text
	 * for the backpage
	 */
	public static interface DataHook {
		/**
		 * Return a fragment of html-formatted text. The returned fragment should not
		 * contain &lt;html> or &lt;body> tags, but it can contain most other html tags.
		 * <p>
		 * The function getHtml is normally called from a worker thread.
		 */
//		@WorkerThreadOnly
//		public String getHtml(SwingEngine swe);
		@WorkerThreadOnly
		public Object getHtml(PathwayObject e);
	}

	/**
	 * A @{link DataHook} that generates a section of the data panel showing:
	 * currently loaded databases currently loaded datasets currently loaded
	 * visualizations
	 * 
	 */
	public static class DataAttributes implements DataHook {
		private SwingEngine swe;

		public DataAttributes(SwingEngine swe) {
			this.swe = swe;
		}
//		String gdb = swe.getGdbManager().getGeneDb().toString();
//		String mdb = swe.getGdbManager().getMetaboliteDb().toString();
//		String text = gdb + mdb ;

		@Override
		public Object getHtml(PathwayObject e) {
			// TODO Auto-generated method stub
			return null;
		}

//		@Override
//		public String getHtml(SwingEngine swe) {
//			String text = "";
//			
//			try
//			{
//				String gdb = "" + swe.getGdbManager().getGeneDb();
//				String mdb = "" + swe.getGdbManager().getMetaboliteDb();
//			}
//			catch (Exception ex)
//			{
//				text += "Exception occurred, see log for details</br>";
//				Logger.log.error ("Error fetching data panel info", ex);
//			}
//			return text;
//		}

	}

	/**
	 * Register a BackpageHook with this text provider. Backpage fragments are
	 * generated in the order that the hooks were registered.
	 */
	public void addDataHook(DataHook hook) {
		hooks.add(hook);
	}

	private final List<DataHook> hooks = new ArrayList<DataHook>();

	public DataPaneTextProvider() {
		initializeHeader();
	}

	/**
	 * generates html for a given PathwayElement. Combines the base header with
	 * fragments from all BackpageHooks into one html String. TODO
	 */
	public String getAnnotationHTML(PathwayObject e) {
		if (e == null) {
			return "<p>No pathway element is selected.</p>";
		} else if (!(e instanceof Xrefable)) {
			return "<p>It is currently not possible to annotate this type of pathway element."
					+ "<BR>Only Pathways, DataNodes, States, Interactions and Groups can be annotated.</p>";
		} else if (((Xrefable) e).getXref() == null) {
			return "<p>This pathway element has not yet been annotated.</p>";
		} else if (((Xrefable) e).getXref().getDataSource() == null || ((Xrefable) e).getXref().getId().equals("")) {
			return "<p>This pathway element has not yet been annotated.</p>";
		}
		StringBuilder builder = new StringBuilder(backpagePanelHeader);
		for (DataHook h : hooks) {
			builder.append(h.getHtml(e));
		}
		builder.append("</body></html>");
		return builder.toString();
	}

	/**
	 * generates html for PathVisio Data. Combines the base header with fragments
	 * from all BackpageHooks into one html String.
	 */
	public static String getDataHTML(SwingEngine swe) {
		if (swe == null) {
			return "<p>No pathway element is selected.</p>";
		}
		StringBuilder builder = new StringBuilder();
		String gdb = "" + swe.getGdbManager().getGeneDb();
		String mdb = "" + swe.getGdbManager().getMetaboliteDb();
		builder.append(gdb);
		builder.append(mdb);
		builder.append("</body></html>");
		return builder.toString();
	}

	/**
	 * Header file, containing style information
	 */
	final private static String HEADERFILE = "header.html";

	private String backpagePanelHeader;

	/**
	 * Reads the header of the HTML content displayed in the browser. This header is
	 * displayed in the file specified in the {@link HEADERFILE} field
	 */
	private void initializeHeader() {
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(Resources.getResourceURL(HEADERFILE).openStream()));
			String line;
			backpagePanelHeader = "";
			while ((line = input.readLine()) != null) {
				backpagePanelHeader += line.trim();
			}
		} catch (Exception e) {
			Logger.log.error("Unable to read header file for data browser: " + e.getMessage(), e);
		}
	}

}
