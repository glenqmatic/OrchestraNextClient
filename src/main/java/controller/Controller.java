package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import dto.DTOBranch;
import dto.DTOQueue;
import dto.DTOServicePoint;
import dto.DTOUserStatus;
import dto.DTOWorkProfile;
import dto.OrchestraDTO;
import utils.Props;
import views.LoginUser;

public class Controller {
	private final static Logger log = LogManager.getLogger(Controller.class);

	private final static boolean sortByName = Boolean
			.valueOf(Props.getGlobalProperty(Props.GlobalProperties.SORT_BY_NAME));

	public List<DTOBranch> getBranches(LoginUser lu) throws UnirestException {
		List<DTOBranch> ret = new ArrayList<DTOBranch>();
		HttpResponse<JsonNode> asJson = Unirest.get(lu.getServerIPPort() + "/rest/servicepoint/branches/")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();

		JSONArray json = new JSONArray(asJson.getBody().toString());
		for (int i = 0; i < json.length(); i++) {
			JSONObject object = json.getJSONObject(i);
			DTOBranch fromJson = new Gson().fromJson(object.toString(), DTOBranch.class);
			ret.add(fromJson);
		}
		sortAndRemove(ret, sortByName);
		return ret;
	}

	public List<DTOWorkProfile> getWorkProfile(LoginUser lu, DTOBranch branchId) throws UnirestException {
		List<DTOWorkProfile> ret = new ArrayList<DTOWorkProfile>();
		HttpResponse<JsonNode> asJson = Unirest
				.get(lu.getServerIPPort() + "/rest/servicepoint/branches/{branchId}/workProfiles/")
				.routeParam("branchId", branchId.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();

		JSONArray json = new JSONArray(asJson.getBody().toString());
		for (int i = 0; i < json.length(); i++) {
			JSONObject object = json.getJSONObject(i);
			DTOWorkProfile fromJson = new Gson().fromJson(object.toString(), DTOWorkProfile.class);
			ret.add(fromJson);
		}
		sortAndRemove(ret, sortByName);
		return ret;
	}

	public List<DTOServicePoint> getServicePoints(LoginUser lu, DTOBranch branchId) throws UnirestException {
		List<DTOServicePoint> ret = new ArrayList<DTOServicePoint>();
		HttpResponse<JsonNode> asJson = Unirest
				.get(lu.getServerIPPort()
						+ "/rest/servicepoint/branches/{branchID}/servicePoints/deviceTypes/SW_SERVICE_POINT")
				.routeParam("branchID", branchId.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();

		JSONArray json = new JSONArray(asJson.getBody().toString());
		for (int i = 0; i < json.length(); i++) {
			JSONObject object = json.getJSONObject(i);
			DTOServicePoint fromJson = new Gson().fromJson(object.toString(), DTOServicePoint.class);
			ret.add(fromJson);
		}
		sortAndRemove(ret, sortByName);
		return ret;

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

		log.info(asJson.getStatus());
		log.info(asJson.getStatusText());
		log.info(asJson.getHeaders());
		log.info(asJson.getBody());

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

		log.info(asJson.getStatus());
		log.info(asJson.getStatusText());
		log.info(asJson.getHeaders());
		log.info(asJson.getBody());
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

		log.info(asJson.getStatus());
		log.info(asJson.getStatusText());
		log.info(asJson.getHeaders());
		log.info(asJson.getBody());

		JSONObject object = new JSONObject(asJson.getBody());

		DTOUserStatus userStat = new Gson().fromJson(object.getJSONObject("object").toString(), DTOUserStatus.class);
		DTOUserStatus.Visit visi = new Gson().fromJson(object.getJSONObject("object").getJSONObject("visit").toString(),
				DTOUserStatus.Visit.class);

		userStat.setVisit(visi);

		return userStat;
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

		log.info(asJson.getStatus());
		log.info(asJson.getStatusText());
		log.info(asJson.getHeaders());
		log.info(asJson.getBody());

		JSONObject object = new JSONObject(asJson.getBody());

		DTOUserStatus userStat = new Gson().fromJson(object.getJSONObject("object").toString(), DTOUserStatus.class);
		DTOUserStatus.Visit visi = new Gson().fromJson(object.getJSONObject("object").getJSONObject("visit").toString(),
				DTOUserStatus.Visit.class);

		userStat.setVisit(visi);

		return userStat;
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
		log.info(asJson.getStatus());
		log.info(asJson.getStatusText());
		log.info(asJson.getHeaders());
		log.info(asJson.getBody());
	}

	public void endVisit(LoginUser lu, DTOBranch branchId, String visitId) throws UnirestException {
		HttpResponse<JsonNode> asJson = Unirest
				.put(lu.getServerIPPort() + "/rest/servicepoint/branches/{branchID}/visits/{visitId}/end/")
				.routeParam("branchID", branchId.getIdAsString())
				.routeParam("visitId", visitId)
				.header("Allow", "PUT")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();
		log.info(asJson.getStatus());
		log.info(asJson.getStatusText());
		log.info(asJson.getHeaders());
		log.info(asJson.getBody());

	}

	public List<DTOQueue> getQueueInfoForWorkprofile(LoginUser lu, DTOBranch branch, DTOWorkProfile wp)
			throws UnirestException {
		List<DTOQueue> ret = new ArrayList<DTOQueue>();
		HttpResponse<JsonNode> asJson = Unirest
				.get(lu.getServerIPPort()
						+ "/rest/servicepoint/branches/{branchID}/workProfiles/{workProfileId}/queues/")
				.routeParam("branchID", branch.getIdAsString())
				.routeParam("workProfileId", wp.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();

		JSONArray json = new JSONArray(asJson.getBody().toString());
		for (int i = 0; i < json.length(); i++) {
			JSONObject object = json.getJSONObject(i);
			DTOQueue fromJson = new Gson().fromJson(object.toString(), DTOQueue.class);
			ret.add(fromJson);
		}
		sortAndRemove(ret, sortByName);

		return ret;
	}

	public List<DTOQueue> getQueueInfo(LoginUser lu, DTOBranch branch) throws UnirestException {
		List<DTOQueue> ret = new ArrayList<DTOQueue>();
		HttpResponse<JsonNode> asJson = Unirest
				.get(lu.getServerIPPort() + "/rest/servicepoint/branches/{branchID}/queues/")
				.routeParam("branchID", branch.getIdAsString())
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();

		JSONArray json = new JSONArray(asJson.getBody().toString());
		for (int i = 0; i < json.length(); i++) {
			JSONObject object = json.getJSONObject(i);
			DTOQueue fromJson = new Gson().fromJson(object.toString(), DTOQueue.class);
			ret.add(fromJson);
		}

		sortAndRemove(ret, sortByName);

		return ret;
	}
	
	public void logout(LoginUser lu) throws UnirestException, IOException {
		Unirest.put(lu.getServerIPPort() + "/rest/servicepoint/logout")
				.basicAuth(lu.getUsername(), lu.getPassword())
				.asJson();
		Unirest.shutdown();
	}
	

	private <T extends OrchestraDTO> void sortAndRemove(List<T> ret, boolean sortByName) {
		if (sortByName) {
			ret.sort(new Comparator<T>() {
				@Override
				public int compare(T arg0, T arg1) {
					return arg0.getName().compareTo(arg1.getName());
				}
			});
		} else {
			ret.sort(new Comparator<T>() {
				@Override
				public int compare(T arg0, T arg1) {
					Integer i1 = arg0.getId();
					Integer i2 = arg1.getId();
					return i1.compareTo(i2);
				}
			});
		}

		// Remove casual called (J8 FTW)
		ret.removeIf(p -> p.getName().toLowerCase().equals("casual caller"));
	}
}
