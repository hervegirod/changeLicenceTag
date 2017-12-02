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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.PropertyResourceBundle;
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
 * @version 0.2
 */
public class MainWindow extends JFrame {
   private final Options options = new Options();
   private ChangeLicenseEngine engine;
   private File dir = null;
   private JTextField dirField = null;
   private JTextField tagField = null;
   private JTextField propsField = null;

   public MainWindow() {
      super();
      engine = new ChangeLicenseEngine(options);
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
         engine.storeNewTag(file);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void setProperties(File file) {
      options.reset();
      try {
         try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            PropertyResourceBundle bundle = new PropertyResourceBundle(in);
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
               String key = keys.nextElement();
               String value = bundle.getString(key);
               if (value != null) {
                  if (key.equals("filter")) {
                     options.filter = value.trim();
                  }
                  if (key.equals("date")) {
                     options.currentDate = value.trim();
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

   private void changeLicenceTags() throws IOException {
      List<File> skippedFiles = engine.changeLicenceTags(dir);
      if (skippedFiles != null && !skippedFiles.isEmpty()) {
         showSkippedFiles(skippedFiles);
      }
   }

   private void showSkippedFiles(List<File> skippedFiles) {
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
      Container pane = dialog.getContentPane();
      pane.setLayout(new BorderLayout());
      pane.add(new JScrollPane(textArea), BorderLayout.CENTER);

      JPanel printPanel = new JPanel();
      JButton okButton = new JButton("OK");
      JButton printButton = new JButton("Print");
      okButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            dialog.dispose();
         }
      });
      printButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            print(skippedFiles);
         }
      });
      printPanel.add(okButton);
      printPanel.add(printButton);

      pane.add(printPanel, BorderLayout.SOUTH);

      dialog.setSize(500, 700);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
   }

   /**
    * Print the content of the panel as html.
    */
   private void print(List<File> skippedFiles) {
      JFileChooser chooser = new JFileChooser();
      chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
      chooser.setDialogTitle("Set HTML File");
      chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int ret = chooser.showOpenDialog(this);
      if (ret == JFileChooser.APPROVE_OPTION) {
         File file = chooser.getSelectedFile();
         String name = file.getName();
         int index = name.lastIndexOf('.');
         if (index == -1) {
            name = name + ".html";
            file = new File(file.getParentFile(), name);
         }
         try {
            printImpl(skippedFiles, file);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private void printImpl(List<File> skippedFiles, File file) throws IOException {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
         Iterator<File> it = skippedFiles.iterator();
         while (it.hasNext()) {
            File skippedFile = it.next();
            String path = skippedFile.getPath();
            path = path.replace("\\", "/");
            String hyperlink = "<a href=\"file://" + path + "\">" + path + "</a>";
            writer.append(hyperlink);
            writer.newLine();
            writer.append("<br/>");
            writer.newLine();
         }
         writer.flush();
      }
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
