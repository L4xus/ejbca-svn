--
-- These definitions should work for EJBCA 3.4.x, Oracle 9.x or 10.x and the JDBC driver version 10.1.0.2.
--

CREATE TABLE "ACCESSRULESDATA" (
"PK" NUMBER(10) NOT NULL,
    "ACCESSRULE" VARCHAR2(255 byte), "RULE" NUMBER(10) NOT NULL, 
    "ISRECURSIVE" NUMBER(1) NOT NULL, 
    "ADMINGROUPDATA_ACCESSRULES" NUMBER(10), 
    CONSTRAINT "PK_ACCESSRULESDATA" PRIMARY KEY("PK")
	) ;
     
CREATE TABLE "ADMINENTITYDATA" (
	"PK" NUMBER(10) NOT NULL,
    "MATCHWITH" NUMBER(10) NOT NULL,
 "MATCHTYPE" NUMBER(10) NOT NULL,
 "MATCHVALUE" VARCHAR2(255 byte), 
    "ADMINGROUPDATA_ADMINENTITIES" NUMBER(10), 
    CONSTRAINT "PK_ADMINENTITYDATA" PRIMARY KEY("PK")
	);

CREATE TABLE "ADMINGROUPDATA" (
"PK" NUMBER(10) NOT NULL,
    "ADMINGROUPNAME" VARCHAR2(255 byte), 
"CAID" NUMBER(10) NOT NULL, 
    CONSTRAINT "PK_ADMINGROUPDATA" PRIMARY KEY("PK")
	);
     
CREATE TABLE "ADMINPREFERENCESDATA" (
"ID" VARCHAR2(255 byte) NOT NULL,
 "DATA" BLOB, 
    CONSTRAINT "PK_ADMINPREFERENCESDATA" PRIMARY KEY("ID")
	);
     
CREATE TABLE "APPROVALDATA" (
 "ID" NUMBER(10) NOT NULL, 
 "APPROVALID" NUMBER(10) NOT NULL, 
 "APPROVALTYPE" NUMBER(10) NOT NULL, 
 "ENDENTITYPROFILEID" NUMBER(10) NOT NULL, 
 "CAID" NUMBER(10) NOT NULL, 
 "REQADMINCERTISSUERDN" VARCHAR2(255 byte), 
 "REQADMINCERTSN" VARCHAR2(255 byte), 
 "STATUS" NUMBER(10) NOT NULL, 
 "APPROVALDATA" VARCHAR2(4000 byte), 
 "REQUESTDATA" LONG, 
 "REQUESTDATE" NUMBER(19)  NOT NULL, 
 "EXPIREDATE" NUMBER(19)  NOT NULL, 
 "REMAININGAPPROVALS" NUMBER(10) NOT NULL, 
    CONSTRAINT "PK_APPROVALDATA" PRIMARY KEY("ID")
); 

CREATE TABLE "AUTHORIZATIONTREEUPDATEDATA" ("PK" 
    NUMBER(10) NOT NULL, "AUTHORIZATIONTREEUPDATENUMBER" 
    NUMBER(10) NOT NULL, 
    CONSTRAINT "PK_AUTHORIZATIONTREEUPDATEDATA" PRIMARY KEY("PK")
	);

CREATE TABLE "CADATA" (
"CAID" NUMBER(10) NOT NULL, 
    "NAME" VARCHAR2(255 byte), 
"SUBJECTDN" VARCHAR2(255 byte), 
    "STATUS" NUMBER(10) NOT NULL, 
"EXPIRETIME" NUMBER(19) NOT NULL, 
"DATA" LONG, 
    CONSTRAINT "PK_CADATA" PRIMARY KEY("CAID") 
);

CREATE TABLE "CERTIFICATEDATA" (
"FINGERPRINT"  VARCHAR2(255 byte) NOT NULL, 
"ISSUERDN" VARCHAR2(255 byte), 
    "SUBJECTDN" VARCHAR2(255 byte), 
"CAFINGERPRINT" VARCHAR2(255 byte), 
"STATUS" NUMBER(10) NOT NULL, 
"TYPE" NUMBER(10) NOT NULL, 
"SERIALNUMBER" VARCHAR2(255 byte),
 "EXPIREDATE"  NUMBER(19) NOT NULL, 
"REVOCATIONDATE" NUMBER(19) NOT NULL, 
    "REVOCATIONREASON" NUMBER(10) NOT NULL, 
"BASE64CERT" LONG, 
    "USERNAME" VARCHAR2(255 byte), 
    CONSTRAINT "PK_CERTIFICATEDATA" PRIMARY KEY("FINGERPRINT") 
);

