# HydroWiki
This is a basic wiki software created for fun. 

The following technologies are used:
* Jetty, for the webserver
* FreeMarker, for templating
* MariaDB, for the database
* java-diff-utils for comparison operations
* Gradle, for building

## Features
* Simple wikitext parser (only headings and paragraphs)
* Writing new articles
* Find random article
* Display recent changes
* View differences between versions

## How to run
### 1) Database installation
To setup the database, log into your mariadb server and execute the installation script.
```
mysql -u root -p < sql/install.sql
```
Also change the settings in `data/config.properties` to connect to your database.  
### 2) Running HydroWiki
Run the gradle task `downloadBoostrap` at least once.

Run `dk.hydrozoa.hydrowiki.Main`.

## Future development
The following features are planned:
* Users and logging in
* Uploadable images by users