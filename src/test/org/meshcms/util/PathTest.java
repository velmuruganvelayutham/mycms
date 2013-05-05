package org.meshcms.util;

import junit.framework.*;
import java.io.*;
import java.util.*;

public class PathTest extends TestCase {
  
  public PathTest(String testName) {
    super(testName);
  }
  
  protected void setUp() throws Exception {
  }
  
  protected void tearDown() throws Exception {
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(PathTest.class);
    
    return suite;
  }
  
  /**
   * Test of add method, of class org.meshcms.util.Path.
   */
  public void testAdd() {
    System.out.println("add");
    
    Path instance = new Path("a/b");
    instance = instance.add("c");
    assertEquals(instance, new Path("a/b/c"));
    instance = instance.add("../d");
    assertEquals(instance, new Path("a/b/d"));
    instance = new Path("../c");
    instance = instance.add("../../d");
    assertEquals(instance, new Path("../../d"));
  }
  
  /**
   * Test of getParent method, of class org.meshcms.util.Path.
   */
  public void testGetParent() {
    System.out.println("getParent");
    
    Path instance = new Path("a/b");
    instance = instance.getParent();
    assertEquals(instance, new Path("a"));
    instance = new Path("../../a");
    instance = instance.getParent();
    assertEquals(instance, new Path("../.."));
    instance = new Path("..");
    instance = instance.getParent();
    assertEquals(instance, new Path("../.."));
  }
  
  /**
   * Test of getPartial method, of class org.meshcms.util.Path.
   */
  public void testGetPartial() {
    System.out.println("getPartial");
    
    Path instance = new Path("a/b/c/d");
    instance = instance.getPartial(2);
    assertEquals(instance, new Path("a/b"));
  }
  
  /**
   * Test of getCommonPath method, of class org.meshcms.util.Path.
   */
  public void testGetCommonPath() {
    System.out.println("getCommonPath");
    
    Path instance = new Path("a/b/c/d");
    instance = instance.getCommonPath(new Path("a/b/e"));
    assertEquals(instance, new Path("a/b"));
    instance = instance.getCommonPath(new Path("b/e"));
    assertEquals(instance, Path.ROOT);
  }
  
  /**
   * Test of isRelative method, of class org.meshcms.util.Path.
   */
  public void testIsRelative() {
    System.out.println("isRelative");
    
    Path instance = new Path("a/b/c/d");
    assertTrue(!instance.isRelative());
    instance = new Path("..");
    assertTrue(instance.isRelative());
    instance = new Path("../c");
    assertTrue(instance.isRelative());
  }
  
  /**
   * Test of isRoot method, of class org.meshcms.util.Path.
   */
  public void testIsRoot() {
    System.out.println("isRoot");
    
    Path instance = new Path("a/b/c/d");
    assertTrue(!instance.isRoot());
    instance = new Path("c/..");
    assertTrue(instance.isRoot());
  }
  
  /**
   * Test of isChildOf method, of class org.meshcms.util.Path.
   */
  public void testIsChildOf() {
    System.out.println("isChildOf");
    
    Path instance = new Path("a/b/c/d");
    assertTrue(!instance.isChildOf(new Path("a/b")));
    assertTrue(instance.isChildOf(new Path("a/b/c")));
    assertTrue(!instance.isChildOf(new Path("a/b/c/d")));
    assertTrue(!instance.isChildOf(new Path("a/b/c/d/e")));
    instance = new Path("../a");
    assertTrue(instance.isChildOf(new Path("..")));
    assertTrue(!instance.isChildOf(Path.ROOT));
  }
  
  /**
   * Test of isContainedIn method, of class org.meshcms.util.Path.
   */
  public void testIsContainedIn() {
    System.out.println("isContainedIn");
    
    Path instance = new Path("a/b/c/d");
    assertTrue(instance.isContainedIn(new Path("a/b")));
    assertTrue(instance.isContainedIn(new Path("a/b/c")));
    assertTrue(instance.isContainedIn(new Path("a/b/c/d")));
    assertTrue(!instance.isContainedIn(new Path("a/b/c/d/e")));
    instance = new Path("../a");
    assertTrue(!instance.isContainedIn(Path.ROOT));
  }
  
