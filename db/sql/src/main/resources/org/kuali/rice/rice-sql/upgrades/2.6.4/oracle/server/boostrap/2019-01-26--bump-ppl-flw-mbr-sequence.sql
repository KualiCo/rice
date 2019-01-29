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

DECLARE ppl_flw_mbr_inc NUMBER;
BEGIN
    SELECT krew_ppl_flw_s.nextval - krew_ppl_flw_mbr_s.nextval INTO ppl_flw_mbr_inc FROM dual;
    IF ppl_flw_mbr_inc > 0 THEN
	    EXECUTE IMMEDIATE 'alter sequence krew_ppl_flw_mbr_s increment by ' || ppl_flw_mbr_inc;
	    EXECUTE IMMEDIATE 'SELECT krew_ppl_flw_mbr_s.nextval FROM dual' INTO ppl_flw_mbr_inc;
	    EXECUTE IMMEDIATE 'ALTER SEQUENCE krew_ppl_flw_mbr_s INCREMENT BY 1';
	END IF;
END;
/
