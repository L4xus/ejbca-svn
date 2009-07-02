<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>



	<h:panelGroup>
		<h:outputText value="#{web.text.CRLUPDATEWORKERSETTINGS}"/>
	</h:panelGroup>
	<h:panelGroup>
		<f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
	</h:panelGroup>
	<h:panelGroup>
		<h:outputText value="#{web.text.CASTOCHECK}"/>
	</h:panelGroup>
	<h:panelGroup>							
		<h:selectManyListbox id="crlUpdateCASelect" value="#{editService.baseWorkerType.selectedCANamesToCheck}" size="10">
			<f:selectItems value="#{editService.availableCAsWithAnyOption}"/>
		</h:selectManyListbox>		
	</h:panelGroup>	
