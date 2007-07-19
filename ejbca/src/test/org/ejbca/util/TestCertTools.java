/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.ejbca.util;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.DEREncodable;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.qualified.ETSIQCObjectIdentifiers;
import org.bouncycastle.asn1.x509.qualified.RFC3739QCObjectIdentifiers;
import org.bouncycastle.util.encoders.Hex;
import org.ejbca.util.cert.QCStatementExtension;
import org.ejbca.util.cert.SubjectDirAttrExtension;

import com.novell.ldap.LDAPDN;


/**
 * Tests the CertTools class .
 *
 * @version $Id: TestCertTools.java,v 1.6 2007-07-19 15:32:41 anatom Exp $
 */
public class TestCertTools extends TestCase {
    private static Logger log = Logger.getLogger(TestCertTools.class);
    private static byte[] testcert = Base64.decode(("MIIDATCCAmqgAwIBAgIIczEoghAwc3EwDQYJKoZIhvcNAQEFBQAwLzEPMA0GA1UE"
            + "AxMGVGVzdENBMQ8wDQYDVQQKEwZBbmFUb20xCzAJBgNVBAYTAlNFMB4XDTAzMDky"
            + "NDA2NDgwNFoXDTA1MDkyMzA2NTgwNFowMzEQMA4GA1UEAxMHcDEydGVzdDESMBAG"
            + "A1UEChMJUHJpbWVUZXN0MQswCQYDVQQGEwJTRTCBnTANBgkqhkiG9w0BAQEFAAOB"
            + "iwAwgYcCgYEAnPAtfpU63/0h6InBmesN8FYS47hMvq/sliSBOMU0VqzlNNXuhD8a"
            + "3FypGfnPXvjJP5YX9ORu1xAfTNao2sSHLtrkNJQBv6jCRIMYbjjo84UFab2qhhaJ"
            + "wqJgkQNKu2LHy5gFUztxD8JIuFPoayp1n9JL/gqFDv6k81UnDGmHeFcCARGjggEi"
            + "MIIBHjAPBgNVHRMBAf8EBTADAQEAMA8GA1UdDwEB/wQFAwMHoAAwOwYDVR0lBDQw"
            + "MgYIKwYBBQUHAwEGCCsGAQUFBwMCBggrBgEFBQcDBAYIKwYBBQUHAwUGCCsGAQUF"
            + "BwMHMB0GA1UdDgQWBBTnT1aQ9I0Ud4OEfNJkSOgJSrsIoDAfBgNVHSMEGDAWgBRj"
            + "e/R2qFQkjqV0pXdEpvReD1eSUTAiBgNVHREEGzAZoBcGCisGAQQBgjcUAgOgCQwH"
            + "Zm9vQGZvbzASBgNVHSAECzAJMAcGBSkBAQEBMEUGA1UdHwQ+MDwwOqA4oDaGNGh0"
            + "dHA6Ly8xMjcuMC4wLjE6ODA4MC9lamJjYS93ZWJkaXN0L2NlcnRkaXN0P2NtZD1j"
            + "cmwwDQYJKoZIhvcNAQEFBQADgYEAU4CCcLoSUDGXJAOO9hGhvxQiwjGD2rVKCLR4"
            + "emox1mlQ5rgO9sSel6jHkwceaq4A55+qXAjQVsuy76UJnc8ncYX8f98uSYKcjxo/"
            + "ifn1eHMbL8dGLd5bc2GNBZkmhFIEoDvbfn9jo7phlS8iyvF2YhC4eso8Xb+T7+BZ"
            + "QUOBOvc=").getBytes());

    private static byte[] guidcert = Base64.decode(
            ("MIIC+zCCAmSgAwIBAgIIBW0F4eGmH0YwDQYJKoZIhvcNAQEFBQAwMTERMA8GA1UE"
            +"AxMIQWRtaW5DQTExDzANBgNVBAoTBkFuYVRvbTELMAkGA1UEBhMCU0UwHhcNMDQw"
            +"OTE2MTc1NzQ1WhcNMDYwOTE2MTgwNzQ1WjAyMRQwEgYKCZImiZPyLGQBARMEZ3Vp"
            +"ZDENMAsGA1UEAxMER3VpZDELMAkGA1UEBhMCU0UwgZ8wDQYJKoZIhvcNAQEBBQAD"
            +"gY0AMIGJAoGBANdjsBcLJKUN4hzJU1p3cqaXhPgEjGul62/3xv+Gow+7oOYePcK8"
            +"bM5VO4zdQVWEhuGOZFaZ70YbXhei4F9kvqlN7xuG47g7DNZ0/fnRzvGY0BHmIR4Y"
            +"/U87oMEDa2Giy0WTjsmT14uzy4luFgqb2ZA3USGcyJ9hoT6j1WDyOxitAgMBAAGj"
            +"ggEZMIIBFTAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIFoDA7BgNVHSUENDAy"
            +"BggrBgEFBQcDAQYIKwYBBQUHAwIGCCsGAQUFBwMEBggrBgEFBQcDBQYIKwYBBQUH"
            +"AwcwHQYDVR0OBBYEFJlDddj88zI7tz3SPfdig0gw5IWvMB8GA1UdIwQYMBaAFI1k"
            +"9WhE1WXpeezZx/kM0qsoZyqVMHgGA1UdEQRxMG+BDGd1aWRAZm9vLmNvbYIMZ3Vp"
            +"ZC5mb28uY29thhRodHRwOi8vZ3VpZC5mb28uY29tL4cECgwNDqAcBgorBgEEAYI3"
            +"FAIDoA4MDGd1aWRAZm9vLmNvbaAXBgkrBgEEAYI3GQGgCgQIEjRWeJCrze8wDQYJ"
            +"KoZIhvcNAQEFBQADgYEAq39n6CZJgJnW0CH+QkcuU5F4RQveNPGiJzIJxUeOQ1yQ"
            +"gSkt3hvNwG4kLBmmwe9YLdS83dgNImMWL/DgID/47aENlBNai14CvtMceokik4IN"
            +"sacc7x/Vp3xezHLuBMcf3E3VSo4FwqcUYFmu7Obke3ebmB08nC6gnQHkzjNsmQw=").getBytes());

    private static byte[] altNameCert = Base64.decode(
            ("MIIDDzCCAfegAwIBAgIIPiL0klmu1uIwDQYJKoZIhvcNAQEFBQAwNzERMA8GA1UE"
             +"AxMIQWRtaW5DQTExFTATBgNVBAoTDEVKQkNBIFNhbXBsZTELMAkGA1UEBhMCU0Uw"
             +"HhcNMDUwODAyMTAxOTQ5WhcNMDcwODAyMTAyOTQ5WjAsMQwwCgYDVQQDEwNmb28x"
             +"DzANBgNVBAoTBkFuYVRvbTELMAkGA1UEBhMCU0UwXDANBgkqhkiG9w0BAQEFAANL"
             +"ADBIAkEAmMVWkkEMLbDNoB/NG3kJ22eC18syXqaHWRWc4DldFeCMGeLzfB2NklNv"
             +"hmr2kgIJcK+wyFpMkYm46dSMOrvovQIDAQABo4HxMIHuMAwGA1UdEwEB/wQCMAAw"
             +"DgYDVR0PAQH/BAQDAgWgMDsGA1UdJQQ0MDIGCCsGAQUFBwMBBggrBgEFBQcDAgYI"
             +"KwYBBQUHAwQGCCsGAQUFBwMFBggrBgEFBQcDBzAdBgNVHQ4EFgQUIV/Fck/+UVnw"
             +"tJigtZIF5OuuhlIwHwYDVR0jBBgwFoAUB/2KRYNOZxRDkJ5oChjNeXgwtCcwUQYD"
             +"VR0RBEowSIEKdG9tYXNAYS5zZYIId3d3LmEuc2WGEGh0dHA6Ly93d3cuYS5zZS+H"
             +"BAoBAQGgGAYKKwYBBAGCNxQCA6AKDAhmb29AYS5zZTANBgkqhkiG9w0BAQUFAAOC"
             +"AQEAfAGJM0/s+Yi1Ewmvt9Z/9w8X/T/02bF8P8MJG2H2eiIMCs/tkNhnlFGYYGhD"
             +"Km8ynveQZbdYvKFioOr/D19gMis/HNy9UDfOMrJdeGWiwxUHvKKbtcSlOPH3Hm0t"
             +"LSKomWdKfjTksfj69Tf01S0oNonprvwGxIdsa1uA9BC/MjkkPt1qEWkt/FWCfq9u"
             +"8Xyj2tZEJKjLgAW6qJ3ye81pEVKHgMmapWTQU2uI1qyEPYxoT9WkQtSObGI1wCqO"
             +"YmKglnd5BIUBPO9LOryyHlSRTID5z0UgDlrTAaNYuN8QOYF+DZEQxm4bSXTDooGX"
             +"rHjSjn/7Urb31CXWAxq0Zhk3fg==").getBytes());
    
