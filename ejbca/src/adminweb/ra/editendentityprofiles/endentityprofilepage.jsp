<%
  profiledata = ejbcarabean.getTemporaryEndEntityProfile();
  if(profiledata == null){
    profiledata = ejbcarabean.getEndEntityProfile(profile);
  }
   TreeMap certificateprofilenames = ejbcawebbean.getInformationMemory().getAuthorizedEndEntityCertificateProfileNames();
   boolean used = false;

   String[] subjectfieldtexts = {"","","", "OLDEMAILDN2", "UID", "COMMONNAME", "SERIALNUMBER1", 
                                 "GIVENNAME2", "INITIALS", "SURNAME","TITLE","ORGANIZATIONUNIT","ORGANIZATION",
                                "LOCALE","STATE","DOMAINCOMPONENT","COUNTRY"
                                , "RFC822NAME", "DNSNAME", "IPADDRESS", "OTHERNAME", "UNIFORMRESOURCEID", "X400ADDRESS", "DIRECTORYNAME"
                                ,"EDIPARTNAME", "REGISTEREDID","","","","","","","","","","","UPN"};

   int[] subjectdnfields = {EndEntityProfile.OLDDNE,EndEntityProfile.UID,EndEntityProfile.COMMONNAME,EndEntityProfile.SN
                           ,EndEntityProfile.GIVENNAME,EndEntityProfile.INITIALS,EndEntityProfile.SURNAME
                           ,EndEntityProfile.TITLE,EndEntityProfile.ORGANIZATIONUNIT,EndEntityProfile.ORGANIZATION
                           ,EndEntityProfile.LOCALE,EndEntityProfile.STATE,EndEntityProfile.DOMAINCOMPONENT
                           ,EndEntityProfile.COUNTRY};

   int[] subjectaltnamefields = {EndEntityProfile.RFC822NAME
                                 ,EndEntityProfile.DNSNAME
                                 ,EndEntityProfile.IPADDRESS
                                 //,EndEntityProfile.OTHERNAME
                                 ,EndEntityProfile.UNIFORMRESOURCEID
                                 //,EndEntityProfile.X400ADDRESS
                                 //,EndEntityProfile.DIRECTORYNAME
                                 //,EndEntityProfile.EDIPARTNAME
                                 //,EndEntityProfile.REGISTEREDID
                                 ,EndEntityProfile.UPN};


   String[] tokentexts = RAInterfaceBean.tokentexts;
   int[] tokenids = RAInterfaceBean.tokenids;
   String[] hardtokenissueraliases = new String[0];
   int[] hardtokenissuerids = new int[0];

   HashMap caidtonamemap = cabean.getCAIdToNameMap();
   Collection authorizedcas = ejbcawebbean.getAuthorizedCAIds();

   if(globalconfiguration.getIssueHardwareTokens()){
      TreeMap hardtokenprofiles = ejbcawebbean.getInformationMemory().getHardTokenProfiles();      

      tokentexts = new String[RAInterfaceBean.tokentexts.length + hardtokenprofiles.keySet().size()];
      tokenids   = new int[tokentexts.length];
      for(int i=0; i < RAInterfaceBean.tokentexts.length; i++){
        tokentexts[i]= RAInterfaceBean.tokentexts[i];
        tokenids[i] = RAInterfaceBean.tokenids[i];
      }
      Iterator iter = hardtokenprofiles.keySet().iterator();
      int index=0;
      while(iter.hasNext()){       
        String name = (String) iter.next();
        tokentexts[index+RAInterfaceBean.tokentexts.length]= name;
        tokenids[index+RAInterfaceBean.tokentexts.length] = ((Integer) hardtokenprofiles.get(name)).intValue();
        index++;
      }

      hardtokenissueraliases = new String[ejbcawebbean.getInformationMemory().getHardTokenIssuers().keySet().size()];
      Iterator issueriter = ejbcawebbean.getInformationMemory().getHardTokenIssuers().keySet().iterator();
      
      hardtokenissuerids = new int[hardtokenissueraliases.length];
      for(int i=0; i < hardtokenissueraliases.length; i++){
        hardtokenissueraliases[i] = (String) issueriter.next();
        hardtokenissuerids[i]= tokenbean.getHardTokenIssuerId(hardtokenissueraliases[i]);
      }
   }

   boolean emailfieldexists = false;

   int row = 0;
%>
<SCRIPT language="JavaScript">

  <!-- // Method to check all textfields for valid input -->
<!--  
    var numbersubjectdnfields = <%= profiledata.getSubjectDNFieldOrderLength()%>
    var dnfieldtypes = new Array(<%= profiledata.getSubjectDNFieldOrderLength()%>);
    <% for(int i=0; i < profiledata.getSubjectDNFieldOrderLength(); i++){ %>
    dnfieldtypes[<%=i %>] = <%= profiledata.getSubjectDNFieldsInOrder(i)[EndEntityProfile.FIELDTYPE]%>
    <%}%>

    var numbersubjectaltnamesfields = <%= profiledata.getSubjectAltNameFieldOrderLength()%>
    var altnamesfieldtypes = new Array(<%= profiledata.getSubjectAltNameFieldOrderLength()%>);
    <% for(int i=0; i < profiledata.getSubjectAltNameFieldOrderLength(); i++){ %>
    altnamesfieldtypes[<%=i %>] = <%=profiledata.getSubjectAltNameFieldsInOrder(i)[EndEntityProfile.FIELDTYPE]%>
    <%}%>

