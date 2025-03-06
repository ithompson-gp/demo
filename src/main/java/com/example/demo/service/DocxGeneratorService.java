package com.example.demo.service;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.docx4j.Docx4J;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class DocxGeneratorService {

  private static final String TEMPLATE_FILES_EXTENSION = ".html";
  private static final String FILES_LOCATION = "templates/";
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
  public void generateDocx(
      final String templateName,
      final Map<String, Object> jsonData,
      final OutputStream outputStream) throws Exception {
    // Load and compile the Handlebars template
    final Template template = loadTemplate(templateName);

    // Apply JSON data to the template
    final String html = template.apply(jsonData);

    // Setup Word package
    final WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
    final NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
    wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
    ndp.unmarshalDefaultNumbering();
    // Create an HTML importer (to convert to DocX)
    final XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMLPackage);

    // Get the Document - XHTML - from the rendered template
    final Document document = getDocument(html);

    // Clean and prepare the XHTML
    final String cleanHtml = document.html();

    // In order for the conversion to access the files and other resources
    // associated with the XHTML
    final String baseUrl = new ClassPathResource(FILES_LOCATION).getURL().toString();

    // Convert XHTML to DocX
    // Use ByteArrayInputStream to avoid file system operations
    try (final InputStream htmlStream = new ByteArrayInputStream(cleanHtml.getBytes(StandardCharsets.UTF_8))) {
      wordMLPackage.getMainDocumentPart().getContent().addAll(
          xhtmlImporter.convert(htmlStream, baseUrl));
    }

    // Stream the DocX directly to the output
    Docx4J.save(wordMLPackage, outputStream);
  }

  private Document getDocument(final String html) {
    final Document document = Jsoup.parse(html);
    document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    return document;
  }

  private Template loadTemplate(final String templateName) throws IOException {
    final Resource resource = new ClassPathResource(FILES_LOCATION + templateName + TEMPLATE_FILES_EXTENSION);
    try (final Reader reader = new InputStreamReader(resource.getInputStream())) {
      return handlebars.compileInline(new BufferedReader(reader)
          .lines()
          .reduce("", (a, b) -> a + "\n" + b));
    }
  }
}
