package gradeskope.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFolder {

  public ZipFolder() {
  }

  public void zipFolder(File srcFolder, File destZipFile) throws Exception {
    try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
         ZipOutputStream zip = new ZipOutputStream(fileWriter)) {

      addFolderToZip(srcFolder, srcFolder, zip);
    }
  }

  private void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip) throws Exception {

    if (srcFile.isDirectory()) {
      addFolderToZip(rootPath, srcFile, zip);
    } else {
      byte[] buf = new byte[1024];
      int len;
      try (FileInputStream in = new FileInputStream(srcFile)) {
        String name = srcFile.getPath();
        name = name.replace(rootPath.getPath(), "");
        System.out.println("Zip " + srcFile + "\n to " + name);
        zip.putNextEntry(new ZipEntry(name));
        while ((len = in.read(buf)) > 0) {
          zip.write(buf, 0, len);
        }
      }
    }
  }

  private void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip) throws Exception {
    for (File fileName : srcFolder.listFiles()) {
      addFileToZip(rootPath, fileName, zip);
    }
  }

  public void zipFolder(File srcFolder, File destZipFile, File[] excludeFiles) throws Exception {
    try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
         ZipOutputStream zip = new ZipOutputStream(fileWriter)) {

      addFolderToZip(srcFolder, srcFolder, zip, excludeFiles);
    }
  }

  private void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip,
                              File[] excludeFiles) throws Exception {
    for (File fileName : srcFolder.listFiles()) {
      addFileToZip(rootPath, fileName, zip, excludeFiles);
    }
  }

  private void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip, File[] excludeFiles)
      throws Exception {

    for (File excludedFile : excludeFiles) {
      if (Files.isSameFile(srcFile.toPath(), excludedFile.toPath())) {
        return;
      }
    }

    if (srcFile.isDirectory()) {
      addFolderToZip(rootPath, srcFile, zip);
    } else {
      byte[] buf = new byte[1024];
      int len;
      try (FileInputStream in = new FileInputStream(srcFile)) {
        String name = srcFile.getPath();
        name = name.replace(rootPath.getPath(), "");
        System.out.println("Zip " + srcFile + "\n to " + name);
        zip.putNextEntry(new ZipEntry(name));
        while ((len = in.read(buf)) > 0) {
          zip.write(buf, 0, len);
        }
      }
    }
  }
}