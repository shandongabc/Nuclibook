package nuclibook.routes;

import nuclibook.entity_utils.PermissionUtils;
import nuclibook.entity_utils.StaffRoleUtils;
import nuclibook.models.Permission;
import nuclibook.models.StaffRole;
import nuclibook.server.HtmlRenderer;
import spark.Request;
import spark.Response;

import java.util.List;

public class StaffRolesRoute extends DefaultRoute {

	@Override
	public Object handle(Request request, Response response) throws Exception {
		prepareToHandle();

		// start renderer
		HtmlRenderer renderer = getRenderer();
		renderer.setTemplateFile("staff-roles.html");

		// get staff roles and add to renderer
		List<StaffRole> allStaffRoles = StaffRoleUtils.getAllStaffRoles(true);
		renderer.setCollection("staff-roles", allStaffRoles);

		// get permissions and add to renderer
		List<Permission> allPermissions = PermissionUtils.getAllPermissions();
		renderer.setCollection("permissions", allPermissions);

		return renderer.render();
	}
}