CREATE TABLE "CERTIFICATEPROFILEDATA" (
"ID" NUMBER(10) NOT NULL,
 "CERTIFICATEPROFILENAME" VARCHAR2(255 byte), 
"DATA"   BLOB, 
    CONSTRAINT "PK_CERTIFICATEPROFILEDATA" PRIMARY KEY("ID") 
);

CREATE TABLE "CERTREQHISTORYDATA" (
"FINGERPRINT"  VARCHAR2(255 byte) NOT NULL,
 "ISSUERDN" VARCHAR2(255 byte), 
    "SERIALNUMBER" VARCHAR2(255 byte),
 "TIMESTAMP" NUMBER(19) NOT NULL,
 "USERDATAVO" LONG, 
"USERNAME" VARCHAR2(255 byte), 
    CONSTRAINT "PK_CERTREQHISTORYDATA" PRIMARY KEY("FINGERPRINT")
);
     
CREATE TABLE "CRLDATA" (
"FINGERPRINT" VARCHAR2(255 byte) NOT NULL, 
"CRLNUMBER" NUMBER(10) NOT NULL,
 "ISSUERDN"  VARCHAR2(255 byte), 
"CAFINGERPRINT" VARCHAR2(255 byte), 
    "THISUPDATE" NUMBER(19) NOT NULL, 
"NEXTUPDATE" NUMBER(19) NOT NULL, 
"BASE64CRL" LONG, 
    CONSTRAINT "PK_CRLDATA" PRIMARY KEY("FINGERPRINT") 
);
     
CREATE TABLE "ENDENTITYPROFILEDATA" (
"ID" NUMBER(10) NOT NULL, 
"PROFILENAME" VARCHAR2(255 byte), 
"DATA" BLOB, 
    CONSTRAINT "PK_ENDENTITYPROFILEDATA" PRIMARY KEY("ID")
); 
     
CREATE TABLE "GLOBALCONFIGURATIONDATA" (
"CONFIGURATIONID" VARCHAR2(255 byte) NOT NULL, 
"DATA" BLOB, 
    CONSTRAINT "PK_GLOBALCONFIGURATIONDATA" PRIMARY  KEY("CONFIGURATIONID") 
);
     
CREATE TABLE "HARDTOKENCERTIFICATEMAP"  (
"CERTIFICATEFINGERPRINT" VARCHAR2(255 byte) NOT NULL, 
    "TOKENSN" VARCHAR2(255 byte), 
    CONSTRAINT "PK_HARDTOKENCERTIFICATEMAP" PRIMARY KEY("CERTIFICATEFINGERPRINT") 
);
     
CREATE TABLE "HARDTOKENDATA" (
"TOKENSN" VARCHAR2(255 byte) NOT NULL,
 "USERNAME" VARCHAR2(255 byte),
 "CTIME"  NUMBER(19) NOT NULL, 
"MTIME" NUMBER(19) NOT NULL,
 "TOKENTYPE"  NUMBER(10) NOT NULL,
 "SIGNIFICANTISSUERDN" VARCHAR2(255 byte),
    "DATA" BLOB, 
    CONSTRAINT "PK_HARDTOKENDATA" PRIMARY KEY("TOKENSN") 
);
     
CREATE TABLE "HARDTOKENISSUERDATA" (
"ID" NUMBER(10) NOT NULL,
 "ALIAS" VARCHAR2(255 byte),
 "ADMINGROUPID" NUMBER(10)  NOT NULL, 
"DATA" BLOB, 
    CONSTRAINT "PK_HARDTOKENISSUERDATA" PRIMARY KEY("ID") 
);
     
CREATE TABLE "HARDTOKENPROFILEDATA" (
"ID" NUMBER(10) NOT NULL, 
"NAME" VARCHAR2(255 byte), 
"UPDATECOUNTER" NUMBER(10)  NOT NULL, 
"DATA" LONG, 
    CONSTRAINT "PK_HARDTOKENPROFILEDATA" PRIMARY KEY("ID")
); 

CREATE TABLE "HARDTOKENPROPERTYDATA" (
"ID" VARCHAR2(255 byte) NOT NULL, 
"PROPERTY" VARCHAR2(255 byte) NOT NULL, 
    "VALUE" VARCHAR2(255 byte), 
    CONSTRAINT "PK_HARDTOKENPROPERTYDATA" PRIMARY KEY("ID", "PROPERTY") 
);
     
