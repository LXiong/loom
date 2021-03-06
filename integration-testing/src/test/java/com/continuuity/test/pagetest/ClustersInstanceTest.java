/**
 * Copyright 2012-2014, Continuuity, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.continuuity.test.pagetest;

import com.continuuity.loom.admin.ProvisionerAction;
import com.continuuity.test.Constants;
import com.continuuity.test.GenericTest;
import com.continuuity.test.TestUtil;
import com.continuuity.test.input.ClusterReader;
import com.continuuity.test.input.TestNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import static com.continuuity.test.drivers.Global.globalDriver;
import static org.junit.Assert.assertEquals;

/**
 * Test GET v1/loom/user/clusters/cluster/<cluster-id>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClustersInstanceTest extends GenericTest {
  private static final TestUtil TEST_UTIL = new TestUtil();
  private static final String CLUSTERTEMPLATE = "clusterTemplate";
  private static final By TABLE = By.cssSelector(".table.table-striped");
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final String HOUR = "hour";
  private static final String MINUTE = "minute";
  private static final String SECOND = "second";
  private static final By LOG = By.cssSelector(".serviceset-node-logs");
  private static final int ROW = 0;
  private static WebElement table;
  private static JsonObject cluster;

  @BeforeClass
  public static void runInitial() throws Exception {
    cluster= new ClusterReader().getCluster();
    globalDriver.get(Constants.CLUSTERS_INSTANCE_URL);
    table = globalDriver.findElement(TABLE);
  }

  @Test
  public void test_01_leftpanel() {
    assertEquals("Leftpanel is not correct.", Constants.LEFT_PANEL, TEST_UTIL.getLeftPanel(globalDriver));
  }

  @Test
  public void test_02_provider() {
    String templateName = cluster.get(CLUSTERTEMPLATE).getAsJsonObject().get("defaults").getAsJsonObject()
      .get("provider").getAsString();
    assertEquals("Provider is not correct.", templateName, TEST_UTIL.getTd(table, 6, 1));
  }

  @Test
  public void test_03_templateDescription() {
    String templateDescription = cluster.get(CLUSTERTEMPLATE).getAsJsonObject().get("description").getAsString();
    assertEquals("Templage description is not correct.", templateDescription, TEST_UTIL.getTd(table, 8, 1));
  }

  @Test
  public void test_04_nodes() throws ParseException {
    List<WebElement> serviceSets = globalDriver.findElements(By.cssSelector(".serviceset-name:not(.ng-hide)"));
    for (int i = 0; i < serviceSets.size(); i++) {
      serviceSets.get(i).click();
    }
    Set<TestNode> expectedTestNodes = getExpectedTestNodes();
    Set<TestNode> actualTestNodes = getActualTestNodes();
    assertEquals("Size is not correct.", expectedTestNodes.size(), actualTestNodes.size());
    assertEquals("Nodes are not correct.", expectedTestNodes, actualTestNodes);
  }

  @Test
  public void test_05_topmenu() {
    ImmutableSet<String> expectedTopList = ImmutableSet.of("test-woo", "loom-prod");
    assertEquals("The list of the topmenu is not correct.", expectedTopList, TEST_UTIL.getTopList(globalDriver));
    String uriPrefix = Constants.ROOT_URL + "/user/clusters/cluster/";
    ImmutableSet<String> clusterIds = ImmutableSet.of("00000139", "00000138");
    assertEquals("The uri of top list is not correct.", TEST_UTIL.getTopListUri(clusterIds, uriPrefix),
                 TEST_UTIL.getTopListUri(globalDriver));
  }

  @Test
  public void test_06_templateName() {
    String templateName = cluster.get(CLUSTERTEMPLATE).getAsJsonObject().get("name").getAsString();
    assertEquals("Template name is not correct.", templateName, TEST_UTIL.getTd(table, 7, 1));
  }

  @Test
  public void test_07_expiryTime() {
    int expiryTime = cluster.get("expireTime").getAsInt();
    String expiryString = "";
    if (expiryTime == 0) {
      expiryString = "Never";
    }
    assertEquals("Expiry time is not correct.", expiryString, TEST_UTIL.getTd(table, 3, 1));
  }

  private Set<TestNode> getExpectedTestNodes() {
    Set<TestNode> idTestNodes = Sets.newHashSet();
    JsonArray jsonArray = cluster.getAsJsonArray("nodes");

    for (int i = 0; i < jsonArray.size(); i++) {
      JsonObject properties = jsonArray.get(i).getAsJsonObject().getAsJsonObject("properties");
      String hostname = properties.get("hostname").getAsString();
      String id = cluster.getAsJsonArray("nodes").get(i).getAsJsonObject().get("id").getAsString();
      String ip = properties.get("ipaddress").getAsString();
      String username = properties.getAsJsonObject("ssh-auth").get("user").getAsString();
      String password = properties.getAsJsonObject("ssh-auth").get("password").getAsString();
      Set<TestNode.Action> actions = getExpectedActions(jsonArray.get(i).getAsJsonObject());
      idTestNodes.add(new TestNode(hostname, id, actions, ip, username, password));
    }
    return idTestNodes;
  }

  private Set<TestNode> getActualTestNodes() throws ParseException {

    Set<TestNode> idTestNodes = Sets.newHashSet();
    List<WebElement> tableRows = globalDriver.findElements(By.cssSelector(".node-entry"));

    for (int i = 0; i < tableRows.size(); i++) {
      WebElement tableRow = tableRows.get(i);
      List<WebElement> tableCells = TEST_UTIL.geTdsFromTr(tableRow);

      String hostname = tableCells.get(0).getAttribute(Constants.INNER_HTML);
      String nodeId = tableCells.get(1).getAttribute(Constants.INNER_HTML);
      String ip = tableCells.get(3).getAttribute(Constants.INNER_HTML);
      String username = tableCells.get(4).getAttribute(Constants.INNER_HTML);
      String password = tableCells.get(5).getAttribute(Constants.INNER_HTML);

      Set<TestNode.Action> actions = getActualActions(tableCells.get(2));
      idTestNodes.add(new TestNode(hostname, nodeId, actions, ip, username, password));

    }
    return idTestNodes;
  }

  private Set<TestNode.Action> getActualActions(WebElement tableCell) throws ParseException {

    Set<TestNode.Action> results = Sets.newHashSet();
    WebElement actionTable = tableCell.findElement(By.cssSelector(Constants.ACTION_TABLE_CLASSNAME));
    List<WebElement> tableRows = TEST_UTIL.getRows(actionTable);

    for (int i = 0; i < tableRows.size(); i++) {

      List<WebElement> tableCells = TEST_UTIL.geTdsFromTr(tableRows.get(i));

      ProvisionerAction action = ProvisionerAction.valueOf(tableCells.get(0).getAttribute(Constants.INNER_HTML));
      String service = tableCells.get(1).getAttribute(Constants.INNER_HTML);
      long submitTime = DATE_FORMAT.parse(tableCells.get(2).getAttribute(Constants.INNER_HTML)).getTime() / 1000;
      submitTime -= 8 * 60 * 60;
      long duration = getDuration(tableCells.get(3).getAttribute(Constants.INNER_HTML));
      String status = tableCells.get(4).getAttribute(Constants.INNER_HTML);
      results.add(new TestNode.Action(action, service, submitTime, duration, status));
    }

    return results;
  }

  private long getDuration(String durationText) {
    long duration = 0;
    int scale = 1;
    List<Integer> numbers = parseTime(durationText);
    for (int i = numbers.size() - 1; i >= 0; i-- ) {
      duration += numbers.get(i) * scale;
      scale *= 60;
    }
    return duration;
  }

  private List<Integer> parseTime(String durationText) {
    List<Integer> numbers = Lists.newArrayList();
    int startIndex = 0;
    int endIndex = 0;

    // Parse hour
    endIndex = parseNumber(numbers, durationText, HOUR, startIndex);

    // Parse minute
    startIndex = durationText.indexOf(" ", endIndex);
    endIndex = parseNumber(numbers, durationText, MINUTE, startIndex);

    // Parse second
    startIndex = durationText.indexOf(" ", endIndex);
    endIndex = parseNumber(numbers, durationText, SECOND, startIndex);
    return numbers;
  }

  private int parseNumber(List<Integer> numbers, String durationText, String term, int startIndex) {
    int endIndex =durationText.indexOf(term, startIndex);
    if (endIndex == -1) {
      numbers.add(0);
      endIndex = startIndex;
    } else {
      numbers.add(Integer.parseInt(durationText.substring(startIndex, endIndex).trim()));
    }
    return endIndex;
  }

  private Set<TestNode.Action> getExpectedActions(JsonObject jsonObject) {
    Set<TestNode.Action> results = Sets.newHashSet();
    JsonArray actionArray = jsonObject.getAsJsonObject().getAsJsonArray("actions");
    for (int i = 0; i < actionArray.size(); i++) {
      JsonObject json = actionArray.get(i).getAsJsonObject();
      String service = json.get("service").getAsString();
      ProvisionerAction action = ProvisionerAction.valueOf(json.get("action").getAsString().toUpperCase());
      long submitTime = Long.parseLong(json.get("submitTime").getAsString());
      long statusTime = Long.parseLong(json.get("statusTime").getAsString());
      long duration = Math.round((statusTime - submitTime) / 1000.0);
      String status = json.get("status").getAsString();
      results.add(new TestNode.Action(action, service, submitTime / 1000, duration, status));
    }
    return results;
  }

  @AfterClass
  public static void tearDown() {
    closeDriver();
  }
}