function checkallfields(){
    var illegalfields = 0;
    var fieldname;

    if(!checkfieldforlegalcharswithchangeable("document.editprofile.<%=TEXTFIELD_USERNAME%>","<%= ejbcawebbean.getText("ONLYCHARACTERS2") %>"))
      illegalfields++;
 


    for(var i=0; i < numbersubjectdnfields; i++){
      if( dnfieldtypes[i] != <%= EndEntityProfile.OLDDNE %>){
        fieldname = "document.editprofile.<%=TEXTFIELD_SUBJECTDN%>" + i;
        if(!checkfieldforlegaldncharswithchangeable(fieldname,"<%= ejbcawebbean.getText("ONLYDNCHARACTERS") %>"))
          illegalfields++;
      }    
    } 

    for(var i=0; i < numbersubjectaltnamesfields; i++){
      if(altnamesfieldtypes[i] != <%= EndEntityProfile.RFC822NAME%>){
        fieldname = "document.editprofile.<%=TEXTFIELD_SUBJECTALTNAME%>"+i;
        if(!checkfieldforlegaldncharswithchangeable(fieldname,"<%= ejbcawebbean.getText("ONLYDNCHARACTERS") %>"))
          illegalfields++;
      }    
    } 
  

    if(!checkfieldforlegalemailcharswithoutatwithchangeable("document.editprofile.<%=TEXTFIELD_EMAIL%>","<%= ejbcawebbean.getText("ONLYEMAILCHARSNOAT") %>"))
      illegalfields++;
 
    if(document.editprofile.<%= SELECT_DEFAULTCERTPROFILE %>.options.selectedIndex == -1){
      alert("<%=  ejbcawebbean.getText("ADEFAULTCERTPROFILE") %>");
      illegalfields++;
    }
    <%    if(globalconfiguration.getIssueHardwareTokens()){ %>
    if(document.editprofile.<%= SELECT_DEFAULTHARDTOKENISSUER %>.options.selectedIndex == -1 && document.editprofile.<%=CHECKBOX_USE_HARDTOKENISSUERS %>.checked){
      alert("<%=  ejbcawebbean.getText("ADEFAULTHARDTOKENISSUER") %>");
      illegalfields++;
    }
    <% } %>

    if(document.editprofile.<%=CHECKBOX_USE_SENDNOTIFICATION%>.checked){            
      if(trim(document.editprofile.<%=TEXTFIELD_NOTIFICATIONSENDER%>.value) == ""){
        alert("<%=  ejbcawebbean.getText("MUSTFILLINANOTIFICATIONSENDER") %>");
        illegalfields++;
      } 

      if(!checkfieldforlegalemailchars("document.editprofile.<%=TEXTFIELD_NOTIFICATIONSENDER%>","<%= ejbcawebbean.getText("NOTIFICATIONSENDERNOTVALID") %>"))
      illegalfields++;

      if(trim(document.editprofile.<%=TEXTFIELD_NOTIFICATIONSUBJECT%>.value) == ""){
        alert("<%=  ejbcawebbean.getText("MUSTFILLINANOTIFICATIONSUBJECT") %>");
        illegalfields++;
      } 
      if(trim(document.editprofile.<%=TEXTAREA_NOTIFICATIONMESSAGE%>.value) == ""){
        alert("<%=  ejbcawebbean.getText("MUSTFILLINANOTIFICATIONMESSAGE") %>");
        illegalfields++;
      } 
    }

    if(illegalfields == 0){
      document.editprofile.<%= CHECKBOX_CLEARTEXTPASSWORD %>.disabled = false;
      document.editprofile.<%= CHECKBOX_REQUIRED_CLEARTEXTPASSWORD %>.disabled = false; 
      document.editprofile.<%= TEXTFIELD_EMAIL %>.disabled = false;
      document.editprofile.<%= CHECKBOX_USE_EMAIL %>.disabled = false;
      document.editprofile.<%= CHECKBOX_REQUIRED_ADMINISTRATOR %>.disabled = false;
      document.editprofile.<%= CHECKBOX_ADMINISTRATOR %>.disabled = false;
      <% if(globalconfiguration.getEnableKeyRecovery()){ %>
      document.editprofile.<%= CHECKBOX_REQUIRED_KEYRECOVERABLE %>.disabled = false;
      document.editprofile.<%= CHECKBOX_KEYRECOVERABLE %>.disabled = false;
      <% } %>
      document.editprofile.<%= CHECKBOX_REQUIRED_SENDNOTIFICATION %>.disabled = false;
      document.editprofile.<%= CHECKBOX_SENDNOTIFICATION %>.disabled = false;      
    }


     return illegalfields == 0;  
} 

function checkusecheckbox(usefield, value, required){
  var usebox = eval("document.editprofile." + usefield);
  var valuefield = eval("document.editprofile." + value);
  var reqbox = eval("document.editprofile." + required);
  if(usebox.checked){
    valuefield.disabled = false;
    reqbox.disabled = false;
  }
  else{
    valuefield.checked=false;
    valuefield.disabled = true;
    reqbox.checked = false;
    reqbox.disabled = true;
  }
}

