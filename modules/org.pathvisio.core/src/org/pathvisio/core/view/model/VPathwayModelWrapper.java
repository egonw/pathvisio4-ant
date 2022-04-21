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
package org.pathvisio.core.view.model;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.pathvisio.libgpml.model.CopyElement;
import org.pathvisio.libgpml.model.PathwayElement;
import org.pathvisio.libgpml.model.PathwayModel;
import org.pathvisio.libgpml.model.PathwayObject;

/**
 * Wrapper for VPathwayModel that handles toolkit (swing / SWT) dependent
 * differences.
 */
public abstract interface VPathwayModelWrapper {
	public void redraw();

	public void redraw(Rectangle r);

	public Rectangle getViewRect();

	/** signal to indicate that the pathway changed size */
	public void resized();

	public VPathwayModel createVPathwayModel();

	public void registerKeyboardAction(KeyStroke k, Action a);

	public void copyToClipboard(PathwayModel source, List<CopyElement> copyElements); 

	public void pasteFromClipboard();

	public void positionPasteFromClipboard(Point cursorPosition);

	/** make sure r is visible */
	public void scrollTo(Rectangle r);

	public void scrollCenterTo(int x, int y);

	/** called by VPathway.dispose() */
	public void dispose();
}