CREATE TABLE "KEYRECOVERYDATA" (
"CERTSN" VARCHAR2(255 byte) NOT NULL,
 "ISSUERDN" VARCHAR2(255 byte) NOT NULL, 
    "USERNAME" VARCHAR2(255 byte),
 "MARKEDASRECOVERABLE"  NUMBER(1) NOT NULL,
 "KEYDATA" LONG, 
    CONSTRAINT "PK_KEYRECOVERYDATA" PRIMARY KEY("CERTSN", "ISSUERDN") 
);
     
CREATE TABLE "LOGCONFIGURATIONDATA" (
"ID" NUMBER(10) NOT NULL, 
"LOGCONFIGURATION" BLOB, 
"LOGENTRYROWNUMBER" NUMBER(10) NOT NULL, 
    CONSTRAINT "PK_LOGCONFIGURATIONDATA" PRIMARY KEY("ID") 
);

CREATE TABLE "LOGENTRYDATA" (
"ID" NUMBER(10) NOT NULL, 
    "ADMINTYPE" NUMBER(10) NOT NULL, 
"ADMINDATA" VARCHAR2(255 byte), 
"CAID" NUMBER(10) NOT NULL, 
"MODULE" NUMBER(10) NOT NULL, 
"TIME" NUMBER(19) NOT NULL,
 "USERNAME" VARCHAR2(255 byte), 
"CERTIFICATESNR" VARCHAR2(255 byte), 
"EVENT" NUMBER(10) NOT NULL, 
"COMMENT_" VARCHAR2(512 byte), 
    CONSTRAINT "PK_LOGENTRYDATA" PRIMARY KEY("ID") 
);

CREATE TABLE "PUBLISHERDATA" (
 "ID" NUMBER(10) NOT NULL, 
 "NAME" VARCHAR2(255 byte),
 "UPDATECOUNTER" NUMBER(10) NOT NULL, 
 "DATA" LONG, 
    CONSTRAINT "PK_PUBLISHERDATA" PRIMARY KEY("ID") 
);

CREATE TABLE "SERVICEDATA" (
 "ID" NUMBER(10) NOT NULL, 
 "NAME" VARCHAR2(255 byte),
 "DATA" LONG, 
    CONSTRAINT "PK_SERVICEDATA" PRIMARY KEY("ID") 
);

CREATE TABLE "TABLEPROTECTDATA" (
 "ID" VARCHAR2(255 byte) NOT NULL, 
 "VERSION" NUMBER(10) NOT NULL, 
 "HASHVERSION" NUMBER(10) NOT NULL, 
 "PROTECTIONALG" VARCHAR2(255 byte), 
 "HASH" VARCHAR2(255 byte), 
 "SIGNATURE" VARCHAR2(255 byte), 
 "TIME" NUMBER(19)  NOT NULL, 
 "DBKEY" VARCHAR2(255 byte), 
 "DBTYPE" VARCHAR2(255 byte), 
 "KEYREF" VARCHAR2(255 byte), 
 "KEYTYPE" VARCHAR2(255 byte), 
    CONSTRAINT "PK_TABLEPROTECTDATA" PRIMARY KEY("ID") 
);

CREATE TABLE "USERDATA" (
"USERNAME" VARCHAR2(255 byte) NOT NULL, 
"SUBJECTDN" VARCHAR2(255 byte),
 "CAID" NUMBER(10) NOT NULL, 
"SUBJECTALTNAME" VARCHAR2(255 byte),
 "SUBJECTEMAIL"  VARCHAR2(255 byte),
 "STATUS" NUMBER(10) NOT NULL, 
"TYPE"   NUMBER(10) NOT NULL,
 "CLEARPASSWORD" VARCHAR2(255 byte), 
    "PASSWORDHASH" VARCHAR2(255 byte), 
"TIMECREATED" NUMBER(19)  NOT NULL, 
"TIMEMODIFIED" NUMBER(19) NOT NULL, 
    "ENDENTITYPROFILEID" NUMBER(10) NOT NULL, 
    "CERTIFICATEPROFILEID" NUMBER(10) NOT NULL,
 "TOKENTYPE"  NUMBER(10) NOT NULL, 
"HARDTOKENISSUERID" NUMBER(10) NOT NULL,
    "EXTENDEDINFORMATIONDATA" LONG, 
"KEYSTOREPASSWORD"  VARCHAR2(255 byte), 
    CONSTRAINT "PK_USERDATA" PRIMARY KEY("USERNAME") 
);

CREATE TABLE "USERDATASOURCEDATA" (
 "ID" NUMBER(10) NOT NULL, 
 "NAME" VARCHAR2(255 byte), 
 "UPDATECOUNTER" NUMBER(10) NOT NULL, 
 "DATA" LONG, 
    CONSTRAINT "PK_USERDATASOURCEDATA" PRIMARY KEY("ID")
); 
