/*
 * $Id: ModisUtils.java,v 1.1 2006/09/19 07:00:04 marcop Exp $
 *
 * Copyright (C) 2002,2003  by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.esa.beam.dataio.modis;

import org.esa.beam.dataio.modis.hdf.HdfAttributeContainer;
import org.esa.beam.dataio.modis.hdf.HdfAttributes;
import org.esa.beam.dataio.modis.productdb.ModisProductDb;
import org.esa.beam.framework.dataio.ProductIOException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

class ModisDaacUtils {

    public static String extractCoreString(HdfAttributes hdfGlobalAttributes) {
        final String coreKey = ModisConstants.CORE_META_KEY;
        final String coreString = coreKey.substring(0, coreKey.length() - 2);

        final Map<String, HdfAttributeContainer> resultMap = new TreeMap<String, HdfAttributeContainer>();
        for (int i = 0; i < hdfGlobalAttributes.getNumAttributes(); i++) {
            final HdfAttributeContainer attributeAt = hdfGlobalAttributes.getAttributeAt(i);
            final String name = attributeAt.getName();
            if (name.startsWith(coreString) && name.length() == coreKey.length()) {
                resultMap.put(name, attributeAt);
            }
        }

        final StringBuffer buffer = new StringBuffer();
        for (HdfAttributeContainer hdfAttributeContainer : resultMap.values()) {
            buffer.append(hdfAttributeContainer.getStringValue());
        }
        return correctAmpersandWrap(buffer.toString());
    }

    public static String correctAmpersandWrap(final String input) {
        final StringReader reader = new StringReader(input);
        final StringWriter result = new StringWriter();

        boolean ampersandMode = false;
        boolean maybeAmpersandMode = false;
        try {
            for (int current = reader.read(); current > -1; current = reader.read()) {
                if (maybeAmpersandMode) {
                    if (current == '\n' || current == '\r') {
                        ampersandMode = true;
                        maybeAmpersandMode = false;
                    } else {
                        result.write('&');
                        maybeAmpersandMode = false;
                    }
                }
                if (ampersandMode) {
                    if (current == '\n' || current == '\r' || current == ' ') {
                        //skip
                    } else {
                        result.write(current);
                        ampersandMode = false;
                    }
                } else {
                    if (current == '&') {
                        maybeAmpersandMode = true;
                    } else {
                        result.write(current);
                    }
                }
            }
        } catch (IOException e) {
            // ignore because the readers input is a StringReader
        }
        return result.toString();
    }

    public static String extractProductType(String s) throws ProductIOException {
        final ModisProductDb db = ModisProductDb.getInstance();

        final ArrayList<String> prodType = new ArrayList<String>();
        final StringTokenizer tokenizer = new StringTokenizer(s, ".");
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            if (db.isSupportedProduct(token)) {
                prodType.add(token);
            }
        }
        if (prodType.size() == 1) {
            return prodType.get(0);
        } else {
            return "";
        }
    }
}