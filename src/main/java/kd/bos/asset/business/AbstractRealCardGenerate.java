package kd.bos.asset.business;

import kd.fi.fa.business.cardgenerate.*;
import kd.fi.fa.business.dao.factory.*;
import kd.fi.fa.business.errorcode.*;
import kd.bos.entity.validate.*;
import kd.fi.fa.business.*;
import kd.bos.dataentity.*;
import kd.bos.servicehelper.operation.*;
import kd.bos.orm.util.*;
import kd.bos.dataentity.resource.*;
import kd.bos.dataentity.entity.*;
import kd.bos.exception.*;
import kd.bos.entity.operate.result.*;
import java.util.*;

public abstract class AbstractRealCardGenerate implements IRealCardGenerate
{
    protected DynamicObject bill;
    protected List<DynamicObject> realList;
    
    protected final DynamicObject getNewRealCard() {
        final DynamicObject realCard = FaRealCardDaoFactory.getInstance().getEmptyDynamicObject();
        this.setDefaultRealCardInfo(realCard);
        this.realList.add(realCard);
        return realCard;
    }
    
    protected void setDefaultRealCardInfo(final DynamicObject realCard) {
        final Object sourceBillNumber = this.bill.get("billno");
        if (sourceBillNumber == null) {
            throw new KDException(CardGenerateError.GET_SRC_BILLNO_ERROR, new Object[0]);
        }
        this.setValToCard(realCard, "srcbillnumber", this.bill.get("billno"));
        this.setValToCard(realCard, "srcbillid", this.bill.getPkValue());
        this.setValToCard(realCard, "srcbillentityname", this.getEntityName());
        this.setValToCard(realCard, "billstatus", BillStatus.A);
        this.setValToCard(realCard, "bizstatus", BizStatusEnum.ADD);
        this.setValToCard(realCard, "realaccountdate", new Date());
    }
    
    protected void setValToCard(final DynamicObject card, final String property, final Object value) {
        card.set(property, value);
    }
    
    protected Object[] saveRealCardList() {
        final DynamicObject[] realCards = new DynamicObject[0];
        final OperationResult result = OperationServiceHelper.executeOperate("save", "fa_card_real", (DynamicObject[])this.realList.toArray(realCards), OperateOption.create());
        if (!result.isSuccess()) {
            final List<IOperateInfo> allError = (List<IOperateInfo>)result.getAllErrorOrValidateInfo();
            final StringBuilder errorInfo = new StringBuilder();
            if (!CollectionUtils.isEmpty((Collection)allError)) {
                for (final IOperateInfo in : allError) {
                    errorInfo.append(in.getMessage());
                }
            }
            final String msg = new LocaleString(String.format("%s%s-%s", ResManager.loadKDString("\u751f\u6210\u5b9e\u7269\u5361\u7247\u5931\u8d25", "AbstractRealCardGenerate_1", "fi-fa-business", new Object[0]), result.getMessage(), errorInfo.toString())).toString();
            throw new KDBizException(msg);
        }
        final List<Object> successPkIds = (List<Object>)result.getSuccessPkIds();
        return successPkIds.toArray(new Object[successPkIds.size()]);
    }
    
    private Object[] getPKs(final DynamicObject[] realCards) {
        final Object[] pks = new Object[realCards.length];
        for (int idx = 0; idx < realCards.length; ++idx) {
            pks[idx] = realCards[idx].getPkValue();
        }
        return pks;
    }
    
    @Override
    public final Object[] generateRealCard() {
        this.generate();
        return this.saveRealCardList();
    }
    
    abstract void generate();
    
    protected abstract String getEntityName();
}