    private static byte[] altNameCertWithDirectoryName = Base64.decode(("MIIFkjCCBPugAwIBAgIIBzGqGNsLMqwwDQYJKoZIhvcNAQEFBQAwWTEYMBYGA1UEAwwPU1VCX0NBX1dJTkRPV1MzMQ8wDQYDVQQLEwZQS0lHVkExHzAdBgNVBAoTFkdlbmVyYWxpdGF0IFZhbGVuY2lhbmExCzAJBgNVBAYTAkVTMB4XDTA2MDQyMTA5NDQ0OVoXDTA4MDQyMDA5NTQ0OVowcTEbMBkGCgmSJomT8ixkAQETC3Rlc3REaXJOYW1lMRQwEgYDVQQDEwt0ZXN0RGlyTmFtZTEOMAwGA1UECxMFbG9nb24xHzAdBgNVBAoTFkdlbmVyYWxpdGF0IFZhbGVuY2lhbmExCzAJBgNVBAYTAkVTMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCDLxMhz40RxCm21HoCBNa9x1UyPmhVkPdtt2V7dixgjOYz+ffKeebjn/jSd4nfXgd7fxpzezB8t673F2OtC3ENl1zek5Msj2KoinVu8vvZ78KMRq/H1rDFguhjSL0o19Cpob0qQFB/ukPZMNoKBNnMVnR1C4juB1eJVXWmHyJxIwIDAQABo4IDSTCCA0UwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCBaAwMwYDVR0lBCwwKgYIKwYBBQUHAwIGCCsGAQUFBwMEBggrBgEFBQcDBwYKKwYBBAGCNxQCAjAdBgNVHQ4EFgQUZz4hrh3dr6VWvEbAPe8pg7szNi4wHwYDVR0jBBgwFoAUTuOaap9UBpQ8dqwOufYoOQucfUowXAYDVR0RBFUwU6QhMB8xHTAbBgNVBAMMFHRlc3REaXJOYW1lfGRpcnxuYW1loC4GCisGAQQBgjcUAgOgIAwedGVzdERpck5hbWVAamFtYWRvci5wa2kuZ3ZhLmVzMIIBtgYDVR0gBIIBrTCCAakwggGlBgsrBgEEAb9VAwoBADCCAZQwggFeBggrBgEFBQcCAjCCAVAeggFMAEMAZQByAHQAaQBmAGkAYwBhAGQAbwAgAHIAZQBjAG8AbgBvAGMAaQBkAG8AIABkAGUAIABFAG4AdABpAGQAYQBkACAAZQB4AHAAZQBkAGkAZABvACAAcABvAHIAIABsAGEAIABBAHUAdABvAHIAaQBkAGEAZAAgAGQAZQAgAEMAZQByAHQAaQBmAGkAYwBhAGMAaQDzAG4AIABkAGUAIABsAGEAIABDAG8AbQB1AG4AaQB0AGEAdAAgAFYAYQBsAGUAbgBjAGkAYQBuAGEAIAAoAFAAbAAuACAATQBhAG4AaQBzAGUAcwAgADEALgAgAEMASQBGACAAUwA0ADYAMQAxADAAMAAxAEEAKQAuACAAQwBQAFMAIAB5ACAAQwBQACAAZQBuACAAaAB0AHQAcAA6AC8ALwB3AHcAdwAuAGEAYwBjAHYALgBlAHMwMAYIKwYBBQUHAgEWJGh0dHA6Ly93d3cuYWNjdi5lcy9sZWdpc2xhY2lvbl9jLmh0bTBDBgNVHR8EPDA6MDigNqA0hjJodHRwOi8vemFyYXRob3MuamFtYWRvci5ndmEuZXMvU1VCX0NBX1dJTkRPV1MzLmNybDBTBggrBgEFBQcBAQRHMEUwQwYIKwYBBQUHMAGGN2h0dHA6Ly91bGlrLnBraS5ndmEuZXM6ODA4MC9lamJjYS9wdWJsaWN3ZWIvc3RhdHVzL29jc3AwDQYJKoZIhvcNAQEFBQADgYEASofgaj06BOE847RTEgVba52lmPWADgeWxKHZAk1t9LdNzuFJ8B/SC3gi0rsAA/lQGSd4WzPbkmJKkVZ6Q9ybpqg4AJRaIZBkoQw1KNXPYAcgt5XLeIhUACdKIPhfPQr+vQtaC1wi5xV8EBCLpLmpzN9bpZdze/724UB4Y94KhII=").getBytes());

    /** The reference certificate from RFC3739 */
    private static byte[] qcRefCert = Base64.decode(
            ("MIIDEDCCAnmgAwIBAgIESZYC0jANBgkqhkiG9w0BAQUFADBIMQswCQYDVQQGEwJE"
            +"RTE5MDcGA1UECgwwR01EIC0gRm9yc2NodW5nc3plbnRydW0gSW5mb3JtYXRpb25z"
            +"dGVjaG5payBHbWJIMB4XDTA0MDIwMTEwMDAwMFoXDTA4MDIwMTEwMDAwMFowZTEL"
            +"MAkGA1UEBhMCREUxNzA1BgNVBAoMLkdNRCBGb3JzY2h1bmdzemVudHJ1bSBJbmZv"
            +"cm1hdGlvbnN0ZWNobmlrIEdtYkgxHTAMBgNVBCoMBVBldHJhMA0GA1UEBAwGQmFy"
            +"emluMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDc50zVodVa6wHPXswg88P8"
            +"p4fPy1caIaqKIK1d/wFRMN5yTl7T+VOS57sWxKcdDzGzqZJqjwjqAP3DqPK7AW3s"
            +"o7lBG6JZmiqMtlXG3+olv+3cc7WU+qDv5ZXGEqauW4x/DKGc7E/nq2BUZ2hLsjh9"
            +"Xy9+vbw+8KYE9rQEARdpJQIDAQABo4HpMIHmMGQGA1UdCQRdMFswEAYIKwYBBQUH"
            +"CQQxBBMCREUwDwYIKwYBBQUHCQMxAxMBRjAdBggrBgEFBQcJATERGA8xOTcxMTAx"
            +"NDEyMDAwMFowFwYIKwYBBQUHCQIxCwwJRGFybXN0YWR0MA4GA1UdDwEB/wQEAwIG"
            +"QDASBgNVHSAECzAJMAcGBSskCAEBMB8GA1UdIwQYMBaAFAABAgMEBQYHCAkKCwwN"
            +"Dg/+3LqYMDkGCCsGAQUFBwEDBC0wKzApBggrBgEFBQcLAjAdMBuBGW11bmljaXBh"
            +"bGl0eUBkYXJtc3RhZHQuZGUwDQYJKoZIhvcNAQEFBQADgYEAj4yAu7LYa3X04h+C"
            +"7+DyD2xViJCm5zEYg1m5x4znHJIMZsYAU/vJJIJQkPKVsIgm6vP/H1kXyAu0g2Ep"
            +"z+VWPnhZK1uw+ay1KRXw8rw2mR8hQ2Ug6QZHYdky2HH3H/69rWSPp888G8CW8RLU"
            +"uIKzn+GhapCuGoC4qWdlGLWqfpc=").getBytes());
    
