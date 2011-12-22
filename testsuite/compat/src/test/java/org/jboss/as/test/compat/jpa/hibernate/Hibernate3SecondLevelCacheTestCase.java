/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.compat.jpa.hibernate;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Scott Marlow
 */
@RunWith(Arquillian.class)
public class Hibernate3SecondLevelCacheTestCase {

    private static final String ARCHIVE_NAME = "Hibernate3SecondLevelCacheTestCase";


    private static final String persistence_xml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                    "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\" version=\"1.0\">" +
                    "  <persistence-unit name=\"hibernate3_pc\">" +
                    "    <description>Persistence Unit</description>" +
                    "    <jta-data-source>java:jboss/datasources/ExampleDS</jta-data-source>" +
                    "    <shared-cache-mode>ENABLE_SELECTIVE</shared-cache-mode>" +
                    "    <properties>" +
                    "      <property name=\"hibernate.hbm2ddl.auto\" value=\"create-drop\"/>" +
                    "      <property name=\"hibernate.show_sql\" value=\"true\"/>" +
                    "      <property name=\"hibernate.cache.use_second_level_cache\" value=\"true\"/>" +
                    "      <property name=\"hibernate.generate_statistics\" value=\"true\"/>" +
                    "      <property name=\"jboss.as.jpa.providerModule\" value=\"hibernate3-bundled\"/>" +
                    "    </properties>" +
                    "  </persistence-unit>" +
                    "</persistence>";

    private static void addHibernate3JarsToEar(EnterpriseArchive ear) {
        final String basedir = System.getProperty("basedir");
        final String testdir = basedir + File.separatorChar + "target" + File.separatorChar + "test-libs";
        File hibernateCore = new File(testdir, "hibernate3-core.jar");
        File hibernateAnnotations = new File(testdir, "hibernate3-commons-annotations.jar");
        File hibernateEntitymanager = new File(testdir, "hibernate3-entitymanager.jar");
        File hibernateInfinispan = new File(testdir, "hibernate3-infinispan.jar");
        File dom4j = new File(testdir, "dom4j.jar");
        File commonCollections = new File(testdir, "commons-collections.jar");
        File antlr = new File(testdir, "antlr.jar");
        ear.addAsLibraries(
            hibernateCore,
            hibernateAnnotations,
            hibernateEntitymanager,
            hibernateInfinispan,
            dom4j,
            commonCollections,
            antlr
        );
    }

    @Deployment
    public static Archive<?> deploy() throws Exception {

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, ARCHIVE_NAME + ".ear");
        addHibernate3JarsToEar(ear);

        JavaArchive lib = ShrinkWrap.create(JavaArchive.class, "beans.jar");
        lib.addClasses(SFSB1.class);
        ear.addAsModule(lib);

        lib = ShrinkWrap.create(JavaArchive.class, "entities.jar");
        lib.addClasses(Employee.class);
        lib.addAsManifestResource(new StringAsset(persistence_xml), "persistence.xml");
        ear.addAsLibraries(lib);

        final WebArchive main = ShrinkWrap.create(WebArchive.class, "main.war");
        main.addClasses(Hibernate3SecondLevelCacheTestCase.class);
        ear.addAsModule(main);

        // add application dependency on H2 JDBC driver, so that the Hibernate classloader (same as app classloader)
        // will see the H2 JDBC driver.
        // equivalent hack for use of shared Hiberante module, would be to add the H2 dependency directly to the
        // shared Hibernate module.
        // also add dependency on org.slf4j
        ear.addAsManifestResource(new StringAsset(
            "<jboss-deployment-structure>" +
            " <deployment>" +
            "  <dependencies>" +
            "   <module name=\"org.infinispan\" />" +
            "   <module name=\"com.h2database.h2\" />" +
            "   <module name=\"org.slf4j\"/>" +
            "  </dependencies>" +
            " </deployment>" +
            "</jboss-deployment-structure>"),
            "jboss-deployment-structure.xml");

        return ear;
    }

    @ArquillianResource
    private InitialContext iniCtx;

    protected <T> T lookup(String beanName, Class<T> interfaceType) throws NamingException {
        return interfaceType.cast(iniCtx.lookup("java:global/" + ARCHIVE_NAME + "/beans/" + beanName + "!" + interfaceType.getName()));
    }

    protected <T> T rawLookup(String name, Class<T> interfaceType) throws NamingException {
        return interfaceType.cast(iniCtx.lookup(name));
    }

    @Test
    public void testMultipleNonTXTransactionalEntityManagerInvocations() throws Exception {
        SFSB1 sfsb1 = lookup("SFSB1", SFSB1.class);
        sfsb1.createEmployee("Kelly Smith", "Watford, England", 10);
        sfsb1.createEmployee("Alex Scott", "London, England", 20);
        sfsb1.getEmployeeNoTX(10);
        sfsb1.getEmployeeNoTX(20);

        DataSource ds = rawLookup("java:jboss/datasources/ExampleDS", DataSource.class);
        Connection conn = ds.getConnection();
        try {
            int deleted = conn.prepareStatement("delete from Employee").executeUpdate();
            // verify that delete worked (or test is invalid)
            assertTrue("was able to delete added rows.  delete count=" + deleted, deleted > 1);

        } finally {
            conn.close();
        }

        // read deleted data from second level cache
        Employee emp = sfsb1.getEmployeeNoTX(10);

        assertTrue("was able to read deleted database row from second level cache", emp != null);
    }
}
