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

import java.io.File;
import javax.naming.InitialContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.test.compat.common.EmployeeBean;
import org.jboss.as.test.compat.common.HttpRequest;
import org.jboss.as.test.compat.common.JndiUtil;
import org.jboss.as.test.compat.common.Employee;
import org.jboss.as.test.compat.common.JpaEmployeeBean;
import org.jboss.as.test.compat.common.SimpleServlet;
import org.jboss.as.test.compat.common.TestUtil;
import org.jboss.as.test.compat.common.WebLink;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * test for https://hibernate.onjira.com/browse/HHH-6716.
 *
 * @author Strong Liu
 */
@RunWith(Arquillian.class)
public class Dom4jLoadingTestCase {
    private static final String ARCHIVE_NAME = "hibernate_dom4j";

       private static final String persistence_xml =
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
               "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\" version=\"1.0\">" +
               "  <persistence-unit name=\"test-compat-persistence-context\">" +
               "    <description>Persistence Unit." +
               "    </description>" +
               "  <jta-data-source>java:jboss/datasources/ExampleDS</jta-data-source>" +
               "<properties> <property name=\"hibernate.hbm2ddl.auto\" value=\"create-drop\"/>" +
               "<property name=\"hibernate.show_sql\" value=\"true\"/>" +
               "</properties>" +
               "  </persistence-unit>" +
               "</persistence>";

       private static final String web_persistence_xml =
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
               "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\" version=\"1.0\">" +
               "  <persistence-unit name=\"web_hibernate3_pc\">" +
               "    <description>Persistence Unit." +
               "    </description>" +
               "  <jta-data-source>java:jboss/datasources/ExampleDS</jta-data-source>" +
               "<properties> <property name=\"hibernate.hbm2ddl.auto\" value=\"create-drop\"/>" +
               "<property name=\"hibernate.show_sql\" value=\"true\"/>" +
               "</properties>" +
               "  </persistence-unit>" +
               "</persistence>";

       private static void addDom4jJarToEar(EnterpriseArchive ear) {
           final String basedir = System.getProperty("basedir");
           final String testdir = basedir + File.separatorChar + "target" + File.separatorChar + "test-libs";
           File dom4j = new File(testdir, "dom4j.jar");
           ear.addAsLibrary( dom4j );
       }

       @Deployment
       public static Archive<?> deploy() throws Exception {

           EnterpriseArchive ear = ShrinkWrap.create( EnterpriseArchive.class, ARCHIVE_NAME + ".ear" );
           addDom4jJarToEar( ear );

           JavaArchive lib = ShrinkWrap.create(JavaArchive.class, "beans.jar");
           lib.addClasses(EmployeeBean.class, JpaEmployeeBean.class, HttpRequest.class);
           ear.addAsModule(lib);

           lib = ShrinkWrap.create(JavaArchive.class, "entities.jar");
           lib.addClasses(Employee.class);
           lib.addAsManifestResource(new StringAsset(persistence_xml), "persistence.xml");
           ear.addAsLibraries(lib);

           final WebArchive main = ShrinkWrap.create(WebArchive.class, "main.war");
           main.addClasses(Dom4jLoadingTestCase.class, JndiUtil.class, TestUtil.class);
           ear.addAsModule(main);

           // add war that contains its own pu
           WebArchive war = ShrinkWrap.create(WebArchive.class, ARCHIVE_NAME + ".war");
           war.addClasses(SimpleServlet.class, WebLink.class);
           war.addAsResource(new StringAsset(web_persistence_xml), "META-INF/persistence.xml");

           war.addAsWebInfResource(
                   new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "\n" +
                           "<web-app version=\"3.0\"\n" +
                           "         xmlns=\"http://java.sun.com/xml/ns/javaee\"\n" +
                           "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                           "         xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\"\n" +
                           "         metadata-complete=\"false\">\n" +
                           "<servlet-mapping>\n" +
                           "        <servlet-name>SimpleServlet</servlet-name>\n" +
                           "        <url-pattern>/simple/*</url-pattern>\n" +
                           "    </servlet-mapping>\n" +
                           "</web-app>"),
                   "web.xml");

           ear.addAsModule(war);

           return ear;
       }

       @ArquillianResource
       private InitialContext iniCtx;

       @Test
       public void testSimpleCreateAndLoadEntities() throws Exception {
           final EmployeeBean employeeBean = JndiUtil.lookup(iniCtx, ARCHIVE_NAME, JpaEmployeeBean.class, EmployeeBean.class);
           TestUtil.testSimpleCreateAndLoadEntities(employeeBean);
       }

       private static String performCall(String urlPattern, String param) throws Exception {
           return HttpRequest.get("http://localhost:8080/" + ARCHIVE_NAME + "/" + urlPattern + "?input=" + param, 10, SECONDS);
       }

       @Test
       public void testServletSubDeploymentRead() throws Exception {
           TestUtil.testServletSubDeploymentRead(ARCHIVE_NAME, "Hello+world");
       }

}