    private static byte[] qcPrimeCert = Base64.decode(
            ("MIIDMDCCAhigAwIBAgIIUDIxBvlO2qcwDQYJKoZIhvcNAQEFBQAwNzERMA8GA1UE"
    		+"AxMIQWRtaW5DQTExFTATBgNVBAoTDEVKQkNBIFNhbXBsZTELMAkGA1UEBhMCU0Uw"
    		+"HhcNMDYwMTIyMDgxNTU0WhcNMDgwMTIyMDgyNTU0WjAOMQwwCgYDVQQDEwNxYzIw"
    		+"gZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKkuPOqOEWCJH9xb11sS++vfKb/z"
    		+"gHf2clwyf2vSFWTSDzQHOa2j5rwZ/F23X/mZl96fFAIfTBmr5dCwt0xAXZvTcKfO"
    		+"RAcKl7ZBXvsAYvwl1KIUpA8NqEbgjwA+OaTdND2vpAhII7PoU4CkoNajy44EuL3Y"
    		+"xP6KNWTMiks9KP5vAgMBAAGjgewwgekwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8E"
    		+"BAMCBPAwJwYDVR0lBCAwHgYIKwYBBQUHAwIGCCsGAQUFBwMEBggrBgEFBQcDBzAd"
    		+"BgNVHQ4EFgQUZsj/dUVp1FmOJpYZ2j5fYKIdXYowHwYDVR0jBBgwFoAUs8UBsa9O"
    		+"S1c8/I07DHYFJp0po0AwYAYIKwYBBQUHAQMEVDBSMCMGCCsGAQUFBwsBMBcGAykB"
    		+"AjAQgQ5xY0BwcmltZWtleS5zZTAIBgYEAI5GAQEwFwYGBACORgECMA0TA1NFSwID"
    		+"AMNQAgEAMAgGBgQAjkYBBDANBgkqhkiG9w0BAQUFAAOCAQEAjmL27XY5Wt0/axsI"
    		+"PbtcfrJ6xEm5PlYabM+T3I6lksov6Rz1+/n/L1S5poGPG8iOdJCExcnR0HbNkeB+"
    		+"2oPltqSaxyoSfGugVn/Oufz2BfFd7OCWe14dPsA181oC7/nq+mzhBpQ7App9JirA"
    		+"aeJQrcRDNK7vVOmg2LZ2oSYno/TuRTFq0GxsEVjEdzAxpAxY7N8ff6gY7IHd7+hc"
    		+"4GiFY+NnNp9Dvf6mOYTXLxsOc+093S7uK2ohhq99aYCkzJmrngtrImtKi0y/LMjq"
    		+"oviMCQmzMLY2Ifcw+CsOyQZx7nxwafZ7BAzm6vIvSeiIe3VlskRGzYDM66NJJNNo"
    		+"C2HsPA==").getBytes());

    private static byte[] aiaCert = Base64.decode(
            ("MIIDTTCCAjWgAwIBAgIIepmLoJzsjC8wDQYJKoZIhvcNAQEFBQAwNzERMA8GA1UE"
            +"AxMIQWRtaW5DQTExFTATBgNVBAoTDEVKQkNBIFNhbXBsZTELMAkGA1UEBhMCU0Uw"
            +"HhcNMDYwMjA5MTA0OTA1WhcNMDgwMjA5MTA1OTA1WjAqMQ0wCwYDVQQDEwRmb280"
            +"MQwwCgYDVQQKEwNGb28xCzAJBgNVBAYTAlNFMIGfMA0GCSqGSIb3DQEBAQUAA4GN"
            +"ADCBiQKBgQCSsptDGz1XODuTKBGGCY/Y6B6bfw22LVxaIbCx9Ih+qghlwJ2HYRcl"
            +"OpyGiMMsiTZADH4hL8WRam/8aq0x45YfQ8wSdxUkWSoVL0oahAbvY4h5J4S0hLrv"
            +"8Z9CVcUvuH/StTtWHOh4af0klTvLwcnyGhswkSrwM8a3grQvGSIN5wIDAQABo4Ht"
            +"MIHqMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgWgMDsGA1UdJQQ0MDIGCCsG"
            +"AQUFBwMBBggrBgEFBQcDAgYIKwYBBQUHAwQGCCsGAQUFBwMFBggrBgEFBQcDBzAd"
            +"BgNVHQ4EFgQUCFwQPEQjTdWh27GEMxmV/onyADgwHwYDVR0jBBgwFoAUB/2KRYNO"
            +"ZxRDkJ5oChjNeXgwtCcwTQYIKwYBBQUHAQEEQTA/MD0GCCsGAQUFBzABhjFodHRw"
            +"Oi8vbG9jYWxob3N0OjgwODAvZWpiY2EvcHVibGljd2ViL3N0YXR1cy9vY3NwMA0G"
            +"CSqGSIb3DQEBBQUAA4IBAQAe6ild0bNz6wD0bPhuumG5j5+9rDaPFebaYqV/AoEU"
            +"4kovLzvqhPqUR/zQOEx9SSFFs+pxY6YMYDYha7mFrjpCSWr9wGNyv4BRAOMAl2YX"
            +"P3DfYh/etqUySTuYLzDi65SOSRuvYPP9jJPWt0Ucsm10A10yqJITcAFVajTfNj0r"
            +"WtTQ4Hbz/U5xkThvzCcx9Z3vIg1k0b5i3qs0JlDFxdWnTGCAn0TGBdsFFvAcSlJR"
            +"UBSOmiFi7edaayqV8qMyNirSA2tOdOzcTr8zyGfozaHRVmMqTmpSOe1t/LyIK5uh"
            +"tjsFYZQuz5pxRzvzXKmhKwzRTaJLPezBsIvhIZh41qTu").getBytes());

    private static byte[] subjDirAttrCert = Base64.decode(
            ("MIIGmTCCBYGgAwIBAgIQGMYCpWmOBXXOL2ODrM8FHzANBgkqhkiG9w0BAQUFADBx"
+"MQswCQYDVQQGEwJUUjEoMCYGA1UEChMfRWxla3Ryb25payBCaWxnaSBHdXZlbmxp"
+"Z2kgQS5TLjE4MDYGA1UEAxMvZS1HdXZlbiBFbGVrdHJvbmlrIFNlcnRpZmlrYSBI"
+"aXptZXQgU2FnbGF5aWNpc2kwHhcNMDYwMzI4MDAwMDAwWhcNMDcwMzI4MjM1OTU5"
+"WjCCAR0xCzAJBgNVBAYTAlRSMSgwJgYDVQQKDB9FbGVrdHJvbmlrIEJpbGdpIEd1"
+"dmVubGlnaSBBLlMuMQ8wDQYDVQQLDAZHS05FU0kxFDASBgNVBAUTCzIyOTI0NTQ1"
+"MDkyMRswGQYDVQQLDBJEb2d1bSBZZXJpIC0gQlVSU0ExIjAgBgNVBAsMGURvZ3Vt"
+"IFRhcmloaSAtIDAxLjA4LjE5NzcxPjA8BgNVBAsMNU1hZGRpIFPEsW7EsXIgLSA1"
+"MC4wMDAgWVRMLTIuMTYuNzkyLjEuNjEuMC4xLjUwNzAuMS4yMRcwFQYDVQQDDA5Z"
+"QVPEsE4gQkVDRU7EsDEjMCEGCSqGSIb3DQEJARYUeWFzaW5AdHVya2VrdWwuYXYu"
+"dHIwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKaJXVLvXC7qyjiqTAlM582X"
+"GPdQJxUfRxgTm6jlBZKtEhbWN5hbH4ASJTzmXWryGricejdKM+JBJECFdelyWPHs"
+"UkEL/U0uft3KLIdYo72oTibaL3j4vkEhjyubikSdl9CywkY6WS8nV9JNc66QOYxE"
+"5ZdE5CR19ScIYcOh7YpxAgMBAAGjggMBMIIC/TAJBgNVHRMEAjAAMAsGA1UdDwQE"
+"AwIGwDBWBgNVHR8ETzBNMEugSaBHhkVodHRwOi8vY3JsLmUtZ3V2ZW4uY29tL0Vs"
+"ZWt0cm9uaWtCaWxnaUd1dmVubGlnaUFTR0tORVNJL0xhdGVzdENSTC5jcmwwHwYD"
+"VR0jBBgwFoAUyT6jfNNisqvczhIzwmTXZTTyfrowggEcBgNVHSAEggETMIIBDzCB"
+"/wYJYIYYAwABAQECMIHxMDYGCCsGAQUFBwIBFipodHRwczovL3d3dy5lLWd1dmVu"
+"LmNvbS9lLWltemEvYmlsZ2lkZXBvc3UwgbYGCCsGAQUFBwICMIGpGoGmQnUgc2Vy"
+"dGlmaWthLCA1MDcwIHNhef1s/SBFbGVrdHJvbmlrIN1temEgS2FudW51bmEgZ/Zy"
+"ZSBuaXRlbGlrbGkgZWxla3Ryb25payBzZXJ0aWZpa2Fk/XIuIE9JRDogMi4xNi43"
+"OTIuMS42MS4wLjEuNTA3MC4xLjEgLSBPSUQ6IDAuNC4wLjE0NTYuMS4yIC0gT0lE"
+"OiAwLjQuMC4xODYyLjEuMTALBglghhgDAAEBBQQwgaEGCCsGAQUFBwEDBIGUMIGR"
+"MHYGCCsGAQUFBwsBMGoGC2CGGAE9AAGnTgEBMFuGWUJ1IFNlcnRpZmlrYSA1MDcw"
+"IHNhef1s/SBFbGVrdHJvbmlrIN1temEgS2FudW51bmEgZ/ZyZSBuaXRlbGlrbGkg"
+"ZWxla3Ryb25payBzZXJ0aWZpa2Fk/XIuMBcGBgQAjkYBAjANEwNZVEwCAwDDUAIB"
+"ADB2BggrBgEFBQcBAQRqMGgwIwYIKwYBBQUHMAGGF2h0dHA6Ly9vY3NwLmUtZ3V2"
+"ZW4uY29tMCIGCCsGAQUFBzAChhZodHRwOi8vd3d3LmUtZ3V2ZW4uY29tMB0GAytv"
+"DoYWaHR0cDovL3d3dy5lLWd1dmVuLmNvbTAbBgNVHQkEFDASMBAGCCsGAQUFBwkE"
+"MQQTAlRSMBEGCWCGSAGG+EIBAQQEAwIHgDANBgkqhkiG9w0BAQUFAAOCAQEA3yVY"
+"rURakBcrfv1hJjhDg7+ylCjXf9q6yP2E03kG4t606TLIyqWoqGkrndMtanp+a440"
+"rLPIe456XfRJBilj99H0NjzKACAVfLMTL8h/JBGLDYJJYA1S8PzBnMLHA8dhfBJ7"
+"StYEPM9BKW/WuBfOOdBNrRZtYKCHwGK2JANfM/JlfzOyG4A+XDQcgjiNoosjes1P"
+"qUHsaccIy0MM7FLMVV0HJNNQ84N9CuKIrBSSWopOudkajVqNtI3+FCcy+yXiH6LX"
+"fmpHZ346zprcafcjQmAiKfzPSljruvGDIVI3WN7S7WOMrx6MDq54626cZzQl9GFT"
+"D1gNo3fjOFhK33DY1Q==").getBytes());

