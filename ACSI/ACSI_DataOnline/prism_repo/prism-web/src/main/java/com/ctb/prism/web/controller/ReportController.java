package com.ctb.prism.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.util.JRProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ctb.prism.core.constant.IApplicationConstants;
import com.ctb.prism.core.dao.BaseDAO;
import com.ctb.prism.core.exception.SystemException;
import com.ctb.prism.core.logger.IAppLogger;
import com.ctb.prism.core.logger.LogFactory;
import com.ctb.prism.core.util.CustomStringUtil;
import com.ctb.prism.core.util.Utils;
import com.ctb.prism.report.ipcontrol.InputControlFactory;
import com.ctb.prism.report.ipcontrol.InputControlFactoryImpl;
import com.ctb.prism.report.service.DownloadService;
import com.ctb.prism.report.service.IReportService;
import com.ctb.prism.report.transferobject.AssessmentTO;
import com.ctb.prism.report.transferobject.InputControlTO;
import com.ctb.prism.report.transferobject.ObjectValueTO;
import com.ctb.prism.report.transferobject.ReportFilterTO;
import com.ctb.prism.report.transferobject.ReportTO;
import com.ctb.prism.web.util.JsonUtil;


@Controller
public class ReportController extends BaseDAO {

	private static final IAppLogger logger = LogFactory
			.getLoggerInstance(ReportController.class.getName());

	@Autowired
	private IReportService reportService;
	@Autowired
	private static IReportService reportServiceStatic;
	
	@Autowired
	private DownloadService downloadService;

	@RequestMapping(value = "/openDrilldownReport", method = RequestMethod.GET)
	public ModelAndView openDrilldownReport(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		return openReportHtml(req, res);
	}
	
	/**
	 * This method clears all related sessions for a report when user close the tab
	 * @param req
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/clearSessionReports", method = RequestMethod.GET)
	public @ResponseBody String clearSessionReports(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		try {
			String reportId = req.getParameter("reportId");
			String sessionParam = CustomStringUtil.appendString(reportId, "_",
					(String) req.getSession().getAttribute(IApplicationConstants.CURRUSER));
			req.getSession().removeAttribute(sessionParam);
		} catch (Exception e) {
			// nothing to do
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * This method is responsible for rendering report in html format 
	 * with default input controls
	 * @param req
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@Secured({"ROLE_ACSI", "ROLE_SCHOOL", "ROLE_CLASS", "ROLE_ADMIN", "ROLE_PARENT"})
	@RequestMapping(value = "/openReportHtml", method = RequestMethod.GET)
	public ModelAndView openReportHtml(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		logger.log(IAppLogger.INFO, "Enter: ReportController - openReportHtml");
		Connection conn = null;
		String reportName = null;
		try {
			String studentBioId = req.getParameter("studentId");
			
			String reportUrl = req.getParameter("reportUrl");
			String reportId = req.getParameter("reportId");
			reportName = req.getParameter("reportName");
			String filter = req.getParameter("filter");
			String assessmentId = req.getParameter("assessmentId");
			String achAssessmentId = req.getParameter("p_ach_assessmentId");
			req.getSession().setAttribute(IApplicationConstants.CURR_ASSESSMENT, assessmentId);
			if(achAssessmentId != null && achAssessmentId.trim().length() > 0)
				req.getSession().setAttribute(IApplicationConstants.CURR_ASSESSMENT, achAssessmentId);
			//String sessionParam = reportUrl.replace("/", "_");
			String sessionParam = CustomStringUtil.appendString(reportId, "_",
					(String) req.getSession().getAttribute(IApplicationConstants.CURRUSER));
			
			// is it drilldown report?
			String drilldown = req.getParameter("drillDown");
			if(IApplicationConstants.TRUE.equals(drilldown)) {
				filter = drilldown;
			}
			// get jasper report
			List<ReportTO> jasperReports = getJasperReportObject(reportUrl);
			req.getSession().setAttribute(CustomStringUtil.appendString(reportUrl, "_", assessmentId), getMainReport(jasperReports));
			
			// get jasper print object
			//JasperPrint jasperPrint = getJasperPrintObject(reportUrl, filter, assessmentId, sessionParam, req, drilldown);
			JasperPrint jasperPrint = getJasperPrintObject(jasperReports, reportUrl, filter, assessmentId, sessionParam, req, drilldown, false, studentBioId);
			if(jasperPrint != null) reportName = jasperPrint.getName();
			// export the report
			res.setContentType("text/html");
			PrintWriter out = res.getWriter();
			
			JRXhtmlExporter exporter = new JRXhtmlExporter();
			
			JRProperties.setProperty("com.jaspersoft.jasperreports.fusion.charts.context.swf.url", "fusion/charts");
			JRProperties.setProperty("com.jaspersoft.jasperreports.fusion.charts.base.swf.url", "fusion/charts");
			JRProperties.setProperty("com.jaspersoft.jasperreports.fusion.maps.context.swf.url", "fusion/maps");
			JRProperties.setProperty("com.jaspersoft.jasperreports.fusion.maps.base.swf.url", "fusion/maps");
			JRProperties.setProperty("com.jaspersoft.jasperreports.fusion.widgets.context.swf.url", "fusion/widgets");
			JRProperties.setProperty("com.jaspersoft.jasperreports.fusion.widgets.base.swf.url", "fusion/widgets");
			
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, out);
			exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "image?image=");
			
			// for pagination
			int noOfPages = jasperPrint.getPages().size();
			String pageStr = req.getParameter("page");
			pageStr = pageStr == null? "0": pageStr;
			if ( noOfPages > 1 ) {
				req.getSession().setAttribute(sessionParam, jasperPrint);
				exporter.setParameter(JRExporterParameter.PAGE_INDEX, Integer.parseInt(pageStr));
				req.getSession().setAttribute(reportUrl, IApplicationConstants.TRUE);
				//req.setAttribute("enablePagination", "true");
				//req.setAttribute("page", 0);
				req.setAttribute("totalPages", noOfPages );
				req.getSession().setAttribute(IApplicationConstants.TOTAL_PAGES, noOfPages);
			} else {
				req.getSession().setAttribute(reportUrl, IApplicationConstants.FALSE);
			}
			if(IApplicationConstants.TRUE.equals(drilldown)) {
				req.setAttribute("reportUrl", reportUrl );
				req.setAttribute("lastPage", noOfPages-1 );
				req.setAttribute("nextPage", Integer.parseInt(pageStr)+1 );
				req.setAttribute("prevPage", Integer.parseInt(pageStr)-1 );
			}
			
			if(noOfPages == 0) {
				ModelAndView modelAndView = new ModelAndView("report/emptyReport");
				modelAndView.addObject("reportName", jasperPrint.getName());
				if(req.getParameter("msg") != null) {
					modelAndView.addObject("reportMsg", req.getParameter("msg"));
				} else {
					modelAndView.addObject("reportMsg", IApplicationConstants.EMPTY_REPORT);
				}
				// Message for Parent reports
				if(studentBioId != null && studentBioId.length() > 0) {
					if("TerraNova 3 Student Report".equals(jasperPrint.getName())
							|| "Bible Student Report".equals(jasperPrint.getName()) ) {
						modelAndView.addObject("reportMsg", "This student did not take the test.");
					}
				}
				return modelAndView;
			} else {
				req.getSession().setAttribute(
						CustomStringUtil.appendString(reportUrl, IApplicationConstants.REPORT_HEIGHT), 
						jasperPrint.getPageHeight());
				req.getSession().setAttribute(
						CustomStringUtil.appendString(reportUrl, IApplicationConstants.REPORT_WIDTH), 
						jasperPrint.getPageWidth());
			}
			
			exporter.exportReport();
		} catch (Exception exception) {
			ModelAndView modelAndView = new ModelAndView("report/emptyReport");
			modelAndView.addObject("reportName", (reportName != null)? reportName : "Error rendering report. Please try later.");
			modelAndView.addObject("reportMsg", IApplicationConstants.EMPTY_REPORT);
			logger.log(IAppLogger.ERROR, exception.getMessage(), exception);
			return modelAndView;
		} finally {
			if(conn != null) try {conn.close();} catch (SQLException e) {}
			logger.log(IAppLogger.INFO, "Exit: ReportController - openReportHtml");
		}

		return null;
	}
	

	/**
	 * Get jasper print object - with report-URL and without session param
	 * 
	 * @throws Exception 
	 * @throws DataAccessException 
	 * @see com.ctb.prism.web.controller.ReportController.getJasperPrintObject
	 */
	private JasperPrint getJasperPrintObject(String filter, String reportUrl, String assessmentId, HttpServletRequest req, String drilldown, boolean isPrinterFriendly) throws DataAccessException, Exception {
		return getJasperPrintObject(null, reportUrl, filter, assessmentId, "", req, drilldown, isPrinterFriendly, null);
	}
	