function usenotificationchange(){
  if(document.editprofile.<%=CHECKBOX_USE_SENDNOTIFICATION%>.checked){
     document.editprofile.<%=TEXTFIELD_NOTIFICATIONSENDER%>.disabled = false;
     document.editprofile.<%=TEXTFIELD_NOTIFICATIONSUBJECT%>.disabled = false;
     document.editprofile.<%=TEXTAREA_NOTIFICATIONMESSAGE%>.disabled = false;
  }else{
     document.editprofile.<%=TEXTFIELD_NOTIFICATIONSENDER%>.disabled = true;
     document.editprofile.<%=TEXTFIELD_NOTIFICATIONSUBJECT%>.disabled = true;
     document.editprofile.<%=TEXTAREA_NOTIFICATIONMESSAGE%>.disabled = true;                  
  }
}

function checkautogenbox(){
  var usebox = eval("document.editprofile.<%= CHECKBOX_USE_PASSWORD %>");
  var valuefield = eval("document.editprofile.<%= TEXTFIELD_PASSWORD %>");
  var reqbox = eval("document.editprofile.<%= CHECKBOX_REQUIRED_PASSWORD %>");
  var modifyablebox = eval("document.editprofile.<%= CHECKBOX_MODIFYABLE_PASSWORD %>");

  if(usebox.checked){
    valuefield.value = "";
    valuefield.disabled = true;
    reqbox.checked = false;
    reqbox.disabled = true;
    modifyablebox.checked = false;
    modifyablebox.disabled = true;
  }
  else{    
    valuefield.disabled = false;    
    reqbox.disabled = false;
    modifyablebox.disabled = false;
  }
}

function checkusehardtokenissuers(){
  if(document.editprofile.<%=CHECKBOX_USE_HARDTOKENISSUERS %>.checked){
    document.editprofile.<%=SELECT_DEFAULTHARDTOKENISSUER %>.disabled = false;
    document.editprofile.<%=SELECT_AVAILABLEHARDTOKENISSUERS %>.disabled = false;
  }
  else{
    document.editprofile.<%=SELECT_DEFAULTHARDTOKENISSUER %>.disabled = true;
    document.editprofile.<%=SELECT_AVAILABLEHARDTOKENISSUERS %>.disabled = true;
  }
}


function checkusetextfield(usefield, value, required, change){
  var usebox = eval("document.editprofile." + usefield);
  var valuefield = eval("document.editprofile." + value);
  var reqbox = eval("document.editprofile." + required);
  var changebox = eval("document.editprofile." + change);

  if(usebox.checked){
    valuefield.disabled = false;
    reqbox.disabled = false;
    changebox.disabled = false;
  }
  else{
    valuefield.value = "";
    valuefield.disabled = true;
    reqbox.checked = false;
    reqbox.disabled = true;
    changebox.checked = false;
    changebox.disabled = true;
  }
}

function checkemailfield(reqfield){
  var box = eval("document.editprofile." + reqfield);

  if(box.checked){
      document.editprofile.<%= CHECKBOX_REQUIRED_EMAIL %>.checked = true;    
  }

}

function checkuseemailfield(){
  if(document.editprofile.<%= CHECKBOX_USE_SENDNOTIFICATION %>.checked)
    document.editprofile.<%= CHECKBOX_USE_EMAIL %>.checked = true;    
}
-->

</SCRIPT>
<div align="center"> 
  <h2><%= ejbcawebbean.getText("EDITPROFILE") %><br>
  </h2>
  <h3><%= ejbcawebbean.getText("PROFILE") + " : " + profile %> </h3>
