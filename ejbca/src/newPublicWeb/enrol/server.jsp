<%@ include file="header.jsp" %>
<h1 class="title">Enroll For Server Certificate</h1>
<p>Please give your username and password, paste the PEM-formated PKCS10 certification request into the field below and
   click OK to fetch your certificate. 
</p>

<p>A PEM-formatted request is a BASE64 encoded PKCS10 request starting with<br />
  <code>-----BEGIN CERTIFICATE REQUEST-----</code><br />
  and ending with<br />
  <code>-----END CERTIFICATE REQUEST-----</code>
</p>

<form name="EJBCA" action="../certreq" enctype="x-www-form-encoded" method="post">
  <fieldset >
    <legend>Enroll</legend>
	<label for="user">Username</label>
	<input type="text" size="10" name="user" id="user" value="foo" accesskey="u" />
	<br />
	<label for="password">Password</label>
	<input type="text" size="10" name="password" id="password" value="foo123" accesskey="p" />
	<br />
	<br />
	<label for="pkcs10req"></label>
	<textarea rows="15" cols="70" name="pkcs10req" id="pkcs10req"></textarea>
	<br />
	<br />
	<label for="resulttype">Result type</label>
	<select name="resulttype" id="resulttype">
		<option value="<%=org.ejbca.ui.web.RequestHelper.ENCODED_CERTIFICATE%>">PEM Certificate</option> 
		<option value="<%=org.ejbca.ui.web.RequestHelper.ENCODED_PKCS7%>">PKCS7</option>
	</select>
	<br />
	<label for="ok"></label>
	<input type="submit" id="ok" value="OK" />
  </fieldset>
</form>

<%@ include file="footer.inc" %>
