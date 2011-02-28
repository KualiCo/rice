/*
 * Copyright 2006-2011 The Kuali Foundation
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

package org.kuali.rice.core.api.component

import javax.xml.bind.JAXBContext
import org.junit.Assert
import org.junit.Test

class ComponentTest {
    private static final String XML = """
        <component xmlns="http://rice.kuali.org/core/v1_1">
            <code>PC</code>
            <name>Config</name>
            <namespaceCode>NSC</namespaceCode>
            <virtual>false</virtual>
            <active>true</active>
        </component>
    """

    private static final String CODE = "PC"
    private static final String NAME = "Config"
    private static final String NAMESPACE_CODE = "NSC"
    private static final boolean VIRTUAL = false
    private static final boolean ACTIVE = true

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_all_null() {
        Component.Builder.create(null, null, null, false);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_first_null() {
        Component.Builder.create(null, CODE, NAME, VIRTUAL);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_first_empty() {
        Component.Builder.create("", CODE, NAME, VIRTUAL);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_first_whitespace() {
        Component.Builder.create("  ", CODE, NAME, VIRTUAL);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_second_null() {
        Component.Builder.create(NAMESPACE_CODE, null, NAME, VIRTUAL);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_second_empty() {
        Component.Builder.create(NAMESPACE_CODE, "", NAME, VIRTUAL);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_second_whitespace() {
        Component.Builder.create(NAMESPACE_CODE, " ", NAME, VIRTUAL);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_third_null() {
        Component.Builder.create(NAMESPACE_CODE, CODE, null, VIRTUAL);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_third_empty() {
        Component.Builder.create(NAMESPACE_CODE, CODE, "", VIRTUAL);
    }

    @Test(expected=IllegalArgumentException.class)
    void test_Builder_fail_third_whitespace() {
        Component.Builder.create(NAMESPACE_CODE, CODE, "  ", VIRTUAL);
    }

    @Test
    void happy_path() {
        Component.Builder.create(NAMESPACE_CODE, CODE, NAME, VIRTUAL);
    }

    @Test
	public void test_Xml_Marshal_Unmarshal() {
	  def jc = JAXBContext.newInstance(Component.class)
	  def marshaller = jc.createMarshaller()
	  def sw = new StringWriter()

	  def param = this.create()
	  marshaller.marshal(param,sw)

	  def unmarshaller = jc.createUnmarshaller();
	  def actual = unmarshaller.unmarshal(new StringReader(sw.toString()))
	  def expected = unmarshaller.unmarshal(new StringReader(XML))

	  Assert.assertEquals(expected,actual)
	}

    private create() {
		return Component.Builder.create(new ComponentContract() {
				def String code ="PC"
				def String name = "Config"
				def String namespaceCode = "NSC"
                def boolean virtual = false
                def boolean active = true
			}).build()
	}
}
