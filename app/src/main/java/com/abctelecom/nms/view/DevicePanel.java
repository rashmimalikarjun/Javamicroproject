package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.model.Device;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.SubscriberService;
import com.abctelecom.nms.service.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Screen 6: Device Management (CRUD via SubscriberService device ops). */
public class DevicePanel extends CrudPanel {
    private final SubscriberService svc = new SubscriberService();

    public DevicePanel() {
        super("Device Management");
        setColumns("Device ID", "Subscriber", "Type", "Signal dBm", "Cell", "Status");
        addField("Device ID");
        addField("Subscriber ID");
        addCombo("Device Type", new ArrayList<>(Validators.DEVICE_TYPES));
        addField("Signal dBm");
        addField("Connected Cell");
        addCombo("Status", new ArrayList<>(Validators.DEVICE_STATUS));
        setEditable(ctx.can(Capability.MANAGE_SUBSCRIBERS_DEVICES));
    }

    @Override protected List<Object[]> fetchRows() throws Exception {
        List<Object[]> out = new ArrayList<>();
        for (Device d : svc.listDevices())
            out.add(new Object[]{d.getDeviceId(), d.getSubscriberId(), d.getDeviceType(),
                    d.getSignalStrengthDbm(), d.getConnectedCell(), d.getStatus()});
        return out;
    }

    private Device fromForm(Map<String, String> v) throws QitromException {
        Device d = new Device();
        d.setDeviceId(v.get("Device ID"));
        d.setSubscriberId(v.get("Subscriber ID"));
        d.setDeviceType(v.get("Device Type"));
        d.setSignalStrengthDbm(parseInt(v.get("Signal dBm"), "Signal dBm"));
        d.setConnectedCell(v.get("Connected Cell"));
        d.setStatus(v.get("Status"));
        return d;
    }

    @Override protected void doSave(Map<String, String> v, boolean isNew) throws QitromException {
        if (isNew) svc.createDevice(fromForm(v), ctx.currentUser());
        else     svc.updateDevice(fromForm(v), ctx.currentUser());
    }

    @Override protected void doDelete(Object[] row) throws QitromException {
        svc.deleteDevice(String.valueOf(row[0]), ctx.currentUser());
    }

    @Override protected void fillForm(Object[] row) {
        fields.get("Device ID").setText(BaseStationPanel.str(row[0]));
        fields.get("Subscriber ID").setText(BaseStationPanel.str(row[1]));
        combos.get("Device Type").setSelectedItem(BaseStationPanel.str(row[2]));
        fields.get("Signal dBm").setText(BaseStationPanel.str(row[3]));
        fields.get("Connected Cell").setText(BaseStationPanel.str(row[4]));
        combos.get("Status").setSelectedItem(BaseStationPanel.str(row[5]));
    }
}