	/**
	 * Get jasper print object - with report-URL
	 * 
	 * @throws DataAccessException
	 * @throws Exception
	 */
	private JasperPrint getJasperPrintObject(String reportUrl, String filter, 
			String assessmentId, String sessionParam, HttpServletRequest req, String drilldown) throws DataAccessException, Exception {
		return getJasperPrintObject(null, reportUrl, filter, assessmentId, sessionParam, req, drilldown, false, null);
	}
	
	/**
	 * Get jasper print object with filled parameters
	 * @param reportUrl
	 * @param filter
	 * @param assessmentId
	 * @param sessionParam
	 * @param req
	 * @return
	 * @throws DataAccessException, Exception 
	 */
	private JasperPrint getJasperPrintObject(List<ReportTO> reportList, String reportUrl, String filter, 
			String assessmentId, String sessionParam, HttpServletRequest req, String drilldown, 
			boolean isPrinterFriendly, String studentBioId) throws DataAccessException, Exception {
		Connection conn = null;
		JasperPrint jasperPrint = null;
		try {
			String currentUser = (String) req.getSession().getAttribute(IApplicationConstants.CURRUSER);
			String currentOrg = (String) req.getSession().getAttribute(IApplicationConstants.CURRORG);
			
			// get compiled jasper report
			JasperReport jasperReport = null;
			boolean mainReportPresent = false;
			
			//fetch report list 
			if(reportList == null) reportList = getCompliledJrxmlList(reportUrl);
			
			if(reportList != null && !reportList.isEmpty()) {
				for(ReportTO reportTo : reportList) {
					if(reportTo.isMainReport()) {
						jasperReport = reportTo.getCompiledReport();
						mainReportPresent = true;
						break;
					}
				}
				
				if(!mainReportPresent) {
					jasperReport = reportList.get(0).getCompiledReport();
				}
			} else {
				// report empty
				throw new Exception("Report not found");
			}
			
			
			// get all input controls for report
			List<InputControlTO> allInputControls = getInputControlList(reportUrl);
			
			Map<String, Object> parameters = null;
			if(IApplicationConstants.TRUE.equals(filter)) {
				parameters = getReportParametersFromRequest(req, allInputControls, new ReportFilterTO(), currentOrg, drilldown);
				// remove jasper print object from session when filtering 
				req.getSession().removeAttribute(sessionParam);
			} else {
				// get default parameters for logged-in user
				ReportFilterTO reportFilterTO = reportService.getDefaultFilter(allInputControls, currentUser, assessmentId, "", reportUrl);

				// get parameter values for report
				//parameters = getReportParameter(allInputControls, reportFilterTO);
				parameters = getReportParameter(allInputControls, reportFilterTO, jasperReport);
			}
			if(isPrinterFriendly) {
				parameters.put("p_Is3D", IApplicationConstants.FLAG_N);
			}
			if(studentBioId != null && studentBioId.trim().length() > 0) {
				parameters.put("p_Student_Bio_Id", studentBioId);
			}
			// add subreports 
			if(mainReportPresent && reportList.size() > 1) {
				int count = 0;
				// add subreports
				for(ReportTO reportTo : reportList) {
					if(!reportTo.isMainReport() && reportTo.isJrxml()) {
						count++;
						parameters.put("Subreport_"+count, reportTo.getCompiledReport());
					}
				}
				// add images
				count = 0;
				for(ReportTO reportTo : reportList) {
					if(!reportTo.isJrxml()) {
						count++;
						String logoFile = "";
						InputStream inputStream = null;
						URL settingsUrl = null;
						assessmentId =(String) req.getSession().getAttribute(IApplicationConstants.CURR_ASSESSMENT);
						if(assessmentId == null) assessmentId = "101"; // fallback code for product logo
						if("101".equals(assessmentId)) logoFile = "TerranovaLogo.jpg";
						if("102".equals(assessmentId)) logoFile = "PTCSLogo.jpg";
						if("103".equals(assessmentId)) logoFile = "InViewLogo.jpg";
						if("104".equals(assessmentId)) logoFile = "BibleLogo.jpg";
						if(IApplicationConstants.IS_INVITATION_PDF.equals(assessmentId)) logoFile = "ACSILogo.png";
							
						settingsUrl = Thread.currentThread().getContextClassLoader().getResource(logoFile);
						if(settingsUrl != null) parameters.put("Image_"+count, settingsUrl);							
						
						  // settingsUrl = Thread.currentThread().getContextClassLoader().getResource(logoFile);
							//if(settingsUrl != null) inputStream = settingsUrl.openStream();
							//parameters.put("Image_"+count, inputStream);
												
						/*req.getSession().setAttribute(CustomStringUtil.appendString(
								IApplicationConstants.REPORT_IMAGE, assessmentId), reportTo.getImage());*/
					}
				}
			}
			// fill the report with parameter
			//conn = getPrismConnection();
			jasperPrint = (JasperPrint) req.getSession().getAttribute(sessionParam);
			if ( jasperPrint == null ) {
				//jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
				jasperPrint = reportService.getFilledReport(jasperReport, parameters);
			}
		} catch (DataAccessException exception) {
			logger.log(IAppLogger.ERROR, exception.getMessage(), exception);
			throw exception;
		} catch (Exception exception) {
			logger.log(IAppLogger.ERROR, exception.getMessage(), exception);
			throw exception;
		} finally {
			if(conn != null) try {conn.close();} catch (SQLException e) {}
		}
		return jasperPrint;
	}
	
