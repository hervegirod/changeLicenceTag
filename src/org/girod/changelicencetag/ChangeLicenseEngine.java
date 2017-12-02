/*
Copyright (c) 2017, Herve Girod
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies,
either expressed or implied, of the FreeBSD Project.

Alternatively if you have any questions about this project, you can visit
the project website at the project page on https://github.com/hervegirod/ChangeLicenceTag
 */
package org.girod.changelicencetag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @since 0.2
 */
public class ChangeLicenseEngine {
   private Options options;
   private static final Pattern DATE = Pattern.compile("\\d+");
   private static final Pattern DATE2 = Pattern.compile("(.*-\\s*)(\\d+)");
   private final List<String> newTag = new ArrayList<>();
   private int countProcessed = 0;
   private int countChanged = 0;
   private final List<File> skippedFiles = new ArrayList<>();
   private int countSkipped = 0;

   public ChangeLicenseEngine(Options options) {
      this.options = options;
   }

   public void storeNewTag(File file) throws IOException {
      newTag.clear();
      try (BufferedReader in = new BufferedReader(new FileReader(file))) {
         boolean first = true;
         while (true) {
            String line = in.readLine();
            if (line != null) {
               if (first) {
                  first = false;
               } else {
                  newTag.add("\n");
               }
               newTag.add(line);
            } else {
               break;
            }
         }
      }
   }

   public List<File> changeLicenceTags(File dir) throws IOException {
      countProcessed = 0;
      countChanged = 0;
      countSkipped = 0;
      skippedFiles.clear();
      if (dir == null || newTag.isEmpty()) {
         System.out.println("Parameters incorrect");
         return null;
      } else {
         changeLicenceTagsImpl(dir);
         System.out.println("finished, " + countProcessed + " files processed, " + countChanged + " files changed, skipped " + countSkipped);
         return skippedFiles;
      }
   }

   private void changeLicenceTagsImpl(File dir) throws IOException {
      File[] files = dir.listFiles();
      if (files != null && files.length > 0) {
         for (int i = 0; i < files.length; i++) {
            File child = files[i];
            if (child.isDirectory()) {
               changeLicenceTagsImpl(child);
            } else if (child.getName().endsWith(".java")) {
               changeLicenceTag(child);
            }
         }
      }
   }

   private void changeLicenceTag(File file) throws IOException {
      countProcessed++;
      List<String> oldContent;
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
         oldContent = new ArrayList<>();
         boolean first = true;
         while (true) {
            String line = reader.readLine();
            if (line != null) {
               if (first) {
                  first = false;
               } else {
                  oldContent.add("\n");
               }
               oldContent.add(line);
            } else {
               break;
            }
         }
      }
      if (!oldContent.isEmpty()) {
         boolean isFiltered = filter(oldContent);
         if (isFiltered) {
            String firstLine = oldContent.get(0);
            if (firstLine.trim().startsWith("/*")) {
               countChanged++;
               String date = getDate(oldContent);
               List<String> newContent = replaceLicenceTag(oldContent, date);
               try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                  Iterator<String> it = newContent.iterator();
                  while (it.hasNext()) {
                     String line = it.next();
                     writer.append(line);
                  }
                  writer.flush();
               }
            } else {
               skippedFiles.add(file);
               countSkipped++;
            }
         } else {
            skippedFiles.add(file);
            countSkipped++;
         }
      }
   }

   private List<String> replaceLicenceTag(List<String> oldContent, String date) {
      List<String> newContent = new ArrayList<>();
      boolean inTag = true;

      Iterator<String> it = newTag.iterator();
      while (it.hasNext()) {
         String line = it.next();
         int index = line.indexOf("$date");
         if (index != -1) {
            line = line.substring(0, index) + date + line.substring(index + 5);
         }
         newContent.add(line);
      }

      it = oldContent.iterator();
      while (it.hasNext()) {
         String line = it.next();
         if (!inTag) {
            newContent.add(line);
         } else if (line.trim().endsWith("*/")) {
            inTag = false;
         }
      }
      return newContent;
   }

   private boolean filter(List<String> content) {
      if (options.filter == null) {
         return true;
      } else {
         Iterator<String> it = content.iterator();
         while (it.hasNext()) {
            String line = it.next();
            if (line.contains(options.filter)) {
               return true;
            }
         }
         return false;
      }
   }

   private String getDate(List<String> content) {
      Iterator<String> it = content.iterator();
      StringBuilder buf = new StringBuilder();
      boolean foundLine = false;
      while (it.hasNext()) {
         String line = it.next();
         Matcher m = DATE.matcher(line);

         int start = 0;
         while (true) {
            boolean founded;
            if (start == 0) {
               founded = m.find();
            } else {
               founded = m.find(start);
            }
            if (founded) {
               foundLine = true;
               start = m.start();
               int end = m.end();
               String group = line.substring(start, end);
               if (!buf.toString().isEmpty()) {
                  if (start > 3 && line.substring(0, start).trim().endsWith("-")) {
                     buf.append(" - ");
                  } else {
                     buf.append(", ");
                  }
               }
               buf.append(group);
               start = end + 1;
               if (start >= line.length() - 1) {
                  break;
               }
            } else {
               break;
            }
         }
         if (foundLine) {
            break;
         }
      }
      String date = buf.toString();
      if (date.isEmpty()) {
         return null;
      }
      if (options.currentDate != null) {
         String _date = date.trim();
         if (!_date.endsWith(options.currentDate) && !_date.endsWith(",")) {
            Matcher m = DATE2.matcher(_date);
            if (m.matches()) {
               String group1 = m.group(1);
               date = group1 + options.currentDate;
            } else {
               date += ", " + options.currentDate;
            }
         }
      }
      return date;
   }
}
