package org.exoplatform.addons.searchengine;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import io.swagger.annotations.*;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import java.net.URI;
import java.net.URL;


@Path("/searchengine")
@Api(value = "/searchengine")
public class RequestHandler implements ResourceContainer {
    private static final Log LOG =
            ExoLogger.getLogger(RequestHandler.class);
    private final String JIRA_PATTERN = "(?i)^(AM|BITNAMI|CMTC|CM|ACCOUNTS|UA|BD|CWI|DEVSTD|DOC|CECP|WWW|HR|INTRA|ITOP|MKT|PRESALES|REL|QACAP|QAF|TESTFR|TESTVN|TESTTN|TESTUKR|ENT|SWF|CCP|SST|ADM|ADMTN|ADMUA|ADMVN|UI|TQA|TRCRT|CRASH|TNCP|CP|ACC|ANS|CHAT|ES|TA|VIDEO|ALL|CAL|CLDINT|CLDIDE|CMIS|CMISE|CS|COMMONS|COR|DOCKER|ECMS|EVERREST|FORUM|EXOGTN|GWTX|IDE|INTEG|JCR|KER|KS|MSE|PLF|PLFBONITA|PLFENG|CROSS|RELMAN|RELEASE|SECURITY|SOC|SCL|ORG|WIKI|WS|ECM|WCM|WF|JCRS|LR|MOB|PORTAL|PC|WEBOS|FAL|FQA|JUZU|PAA|DEP|PAR|MI|MY|PLFCLD|PFR|PRM|SECMSS|SPUB|SWFSANDBOX|TEST|UXP|WKBK)\\-[0-9]+$";
    private final String ISSUE_PATTERN = "(?i)^[\\w\\d]+-[0-9]+$";
    private final String GIT_PATTERN = "(?i)^exo(plf|add|dev|dock|sam|swf)( [\\w-]+([0-9]|[\\w-])*)*$";
    private final String DOC_PATTERN = "(?i)^exodoc( [\\w-]+)*$";
    private final String CI_PATTERN = "(?i)^exoci( [\\w-]+ \\d+)*$";
    private final String JIRA_SEARCH_PATTERN = "(?i)^exojira( [\\w-]+)*$";
    private final String SPACE_PATTERN = "(?i)^g( \\w+)+";
    private final String USER_PATTERN = "(?i)^u( \\w+)+";
    private final String JIRA_URL = "https://jira.exoplatform.org";
    private final String SUPPORT_URL = CommonsUtils.getCurrentDomain() + "/portal/support/";
    private final String SPACES_URL = CommonsUtils.getCurrentDomain() + "/portal/g/:spaces:";
    private final String PROFILE_URL = CommonsUtils.getCurrentDomain() + "/portal/intranet/profile/";

    @GET
    @Path("search")
    @ApiOperation(value = "Redirect to URL", httpMethod = "GET", response = Response.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Request fulfilled"),
            @ApiResponse(code = 400, message = "Invalid query input"),
            @ApiResponse(code = 403, message = "Unauthorized operation"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response search(@ApiParam(value = "q", required = true) @QueryParam("q") String q) {
        if (q == null || q.trim().length() == 0) {
            LOG.warn("Query Parameter is empty");
            return Response.status(401).build();
        }
        try {
            return Response.temporaryRedirect(URI.create(generateURI(q))).build();
        } catch (Exception e) {
            LOG.error("Error while handling redirection", e);
        }
        return Response.status(401).build();
    }

    private String generateURI(String query) {
        if (query.matches(ISSUE_PATTERN)) {
            if (query.matches(JIRA_PATTERN)) {
                return JIRA_URL + "/browse/"+query;
            } else {
                return SUPPORT_URL + query;
            }
        } else if (query.matches(GIT_PATTERN)) {
            String repo = "";
            String prnumber = "";
            String searchword = "";
            if (query.indexOf(" ") != -1) {
                String arr[] = query.split(" ");
                repo = arr[1].trim();
                query = arr[0].trim();
                prnumber = arr.length > 2 && arr[2].matches("\\d+") ? arr[2].trim() : "";
                searchword = arr.length > 2 && prnumber.length() == 0 ? arr[2].trim() : "";
            }
            String organization = query;
            organization = (query.equals("exoplf")) ? "exoplatform" : organization;
            organization = (query.equals("exoadd")) ? "exo-addons" : organization;
            organization = (query.equals("exodock")) ? "exo-docker" : organization;
            organization = (query.equals("exosam")) ? "exo-samples" : organization;
            organization = (query.equals("exoswf")) ? "exo-swf" : organization;
            prnumber = prnumber.length() > 0 ? "/pull/" + prnumber : "";
            searchword = searchword.length() > 0 ? "/search?q=" + searchword : "";
            return "https://github.com/" + organization + "/" + repo + prnumber + searchword;
        } else if (query.matches(DOC_PATTERN)) {
            String field = "";
            if (query.indexOf(" ") != -1) {
                field = "/en/latest/search.html?q=" + query.substring(query.indexOf(" ") + 1).trim();
            }
            return "https://docs.exoplatform.com" + field.replaceAll("\\s", "+");
        } else if (query.matches(CI_PATTERN)) {
            String field = "";
            String[] items = query.split(" ");
            if (items.length == 3) {
                field = "/view/PR/job/PR/job/" + items[1] + "-pr-" + items[2] + "-ci/";
            }
            return "https://ci.exoplatform.org" + field;
        } else if (query.matches(JIRA_SEARCH_PATTERN)) {
            String field = "";
            if (query.indexOf(" ") != -1) {
                field = "/secure/QuickSearch.jspa?searchString=" + query.substring(query.indexOf(" ") + 1).trim();
            }
            return JIRA_URL + field;
        } else if (query.matches(SPACE_PATTERN)) {
            return SPACES_URL + query.substring(2).toLowerCase().replaceAll("\\s", "_");
        } else if (query.matches(USER_PATTERN)) {
            return PROFILE_URL + query.substring(2).toLowerCase().replaceAll("\\s", ".");
        } else if (isValidUrl(query)) {
            return query;
        } else if (query.toLowerCase().equals("exotribe")) {
            return "https://community.exoplatform.com";
        } else if (query.toLowerCase().equals("exotribepre")) {
            return "https://community-preprod.exoplatform.com";
        } else if (query.toLowerCase().equals("exotribedev")) {
            return "https://community-dev.exoplatform.com";
        } else if (query.toLowerCase().equals("exotribeqa")) {
            return "https://community-qa.exoplatform.com";
        } else if (query.toLowerCase().equals("exoacc")) {
            return "https://acceptance.exoplatform.org";
        } else if (query.toLowerCase().equals("exomy")) {
            return "https://my.exoplatform.org";
        } else if (query.toLowerCase().equals("exorepo")) {
            return "https://repository.exoplatform.org";
        } else {
            return "https://google.com/search?q=" + query;
        }
    }


    private boolean isValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

}