	/**
	 * Get jasper report object 
	 * @param reportUrl
	 * @return JasperReport object list
	 * @throws DataAccessException, Exception 
	 */
	private List<ReportTO> getJasperReportObject(String reportUrl) throws DataAccessException, Exception {
		Connection conn = null;
		JasperReport jasperReport = null;
		List<ReportTO> reportList = null;
		try {
			// get compiled jasper report
			//JasperReport jasperReport = getCompliledJrxml(reportUrl);
			boolean mainReportPresent = false;
			reportList = getCompliledJrxmlList(reportUrl);
			if(reportList != null && !reportList.isEmpty()) {
				for(ReportTO reportTo : reportList) {
					if(reportTo.isMainReport()) {
						jasperReport = reportTo.getCompiledReport();
						mainReportPresent = true;
						break;
					}
				}
				
				if(!mainReportPresent) {
					jasperReport = reportList.get(0).getCompiledReport();
				}
			} else {
				// report empty
				throw new Exception("Report not found");
			}
			
		} catch (DataAccessException exception) {
			logger.log(IAppLogger.ERROR, exception.getMessage(), exception);
			throw exception;
		} catch (Exception exception) {
			logger.log(IAppLogger.ERROR, exception.getMessage(), exception);
			throw exception;
		} finally {
			if(conn != null) try {conn.close();} catch (SQLException e) {}
		}
		return reportList;
	}
	
	/**
	 * Retrieve main report object
	 * @param jasperReportList
	 * @throws Exception
	 */
	private JasperReport getMainReport(List<ReportTO> jasperReportList) throws Exception {
		JasperReport jasperReport = null;
		boolean mainReportPresent = false;
		if(jasperReportList != null && !jasperReportList.isEmpty()) {
			for(ReportTO reportTo : jasperReportList) {
				if(reportTo.isMainReport()) {
					jasperReport = reportTo.getCompiledReport();
					mainReportPresent = true;
					break;
				}
			}
			
			if(!mainReportPresent) {
				jasperReport = jasperReportList.get(0).getCompiledReport();
			}
		} else {
			// report empty
			throw new Exception("Report not found");
		}
		return jasperReport;
	}
	
	/**
	 * This method is to check pagination for a report
	 * @param req
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@Secured({"ROLE_ACSI", "ROLE_SCHOOL", "ROLE_CLASS", "ROLE_ADMIN", "ROLE_PARENT"})
	@RequestMapping(value = "/checkpagination", method = RequestMethod.GET)
	public @ResponseBody String checkpagination(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		logger.log(IAppLogger.INFO, "Enter: ReportController - checkpagination");

		try {
			String reportUrl = req.getParameter("reportUrl");
			Integer height = (Integer) req.getSession().getAttribute(
					CustomStringUtil.appendString(reportUrl, IApplicationConstants.REPORT_HEIGHT));
			Integer width = (Integer) req.getSession().getAttribute(
					CustomStringUtil.appendString(reportUrl, IApplicationConstants.REPORT_WIDTH));
			String paginate = "";
			int page = 0;
			if(IApplicationConstants.TRUE.equals(req.getSession().getAttribute(reportUrl))) {
				paginate = IApplicationConstants.TRUE;
				page = (Integer) req.getSession().getAttribute(IApplicationConstants.TOTAL_PAGES);
			}
			res.setContentType("application/json");
			res.getWriter().write( "{\"status\":\"Success\", \"paginate\":\""+paginate+"\", \"page\":\""+page+"\", \"height\":\""+height+"\", \"width\":\""+width+"\"}" );
			
		} catch (Exception exception) {
			logger.log(IAppLogger.ERROR, exception.getMessage(), exception);
		} finally {
			logger.log(IAppLogger.INFO, "Exit: ReportController - checkpagination");
		}
		return null;
	}
	
	/**
	 * Get map from request
	 * @param req
	 * @param allInputControls
	 * @return
	 */
	private Map<String, Object> getReportParametersFromRequest(HttpServletRequest req, 
			List<InputControlTO> allInputControls, ReportFilterTO reportFilterTO, String userName, String drilldown) {
		Map<String, Object> parameters = null;
		
		/*parameters = new HashMap<String, Object>();
		parameters.put(IApplicationConstants.JASPER_ORG_PARAM, "1001" reportFilterTO.getLoggedInUserJasperOrgId());
		parameters.putAll(req.getParameterMap()); */
		
		if(allInputControls != null) {
			parameters = new HashMap<String, Object>();
			parameters.put(IApplicationConstants.JASPER_ORG_PARAM, userName /*reportFilterTO.getLoggedInUserJasperOrgId()*/);
			for(InputControlTO inputTO : allInputControls) {
				/*if(req.getParameter(inputTO.getLabelId()) != null 
						&& req.getParameter(inputTO.getLabelId()).trim().length() > 0) {
					parameters.put(inputTO.getLabelId(), req.getParameter(inputTO.getLabelId()));
				}*/
				if(req.getParameterValues(inputTO.getLabelId()) != null 
						&& req.getParameterValues(inputTO.getLabelId()).length > 0) {
					if(req.getParameterValues(inputTO.getLabelId()).length == 1 && !(IApplicationConstants.DATA_TYPE_COLLECTION.equals(inputTO.getType()))) {
						// block for multiselect box
						if(IApplicationConstants.TRUE.equals(drilldown)) {
							// for drilldown report ethnicity comes as [x, y, z]
							if(req.getParameter(inputTO.getLabelId()).indexOf("[") != -1) {
								String[] params = (req.getParameter(inputTO.getLabelId())).replace("[", "").replace("]", "").split(",");
								String[] trimmedArray = new String[params.length];
								for(int i = 0; i < params.length; i++) trimmedArray[i] = params[i].trim();
								List<String> inputCollection = Arrays.asList(trimmedArray);
								parameters.put(inputTO.getLabelId(), inputCollection);
							} else {
								parameters.put(inputTO.getLabelId(), req.getParameter(inputTO.getLabelId()));
							}
						} else {
							parameters.put(inputTO.getLabelId(), req.getParameter(inputTO.getLabelId()));
						}
						//parameters.put(inputTO.getLabelId(), req.getParameter(inputTO.getLabelId()));
					} else {
						// block for multiselect box
						if(IApplicationConstants.TRUE.equals(drilldown)) {
							// for drilldown report ethnicity comes as [x, y, z]
							if(req.getParameter(inputTO.getLabelId()).indexOf("[") != -1) {
								String[] params = (req.getParameter(inputTO.getLabelId())).replace("[", "").replace("]", "").split(",");
								String[] trimmedArray = new String[params.length];
								for(int i = 0; i < params.length; i++) trimmedArray[i] = params[i].trim();
								List<String> inputCollection = Arrays.asList(trimmedArray);
								parameters.put(inputTO.getLabelId(), inputCollection);
							} else {
								parameters.put(inputTO.getLabelId(), req.getParameter(inputTO.getLabelId()));
							}
						} else {
							String[] params = req.getParameterValues(inputTO.getLabelId());
							List<String> inputCollection = Arrays.asList(params);;
							parameters.put(inputTO.getLabelId(), inputCollection);
						}
					}
				}
			}
		}
		return parameters;
	}
	