</div>
<form name="editprofile" method="post" action="<%=THIS_FILENAME %>">
  <input type="hidden" name='<%= ACTION %>' value='<%=ACTION_EDIT_PROFILE %>'>
  <input type="hidden" name='<%= HIDDEN_PROFILENAME %>' value='<%=profile %>'>
  <table width="100%" border="0" cellspacing="3" cellpadding="3">
    <tr id="Row<%=row++%2%>"> 
      <td width="15%" valign="top">
         &nbsp;
      </td>
      <td width="35%" valign="top"> 
        <div align="left"> 
          <h3>&nbsp;</h3>
        </div>
      </td>
      <td width="50%" valign="top"> 
        <div align="right">
        <A href="<%=THIS_FILENAME %>"><u><%= ejbcawebbean.getText("BACKTOPROFILES") %></u></A>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
   <!--     <A  onclick='displayHelpWindow("<%= ejbcawebbean.getHelpfileInfix("ra_help.html") + "#profiles"%>")'>
        <u><%= ejbcawebbean.getText("HELP") %></u> </A></div> -->
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%"  align="right"> 
        <%= ejbcawebbean.getText("USERNAME") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <input type="text" name="<%=TEXTFIELD_USERNAME%>" size="40" maxlength="1024" 
           value="<%= profiledata.getValue(EndEntityProfile.USERNAME,0)%>"><br>
           <%= ejbcawebbean.getText("REQUIRED") %>
           <input type="checkbox" name="<%=CHECKBOX_REQUIRED_USERNAME %>" value="<%=CHECKBOX_VALUE %>" 
           <%  if(profiledata.isRequired(EndEntityProfile.USERNAME,0))
                 out.write(" CHECKED ");
           %>> 
        &nbsp;&nbsp;<%= ejbcawebbean.getText("MODIFYABLE") %> 
        <input type="checkbox" name="<%=CHECKBOX_MODIFYABLE_USERNAME %>" value="<%=CHECKBOX_VALUE %>" 
           <%if(profiledata.isModifyable(EndEntityProfile.USERNAME,0))
                 out.write("CHECKED");
           %>> 
      </td>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%"  align="right"> 
        <%= ejbcawebbean.getText("PASSWORD") %> <br> &nbsp;
      </td>
      <td width="70%"> 
        <% used = profiledata.getUse(EndEntityProfile.PASSWORD,0);   %>
        <input type="text" name="<%=TEXTFIELD_PASSWORD%>" size="40" maxlength="1024" 
           value="<%= profiledata.getValue(EndEntityProfile.PASSWORD,0)%>" <% if(!used) out.write(" disabled "); %><br>
           <%= ejbcawebbean.getText("AUTOGENERATED") %>
           <input type="checkbox" name="<%=CHECKBOX_USE_PASSWORD %>" value="<%=CHECKBOX_VALUE %>" onclick='checkautogenbox()'
           <% if(!used)
                 out.write("CHECKED");
           %>> 
           <%= ejbcawebbean.getText("REQUIRED") %>
           <input type="checkbox" name="<%=CHECKBOX_REQUIRED_PASSWORD %>" value="<%=CHECKBOX_VALUE %>" <% if(!used) out.write(" disabled "); %> 
           <% if(profiledata.isRequired(EndEntityProfile.PASSWORD,0))
                 out.write("CHECKED");
           %>> 
      </td>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%"  align="right"> 
        <%= ejbcawebbean.getText("USEINBATCH") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <% used = false;
             if(profiledata.getUse(EndEntityProfile.CLEARTEXTPASSWORD,0)) 
                 used=true; %>
        <%= ejbcawebbean.getText("USE") %> 
        <input type="checkbox" name="<%=CHECKBOX_USE_CLEARTEXTPASSWORD %>" value="<%=CHECKBOX_VALUE %>" onclick="checkusecheckbox('<%=CHECKBOX_USE_CLEARTEXTPASSWORD %>', '<%=CHECKBOX_CLEARTEXTPASSWORD%>', '<%=CHECKBOX_REQUIRED_CLEARTEXTPASSWORD %>')"
           <%  if(used)
                 out.write(" CHECKED ");
           %>><br>
        <%= ejbcawebbean.getText("DEFAULT") %> 
        <input type="checkbox" name="<%=CHECKBOX_CLEARTEXTPASSWORD%>"  value="<%=CHECKBOX_VALUE %>" <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.getValue(EndEntityProfile.CLEARTEXTPASSWORD,0).equals(EndEntityProfile.TRUE) && used)
                 out.write(" CHECKED ");
           %>> &nbsp;&nbsp; 
        <%= ejbcawebbean.getText("REQUIRED") %>
        <input type="checkbox" name="<%=CHECKBOX_REQUIRED_CLEARTEXTPASSWORD %>" value="<%=CHECKBOX_VALUE %>" <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.isRequired(EndEntityProfile.CLEARTEXTPASSWORD,0) && used)
                 out.write("CHECKED");
           %>> 

      </td>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
        <%= ejbcawebbean.getText("SELECTFORREMOVAL") %>
      </td>
      <td width="25%"  align="right"> 
        <%= ejbcawebbean.getText("SUBJECTDNFIELDS") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_ADDSUBJECTDN %>" size="1" >
            <% 
               for(int i=0; i < subjectdnfields.length; i++){ %>
           <option  value='<%= subjectdnfields[i]%>'><%= ejbcawebbean.getText(subjectfieldtexts[subjectdnfields[i]]) %>
           </option>
            <% } %>
        </select>
        &nbsp;<input type="submit" name="<%= BUTTON_ADDSUBJECTDN %>" value="<%= ejbcawebbean.getText("ADD") %>">   
      </td> 
    </tr>
    <% numberofsubjectdnfields = profiledata.getSubjectDNFieldOrderLength();
       for(int i=0; i < numberofsubjectdnfields; i++){
         fielddata =  profiledata.getSubjectDNFieldsInOrder(i);
    %>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
        <input type="checkbox" name="<%=CHECKBOX_SELECTSUBJECTDN + i%>" value="<%=CHECKBOX_VALUE %>">      
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText(subjectfieldtexts[fielddata[EndEntityProfile.FIELDTYPE]]) %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <% if(fielddata[EndEntityProfile.FIELDTYPE] != EndEntityProfile.OLDDNE ){ %>
        <input type="text" name="<%=TEXTFIELD_SUBJECTDN + i%>" size="40" maxlength="1024"
           value="<% if(profiledata.getValue(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]) != null) out.write(profiledata.getValue(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER])); %>"><br> 
        <%= ejbcawebbean.getText("REQUIRED") %>
        <input type="checkbox" name="<%=CHECKBOX_REQUIRED_SUBJECTDN + i %>" value="<%=CHECKBOX_VALUE %>"
           <% if(profiledata.isRequired(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]))
                 out.write("CHECKED");
           %>> 
        &nbsp;&nbsp;<%= ejbcawebbean.getText("MODIFYABLE") %> 
        <input type="checkbox" name="<%=CHECKBOX_MODIFYABLE_SUBJECTDN + i %>" value="<%=CHECKBOX_VALUE %>" 
           <% if(profiledata.isModifyable(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]))
                 out.write("CHECKED");
           %>> 
        <% }
           else{ 
             emailfieldexists=true; 
              %>
           <%= ejbcawebbean.getText("REQUIRED") %>
        <input type="checkbox" name="<%=CHECKBOX_REQUIRED_SUBJECTDN + i %>" value="<%=CHECKBOX_VALUE %>" onclick='checkemailfield("<%=CHECKBOX_REQUIRED_SUBJECTDN + i %>")'
           <% if(profiledata.isRequired(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]))
                 out.write("CHECKED");
           %>>&nbsp;<%=ejbcawebbean.getText("SEEEMAILCONFIGURATION") %>
        <% }%>
      </td>
    </tr>
   <% } %>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
        <input type="submit" name="<%= BUTTON_DELETESUBJECTDN %>" value="<%= ejbcawebbean.getText("REMOVE") %>">
      </td>
      <td width="25%"  align="right"> 
           &nbsp;&nbsp;
      </td>
      <td width="70%"> 
        &nbsp;&nbsp;
      </td> 
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
        <%= ejbcawebbean.getText("SELECTFORREMOVAL") %>
      </td>
      <td width="25%"  align="right"> 
        <%= ejbcawebbean.getText("SUBJECTALTNAMEFIELDS") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_ADDSUBJECTALTNAME %>" size="1" >
            <% 
               for(int i=0; i < subjectaltnamefields.length; i++){ %>
           <option  value='<%= subjectaltnamefields[i]%>'><%= ejbcawebbean.getText(subjectfieldtexts[subjectaltnamefields[i]]) %>
           </option>
            <% }%>
        </select>
        &nbsp;<input type="submit" name="<%= BUTTON_ADDSUBJECTALTNAME %>" value="<%= ejbcawebbean.getText("ADD") %>"> 
      </td> 
    </tr>
    <% numberofsubjectdnfields = profiledata.getSubjectAltNameFieldOrderLength();
       for(int i=0; i < numberofsubjectdnfields; i++){
         fielddata =  profiledata.getSubjectAltNameFieldsInOrder(i);
         int fieldtype = fielddata[EndEntityProfile.FIELDTYPE];
         if(fieldtype != EndEntityProfile.OTHERNAME && fieldtype != EndEntityProfile.X400ADDRESS && fieldtype != EndEntityProfile.DIRECTORYNAME && 
            fieldtype != EndEntityProfile.EDIPARTNAME && fieldtype != EndEntityProfile.REGISTEREDID ){
    %>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
        <input type="checkbox" name="<%=CHECKBOX_SELECTSUBJECTALTNAME + i %>" value="<%=CHECKBOX_VALUE %>">      
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText(subjectfieldtexts[fielddata[EndEntityProfile.FIELDTYPE]]) %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <% if(fielddata[EndEntityProfile.FIELDTYPE] != EndEntityProfile.RFC822NAME ){ %>
        <input type="text" name="<%=TEXTFIELD_SUBJECTALTNAME + i%>" size="40" maxlength="1024" 
           value="<% if(profiledata.getValue(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]) != null) out.write(profiledata.getValue(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER])); %>"><br>

        <%= ejbcawebbean.getText("REQUIRED") %>
        <input type="checkbox" name="<%=CHECKBOX_REQUIRED_SUBJECTALTNAME + i %>" value="<%=CHECKBOX_VALUE %>"
           <% if(profiledata.isRequired(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]))
                 out.write("CHECKED");
           %>> 
        &nbsp;&nbsp;<%= ejbcawebbean.getText("MODIFYABLE") %> 
        <input type="checkbox" name="<%=CHECKBOX_MODIFYABLE_SUBJECTALTNAME + i %>" value="<%=CHECKBOX_VALUE %>" 
           <% if(profiledata.isModifyable(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]))
                 out.write("CHECKED");
           %>> 
        <% }
           else{ 
             emailfieldexists=true; 
              %>
             <%= ejbcawebbean.getText("REQUIRED") %>
        <input type="checkbox" name="<%=CHECKBOX_REQUIRED_SUBJECTALTNAME + i %>" value="<%=CHECKBOX_VALUE %>" onclick='checkemailfield("<%=CHECKBOX_REQUIRED_SUBJECTALTNAME + i %>")'
           <% if(profiledata.isRequired(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]))
                 out.write("CHECKED");
           %>>&nbsp;<%=ejbcawebbean.getText("SEEEMAILCONFIGURATION") %>
        <% }%>
      </td>
    </tr>
   <%   }
      }%>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
        <input type="submit" name="<%= BUTTON_DELETESUBJECTALTNAME %>" value="<%= ejbcawebbean.getText("REMOVE") %>">
      </td>
      <td width="25%"  align="right"> 
           &nbsp;&nbsp;
      </td>
      <td width="70%"> 
        &nbsp;&nbsp;
      </td> 
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("EMAILDOMAIN") %> <br><%= ejbcawebbean.getText("USEONLYDOMAIN") %>
      </td>
      <td width="70%"> 
        <% used = false;
           if(profiledata.getUse(EndEntityProfile.EMAIL,0) || emailfieldexists) 
             used=true; %>
        <input type="text" name="<%=TEXTFIELD_EMAIL%>" size="40" maxlength="1024"  <% if(!used) out.write(" disabled "); %>
           value="<% if(profiledata.getValue(EndEntityProfile.EMAIL,0) != null && used) out.write(profiledata.getValue(EndEntityProfile.EMAIL,0)); %>"><br>
        <%= ejbcawebbean.getText("USE") %> 
        <input type="checkbox" name="<%=CHECKBOX_USE_EMAIL %>" value="<%=CHECKBOX_VALUE %>" onclick="checkusetextfield('<%=CHECKBOX_USE_EMAIL %>', '<%=TEXTFIELD_EMAIL%>', '<%=CHECKBOX_REQUIRED_EMAIL %>', '<%=CHECKBOX_MODIFYABLE_EMAIL %>')"
           <% if(used)
                 out.write(" CHECKED ");
              if(emailfieldexists)
                 out.write(" disabled "); %>
           >&nbsp;&nbsp;
               <%= ejbcawebbean.getText("REQUIRED") %>
        <input type="checkbox" name="<%=CHECKBOX_REQUIRED_EMAIL%>" value="<%=CHECKBOX_VALUE %>"  <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.isRequired(EndEntityProfile.EMAIL,0) && used)
                 out.write("CHECKED");
           %>> 
        &nbsp;&nbsp;<%= ejbcawebbean.getText("MODIFYABLE") %> 
        <input type="checkbox" name="<%=CHECKBOX_MODIFYABLE_EMAIL  %>" value="<%=CHECKBOX_VALUE %>"  <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.isModifyable(EndEntityProfile.EMAIL,0) && used)
                 out.write("CHECKED");
           %>> 
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("DEFAULTCERTIFICATEPROFILE") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_DEFAULTCERTPROFILE %>" size="1" >
            <% Iterator iter = certificateprofilenames.keySet().iterator();
               while(iter.hasNext()){
                 String nextprofilename = (String) iter.next();
                 int certprofid = ((Integer) certificateprofilenames.get(nextprofilename)).intValue();%>
           <option <%  if(profiledata.getValue(EndEntityProfile.DEFAULTCERTPROFILE ,0) != null)
                          if(profiledata.getValue(EndEntityProfile.DEFAULTCERTPROFILE ,0).equals(Integer.toString(certprofid)))
                            out.write(" selected "); %>
                    value='<%= certprofid %>'><%= nextprofilename %>
           </option>
            <% } %>
        </select>
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("AVAILABLECERTIFICATEPROF") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_AVAILABLECERTPROFILES %>" size="7" multiple >
            <% String[] availablecertprofs = profiledata.getValue(EndEntityProfile.AVAILCERTPROFILES, 0).split(EndEntityProfile.SPLITCHAR);
               iter = certificateprofilenames.keySet().iterator();
               while(iter.hasNext()){
                 String nextprofilename = (String) iter.next();
                 int certprofid = ((Integer) certificateprofilenames.get(nextprofilename)).intValue(); %>
           <option <% for(int j=0;j< availablecertprofs.length;j++){
                         if(availablecertprofs[j].equals(Integer.toString(certprofid)))
                            out.write(" selected "); 
                      }%>
                    value='<%= certprofid%>'><%= nextprofilename %>
           </option>
            <% } %>
        </select>
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("DEFAULTCA") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_DEFAULTCA %>" size="1" >
            <% iter = authorizedcas.iterator();
               while(iter.hasNext()){
                 Integer caid = (Integer) iter.next(); %>
           <option <%  if(profiledata.getValue(EndEntityProfile.DEFAULTCA ,0) != null)
                          if(profiledata.getValue(EndEntityProfile.DEFAULTCA ,0).equals(caid.toString()))
                            out.write(" selected "); %>
                    value='<%= caid.toString() %>'><%= caidtonamemap.get(caid) %>
           </option>
            <% } %>
        </select>
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("AVAILABLECAS") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_AVAILABLECAS %>" size="7" multiple >
            <% String[] availablecas = profiledata.getValue(EndEntityProfile.AVAILCAS, 0).split(EndEntityProfile.SPLITCHAR);
               iter = authorizedcas.iterator();
               while(iter.hasNext()){
                Integer caid = (Integer) iter.next(); %>
           <option <% for(int j=0;j< availablecas.length;j++){
                         if(availablecas[j].equals(caid.toString()))
                            out.write(" selected "); 
                      }%>
                    value='<%= caid.toString()%>'><%= caidtonamemap.get(caid) %>
           </option>
            <% } %>
        </select>
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("DEFAULTTOKEN") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_DEFAULTTOKENTYPE %>" size="1" >
            <% for(int i=0; i < tokentexts.length;i++){ %>
           <option <%  if(profiledata.getValue(EndEntityProfile.DEFKEYSTORE  ,0) != null)
                          if(profiledata.getValue(EndEntityProfile.DEFKEYSTORE  ,0).equals(Integer.toString(tokenids[i])))
                            out.write(" selected "); %>
                    value='<%= tokenids[i] %>'><%  if(tokenids[i] <= SecConst.TOKEN_SOFT) 
                                                      out.write(ejbcawebbean.getText(tokentexts[i]));
                                                   else
                                                      out.write(tokentexts[i]);%>
           </option>
            <% } %>
        </select>
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("AVAILABLETOKENS") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_AVAILABLETOKENTYPES %>" size="7" multiple >
            <% String[] profileavailabletokens = profiledata.getValue(EndEntityProfile.AVAILKEYSTORE, 0).split(EndEntityProfile.SPLITCHAR);
               for(int i=0; i < tokentexts.length;i++){ %>
           <option <% for(int j=0;j< profileavailabletokens.length;j++){
                         if(profileavailabletokens[j].equals(Integer.toString(tokenids[i])))
                            out.write(" selected "); 
                      }%>
                    value='<%= tokenids[i]%>'><%  if(tokenids[i] <= SecConst.TOKEN_SOFT) 
                                                      out.write(ejbcawebbean.getText(tokentexts[i]));
                                                   else
                                                      out.write(tokentexts[i]);%>
           </option>
            <% } %>
        </select>
      </td>
    </tr>
   <% if(globalconfiguration.getIssueHardwareTokens()){ %>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("USEHARDTOKENISSUERS") %> <br>&nbsp;
      </td>
      <td width="70%"> 
           <% used =  profiledata.getUse(EndEntityProfile.AVAILTOKENISSUER,0); %>
        <input type="checkbox" name="<%=CHECKBOX_USE_HARDTOKENISSUERS %>" value="<%=CHECKBOX_VALUE %>" onclick="checkusehardtokenissuers()"
           <% if(used)
                 out.write("CHECKED");
           %>>
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("DEFAULTHARDTOKENISSUER") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_DEFAULTHARDTOKENISSUER %>" size="1" <% if(!used) out.write(" disabled "); %>>
            <% for(int i=0; i < hardtokenissueraliases.length;i++){ %>
           <option <%  if(profiledata.getValue(EndEntityProfile.DEFAULTTOKENISSUER ,0) != null)
                          if(profiledata.getValue(EndEntityProfile.DEFAULTTOKENISSUER  ,0).equals(Integer.toString(hardtokenissuerids[i])))
                            out.write(" selected "); %>
                    value='<%= hardtokenissuerids[i] %>'><%= hardtokenissueraliases[i] %>
           </option>
            <% } %>
        </select>
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("AVAILABLEHARDTOKENISSUERS") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <select name="<%=SELECT_AVAILABLEHARDTOKENISSUERS %>" size="7" multiple <% if(!used) out.write(" disabled "); %>>
            <% String[] availableissuers = profiledata.getValue(EndEntityProfile.AVAILTOKENISSUER, 0).split(EndEntityProfile.SPLITCHAR); 
               for(int i=0; i < hardtokenissueraliases.length;i++){ %>
           <option <% for(int j=0;j< availableissuers.length;j++){
                         if(availableissuers[j].equals(Integer.toString(hardtokenissuerids[i])))
                            out.write(" selected "); 
                      }%>
                    value='<%= hardtokenissuerids[i]%>'><%= hardtokenissueraliases[i]%>
           </option>
            <% } %>
        </select>
      </td>
    </tr>
    <% } %>
    <tr  id="Row<%=row++%2%>">       
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" valign="top" align="right"><%= ejbcawebbean.getText("TYPES") %></td>
      <td width="70%" valign="top" align="right">&nbsp;</td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("ADMINISTRATOR") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <% used = profiledata.getUse(EndEntityProfile.ADMINISTRATOR,0); %>
         <%= ejbcawebbean.getText("USE") %> 
        <input type="checkbox" name="<%=CHECKBOX_USE_ADMINISTRATOR %>" value="<%=CHECKBOX_VALUE %>" onclick="checkusecheckbox('<%=CHECKBOX_USE_ADMINISTRATOR %>', '<%=CHECKBOX_ADMINISTRATOR%>', '<%=CHECKBOX_REQUIRED_ADMINISTRATOR %>')"
           <% if(used)
                 out.write("CHECKED");
           %>><br>
         <%= ejbcawebbean.getText("DEFAULT") %> 
        <input type="checkbox" name="<%=CHECKBOX_ADMINISTRATOR%>" value="<%=CHECKBOX_VALUE %>" <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.getValue(EndEntityProfile.ADMINISTRATOR,0) != null && used)
                 if(profiledata.getValue(EndEntityProfile.ADMINISTRATOR,0).equals(EndEntityProfile.TRUE))
                   out.write("CHECKED");
           %>>&nbsp;&nbsp;
        <%= ejbcawebbean.getText("REQUIRED") %>
        <input type="checkbox" name="<%=CHECKBOX_REQUIRED_ADMINISTRATOR%>" value="<%=CHECKBOX_VALUE %>" <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.isRequired(EndEntityProfile.ADMINISTRATOR,0) && used)
                out.write("CHECKED");
           %>> 
      </td>
    </tr>
