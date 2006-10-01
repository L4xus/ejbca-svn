<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<%@ page pageEncoding="ISO-8859-1"%>
<%@ page contentType="text/html; charset=@page.encoding@" %>
<%@page errorPage="/errorpage.jsp" import="org.ejbca.core.model.ra.raadmin.GlobalConfiguration,org.ejbca.ui.web.RequestHelper,
                                           org.ejbca.ui.web.admin.configuration.EjbcaJSFHelper, org.ejbca.ui.web.admin.services.EditServiceManagedBean" %>
<jsp:useBean id="ejbcawebbean" scope="session" class="org.ejbca.ui.web.admin.configuration.EjbcaWebBean" />
<jsp:setProperty name="ejbcawebbean" property="*" /> 
<%   // Initialize environment
 GlobalConfiguration globalconfiguration = ejbcawebbean.initialize(request,"/administrator"); 
 EjbcaJSFHelper helpbean = EjbcaJSFHelper.getBean();
 helpbean.setEjbcaWebBean(ejbcawebbean);
 helpbean.authorizedToServicesPages();
 
 String workerPage = EditServiceManagedBean.getBean().getServiceConfigurationView().getWorkerType().getJSFSubViewPage();
 String intervalPage = EditServiceManagedBean.getBean().getServiceConfigurationView().getIntervalType().getJSFSubViewPage();
 String actionPage = EditServiceManagedBean.getBean().getServiceConfigurationView().getActionType().getJSFSubViewPage();
%>
<html>
<head>
  <title><%= globalconfiguration.getEjbcaTitle() %></title>
  <base href="<%= ejbcawebbean.getBaseUrl() %>">
  <link rel=STYLESHEET href="<%= ejbcawebbean.getCssFile() %>">
  <meta http-equiv="Content-Type" content="text/html; charset=<%= RequestHelper.getDefaultContentEncoding() %>">
  <link href="/themes/default_theme.css" rel="stylesheet" type="text/css"/>
  </head>


<f:view>
<body>
<div align="center"><h:outputText value="#{web.text.EDITSERVICE}" styleClass="header" style="text-align: center"/></div>
<div align="center"><h:outputText value="#{web.text.NAME}" styleClass="subheader" style="text-align: center"/><f:verbatim> : </f:verbatim><h:outputText value="#{editService.serviceName}" styleClass="subheader" style="text-align: center"/></div>

  <div align="right">  <h:commandLink id="backToServices" action="listservices" immediate="true" style="text-align: right">
		<h:outputText value="#{web.text.BACKTOSERVICES}" style="text-align: right"/>
	</h:commandLink>
  </div>	
<h:form>

<h:panelGrid width="100%" columns="2" rowClasses="jsfrow1, jsfrow2">
	<h:panelGroup>
		<h:outputText value="#{web.text.SELECTWORKER}"/>
	</h:panelGroup>
	<h:panelGroup>
		<h:selectOneMenu value="#{editService.selectedWorker}" valueChangeListener="#{editService.changeWorker}">
			<f:selectItems value="#{editService.availableWorkers}"/>
		</h:selectOneMenu>
	</h:panelGroup>
    <jsp:include page="<%= workerPage %>"/>
    <h:panelGroup>
		<h:outputText value="#{web.text.SELECTINTERVAL}"/>
	</h:panelGroup>
	<h:panelGroup>
		<h:selectOneMenu value="#{editService.selectedInterval}" valueChangeListener="#{editService.changeInterval}">
			<f:selectItems value="#{editService.availableIntervals}"/>
		</h:selectOneMenu>			
	</h:panelGroup>
    <jsp:include page="<%= intervalPage %>"/>
    	<h:panelGroup>
		<h:outputText value="#{web.text.SELECTACTION}"/>
	</h:panelGroup>
	<h:panelGroup>
		<h:selectOneMenu value="#{editService.selectedAction}" valueChangeListener="#{editService.changeAction}">
			<f:selectItems value="#{editService.availableActions}"/>
		</h:selectOneMenu>			
	</h:panelGroup>
    <jsp:include page="<%= actionPage %>"/>        
	<h:panelGroup>
		<h:outputText value="#{web.text.ACTIVE}"/>
	</h:panelGroup>
	<h:panelGroup>

			<h:selectBooleanCheckbox id="activeCheckbox" value="#{editService.serviceConfigurationView.active}"/>

	</h:panelGroup>
	<h:panelGroup>
		<h:outputText value="#{web.text.DESCRIPTION}"/>
	</h:panelGroup>
	<h:panelGroup>
		<h:inputTextarea id="descriptionTextArea" value="#{editService.serviceConfigurationView.description}" rows="6" cols="40"/>
	</h:panelGroup>
	<h:panelGroup>
		
	</h:panelGroup>
	<h:panelGroup>
		<h:commandButton id="saveButton" action="#{editService.save}" value="#{web.text.SAVE}"/>		
		<f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
		<h:commandButton id="cancelButton" action="#{editService.cancel}" value="#{web.text.CANCEL}"/>		
	</h:panelGroup>
</h:panelGrid>
</h:form>
</body>
</f:view>
</html>

