# rest-users-connectors

A sample Rest Connector for midPoint implementing the Rest Connector Superclass model

# Installation

1. Clone this repository

```
git clone https://github.com/UPeU-CRAI/dspace-connector.git
```

2. Compile the sources and run the application

```
cd dspace-connector
mvn clean package
```

3. Copy the connector jar to the midpoint folder

```
cp target/cd dspace-connector-0.0.X-SNAPSHOT.jar $MIDPOINT_HOME/var/icf-connectors/
```

4. Restart midPoint

5. Create the resource using the connector


# References

* Rest Users Api: https://github.com/Identicum/rest-users-api
* Rest Connector Superclass: https://wiki.evolveum.com/display/midPoint/REST+Connector+Superclass 
* Rest Resource Example: https://github.com/Evolveum/midpoint/tree/master/samples/resources/rest
