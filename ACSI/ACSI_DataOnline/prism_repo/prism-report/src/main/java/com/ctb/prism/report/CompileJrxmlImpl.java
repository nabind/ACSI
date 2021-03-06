/*
 * JasperReports - Free Java Reporting Library.
 * Copyright (C) 2001 - 2011 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of JasperReports.
 *
 * JasperReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JasperReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JasperReports. If not, see <http://www.gnu.org/licenses/>.
 */
package com.ctb.prism.report;

import java.io.File;
import java.io.FilenameFilter;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

import com.ctb.prism.core.logger.IAppLogger;
import com.ctb.prism.core.logger.LogFactory;


/**
 * @author TCS
 * @version $Id$
 */
public class CompileJrxmlImpl implements CompileJrxml {
	
	private static final IAppLogger logger = LogFactory
	.getLoggerInstance(CompileJrxmlImpl.class.getName());
	
	private static final String JRXML_FILE_EXTN = ".jrxml";
	/*private static final IAppLogger logger = LogFactory
			.getLoggerInstance(CompileJrxmlImpl.class.getName());*/

	public void compileAllJrxml(String srcLocation) {
		try {
			File jrxmlFolder = new File( srcLocation );
			if (jrxmlFolder != null) {
				File[] allJrxml = jrxmlFolder.listFiles(textFilter);
				if(allJrxml != null) {
					for (File jrxml : allJrxml) {
						//logger.log(IAppLogger.DEBUG, "Compiling JRXML : " + jrxml.getName() );
						logger.log(IAppLogger.DEBUG, "Compiling JRXML : " + jrxml.getName() );
						JasperCompileManager.compileReportToFile(jrxml.getAbsolutePath());
						
						// TODO compile report to inputstream
						// save the input stream into database
					}
				} else {
					//logger.log(IAppLogger.INFO, "No JRXML file found to compile." );
					logger.log(IAppLogger.DEBUG, "No JRXML file found to compile.");
				}
			}
		} catch (JRException e) {
			e.printStackTrace();
		}
	}
	
	FilenameFilter textFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            String lowercaseName = name.toLowerCase();
            if (lowercaseName.endsWith(JRXML_FILE_EXTN)) {
                return true;
            } else {
                return false;
            }
        }
    };
	
	public static void main(String[] args) {
		new CompileJrxmlImpl().compileAllJrxml("C:/jasper/prism_repo/assets/jrxml");
	}

}
