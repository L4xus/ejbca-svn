<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ page pageEncoding="ISO-8859-1"%>
<% response.setContentType("text/html; charset="+org.ejbca.config.WebConfiguration.getWebContentEncoding()); %>
<%@page errorPage="/errorpage.jsp" import="org.ejbca.core.model.ra.raadmin.GlobalConfiguration,org.ejbca.ui.web.RequestHelper,
                                           org.ejbca.ui.web.admin.configuration.EjbcaJSFHelper" %>
<jsp:useBean id="ejbcawebbean" scope="session" class="org.ejbca.ui.web.admin.configuration.EjbcaWebBean" />
<jsp:setProperty name="ejbcawebbean" property="*" /> 
<%   // Initialize environment
 GlobalConfiguration globalconfiguration = ejbcawebbean.initialize(request,"/administrator"); 
 EjbcaJSFHelper helpbean = EjbcaJSFHelper.getBean();
 helpbean.setEjbcaWebBean(ejbcawebbean);
 helpbean.authorizedToServicesPages();
%>
<html>
<head>
  <title><%= globalconfiguration.getEjbcaTitle() %></title>
  <base href="<%= ejbcawebbean.getBaseUrl() %>">
  <link rel=STYLESHEET href="<%= ejbcawebbean.getCssFile() %>">
  <meta http-equiv="Content-Type" content="text/html; charset=<%= org.ejbca.config.WebConfiguration.getWebContentEncoding() %>">
  <link href="/themes/default_theme.css" rel="stylesheet" type="text/css"/>
  </head>


<f:view>
<body>
<h1><h:outputText value="#{web.text.EDITSERVICES}"/></h1>
<p>
	<h:messages styleClass="alert" layout="table"/>
	</p>
<h3><h:outputText value="#{web.text.CURRENTSERVICES}"/></h3>
	<h:form>
		<h:selectOneListbox id="listServices" value="#{listServicesManagedBean.selectedServiceName}" style="width: 50em" size="15">
			<f:selectItems value="#{listServicesManagedBean.availableServices}"/>
		</h:selectOneListbox>
		<p>
	    <h:commandButton id="editButton" action="#{listServicesManagedBean.editService}" value="#{web.text.EDITSERVICE}"/>
	    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	    <h:commandButton id="deleteButton" action="#{listServicesManagedBean.deleteService}" value="#{web.text.DELETESERVICE}" onclick="return confirm('#{web.text.AREYOUSURE}');"/>
		</p>
		<h3><h:outputText value="#{web.text.ADD}"/></h3>
		<h:inputText id="newServiceName" value="#{listServicesManagedBean.newServiceName}" size="40"/>
		<h:commandButton id="addButton" action="#{listServicesManagedBean.addService}" value="#{web.text.ADD}"/>
		<br/>
		<h:commandButton id="renameButton" action="#{listServicesManagedBean.renameService}" value="#{web.text.RENAMESELECTED}"/>&nbsp;&nbsp;&nbsp;&nbsp;
		<h:commandButton id="cloneButton" action="#{listServicesManagedBean.cloneService}" value="#{web.text.USESELECTEDASTEMPLATE}"/>
		<br/>
		<p></p>
	</h:form>

	<%	// Include Footer 
	String footurl = globalconfiguration.getFootBanner(); %>
   
	<jsp:include page="<%= footurl %>" />

</body>
</f:view>
</html>