    private static byte[] subjDirAttrCert2 = Base64.decode(
            ("MIIEsjCCA5qgAwIBAgIIFsYK/Jx7XEEwDQYJKoZIhvcNAQEFBQAwNzERMA8GA1UE"
+"AxMIQWRtaW5DQTExFTATBgNVBAoTDEVKQkNBIFNhbXBsZTELMAkGA1UEBhMCU0Uw"
+"HhcNMDYwNTMwMDcxNjU2WhcNMDgwNTI5MDcyNjU2WjA5MRkwFwYDVQQDExBUb21h"
+"cyBHdXN0YXZzc29uMQ8wDQYDVQQKEwZGb29PcmcxCzAJBgNVBAYTAlNFMIGfMA0G"
+"CSqGSIb3DQEBAQUAA4GNADCBiQKBgQCvhUYzNVW6iG5TpYi2Dr9VX37g05jcGEyP"
+"Lix05oxs3FnzPUf6ykxGy4nUYO12PfC6u9Gh+zelFfg6nKNQqYI48D4ufJc928Nx"
+"dZQZi41UmnFT5UXn3JcG4DQe0wZp+BKCch/UbtRjuE6iNxH24R//8W4wXc1R++FG"
+"5V6CQzHxXwIDAQABo4ICQjCCAj4wDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMC"
+"BPAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMB0GA1UdDgQWBBQ54I1p"
+"TGNwAeQEdnmcjNT+XMMjsjAfBgNVHSMEGDAWgBRzBo+b/XQZqq0DU6J10x17GoKS"
+"sDBMBgNVHSAERTBDMEEGAykBATA6MB4GCCsGAQUFBwICMBIeEABGAPYA9gBCAGEA"
+"cgDkAOQwGAYIKwYBBQUHAgEWDGh0dHA6LzExMS5zZTBuBgNVHR8EZzBlMGOgYaBf"
+"hl1odHRwOi8vbG9jYWxob3N0OjgwODAvZWpiY2EvcHVibGljd2ViL3dlYmRpc3Qv"
+"Y2VydGRpc3Q/Y21kPWNybCZpc3N1ZXI9Q049VGVzdENBLE89QW5hVG9tLEM9U0Uw"
+"TQYIKwYBBQUHAQEEQTA/MD0GCCsGAQUFBzABhjFodHRwOi8vbG9jYWxob3N0Ojgw"
+"ODAvZWpiY2EvcHVibGljd2ViL3N0YXR1cy9vY3NwMDoGCCsGAQUFBwEDBC4wLDAg"
+"BggrBgEFBQcLAjAUMBKBEHJhQGNvbW1maWRlcy5jb20wCAYGBACORgEBMHYGA1Ud"
+"CQRvMG0wEAYIKwYBBQUHCQUxBBMCU0UwEAYIKwYBBQUHCQQxBBMCU0UwDwYIKwYB"
+"BQUHCQMxAxMBTTAXBggrBgEFBQcJAjELEwlTdG9ja2hvbG0wHQYIKwYBBQUHCQEx"
+"ERgPMTk3MTA0MjUxMjAwMDBaMA0GCSqGSIb3DQEBBQUAA4IBAQA+vgNnGjw29xEs"
+"cnJi7wInUBvtTzQ4+SVSBPTzNA/ZEk+CJVsr/2xbPl+SShZ0SHObj9un1kwKst4n"
+"zcNqsnBorrluM92Z5gYwDN3mRGF0szbYEshr/KezMhY2MdXkE+i3nEx6awdemuCG"
+"g+LAfL4ODLAzAJJI4MfF+fz0IK7Zeobo1aVGS6Ii9sEnDdQOsLbdfHBNccrT353d"
+"NAwxPGnfunGBQ+Los6vjDApy/szMT32NFJDe4WTmkDxqYJQqQjhdrHTxpFEr0VQB"
+"s7KRRCYjga/Z52XytwwDBLFM9CPZJfyKxZTV9I9i6e0xSn2xEW8NRplY1HOKa/2B"
+"VzvWW9G5").getBytes());

    /**
     * Creates a new TestCertTools object.
     *
     * @param name DOCUMENT ME!
     */
    public TestCertTools(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        log.debug(">setUp()");
        CertTools.installBCProvider();
        log.debug("<setUp()");
    }

