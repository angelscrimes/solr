/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.core;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.apache.commons.io.file.PathUtils;
import org.apache.solr.SolrTestCaseJ4;
import org.junit.Test;

public class TestCorePropertiesReload extends SolrTestCaseJ4 {

  private final Path solrHomeDirectory = createTempDir();

  public void setMeUp() throws Exception {
    PathUtils.copyDirectory(TEST_HOME(), solrHomeDirectory);
    Properties props = new Properties();
    props.setProperty("test", "Before reload");
    writeProperties(props);
    initCore("solrconfig.xml", "schema.xml", solrHomeDirectory);
  }

  @Test
  public void testPropertiesReload() throws Exception {
    setMeUp();
    SolrCore core = h.getCore();
    CoreDescriptor coreDescriptor = core.getCoreDescriptor();
    String testProp = coreDescriptor.getCoreProperty("test", null);
    assertEquals("Before reload", testProp);

    // Re-write the properties file
    Properties props = new Properties();
    props.setProperty("test", "After reload");
    writeProperties(props);

    h.reload();
    core = h.getCore();
    coreDescriptor = core.getCoreDescriptor();

    testProp = coreDescriptor.getCoreProperty("test", null);
    assertEquals("After reload", testProp);
  }

  private void writeProperties(Properties props) throws Exception {
    Writer out = null;
    try {
      Path confDir = solrHomeDirectory.resolve("collection1").resolve("conf");
      out =
          new BufferedWriter(
              new OutputStreamWriter(
                  Files.newOutputStream(confDir.resolve("solrcore.properties")),
                  StandardCharsets.UTF_8));
      props.store(out, "Reload Test");

    } finally {
      out.close();
    }
  }
}
