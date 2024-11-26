package com.upeu.connector.handler;

import com.upeu.connector.DSpaceClient;
import com.upeu.connector.filter.EPersonFilterTranslator;
import com.upeu.connector.util.JsonUtil;
import com.upeu.connector.util.ValidationUtil;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler para gestionar operaciones de ePerson en DSpace-CRIS.
 * Optimizado para ser compatible con Midpoint.
 */
public class EPersonHandler {

    private static final Logger LOGGER = Logger.getLogger(EPersonHandler.class.getName());
    private final DSpaceClient client;
    private final EPersonFilterTranslator filterTranslator;

    /**
     * Constructor del handler.
     *
     * @param client Cliente DSpace para interactuar con la API.
     */
    public EPersonHandler(DSpaceClient client) {
        this.client = client;
        this.filterTranslator = new EPersonFilterTranslator();
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
            String response = client.get("/server/api/eperson/epersons/" + personId);
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
            JSONObject metadata = new JSONObject();
            metadata.put("eperson.firstname", JsonUtil.createMetadataArray(firstName));
            metadata.put("eperson.lastname", JsonUtil.createMetadataArray(lastName));
            metadata.put("eperson.email", JsonUtil.createMetadataArray(email));

            JSONObject requestBody = new JSONObject();
            requestBody.put("metadata", metadata);
            requestBody.put("canLogIn", canLogIn);

            String response = client.post("/server/api/eperson/epersons", requestBody.toString());
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
            String response = client.put("/server/api/eperson/epersons/" + id, updates.toString());
            return parseEPerson(new JSONObject(response));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar ePerson con ID {0}: {1}", new Object[]{id, e.getMessage()});
            throw e;
        }
    }

    /**
     * Elimina un ePerson por su ID.
     *
     * @param id ID del ePerson.
     * @throws Exception Si ocurre algún error durante la eliminación.
     */
    public void deleteEPerson(String id) throws Exception {
        ValidationUtil.validateId(id, "El ID de ePerson es requerido para eliminar");
        try {
            client.delete("/server/api/eperson/epersons/" + id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar ePerson con ID {0}: {1}", new Object[]{id, e.getMessage()});
            throw e;
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
        StringBuilder endpoint = new StringBuilder("/server/api/eperson/epersons");

        if (filter != null) {
            List<String> queryParams = filterTranslator.translate(filter);
            if (!queryParams.isEmpty()) {
                endpoint.append(queryParams.get(0));
            }
        }

        return fetchEPersons(endpoint.toString());
    }

    /**
     * Realiza una solicitud para obtener una lista de ePersons desde un endpoint específico.
     *
     * @param endpoint URL del endpoint.
     * @return Lista de objetos EPerson.
     * @throws Exception Si ocurre algún error durante la solicitud.
     */
    private List<EPerson> fetchEPersons(String endpoint) throws Exception {
        List<EPerson> ePersons = new ArrayList<>();
        try {
            String response = client.get(endpoint);
            JSONObject jsonResponse = new JSONObject(response);

            if (!jsonResponse.has("_embedded") || !jsonResponse.getJSONObject("_embedded").has("epersons")) {
                LOGGER.warning("La respuesta no contiene el campo '_embedded.epersons'");
                return ePersons;
            }

            JSONArray ePersonsArray = jsonResponse.getJSONObject("_embedded").getJSONArray("epersons");
            for (int i = 0; i < ePersonsArray.length(); i++) {
                JSONObject ePersonJson = ePersonsArray.getJSONObject(i);
                ePersons.add(parseEPerson(ePersonJson));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener ePersons desde el endpoint {0}: {1}", new Object[]{endpoint, e.getMessage()});
            throw e;
        }
        return ePersons;
    }

    /**
     * Convierte un objeto JSON de respuesta en un objeto EPerson.
     *
     * @param ePersonJson Objeto JSON con los datos del ePerson.
     * @return Objeto EPerson.
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
            LOGGER.log(Level.SEVERE, "Error al parsear JSON de ePerson: {0}", e.getMessage());
            throw new RuntimeException("Estructura de JSON de ePerson no válida", e);
        }
    }
}
