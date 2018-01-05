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
package org.kuali.rice.kim.test.service;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.kuali.rice.core.api.delegation.DelegationType;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.kim.api.common.delegate.DelegateMember;
import org.kuali.rice.kim.api.common.delegate.DelegateType;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleMember;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.rice.kim.api.type.KimTypeInfoService;
import org.kuali.rice.kim.test.KIMTestCase;
import org.kuali.rice.test.BaselineTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Test the RoleService
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
@BaselineTestCase.BaselineMode(BaselineTestCase.Mode.CLEAR_DB)
public class RoleServiceTest extends KIMTestCase {

	private RoleService roleService;
	private KimTypeInfoService kimTypeInfoService;

	public void setUp() throws Exception {
		super.setUp();
		this.roleService = KimApiServiceLocator.getRoleService();
		this.kimTypeInfoService = KimApiServiceLocator.getKimTypeInfoService();
	}
	
	@Test
	public void testPrincipaHasRoleOfDirectAssignment() {
		List <String>roleIds = new ArrayList<String>();
		roleIds.add("r1");
		assertTrue( "p1 has direct role r1", roleService.principalHasRole("p1", roleIds, Collections.<String, String>emptyMap() ));
		//assertFalse( "p4 has no direct/higher level role r1", roleService.principalHasRole("p4", roleIds, null ));	
		Map<String, String> qualification = new HashMap<String, String>();
		qualification.put("Attribute 2", "CHEM");
		assertTrue( "p1 has direct role r1 with rp2 attr data", roleService.principalHasRole("p1", roleIds,
                qualification));
		qualification.clear();
		//requested qualification rolls up to a higher element in some hierarchy 
		// method not implemented yet, not quite clear how this works
		qualification.put("Attribute 3", "PHYS");
		assertTrue( "p1 has direct role r1 with rp2 attr data", roleService.principalHasRole("p1", roleIds, Maps.newHashMap(
                qualification)));
	}

	@Test
	public void testPrincipalHasRoleOfHigherLevel() {
		// "p3" is in "r2" and "r2 contains "r1"
		List <String>roleIds = new ArrayList<String>();
		roleIds.add("r2");
		assertTrue( "p1 has assigned in higher level role r1", roleService.principalHasRole("p1", roleIds, Collections.<String, String>emptyMap() ));
	}
	
	@Test
	public void testPrincipalHasRoleContainsGroupAssigned() {
		// "p2" is in "g1" and "g1" assigned to "r2"
		List <String>roleIds = new ArrayList<String>();
		roleIds.add("r2");
		assertTrue( "p2 is assigned to g1 and g1 assigned to r2", roleService.principalHasRole("p2", roleIds, Collections.<String, String>emptyMap() ));
	}

	@Test
	public void testGetPrincipalsFromCircularRoles() {
		// "p2" is in "g1" and "g1" assigned to "r2"
		List <String>roleIds = new ArrayList<String>();
		Collection <String>rolePrincipalIds;
		roleIds.add("r101");
		rolePrincipalIds = roleService.getRoleMemberPrincipalIds("ADDL_ROLES_TESTS", "Role A",  Collections
                .<String, String>emptyMap());
		assertNotNull(rolePrincipalIds);
		assertEquals("RoleTwo should have 6 principal ids", 5, rolePrincipalIds.size());
	}

