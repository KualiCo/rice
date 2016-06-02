<%--

    Copyright 2005-2016 The Kuali Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.opensource.org/licenses/ecl2.php

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<!-- test -->
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<kul:page showDocumentInfo="false"
          headerTitle="Balance Inquiry Report Menu" docTitle=""
          transactionalDocument="false"
          htmlFormAction="${KFSConstants.MAPPING_BALANCE_INQUIRY_REPORT_MENU}">
  <html:hidden property="backLocation" write="false" />
  <html:hidden property="docFormKey" write="false" />
  <html:hidden property="balanceInquiryReportMenuCallerDocFormKey"
               write="false" />
  <html:hidden property="chartOfAccountsCode" write="false" />
  <div class="topblurb">
    <div align="center"><b><font size="+1">Balance Inquiry Report Menu</font></b>
      <br />
      <br />
      <br />
      <table cellpadding="0" class="container2">
        <tr>
          <td><b>Available Balances</b>&nbsp;</td>
          <td><gl:balanceInquiryLookup
                  boClassName="org.kuali.kfs.gl.businessobject.AccountBalance"
                  actionPath="${KFSConstants.GL_MODIFIED_INQUIRY_ACTION}"
                  lookupParameters="${KualiForm.availableBalancesBalanceInquiryLookupParameters}"
                  hideReturnLink="true" /></td>
        </tr>
        <tr>
          <td colspan="2">&nbsp;</td>
        </tr>
        <tr>
          <td align="center" colspan="2"><html:image
                  src="${ConfigProperties.kr.externalizable.images.url}buttonsmall_cancel.gif" styleClass="globalbuttons"
                  property="methodToCall.cancel" alt="cancel" title="cancel" /></td>
        </tr>
      </table>
    </div>
  </div>
</kul:page>
