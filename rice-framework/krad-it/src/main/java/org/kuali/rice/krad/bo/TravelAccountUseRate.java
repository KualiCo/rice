/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.kuali.rice.core.api.util.type.KualiPercent;

/**
 * Copied from sample app for testing
 */
@Entity
@Table(name = "TRV_ACCT_USE_RT_T")
public class TravelAccountUseRate extends InactivatableFromToImpl {
	private static final long serialVersionUID = 7433417595650091555L;

	@Id
	private String id;

	@Column(name = "ACCT_NUM")
	private String number;

	private KualiPercent rate;

	public TravelAccountUseRate() {
		super();
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNumber() {
		return this.number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public KualiPercent getRate() {
		return this.rate;
	}

	public void setRate(KualiPercent rate) {
		this.rate = rate;
	}
}