  /**
   * Test of getRelativeTo method, of class org.meshcms.util.Path.
   */
  public void testGetRelativeTo() {
    System.out.println("getRelativeTo");
    
    Path instance = new Path("a/b/c/d");
    instance = instance.getRelativeTo(new Path("a/b"));
    assertEquals(instance, new Path("c/d"));
    instance = new Path("a");
    instance = instance.getRelativeTo(new Path("b/c"));
    assertEquals(instance, new Path("../../a"));
    instance = new Path("../a");
    instance = instance.getRelativeTo(new Path("b/c"));
    assertEquals(instance, new Path("../../../a"));
    instance = new Path("a/b/c/d");
    instance = instance.getRelativeTo(new Path("a/e/f"));
    assertEquals(instance, new Path("../../b/c/d"));
    
    assertEquals(new Path("a/b").getRelativeTo(new Path("c")), new Path("../a/b"));
    assertEquals(new Path("a/b").getRelativeTo(new Path("a")), new Path("b"));
    assertEquals(new Path("a/b").getRelativeTo(new Path(".")), new Path("a/b"));
    assertEquals(new Path("../a").getRelativeTo(new Path(".")), new Path("../a"));
    assertEquals(new Path("../a").getRelativeTo(new Path("b/c")), new Path("../../../a"));
    assertEquals(new Path("a").getRelativeTo(new Path("b/c")), new Path("../../a"));
    assertEquals(new Path("../a/b").getRelativeTo(new Path("a")), new Path("../../a/b"));
    assertEquals(new Path("../a/b").getRelativeTo(new Path("../a")), new Path("b"));
    assertEquals(new Path("../a/b").getRelativeTo(new Path("../a/c")), new Path("../b"));
  }
  
  /**
   * Test of getElementCount method, of class org.meshcms.util.Path.
   */
  public void testGetElementCount() {
    System.out.println("getElementCount");
    
    Path instance = new Path("a/../b/c/./d");
    assertEquals(instance.getElementCount(), 3);
  }
  
  /**
   * Test of getElementAt method, of class org.meshcms.util.Path.
   */
  public void testGetElementAt() {
    System.out.println("getElementAt");
    
    Path instance = new Path("a/../b/c/./d");
    assertEquals(instance.getElementAt(1), "c");
  }
  
  /**
   * Test of getLastElement method, of class org.meshcms.util.Path.
   */
  public void testGetLastElement() {
    System.out.println("getLastElement");
    
    Path instance = new Path("a/b/c/d");
    assertEquals(instance.getLastElement(), "d");
    instance = new Path("a/b/c/..");
    assertEquals(instance.getLastElement(), "b");
  }
  
  /**
   * Test of getAsLink method, of class org.meshcms.util.Path.
   */
  public void testGetAsLink() {
    System.out.println("getAsLink");
    
    Path instance = new Path("a/b");
    assertEquals(instance.getAsLink(), "/a/b");
    instance = new Path("../a/b");
    assertEquals(instance.getAsLink(), "../a/b");
    assertEquals(Path.ROOT.getAsLink(), "");
  }
  
  /**
   * Test of compareTo method, of class org.meshcms.util.Path.
   */
  public void testCompareTo() {
    System.out.println("compareTo");
    
    Path p1 = new Path("abc/def");
    Path p2 = new Path("abc/de/f");
    Path p3 = new Path("abcd/e/f");
    Path p4 = new Path("abc");
    
    assertEquals(p3.compareTo(p3), 0);
    assertTrue(p1.compareTo(p2) > 0);
    assertTrue(p1.compareTo(p3) < 0);
    assertTrue(p1.compareTo(p4) > 0);
    assertTrue(p2.compareTo(p1) < 0);
    assertTrue(p2.compareTo(p3) < 0);
    assertTrue(p2.compareTo(p4) > 0);
    assertTrue(p4.compareTo(p3) < 0);
  }
  
  /**
   * Test of equals method, of class org.meshcms.util.Path.
   */
  public void testEquals() {
    System.out.println("equals");
    
    Path p1 = new Path("abc/def");
    Path p2 = new Path("abc/./def/b/..");
    assertTrue(p1.equals(p2));
  }
  
  /**
   * Test of commonPart method, of class org.meshcms.util.Path.
   */
  public void testCommonPart() {
    // tested by testGetCommonPath
  }
  
  /**
   * Test of replace method, of class org.meshcms.util.Path.
   */
  public void testReplace() {
    System.out.println("replace");
    
    Path instance = new Path("a/b/c");
    instance = instance.replace(1, "d");
    assertEquals(instance, new Path("a/d/c"));
    instance = instance.replace(2, "e");
    assertEquals(instance, new Path("a/d/e"));
  }
}
