--
-- Copyright 2005-2019 The Kuali Foundation
--
-- Licensed under the Educational Community License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.opensource.org/licenses/ecl2.php
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE KREW_STUCK_DOC_INCIDENT_T
(
      STUCK_DOC_INCIDENT_ID VARCHAR(40) NOT NULL,
      DOC_HDR_ID VARCHAR(40) NOT NULL,
      START_DT DATETIME NOT NULL,
      END_DT DATETIME,
      STATUS VARCHAR(20) NOT NULL,
      CONSTRAINT KREW_STUCK_DOC_INCIDENT_TP1 PRIMARY KEY(STUCK_DOC_INCIDENT_ID),
      CONSTRAINT KREW_STUCK_DOC_INCIDENT_TR1 FOREIGN KEY (DOC_HDR_ID) REFERENCES KREW_DOC_HDR_T(DOC_HDR_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin
/

CREATE TABLE KREW_STUCK_DOC_INCIDENT_S
(
	id bigint(19) not null auto_increment, primary key (id)
) ENGINE MyISAM
/
ALTER TABLE KREW_STUCK_DOC_INCIDENT_S auto_increment = 1000
/

CREATE TABLE KREW_STUCK_DOC_FIX_ATTMPT_T
(
      STUCK_DOC_FIX_ATTMPT_ID VARCHAR(40) NOT NULL,
      STUCK_DOC_INCIDENT_ID VARCHAR(40) NOT NULL,
      ATTMPT_TS DATETIME NOT NULL,
      CONSTRAINT KREW_STUCK_DOC_FIX_ATTMPT_TP1 PRIMARY KEY(STUCK_DOC_FIX_ATTMPT_ID),
      CONSTRAINT KREW_STUCK_DOC_FIX_ATTMPTT_TR1 FOREIGN KEY (STUCK_DOC_INCIDENT_ID) REFERENCES KREW_STUCK_DOC_INCIDENT_T(STUCK_DOC_INCIDENT_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin
/

CREATE TABLE KREW_STUCK_DOC_FIX_ATTMPT_S
(
	id bigint(19) not null auto_increment, primary key (id)
) ENGINE MyISAM
/
ALTER TABLE KREW_STUCK_DOC_FIX_ATTMPT_S auto_increment = 1000
/
