<% String[] eventnames     = logbean.getAllLocalEventNames();
   String[] modulenames    = logbean.getLocalModuleNames(ejbcawebbean);
   HashMap  eventtexttoid  = logbean.getEventNameToIdMap();
   HashMap  moduletexttoid = logbean.getModuleNameToIdMap();
   HashMap  caidtonamemap  = ejbcawebbean.getInformationMemory().getCAIdToNameMap();
   Collection authorizedcaids = ejbcawebbean.getAuthorizedCAIds();
   
   String[] admintypes = new String[ADMINTYPES.length];
   for(int i=0; i < ADMINTYPES.length; i++){
     admintypes[i] = ejbcawebbean.getText(ADMINTYPES[i]);
   }
 
   String[] connectorreferences = {"AND","OR","ANDNOT","ORNOT"};
   String[] monthreferences     = {"MONTHJAN","MONTHFEB","MONTHMAR","MONTHAPR","MONTHMAY","MONTHJUN","MONTHJUL","MONTHAUG","MONTHSEP"
                                  ,"MONTHOCT","MONTHNOV","MONTHDEC"};
   Calendar calendar = Calendar.getInstance();
   int dayofmonth    = calendar.get(Calendar.DAY_OF_MONTH);
   int month         = calendar.get(Calendar.MONTH);
   int year          = calendar.get(Calendar.YEAR);
   int hour          = calendar.get(Calendar.HOUR_OF_DAY);
   hour++;
   if(hour >= 24) hour =0;

   int index;

   // Build sorted CA name List
   TreeMap canameandids = new TreeMap();
   Iterator iter = authorizedcaids.iterator();
   while(iter.hasNext()){
     Object obj = iter.next();
     canameandids.put(caidtonamemap.get(obj),obj);
   }
      
%> 

<script language=javascript>
<!--
   var ID    = 0;
   var NAME  = 1;
  
   var eventfields   = new Array(2);
   eventfields[ID]   = new Array(<%= eventnames.length %>);
   eventfields[NAME] = new Array(<%= eventnames.length %>);
   <% for(int i = 0; i < eventnames.length; i++){ %>
      eventfields[ID][<%=i %>]   = "<%=eventtexttoid.get(eventnames[i]) %>";
      eventfields[NAME][<%=i %>]  = "<%=eventnames[i] %>";
    <% } %>

   var cafields = new Array(2);
   cafields[ID] = new Array(<%= authorizedcaids.size() %>);
   cafields[NAME] = new Array(<%= authorizedcaids.size() %>);
   <% Iterator canameiter = canameandids.keySet().iterator();
      for(int i=0; i < canameandids.keySet().size(); i++){
         String caname = (String) canameiter.next(); %>
   cafields[ID][<%=i%>] = <%= ((Integer) canameandids.get(caname)).toString() %>;   
   cafields[NAME][<%=i%>] = "<%= caname%>";
   <% } %>

   var modulefields   = new Array(2);
   modulefields[ID]   = new Array(<%= modulenames.length %>);
   modulefields[NAME] = new Array(<%= modulenames.length %>);
   <% for(int i = 0; i < modulenames.length; i++){ %>
      modulefields[ID][<%=i %>]   = "<%=moduletexttoid.get(modulenames[i]) %>";
      modulefields[NAME][<%=i %>]  = "<%=modulenames[i] %>";
    <% } %>

   
   var admintypes = new Array(2);
   admintypes[ID]   = new Array(<%= ADMINTYPES.length %>);
   admintypes[NAME] = new Array(<%= ADMINTYPES.length %>);
   <% for(int i = 0; i <ADMINTYPES.length ; i++){ %>
      admintypes[ID][<%=i %>] = "<%=i%>";
      admintypes[NAME][<%=i %>] = "<%=ejbcawebbean.getText(ADMINTYPES[i])%>";
    <% } %>
   
   var matchtypefields = new Array(2);
   matchtypefields[ID] = new Array(2);
   matchtypefields[ID][0]= <%= BasicMatch.MATCH_TYPE_EQUALS %>;
   matchtypefields[ID][1] = <%= BasicMatch.MATCH_TYPE_BEGINSWITH %>;
   matchtypefields[ID][2] = <%= BasicMatch.MATCH_TYPE_CONTAINS %>;

   matchtypefields[NAME] = new Array(3);
   matchtypefields[NAME][0] = "<%= ejbcawebbean.getText("EQUALS") %>";
   matchtypefields[NAME][1] = "<%= ejbcawebbean.getText("BEGINSWITH") %>";
   matchtypefields[NAME][2] = "<%= ejbcawebbean.getText("CONTAINS") %>";



