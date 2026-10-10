package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.model.NetworkSlice;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.SliceService;
import com.abctelecom.nms.service.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Screen 7: Network Slice Management (CRUD via SliceService). */
public class SlicePanel extends CrudPanel {
    private final SliceService svc = new SliceService();

    public SlicePanel() {
        super("Network Slice Management");
        setColumns("Slice ID", "Type", "BW (Mbps)", "Priority", "Latency (ms)", "Alloc %");
        addField("Slice ID");
        addCombo("Slice Type", new ArrayList<>(Validators.SLICE_TYPES));
        addField("Bandwidth Mbps");
        addField("Priority (1-5)");
        addField("Latency Req ms");
        addField("Resource Alloc %");
        setEditable(ctx.can(Capability.MANAGE_SLICES));
    }

    @Override protected List<Object[]> fetchRows() throws Exception {
        List<Object[]> out = new ArrayList<>();
        for (NetworkSlice s : svc.list())
            out.add(new Object[]{s.getSliceId(), s.getSliceType(), s.getBandwidthMbps(),
                    s.getPriority(), s.getLatencyRequirementMs(), s.getResourceAllocationPct()});
        return out;
    }

    private NetworkSlice fromForm(Map<String, String> v) throws QitromException {
        NetworkSlice s = new NetworkSlice();
        s.setSliceId(v.get("Slice ID"));
        s.setSliceType(v.get("Slice Type"));
        s.setBandwidthMbps(parseInt(v.get("Bandwidth Mbps"), "Bandwidth Mbps"));
        s.setPriority(parseInt(v.get("Priority (1-5)"), "Priority"));
        s.setLatencyRequirementMs(parseDouble(v.get("Latency Req ms"), "Latency Requirement"));
        s.setResourceAllocationPct(parseDouble(v.get("Resource Alloc %"), "Resource Allocation %"));
        return s;
    }

    @Override protected void doSave(Map<String, String> v, boolean isNew) throws QitromException {
        if (isNew) svc.create(fromForm(v), ctx.currentUser());
        else     svc.update(fromForm(v), ctx.currentUser());
    }

    @Override protected void doDelete(Object[] row) throws QitromException {
        svc.delete(String.valueOf(row[0]), ctx.currentUser());
    }

    @Override protected void fillForm(Object[] row) {
        fields.get("Slice ID").setText(BaseStationPanel.str(row[0]));
        combos.get("Slice Type").setSelectedItem(BaseStationPanel.str(row[1]));
        fields.get("Bandwidth Mbps").setText(BaseStationPanel.str(row[2]));
        fields.get("Priority (1-5)").setText(BaseStationPanel.str(row[3]));
        fields.get("Latency Req ms").setText(BaseStationPanel.str(row[4]));
        fields.get("Resource Alloc %").setText(BaseStationPanel.str(row[5]));
    }
}
