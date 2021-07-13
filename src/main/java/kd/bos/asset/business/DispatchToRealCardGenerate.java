package kd.bos.asset.business;

import java.util.ArrayList;
import java.util.Date;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.fi.fa.business.SourceFlagEnum;
import kd.fi.fa.business.dao.factory.FaRealCardDaoFactory;
import kd.fi.fa.business.utils.FaChangeModeConfUtil;

public class DispatchToRealCardGenerate extends AbstractRealCardGenerate
{
    public DispatchToRealCardGenerate(final Object billId) {
        final DynamicObject _bill = this.loadSingle(billId, (DynamicObjectType)this.getDataEntityType());
        this.init(_bill);
    }
    
    private final void init(final DynamicObject bill) {
        this.bill = bill;
        this.realList = new ArrayList<DynamicObject>();
    }
    
    private final DynamicObject loadSingle(final Object pk, final DynamicObjectType type) {
        return BusinessDataServiceHelper.loadSingle(pk, type);
    }
    
    public final MainEntityType getDataEntityType() {
        return EntityMetadataCache.getDataEntityType(this.getEntityName());
    }
    
    public void generate() {
        final DynamicObjectCollection dispatchEntry = this.bill.getDynamicObjectCollection("dispatchentry");
        for (final DynamicObject row : dispatchEntry) {
            final DynamicObject realCard = this.getNewRealCard();
            this.setRealInfo(this.bill, row, realCard);
        }
    }
    
    private void setRealInfo(final DynamicObject obj, final DynamicObject row, final DynamicObject realCard) {
        final Object cardId = row.getDynamicObject("realcard").getPkValue();
        final DynamicObject entryCard = FaRealCardDaoFactory.getInstance().queryOne(cardId);
        this.setValToCard(realCard, "sourceflag", SourceFlagEnum.DISPATCH.name());
        this.setValToCard(realCard, "assetcat", entryCard.get("assetcat"));
        this.setValToCard(realCard, "org", obj.get("inorg"));
        this.setValToCard(realCard, "assetunit", obj.get("inassetunit"));
        this.setValToCard(realCard, "number", entryCard.get("number"));
        this.setValToCard(realCard, "assetname", entryCard.get("assetname"));
        this.setValToCard(realCard, "remark", ResManager.loadKDString("\u8d44\u4ea7\u8c03\u5165", "DispatchToRealCardGenerate_0", "fi-fa-business", new Object[0]));
        this.setValToCard(realCard, "barcode", entryCard.get("barcode"));
        this.setValToCard(realCard, "model", entryCard.get("model"));
        this.setValToCard(realCard, "assetamount", entryCard.get("assetamount"));
        this.setValToCard(realCard, "unit", entryCard.get("unit"));
        final RequestContext requestContext = RequestContext.get();
        final Long creator = Long.parseLong(requestContext.getUserId());
        final Date creatDate = new Date();
        this.setValToCard(realCard, "createtime", creatDate);
        this.setValToCard(realCard, "creator", creator);
        this.setValToCard(realCard, "realaccountdate", entryCard.getDate("realaccountdate"));
        this.setValToCard(realCard, "usedate", entryCard.getDate("usedate"));
        this.setValToCard(realCard, "usestatus", entryCard.get("usestatus"));
        this.setValToCard(realCard, "supplier", entryCard.get("supplier"));
        this.setValToCard(realCard, "sourceentryid", entryCard.getPkValue());
        final DynamicObject inAssetUnit = (DynamicObject)obj.get("inassetunit");
        this.setValToCard(realCard, "originmethod_id", FaChangeModeConfUtil.getChangeModeDefaultValue((Long)inAssetUnit.getPkValue(), "fa_dispatch_in", "fa_card_real"));
        final DynamicObject inUseDept = row.getDynamicObject("inusedept");
        if (inUseDept != null) {
            this.setValToCard(realCard, "headusedept", inUseDept.getPkValue());
        }
    }
    
    @Override
    protected String getEntityName() {
        return "fa_dispatch";
    }
}

