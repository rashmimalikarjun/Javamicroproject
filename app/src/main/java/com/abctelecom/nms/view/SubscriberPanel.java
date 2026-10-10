package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.model.Subscriber;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.SubscriberService;
import com.abctelecom.nms.service.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Screen 5: Subscriber Management (CRUD via SubscriberService). */
public class SubscriberPanel extends CrudPanel {
    private final SubscriberService svc = new SubscriberService();

    public SubscriberPanel() {
        super("Subscriber Management");
        setColumns("Subscriber ID", "Name", "Plan", "QoS", "Connected Cell", "Data (MB)");
        addField("Subscriber ID");
        addField("Name");
        addCombo("Service Plan", new ArrayList<>(Validators.SERVICE_PLANS));
        addField("QoS Level (1-5)");
        addField("Connected Cell");
        addField("Data Usage MB");
        setEditable(ctx.can(Capability.MANAGE_SUBSCRIBERS_DEVICES));
    }

    @Override protected List<Object[]> fetchRows() throws Exception {
        List<Object[]> out = new ArrayList<>();
        for (Subscriber s : svc.listSubscribers())
            out.add(new Object[]{s.getSubscriberId(), s.getName(), s.getServicePlan(),
                    s.getQosLevel(), s.getConnectedCell(), s.getDataUsageMb()});
        return out;
    }

    private Subscriber fromForm(Map<String, String> v) throws QitromException {
        Subscriber s = new Subscriber();
        s.setSubscriberId(v.get("Subscriber ID"));
        s.setName(v.get("Name"));
        s.setServicePlan(v.get("Service Plan"));
        s.setQosLevel(parseInt(v.get("QoS Level (1-5)"), "QoS Level"));
        s.setConnectedCell(v.get("Connected Cell"));
        s.setDataUsageMb(parseInt(v.get("Data Usage MB"), "Data Usage MB"));
        return s;
    }

    @Override protected void doSave(Map<String, String> v, boolean isNew) throws QitromException {
        if (isNew) svc.createSubscriber(fromForm(v), ctx.currentUser());
        else     svc.updateSubscriber(fromForm(v), ctx.currentUser());
    }

    @Override protected void doDelete(Object[] row) throws QitromException {
        svc.deleteSubscriber(String.valueOf(row[0]), ctx.currentUser());
    }

    @Override protected void fillForm(Object[] row) {
        fields.get("Subscriber ID").setText(BaseStationPanel.str(row[0]));
        fields.get("Name").setText(BaseStationPanel.str(row[1]));
        combos.get("Service Plan").setSelectedItem(BaseStationPanel.str(row[2]));
        fields.get("QoS Level (1-5)").setText(BaseStationPanel.str(row[3]));
        fields.get("Connected Cell").setText(BaseStationPanel.str(row[4]));
        fields.get("Data Usage MB").setText(BaseStationPanel.str(row[5]));
    }
}
