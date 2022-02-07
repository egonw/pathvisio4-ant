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
package org.pathvisio.gui.dnd;

import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.pathvisio.core.gui.PathwayTransferable;
import org.pathvisio.gui.view.VPathwaySwing;
import org.pathvisio.libgpml.debug.Logger;
import org.pathvisio.libgpml.model.ConverterException;
import org.pathvisio.libgpml.model.GpmlFormat;
import org.pathvisio.libgpml.model.ObjectType;
import org.pathvisio.libgpml.model.Pathway;
import org.pathvisio.libgpml.model.PathwayElement;

public class PathwayImportHandler extends TransferHandler implements ClipboardOwner {

	static final int NOT_OWNER = -1;
	int timesPasted; //Keeps track of how many times the same data is pasted
	static final double M_PASTE_OFFSET = 10;

	Set<DataFlavor> supportedFlavors;

	public PathwayImportHandler() {
		supportedFlavors = new HashSet<DataFlavor>();
		supportedFlavors.add(PathwayTransferable.GPML_DATA_FLAVOR);
		supportedFlavors.add(DataFlavor.stringFlavor);
	}

	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		for(DataFlavor d : transferFlavors) {
			if(supportedFlavors.contains(d)) return true;
		}
		return false;
	}
	
	public boolean importData(JComponent comp, Transferable t) {
		try {
			String xml = PathwayTransferable.getText(t);
			if(xml != null) {
				Logger.log.trace("Importing from xml: " + xml);
				importGpml(comp, xml, null);
			}

		} catch(Exception e) {
			Logger.log.error("Unable to paste pathway data", e);
		}
		return false;
	}
	
	public boolean importDataAtCursorPosition(JComponent comp, Transferable t, Point p) {
		try {
			String xml = PathwayTransferable.getText(t);
			if(xml != null) {
				Logger.log.trace("Importing from xml: " + xml);
				importGpml(comp, xml, p);
			}

		} catch(Exception e) {
			Logger.log.error("Unable to paste pathway data", e);
		}
		return false;
	}

	/**
	 * new parameter p is just used for paste with the right click menu
	 */
	private boolean importGpml(JComponent comp, String xml, Point p) throws UnsupportedFlavorException, IOException, ConverterException {
		Pathway pnew = new Pathway();
		GpmlFormat.readFromXml(pnew, new StringReader(xml), true);

		List<PathwayElement> elements = new ArrayList<PathwayElement>();
		for(PathwayElement elm : pnew.getDataObjects()) {
			if(elm.getObjectType() != ObjectType.MAPPINFO) {
				elements.add(elm);
			} else {
				//Only add mappinfo if it's not generated by the transferable
				String source = elm.getMapInfoDataSource();
				if(!PathwayTransferable.INFO_DATASOURCE.equals(source)) {
					elements.add(elm);
				}
			}
		}
		
		if(p == null) {
			int shift = 0;
			if(timesPasted != NOT_OWNER) shift = ++timesPasted;
			((VPathwaySwing)comp).getChild().paste(elements, shift * M_PASTE_OFFSET, shift * M_PASTE_OFFSET);
		} else {
			Point2D.Double shift = calculateShift(elements, p);
			((VPathwaySwing)comp).getChild().paste(elements, shift.x, shift.y);
		}
		return false;
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		timesPasted = NOT_OWNER;
	}

	public void obtainedOwnership() {
		timesPasted = 0;
	}
	
	private Point2D.Double calculateShift(List<PathwayElement> elements, Point cursorPosition) {
		Point2D.Double topLeftCorner = getTopLeftCorner(elements);
		double xShift = cursorPosition.x - topLeftCorner.x;
		double yShift = cursorPosition.y - topLeftCorner.y;
		return new Point2D.Double(xShift, yShift);
	}
	
	/**
	 * Returns the top left corner of the bounding box around the elements
	 * @param elements = list of PathwayElement objects
	 * @return
	 */
	private Point2D.Double getTopLeftCorner(List<PathwayElement> elements) {
		
		Rectangle2D vr = null;
		for (PathwayElement o : elements)
		{
			if (o.getObjectType() == ObjectType.INFOBOX) continue;
			if (o.getObjectType() == ObjectType.BIOPAX) continue;
			else {
				if (vr == null) vr = o.getMBounds();
				else vr.add(o.getMBounds());
			}
		}
		
		return new Point2D.Double(vr.getX(), vr.getY());
	}
}