function changematchfields(row){

 // check value on matchwith
  matchwith = eval("document.form.selectmatchwithrow" + row);
  matchtype = eval("document.form.selectmatchtyperow" + row);
  textmatchvalue = eval("document.form.textfieldmatchvaluerow" + row);
  menumatchvalue = eval("document.form.selectmatchvaluerow" + row);
  var index = matchwith.selectedIndex;
  var numofvalues;
  matchwithvalue = matchwith[index].value;  
  var i;

   // if eventname use menu and no texfield
  if(matchwithvalue == <%= LogMatch.MATCH_WITH_EVENT %> ){

    menumatchvalue.disabled = false;
    textmatchvalue.disabled = true;
    textmatchvalue.value= "";
    textmatchvalue.size=1;

    var numoftypes = matchtype.length;
    for( i=numoftypes-1; i >= 0; i-- ){
      matchtype.options[i]=null;
     }
     matchtype.options[0]= new Option(matchtypefields[NAME][0],matchtypefields[ID][0]);

     numofvalues = menumatchvalue.length;
     for(i=numofvalues-1; i >= 0; i--){
       menumatchvalue.options[i]=null;
     }  
     for( i = 0; i < eventfields[ID].length; i++){
       menumatchvalue.options[i]= new Option(eventfields[NAME][i],eventfields[ID][i]);       
     }
  }else{
    if(matchwithvalue == <%= LogMatch.MATCH_WITH_CA %> ){

      menumatchvalue.disabled = false;
      textmatchvalue.disabled = true;
      textmatchvalue.value= "";
      textmatchvalue.size=1;

     var numoftypes = matchtype.length;
     for( i=numoftypes-1; i >= 0; i-- ){
       matchtype.options[i]=null;
     }
     matchtype.options[0]= new Option(matchtypefields[NAME][0],matchtypefields[ID][0]);

     numofvalues = menumatchvalue.length;
     for(i=numofvalues-1; i >= 0; i--){
       menumatchvalue.options[i]=null;
     }  
     for( i = 0; i < cafields[ID].length; i++){
       menumatchvalue.options[i]= new Option(cafields[NAME][i],cafields[ID][i]);       
     }
    }
    else{
      if(matchwithvalue == <%= LogMatch.MATCH_WITH_MODULE %> ){

        menumatchvalue.disabled = false;
        textmatchvalue.disabled = true;
        textmatchvalue.value= "";
        textmatchvalue.size=1;

        var numoftypes = matchtype.length;
        for( i=numoftypes-1; i >= 0; i-- ){
          matchtype.options[i]=null;
        }
        matchtype.options[0]= new Option(matchtypefields[NAME][0],matchtypefields[ID][0]);

        numofvalues = menumatchvalue.length;
        for(i=numofvalues-1; i >= 0; i--){
          menumatchvalue.options[i]=null;
        }  
        for( i = 0; i < modulefields[ID].length; i++){
         menumatchvalue.options[i]= new Option(modulefields[NAME][i],modulefields[ID][i]);       
        }
      }
      else{
        // if admintype use manu and no textfield. 
        if(matchwithvalue == <%= LogMatch.MATCH_WITH_SPECIALADMIN %> ){
  
          menumatchvalue.disabled = false;
          textmatchvalue.disabled = true;
          textmatchvalue.value= "";
          textmatchvalue.size=1;

          var numoftypes = matchtype.length;
          for( i=numoftypes-1; i >= 0; i-- ){
            matchtype.options[i]=null;
          }
          matchtype.options[0]= new Option(matchtypefields[NAME][0],matchtypefields[ID][0]);

          numofvalues = menumatchvalue.length;
          for(i=numofvalues-1; i >= 0; i--){
            menumatchvalue.options[i]=null;
          }  
          for( i = 0; i < admintypes[ID].length; i++){
            menumatchvalue.options[i]= new Option(admintypes[NAME][i],admintypes[ID][i]);       
          }
      
       }
       else{      
   // else use textfield.  
     
          var numoftypes = matchtype.length;
          for(i=numoftypes-1; i >= 0; i-- ){
            matchtype.options[i]=null;
          }
          matchtype.options[0]= new Option(matchtypefields[NAME][0],matchtypefields[ID][0]);
          matchtype.options[1]= new Option(matchtypefields[NAME][1],matchtypefields[ID][1]);
          matchtype.options[2]= new Option(matchtypefields[NAME][2],matchtypefields[ID][2]);

          numofvalues = menumatchvalue.length;
          for(i=numofvalues-1; i >= 0; i--){
            menumatchvalue.options[i]=null;
          }     
          menumatchvalue.disabled = true;
          textmatchvalue.disabled = false;
          textmatchvalue.size=40;
        
       }  
     }
   }
  }
}

function checkfields(){
  var returnval =true;
  // Check if matchwithfield is admin cert or certificate when check and replace spaces in hex serial number.
  var matchwithrows1 = eval("document.form.selectmatchwithrow1");
  var matchwithrows2 = eval("document.form.selectmatchwithrow2");
  var matchwithrows3 = eval("document.form.selectmatchwithrow3");

  if(matchwithrows1[matchwithrows1.selectedIndex].value == <%=LogMatch.MATCH_WITH_CERTIFICATE %> || matchwithrows1[matchwithrows1.selectedIndex].value == <%= LogMatch.MATCH_WITH_ADMINCERTIFICATE%>)
    if(!checkfieldforhexadecimalnumbers("document.form.selectmatchwithrow1", "<%= ejbcawebbean.getText("ONLYHEXNUMBERS") %>"))
      returnval = false;

  if(matchwithrows2[matchwithrows2.selectedIndex].value == <%= LogMatch.MATCH_WITH_CERTIFICATE %> || matchwithrows2[matchwithrows2.selectedIndex].value == <%= LogMatch.MATCH_WITH_ADMINCERTIFICATE%>)
    if(!checkfieldforhexadecimalnumbers("document.form.selectmatchwithrow2" , "<%= ejbcawebbean.getText("ONLYHEXNUMBERS") %>"))
      returnval = false;

  if(matchwithrows3[matchwithrows3.selectedIndex].value == <%= LogMatch.MATCH_WITH_CERTIFICATE %> || matchwithrows3[matchwithrows3.selectedIndex].value == <%= LogMatch.MATCH_WITH_ADMINCERTIFICATE%>)
    if(!checkfieldforhexadecimalnumbers("document.form.selectmatchwithrow3" , "<%= ejbcawebbean.getText("ONLYHEXNUMBERS") %>"))
      returnval = false;

  return returnval; 
}


 -->
