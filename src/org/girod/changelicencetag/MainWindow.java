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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @since 0.1
 */
public class MainWindow extends JFrame {
   private final List<String> newTag = new ArrayList<>();
   private static final Pattern DATES = Pattern.compile("\\d+");
   private JTextField dirField = null;
   private JTextField tagField = null;
   private JTextField propsField = null;
   private File dir = null;
   private String filter = null;
   private String currentDate = null;
   private int countProcessed = 0;
   private int countChanged = 0;
   private final List<File> skippedFiles = new ArrayList<>();
   private int countSkipped = 0;

   public MainWindow() {
      super();
      this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      this.setTitle("Change Licence Tags " + getVersion());
      createContent();
   }

   private String getVersion() {
      String version = "";
      try {
         URL url = getClass().getResource("config.properties");
         try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            PropertyResourceBundle bundle = new PropertyResourceBundle(in);
            version = bundle.getString("version");
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return version;
   }

   private void selectNewLicenceTag(File file) {
      try {
         storeNewTag(file);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void setProperties(File file) {
      try {
         try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            PropertyResourceBundle bundle = new PropertyResourceBundle(in);
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
               String key = keys.nextElement();
               String value = bundle.getString(key);
               if (value != null) {
                  if (key.equals("filter")) {
                     filter = value.trim();
                  }
                  if (key.equals("date")) {
                     currentDate = value.trim();
                  }
               }
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void createContent() {
      JButton but = new JButton("Text");
      int height = but.getPreferredSize().height;
      Container pane = this.getContentPane();
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

      pane.add(Box.createVerticalStrut(5));
      JPanel propertiesPanel = new JPanel();
      propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.X_AXIS));

      JLabel label = new JLabel("Properties");
      propertiesPanel.add(label);
      propertiesPanel.add(Box.createHorizontalStrut(5));
      propertiesPanel.add(label);
      propsField = new JTextField(20);
      propsField.setEditable(false);
      propsField.setMaximumSize(new Dimension(propsField.getMaximumSize().width, height));
      propertiesPanel.add(propsField);

      JButton fileButton = new JButton("...");
      fileButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Set Properties");
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int ret = chooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
               File file = chooser.getSelectedFile();
               propsField.setText(file.getPath());
               setProperties(file);
            }
         }
      });

      propertiesPanel.add(Box.createHorizontalStrut(5));
      propertiesPanel.add(fileButton);
      propertiesPanel.add(Box.createHorizontalGlue());
      pane.add(propertiesPanel);
      pane.add(Box.createVerticalStrut(5));

      JPanel tagPanel = new JPanel();
      tagPanel.setLayout(new BoxLayout(tagPanel, BoxLayout.X_AXIS));

      label = new JLabel("New Licence");
      tagPanel.add(label);
      tagPanel.add(Box.createHorizontalStrut(5));
      tagField = new JTextField(20);
      tagField.setMaximumSize(new Dimension(tagField.getMaximumSize().width, height));
      tagField.setEditable(false);
      tagPanel.add(tagField);
      tagPanel.add(Box.createHorizontalStrut(5));
      fileButton = new JButton("...");
      fileButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Set new Licence tag");
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int ret = chooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
               File file = chooser.getSelectedFile();
               tagField.setText(file.getPath());
               selectNewLicenceTag(file);
            }
         }
      });
      tagPanel.add(fileButton);
      tagPanel.add(Box.createHorizontalStrut(5));
      tagPanel.add(Box.createHorizontalGlue());

      pane.add(tagPanel);
      pane.add(Box.createVerticalStrut(5));

      JPanel dirPanel = new JPanel();
      dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.X_AXIS));

      label = new JLabel("Directory");
      dirPanel.add(label);
      dirPanel.add(Box.createHorizontalStrut(5));
      dirField = new JTextField(20);
      dirField.setEditable(false);
      dirField.setMaximumSize(new Dimension(dirField.getMaximumSize().width, height));
      dirPanel.add(dirField);
      dirPanel.add(Box.createHorizontalStrut(5));
      fileButton = new JButton("...");
      fileButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Set directory");
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = chooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
               dir = chooser.getSelectedFile();
               dirField.setText(dir.getPath());
            }
         }
      });
      dirPanel.add(fileButton);
      dirPanel.add(Box.createHorizontalGlue());
      pane.add(dirPanel);
      pane.add(Box.createVerticalStrut(5));

      JPanel applyPanel = new JPanel();
      applyPanel.setLayout(new BorderLayout());
      JButton applyButton = new JButton("Apply");
      applyPanel.add(applyButton, BorderLayout.SOUTH);
      pane.add(applyPanel);
      applyButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               changeLicenceTags();
            } catch (IOException ex) {
            }
         }
      });

      pane.add(Box.createVerticalGlue());
   }

   private void storeNewTag(File file) throws IOException {
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

   private void changeLicenceTags() throws IOException {
      countProcessed = 0;
      countChanged = 0;
      countSkipped = 0;
      skippedFiles.clear();
      if (dir == null || newTag.isEmpty()) {
         System.out.println("Parameters incorrect");
      } else {
         changeLicenceTags(dir);
         System.out.println("finished, " + countProcessed + " files processed, " + countChanged + " files changed, skipped " + countSkipped);
         if (!skippedFiles.isEmpty()) {
            showSkippedFiles();
         }
      }
   }

   private void showSkippedFiles() {
      JTextArea textArea = new JTextArea();
      StringBuilder buf = new StringBuilder();
      Iterator<File> it = skippedFiles.iterator();
      while (it.hasNext()) {
         File file = it.next();
         buf.append(file.getPath());
         if (it.hasNext()) {
            buf.append("\n");
         }
      }
      textArea.setText(buf.toString());
      JDialog dialog = new JDialog(this, "Skipped Files");
      dialog.getContentPane().add(new JScrollPane(textArea));
      dialog.setSize(500, 700);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
   }

   private void changeLicenceTags(File dir) throws IOException {
      File[] files = dir.listFiles();
      if (files != null && files.length > 0) {
         for (int i = 0; i < files.length; i++) {
            File child = files[i];
            if (child.isDirectory()) {
               changeLicenceTags(child);
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
      if (filter == null) {
         return true;
      } else {
         Iterator<String> it = content.iterator();
         while (it.hasNext()) {
            String line = it.next();
            if (line.contains(filter)) {
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
         Matcher m = DATES.matcher(line);

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
                  buf.append(", ");
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
      if (currentDate != null) {
         String _date = date.trim();
         if (!_date.endsWith(currentDate) && !_date.endsWith(",")) {
            date += ", " + currentDate;
         }
      }
      return date;
   }

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) throws IOException {
      MainWindow window = new MainWindow();
      window.setSize(400, 200);
      window.setVisible(true);
   }

}
