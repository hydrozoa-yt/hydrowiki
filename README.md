# HydroWiki
This is a basic wiki software created for fun. 

The following libraries are used:
* Jetty, for networking
* FreeMarker, for templating
* MariaDB, for the database
* java-diff-utils for comparison operations

## Features
* Wikitext parser (markdown, wikilinks and -images)
* Writing new articles
* Display random article
* Display recent changes
* Display differences between versions
* Users and logging in

## How to run
### 1) Database installation
To setup the database, log into your mariadb server and execute the installation script.
```
mysql -u root -p < sql/install.sql
```
Also change the settings in `data/config.properties` to connect to your database.

### 2) Configuration
Configuration is done in `data/config.properties`, make sure you have the correct info for your database.
```
app.url = 128.0.0.1
app.port = 8080
app.debug = true

# Database
db.address=128.0.0.1
db.username=hydrowiki_user
db.password=password
db.database=hydrowiki_dev
db.pool_size=16

# Object storage for media
s3.endpoint = https://example.cloud
s3.region = nl-ams
s3.bucket_name = example-bucket
s3.access_key = a_access_key
s3.secret_key = a_secret_key
s3.public_access = https://example-bucket.example.cloud
```

### 3) Running HydroWiki locally
1. Run the gradle task `downloadBoostrap` at least once. This downloads bootstrap js and css.
2. Run `dk.hydrozoa.hydrowiki.Main`.

### 4) Running HydroWiki remotely
1. Run the gradle task `distributeApp`. This will pack all needed files to run into `dis/`. Make sure to copy `data/config.properties` to `dis/data/config.properties`.
2. Copy `dis/` to somewhere on your server, for example `/var/www/hydrowiki/` on linux.
3. Run the hydrowiki jar on your server by running `java -jar hydrowiki.jar`, or create a systemd task that starts it

## Future development
The following features are planned:
* Red links for missing articles
* Page last edited on all articles
* Debug info containing nanos to render the page and number of database lookups