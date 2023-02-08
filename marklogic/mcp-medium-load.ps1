$mlcp_batch_file = "E:\FAX\MasterTeza\MarkLogic\mlcp-10.0.6.1\bin\mlcp.bat"
$input_file_type = "-input_file_type RDF"
$output_uri_prefix = "-output_uri_prefix /triplestore/"
$credentials = "-username admin -password admin"
$mode_address_port = "-mode local -host localhost -port 8111 "

$rdf_files = Get-ChildItem "C:\Users\Dimitrije Mitic\Desktop\RedCrusaderJr-sparql-playground\geospatial\rdf-data\medium-data\"

foreach ($rdf_file in $rdf_files)
{
  $full_file_name = $rdf_file.FullName
  Write-Output "Importing $full_file_name"

  $input_file_path = "-input_file_path $full_file_name"
  Start-Process $mlcp_batch_file -ArgumentList import, $input_file_path, $input_file_type, $output_uri_prefix, $mode_address_port, $credentials -wait
}