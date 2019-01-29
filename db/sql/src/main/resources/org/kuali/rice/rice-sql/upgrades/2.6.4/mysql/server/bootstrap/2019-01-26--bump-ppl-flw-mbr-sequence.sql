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

SET @ppl_flw_max = (SELECT MAX(id) + 1 FROM krew_ppl_flw_s);
SET @ppl_flw_mbr_max = (SELECT MAX(id) + 1 FROM krew_ppl_flw_mbr_s);
SET @ppl_flw_auto_inc = (select if (@ppl_flw_max > @ppl_flw_mbr_max, @ppl_flw_max, @ppl_flw_mbr_max));
SET @alter_auto_increment = CONCAT('ALTER TABLE krew_ppl_flw_mbr_s AUTO_INCREMENT=', @ppl_flw_auto_inc);
PREPARE stmt FROM @alter_auto_increment;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