    protected void tearDown() throws Exception {
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void test01GetPartFromDN() throws Exception {
        log.debug(">test01GetPartFromDN()");

        // We try to examine the general case and som special cases, which we want to be able to handle
        String dn0 = "C=SE, O=AnaTom, CN=foo";
        assertEquals(CertTools.getPartFromDN(dn0, "CN"), "foo");
        assertEquals(CertTools.getPartFromDN(dn0, "O"), "AnaTom");
        assertEquals(CertTools.getPartFromDN(dn0, "C"), "SE");
        assertEquals(CertTools.getPartFromDN(dn0, "cn"), "foo");
        assertEquals(CertTools.getPartFromDN(dn0, "o"), "AnaTom");
        assertEquals(CertTools.getPartFromDN(dn0, "c"), "SE");

        String dn1 = "c=SE, o=AnaTom, cn=foo";
        assertEquals(CertTools.getPartFromDN(dn1, "CN"), "foo");
        assertEquals(CertTools.getPartFromDN(dn1, "O"), "AnaTom");
        assertEquals(CertTools.getPartFromDN(dn1, "C"), "SE");
        assertEquals(CertTools.getPartFromDN(dn1, "cn"), "foo");
        assertEquals(CertTools.getPartFromDN(dn1, "o"), "AnaTom");
        assertEquals(CertTools.getPartFromDN(dn1, "c"), "SE");

        String dn2 = "C=SE, O=AnaTom, CN=cn";
        assertEquals(CertTools.getPartFromDN(dn2, "CN"), "cn");

        String dn3 = "C=SE, O=AnaTom, CN=CN";
        assertEquals(CertTools.getPartFromDN(dn3, "CN"), "CN");

        String dn4 = "C=CN, O=AnaTom, CN=foo";
        assertEquals(CertTools.getPartFromDN(dn4, "CN"), "foo");

        String dn5 = "C=cn, O=AnaTom, CN=foo";
        assertEquals(CertTools.getPartFromDN(dn5, "CN"), "foo");

        String dn6 = "CN=foo, O=PrimeKey, C=SE";
        assertEquals(CertTools.getPartFromDN(dn6, "CN"), "foo");
        assertEquals(CertTools.getPartFromDN(dn6, "O"), "PrimeKey");
        assertEquals(CertTools.getPartFromDN(dn6, "C"), "SE");

        String dn7 = "CN=foo, O=PrimeKey, C=cn";
        assertEquals(CertTools.getPartFromDN(dn7, "CN"), "foo");
        assertEquals(CertTools.getPartFromDN(dn7, "C"), "cn");

        String dn8 = "CN=foo, O=PrimeKey, C=CN";
        assertEquals(CertTools.getPartFromDN(dn8, "CN"), "foo");
        assertEquals(CertTools.getPartFromDN(dn8, "C"), "CN");

        String dn9 = "CN=foo, O=CN, C=CN";
        assertEquals(CertTools.getPartFromDN(dn9, "CN"), "foo");
        assertEquals(CertTools.getPartFromDN(dn9, "O"), "CN");

        String dn10 = "CN=foo, CN=bar,O=CN, C=CN";
        assertEquals(CertTools.getPartFromDN(dn10, "CN"), "foo");
        assertEquals(CertTools.getPartFromDN(dn10, "O"), "CN");

        String dn11 = "CN=foo,CN=bar, O=CN, C=CN";
        assertEquals(CertTools.getPartFromDN(dn11, "CN"), "foo");
        assertEquals(CertTools.getPartFromDN(dn11, "O"), "CN");

        String dn12 = "CN=\"foo, OU=bar\", O=baz\\\\\\, quux,C=C";
        assertEquals(CertTools.getPartFromDN(dn12, "CN"), "foo, OU=bar");
        assertEquals(CertTools.getPartFromDN(dn12, "O"), "baz\\, quux");
        assertNull(CertTools.getPartFromDN(dn12, "OU"));

        String dn13 = "C=SE, O=PrimeKey, EmailAddress=foo@primekey.se";
        ArrayList emails = CertTools.getEmailFromDN(dn13);
        assertEquals((String)emails.get(0), "foo@primekey.se");

        String dn14 = "C=SE, E=foo@primekey.se, O=PrimeKey";
        emails = CertTools.getEmailFromDN(dn14);
        assertEquals((String)emails.get(0), "foo@primekey.se");

        String dn15 = "C=SE, E=foo@primekey.se, O=PrimeKey, EmailAddress=bar@primekey.se";
        emails = CertTools.getEmailFromDN(dn15);
        assertEquals((String)emails.get(0), "bar@primekey.se");

        log.debug("<test01GetPartFromDN()");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void test02StringToBCDNString() throws Exception {
        log.debug(">test02StringToBCDNString()");

        // We try to examine the general case and som special cases, which we want to be able to handle
        String dn1 = "C=SE, O=AnaTom, CN=foo";
        assertEquals(CertTools.stringToBCDNString(dn1), "CN=foo,O=AnaTom,C=SE");

        String dn2 = "C=SE, O=AnaTom, CN=cn";
        assertEquals(CertTools.stringToBCDNString(dn2), "CN=cn,O=AnaTom,C=SE");

        String dn3 = "CN=foo, O=PrimeKey, C=SE";
        assertEquals(CertTools.stringToBCDNString(dn3), "CN=foo,O=PrimeKey,C=SE");

        String dn4 = "cn=foo, o=PrimeKey, c=SE";
        assertEquals(CertTools.stringToBCDNString(dn4), "CN=foo,O=PrimeKey,C=SE");

        String dn5 = "cn=foo,o=PrimeKey,c=SE";
        assertEquals(CertTools.stringToBCDNString(dn5), "CN=foo,O=PrimeKey,C=SE");

        String dn6 = "C=SE, O=AnaTom, CN=CN";
        assertEquals(CertTools.stringToBCDNString(dn6), "CN=CN,O=AnaTom,C=SE");

        String dn7 = "C=CN, O=AnaTom, CN=foo";
        assertEquals(CertTools.stringToBCDNString(dn7), "CN=foo,O=AnaTom,C=CN");

        String dn8 = "C=cn, O=AnaTom, CN=foo";
        assertEquals(CertTools.stringToBCDNString(dn8), "CN=foo,O=AnaTom,C=cn");

        String dn9 = "CN=foo, O=PrimeKey, C=CN";
        assertEquals(CertTools.stringToBCDNString(dn9), "CN=foo,O=PrimeKey,C=CN");

        String dn10 = "CN=foo, O=PrimeKey, C=cn";
        assertEquals(CertTools.stringToBCDNString(dn10), "CN=foo,O=PrimeKey,C=cn");

        String dn11 = "CN=foo, O=CN, C=CN";
        assertEquals(CertTools.stringToBCDNString(dn11), "CN=foo,O=CN,C=CN");

        String dn12 = "O=PrimeKey,C=SE,CN=CN";
        assertEquals(CertTools.stringToBCDNString(dn12), "CN=CN,O=PrimeKey,C=SE");

        String dn13 = "O=PrimeKey,C=SE,CN=CN, OU=FooOU";
        assertEquals(CertTools.stringToBCDNString(dn13), "CN=CN,OU=FooOU,O=PrimeKey,C=SE");

        String dn14 = "O=PrimeKey,C=CN,CN=CN, OU=FooOU";
        assertEquals(CertTools.stringToBCDNString(dn14), "CN=CN,OU=FooOU,O=PrimeKey,C=CN");

        String dn15 = "O=PrimeKey,C=CN,CN=cn, OU=FooOU";
        assertEquals(CertTools.stringToBCDNString(dn15), "CN=cn,OU=FooOU,O=PrimeKey,C=CN");

        String dn16 = "CN=foo, CN=bar,O=CN, C=CN";
        assertEquals(CertTools.stringToBCDNString(dn16), "CN=foo,CN=bar,O=CN,C=CN");

        String dn17 = "CN=foo,CN=bar, O=CN, O=C, C=CN";
        assertEquals(CertTools.stringToBCDNString(dn17), "CN=foo,CN=bar,O=CN,O=C,C=CN");

        String dn18 = "cn=jean,cn=EJBCA,dc=home,dc=jean";
        assertEquals(CertTools.stringToBCDNString(dn18), "CN=jean,CN=EJBCA,DC=home,DC=jean");

        String dn19 = "cn=bar, cn=foo,o=oo, O=EJBCA,DC=DC2, dc=dc1, C=SE";
        assertEquals(CertTools.stringToBCDNString(dn19), "CN=bar,CN=foo,O=oo,O=EJBCA,DC=DC2,DC=dc1,C=SE");

        String dn20 = " CN=\"foo, OU=bar\",  O=baz\\\\\\, quux,C=SE ";
        // BC always escapes with backslash, it doesn't use quotes.
        assertEquals(CertTools.stringToBCDNString(dn20), "CN=foo\\, OU=bar,O=baz\\\\\\, quux,C=SE");

        String dn21 = "C=SE,O=Foo\\, Inc, OU=Foo\\, Dep, CN=Foo\\'";
        String bcdn21 = CertTools.stringToBCDNString(dn21);
        assertEquals(bcdn21, "CN=Foo\',OU=Foo\\, Dep,O=Foo\\, Inc,C=SE");
        // it is allowed to escape ,
        assertEquals(StringTools.strip(bcdn21), "CN=Foo',OU=Foo\\, Dep,O=Foo\\, Inc,C=SE");

        String dn22 = "C=SE,O=Foo\\, Inc, OU=Foo, Dep, CN=Foo'";
        String bcdn22 = CertTools.stringToBCDNString(dn22);
        assertEquals(bcdn22, "CN=Foo',OU=Foo,O=Foo\\, Inc,C=SE");
        assertEquals(StringTools.strip(bcdn22), "CN=Foo',OU=Foo,O=Foo\\, Inc,C=SE");
        
        String dn23 = "C=SE,O=Foo, OU=FooOU, CN=Foo, DN=qualf";
        String bcdn23 = CertTools.stringToBCDNString(dn23);
        assertEquals(bcdn23, "DN=qualf,CN=Foo,OU=FooOU,O=Foo,C=SE");
        assertEquals(StringTools.strip(bcdn23), "DN=qualf,CN=Foo,OU=FooOU,O=Foo,C=SE");

        log.debug("<test02StringToBCDNString()");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void test03AltNames() throws Exception {
        log.debug(">test03AltNames()");

        // We try to examine the general case and som special cases, which we want to be able to handle
        String alt1 = "rfc822Name=ejbca@primekey.se, dNSName=www.primekey.se, uri=http://www.primekey.se/ejbca";
        assertEquals(CertTools.getPartFromDN(alt1, CertTools.EMAIL), "ejbca@primekey.se");
        assertNull(CertTools.getPartFromDN(alt1, CertTools.EMAIL1));
        assertNull(CertTools.getPartFromDN(alt1, CertTools.EMAIL2));
        assertEquals(CertTools.getPartFromDN(alt1, CertTools.DNS), "www.primekey.se");
        assertNull(CertTools.getPartFromDN(alt1, CertTools.URI));
        assertEquals(CertTools.getPartFromDN(alt1, CertTools.URI1), "http://www.primekey.se/ejbca");

        String alt2 = "email=ejbca@primekey.se, dNSName=www.primekey.se, uniformResourceIdentifier=http://www.primekey.se/ejbca";
        assertEquals(CertTools.getPartFromDN(alt2, CertTools.EMAIL1), "ejbca@primekey.se");
        assertEquals(CertTools.getPartFromDN(alt2, CertTools.URI), "http://www.primekey.se/ejbca");

        String alt3 = "EmailAddress=ejbca@primekey.se, dNSName=www.primekey.se, uniformResourceIdentifier=http://www.primekey.se/ejbca";
        assertEquals(CertTools.getPartFromDN(alt3, CertTools.EMAIL2), "ejbca@primekey.se");

        X509Certificate cert = CertTools.getCertfromByteArray(guidcert);
        String upn = CertTools.getUPNAltName(cert);
        assertEquals(upn, "guid@foo.com");
        String guid = CertTools.getGuidAltName(cert);
        assertEquals(guid, "1234567890abcdef");
        
        String customAlt = "rfc822Name=foo@bar.com";
        ArrayList oids = CertTools.getCustomOids(customAlt);
        assertEquals(0, oids.size());
        customAlt = "rfc822Name=foo@bar.com, 1.1.1.1.2=foobar, 1.2.2.2.2=barfoo";
        oids = CertTools.getCustomOids(customAlt);
        assertEquals(2, oids.size());
        String oid1 = (String)oids.get(0);
        assertEquals("1.1.1.1.2", oid1);
        String oid2 = (String)oids.get(1);
        assertEquals("1.2.2.2.2", oid2);
        String val1 = CertTools.getPartFromDN(customAlt, oid1);
        assertEquals("foobar", val1);
        String val2 = CertTools.getPartFromDN(customAlt, oid2);
        assertEquals("barfoo", val2);
        
        log.debug("<test03AltNames()");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void test04DNComponents() throws Exception {
        log.debug(">test04DNComponents()");

        // We try to examine the general case and som special cases, which we want to be able to handle
        String dn1 = "CN=CommonName, O=Org, OU=OrgUnit, SerialNumber=SerialNumber, SurName=SurName, GivenName=GivenName, Initials=Initials, C=SE";
        String bcdn1 = CertTools.stringToBCDNString(dn1);
        log.debug("dn1: " + dn1);
        log.debug("bcdn1: " + bcdn1);
        assertEquals(bcdn1,
                "CN=CommonName,SN=SerialNumber,GIVENNAME=GivenName,INITIALS=Initials,SURNAME=SurName,OU=OrgUnit,O=Org,C=SE");

        dn1 = "CN=CommonName, O=Org, OU=OrgUnit, SerialNumber=SerialNumber, SurName=SurName, GivenName=GivenName, Initials=Initials, C=SE, 1.1.1.1=1111Oid, 2.2.2.2=2222Oid";
        bcdn1 = CertTools.stringToBCDNString(dn1);
        log.debug("dn1: " + dn1);
        log.debug("bcdn1: " + bcdn1);
        assertEquals(bcdn1,
                "CN=CommonName,SN=SerialNumber,GIVENNAME=GivenName,INITIALS=Initials,SURNAME=SurName,OU=OrgUnit,O=Org,C=SE,2.2.2.2=2222Oid,1.1.1.1=1111Oid");

        dn1 = "CN=CommonName, 3.3.3.3=3333Oid,O=Org, OU=OrgUnit, SerialNumber=SerialNumber, SurName=SurName, GivenName=GivenName, Initials=Initials, C=SE, 1.1.1.1=1111Oid, 2.2.2.2=2222Oid";
        bcdn1 = CertTools.stringToBCDNString(dn1);
        log.debug("dn1: " + dn1);
        log.debug("bcdn1: " + bcdn1);
        // 3.3.3.3 is not a valid OID so it should be silently dropped
        assertEquals(bcdn1,"CN=CommonName,SN=SerialNumber,GIVENNAME=GivenName,INITIALS=Initials,SURNAME=SurName,OU=OrgUnit,O=Org,C=SE,2.2.2.2=2222Oid,1.1.1.1=1111Oid");

        dn1 = "CN=CommonName, 2.3.3.3=3333Oid,O=Org, K=KKK, OU=OrgUnit, SerialNumber=SerialNumber, SurName=SurName, GivenName=GivenName, Initials=Initials, C=SE, 1.1.1.1=1111Oid, 2.2.2.2=2222Oid";
        bcdn1 = CertTools.stringToBCDNString(dn1);
        log.debug("dn1: " + dn1);
        log.debug("bcdn1: " + bcdn1);
        assertEquals(bcdn1,
                "CN=CommonName,SN=SerialNumber,GIVENNAME=GivenName,INITIALS=Initials,SURNAME=SurName,OU=OrgUnit,O=Org,C=SE,2.2.2.2=2222Oid,1.1.1.1=1111Oid,2.3.3.3=3333Oid");

        log.debug("<test04DNComponents()");
    }

    /** Tests string coding/decoding international (swedish characters)
     *
     * @throws Exception if error...
     */
    public void test05IntlChars() throws Exception {
        log.debug(">test05IntlChars()");
        // We try to examine the general case and som special cases, which we want to be able to handle
        String dn1 = "CN=Tomas?????????, O=?????????-Org, OU=??????-Unit, C=SE";
        String bcdn1 = CertTools.stringToBCDNString(dn1);
        log.debug("dn1: " + dn1);
        log.debug("bcdn1: " + bcdn1);
        assertEquals("CN=Tomas?????????,OU=??????-Unit,O=?????????-Org,C=SE", bcdn1);
        log.debug("<test05IntlChars()");
    }

    /** Tests some of the other methods of CertTools
     *
     * @throws Exception if error...
     */
    public void test06CertOps() throws Exception {
        log.debug(">test06CertOps()");
        X509Certificate cert = CertTools.getCertfromByteArray(testcert);
        X509Certificate gcert = CertTools.getCertfromByteArray(guidcert);
        assertEquals("Wrong issuerDN", CertTools.getIssuerDN(cert), CertTools.stringToBCDNString("CN=TestCA,O=AnaTom,C=SE"));
        assertEquals("Wrong subjectDN", CertTools.getSubjectDN(cert), CertTools.stringToBCDNString("CN=p12test,O=PrimeTest,C=SE"));
        assertEquals("Wrong subject key id", new String(Hex.encode(CertTools.getSubjectKeyId(cert))), "E74F5690F48D147783847CD26448E8094ABB08A0".toLowerCase());
        assertEquals("Wrong authority key id", new String(Hex.encode(CertTools.getAuthorityKeyId(cert))), "637BF476A854248EA574A57744A6F45E0F579251".toLowerCase());
        assertEquals("Wrong upn alt name", "foo@foo", CertTools.getUPNAltName(cert));
        assertEquals("Wrong guid alt name", "1234567890abcdef", CertTools.getGuidAltName(gcert));
        assertEquals("Wrong certificate policy", "1.1.1.1.1.1", CertTools.getCertificatePolicyId(cert, 0));
        assertNull("Not null policy", CertTools.getCertificatePolicyId(cert, 1));
//        System.out.println(cert);
//        FileOutputStream fos = new FileOutputStream("foo.cert");
//        fos.write(cert.getEncoded());
//        fos.close();
        log.debug("<test06CertOps()");
    }

    /** Tests the handling of DC components
     *
     * @throws Exception if error...
     */
    public void test07TestDC() throws Exception {
        log.debug(">test07TestDC()");
        // We try to examine the that we handle modern dc components for ldap correctly
        String dn1 = "dc=bigcorp,dc=com,dc=se,ou=users,cn=Mike Jackson";
        String bcdn1 = CertTools.stringToBCDNString(dn1);
        log.debug("dn1: " + dn1);
        log.debug("bcdn1: " + bcdn1);
        //assertEquals("CN=Mike Jackson,OU=users,DC=se,DC=bigcorp,DC=com", bcdn1);
        String dn2 = "cn=Mike Jackson,ou=users,dc=se,dc=bigcorp,dc=com";
        String bcdn2 = CertTools.stringToBCDNString(dn2);
        log.debug("dn2: " + dn2);
        log.debug("bcdn2: " + bcdn2);
        assertEquals("CN=Mike Jackson,OU=users,DC=se,DC=bigcorp,DC=com", bcdn2);
        log.debug("<test07TestDC()");
    }

    /** Tests the handling of unstructuredName/Address
     *
     * @throws Exception if error...
     */
    public void test08TestUnstructured() throws Exception {
        log.debug(">test08TestUnstructured()");
        // We try to examine the that we handle modern dc components for ldap correctly
        String dn1 = "C=SE,O=PrimeKey,unstructuredName=10.1.1.2,unstructuredAddress=foo.bar.se,cn=test";
        String bcdn1 = CertTools.stringToBCDNString(dn1);
        log.debug("dn1: " + dn1);
        log.debug("bcdn1: " + bcdn1);
        assertEquals("unstructuredAddress=foo.bar.se,unstructuredName=10.1.1.2,CN=test,O=PrimeKey,C=SE", bcdn1);
        log.debug("<test08TestUnstructured()");
    }

    /** Tests the reversing of a DN
    *
    * @throws Exception if error...
    */
   public void test09TestReverse() throws Exception {
       log.debug(">test09TestReverse()");
       // We try to examine the that we handle modern dc components for ldap correctly
       String dn1 = "dc=com,dc=bigcorp,dc=se,ou=orgunit,ou=users,cn=Tomas G";
       String dn2 = "cn=Tomas G,ou=users,ou=orgunit,dc=se,dc=bigcorp,dc=com";
       assertTrue(CertTools.isDNReversed(dn1));
       assertTrue(!CertTools.isDNReversed(dn2));
       assertTrue(CertTools.isDNReversed("C=SE,CN=Foo"));
       assertTrue(!CertTools.isDNReversed("CN=Foo,O=FooO"));
       String revdn1 = CertTools.reverseDN(dn1);
       log.debug("dn1: " + dn1);
       log.debug("revdn1: " + revdn1);
       assertEquals(dn2, revdn1);
       
       log.debug("<test09TestReverse()");
   }
    /** Tests the handling of DC components
    *
    * @throws Exception if error...
    */
   public void test10TestMultipleReversed() throws Exception {
       log.debug(">test10TestMultipleReversed()");
       // We try to examine the that we handle modern dc components for ldap correctly
       String dn1 = "dc=com,dc=bigcorp,dc=se,ou=orgunit,ou=users,cn=Tomas G";
       String bcdn1 = CertTools.stringToBCDNString(dn1);
       log.debug("dn1: " + dn1);
       log.debug("bcdn1: " + bcdn1);
       assertEquals("CN=Tomas G,OU=users,OU=orgunit,DC=se,DC=bigcorp,DC=com", bcdn1);

       String dn19 = "C=SE, dc=dc1,DC=DC2,O=EJBCA, O=oo, cn=foo, cn=bar";
       assertEquals("CN=bar,CN=foo,O=oo,O=EJBCA,DC=DC2,DC=dc1,C=SE", CertTools.stringToBCDNString(dn19));
       String dn20 = " C=SE,CN=\"foo, OU=bar\",  O=baz\\\\\\, quux  ";
       // BC always escapes with backslash, it doesn't use quotes.
       assertEquals("CN=foo\\, OU=bar,O=baz\\\\\\, quux,C=SE", CertTools.stringToBCDNString(dn20));

       String dn21 = "C=SE,O=Foo\\, Inc, OU=Foo\\, Dep, CN=Foo\\'";
       String bcdn21 = CertTools.stringToBCDNString(dn21);
       assertEquals("CN=Foo\',OU=Foo\\, Dep,O=Foo\\, Inc,C=SE", bcdn21);        
       assertEquals("CN=Foo',OU=Foo\\, Dep,O=Foo\\, Inc,C=SE", StringTools.strip(bcdn21));        
       log.debug("<test10TestMultipleReversed()");
   }
   
   /** Tests the insertCNPostfix function
   *
   * @throws Exception if error...
   */
  public void test11TestInsertCNPostfix() throws Exception {
      log.debug(">test11TestInsertCNPostfix()");
      
      // Test the regular case with one CN beging replaced with " (VPN)" postfix
      String dn1 = "CN=Tomas G,OU=users,OU=orgunit,DC=se,DC=bigcorp,DC=com";
      String cnpostfix1 = " (VPN)";      
      String newdn1 = CertTools.insertCNPostfix(dn1,cnpostfix1);
      assertEquals("CN=Tomas G (VPN),OU=users,OU=orgunit,DC=se,DC=bigcorp,DC=com", newdn1);
      
      // Test case when CN doesn't exist
      String dn2 = "OU=users,OU=orgunit,DC=se,DC=bigcorp,DC=com";
      String newdn2 = CertTools.insertCNPostfix(dn2,cnpostfix1); 
      assertEquals("OU=users,OU=orgunit,DC=se,DC=bigcorp,DC=com", newdn2);
      
      // Test case with two CNs in DN only first one should be replaced.
      String dn3 = "CN=Tomas G,CN=Bagare,OU=users,OU=orgunit,DC=se,DC=bigcorp,DC=com";  
      String newdn3 = CertTools.insertCNPostfix(dn3,cnpostfix1); 
      assertEquals("CN=Tomas G (VPN),CN=Bagare,OU=users,OU=orgunit,DC=se,DC=bigcorp,DC=com", newdn3);
 
      // Test case with two CNs in reversed DN 
      String dn4 = "dc=com,dc=bigcorp,dc=se,ou=orgunit,ou=users,cn=Tomas G,CN=Bagare";
      String newdn4 = CertTools.insertCNPostfix(dn4,cnpostfix1); 
      assertEquals("dc=com,dc=bigcorp,dc=se,ou=orgunit,ou=users,cn=Tomas G (VPN),CN=Bagare", newdn4);

      // Test case with two CNs in reversed DN 
      String dn5 = "UID=tomas,CN=tomas,OU=users,OU=orgunit,DC=se,DC=bigcorp,DC=com";
      String cnpostfix5 = " (VPN)";      
      String newdn5 = CertTools.insertCNPostfix(dn5,cnpostfix5);
      assertEquals("UID=tomas,CN=tomas (VPN),OU=users,OU=orgunit,DC=se,DC=bigcorp,DC=com", newdn5);
      
      log.debug("<test11TestInsertCNPostfix()");
  }
  
  /**
   */
  public void test12GetPartsFromDN() throws Exception {
      log.debug(">test01GetPartFromDN()");

      // We try to examine the general case and som special cases, which we want to be able to handle
      String dn0 = "C=SE, O=AnaTom, CN=foo";
      assertEquals(CertTools.getPartsFromDN(dn0, "CN").size(), 1);
      assertTrue(CertTools.getPartsFromDN(dn0, "CN").contains("foo"));
      assertEquals(CertTools.getPartsFromDN(dn0, "O").size(), 1);
      assertTrue(CertTools.getPartsFromDN(dn0, "O").contains("AnaTom"));
      assertEquals(CertTools.getPartsFromDN(dn0, "C").size(), 1);
      assertTrue(CertTools.getPartsFromDN(dn0, "C").contains("SE"));
      assertEquals(CertTools.getPartsFromDN(dn0, "cn").size(), 1);
      assertTrue(CertTools.getPartsFromDN(dn0, "cn").contains("foo"));
      assertEquals(CertTools.getPartsFromDN(dn0, "o").size(), 1);
      assertTrue(CertTools.getPartsFromDN(dn0, "o").contains("AnaTom"));
      assertEquals(CertTools.getPartsFromDN(dn0, "c").size(), 1);
      assertTrue(CertTools.getPartsFromDN(dn0, "c").contains("SE"));

      String dn1 = "uri=http://www.a.se, C=SE, O=AnaTom, CN=foo";
      assertEquals(CertTools.getPartsFromDN(dn1, "CN").size(), 1);
      assertTrue(CertTools.getPartsFromDN(dn1, "CN").contains("foo"));
      assertEquals(CertTools.getPartsFromDN(dn1, CertTools.URI).size(), 0);
      assertEquals(CertTools.getPartsFromDN(dn1, CertTools.URI1).size(), 1);
      assertTrue(CertTools.getPartsFromDN(dn1, CertTools.URI1).contains("http://www.a.se"));

      String dn2 = "uri=http://www.a.se, uri=http://www.b.se, C=SE, O=AnaTom, CN=foo";
      assertEquals(CertTools.getPartsFromDN(dn2, "CN").size(), 1);
      assertTrue(CertTools.getPartsFromDN(dn2, "CN").contains("foo"));
      assertEquals(CertTools.getPartsFromDN(dn2, CertTools.URI1).size(), 2);
      assertTrue(CertTools.getPartsFromDN(dn2, CertTools.URI1).contains("http://www.a.se"));
      assertTrue(CertTools.getPartsFromDN(dn2, CertTools.URI1).contains("http://www.b.se"));

      log.debug("<test12GetPartsFromDN()");
  }
  
  public void test13GetSubjectAltNameString() throws Exception {
      log.debug(">test13GetSubjectAltNameString()");
      
      String altNames = CertTools.getSubjectAlternativeName(CertTools.getCertfromByteArray(altNameCert));
      log.debug(altNames);
      String name = CertTools.getPartFromDN(altNames,CertTools.UPN);
      assertEquals("foo@a.se", name);
      assertEquals("foo@a.se", CertTools.getUPNAltName(CertTools.getCertfromByteArray(altNameCert)));
      name = CertTools.getPartFromDN(altNames,CertTools.URI);
      assertEquals("http://www.a.se/", name);
      name = CertTools.getPartFromDN(altNames,CertTools.EMAIL);
      assertEquals("tomas@a.se", name);
      name = CertTools.getEMailAddress(CertTools.getCertfromByteArray(altNameCert));
      assertEquals("tomas@a.se", name);
      name = CertTools.getEMailAddress(CertTools.getCertfromByteArray(testcert));
      assertNull(name);
      name = CertTools.getEMailAddress(null);
      assertNull(name);
      name = CertTools.getPartFromDN(altNames,CertTools.DNS);
      assertEquals("www.a.se", name);
      name = CertTools.getPartFromDN(altNames,CertTools.IPADDR);
      assertEquals("10.1.1.1", name);
      log.debug("<test13GetSubjectAltNameString()");
  }

  public void test14QCStatement()  throws Exception {
      X509Certificate cert = CertTools.getCertfromByteArray(qcRefCert);
      //System.out.println(cert);
      assertEquals("rfc822name=municipality@darmstadt.de", QCStatementExtension.getQcStatementAuthorities(cert));
      Collection ids = QCStatementExtension.getQcStatementIds(cert);
      assertTrue(ids.contains(RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v2.getId()));
      X509Certificate cert2 = CertTools.getCertfromByteArray(qcPrimeCert);
      assertEquals("rfc822name=qc@primekey.se", QCStatementExtension.getQcStatementAuthorities(cert2));
      ids = QCStatementExtension.getQcStatementIds(cert2);
      assertTrue(ids.contains(RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v1.getId()));
      assertTrue(ids.contains(ETSIQCObjectIdentifiers.id_etsi_qcs_QcCompliance.getId()));
      assertTrue(ids.contains(ETSIQCObjectIdentifiers.id_etsi_qcs_QcSSCD.getId()));
      assertTrue(ids.contains(ETSIQCObjectIdentifiers.id_etsi_qcs_LimiteValue.getId()));
      String limit = QCStatementExtension.getQcStatementValueLimit(cert2);
      assertEquals("50000 SEK", limit);
  }
  public void test15AiaOcspUri() throws Exception {
      X509Certificate cert = CertTools.getCertfromByteArray(aiaCert);
      //System.out.println(cert);
      assertEquals("http://localhost:8080/ejbca/publicweb/status/ocsp", CertTools.getAuthorityInformationAccessOcspUrl(cert));
  }
  public void test16GetSubjectAltNameStringWithDirectoryName() throws Exception {
        log.debug(">test16GetSubjectAltNameStringWithDirectoryName()");

        X509Certificate cer = CertTools.getCertfromByteArray(altNameCertWithDirectoryName);
        String altNames = CertTools.getSubjectAlternativeName(cer);
        log.debug(altNames);
        
        String name = CertTools.getPartFromDN(altNames, CertTools.UPN);
        assertEquals("testDirName@jamador.pki.gva.es", name);
        assertEquals("testDirName@jamador.pki.gva.es", CertTools.getUPNAltName(cer));
        
        name = CertTools.getPartFromDN(altNames, CertTools.DIRECTORYNAME);
        assertEquals("CN=testDirName|dir|name", name);
        assertEquals(name.substring("CN=".length()), new X509Name("CN=testDirName|dir|name").getValues().get(0));
        
        String altName = "rfc822name=foo@bar.se, uri=http://foo.bar.se, directoryName="+LDAPDN.escapeRDN("CN=testDirName, O=Foo, OU=Bar, C=SE")+", dnsName=foo.bar.se";
        GeneralNames san = CertTools.getGeneralNamesFromAltName(altName);  
        GeneralName[] gns = san.getNames();
        boolean found = false;
        for (int i = 0;i < gns.length; i++) {
            int tag = gns[i].getTagNo();
            if (tag == 4) {
                found = true;
                DEREncodable enc = gns[i].getName();
                X509Name dir = (X509Name)enc;
                String str = dir.toString();
                log.debug("DirectoryName: "+str);
                assertEquals("CN=testDirName,O=Foo,OU=Bar,C=SE", str);
            }
            
        }
        assertTrue(found);
        
        altName = "rfc822name=foo@bar.se, rfc822name=foo@bar.com, uri=http://foo.bar.se, directoryName="+LDAPDN.escapeRDN("CN=testDirName, O=Foo, OU=Bar, C=SE")+", dnsName=foo.bar.se, dnsName=foo.bar.com";
        san = CertTools.getGeneralNamesFromAltName(altName);  
        gns = san.getNames();
        int dnscount = 0;
        int rfc822count = 0;
        for (int i = 0;i < gns.length; i++) {
            int tag = gns[i].getTagNo();
            if (tag == 2) {
                dnscount++;
                DEREncodable enc = gns[i].getName();
                DERIA5String dir = (DERIA5String)enc;
                String str = dir.getString();
                log.info("DnsName: "+str);
            }
            if (tag == 1) {
                rfc822count++;
                DEREncodable enc = gns[i].getName();
                DERIA5String dir = (DERIA5String)enc;
                String str = dir.getString();
                log.info("Rfc822Name: "+str);
            }
            
        }
        assertEquals(2, dnscount);
        assertEquals(2, rfc822count);
        log.debug("<test16GetSubjectAltNameStringWithDirectoryName()");
      }  

  public void test17SubjectDirectoryAttributes() throws Exception {
      log.debug(">test17SubjectDirectoryAttributes()");
      X509Certificate cer = CertTools.getCertfromByteArray(subjDirAttrCert);
      String ret = SubjectDirAttrExtension.getSubjectDirectoryAttributes(cer);
      assertEquals("countryOfCitizenship=TR", ret);
      cer = CertTools.getCertfromByteArray(subjDirAttrCert2);
      ret = SubjectDirAttrExtension.getSubjectDirectoryAttributes(cer);
      assertEquals("countryOfResidence=SE, countryOfCitizenship=SE, gender=M, placeOfBirth=Stockholm, dateOfBirth=19710425", ret);
      log.debug("<test17SubjectDirectoryAttributes()");	  
  }
  
  public void test18DNSpaceTrimming() throws Exception {
      log.debug(">test18DNSpaceTrimming()");
      String dn1 = "CN=CommonName, O= Org,C=SE";
      String bcdn1 = CertTools.stringToBCDNString(dn1);
      log.debug("dn1: " + dn1);
      log.debug("bcdn1: " + bcdn1);
      assertEquals("CN=CommonName,O= Org,C=SE", bcdn1);

      dn1 = "CN=CommonName, O =Org,C=SE";
      bcdn1 = CertTools.stringToBCDNString(dn1);
      log.debug("dn1: " + dn1);
      log.debug("bcdn1: " + bcdn1);
      assertEquals("CN=CommonName,O=Org,C=SE", bcdn1);
      log.debug("<test18DNSpaceTrimming()");	  
  }

}
