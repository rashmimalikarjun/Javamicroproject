package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.model.TrafficFlow;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.TrafficService;
import com.abctelecom.nms.service.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Screen 10: Traffic Monitoring (view + add/delete flows via TrafficService). */
public class TrafficPanel extends CrudPanel {
    private final TrafficService svc = new TrafficService();

    public TrafficPanel() {
        super("Traffic Monitoring");
        setColumns("Flow ID", "Source", "Destination", "BW Mbps", "Latency ms",
                   "Loss %", "Throughput Mbps", "Congestion", "Recorded At");
        addField("Flow ID");
        addField("Source Node");
        addField("Destination Node");
        addField("Bandwidth Mbps");
        addField("Latency ms");
        addField("Packet Loss %");
        addField("Throughput Mbps");
        addCombo("Congestion", new ArrayList<>(Validators.CONGESTION));
        setEditable(ctx.can(Capability.MONITOR_TRAFFIC));
    }

    @Override protected List<Object[]> fetchRows() throws Exception {
        List<Object[]> out = new ArrayList<>();
        for (TrafficFlow f : svc.list())
            out.add(new Object[]{f.getFlowId(), f.getSourceNode(), f.getDestNode(),
                    f.getBandwidthMbps(), f.getLatencyMs(), f.getPacketLossPct(),
                    f.getThroughputMbps(), f.getCongestionStatus(), String.valueOf(f.getRecordedAt())});
        return out;
    }

    private TrafficFlow fromForm(Map<String, String> v) throws QitromException {
        TrafficFlow f = new TrafficFlow();
        f.setFlowId(v.get("Flow ID"));
        f.setSourceNode(v.get("Source Node"));
        f.setDestNode(v.get("Destination Node"));
        f.setBandwidthMbps(parseDouble(v.get("Bandwidth Mbps"), "Bandwidth Mbps"));
        f.setLatencyMs(parseDouble(v.get("Latency ms"), "Latency ms"));
        f.setPacketLossPct(parseDouble(v.get("Packet Loss %"), "Packet Loss %"));
        f.setThroughputMbps(parseDouble(v.get("Throughput Mbps"), "Throughput Mbps"));
        f.setCongestionStatus(v.get("Congestion"));
        return f;
    }

    @Override protected void doSave(Map<String, String> v, boolean isNew) throws QitromException {
        if (!isNew) throw new com.abctelecom.nms.exception.ValidationException(
                "Traffic records are time-stamped measurements; only New is supported (delete old rows to correct data).");
        svc.create(fromForm(v), ctx.currentUser());
    }

    @Override protected void doDelete(Object[] row) throws QitromException {
        svc.delete(String.valueOf(row[0]), ctx.currentUser());
    }

    @Override protected void fillForm(Object[] row) {
        fields.get("Flow ID").setText(BaseStationPanel.str(row[0]));
        fields.get("Source Node").setText(BaseStationPanel.str(row[1]));
        fields.get("Destination Node").setText(BaseStationPanel.str(row[2]));
        fields.get("Bandwidth Mbps").setText(BaseStationPanel.str(row[3]));
        fields.get("Latency ms").setText(BaseStationPanel.str(row[4]));
        fields.get("Packet Loss %").setText(BaseStationPanel.str(row[5]));
        fields.get("Throughput Mbps").setText(BaseStationPanel.str(row[6]));
        combos.get("Congestion").setSelectedItem(BaseStationPanel.str(row[7]));
    }
}
