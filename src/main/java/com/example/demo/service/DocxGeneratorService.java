package com.example.demo.service;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.docx4j.Docx4J;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class DocxGeneratorService {

  private final Handlebars handlebars = new Handlebars();

  /**
   * Generates a DOCX from a Handlebars template and JSON data, writing to an
   * output stream
   * 
   * @param templateName The name of the template file (without extension)
   * @param jsonData     The data to apply to the template
   * @param outputStream The stream to write the DOCX to
   * @throws Exception If any error occurs during generation
   */
  public void generateDocx(String templateName, Map<String, Object> jsonData, OutputStream outputStream)
      throws Exception {
    // 1. Load and compile the Handlebars template
    Template template = loadTemplate(templateName);

    // 2. Apply JSON data to the template
    String html = template.apply(jsonData);

    // 3. Clean and prepare the HTML
    Document document = Jsoup.parse(html);
    document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    String cleanHtml = document.html();

    // 4. Convert HTML to DocX
    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
    XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMLPackage);

    // Use ByteArrayInputStream to avoid file system operations
    try (InputStream htmlStream = new ByteArrayInputStream(cleanHtml.getBytes(StandardCharsets.UTF_8))) {
      wordMLPackage.getMainDocumentPart().getContent().addAll(
          xhtmlImporter.convert(htmlStream, null));
    }

    // 5. Stream the DocX directly to the output
    Docx4J.save(wordMLPackage, outputStream);
  }

  private Template loadTemplate(String templateName) throws IOException {
    ClassPathResource resource = new ClassPathResource("templates/" + templateName + ".html");
    try (Reader reader = new InputStreamReader(resource.getInputStream())) {
      return handlebars.compileInline(new BufferedReader(reader)
          .lines()
          .reduce("", (a, b) -> a + "\n" + b));
    }
  }
}