<% if(globalconfiguration.getEnableKeyRecovery()){ %> 
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("KEYRECOVERABLE") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <% used = profiledata.getUse(EndEntityProfile.KEYRECOVERABLE,0); %>
         <%= ejbcawebbean.getText("USE") %> 
        <input type="checkbox" name="<%=CHECKBOX_USE_KEYRECOVERABLE %>" value="<%=CHECKBOX_VALUE %>" onclick="checkusecheckbox('<%=CHECKBOX_USE_KEYRECOVERABLE %>', '<%=CHECKBOX_KEYRECOVERABLE%>', '<%=CHECKBOX_REQUIRED_KEYRECOVERABLE %>')"
           <% if(used)
                 out.write("CHECKED");
           %>><br>
        <%= ejbcawebbean.getText("DEFAULT") %> 
        <input type="checkbox" name="<%=CHECKBOX_KEYRECOVERABLE%>" value="<%=CHECKBOX_VALUE %>" <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.getValue(EndEntityProfile.KEYRECOVERABLE,0) != null && used)
                 if(profiledata.getValue(EndEntityProfile.KEYRECOVERABLE,0).equals(EndEntityProfile.TRUE))
                   out.write("CHECKED");
           %>>&nbsp;&nbsp;
        <%= ejbcawebbean.getText("REQUIRED") %>
        <input type="checkbox" name="<%=CHECKBOX_REQUIRED_KEYRECOVERABLE%>" value="<%=CHECKBOX_VALUE %>" <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.isRequired(EndEntityProfile.KEYRECOVERABLE,0) && used)
                out.write("CHECKED");
           %>> 
      </td>
    </tr>
   <% } %>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("SENDNOTIFICATION") %> <br>&nbsp;
      </td>
      <td width="70%"> 
        <% used = profiledata.getUse(EndEntityProfile.SENDNOTIFICATION,0); %>
         <%= ejbcawebbean.getText("USE") %> 
        <input type="checkbox" name="<%=CHECKBOX_USE_SENDNOTIFICATION %>" value="<%=CHECKBOX_VALUE %>" onclick="checkusecheckbox('<%=CHECKBOX_USE_SENDNOTIFICATION %>', '<%=CHECKBOX_SENDNOTIFICATION%>', '<%=CHECKBOX_REQUIRED_SENDNOTIFICATION %>'); checkuseemailfield();usenotificationchange()"
           <% if(used)
                 out.write("CHECKED");
           %>> <br>
        <%= ejbcawebbean.getText("DEFAULT") %> 
        <input type="checkbox" name="<%=CHECKBOX_SENDNOTIFICATION%>" value="<%=CHECKBOX_VALUE %>" <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.getValue(EndEntityProfile.SENDNOTIFICATION,0) != null && used)
                 if(profiledata.getValue(EndEntityProfile.SENDNOTIFICATION,0).equals(EndEntityProfile.TRUE))
                   out.write("CHECKED");
           %>>&nbsp;&nbsp;
        <%= ejbcawebbean.getText("REQUIRED") %>
        <input type="checkbox" name="<%=CHECKBOX_REQUIRED_SENDNOTIFICATION%>" value="<%=CHECKBOX_VALUE %>" <% if(!used) out.write(" disabled "); %>
           <% if(profiledata.isRequired(EndEntityProfile.SENDNOTIFICATION,0) && used)
                out.write("CHECKED");
           %>> 
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("NOTIFICATIONSENDER") %><br>(<%= ejbcawebbean.getText("EMAILADDRESS") %>)
      </td>
      <td width="70%"> 
        <% used = profiledata.getUse(EndEntityProfile.SENDNOTIFICATION,0); %>
        <input type="text" name="<%=TEXTFIELD_NOTIFICATIONSENDER%>" size="40" maxlength="1024" 
           value="<%= profiledata.getNotificationSender() %>"            
           <% if(!used)
                 out.write(" disabled ");
           %>>                  
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("NOTIFICATIONSUBJECT") %> <br>&nbsp;
      </td>
      <td width="70%">         
        <input type="text" name="<%=TEXTFIELD_NOTIFICATIONSUBJECT%>" size="40" maxlength="1024" 
           value="<%= profiledata.getNotificationSubject() %>" 
           <% if(!used)
                 out.write(" disabled ");
           %>>                  
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
        <%= ejbcawebbean.getText("NOTIFICATIONMESSAGE") %> <br>&nbsp;
      </td>
      <td width="70%">       
        <textarea name="<%=TEXTAREA_NOTIFICATIONMESSAGE%>" cols=40 rows=8 <% if(!used) out.write(" disabled ");%>><%= profiledata.getNotificationMessage()%></textarea>  
      </td>
    </tr>
    <tr  id="Row<%=row++%2%>"> 
      <td width="5%" valign="top">
         &nbsp;
      </td>
      <td width="25%" align="right"> 
      <td width="70%" valign="top"> 
        <input type="submit" name="<%= BUTTON_SAVE %>" onClick='return checkallfields()' value="<%= ejbcawebbean.getText("SAVE") %>" >
        <input type="submit" name="<%= BUTTON_CANCEL %>" value="<%= ejbcawebbean.getText("CANCEL") %>">
      </td>
    </tr>
  </table>
 </form>
