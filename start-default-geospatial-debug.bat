java -Dserver.port=8881 -Drepository.type=default -Dspring.profiles.active=nocache -agentlib:jdwp=transport=dt_socket,server=y,address=8801 -jar sparql-playground.war 8881 geospatial small-data default false