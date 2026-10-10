package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.exception.ValidationException;
import com.abctelecom.nms.model.Role;
import com.abctelecom.nms.model.User;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.UserService;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Screen 15: User Management (NETWORK_ADMIN only; passwords never displayed/logged). */
public class UserPanel extends CrudPanel {
    private final UserService svc = new UserService();

    public UserPanel() {
        super("User Management");
        setColumns("ID", "Username", "Full Name", "Role", "Enabled", "Created");
        addField("Username");
        addField("Full Name");
        List<String> roles = new ArrayList<>();
        for (Role r : Role.values()) roles.add(r.name());
        addCombo("Role", roles);
        addField("Initial Password");
        setEditable(ctx.can(Capability.MANAGE_USERS));
        if (!ctx.can(Capability.MANAGE_USERS)) status("View-only: your role cannot manage users.");
    }

    @Override protected List<Object[]> fetchRows() throws Exception {
        List<Object[]> out = new ArrayList<>();
        for (User u : svc.list())
            out.add(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(),
                    u.getRole().name(), u.isEnabled() ? "YES" : "NO", String.valueOf(u.getCreatedAt())});
        return out;
    }

    @Override protected void doSave(Map<String, String> v, boolean isNew) throws QitromException {
        if (!isNew) throw new ValidationException(
                "Use Enable/Disable button or Reset Password button to modify existing users.");
        User u = new User();
        u.setUsername(v.get("Username"));
        u.setFullName(v.get("Full Name"));
        try { u.setRole(Role.fromString(v.get("Role"))); }
        catch (IllegalArgumentException e) { throw new ValidationException("Unknown role: " + v.get("Role")); }
        svc.createUser(u, v.get("Initial Password"), ctx.currentUser());
    }

    @Override protected void doDelete(Object[] row) throws QitromException {
        // Soft delete: disable account rather than removing audit history.
        svc.setEnabled(Integer.parseInt(String.valueOf(row[0])), false, ctx.currentUser());
    }

    @Override protected void fillForm(Object[] row) {
        fields.get("Username").setText(BaseStationPanel.str(row[1]));
        fields.get("Full Name").setText(BaseStationPanel.str(row[2]));
        combos.get("Role").setSelectedItem(BaseStationPanel.str(row[3]));
        fields.get("Initial Password").setText("");
    }

    /** Extra actions wired by MainFrame through reflection-free hook. */
    public void toggleEnabled() {
        int view = getSelectedViewRow();
        if (view < 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
        Object[] row = snapshotRow(table.convertRowIndexToModel(view));
        boolean currentlyEnabled = "YES".equals(String.valueOf(row[4]));
        int id = Integer.parseInt(String.valueOf(row[0]));
        ctx.async(() -> { svc.setEnabled(id, !currentlyEnabled, ctx.currentUser()); return true; },
                ok -> { status(currentlyEnabled ? "Disabled." : "Enabled."); reload(); },
                err -> com.abctelecom.nms.util.ErrorHelper.show(this, err));
    }

    public void resetPassword() {
        int view = getSelectedViewRow();
        if (view < 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
        Object[] row = snapshotRow(table.convertRowIndexToModel(view));
        String username = String.valueOf(row[1]);
        String np = JOptionPane.showInputDialog(this,
                "New password for " + username + " (min 8 chars, letter+digit):");
        if (np == null) return;
        ctx.async(() -> { svc.resetPassword(username, np, ctx.currentUser()); return true; },
                ok -> status("Password hash updated for " + username + " (password not logged)."),
                err -> com.abctelecom.nms.util.ErrorHelper.show(this, err));
    }

    public int getSelectedViewRow() { return table.getSelectedRow(); }
}
