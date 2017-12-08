package org.kuali.rice.edl.impl;

import org.junit.Test;
import org.kuali.rice.edl.impl.service.EdlServiceLocator;
import org.kuali.rice.kew.test.KEWTestCase;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.exception.AuthenticationException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.test.BaselineTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@BaselineTestCase.BaselineMode(BaselineTestCase.Mode.NONE)
public class EDLControllerChainTest extends KEWTestCase {

    protected void loadTestData() throws Exception {
        super.loadXmlFile("EDLControllerChainTest.xml");
    }

    @Test
    public void testRenderEdl_WithEdlFunctions() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "JUnit");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestParser requestParser = new RequestParser(request);
        requestParser.setParameterValue("command","initiate");
        requestParser.setParameterValue("userAction","initiate");

        UserSession userSession = new UserSession("admin");
        GlobalVariables.setUserSession(userSession);

        EDLController edlController = EdlServiceLocator.getEDocLiteService().getEDLControllerUsingEdlName("TestDocumentType");
        EDLControllerChain chain = new EDLControllerChain();
        chain.addEdlController(edlController);

        // render the EDL
        chain.renderEDL(requestParser, response);

        System.out.println(response.getContentAsString());
    }

}