	/**
	 * This test will verify proper behavior when switching from a primary to a secondary delegation via the API.
	 */
	@Test
	public void testSwitchFromPrimaryToSecondaryDelegation() {

		// get the kim type for our role, one that supports two attributes
		KimType typeWithAttributes = kimTypeInfoService.findKimTypeByNameAndNamespace("TEST", "type-with-attributes");
		assertNotNull(typeWithAttributes);

		// first let's create a new Role with a delegation
		String roleId = UUID.randomUUID().toString();
		// note that we are appending role id to the name here to avoid conflicts when the "RemoteRoleServiceTest" runs, since the test harness is not clearing out KIM data for us and this will allow
		// us to be lazy and not have to clean up after ourself after this test runs
		Role.Builder roleBuilder = Role.Builder.create(roleId, "testSwitchFromPrimaryToSecondaryDelegation" + roleId, "TEST", "test role", typeWithAttributes.getId());
		Role role = roleService.createRole(roleBuilder.build());

		// now assign a member
		String roleMemberId = UUID.randomUUID().toString();
		RoleMember.Builder roleMemberBuilder = RoleMember.Builder.create(roleId, roleMemberId, "p10", MemberType.PRINCIPAL, null, null, null, null, null);
		RoleMember roleMember = roleService.createRoleMember(roleMemberBuilder.build());

		// now create a delegation
		DelegateType.Builder delegateTypeBuilder = DelegateType.Builder.create(roleId, DelegationType.PRIMARY, new ArrayList<>());
		delegateTypeBuilder.setKimTypeId(typeWithAttributes.getId());
		DelegateType primaryDelegateType = roleService.createDelegateType(delegateTypeBuilder.build());
		
		// and now let's add a delegation member
		DelegateMember.Builder delegateMemberBuilder = DelegateMember.Builder.create();
		delegateMemberBuilder.setDelegationId(primaryDelegateType.getDelegationId());
		delegateMemberBuilder.setRoleMemberId(roleMember.getMemberId());
		delegateMemberBuilder.setType(MemberType.PRINCIPAL);
		delegateMemberBuilder.setMemberId("p9");
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("attr1", "value1");
		attributes.put("attr2", "value2");
		delegateMemberBuilder.setAttributes(attributes);
		DelegateMember delegateMember = roleService.createDelegateMember(delegateMemberBuilder.build());

		assertNotNull(delegateMember.getDelegationMemberId());
		// just a sanity check, we should have one primary delegation member
		assertEquals(1, roleService.getDelegationMembersByDelegationId(primaryDelegateType.getDelegationId()).size());

		// now what we are going to do is try to run some code that will switch the delegation from primary to secondary

		// first let's fetch our member, and make sure it has the correct attributes
		DelegateMember originalMember = roleService.getDelegationMemberById(delegateMember.getDelegationMemberId());
		assertNotNull(originalMember);
		Map<String, String> originalMemberAttributes = originalMember.getAttributes();
		assertEquals(2, originalMemberAttributes.keySet().size());
		assertEquals("value1", originalMemberAttributes.get("attr1"));
		assertEquals("value2", originalMemberAttributes.get("attr2"));
		DelegateMember.Builder updatedMember = DelegateMember.Builder.create(originalMember);

		// We are wanting to change the delegation type to secondary, so first we remove old
		roleService.removeDelegateMembers(Collections.singletonList(originalMember));
		// add new
		DelegateType.Builder delegateTypeBuilder2 = DelegateType.Builder.create(roleId, DelegationType.SECONDARY, new ArrayList<>());
		delegateTypeBuilder2.setKimTypeId("1"); // set it to the default kim type which has an id of 1
		DelegateType secondaryDelegateType = roleService.createDelegateType(delegateTypeBuilder2.build());

		DelegateMember.Builder newMember = DelegateMember.Builder.create();
		newMember.setDelegationId(secondaryDelegateType.getDelegationId());
		newMember.setMemberId(originalMember.getMemberId());
		newMember.setType(originalMember.getType());
		newMember.setRoleMemberId(originalMember.getRoleMemberId());
		newMember.setAttributes(originalMember.getAttributes());
		newMember.setActiveFromDate(originalMember.getActiveFromDate());
		newMember.setActiveToDate(originalMember.getActiveToDate());
		DelegateMember addedMember = roleService.createDelegateMember(newMember.build());

		// now after all of this, let's check our delegation situation, we should have two DelegateTypes now, one
		// primary and one seconary the primary delegate type should be empty (have no members) while the secondary one
		// should have our new delegation member nestled snuggly inside

		DelegateType finalPrimaryDelegateType = roleService.getDelegateTypeByRoleIdAndDelegateTypeCode(roleId, DelegationType.PRIMARY);
		DelegateType finalSecondaryDelegateType = roleService.getDelegateTypeByRoleIdAndDelegateTypeCode(roleId, DelegationType.SECONDARY);

		assertEquals(1, finalPrimaryDelegateType.getMembers().size());
		assertEquals(1, finalSecondaryDelegateType.getMembers().size());

		// the primary delegate delegation members should be inactive since we removed it
		DelegateMember finalPrimaryDelegationMember = finalPrimaryDelegateType.getMembers().get(0);
		assertFalse(finalPrimaryDelegationMember.isActive());

		DelegateMember finalSecondaryDelegationMember = finalSecondaryDelegateType.getMembers().get(0);
		assertEquals("p9", finalSecondaryDelegationMember.getMemberId());
		assertTrue(finalSecondaryDelegationMember.isActive());
		Map<String, String> finalAttributes = finalSecondaryDelegationMember.getAttributes();
		assertEquals(2, finalAttributes.keySet().size());
		assertEquals("value1", finalAttributes.get("attr1"));
		assertEquals("value2", finalAttributes.get("attr2"));

	}
	
}
