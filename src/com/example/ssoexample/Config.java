package com.example.ssoexample;

import android.os.Bundle;
import com.l7tech.msso.MobileSsoConfig;

import java.util.ArrayList;
import java.util.Arrays;

public class Config {

    // URI of our web service that downloads a product list in JSON format
    static final String PRODUCT_LIST_DOWNLOAD_URI;
    static final String API_ENDPOINT=":8443/login";

    // Token server configuration
    static final Bundle ssoConf;

    static {

        //MAG Sdk Configuration
        Bundle myConfig = new Bundle();
        //myConfig.putString(MobileSsoConfig.PROP_TOKEN_HOSTNAME, "explore.apim.ca");
        myConfig.putString(MobileSsoConfig.PROP_TOKEN_HOSTNAME, "explore.apim.ca");
        myConfig.putString(MobileSsoConfig.PROP_TOKEN_URI_PREFIX, "mag");
        myConfig.putString(MobileSsoConfig.PROP_CLIENT_ID, "809af354-098c-457e-95fb-4d368fef4c18");
        myConfig.putString(MobileSsoConfig.PROP_CLIENT_SECRET, "bd338e2e-5a20-43f7-b4fb-4f62a6818585");
        myConfig.putString(MobileSsoConfig.PROP_ORGANIZATION, "CA Technologies");
        String cert =  "-----BEGIN CERTIFICATE-----\n" +
        		"MIIC+TCCAeGgAwIBAgIJAOzcJidEKVPEMA0GCSqGSIb3DQEBDAUAMBoxGDAWBgNVBAMTD2V4cGxv\n" +
        		"cmUuYXBpbS5jYTAeFw0xNDEyMDkwMTQ3MDJaFw0yNDEyMDYwMTQ3MDJaMBoxGDAWBgNVBAMTD2V4\n" +
        		"cGxvcmUuYXBpbS5jYTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIJDK/p445vJdr8+\n" +
        		"fsWUN/sC91oOTlOQVwFCFespIQoYe51EurkFXky+HPSeXPawZXi6Rpfm0eE7r5P0N7qclOU7chtR\n" +
        		"sqKnpUaf48hUF+Fr0ReIBYi++NyKFZQwASoIwtyVWW61r5dio+uuY+CN7jjcHYSHya7etC7qsgNm\n" +
        		"lnr2lnjViXoJt6wkhnIsBC9xgFZvC7E81p9ZwN5ldhx443lIeMZ/rHxuYjLcEKkB1BzXNgA4Wkkf\n" +
        		"Jgghf2+7niEBf7WM/Nq3JiL1lTPK1onAFs216ur4F6g/ZxFsuOUad0lMcTLRRFazDSUwxOjvi6gF\n" +
        		"Ftjkv42Mvm27kRtQVGqlONsCAwEAAaNCMEAwHQYDVR0OBBYEFNXhWLRSciS7JZFygGXg2Uq6KBMW\n" +
        		"MB8GA1UdIwQYMBaAFNXhWLRSciS7JZFygGXg2Uq6KBMWMA0GCSqGSIb3DQEBDAUAA4IBAQA5ZRUK\n" +
        		"6Gv+36TyKc5d517loKpF/g4odE+3pAU3siZKiZL5L+KUg9CG/MN7C2xX6VpeEXpXDQdtxZWkaNcp\n" +
        		"pfXI0YvilHu4vb6I4/7VGlkB21bMZKOLche94qL1Cl1z8YpW004tkcie//tzakC+H+jRUoTSKlZL\n" +
        		"TQ7bQkFZ+kgbn/VR3JfUwn1/vZl6uPq7ZEL4VOGY53tqMclzyzQxKgDGXN8bZ7bmU7iVidBVnaV3\n" +
        		"QMOv0nmk6p8ABR1dGyAmnnaFzdFn5hibImFCF+YTY5+waK5Y8ZJq4K+dlf8R7qeQ55zjLyNtfxRA\n" +
        		"0jfEeli0AqZhRC3NthgM5jBeqfXpabH3\n" +
        		"-----END CERTIFICATE-----";
        myConfig.putStringArrayList(MobileSsoConfig.PROP_TRUSTED_CERTS_PEM, new ArrayList<String>(Arrays.asList(
                cert)));
        myConfig.putBoolean(MobileSsoConfig.PROP_LOCATION_ENABLED, true);
        myConfig.putBoolean(MobileSsoConfig.PROP_SSO_ENABLED, true);
        myConfig.putBoolean(MobileSsoConfig.PROP_MSISDN_ENABLED, true);
//        myConfig.putString(MobileSsoConfig.PROP_OAUTH_SCOPE, "openid msso");
        //myConfig.putString(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI, "<Redirect URI for Social Logon>");

        //Set the config
        ssoConf = myConfig;

        //Sample endpoint
        PRODUCT_LIST_DOWNLOAD_URI = "https://" + ssoConf.getString(MobileSsoConfig.PROP_TOKEN_HOSTNAME) + API_ENDPOINT;

    }
}
