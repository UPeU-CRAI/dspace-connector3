package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import com.upeu.connector.filter.EPersonFilterTranslator;
import com.upeu.connector.util.JsonUtil;
import com.upeu.connector.util.ValidationUtil;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

public class EPersonHandler extends BaseHandler {

    private static final String EPERSON_ENDPOINT = "/epersons";
    private static final Logger LOGGER = Logger.getLogger(EPersonHandler.class.getName());

    public EPersonHandler(DSpaceClient client) {
        super(client);
    }

    public Uid create(Set<Attribute> attributes) {
        try {
            // Validar y extraer atributos obligatorios
            String email = AttributeUtil.getStringValue(AttributeUtil.find("email", attributes));
            String firstName = AttributeUtil.getStringValue(AttributeUtil.find("firstname", attributes));
            String lastName = AttributeUtil.getStringValue(AttributeUtil.find("lastname", attributes));
            Boolean canLogIn = AttributeUtil.getBooleanValue(AttributeUtil.find("canLogIn", attributes));

            ValidationUtil.validateRequiredFields(email, firstName, lastName);
            ValidationUtil.validateEmail(email);

            // Crear el payload JSON
            JSONObject payload = new JSONObject();
            payload.put("email", email);
            payload.put("firstname", firstName);
            payload.put("lastname", lastName);
            payload.put("canLogIn", canLogIn != null ? canLogIn : false);

            // Usar el método genérico de BaseHandler para enviar la solicitud
            JSONObject response = create(EPERSON_ENDPOINT, payload);

            // Validar la respuesta
            validateJsonResponse(response, "id");

            // Retornar el UID del objeto creado
            return new Uid(response.getString("id"));
        } catch (Exception e) {
            handleApiException("Error al crear EPerson", e);
            return null; // Este punto no se alcanza debido al throw
        }
    }

    public Uid update(String id, Set<Attribute> attributes) {
        try {
            // Crear el objeto JSON con los datos actualizados
            JSONObject updates = new JSONObject();
            for (Attribute attribute : attributes) {
                updates.put(attribute.getName(), AttributeUtil.getSingleValue(attribute));
            }

            // Delegar la lógica al método genérico de BaseHandler
            JSONObject response = update(EPERSON_ENDPOINT, id, updates);

            // Validar la respuesta
            validateJsonResponse(response, "id");

            // Retornar el UID actualizado
            return new Uid(response.getString("id"));
        } catch (Exception e) {
            handleApiException("Error al actualizar el EPerson con ID: " + id, e);
            return null; // Este punto no se alcanza debido al throw
        }
    }

    /**
     * Obtiene los detalles de un ePerson específico por su ID.
     *
     * @param personId ID de la persona.
     * @return Objeto EPerson representando a la persona.
     * @throws Exception Si ocurre algún error durante la solicitud.
     */
    public EPerson getEPersonById(String personId) throws Exception {
        ValidationUtil.validateId(personId, "El ID de ePerson no puede ser nulo o vacío");
        try {
            String response = dSpaceClient.get("/server/api/eperson/epersons/" + personId);
            return parseEPerson(new JSONObject(response));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener ePerson con ID {0}: {1}", new Object[]{personId, e.getMessage()});
            throw e;
        }
    }

    /**
     * Crea un nuevo ePerson en el sistema DSpace-CRIS.
     *
     * @param firstName Nombre del ePerson.
     * @param lastName  Apellido del ePerson.
     * @param email     Correo electrónico del ePerson.
     * @param canLogIn  Indica si el ePerson puede iniciar sesión.
     * @return Objeto EPerson creado.
     * @throws Exception Si ocurre algún error durante la creación.
     */
    public EPerson createEPerson(String firstName, String lastName, String email, boolean canLogIn) throws Exception {
        ValidationUtil.validateRequiredFields(firstName, lastName, email);
        try {
            JSONObject metadata = new JSONObject()
                    .put("eperson.firstname", JsonUtil.createMetadataArray(firstName))
                    .put("eperson.lastname", JsonUtil.createMetadataArray(lastName))
                    .put("eperson.email", JsonUtil.createMetadataArray(email));

            JSONObject requestBody = new JSONObject();
            requestBody.put("metadata", metadata);
            requestBody.put("canLogIn", canLogIn);

            String response = dSpaceClient.post("/server/api/eperson/epersons", requestBody.toString());
            return parseEPerson(new JSONObject(response));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear ePerson: {0}", e.getMessage());
            throw e;
        }
    }

