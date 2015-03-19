package nuclibook.routes;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import nuclibook.constants.P;
import nuclibook.entity_utils.AbstractEntityUtils;
import nuclibook.entity_utils.ActionLogger;
import nuclibook.entity_utils.SecurityUtils;
import nuclibook.models.ActionLog;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;

public class AjaxActionLogDataRoute extends DefaultRoute {

	@Override
	public Object handle(Request request, Response response) throws Exception {
		// necessary prelim routine
		prepareToHandle();

		// security check
		if (!SecurityUtils.requirePermission(P.VIEW_ACTION_LOG, response)) {
			ActionLogger.logAction(ActionLogger.ATTEMPT_VIEW_ACTION_LOG, 0, "Failed as user does not have permissions for this action");
			return null;
		}

		// log
		ActionLogger.logAction(ActionLogger.VIEW_ACTION_LOG, 0);

		// get request info
		int start = Integer.parseInt(request.queryParams("start"));
		int length = Integer.parseInt(request.queryParams("length"));
		String search = "%" + request.queryParams("search[value]").toLowerCase() + "%";
		int orderCol = Integer.parseInt(request.queryParams("order[0][column]"));
		String orderDir = request.queryParams("order[0][dir]");

		// prepare query string
		// TODO
		String whereQuery = "LOWER(`note`) LIKE ?";

		// prepare order string
		String orderQuery = "ORDER BY ";
		switch (orderCol) {
			// TODO

			default:
				orderQuery += "`when`";
				break;
		}
		if (orderDir.equals("asc")) {
			orderQuery += " ASC";
		} else {
			orderQuery += " DESC";
		}

		// get action DAO
		Dao<ActionLog, Integer> dao = AbstractEntityUtils.acquireDao(ActionLog.class);

		// query to get ALL results
		GenericRawResults<String[]> rawTotalResults = dao.queryRaw("SELECT COUNT(*) FROM `action_log`");
		List<String[]> totalResults = rawTotalResults.getResults();
		int totalRecords = Integer.parseInt((totalResults.get(0))[0]);

		// query to get ALL filtered results
		rawTotalResults = dao.queryRaw("SELECT COUNT(*) FROM `action_log` WHERE " + whereQuery, search);
		totalResults = rawTotalResults.getResults();
		int totalFilteredRecords = Integer.parseInt((totalResults.get(0))[0]);

		// query for matched rows
		ArrayList<String[]> records = new ArrayList<>();
		GenericRawResults<ActionLog> rawResults = dao.queryRaw("SELECT * FROM `action_log` WHERE " + whereQuery + " " + orderQuery + " LIMIT " + start + ", " + length, dao.getRawRowMapper(), search);
		List<ActionLog> results = rawResults.getResults();

		// create rows
		for (ActionLog a : results) {
			records.add(new String[]{
					a.getStaff() == null ?
							"Unknown" :
							a.getStaff().getName(),
					a.getWhen().toString("YYYY-MM-dd HH:mm:ss"),
					ActionLogger.actionDescription.get(a.getAction()) == null ?
							"Unknown" :
							ActionLogger.actionDescription.get(a.getAction()).toString(),
					a.getAssociatedId().toString(),
					a.getNote()
			});
		}

		// output
		StringBuilder output = new StringBuilder();
		output.append("{\"recordsTotal\":").append(totalRecords).append(",\"recordsFiltered\":").append(totalFilteredRecords).append(",\"data\":[");
		boolean commaNeeded = false, innerCommaNeeded;
		for (String[] r : records) {
			if (commaNeeded) output.append(",");
			commaNeeded = true;

			output.append("[");
			innerCommaNeeded = false;
			for (String s : r) {
				if (innerCommaNeeded) output.append(",");
				innerCommaNeeded = true;

				output.append("\"").append(s.replace("\"", "\\\"").replace("/", "\\/")).append("\"");
			}
			output.append("]");
		}
		output.append("]}");

		return output.toString();
	}
}
