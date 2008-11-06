/*
 * Copyright 2008 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kim.bo.role.impl;

import java.util.LinkedHashMap;

import org.kuali.rice.kim.bo.types.impl.KimAttributeImpl;
import org.kuali.rice.kns.bo.Inactivateable;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

/**
 * This is a description of what this class does - kellerj don't forget to fill this in. 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class KimPermissionRequiredAttributeImpl extends PersistableBusinessObjectBase implements Inactivateable {

	protected String kimPermissionRequiredAttributeId;
	protected String permissionId;
	protected String kimAttributeId;
	protected boolean active;
	
	protected KimAttributeImpl kimAttribute;
	
	/**
	 * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected LinkedHashMap toStringMapper() {
		LinkedHashMap m = new LinkedHashMap();
		m.put( "kimPermissionRequiredAttributeId", kimPermissionRequiredAttributeId );
		m.put( "permissionId", permissionId );
		m.put( "kimAttributeId", kimAttributeId );
		return m;
	}

	public String getKimPermissionRequiredAttributeId() {
		return this.kimPermissionRequiredAttributeId;
	}

	public void setKimPermissionRequiredAttributeId(String kimPermissionRequiredAttributeId) {
		this.kimPermissionRequiredAttributeId = kimPermissionRequiredAttributeId;
	}

	public String getPermissionId() {
		return this.permissionId;
	}

	public void setPermissionId(String permissionId) {
		this.permissionId = permissionId;
	}

	public String getKimAttributeId() {
		return this.kimAttributeId;
	}

	public void setKimAttributeId(String kimAttributeId) {
		this.kimAttributeId = kimAttributeId;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public KimAttributeImpl getKimAttribute() {
		return this.kimAttribute;
	}

	public void setKimAttribute(KimAttributeImpl kimAttribute) {
		this.kimAttribute = kimAttribute;
	}

}
