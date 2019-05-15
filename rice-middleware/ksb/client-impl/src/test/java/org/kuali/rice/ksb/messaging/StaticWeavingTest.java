/**
 * Copyright 2005-2019 The Kuali Foundation
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
package org.kuali.rice.ksb.messaging;

import org.junit.Test;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * This test verifies that all of the JPA objects in this module are statically weaved.
 *
 * <p>If one executes this test from within an IDE environment, it very well may not pass if one of the JPA objects
 * under examination was modified and then recompiled by the IDE. The static weaving process is handled by Maven,
 * so without executing the appropriate Maven lifecycle phase, the class will not get weaved. Regardless, this test
 * should *always* pass when executed from the command line.</p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class StaticWeavingTest {

    private static final String PACKAGE = StaticWeavingTest.class.getPackage().getName();
    private static final String CLASS_PERSISTENCE_OBJECT = "org.eclipse.persistence.internal.descriptors.PersistenceObject";
    private static final String CLASS_PERSISTENCE_ENTITY = "org.eclipse.persistence.internal.descriptors.PersistenceEntity";
    private static final String WEAVED_METHOD_PREFIX = "_persistence_";

    @Test
    public void testStaticWeaving() {

        final ClassPathScanningCandidateComponentProvider entityScanner =
                new AllClassPathScanningCandidateComponentProvider(false);
        entityScanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        final ClassPathScanningCandidateComponentProvider mscScanner =
                new AllClassPathScanningCandidateComponentProvider(false);
        mscScanner.addIncludeFilter(new AnnotationTypeFilter(MappedSuperclass.class));

        final ClassPathScanningCandidateComponentProvider embeddableScanner =
                new AllClassPathScanningCandidateComponentProvider(false);
        embeddableScanner.addIncludeFilter(new AnnotationTypeFilter(Embeddable.class));

        final Class<?> persistenceObject;
        final Class<?> persistenceEntity;
        try {
            persistenceObject = Class.forName(CLASS_PERSISTENCE_OBJECT);
            persistenceEntity = Class.forName(CLASS_PERSISTENCE_ENTITY);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        final Set<Class<?>> notWeaved = Stream.concat(
                findNotWeaved(Stream.of(persistenceObject, persistenceEntity).collect(Collectors.toSet()),
                        entityScanner.findCandidateComponents(PACKAGE),
                        mscScanner.findCandidateComponents(PACKAGE)),
                findNotWeaved(Collections.singleton(persistenceObject),
                        embeddableScanner.findCandidateComponents(PACKAGE)))
                .collect(Collectors.toSet());

        assertTrue( "(NOTE: it is expected this test may fail if executed from the IDE instead of command line "
                + "since the IDE will not execute the static weaving automatically). Found a class which is "
                + "not bytecode weaved (contains no methods starting with '_persistence'): " + notWeaved + " "
                + "In order to resolve this, please ensure that this type is included in "
                + "META-INF/persistence-weaving.xml", notWeaved.isEmpty());
    }

    /**
     * If JPA weaving is enabled then all annotated classes should implement an interface called PersistenceObject and sometimes PersistenceEntity
     */
    @SafeVarargs
    private final Stream<Class<?>> findNotWeaved(Set<Class<?>> isAssignableFrom, Set<BeanDefinition>... types) {

        final Set<Class<?>> classes = Stream.of(types)
                .flatMap(Set::stream)
                .map(BeanDefinition::getBeanClassName)
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet());

        return classes.stream()
                .filter(type -> {
                    //checking if the objects have "declared" methods because just implementing an interface on a superclass is not enough.
                    //They must also have these methods implemented.
                    //Since rice statically weaves, all classes extended from a rice base class will implement these weaved in interfaces.
                    final boolean weavedMethod = Stream.of(type.getDeclaredMethods())
                            .map(Method::getName)
                            .anyMatch(methodName -> methodName.startsWith(WEAVED_METHOD_PREFIX));

                    return !isAssignableFrom.stream().allMatch(intr -> intr.isAssignableFrom(type)) || !weavedMethod;
                });
    }


    private static final class AllClassPathScanningCandidateComponentProvider extends ClassPathScanningCandidateComponentProvider {

        public AllClassPathScanningCandidateComponentProvider(boolean useDefaultFilters) {
            super(useDefaultFilters);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return true;
        }
    }
}
