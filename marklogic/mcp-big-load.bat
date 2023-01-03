@ECHO OFF
setlocal enabledelayedexpansion
for %%rdfXmlFilePath in (C:\Users\Dimitrije Mitic\Desktop\RedCrusaderJr-sparql-playground\geospatial\rdf-data\big-data\*) do (
  E:/FAX/MasterTeza/MarkLogic/mlcp-10.0.6.1/bin/mlcp.bat import -mode local -host localhost -port 8111 -username admin -password admin -input_file_path %%rdfXmlFilePath -input_file_type RDF -output_uri_prefix /triplestore/
  echo "rdfxml file: %%rdfXmlFilePath"
)