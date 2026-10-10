package com.abctelecom.nms.view;

import com.abctelecom.nms.exception.QitromException;
import com.abctelecom.nms.model.ResourceAllocation;
import com.abctelecom.nms.service.AccessControl.Capability;
import com.abctelecom.nms.service.AllocationService;
import com.abctelecom.nms.service.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Screen 9: Resource Allocation (capacity-validated allocate/release). */
public class AllocationPanel extends CrudPanel {
    private final AllocationService svc = new AllocationService();

    public AllocationPanel() {
        super("Resource Allocation");
        setColumns("ID", "Type", "Target Entity", "Allocated", "Unit", "Ceiling", "Alloc By", "When");
        addField("Target Entity");
        addCombo("Resource Type", new ArrayList<>(Validators.RESOURCE_TYPES));
        addField("Allocated Value");
        addField("Capacity Ceiling");
        addField("Unit (optional)");
        setEditable(ctx.can(Capability.ALLOCATE_RESOURCES));
    }

    @Override protected List<Object[]> fetchRows() throws Exception {
        List<Object[]> out = new ArrayList<>();
        for (ResourceAllocation r : svc.list())
            out.add(new Object[]{r.getAllocationId(), r.getResourceType(), r.getTargetEntity(),
                    r.getAllocatedValue(), r.getUnit(), r.getMaxValue(), r.getAllocatedBy(),
                    String.valueOf(r.getAllocatedAt())});
        return out;
    }

    @Override protected void doSave(Map<String, String> v, boolean isNew) throws QitromException {
        if (!isNew) throw new com.abctelecom.nms.exception.ValidationException(
                "Allocations are immutable; release the row and create a new allocation instead.");
        ResourceAllocation r = new ResourceAllocation();
        r.setResourceType(v.get("Resource Type"));
        r.setTargetEntity(v.get("Target Entity"));
        r.setAllocatedValue(parseDouble(v.get("Allocated Value"), "Allocated Value"));
        r.setMaxValue(parseDouble(v.get("Capacity Ceiling"), "Capacity Ceiling"));
        r.setUnit(v.get("Unit (optional)"));
        svc.allocate(r, ctx.currentUser());
    }

    @Override protected void doDelete(Object[] row) throws QitromException {
        svc.release(Integer.parseInt(String.valueOf(row[0])), ctx.currentUser());
    }

    @Override protected void fillForm(Object[] row) {
        fields.get("Target Entity").setText(BaseStationPanel.str(row[2]));
        combos.get("Resource Type").setSelectedItem(BaseStationPanel.str(row[1]));
        fields.get("Allocated Value").setText(BaseStationPanel.str(row[3]));
        fields.get("Capacity Ceiling").setText(BaseStationPanel.str(row[5]));
    }
}
