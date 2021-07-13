package kd.bos.nurserystock.serviceHelper;

import kd.bos.base.BaseShowParameter;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.dlock.DLock;
import kd.bos.entity.BasedataEntityType;
import kd.bos.entity.BillEntityType;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.mvc.SessionManager;
import kd.bos.nurserystock.utils.ReflectUtils;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 单据帮助类
 */
public class ABillServiceHelper {

    /**
     * 执行单据操作
     *
     * @param operationKey 单据上操作代码
     * @param formId       单据实体编码
     * @param dataEntities 单据数据包
     * @param option       option
     * @return 操作结果
     */
    public static OperationResult executeOperate(String operationKey, String formId, DynamicObject[] dataEntities, OperateOption option) {
        return OperationServiceHelper.executeOperate(operationKey, formId, dataEntities, option);
    }

    /**
     * 执行单据操作
     *
     * @param operationKey 单据上操作代码
     * @param formId       单据实体编码
     * @param ids          单据id集合
     * @param option       option
     * @return 操作结果
     */
    public static OperationResult executeOperate(String operationKey, String formId, Object[] ids, OperateOption option) {
        return OperationServiceHelper.executeOperate(operationKey, formId, ids, option);
    }

    /**
     * 创建新增界面模型
     *
     * @param formId 单据实体编码
     * @return 界面模型
     */
    public static IFormView createAddView(String formId) {
        FormShowParameter parameter = getShowParameter(formId);
        return createViewByShowParameter(parameter);
    }

    /**
     * 通过打开参数创建view
     *
     * @param parameter 打开参数
     * @return 界面模型
     */
    public static IFormView createViewByShowParameter(FormShowParameter parameter) {
        invokeFormServiceMethod(parameter);
        return SessionManager.getCurrent().getView(parameter.getPageId());
    }

    /**
     * 创建修改界面模型
     *
     * @param formId 单据实体编码
     * @param id     id
     * @return 界面模型
     */
    public static IFormView createModifyView(String formId, String id) {
        FormShowParameter parameter = getModifyParameter(formId, id);
        return createViewByShowParameter(parameter);
    }

    /**
     * 修改单据并创建修改锁
     *
     * @param formId 单据实体编码
     * @param id     id
     * @param view   view回调
     * @param action 操作回调
     * @return 操作结果
     */
    public static OperationResult modifyFormWithLock(String formId, String id, Consumer<IFormView> view,
                                                     Function<IFormView, OperationResult> action) {
        DLock lock = DLock.create(formId + id, "createModifyView" + formId + id);
        lock.lock();
        OperationResult result;
        try {
            IFormView modifyView = createModifyView(formId, id);
            view.accept(modifyView);
            result = action.apply(modifyView);
            exitView(modifyView);
        } finally {
            lock.unlock();
        }
        return result;
    }

    /**
     * 执行保存操作并不受到外围事务的影响
     *
     * @param view 单据视图
     * @return 操作结果
     */
    public static OperationResult saveOperateWithNoTx(IFormView view) {
        try (TXHandle ignored = TX.notSupported()) {
            return saveOperate(view);
        }
    }

    /**
     * 执行保存操作并不受到外围事务的影响
     *
     * @param view      单据视图
     * @param autoAudit 是否自动提交审核
     * @return 操作结果
     */
    public static OperationResult saveOperateWithNoTx(IFormView view, boolean autoAudit) {
        try (TXHandle ignored = TX.notSupported()) {
            return saveOperate(view, autoAudit);
        }
    }

    /**
     * 执行保存操作并且受外围事务的影响
     *
     * @param view 单据视图
     * @return 操作结果
     */
    public static OperationResult saveOperate(IFormView view) {
        OperationResult result;
        try {
            result = view.invokeOperation("save");
        } finally {
            exitView(view);
        }
        return result;
    }

    /**
     * 执行保存操作并且受外围事务的影响
     *
     * @param view      单据视图
     * @param autoAudit 是否自动提交审核
     * @return 操作结果
     */
    public static OperationResult saveOperate(IFormView view, boolean autoAudit) {
        OperationResult result = saveOperate(view);
        if (autoAudit) {
            if (result.isSuccess()) {
                IFormView modifyView = createModifyView(view.getEntityId(), result.getSuccessPkIds().get(0).toString());
                try {
                    result = modifyView.invokeOperation("submit");
                    if (result.isSuccess()) {
                        result = modifyView.invokeOperation("audit");
                    }
                } finally {
                    exitView(modifyView);
                }
            }
        }
        return result;
    }

    /**
     * 获取单据打开参数
     *
     * @param formId 单据实体编码
     * @return 打开参数
     */
    public static FormShowParameter getShowParameter(String formId) {
        FormShowParameter parameter;
        MainEntityType mainEntityType = EntityMetadataCache.getDataEntityType(formId);
        if (mainEntityType.getClass().equals(BasedataEntityType.class)) {
            parameter = new BaseShowParameter();
        } else if (mainEntityType.getClass().equals(BillEntityType.class)) {
            parameter = new BillShowParameter();
        } else {
            parameter = new FormShowParameter();
        }
        parameter.setStatus(OperationStatus.ADDNEW);
        parameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        parameter.getOpenStyle().setTargetKey("tabap");
        parameter.setFormId(formId);
        return parameter;
    }

    /**
     * 获取单据修改打开参数
     *
     * @param formId 单据实体编码
     * @param id     id
     * @return 打开参数
     */
    public static FormShowParameter getModifyParameter(String formId, String id) {
        FormShowParameter parameter = getShowParameter(formId);
        if (parameter.getClass().equals(BaseShowParameter.class)) {
            BaseShowParameter baseShowParameter = (BaseShowParameter) parameter;
            try {
                MainEntityType mainEntityType = EntityMetadataCache.getDataEntityType(formId);
                String org = mainEntityType.getMainOrg();
                String pkName = mainEntityType.getPrimaryKey().getName();
                DynamicObject object = QueryServiceHelper.queryOne(formId, org, new QFilter[]{new QFilter(pkName, QCP.equals, id)});
                String orgId = object.getString(org);
                baseShowParameter.setCustomParam("useorgId", orgId);
                baseShowParameter.setUseOrgId(object.getLong(org));
            } catch (Exception ignored) {
            }
            baseShowParameter.setPkId(id);
            baseShowParameter.setStatus(OperationStatus.EDIT);

        } else {
            BillShowParameter billShowParameter = (BillShowParameter) parameter;
            billShowParameter.setPkId(id);
            billShowParameter.setStatus(OperationStatus.EDIT);
        }
        return parameter;
    }

    /**
     * 反射执行内部方法
     *
     * @param parameter 打开参数
     */
    private static void invokeFormServiceMethod(FormShowParameter parameter) {
        ReflectUtils.invokeCosmicMethod("kd.bos.service.ServiceFactory", "FormService", "createConfig", parameter);
        ReflectUtils.invokeCosmicMethod("kd.bos.service.ServiceFactory", "FormService", "batchInvokeAction", parameter.getPageId(), "[{\"key\":\"\",\"methodName\":\"loadData\",\"args\":[],\"postData\":[]}]");
    }

    public static void exitView(IFormView view) {
        view.getModel().setDataChanged(false);
        view.close();
    }
}
