/**
 * @copyright pertilience
 */
    
package WS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.scribe.model.Response;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

 
/**
 *
 * @author Maisonnass
 */


public class DigikeyOAuthApi extends DefaultApi20 {

    private static final String AUTHORIZATION_URL ="https://api.digikey.com/v1/oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s";
         //"https://sso.digikey.com/as/authorization.oauth2?response_type=code&client_id=%s&redirect_uri=%s";
        //"https://quote.fm/labs/authorize?client_id=%s&redirect_uri=%s&scope=%s&response_type=code";
        
             
    @Override
    public String getAccessTokenEndpoint() {
        //return "https://sso.digikey.com/as/token.oauth2";
        return "https://api.digikey.com/v1/oauth2/token";
    }  

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new AccessTokenExtractor() {
            
            @Override
            public Token extract(String response) {
                Preconditions.checkEmptyString(response, "Response body is incorrect. Can't extract a token from an empty string");
 
                Matcher matcherAT = Pattern.compile("\"access_token\"\\s*:\\s*\"([^&\"]+)\"").matcher(response);
                if (matcherAT.find())
                {
                    //
                    String access_token = OAuthEncoder.decode(matcherAT.group(1));
                    Matcher matcherRT = Pattern.compile("\"refresh_token\"\\s*:\\s*\"([^&\"]+)\"").matcher(response);
                    //
                    String refresh_token = "";
                    if (matcherRT.find()){
                        refresh_token = OAuthEncoder.decode(matcherRT.group(1));
                    }else{
                        throw new OAuthException("Response body is incorrect. Can't extract a token from this: '" + response + "'", null);
                    }
                    //
                    return new Token(access_token,refresh_token, response);
                } 
                else
                {
                  throw new OAuthException("Response body is incorrect. Can't extract a token from this: '" + response + "'", null);
                }
            }
        };
    }
    
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }
    
    @Override
    public String getAuthorizationUrl(OAuthConfig oac) {
        return String.format(AUTHORIZATION_URL, oac.getApiKey(), oac.getCallback());
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public OAuthService createService(OAuthConfig config) {
        return new DigikeyOAuth2Service(this, config);
    }
    
    
    public class DigikeyOAuth2Service extends OAuth20ServiceImpl {
 
        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE_REFRESH_TOKEN= "refresh_token";
        
        private static final String GRANT_TYPE = "grant_type";
        private DefaultApi20 api;
        private OAuthConfig config;
 
        public DigikeyOAuth2Service(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }
        
        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
            switch (api.getAccessTokenVerb()) {
            case POST:
                request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
                request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());               
                request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
                break;
            case GET:
            default:
                request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
                request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                if(config.hasScope()) request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());
            }
            Response response = request.send();
            
            return api.getAccessTokenExtractor().extract(response.getBody());
        }
        
        public Token getRenewToken(String accessToken, String refreshToken) {

            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
            //
            request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
            request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
            request.addBodyParameter("refresh_token", refreshToken);
            request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_REFRESH_TOKEN);
            //
            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
            //
        }
    }
  
}