	/**
	 * Get parameters for report
	 * @param allInputControls
	 * @param reportFilterTO
	 * @param JasperReport
	 * @param allInputControls
	 * @param reportFilterTO
	 * @param jasperReport
	 * @param getFullList
	 * @return Map
	 */
	public Map<String, Object> getReportParameter(List<InputControlTO> allInputControls, ReportFilterTO reportFilterTO) {
		return getReportParameter(allInputControls, reportFilterTO, false);
	}
	
	public Map<String, Object> getReportParameter(List<InputControlTO> allInputControls, ReportFilterTO reportFilterTO, JasperReport jasperReport) {
		return getReportParameter(allInputControls, reportFilterTO, jasperReport, false);
	}
	
	public Map<String, Object> getReportParameter(List<InputControlTO> allInputControls, ReportFilterTO reportFilterTO, boolean getFullList) {
		return getReportParameter(allInputControls, reportFilterTO, null, getFullList);
	}
	
	public Map<String, Object> getReportParameter(List<InputControlTO> allInputControls, 
			ReportFilterTO reportFilterTO, JasperReport jasperReport, boolean getFullList) {
		Class<?> c = ReportFilterTO.class;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(IApplicationConstants.JASPER_ORG_PARAM, reportFilterTO.getLoggedInUserJasperOrgId());
		try {
			if(allInputControls != null) {
				for(InputControlTO inputControlTO : allInputControls) {
					String fieldName = CustomStringUtil.capitalizeFirstCharacter(inputControlTO.getLabelId());
					Method m = c.getMethod( CustomStringUtil.appendString("get", fieldName) );
					@SuppressWarnings("unchecked")
					ArrayList<ObjectValueTO> listOfValues = (ArrayList<ObjectValueTO>) m.invoke(reportFilterTO);
					
					/** PATCH FOR DEFAULT SUBTEST AND SCORE TYPE POPULATION (Multiselect) */
					// get default list for multiselect subtest
					// this code is a path for subtest to meet business requirement to show default subtest list
					String[] defaultValues = null;
					boolean checkDefault = false;
					List<String> defaultInputNames = new ArrayList<String>();
					Map<String, String[]> defaultInputValues = new HashMap<String, String[]>();
					try {
						for (IApplicationConstants.PATCH_FOR_SUBTEST subtest : IApplicationConstants.PATCH_FOR_SUBTEST.values()) {
						    if (subtest.name().equals(inputControlTO.getLabelId())) {
						    	defaultInputNames.add(inputControlTO.getLabelId());
						    	for(int i = 0; i < jasperReport.getParameters().length; i++) {
									if(inputControlTO.getLabelId().equals(jasperReport.getParameters()[i].getName())) {
										String value = jasperReport.getParameters()[i].getDefaultValueExpression().getText();
										value = value.replace("Arrays.asList(", "");
										value = value.replace(")", "");
										value = value.replace("\"", "");
										defaultValues = value.split(",");
										checkDefault = true;
										break;
									}
								}		
						    	defaultInputValues.put(inputControlTO.getLabelId(), defaultValues);
						    	//break;
						    }
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					/** END : PATCH FOR DEFAULT SUBTEST AND SCORE TYPE POPULATION (Multiselect) */
					
					if(getFullList) {
						// for input control section
						if(IApplicationConstants.DATA_TYPE_TESTBOX.equals(inputControlTO.getType())) {
							for(int i = 0; i < jasperReport.getParameters().length; i++) {
								if(inputControlTO.getLabelId().equals(jasperReport.getParameters()[i].getName())) {
									String value = jasperReport.getParameters()[i].getDefaultValueExpression().getText();
									parameters.put(inputControlTO.getLabelId(), value);
									break;
								}
							}
						} else {
							parameters.put(inputControlTO.getLabelId(), listOfValues);
							if(!checkDefault) {
								parameters.put(inputControlTO.getLabelId(), listOfValues);
							} else {
								parameters.put(inputControlTO.getLabelId(), listOfValues);
								parameters.put(IApplicationConstants.CHECK_DEFAULT, defaultInputValues);
								parameters.put(IApplicationConstants.CHECK_DEFAULT_NAME, defaultInputNames);
								
								/*// list need to be modified based on default value
								List<ObjectValueTO> inputCollection = new ArrayList<ObjectValueTO>();
								for(ObjectValueTO objectValue : listOfValues) {
									// this field has default values - need to pass values that belongs to this default list
									if(defaultValues != null) {
										for(String currentVal : defaultValues) {
											if(objectValue.getValue().equals(currentVal)) {
												inputCollection.add(objectValue);
											}
										}
									} else {
										inputCollection.add(objectValue);
									}
									parameters.put(IApplicationConstants.CHECK_DEFAULT, defaultValues);
									parameters.put(IApplicationConstants.CHECK_DEFAULT_NAME, defaultInputNames);
								}
								parameters.put(inputControlTO.getLabelId(), inputCollection);*/
							}
						}
					} else {
						// fetch i/p for default values
						if(IApplicationConstants.DATA_TYPE_COLLECTION.equals(inputControlTO.getType())) {
							// passing array
							List<String> inputCollection = new ArrayList<String>();
							for(ObjectValueTO objectValue : listOfValues) {
								if(!checkDefault) {
									inputCollection.add(objectValue.getValue());
									if(IApplicationConstants.EXTENDED_YEAR.equals(inputControlTO.getLabelId())) {
										break;
									}
								} else {
									// this field has default values - need to pass values that belongs to this default list
									if(defaultValues != null) {
										for(String currentVal : defaultValues) {
											if(objectValue.getValue().equals(currentVal)) {
												inputCollection.add(objectValue.getValue());
											}
										}
										parameters.put(IApplicationConstants.CHECK_DEFAULT, defaultInputValues);
										parameters.put(IApplicationConstants.CHECK_DEFAULT_NAME, defaultInputNames);
									} else {
										inputCollection.add(objectValue.getValue());
									}
								}
							}
							parameters.put(inputControlTO.getLabelId(), inputCollection);
						} else if(IApplicationConstants.DATA_TYPE_TESTBOX.equals(inputControlTO.getType())) {
							for(int i = 0; i < jasperReport.getParameters().length; i++) {
								if(fieldName.equals(jasperReport.getParameters()[i].getName())) {
									String value = jasperReport.getParameters()[i].getDefaultValueExpression().getText();
									parameters.put(inputControlTO.getLabelId(), value);
									break;
								}
							}
						} else {
							String value = "";
							if(listOfValues != null && listOfValues.size() > 0) {
								if("Form/Level".equals(inputControlTO.getLabel()) || "Level".equals(inputControlTO.getLabel())) {
									for(ObjectValueTO objectValue : listOfValues) {
										if(objectValue.getName() != null && objectValue.getName().indexOf("Default") != -1) {
											// this block is for form/level
											value = objectValue.getValue();
										}
									}
								} else {
									value = listOfValues.get(0).getValue();
								}
							}
							// fallback code
							if((value == null || value.length() == 0) && listOfValues != null && listOfValues.size() > 0) {
								value = listOfValues.get(0).getValue();
							}
							parameters.put(inputControlTO.getLabelId(), value);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.log(IAppLogger.WARN, CustomStringUtil.appendString("Some error occured : ", e.getMessage()));
			e.printStackTrace();
		} 
		return parameters;
	}
	
	/**
	 * This method returns .jasper object after compiling JRXML
	 * @param reportUrl
	 * @return JasperReport
	 */
	public JasperReport getCompliledJrxml(String reportUrl) throws DataAccessException {
		return reportService.getReportJasperObject(reportUrl);
	}
	
	public static JasperReport getCompliledSubreport(String reportname) throws DataAccessException {
		return reportServiceStatic.getReportJasperObjectForName(reportname);
	}
	
	public List<ReportTO> getCompliledJrxmlList(String reportUrl) throws DataAccessException, JRException {
		return reportService.getReportJasperObjectList(reportUrl);
	}
	
	/**
	 * This method returns all available input controls
	 * @param reportUrl
	 * @return List<InputControlTO> 
	 */
	public List<InputControlTO> getInputControlList(String reportUrl) {
		return reportService.getInputControlDetails(reportUrl);
	}
	
	/**
	 * This method is responsible for returning all available 
	 * input controls for the selected report
	 * @param req
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	@Secured({"ROLE_ACSI", "ROLE_SCHOOL", "ROLE_CLASS", "ROLE_ADMIN", "ROLE_PARENT"})
	@RequestMapping(value = "/populateInputControls", method = RequestMethod.GET)
	public @ResponseBody String populateInputControls(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		logger.log(IAppLogger.INFO, "Enter: ReportController - populateInputControls");

		try {
			String currentUser = (String) req.getSession().getAttribute(IApplicationConstants.CURRUSER);
			String reportUrl = req.getParameter("reportUrl");
			String tabCount = req.getParameter("count");
			String assessmentId = req.getParameter("assessmentId");
			
			// get all input controls for report
			List<InputControlTO> allInputControls = getInputControlList(reportUrl);
			
			// get default parameters for logged-in user
			ReportFilterTO reportFilterTO = reportService.getDefaultFilter(allInputControls, currentUser, assessmentId, "", reportUrl);
			
			// get current JasperReport object
			JasperReport jasperReport = (JasperReport) req.getSession().getAttribute(
					CustomStringUtil.appendString(reportUrl, "_", assessmentId));
			// get parameter values for report
			//Map<String, Object> parameters = getReportParameter(allInputControls, reportFilterTO, true);
			Map<String, Object> parameters = getReportParameter(allInputControls, reportFilterTO, jasperReport, true);
			
			InputControlFactory inputControlFact = new InputControlFactoryImpl();
			
			StringBuilder inputControlDom = new StringBuilder();
			if(allInputControls != null) {
				
				// checking if the input control need to be seperated
				boolean isSeperateInputs = false;
				boolean firstSection = true;
				boolean firstSectionClosed = false;
				for(InputControlTO inputControlTO : allInputControls) {
					if(IApplicationConstants.ETHNICITY_ID.equals(inputControlTO.getLabelId())) {
						isSeperateInputs = true;
						break;
					}
				}
				
				int count = 0;
				if(isSeperateInputs) {
					// start of first section
					inputControlDom.append( inputControlFact.getInputSectionWrapper(true) );
				}
				for(InputControlTO inputControlTO : allInputControls) {
					count++;
					if(parameters.get(inputControlTO.getLabelId()) != null) {
						/* Uncomment this section - if we need to separate bio in next line
						 * if("p_Ethnicity".equals(inputControlTO.getLabelId())) {
							count = appendEmptyBox(count-1, inputControlDom, inputControlFact);
						}*/
						if(isSeperateInputs) { // If BIO input present
							if(firstSection && IApplicationConstants.ETHNICITY_ID.equals(inputControlTO.getLabelId())) {
								firstSection = false;
							}
							if(firstSection) {
								inputControlDom.append( inputControlFact.getSelectInputControl(
										(List<ObjectValueTO>) parameters.get(inputControlTO.getLabelId()), inputControlTO.getLabel(), 
										inputControlTO.getLabelId(), reportUrl, tabCount, inputControlTO.isCollection(), 
										inputControlTO.isInputBox(), assessmentId, parameters, isSeperateInputs) );
							} else {
								if(!firstSectionClosed) {
									// close first section
									inputControlDom.append( inputControlFact.getInputSectionWrapper(false) );
									firstSectionClosed = true;
									// start of section section
									inputControlDom.append( inputControlFact.getInputSectionWrapperTwo(true) );
								}
								inputControlDom.append( inputControlFact.getSelectInputControl(
										(List<ObjectValueTO>) parameters.get(inputControlTO.getLabelId()), inputControlTO.getLabel(), 
										inputControlTO.getLabelId(), reportUrl, tabCount, inputControlTO.isCollection(), 
										inputControlTO.isInputBox(), assessmentId, parameters, isSeperateInputs) );
							}
						} else { // If BIO input is not there
							if(inputControlTO.isInputBox()) {
								inputControlDom.append( inputControlFact.getTextInputControl(
										(String) parameters.get(inputControlTO.getLabelId()), inputControlTO.getLabel(), 
										inputControlTO.getLabelId(), inputControlTO.isInputBox(), assessmentId) );
							} else {
								inputControlDom.append( inputControlFact.getSelectInputControl(
									(List<ObjectValueTO>) parameters.get(inputControlTO.getLabelId()), inputControlTO.getLabel(), 
									inputControlTO.getLabelId(), reportUrl, tabCount, inputControlTO.isCollection(), 
									inputControlTO.isInputBox(), assessmentId, parameters, isSeperateInputs) );
							}
						}
					}
					
				}
				if(isSeperateInputs) {
					// close of first section
					inputControlDom.append( inputControlFact.getInputSectionWrapperTwo(false) );
				}
			}
			
			
			// export the report
			//res.setContentType("plain/text");
			res.setContentType("application/json");
			//res.getWriter().write( inputControlDom.toString() );
			res.getWriter().write( "{\"status\":\"Success\", \"inputDom\":\""+CustomStringUtil.escapeQuote(inputControlDom.toString())+"\"}" );
			
		} catch (Exception exception) {
			logger.log(IAppLogger.ERROR, exception.getMessage());
		} finally {
			logger.log(IAppLogger.INFO, "Exit: ReportController - populateInputControls");
		}

		return null;
	}
	
	private int appendEmptyBox(int count, StringBuilder inputControlDom, InputControlFactory inputControlFact) {
		if(count%4 != 0) {
			inputControlDom.append( inputControlFact.getSelectInputControlEmptyBox() );
			count++;
			appendEmptyBox(count, inputControlDom, inputControlFact);
		}
		return count;
	}
	
	/**
	 * This method will be called on change of any select value
	 * This will get all refined input control values which are depends on selected i/p control value
	 * @param req
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@Secured({"ROLE_ACSI", "ROLE_SCHOOL", "ROLE_CLASS", "ROLE_ADMIN", "ROLE_PARENT"})
	@RequestMapping(value = "/checkCascading", method = RequestMethod.GET)
	public @ResponseBody String checkCascading(HttpServletRequest req,
			HttpServletResponse res) throws IOException {
		
		try {
			String currentUser = (String) req.getSession().getAttribute(IApplicationConstants.CURRUSER);
			String currentOrg = (String) req.getSession().getAttribute(IApplicationConstants.CURRORG);
			String reportUrl = req.getParameter("reportUrl");
			String changedObj = req.getParameter("changedObj");
			String changedValue = req.getParameter("changedValue");
			String tabCount = req.getParameter("count");
			String assessmentId = req.getParameter("assessmentId");
			
			// get all input controls for report
			List<InputControlTO> allInputControls = getInputControlList(reportUrl);
			Map<String, Object> parameters = getReportParametersFromRequest(req, allInputControls, new ReportFilterTO(), currentOrg, null);
			
			// get default parameters for logged-in user
			ReportFilterTO reportFilterTO = reportService.getDefaultFilter(allInputControls, currentUser, assessmentId, "", reportUrl);
			
			/** PATCH FOR STUDENT ROSTER */
			// Rank order is dependent on both Score type and Subtest selection - so for cascading we need to consider both
			List<ObjectValueTO> newObjectList = null;
			if(changedObj != null && changedObj.equals("p_Roster_Score_List")) {
				String[] selectedSubtests = req.getParameterValues("p_Roster_Subtest_MultiSelect");
				if(selectedSubtests != null) {
					newObjectList = new ArrayList<ObjectValueTO>();
					List<String> currentSubtests = Arrays.asList(selectedSubtests);
					List<ObjectValueTO> defaultSubtests = reportFilterTO.getP_Roster_Subtest_MultiSelect();
					for(ObjectValueTO objectVal : defaultSubtests) {
						if(currentSubtests.contains(objectVal.getValue())) {
							newObjectList.add(objectVal);
						}
					}
					// updating subtest list based on current selection
					reportFilterTO.setP_Roster_Subtest_MultiSelect(newObjectList);
				}
			} /* else {
				// need to check if we should reset the subtest list | if yes uncomment this section
				reportFilterTO = reportService.getDefaultFilter(allInputControls, currentUser, assessmentId, "", reportUrl);
			}
			*/
			/** END : PATCH FOR STUDENT ROSTER */
			
			/** PATCH FOR DEFAULT SUBTEST AND SCORE TYPE POPULATION (Multiselect) */
			String[] defaultValues = null;
			boolean checkDefault = false;
			List<String> defaultInputNames = new ArrayList<String>();
			Map<String, String[]> defaultInputValues = new HashMap<String, String[]>();
			try {
				for(InputControlTO inputControlTO : allInputControls) {
					for (IApplicationConstants.PATCH_FOR_SUBTEST subtest : IApplicationConstants.PATCH_FOR_SUBTEST.values()) {
					    if (subtest.name().equals(inputControlTO.getLabelId())) {
					    	defaultInputNames.add(inputControlTO.getLabelId());
					    	// get jasper report
							List<ReportTO> jasperReports = getJasperReportObject(reportUrl);
							JasperReport jasperReport = getMainReport(jasperReports);
					    	for(int i = 0; i < jasperReport.getParameters().length; i++) {
								if(inputControlTO.getLabelId().equals(jasperReport.getParameters()[i].getName())) {
									String value = jasperReport.getParameters()[i].getDefaultValueExpression().getText();
									value = value.replace("Arrays.asList(", "");
									value = value.replace(")", "");
									value = value.replace("\"", "");
									defaultValues = value.split(",");
									checkDefault = true;
									break;
								}
							}
					    	defaultInputValues.put(inputControlTO.getLabelId(), defaultValues);
					    	break;
					    }
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			/** END : PATCH FOR DEFAULT SUBTEST AND SCORE TYPE POPULATION (Multiselect) */
			
			
			// replace all parameters with jasper parameter string
			Map<String, String> replacableParams = new HashMap<String, String>();
			Iterator it = parameters.entrySet().iterator();
			try {
				while (it.hasNext()) {
				    Map.Entry pairs = (Map.Entry)it.next();
				    if(pairs.getValue() != null && pairs.getValue() instanceof String) {
				    	replacableParams.put(CustomStringUtil.getJasperParameterString((String) pairs.getKey()), 
				    			(String) pairs.getValue());
				    }
				}
			} catch (Exception e) {
				logger.log(IAppLogger.WARN, "Some error occuered getting cascading values.",e);
			}
			
			// get list of cascading input controls
			List<InputControlTO> allCascading = getCascadingInputControls(allInputControls, changedObj);
			List<ObjectValueTO> objectValueList = recurrsiveCascading(reportUrl, allInputControls, allCascading, 
					new ArrayList<ObjectValueTO>(), currentUser, changedObj, changedValue, replacableParams, 
					tabCount, reportFilterTO, defaultInputValues, defaultInputNames);
			String jsonStr = JsonUtil.convertToJson(objectValueList);
			jsonStr = CustomStringUtil.appendString("[" , jsonStr.replace("\"", "\\\"") , "]" );
			// return json string to page
			//res.setContentType("application/json");
			//res.getWriter().write( jsonStr );
			
			res.setContentType("text/plain");
			//res.getWriter().write( objectValueList.get(0).getValue() );
			String status = "Fail";
			String optionValue = "";
			String optionName = "";
			if(objectValueList != null && objectValueList.size() > 0) {
				status = "Success";
				optionValue = objectValueList.get(0).getValue();
				optionName = objectValueList.get(0).getName();
			}
			//res.getWriter().write( "{\"status\":\""+status+"\", \"target\":\""+optionName+"\", \"inputDom\":\""+optionValue+"\"}" );
			res.getWriter().write( "{\"status\":\""+status+"\", \"dom\":\""+jsonStr+"\"}" );
		} catch (SystemException e) {
			res.getWriter().write( "{\"status\":\"Fail\"}" );
			e.printStackTrace();
		} catch (Exception e) {
			res.getWriter().write( "{\"status\":\"Fail\"}" );
			e.printStackTrace();
		} 
		return null;
	}
	
	/**
	 * Get cascading input control lists for current report
	 * @param allInputControls
	 * @param allCascading
	 * @param objectValueList
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private List<ObjectValueTO> recurrsiveCascading(String reportUrl, List<InputControlTO> allInputControls, 
			List<InputControlTO> allCascading, List<ObjectValueTO> objectValueList, String userName, 
			String changedObject, String changedValue, Map<String, String> replacableParams, String tabCount, 
			ReportFilterTO reportFilterTO, Map<String, String[]> defaultValues, List<String> defaultInputNames) throws SystemException {
		if(allCascading != null) {
			List<InputControlTO> reCascadingInputControls = null;
			
			ObjectValueTO objectValueTo = null;
			InputControlFactory inputControlFact = new InputControlFactoryImpl();
			for(InputControlTO inputControlTO : allCascading) {
				// get list of values
				List<ObjectValueTO> objects = reportService.getValuesOfSingleInput(inputControlTO.getQuery(), 
						userName, changedObject, changedValue, replacableParams, reportFilterTO);
				boolean isMultiselect = false;
				if(IApplicationConstants.DATA_TYPE_COLLECTION.equals(inputControlTO.getType())) {
					isMultiselect = true;
				}
				String dom = inputControlFact.getOptionsForSelect(
							objects, inputControlTO.getLabel(), 
							inputControlTO.getLabelId(), reportUrl, isMultiselect, 
							defaultValues, defaultInputNames);
				objectValueTo = new ObjectValueTO();
				objectValueTo.setName( inputControlTO.getLabelId() );
				objectValueTo.setValue( dom ); // set the DOM
				objectValueList.add(objectValueTo);
				
				// check if other object depends on this
				/*List<InputControlTO> tempInputLists = getCascadingInputControls(allInputControls, inputControlTO.getLabelId());
				if(tempInputLists != null) {
					if(reCascadingInputControls == null) reCascadingInputControls = new ArrayList<InputControlTO>();
					for(InputControlTO ic : tempInputLists) {
						reCascadingInputControls.add(ic);
					}
				}*/
			}
			
			// call self if other dependency present
			/*if(reCascadingInputControls != null) {
				recurrsiveCascading(reportUrl, allInputControls, reCascadingInputControls, objectValueList, userName, changedObject, changedValue);
			}*/
		}
		return objectValueList;
	}
	
	/**
	 * Get list of related input controls (cascading)
	 * @param allInputControls
	 * @param changedObj
	 * @return
	 */
	private List<InputControlTO> getCascadingInputControls(List<InputControlTO> allInputControls, String changedObj) {
		List<InputControlTO> allCascading = null;
		String selectedId = CustomStringUtil.getJasperParameterString(changedObj);
		logger.log(IAppLogger.INFO, CustomStringUtil.appendString("Getting cascading i/p controls of : ", selectedId));
		if(allInputControls != null) {
			for(InputControlTO inputControlTO : allInputControls) {
				if(inputControlTO.getQuery() != null 
						&& (inputControlTO.getQuery().indexOf(selectedId) != -1 
								|| inputControlTO.getQuery().indexOf(changedObj) != -1)) {
					// selected id is cascaded with this input control
					if(!inputControlTO.getLabelId().equals(changedObj)) {
						if(allCascading == null) allCascading = new ArrayList<InputControlTO>();
						allCascading.add(inputControlTO);
					}
				}
			}
		}
		return allCascading;
	}
	
	/**
	 * Controller method for manage reports 
	 * @return
	 */
	@Secured({"ROLE_CTB"})
	@RequestMapping(value = "/manageReports", method = RequestMethod.GET)
	public ModelAndView openReportList() {
		logger.log(IAppLogger.INFO, "Enter: ReportController - openReportList");
		
		ModelAndView modelAndView = new ModelAndView("report/manageReports");
		List<ReportTO> reportList = reportService.getAllReportList();
		String replist = JsonUtil.convertToJsonAdmin(reportList);
		logger.log(IAppLogger.DEBUG, replist);
		modelAndView.addObject("reportList", reportList);
		modelAndView.addObject("test", "Test");
		logger.log(IAppLogger.INFO, "Exit: ReportController - openReportList");
		return modelAndView;
	}
	
	/**
	 * method to update report data
	 * @param req
	 * @param res
	 * @return
	 */
	@Secured({"ROLE_CTB"})
	@RequestMapping(value="/updateReport", method=RequestMethod.POST )
	public ModelAndView updateReport(HttpServletRequest req, HttpServletResponse res) {
		try {
			logger.log(IAppLogger.INFO, "Enter: ReportController - updateReport");
			
			String reportId = req.getParameter("reportId");
			String reportName = req.getParameter("reportName");
			String reportUrl = req.getParameter("reportUrl");
			String isEnabled = req.getParameter("reportStatus");
			String[] roles = req.getParameterValues("userRole");
			
			 if("1".equals(isEnabled))
			 {
				 isEnabled = IApplicationConstants.ACTIVE_FLAG;
			 }
			 else
			 {
				 isEnabled = IApplicationConstants.INACTIVE_FLAG;
			 }
			
			
			boolean isSaved = reportService.updateReport(reportId, reportName, reportUrl, isEnabled, roles);
			res.setContentType("text/plain");
			String status = "Fail";
			if(isSaved) {
				status = "Success";
			}
			res.getWriter().write( "{\"status\":\""+status+"\"}" );
			
			
			logger.log(IAppLogger.INFO, "Exit: ReportController - updateReport");
			
		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error Saving File", e);
		}
		return null;
	}
	
	/**
	 * This method returns ModelAndView of sideMenu
	 * @return
	 */
	@Secured({"ROLE_ACSI", "ROLE_SCHOOL", "ROLE_CLASS", "ROLE_ADMIN", "ROLE_PARENT"})
	@RequestMapping(value = "/fetchReportMenu", method = RequestMethod.GET)
	public ModelAndView fetchReportMenu(HttpServletRequest req, HttpServletResponse response) {
		logger.log(IAppLogger.INFO, "Enter: ReportController - fetchReportMenu");
		boolean parentReports = false;
		if(IApplicationConstants.TRUE.equals(req.getSession().getAttribute(IApplicationConstants.PARENT_REPORT))) {
			parentReports = true;
		}
				
		ModelAndView modelAndView = new ModelAndView("common/navigableMenu");
		if(!parentReports) {
			List<AssessmentTO> assessmentList = reportService.getAssessments(parentReports);
			modelAndView.addObject("assessmentList", assessmentList);
			modelAndView.addObject("test", "Test");
		}
		logger.log(IAppLogger.INFO, "Exit: ReportController - fetchReportMenu");
		return modelAndView;
	}
	
	/**
	 * This method is responsible for downloading report
	 * @param type
	 * @param reportUrl
	 * @param token
	 * @param assessmentId
	 * @param req
	 * @param response
	 */
	@Secured({"ROLE_ACSI", "ROLE_SCHOOL", "ROLE_CLASS", "ROLE_ADMIN", "ROLE_PARENT"})
	@RequestMapping(value="/download", method = RequestMethod.GET)
	public void download(HttpServletRequest req, HttpServletResponse response) {
		
		String type = req.getParameter("type");
		String reportUrl = req.getParameter("reportUrl");
		String token = req.getParameter("token");
		String assessmentId = req.getParameter("assessmentId");
		String filter = req.getParameter("filter");
		String printerFriendly = req.getParameter("printerFriendly");
		boolean isPrinterFriendly = true;
		if(IApplicationConstants.FLAG_N.equals(printerFriendly)) {
			isPrinterFriendly = false;
		}
		//patch for xls export (to remove "99" print from it)
		
		/*if(IApplicationConstants.EXPORT_AS_XLS.equals(type)) {
			isPrinterFriendly = false;
		}*/
		
		// is it drilldown report?
		String drilldown = req.getParameter("drillDown");
		if(IApplicationConstants.TRUE.equals(drilldown)) {
			filter = drilldown;
			
		}
		// is it invitation pdf ?
		if(IApplicationConstants.IS_INVITATION_PDF.equals(assessmentId)){
			req.getSession().setAttribute(IApplicationConstants.CURR_ASSESSMENT, assessmentId);
		}
					
		logger.log(IAppLogger.INFO, CustomStringUtil.appendString("Downloading report as ", type));
		try {
			// get jasper print object
			JasperPrint jasperPrint = getJasperPrintObject(filter, reportUrl, assessmentId, req, drilldown, isPrinterFriendly);
		
			downloadService.download(type, token, response, jasperPrint);
		} catch (Exception e) {
			showError(response, "Error Downloading report. Please try after some time or contact system administrator.");
			logger.log(IAppLogger.ERROR, CustomStringUtil.appendString("Error while downloading repot : ", reportUrl), e);
		}
	}
	
	/**
	 * This method is called from outside this web application (like bulk PDF generation)
	 * Login not required to access this
	 * @param req
	 * @param response
	 */
	@RequestMapping(value="/icDownload", method = RequestMethod.GET)
	public void icDownload(HttpServletRequest req, HttpServletResponse response) {
		download(req, response);
	}
	
	private void showError(HttpServletResponse response, String message) {
		response.setContentType("text/plain");
		try {
			response.getWriter().write( message );
		} catch (IOException e) {
			logger.log(IAppLogger.ERROR, message, e);
		}
	}
	
	
	/**
	 * Delete selected report
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@Secured({"ROLE_CTB"})
	@RequestMapping(value = "/deleteReport", method = RequestMethod.GET)
	public ModelAndView deleteReport(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		ModelAndView modelAndView = null;
		String reportId = request.getParameter("reportId"); 
		//List<BaseTO> OrgTOs = new ArrayList<BaseTO>();
		try {
		logger.log(IAppLogger.INFO, "Enter: ReportController - deleteReport");
		String status = "Fail";
		boolean isDeleted=false;
		if (reportId != null) {
			isDeleted= reportService.deleteReport(reportId);
		}
		if(isDeleted) {
			//roleList = adminService.getRoleDetails();
			status = "Success";
		}
		response.getWriter().write( "{\"status\":\""+status+"\"}" ); 
		
		}
		catch (Exception exception) {
			logger.log(IAppLogger.ERROR, exception.getMessage(), exception);
		} finally {
			logger.log(IAppLogger.INFO, "Exit: ReportController - deleteReport");
		}
		
		return null;
		
	}
	
	
	/**
	 * Add new dashboard to selected org
	 * @param req
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "/addDashboard", method = RequestMethod.POST)
	public ModelAndView addDashboard(HttpServletRequest req,
			HttpServletResponse res) {
		logger.log(IAppLogger.INFO, "Enter: ReportController - addDashboard");
		try {
			
			ReportTO reportTo=null;
			List<ReportTO> ReportTOs = new ArrayList<ReportTO>();
			String reportName = (String) req.getParameter("reportName");
			String reportDescription = (String) req.getParameter("reportDescription");
			String reportType = (String) req.getParameter("reportType");
			String reportUri = (String) req.getParameter("reportUri");
			String assessmentType = (String) req.getParameter("assessmentType");
			String reportStatus = (String) req.getParameter("reportStatus");
			if ("1".equals(reportStatus)) {
				reportStatus = IApplicationConstants.ACTIVE_FLAG;
			} else {
				reportStatus = IApplicationConstants.INACTIVE_FLAG;
			}
			String[] userRoles = req.getParameterValues("userRole");
			String status = "Fail";
			
			reportTo = reportService.addNewDashboard(reportName, reportDescription, reportType,
					reportUri, assessmentType, reportStatus, userRoles);
					res.setContentType("text/plain");
			
			if (reportTo != null) {
				ReportTOs.add(reportTo);
				String userJsonString = JsonUtil.convertToJsonAdmin(ReportTOs);
				logger.log(IAppLogger.DEBUG, "Added dashboard details .................");
				logger.log(IAppLogger.DEBUG, userJsonString);
				res.getWriter().write(userJsonString);
			} else {
				status = "Faliure";
				res.getWriter().write("{\"status\":\"" + status + "\"}");
			}
					
						
			logger.log(IAppLogger.INFO, "Exit: ReportController - addDashboard");

		} catch (Exception e) {
			logger.log(IAppLogger.ERROR, "Error Saving File", e);
		}
		return null;
	}
	
	
}
