package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import dto.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import utils.Props;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Controller {
	private final static Logger log = LogManager.getLogger(Controller.class);

	private final static boolean sortByName = Boolean
			.valueOf(Props.getGlobalProperty(Props.GlobalProperties.SORT_BY_NAME));
	
	static {
		Unirest.setObjectMapper(new ObjectMapper() {
		    private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
		                = new com.fasterxml.jackson.databind.ObjectMapper();

		    public <T> T readValue(String value, Class<T> valueType) {
		        try {
		            return jacksonObjectMapper.readValue(value, valueType);
		        } catch (IOException e) {
		            throw new RuntimeException(e);
		        }
		    }

		    public String writeValue(Object value) {
		        try {
		            return jacksonObjectMapper.writeValueAsString(value);
		        } catch (JsonProcessingException e) {
		            throw new RuntimeException(e);
		        }
		    }
		});

	}

	public List<DTOBranch> getBranches(LoginUser lu) throws UnirestException {
		HttpResponse<DTOBranch[]> asObject = Unirest.get(lu.getServerIPPort() + "/rest/servicepoint/branches/")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asObject(DTOBranch[].class);
		return sortAndRemove(asObject.getBody(), sortByName);
	}

	public List<DTOWorkProfile> getWorkProfile(LoginUser lu, DTOBranch branchId) throws UnirestException {
		HttpResponse<DTOWorkProfile[]> asObject = Unirest
				.get(lu.getServerIPPort() + "/rest/servicepoint/branches/{branchId}/workProfiles/")
				.routeParam("branchId", branchId.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asObject(DTOWorkProfile[].class);
		return sortAndRemove(asObject.getBody(), sortByName);
	}

	public List<DTOServicePoint> getServicePoints(LoginUser lu, DTOBranch branchId) throws UnirestException {
		HttpResponse<DTOServicePoint[]> asObject = Unirest
				.get(lu.getServerIPPort()
						+ "/rest/servicepoint/branches/{branchID}/servicePoints/deviceTypes/SW_SERVICE_POINT")
				.routeParam("branchID", branchId.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asObject(DTOServicePoint[].class);
		return sortAndRemove(asObject.getBody(), sortByName);
	}
	
	public List<DTOEntryPoint> getEntryPoints(LoginUser lu, DTOBranch branchId) throws UnirestException {
		HttpResponse<DTOEntryPoint[]> asObject = Unirest
				.get(lu.getServerIPPort()
						+ "/rest/entrypoint/branches/{branchID}/entryPoints/deviceTypes/SW_RECEPTION")
				.routeParam("branchID", branchId.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asObject(DTOEntryPoint[].class);
		return sortAndRemove(asObject.getBody(), sortByName);
	}

	public void startSession(LoginUser lu, DTOBranch branchId, DTOServicePoint spId) throws UnirestException {

		HttpResponse<JsonNode> asJson = Unirest
				.put(lu.getServerIPPort()
						+ "/rest/servicepoint/branches/{branchId}/servicePoints/{servicePointId}/users/{username}/")
				.routeParam("branchId", branchId.getIdAsString())
				.routeParam("servicePointId", spId.getIdAsString())
				.routeParam("username", lu.getUsername())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();

		logResponse(asJson);
	}

	public void endSession(LoginUser lu, DTOBranch branchId, DTOServicePoint spId) throws UnirestException {

		HttpResponse<JsonNode> asJson = Unirest
				.delete(lu.getServerIPPort()
						+ "/rest/servicepoint/branches/{branchId}/servicePoints/{servicePointId}/users/{username}/")
				.routeParam("branchId", branchId.getIdAsString()).
				routeParam("servicePointId", spId.getIdAsString())
				.routeParam("username", lu.getUsername())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();

		logResponse(asJson);
	}

	public DTOUserStatus callNext(LoginUser lu, DTOBranch branch, DTOServicePoint spId) throws UnirestException {

		HttpResponse<JsonNode> asJson = Unirest
				.post(lu.getServerIPPort()
						+ "/rest/servicepoint/branches/{branchID}/servicePoints/{servicePointId}/visits/next/")
				.routeParam("branchID", branch.getIdAsString())
				.routeParam("servicePointId", spId.getIdAsString())
				.header("Allow", "POST")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();

		logResponse(asJson);

		JSONObject object = new JSONObject(asJson.getBody());

		DTOUserStatus userStat = new Gson().fromJson(object.getJSONObject("object").toString(), DTOUserStatus.class);
		DTOUserStatus.Visit visi = new Gson().fromJson(object.getJSONObject("object").getJSONObject("visit").toString(),
				DTOUserStatus.Visit.class);

		userStat.setVisit(visi);

		return userStat;
	}
	
	public void callNextAndEnd(LoginUser lu, DTOBranch branch, DTOServicePoint spId, Integer serviceId) throws UnirestException {
		String json = "{\"services\" : [\""+serviceId.toString() + "\"]}";
		String url = lu.getServerIPPort()
				+ "/rest/servicepoint/branches/{branchID}/servicePoints/{servicePointId}/visits/createAndEnd?transactionTime=600";
		HttpResponse<JsonNode> asJson = Unirest .post(url)
				.routeParam("branchID", branch.getIdAsString())	
				.routeParam("servicePointId", spId.getIdAsString())
				.header("Allow", "POST")
				.header("accept", "application/json")
				.header("Content-Type", "application/json")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.body(json)
				.asJson();
		
		logResponse(asJson);
	}

	public DTOUserStatus recall(LoginUser lu, DTOBranch branchId, DTOServicePoint spId) throws UnirestException {

		HttpResponse<JsonNode> asJson = Unirest
				.put(lu.getServerIPPort()
						+ "/rest/servicepoint/branches/{branchID}/servicePoints/{servicePointId}/visit/recall/")
				.routeParam("branchID", branchId.getIdAsString())
				.routeParam("servicePointId", spId.getIdAsString())
				.header("Allow", "PUT")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();

		logResponse(asJson);

		JSONObject object = new JSONObject(asJson.getBody());

		DTOUserStatus userStat = new Gson().fromJson(object.getJSONObject("object").toString(), DTOUserStatus.class);
		DTOUserStatus.Visit visi = new Gson().fromJson(object.getJSONObject("object").getJSONObject("visit").toString(),
				DTOUserStatus.Visit.class);

		userStat.setVisit(visi);

		return userStat;
	}
	
	public DTOVisit createVisit(LoginUser lu, DTOBranch branch, DTOEntryPoint epId, DTOService service) throws UnirestException {
		HttpResponse<JsonNode> asJson = Unirest
				.post(lu.getServerIPPort()
						+ "/rest/entrypoint/branches/{branchID}/entryPoints/{entryPointId}/visits/")
				.routeParam("branchID", branch.getIdAsString())
				.routeParam("entryPointId", epId.getIdAsString())
				.header("Allow", "POST")
				.header("accept", "application/json")
                .header("Content-Type", "application/json")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.body("{\"services\" : [" + service.getId() + "]}")
				.asJson();

		logResponse(asJson);

		JSONObject object = new JSONObject(asJson.getBody());

		DTOVisit vsist = new Gson().fromJson(object.getJSONObject("object").toString(), DTOVisit.class);

		return vsist;
	}

	public void setWorkProfile(LoginUser lu, DTOBranch branchId, DTOWorkProfile wpId) throws UnirestException {
		HttpResponse<JsonNode> asJson = Unirest
				.put(lu.getServerIPPort()
						+ "/rest/servicepoint/branches/{branchID}/users/{userName}/workProfile/{workProfileId}/")
				.routeParam("branchID", branchId.getIdAsString())
				.routeParam("userName", lu.getUsername())
				.routeParam("workProfileId", wpId.getIdAsString())
				.header("Allow", "PUT")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();
		
		log.info("SetWP");
		logResponse(asJson);
	}

	public void endVisit(LoginUser lu, DTOBranch branchId, String visitId) throws UnirestException {
		HttpResponse<JsonNode> asJson = Unirest
				.put(lu.getServerIPPort() + "/rest/servicepoint/branches/{branchID}/visits/{visitId}/end/")
				.routeParam("branchID", branchId.getIdAsString())
				.routeParam("visitId", visitId)
				.header("Allow", "PUT")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();
		logResponse(asJson);

	}

	public List<DTOQueue> getQueueInfoForWorkprofile(LoginUser lu, DTOBranch branch, DTOWorkProfile wp)
			throws UnirestException {
		HttpResponse<DTOQueue[]> asObject = Unirest
				.get(lu.getServerIPPort()
						+ "/rest/servicepoint/branches/{branchID}/workProfiles/{workProfileId}/queues/")
				.routeParam("branchID", branch.getIdAsString())
				.routeParam("workProfileId", wp.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asObject(DTOQueue[].class);
		return sortAndRemove(asObject.getBody(), sortByName);
	}

	public List<DTOQueue> getQueueInfo(LoginUser lu, DTOBranch branch) throws UnirestException {
		HttpResponse<DTOQueue[]> asObject = Unirest
				.get(lu.getServerIPPort() + "/rest/servicepoint/branches/{branchID}/queues/")
				.routeParam("branchID", branch.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asObject(DTOQueue[].class);
		return sortAndRemove(asObject.getBody(), sortByName);
	}
	
	public void logout(LoginUser lu) throws UnirestException, IOException {
		Unirest.put(lu.getServerIPPort() + "/rest/servicepoint/logout")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();
		Unirest.shutdown();
	}
	
	public List<DTOService> getServices(LoginUser lu, DTOBranch dtoBranch) throws UnirestException {
		HttpResponse<DTOService[]> asObject = Unirest
				.get(lu.getServerIPPort() + "/rest/entrypoint/branches/{branchID}/services/")
				.routeParam("branchID", dtoBranch.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asObject(DTOService[].class);
		return sortAndRemove(asObject.getBody(), sortByName);
	}

	@SuppressWarnings("unchecked")
	private <T extends OrchestraDTO> ArrayList<T> sortAndRemove(OrchestraDTO[] obj, boolean sortByName) {
		List<T> ret = new ArrayList<T>();
		for (OrchestraDTO orchestraDTO : obj) {
			ret.add((T) orchestraDTO);
		}
		
		if (sortByName) {
			ret.sort(Comparator.comparing(OrchestraDTO::getName));
		} else {
			ret.sort((arg0, arg1) -> {
                Integer i1 = arg0.getId();
                Integer i2 = arg1.getId();
                return i1.compareTo(i2);
            });
		}

		// Remove casual called (J8 FTW)
		ret.removeIf(p -> p.getName().toLowerCase().equals("casual caller"));
		return (ArrayList<T>) ret;
		
	}

	private void logResponse(HttpResponse<JsonNode> asJson) {
		log.info(asJson.getStatus());
		log.info(asJson.getStatusText());
		log.info(asJson.getHeaders());
		log.info(asJson.getBody());
	}
}
