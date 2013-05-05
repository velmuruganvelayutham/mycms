package org.meshcms.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Base class to perform operations on the contents of a directory.
 *
 * <p>Override the method <code>preProcessDirectory</code>,
 * <code>postProcessDirectory</code> and
 * <code>processFile</code> to define the actions to be taken for files and
 * directories included in the processed directory. You can also override
 * <code>preProcess</code> and <code>postProcess</code> to do additional
 * operations before and after the directory parsing.</p>
 *
 * <p>The directory to be parsed must be specified before starting by calling
 * on of the <code>setInitialDir</code> methods. Then you can start the parsing
 * by calling <code>process</code> or asinchronously by creating a new thread
 * and starting it.</p>
 *
 * <p>Please note that this class is <em>not</em> recursive by default. You must
 * call <code>setRecursive(true)</code> before processing if you want it to
 * process directory contents too.</p>
 *
 * @author Luciano Vernaschi
 */
public class DirectoryParser extends Thread {
  protected File initialDir;

  protected boolean recursive = false;
  protected boolean processStartDir = false;

  private Comparator comparator;

  /**
   * If true, directories will be processed recursively (default false).
   *
   * @param recursive it the directories will be processed recursively
   */
  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  /**
   * If true, <code>processDirectory</code> will be called for the
   * base directory too (default false).
   *
   * @param processStartDir if to process the base directory too.
   *
   * @see #processDirectory
   */
  public void setProcessStartDir(boolean processStartDir) {
    this.processStartDir = processStartDir;
  }

  /**
   * Returns whether directories will be processed recursively or not.
   *
   * @see #setRecursive
   */
  public boolean isRecursive() {
    return recursive;
  }

  /**
   * Returns whether <code>processDirectory</code> will be called for the
   * base directory too.
   *
   * @see #processDirectory
   * @see #setProcessStartDir
   */
  public boolean isProcessStartDir() {
    return processStartDir;
  }

  /**
   * If true, files and directories will be sorted used a
   * <code>FileNameComparator</code>.
   *
   * @see FileNameComparator
   */
  public void setSorted(boolean sorted) {
    if (sorted) {
      comparator = new FileNameComparator();
    } else {
      comparator = null;
    }
  }

  /**
   * Returns whether files and directories will be sorted used a
   * <code>FileNameComparator</code> or not.
   *
   * @see FileNameComparator
   * @see #setSorted
   */
  public boolean isSorted() {
    return comparator != null;
  }

  /**
   * Sets the directory to be processed. An istance of <code>File</code> is
   * created and {@link #setInitialDir(File)} is called.
   *
   * @param dir the file path as a <code>String</code>
   */
  public void setInitialDir(String dir) {
    setInitialDir(Utils.isNullOrEmpty(dir) ? null : new File(dir));
  }

  /**
   * Sets the directory to be processed.
   *
   * @param dir the directory path as a <code>File</code>
   */
  public void setInitialDir(File dir) {
    initialDir = dir;
  }

  /**
   * Returns the directory to be processed.
   */
  public File getInitialDir() {
    return initialDir;
  }

  /**
   * Starts processing (in a separate thread if instantiated properly).
   */
  public void run() {
    process();
  }

  /**
   * Starts processing.
   */
  public void process() {
    if (initialDir == null || !initialDir.exists()) {
      return;
    }

    if (preProcess()) {
      parse(initialDir, Path.ROOT);
    }

    postProcess();
  }

  private void parse(File file, Path path) {
    if (file.isDirectory()) {
      if (recursive || path.getElementCount() == 0) {
        boolean ok = true;

        if (mustProcessDir(path)) {
          ok = preProcessDirectory(file, path);
        }

        if (ok) {
          File[] list = file.listFiles();

          if (comparator != null) {
            Arrays.sort(list, comparator);
          }

          for (int i = 0; i < list.length; i++) {
            parse(list[i], path.add(list[i].getName()));
          }
        }
      }

      if (mustProcessDir(path)) {
        postProcessDirectory(file, path);
      }
    } else if (file.isFile()) {
      processFile(file, path);
    }
  }

  private boolean mustProcessDir(Path path) {
    return processStartDir || path.getElementCount() != 0;
  }

  /**
   * This method is called during the process, but before any element has been
   * processed. If it returns false, no processing will take place.
   *
   * <p>The base implementation does nothing and returns true.</p>
   *
   * @return always true
   */
  protected boolean preProcess() {
    return true;
  }

  /**
   * This method is called at the end of the processing. It is called even if
   * {@link #preProcess} returned false.
   *
   * <p>The base implementation does nothing.</p>
   */
  protected void postProcess() {
  }

  protected boolean preProcessDirectory(File file, Path path) {
    return true;
  }

  protected void postProcessDirectory(File file, Path path) {
  }

  /**
   * This method will be called for any file found while parsing the base
   * directory.
   *
   * @param file the file to be processed
   * @param path the path of the file (relative to the base directory)
   */
  protected void processFile(File file, Path path) {
  }
}
