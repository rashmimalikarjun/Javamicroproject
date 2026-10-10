package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.model.EdgeServer;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.EdgeServerService;
import com.abctelecom.nms.service.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Screen 8: Edge Server Dashboard (CRUD via EdgeServerService). */
public class EdgeServerPanel extends CrudPanel {
    private final EdgeServerService svc = new EdgeServerService();

    public EdgeServerPanel() {
        super("Edge Server Dashboard");
        setColumns("Server ID", "Site", "CPU %", "Mem Used/Total GB", "Stor Used/Total TB",
                   "Apps", "Edge Load %", "Status");
        addField("Server ID");
        addField("Site Location");
        addField("CPU Utilization %");
        addField("Memory Used GB");
        addField("Memory Total GB");
        addField("Storage Used TB");
        addField("Storage Total TB");
        addField("Running Apps (;)");
        addField("Edge Load %");
        addCombo("Status", new ArrayList<>(Validators.EDGE_STATUS));
        setEditable(ctx.can(Capability.MANAGE_EDGE_SERVERS));
    }

    @Override protected List<Object[]> fetchRows() throws Exception {
        List<Object[]> out = new ArrayList<>();
        for (EdgeServer e : svc.list())
            out.add(new Object[]{e.getServerId(), e.getSiteLocation(), e.getCpuUtilizationPct(),
                    e.getMemoryUsedGb() + " / " + e.getMemoryTotalGb(),
                    e.getStorageUsedTb() + " / " + e.getStorageTotalTb(),
                    e.getRunningApps(), e.getEdgeLoadPct(), e.getStatus()});
        return out;
    }

    private EdgeServer fromForm(Map<String, String> v) throws QitromException {
        EdgeServer e = new EdgeServer();
        e.setServerId(v.get("Server ID"));
        e.setSiteLocation(v.get("Site Location"));
        e.setCpuUtilizationPct(parseDouble(v.get("CPU Utilization %"), "CPU Utilization"));
        e.setMemoryUsedGb(parseDouble(v.get("Memory Used GB"), "Memory Used"));
        e.setMemoryTotalGb(parseDouble(v.get("Memory Total GB"), "Memory Total"));
        e.setStorageUsedTb(parseDouble(v.get("Storage Used TB"), "Storage Used"));
        e.setStorageTotalTb(parseDouble(v.get("Storage Total TB"), "Storage Total"));
        e.setRunningApps(v.get("Running Apps (;)"));
        e.setEdgeLoadPct(parseDouble(v.get("Edge Load %"), "Edge Load"));
        e.setStatus(v.get("Status"));
        return e;
    }

    @Override protected void doSave(Map<String, String> v, boolean isNew) throws QitromException {
        if (isNew) svc.create(fromForm(v), ctx.currentUser());
        else     svc.update(fromForm(v), ctx.currentUser());
    }

    @Override protected void doDelete(Object[] row) throws QitromException {
        svc.delete(String.valueOf(row[0]), ctx.currentUser());
    }

    @Override protected void fillForm(Object[] row) {
        fields.get("Server ID").setText(BaseStationPanel.str(row[0]));
        fields.get("Site Location").setText(BaseStationPanel.str(row[1]));
        fields.get("CPU Utilization %").setText(BaseStationPanel.str(row[2]));
        String[] mem = BaseStationPanel.str(row[3]).split(" / ");
        if (mem.length == 2) { fields.get("Memory Used GB").setText(mem[0]); fields.get("Memory Total GB").setText(mem[1]); }
        String[] sto = BaseStationPanel.str(row[4]).split(" / ");
        if (sto.length == 2) { fields.get("Storage Used TB").setText(sto[0]); fields.get("Storage Total TB").setText(sto[1]); }
        fields.get("Running Apps (;)").setText(BaseStationPanel.str(row[5]));
        fields.get("Edge Load %").setText(BaseStationPanel.str(row[6]));
        combos.get("Status").setSelectedItem(BaseStationPanel.str(row[7]));
    }
}
