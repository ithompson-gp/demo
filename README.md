You can start the app with `./gradlew bootRun`


Then call the generation with `curl -X GET http://localhost:8080/api/documents/invoice/INV-12345 -o invoice.docx`

It's streaming the docx generated so, that why I am piping to a file (the `invoice.docx` file, it will be created on the same path as the `curl` call was executed in)
