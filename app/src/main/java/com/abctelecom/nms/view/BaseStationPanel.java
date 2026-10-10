package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.model.BaseStation;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.BaseStationService;
import com.abctelecom.nms.service.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Screen 4: Base Station Management (validated CRUD via BaseStationService). */
public class BaseStationPanel extends CrudPanel {
    private final BaseStationService svc = new BaseStationService();

    public BaseStationPanel() {
        super("Base Station Management");
        setColumns("Station ID", "Cell Name", "Location", "Band", "Tech",
                   "Capacity (Mbps)", "Load (Mbps)", "Util %", "Power");
        addField("Station ID");
        addField("Cell Name");
        addField("Location");
        addField("Frequency Band");
        addCombo("Technology", new ArrayList<>(Validators.TECHNOLOGIES));
        addField("Capacity Mbps");
        addField("Current Load Mbps");
        addCombo("Power Status", new ArrayList<>(Validators.POWER_STATUS));
        setEditable(ctx.can(Capability.MANAGE_BASE_STATIONS));
        onTableClick(r -> {});
    }

    @Override protected List<Object[]> fetchRows() throws Exception {
        List<Object[]> out = new ArrayList<>();
        for (BaseStation b : svc.list()) {
            out.add(new Object[]{b.getStationId(), b.getCellName(), b.getLocation(),
                    b.getFrequencyBand(), b.getTechnology(), b.getCapacityMbps(),
                    b.getCurrentLoadMbps(), String.format("%.1f", b.utilizationPct()),
                    b.getPowerStatus()});
        }
        return out;
    }

    private BaseStation fromForm(Map<String, String> v) throws QitromException {
        BaseStation b = new BaseStation();
        b.setStationId(v.get("Station ID"));
        b.setCellName(v.get("Cell Name"));
        b.setLocation(v.get("Location"));
        b.setFrequencyBand(v.get("Frequency Band"));
        b.setTechnology(v.get("Technology"));
        b.setCapacityMbps(parseInt(v.get("Capacity Mbps"), "Capacity Mbps"));
        b.setCurrentLoadMbps(parseInt(v.get("Current Load Mbps"), "Current Load Mbps"));
        b.setPowerStatus(v.get("Power Status"));
        return b;
    }

    @Override protected void doSave(Map<String, String> v, boolean isNew) throws QitromException {
        if (isNew) svc.create(fromForm(v), ctx.currentUser());
        else     svc.update(fromForm(v), ctx.currentUser());
    }

    @Override protected void doDelete(Object[] row) throws QitromException {
        svc.delete(String.valueOf(row[0]), ctx.currentUser());
    }

    @Override protected void fillForm(Object[] row) {
        fields.get("Station ID").setText(str(row[0]));
        fields.get("Cell Name").setText(str(row[1]));
        fields.get("Location").setText(str(row[2]));
        fields.get("Frequency Band").setText(str(row[3]));
        combos.get("Technology").setSelectedItem(str(row[4]));
        fields.get("Capacity Mbps").setText(str(row[5]));
        fields.get("Current Load Mbps").setText(str(row[6]));
        combos.get("Power Status").setSelectedItem(str(row[8]));
    }

    static String str(Object o) { return o == null ? "" : String.valueOf(o); }
}