</script>
<table width="100%" border="0" cellspacing="1" cellpadding="0">
  <tr> 
    <td width="2%">&nbsp;</td>
    <td width="5%" align="left">&nbsp;
    </td>
    <td width="93%" align="left"> 
        <% int tempval = -1;
           if(oldmatchwithrow1!= null)
             tempval= Integer.parseInt(oldmatchwithrow1); %>
        <select name="<%=SELECT_MATCHWITH_ROW1 %>" onchange='changematchfields(1)' >
           <option  value='<%= VALUE_NONE %>'><%= ejbcawebbean.getText("NONE") %>
           </option>
           <option <%  if(tempval == LogMatch.MATCH_WITH_EVENT)
                         out.write(" selected ");
                    %> value='<%= LogMatch.MATCH_WITH_EVENT %>'><%= ejbcawebbean.getText("MATCHEVENT") %>
           </option>
           <option <%  if(tempval == LogMatch.MATCH_WITH_CA)
                         out.write(" selected ");
                    %> value='<%= LogMatch.MATCH_WITH_CA %>'><%= ejbcawebbean.getText("MATCHCA") %>
           </option>
           <option <%  if(tempval == LogMatch.MATCH_WITH_MODULE)
                         out.write(" selected ");
                    %> value='<%= LogMatch.MATCH_WITH_MODULE %>'><%= ejbcawebbean.getText("MATCHMODULE") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_USERNAME)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_USERNAME %>'><%= ejbcawebbean.getText("MATCHUSERNAME") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_CERTIFICATE)
                         out.write(" selected ");
                         %> value='<%= LogMatch.MATCH_WITH_CERTIFICATE %>'><%= ejbcawebbean.getText("MATCHCERTIFICATE") %>
           </option>
           <option <%if(tempval == LogMatch.MATCH_WITH_SPECIALADMIN)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_SPECIALADMIN %>'><%= ejbcawebbean.getText("MATCHADMINTYPE") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_ADMINCERTIFICATE)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_ADMINCERTIFICATE %>'><%= ejbcawebbean.getText("MATCHADMINCERT") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_IP)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_IP %>'><%= ejbcawebbean.getText("MATCHADMINIP") %>
          </option>
          <option <%if(tempval == LogMatch.MATCH_WITH_COMMENT)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_COMMENT %>'><%= ejbcawebbean.getText("MATCHCOMMENT") %>
          </option>
        </select> &nbsp;&nbsp;
          <%
           tempval = -1;
           if(oldmatchtyperow1!= null)
             tempval= Integer.parseInt(oldmatchtyperow1);
          %>
        <select name="<%=SELECT_MATCHTYPE_ROW1 %>">
          <% if(oldmatchwithrow1 != null){
               if(Integer.parseInt(oldmatchwithrow1) == LogMatch.MATCH_WITH_EVENT || Integer.parseInt(oldmatchwithrow1) == LogMatch.MATCH_WITH_MODULE 
                 || Integer.parseInt(oldmatchwithrow1) == LogMatch.MATCH_WITH_SPECIALADMIN || Integer.parseInt(oldmatchwithrow1) == LogMatch.MATCH_WITH_CA){ %>
          <option <%  if(tempval == BasicMatch.MATCH_TYPE_EQUALS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_EQUALS) %>'><%= ejbcawebbean.getText("EQUALS") %>
          </option>
             <%  }
                 else{ %>
          <option <% if(tempval == BasicMatch.MATCH_TYPE_EQUALS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_EQUALS) %>'><%= ejbcawebbean.getText("EQUALS") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_BEGINSWITH){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_BEGINSWITH) %>'><%= ejbcawebbean.getText("BEGINSWITH") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_CONTAINS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_CONTAINS) %>'><%= ejbcawebbean.getText("CONTAINS") %>
          </option> 
            <%  }
              }else{%>
          <option <% if(tempval == BasicMatch.MATCH_TYPE_EQUALS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_EQUALS) %>'><%= ejbcawebbean.getText("EQUALS") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_BEGINSWITH){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_BEGINSWITH) %>'><%= ejbcawebbean.getText("BEGINSWITH") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_CONTAINS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_CONTAINS) %>'><%= ejbcawebbean.getText("CONTAINS") %>
           <% } %>  
        </select> &nbsp;&nbsp; 

        <select name="<%=SELECT_MATCHVALUE_ROW1 %>"
           <% if(oldmatchwithrow1 != null){
                 if(oldmatchwithrow1.equals(Integer.toString(LogMatch.MATCH_WITH_EVENT))){ %>
              >
                <% for(int i=0; i < eventnames.length; i++){ %>
          <option <% if(oldmatchvaluerow1!= null){
                       if(Integer.parseInt(oldmatchvaluerow1) == ((Integer) eventtexttoid.get(eventnames[i])).intValue())
                         out.write(" selected ");
                    } %> value='<%= ((Integer) eventtexttoid.get(eventnames[i])).intValue() %>'><%= eventnames[i] %>
          </option>                   
                <%  }
                  }
                  else{
                   if(oldmatchwithrow1.equals(Integer.toString(LogMatch.MATCH_WITH_SPECIALADMIN))){ %>
              >
                <% for(int i=0; i < ADMINTYPES.length; i++){ %>
          <option <% if(oldmatchvaluerow1!= null){
                       if(oldmatchvaluerow1.equals(Integer.toString(i)))
                         out.write(" selected ");
                    } %> value='<%= i %>'><%= ejbcawebbean.getText(ADMINTYPES[i]) %>
          </option>                   
                <%  }
                  }
                  else{
                  if(oldmatchwithrow1.equals(Integer.toString(LogMatch.MATCH_WITH_CA))){ %>
              >
                <% 
                   canameiter = canameandids.keySet().iterator();
                   for(int i=0; i < canameandids.keySet().size(); i++){ %>
          <option <% 
                      String caname = (String) canameiter.next(); 
                      index = ((Integer) canameandids.get(caname)).intValue(); 
                      if(oldmatchvaluerow1!= null){
                         if(oldmatchvaluerow1.equals(Integer.toString(index)))
                          out.write(" selected ");
                      }
                     %> value='<%= index %>'><%= caname %>
          </option>                    
                <%  }
                  }
                  else{
                   if(oldmatchwithrow1.equals(Integer.toString(LogMatch.MATCH_WITH_MODULE))){ %>
              >
                <% for(int i=0; i < modulenames.length; i++){ %>
          <option <% index = ((Integer) moduletexttoid.get(modulenames[i])).intValue();
                      if(oldmatchvaluerow1!= null){
                         if(oldmatchvaluerow1.equals(Integer.toString(index)))
                          out.write(" selected ");
                      }
                     %> value='<%= index %>'><%= modulenames[i] %>
          </option>                    
                <%  }
                  }
                  else{ %>
                   disabled > 
                 <% } 
                }}}
              }else{ %>
                disabled > 
           <% } %>
       </select>
       <% if( oldmatchwithrow1!= null){
           if(  oldmatchwithrow1.equals(Integer.toString(LogMatch.MATCH_WITH_EVENT)) || Integer.parseInt(oldmatchwithrow1) == LogMatch.MATCH_WITH_MODULE || oldmatchwithrow1.equals(Integer.toString(LogMatch.MATCH_WITH_SPECIALADMIN)) || oldmatchwithrow1.equals(Integer.toString(LogMatch.MATCH_WITH_CA))){ %>
       <input type="text" name="<%=TEXTFIELD_MATCHVALUE_ROW1 %>"  size="1" maxlength="255" value='' disabled >    
           <% }else{ %>
              <input type="text" name="<%=TEXTFIELD_MATCHVALUE_ROW1 %>" size="40" maxlength="255" value='<%=oldmatchvaluerow1 %>' >
           <% }
           }else{ %>
              <input type="text" name="<%=TEXTFIELD_MATCHVALUE_ROW1 %>" size="40" maxlength="255" value='' >
        <% } %>
    </td>
  </tr>

  <tr> 
    <td width="2%">&nbsp;</td>
    <td width="5%" align="left">
       <select name='<%= SELECT_CONNECTOR_ROW2  %>'>  
         <option  value='<%= VALUE_NONE %>'><%= ejbcawebbean.getText("NONE") %>
         </option>
         <% for(int i=0; i<  connectorreferences.length; i++) { %> 
         <option <% if(oldconnectorrow2 != null)
                      if(oldconnectorrow2.equals(Integer.toString(i))) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
           <%= ejbcawebbean.getText(connectorreferences[i]) %>
         </option> 
         <% } %> 
       </select>
    </td>
    <td width="93%" align="left"> 
        <% tempval = -1;
           if(oldmatchwithrow2!= null)
             tempval= Integer.parseInt(oldmatchwithrow2); %>
        <select name="<%=SELECT_MATCHWITH_ROW2 %>" onchange='changematchfields(2)' >
           <option  value='<%= VALUE_NONE %>'><%= ejbcawebbean.getText("NONE") %>
           </option>
           <option <%  if(tempval == LogMatch.MATCH_WITH_EVENT)
                         out.write(" selected ");
                    %> value='<%=LogMatch.MATCH_WITH_EVENT %>'><%= ejbcawebbean.getText("MATCHEVENT") %>
           </option>
           <option <%  if(tempval == LogMatch.MATCH_WITH_CA)
                         out.write(" selected ");
                    %> value='<%= LogMatch.MATCH_WITH_CA %>'><%= ejbcawebbean.getText("MATCHCA") %>
           </option>
           <option <%  if(tempval == LogMatch.MATCH_WITH_MODULE)
                         out.write(" selected ");
                    %> value='<%= LogMatch.MATCH_WITH_MODULE%>'><%= ejbcawebbean.getText("MATCHMODULE") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_USERNAME)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_USERNAME %>'><%= ejbcawebbean.getText("MATCHUSERNAME") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_CERTIFICATE)
                         out.write(" selected ");
                         %> value='<%= LogMatch.MATCH_WITH_CERTIFICATE %>'><%= ejbcawebbean.getText("MATCHCERTIFICATE") %>
           </option>
           <option <%if(tempval == LogMatch.MATCH_WITH_SPECIALADMIN)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_SPECIALADMIN %>'><%= ejbcawebbean.getText("MATCHADMINTYPE") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_ADMINCERTIFICATE)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_ADMINCERTIFICATE %>'><%= ejbcawebbean.getText("MATCHADMINCERT") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_IP)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_IP %>'><%= ejbcawebbean.getText("MATCHADMINIP") %>
          </option>
          <option <%if(tempval == LogMatch.MATCH_WITH_COMMENT)
                         out.write(" selected ");
                     %> value='<%= LogMatch.MATCH_WITH_COMMENT %>'><%= ejbcawebbean.getText("MATCHCOMMENT") %>
          </option>
        </select> &nbsp;&nbsp;
          <%
           tempval = -1;
           if(oldmatchtyperow2!= null)
             tempval= Integer.parseInt(oldmatchtyperow2);
          %>
        <select name="<%=SELECT_MATCHTYPE_ROW2 %>">
          <% if(oldmatchwithrow2 != null){
               if(Integer.parseInt(oldmatchwithrow2) == LogMatch.MATCH_WITH_EVENT || Integer.parseInt(oldmatchwithrow2) == LogMatch.MATCH_WITH_MODULE 
                  || Integer.parseInt(oldmatchwithrow2) == LogMatch.MATCH_WITH_SPECIALADMIN || Integer.parseInt(oldmatchwithrow2) == LogMatch.MATCH_WITH_CA){ %>
          <option <%  if(tempval == BasicMatch.MATCH_TYPE_EQUALS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_EQUALS) %>'><%= ejbcawebbean.getText("EQUALS") %>
          </option>
             <%  }
                 else{ %>
          <option <% if(tempval == BasicMatch.MATCH_TYPE_EQUALS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_EQUALS) %>'><%= ejbcawebbean.getText("EQUALS") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_BEGINSWITH){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_BEGINSWITH) %>'><%= ejbcawebbean.getText("BEGINSWITH") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_CONTAINS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_CONTAINS) %>'><%= ejbcawebbean.getText("CONTAINS") %>
          </option> 
            <%  }
              }else{%>
          <option <% if(tempval == BasicMatch.MATCH_TYPE_EQUALS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_EQUALS) %>'><%= ejbcawebbean.getText("EQUALS") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_BEGINSWITH){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_BEGINSWITH) %>'><%= ejbcawebbean.getText("BEGINSWITH") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_CONTAINS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_CONTAINS) %>'><%= ejbcawebbean.getText("CONTAINS") %>
           <% } %>  
        </select> &nbsp;&nbsp; 

        <select name="<%=SELECT_MATCHVALUE_ROW2 %>"
           <% if(oldmatchwithrow2 != null){
                 if(oldmatchwithrow2.equals(Integer.toString(LogMatch.MATCH_WITH_EVENT))){ %>
              >
                <% for(int i=0; i < eventnames.length; i++){ %>
          <option <% if(oldmatchvaluerow2!= null){
                       if(Integer.parseInt(oldmatchvaluerow2) == ((Integer) eventtexttoid.get(eventnames[i])).intValue())
                         out.write(" selected ");
                    } %> value='<%= ((Integer) eventtexttoid.get(eventnames[i])).intValue() %>'><%= eventnames[i] %>
          </option>                   
                <%  }
                  }
                  else{
                   if(oldmatchwithrow2.equals(Integer.toString(LogMatch.MATCH_WITH_SPECIALADMIN))){ %>
              >
                <% for(int i=0; i < ADMINTYPES.length; i++){ %>
          <option <% if(oldmatchvaluerow2!= null){
                       if(oldmatchvaluerow2.equals(Integer.toString(i)))
                         out.write(" selected ");
                    } %> value='<%= i %>'><%= ejbcawebbean.getText(ADMINTYPES[i]) %>
          </option>                   
                <%  }
                  }
                  else{
                  if(oldmatchwithrow2.equals(Integer.toString(LogMatch.MATCH_WITH_CA))){ %>
              >
                <% 
                   canameiter = canameandids.keySet().iterator();
                   for(int i=0; i < canameandids.keySet().size(); i++){ %>
          <option <% 
                      String caname = (String) canameiter.next(); 
                      index = ((Integer) canameandids.get(caname)).intValue(); 
                      if(oldmatchvaluerow2!= null){
                         if(oldmatchvaluerow2.equals(Integer.toString(index)))
                          out.write(" selected ");
                      }
                     %> value='<%= index %>'><%= caname %>
          </option>                    
                <%  }
                  }
                  else{
                   if(oldmatchwithrow2.equals(Integer.toString(LogMatch.MATCH_WITH_MODULE))){ %>
              >
                <% for(int i=0; i < modulenames.length; i++){ %>
          <option <% index = ((Integer) moduletexttoid.get(modulenames[i])).intValue();
                      if(oldmatchvaluerow2!= null){
                         if(oldmatchvaluerow2.equals(Integer.toString(index)))
                          out.write(" selected ");
                      }
                     index = ((Integer) moduletexttoid.get(modulenames[i])).intValue();
                     %> value='<%= index %>'><%= modulenames[i] %>
          </option>                  
                <%  }
                  }
                  else{ %>
                   disabled > 
                 <% } 
                }}}
              }else{ %>
                disabled > 
           <% } %>
       </select>
       <% if( oldmatchwithrow2!= null){
           if(  oldmatchwithrow2.equals(Integer.toString(LogMatch.MATCH_WITH_EVENT)) || Integer.parseInt(oldmatchwithrow2) == LogMatch.MATCH_WITH_MODULE || oldmatchwithrow2.equals(Integer.toString(LogMatch.MATCH_WITH_SPECIALADMIN)) || oldmatchwithrow2.equals(Integer.toString(LogMatch.MATCH_WITH_CA))){ %>
       <input type="text" name="<%=TEXTFIELD_MATCHVALUE_ROW2 %>"  size="1" maxlength="255" value='' disabled >    
           <% }else{ %>
              <input type="text" name="<%=TEXTFIELD_MATCHVALUE_ROW2 %>" size="40" maxlength="255" value='<%=oldmatchvaluerow2 %>' >
           <% }
           }else{ %>
              <input type="text" name="<%=TEXTFIELD_MATCHVALUE_ROW2 %>" size="40" maxlength="255" value='' >
        <% } %>
    </td>
  </tr>

  <tr> 
    <td width="2%">&nbsp;</td>
    <td width="5%" align="left">
       <select name='<%= SELECT_CONNECTOR_ROW3  %>'>  
         <option  value='<%= VALUE_NONE %>'><%= ejbcawebbean.getText("NONE") %>
         </option>
         <% for(int i=0; i<  connectorreferences.length; i++) { %> 
         <option <% if(oldconnectorrow3 != null)
                      if(oldconnectorrow3.equals(Integer.toString(i))) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
           <%= ejbcawebbean.getText(connectorreferences[i]) %>
         </option> 
         <% } %> 
       </select>
    </td>
    <td width="93%" align="left"> 
        <% tempval = -1;
           if(oldmatchwithrow3!= null)
             tempval= Integer.parseInt(oldmatchwithrow3); %>
        <select name="<%=SELECT_MATCHWITH_ROW3 %>" onchange='changematchfields(3)' >
           <option  value='<%= VALUE_NONE %>'><%= ejbcawebbean.getText("NONE") %>
           </option>
           <option <%  if(tempval == LogMatch.MATCH_WITH_EVENT)
                         out.write(" selected ");
                    %> value='<%= Integer.toString(LogMatch.MATCH_WITH_EVENT) %>'><%= ejbcawebbean.getText("MATCHEVENT") %>
           </option>
           <option <%  if(tempval == LogMatch.MATCH_WITH_CA)
                         out.write(" selected ");
                    %> value='<%= LogMatch.MATCH_WITH_CA %>'><%= ejbcawebbean.getText("MATCHCA") %>
           </option>
           <option <%  if(tempval == LogMatch.MATCH_WITH_MODULE)
                         out.write(" selected ");
                    %> value='<%= Integer.toString(LogMatch.MATCH_WITH_MODULE) %>'><%= ejbcawebbean.getText("MATCHMODULE") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_USERNAME)
                         out.write(" selected ");
                     %> value='<%= Integer.toString(LogMatch.MATCH_WITH_USERNAME) %>'><%= ejbcawebbean.getText("MATCHUSERNAME") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_CERTIFICATE)
                         out.write(" selected ");
                         %> value='<%= Integer.toString(LogMatch.MATCH_WITH_CERTIFICATE) %>'><%= ejbcawebbean.getText("MATCHCERTIFICATE") %>
           </option>
           <option <%if(tempval == LogMatch.MATCH_WITH_SPECIALADMIN)
                         out.write(" selected ");
                     %> value='<%= Integer.toString(LogMatch.MATCH_WITH_SPECIALADMIN) %>'><%= ejbcawebbean.getText("MATCHADMINTYPE") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_ADMINCERTIFICATE)
                         out.write(" selected ");
                     %> value='<%= Integer.toString(LogMatch.MATCH_WITH_ADMINCERTIFICATE) %>'><%= ejbcawebbean.getText("MATCHADMINCERT") %>
           </option>
           <option <% if(tempval == LogMatch.MATCH_WITH_IP)
                         out.write(" selected ");
                     %> value='<%= Integer.toString(LogMatch.MATCH_WITH_IP) %>'><%= ejbcawebbean.getText("MATCHADMINIP") %>
          </option>
          <option <%if(tempval == LogMatch.MATCH_WITH_COMMENT)
                         out.write(" selected ");
                     %> value='<%= Integer.toString(LogMatch.MATCH_WITH_COMMENT) %>'><%= ejbcawebbean.getText("MATCHCOMMENT") %>
          </option>
        </select> &nbsp;&nbsp;
          <%
           tempval = -1;
           if(oldmatchtyperow3!= null)
             tempval= Integer.parseInt(oldmatchtyperow3);
          %>
        <select name="<%=SELECT_MATCHTYPE_ROW3 %>">
          <% if(oldmatchwithrow3 != null){
               if(Integer.parseInt(oldmatchwithrow3) == LogMatch.MATCH_WITH_EVENT || Integer.parseInt(oldmatchwithrow3) == LogMatch.MATCH_WITH_MODULE 
                  || Integer.parseInt(oldmatchwithrow3) == LogMatch.MATCH_WITH_SPECIALADMIN || Integer.parseInt(oldmatchwithrow3) == LogMatch.MATCH_WITH_CA){ %>
          <option <%  if(tempval == BasicMatch.MATCH_TYPE_EQUALS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_EQUALS) %>'><%= ejbcawebbean.getText("EQUALS") %>
          </option>
             <%  }
                 else{ %>
          <option <% if(tempval == BasicMatch.MATCH_TYPE_EQUALS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_EQUALS) %>'><%= ejbcawebbean.getText("EQUALS") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_BEGINSWITH){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_BEGINSWITH) %>'><%= ejbcawebbean.getText("BEGINSWITH") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_CONTAINS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_CONTAINS) %>'><%= ejbcawebbean.getText("CONTAINS") %>
          </option> 
            <%  }
              }else{%>
          <option <% if(tempval == BasicMatch.MATCH_TYPE_EQUALS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_EQUALS) %>'><%= ejbcawebbean.getText("EQUALS") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_BEGINSWITH){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_BEGINSWITH) %>'><%= ejbcawebbean.getText("BEGINSWITH") %>
          </option> 
          <option <% if(tempval == BasicMatch.MATCH_TYPE_CONTAINS){
                         out.write(" selected ");
                    } %> value='<%= Integer.toString(BasicMatch.MATCH_TYPE_CONTAINS) %>'><%= ejbcawebbean.getText("CONTAINS") %>
           <% } %>  
        </select> &nbsp;&nbsp; 

        <select name="<%=SELECT_MATCHVALUE_ROW3 %>"
           <% if(oldmatchwithrow3 != null){
                 if(oldmatchwithrow3.equals(Integer.toString(LogMatch.MATCH_WITH_EVENT))){ %>
              >
                <% for(int i=0; i < eventnames.length; i++){ %>
          <option <% if(oldmatchvaluerow3!= null){
                       if(Integer.parseInt(oldmatchvaluerow3) == ((Integer) eventtexttoid.get(eventnames[i])).intValue())
                         out.write(" selected ");
                    } %> value='<%= ((Integer) eventtexttoid.get(eventnames[i])).intValue() %>'><%= eventnames[i] %>
          </option>                   
                <%  }
                  }
                  else{
                   if(oldmatchwithrow3.equals(Integer.toString(LogMatch.MATCH_WITH_SPECIALADMIN))){ %>
              >
                <% for(int i=0; i < ADMINTYPES.length; i++){ %>
          <option <% if(oldmatchvaluerow3!= null){
                       if(oldmatchvaluerow3.equals(Integer.toString(i)))
                         out.write(" selected ");
                    } %> value='<%= i %>'><%= ejbcawebbean.getText(ADMINTYPES[i]) %>
          </option>                   
                <%  }
                  }
                  else{
                  if(oldmatchwithrow3.equals(Integer.toString(LogMatch.MATCH_WITH_CA))){ %>
              >
                <% 
                   canameiter = canameandids.keySet().iterator();
                   for(int i=0; i < canameandids.keySet().size(); i++){ %>
          <option <% 
                      String caname = (String) canameiter.next(); 
                      index = ((Integer) canameandids.get(caname)).intValue(); 
                      if(oldmatchvaluerow3!= null){
                         if(oldmatchvaluerow3.equals(Integer.toString(index)))
                          out.write(" selected ");
                      }
                     %> value='<%= index %>'><%= caname %>
          </option>                    
                <%  }
                  }
                  else{
                   if(oldmatchwithrow3.equals(Integer.toString(LogMatch.MATCH_WITH_MODULE))){ %>
              >
                <% for(int i=0; i < modulenames.length; i++){ %>
          <option <% index = ((Integer) moduletexttoid.get(modulenames[i])).intValue();
                      if(oldmatchvaluerow3!= null){
                         if(oldmatchvaluerow3.equals(Integer.toString(index)))
                          out.write(" selected ");
                      }
                     index = ((Integer) moduletexttoid.get(modulenames[i])).intValue();
                     %> value='<%= index %>'><%= modulenames[i] %>
          </option>                   
                <%  }
                  }
                  else{ %>
                   disabled > 
                 <% } 
                }}}
              }else{ %>
                disabled > 
           <% } %>
       </select>
       <% if( oldmatchwithrow3!= null){
           if(  oldmatchwithrow3.equals(Integer.toString(LogMatch.MATCH_WITH_EVENT)) || Integer.parseInt(oldmatchwithrow3) == LogMatch.MATCH_WITH_MODULE || oldmatchwithrow3.equals(Integer.toString(LogMatch.MATCH_WITH_SPECIALADMIN)) || oldmatchwithrow3.equals(Integer.toString(LogMatch.MATCH_WITH_CA))){ %>
       <input type="text" name="<%=TEXTFIELD_MATCHVALUE_ROW3 %>"  size="1" maxlength="255" value='' disabled >    
           <% }else{ %>
              <input type="text" name="<%=TEXTFIELD_MATCHVALUE_ROW3 %>" size="40" maxlength="255" value='<%=oldmatchvaluerow3 %>' >
           <% }
           }else{ %>
              <input type="text" name="<%=TEXTFIELD_MATCHVALUE_ROW3 %>" size="40" maxlength="255" value='' >
        <% } %>
    </td>
  </tr>

  <tr> 
    <td width="2%">&nbsp;</td>
    <td width="5%" align="left">
      <select name='<%= SELECT_CONNECTOR_ROW4  %>'>  
         <option  value='<%= VALUE_NONE %>'><%= ejbcawebbean.getText("NONE") %>
         </option>
         <% for(int i=0; i<  connectorreferences.length; i++) { %> 
         <option <% if(oldconnectorrow4 != null)
                      if(oldconnectorrow4.equals(Integer.toString(i))) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
           <%= ejbcawebbean.getText(connectorreferences[i]) %>
         </option> 
         <% } %> 
      </select>
    </td>
    <td width="93%" align="left"> 
      <select name='<%= SELECT_MATCHWITH_ROW4  %>'>  
         <option  value='<%= VALUE_NONE %>'><%= ejbcawebbean.getText("NONE") %>
         </option>
         <option <% if(oldmatchwithrow4!= null)
                   if(oldmatchwithrow4.equals(Integer.toString(TimeMatch.MATCH_WITH_TIMECREATED)))
                     out.write("selected"); %>
              value='<%= Integer.toString(TimeMatch.MATCH_WITH_TIMECREATED) %>'><%= ejbcawebbean.getText("TIME") %>
         </option>       
      </select>  
      &nbsp;<%= ejbcawebbean.getText("ONORAFTER") %>&nbsp;
      <select name='<%= SELECT_DAY_ROW4  %>'>  
         <% for(int i=0; i <  31; i++) { %> 
         <option <% if(olddayrow4 != null)
                      if(olddayrow4.equals(Integer.toString(i))) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
           <%= i %>
         </option> 
         <% } %> 
      </select>&nbsp;
      <select name='<%= SELECT_MONTH_ROW4  %>'>  
         <% for(int i=0; i < monthreferences.length; i++) { %> 
         <option <% if(oldmonthrow4 != null)
                      if(oldmonthrow4.equals(Integer.toString(i))) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
          <%= ejbcawebbean.getText(monthreferences[i]) %>
         </option> 
         <% } %> 
      </select>&nbsp;
      <select name='<%= SELECT_YEAR_ROW4  %>'>  
         <% for(int i=2000; i <  2020; i++) { %> 
         <option <% if(oldyearrow4 != null)
                      if(oldyearrow4.equals(Integer.toString(i))) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
          <%= i %>
         </option> 
         <% } %> 
      </select>&nbsp;
      <select name='<%= SELECT_TIME_ROW4  %>'>  
          <% Calendar time = Calendar.getInstance();
             DateFormat dateformat = DateFormat.getTimeInstance(DateFormat.SHORT);%>

         <% for(int i=0; i <  24; i++) { %> 
         <option <% if(oldtimerow4 != null)
                      if(oldtimerow4.equals(Integer.toString(i))) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
          <% time.set(0,0,0,i,0); 
             out.print(dateformat.format(time.getTime()));%>
         </option> 
         <% } %> 
      </select>
    </td>
  </tr>

  <tr> 
    <td width="2%">&nbsp;</td>
    <td width="5%" align="left">
     &nbsp;
    </td>
    <td width="93%" align="left"> 
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      &nbsp;&nbsp;&nbsp;&nbsp; 
      &nbsp;<%= ejbcawebbean.getText("ONORBEFORE") %>&nbsp;
      <select name='<%= SELECT_DAY_ROW5  %>'> 
         <%  tempval =0;
             if(olddayrow5 != null){
               tempval = Integer.parseInt(olddayrow5);  
             }else{ 
                tempval = dayofmonth;
             }  
            for(int i=0; i <  31; i++) { %> 
         <option  <%   if(tempval == i) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
           <%= i %>
         </option> 
         <% } %> 
      </select>&nbsp;
      <select name='<%= SELECT_MONTH_ROW5  %>'>  
         <%  tempval =0;
             if(oldmonthrow5 != null){
               tempval = Integer.parseInt(oldmonthrow5);  
             }else{ 
                tempval = month;
             } 

             for(int i=0; i < monthreferences.length; i++) { %> 
         <option <%   if(tempval == i) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
          <%= ejbcawebbean.getText(monthreferences[i]) %>
         </option> 
         <% } %> 
      </select>&nbsp;
      <select name='<%= SELECT_YEAR_ROW5  %>'>  
         <%  tempval =0;
             if(oldyearrow5 != null){
               tempval = Integer.parseInt(oldyearrow5);  
             }else{ 
                tempval = year;
             } 
            for(int i=2000; i <  2020; i++) { %> 
         <option  <%   if(tempval == i) 
                        out.print(" selected ");
                      %>value='<%= i %>'> 
          <%= i %>
         </option> 
         <% } %> 
      </select>&nbsp;
      <select name='<%= SELECT_TIME_ROW5  %>'>  
          <% time = Calendar.getInstance();
             dateformat = DateFormat.getTimeInstance(DateFormat.SHORT);%>
         <%  tempval =0;
             if(oldtimerow5 != null){
               tempval = Integer.parseInt(oldtimerow5);  
             }else{ 
                tempval = hour;
             } 

           for(int i=0; i <  24; i++) { %> 
         <option  <%   if(tempval == i) 
                        out.print(" selected ");
                      %> value='<%= i %>'> 
          <% time.set(0,0,0,i,0); 
             out.print(dateformat.format(time.getTime()));%>
         </option> 
         <% } %> 
      </select>
    </td>
  </tr>
    <td width="2%">&nbsp;</td>
    <td width="5%" align="left">
    </td>
    <td width="93%" align="left"> <input type="submit" name="<%=BUTTON_ADVANCEDLIST %>" onclick='return checkfields()' value="<%= ejbcawebbean.getText("LIST") %>">
    </td>        
</table>