    /**
     * Actualiza la información de un ePerson existente.
     *
     * @param id      ID del ePerson.
     * @param updates Objeto JSON con los cambios a realizar.
     * @return Objeto EPerson actualizado.
     * @throws Exception Si ocurre algún error durante la actualización.
     */
    public EPerson updateEPerson(String id, JSONObject updates) throws Exception {
        ValidationUtil.validateId(id, "El ID de ePerson es requerido para actualizar");
        ValidationUtil.validateNotEmpty(updates, "Los cambios no pueden ser nulos o vacíos");
        try {
            String response = dSpaceClient.put("/server/api/eperson/epersons/" + id, updates.toString());
            return parseEPerson(new JSONObject(response));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar ePerson con ID {0}: {1}", new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Elimina un EPerson en DSpace.
     *
     * @param id El ID del EPerson a eliminar.
     */
    public void delete(String id) {
        try {
            // Delegar la lógica de eliminación al método genérico de BaseHandler
            delete(EPERSON_ENDPOINT, id);
            logger.info("EPerson con ID: " + id + " eliminado exitosamente.");
        } catch (Exception e) {
            handleApiException("Error al eliminar el EPerson con ID: " + id, e);
        }
    }

    /**
     * Realiza una búsqueda de EPerson en DSpace.
     *
     * @param query Parámetros de consulta en formato de cadena.
     * @param handler Handler para procesar los resultados.
     */
    public void search(String query, ResultsHandler handler) {
        try {
            // Delegar la búsqueda al método genérico en BaseHandler
            List<JSONObject> results = search(EPERSON_ENDPOINT, query);

            // Procesar cada resultado y enviarlo al handler
            for (JSONObject json : results) {
                ConnectorObject connectorObject = new ConnectorObjectBuilder()
                        .setUid(json.getString("id"))
                        .setName(json.getString("email"))
                        .addAttribute("firstname", json.getString("firstname"))
                        .addAttribute("lastname", json.getString("lastname"))
                        .addAttribute("canLogIn", json.optBoolean("canLogIn", false))
                        .build();

                handler.handle(connectorObject);
            }
        } catch (Exception e) {
            handleApiException("Error al buscar EPerson con query: " + query, e);
        }
    }

    /**
     * Obtiene una lista de ePersons con filtrado opcional.
     *
     * @param filter Filtro opcional para la búsqueda.
     * @return Lista de ePersons que cumplen con los criterios.
     * @throws Exception Si ocurre algún error durante la búsqueda.
     */
    public List<EPerson> getEPersons(Filter filter) throws Exception {
        // Define el endpoint base
        String baseEndpoint = "/server/api/eperson/epersons";

        // Crear una instancia del FilterHandler y traducir el filtro
        FilterHandler filterHandler = new FilterHandler();
        List<String> queryParams = filterHandler.translateFilter(filter);

        // Construir el endpoint completo con los parámetros de consulta
        String fullEndpoint = buildEndpoint(baseEndpoint, queryParams.toArray(new String[0]));

        // Realizar la solicitud GET y obtener los resultados
        String response = dSpaceClient.get(fullEndpoint);

        // Convertir la respuesta JSON y retornar la lista de EPersons
        return parseEPersons(new JSONObject(response));
    }

    /**
     * Convierte un JSONObject de respuesta en una lista de objetos EPerson.
     *
     * @param jsonResponse El objeto JSON que contiene los datos de ePersons.
     * @return Una lista de objetos EPerson.
     */
    private List<EPerson> parseEPersons(JSONObject jsonResponse) {
        List<EPerson> ePersons = new ArrayList<>();

        try {
            // Validar que la respuesta contiene el campo "_embedded" y "epersons"
            if (!jsonResponse.has("_embedded") || !jsonResponse.getJSONObject("_embedded").has("epersons")) {
                LOGGER.warning("La respuesta no contiene el campo '_embedded.epersons'");
                return ePersons;
            }

            // Obtener el array de ePersons
            JSONArray ePersonsArray = jsonResponse.getJSONObject("_embedded").getJSONArray("epersons");

            // Iterar sobre cada objeto JSON en el array
            for (int i = 0; i < ePersonsArray.length(); i++) {
                JSONObject ePersonJson = ePersonsArray.getJSONObject(i);
                ePersons.add(parseEPerson(ePersonJson));
            }
        } catch (Exception e) {
            LOGGER.severe("Error al parsear ePersons: " + e.getMessage());
            throw new RuntimeException("Error al parsear la respuesta de ePersons", e);
        }

        return ePersons;
    }

    /**
     * Convierte un objeto JSON de ePerson en un objeto EPerson.
     *
     * @param ePersonJson El objeto JSON con los datos de ePerson.
     * @return Un objeto EPerson.
     */
    private EPerson parseEPerson(JSONObject ePersonJson) {
        try {
            String id = ePersonJson.optString("id", null);
            String email = ePersonJson.optString("email", null);
            boolean canLogIn = ePersonJson.optBoolean("canLogIn", false);

            JSONObject metadata = ePersonJson.optJSONObject("metadata");
            String firstName = JsonUtil.extractMetadataValue(metadata, "eperson.firstname");
            String lastName = JsonUtil.extractMetadataValue(metadata, "eperson.lastname");

            return new EPerson(id, email, firstName, lastName, canLogIn);
        } catch (Exception e) {
            LOGGER.severe("Error al parsear JSON de ePerson: " + e.getMessage());
            throw new RuntimeException("Estructura de JSON de ePerson no válida", e);
        }
    }

    @Override
    protected boolean validate(Object entity) {
        if (entity instanceof EPerson) {
            EPerson ePerson = (EPerson) entity;

            // Validaciones genéricas usando ValidationUtil
            ValidationUtil.validateRequiredFields(
                    ePerson.getEmail(),
                    ePerson.getFirstName(),
                    ePerson.getLastName()
            );

            // Aquí puedes agregar reglas adicionales específicas de negocio
            if (!ePerson.getEmail().contains("@")) {
                logger.error("El correo electrónico no es válido: " + ePerson.getEmail());
                return false;
            }

            return true; // Validación exitosa
        } else {
            logger.error("El objeto no es una instancia de EPerson.");
            return false;
        }
    }

}
