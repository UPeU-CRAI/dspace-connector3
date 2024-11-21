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


# Documentación del Flujo del Conector DSpace-CRIS

Este conector integra operaciones con el sistema DSpace-CRIS mediante un flujo bien estructurado que gestiona la autenticación, las rutas de API y las operaciones CRUD en entidades clave como `EPerson`, `Group` e `Item`. A continuación, se describe en detalle cómo se gestionan los componentes fundamentales del conector: `baseUrl`, `Endpoints` y autenticación.

## Flujo de `baseUrl`

- El `baseUrl` representa la URL base de la instancia DSpace que se está conectando. Este se define y valida en la clase de configuración `DSpaceConnectorConfiguration` para asegurar que:
    - No esté vacío o nulo.
    - Comience con `http://` o `https://` para garantizar un formato adecuado.
- Este `baseUrl` se utiliza para inicializar la clase `Endpoints`, que centraliza la construcción de rutas de API dinámicas.

## Flujo de `Endpoints`

La clase `Endpoints` es responsable de gestionar las rutas de API necesarias para las operaciones del conector:

- **Creación Dinámica de Rutas**: Se construyen dinámicamente las rutas añadiendo los paths específicos al `baseUrl`. Ejemplo:

    ```java
    public String getEPersonsEndpoint() {
        return buildEndpoint("/server/api/eperson/epersons");
    }
    ```

- **Centralización de Rutas**: Las rutas están organizadas en métodos que generan dinámicamente los endpoints para cada operación (e.g., CRUD de `EPerson`, `Group`, e `Item`).

- **Consistencia**: Todos los `Handlers` (`EPersonHandler`, `GroupHandler`, `ItemHandler`) reciben la instancia de `Endpoints` desde el conector principal (`DSpaceConnector`), garantizando un uso consistente y centralizado.

## Flujo de Autenticación

La clase `AuthenticationHandler` gestiona la autenticación de la conexión al sistema DSpace utilizando tokens CSRF y JWT:

- **Token CSRF**:
    - Se obtiene mediante una solicitud al endpoint `/server/api/authn/status`.
    - Se almacena en un `BasicCookieStore` y se reutiliza para solicitudes posteriores.

- **Token JWT**:
    - Se obtiene mediante el endpoint `/server/api/authn/login` utilizando las credenciales de usuario configuradas (`username` y `password`).
    - El token se renueva automáticamente si ha expirado, asegurando sesiones válidas en todo momento.

- **Reutilización de HttpClient**:
    - Un único cliente HTTP (`CloseableHttpClient`) se reutiliza para manejar todas las solicitudes del conector, configurado con un `BasicCookieStore` para gestionar las cookies de sesión.

## Ejemplo de Uso

### Configuración

Define el `baseUrl` y las credenciales en la configuración del conector:

```java
DSpaceConnectorConfiguration config = new DSpaceConnectorConfiguration();
config.setBaseUrl("http://localhost:8080");
config.setUsername("admin");
config.setPassword(new GuardedString("password".toCharArray()));
