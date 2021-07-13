package kd.bos.asset.inventoryPlugin;

import kd.bos.list.plugin.*;
import kd.fi.fa.common.util.*;
import kd.bos.form.control.events.*;
import kd.bos.form.*;
import kd.bos.form.plugin.*;
import kd.bos.form.events.*;
import kd.bos.orm.query.*;
import java.util.*;
import kd.bos.entity.cache.*;
import kd.bos.logging.*;
/**
 * 盘点记录列表
 * @author Administrator
 *
 */
public class FaInventoryRecordList extends AbstractListPlugin
{
    private static final Log log;
    private Long userId;
    
    public FaInventoryRecordList() {
        this.userId = ContextUtil.getUserId();
    }
    
    public void registerListener(final EventObject e) {
        this.addItemClickListeners(new String[] { "scan" });
    }
    
    public void closedCallBack(final ClosedCallBackEvent e) {
        if ("scan".equals(e.getActionId())) {
            final String refreshOp = (String)e.getReturnData();
            this.getView().invokeOperation("refresh");
        }
    }
    
    public void itemClick(final ItemClickEvent evt) {
        final String itemKey;
        final String key = itemKey = evt.getItemKey();
        switch (itemKey) {
            case "scan": {
                final FormShowParameter formShowParameter = new FormShowParameter();
                formShowParameter.setFormId("fa_inventory_scan_pc");
                formShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                final CloseCallBack closeCallBack = new CloseCallBack((IFormPlugin)this, "scan");
                formShowParameter.setCloseCallBack(closeCallBack);
                this.getView().showForm(formShowParameter);
                break;
            }
        }
    }
    
    public void setFilter(final SetFilterEvent e) {
        final Map<String, Object> customParams = (Map<String, Object>)this.getView().getFormShowParameter().getCustomParams();
        final Object inventorytaskid = customParams.get("inventorytaskid");
        FaInventoryRecordList.log.info("\u6b63\u5728\u64cd\u4f5c\u7684\u76d8\u70b9\u4efb\u52a1id\uff1a" + inventorytaskid);
        final IAppCache cacheDepreCheck = AppCache.get("fa");
        cacheDepreCheck.put("faInventoryRecordInventorytaskid" + this.userId, inventorytaskid);
        if (inventorytaskid != null) {
            e.getQFilters().add(new QFilter("inventorytask_id", "=", inventorytaskid));
        }
    }
    
    static {
        log = LogFactory.getLog((Class)FaInventoryRecordList.class);
    }
}

