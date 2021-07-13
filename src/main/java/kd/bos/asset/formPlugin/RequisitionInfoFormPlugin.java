package kd.bos.asset.formPlugin;

import kd.bos.form.plugin.*;
import kd.fi.fa.common.util.*;
import kd.fi.fa.utils.*;
import kd.bos.servicehelper.org.*;
import kd.bos.entity.datamodel.events.*;
import kd.bos.dataentity.utils.*;
import kd.bos.servicehelper.inte.*;
import kd.fi.fa.business.dao.factory.*;
import kd.bos.dataentity.entity.*;
import kd.bos.form.field.*;
import kd.bos.entity.format.*;
import java.text.*;
import kd.bos.form.field.events.*;
import kd.bos.orm.query.*;
import java.util.stream.*;
import kd.bos.list.*;
import java.util.*;
import java.util.List;

import kd.bos.entity.datamodel.*;
import kd.bos.form.events.*;
import kd.bos.form.control.*;

public class RequisitionInfoFormPlugin extends AbstractFormPlugin implements BeforeF7SelectListener
{
    private static final String OP_SAVE = "save";
    private static final String OP_SUBMIT = "submit";
    
    public void registerListener(final EventObject e) {
        final BasedataEdit f7enableperiod = (BasedataEdit)this.getControl("real_card");
        f7enableperiod.addBeforeF7SelectListener((BeforeF7SelectListener)this);
    }
    
    public void afterBindData(final EventObject e) {
        final DynamicObject reqDObject = this.getView().getModel().getDataEntity();
        if (reqDObject != null) {
            final Long pk = reqDObject.getLong("asset_apply_bill_id");
            if (pk != 0L) {
                this.showApplyList(pk);
            }
        }
        DynamicObject user = (DynamicObject)this.getModel().getValue("req_user");
        if (user == null) {
            this.getModel().setValue("req_user", (Object)ContextUtil.getUserId());
            user = (DynamicObject)this.getModel().getValue("req_user");
        }
        final Image userImage = (Image)this.getControl("picturefield");
        userImage.setUrl(user.getString("picturefield"));
        final Long creatorId = user.getLong("id");
        this.getModel().setValue("creator", (Object)creatorId);
        final Map<String, Object> dpt = FaAddHeadPicture.getDptNameByUserId(creatorId);
        this.getModel().setValue("req_department", dpt.get("dpt"));
        final Map<String, Object> depParent = (Map<String, Object>)OrgUnitServiceHelper.getCompanyfromOrg(dpt.get("dpt"));
        this.getModel().setValue("org", depParent.get("id"));
        this.getModel().setValue("assetorg", depParent.get("id"));
        this.getModel().setDataChanged(false);
    }
    
    public void propertyChanged(final PropertyChangedArgs e) {
        if (StringUtils.equals((CharSequence)e.getProperty().getName(), (CharSequence)"real_card")) {
            final DynamicObject realCard = (DynamicObject)e.getChangeSet()[0].getDataEntity().get("real_card");
            final int rowIndex = e.getChangeSet()[0].getRowIndex();
            if (realCard != null) {
                this.getModel().setValue("store_place", realCard.getDynamicObject("storeplace").getPkValue(), rowIndex);
                this.getModel().setValue("use_state", realCard.get("usestatus"), rowIndex);
            }
        }
    }
    
    private void showApplyList(final Object pk) {
        final FormatObject fobj = InteServiceHelper.getUserFormat(ContextUtil.getUserId());
        final Format format = FormatFactory.get(FormatTypes.Number).getFormat(fobj);
        final DynamicObject applyDObject = FaBaseDaoFactory.getInstance("fa_asset_apply").queryOne(pk);
        final DynamicObjectCollection entryEntity = (DynamicObjectCollection)applyDObject.get("entryentity");
        if (entryEntity != null) {
            this.getModel().deleteEntryData("apply_entry_entity");
            this.getModel().batchCreateNewEntryRow("apply_entry_entity", entryEntity.size());
            for (int i = 0; i < entryEntity.size(); ++i) {
                this.getModel().setValue("apply_asset_name", ((DynamicObject)entryEntity.get(i)).get("asset_name"), i);
                this.getModel().setValue("apply_number", (Object)format.format(((DynamicObject)entryEntity.get(i)).getBigDecimal("number")), i);
            }
            final EntryGrid entryGrid = (EntryGrid)this.getView().getControl("apply_entry_entity");
            entryGrid.getItems().stream().forEach(v -> ((FieldEdit) v).setEnable("", false, -1));
        }
    }
    
    public void beforeF7Select(final BeforeF7SelectEvent arg) {
        final String property_name = arg.getProperty().getName();
        if ("real_card".equals(property_name)) {
            final ListShowParameter enablePeriodpara = (ListShowParameter)arg.getFormShowParameter();
            final ListFilterParameter listFilterParameter = enablePeriodpara.getListFilterParameter();
            final List<QFilter> qFilters = (List<QFilter>)listFilterParameter.getQFilters();
            final IDataModel model = this.getModel();
            final DynamicObject assetOrg = (DynamicObject)model.getValue("assetorg");
            qFilters.add(new QFilter("org", "=", assetOrg.getPkValue()));
            final DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("entryentity");
            final Set<Object> cardIdSet = entryEntity.stream().map(v -> v.get("real_card.id")).filter(v -> v != null).collect((Collector<? super Object, ?, Set<Object>>)Collectors.toSet());
            qFilters.get(0).and(new QFilter("id", "not in", (Object)cardIdSet));
        }
    }
    
    public void afterDoOperation(final AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        final String operateKey = args.getOperateKey();
        if ("save".equals(operateKey) || "submit".equals(operateKey)) {
            final DynamicObject reqDObject = this.getView().getModel().getDataEntity();
            if (reqDObject != null) {
                final Long pk = reqDObject.getLong("asset_apply_bill_id");
                if (pk != 0L) {
                    this.showApplyList(pk);
                }
            }
        }
    }
}

