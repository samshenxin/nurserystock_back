package kd.bos.asset.utils;

import kd.fi.fa.utils.*;
import kd.bos.metadata.form.*;
import kd.bos.metadata.dao.*;
import kd.bos.dataentity.resource.*;
import kd.bos.exception.*;
import kd.bos.metadata.print.control.*;
import kd.bos.dataentity.utils.*;
import java.util.*;
import kd.bos.form.*;
import kd.bos.list.*;
import kd.bos.orm.query.*;
import kd.bos.servicehelper.*;
import kd.bos.entity.datamodel.*;
import kd.bos.dataentity.entity.*;
import java.util.regex.*;

public class FaRealCardBarCodeCheckUtil
{
    private static String getEncodeMode() {
        String encodeMode = null;
        Barcode control = null;
        final String teplateKey = "fa_card_real_printsetting";
        final String defaultTemplateId = FaUtils.getRealCardPrint(teplateKey);
        final PrintMetadata meta = (PrintMetadata)MetadataDao.readMeta(defaultTemplateId, MetaCategory.Form);
        if (meta == null) {
            throw new KDBizException(ResManager.loadKDString("\u5b9e\u7269\u5957\u6253\u6a21\u7248id\u9519\u8bef\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458", "FaRecardFilterList_0", "fi-fa-formplugin", new Object[0]));
        }
        final List<BaseControl<?>> items = (List<BaseControl<?>>)meta.getItems();
        for (final BaseControl<?> item : items) {
            if (item instanceof Barcode) {
                control = (Barcode)item;
                break;
            }
        }
        if (control != null) {
            encodeMode = control.getEncodeMode();
            if (StringUtils.isEmpty((CharSequence)encodeMode)) {
                throw new KDBizException(ResManager.loadKDString("\u5b9e\u7269\u5957\u6253\u6a21\u7248\u7f16\u7801\u914d\u7f6e\u4e3a\u7a7a\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458", "FaRecardFilterList_2", "fi-fa-formplugin", new Object[0]));
            }
        }
        return encodeMode;
    }
    
    public static String checkPrint(final IFormView view) {
        String error = null;
        final String encodeMode = getEncodeMode();
        if (encodeMode == null) {
            return null;
        }
        final String s = encodeMode;
        switch (s) {
            case "ean-13": {
                Object[] key = null;
                if (view instanceof IListView) {
                    final ListSelectedRowCollection rows = ((IListView)view).getSelectedRows();
                    key = rows.getPrimaryKeyValues();
                }
                else if (view instanceof IFormView) {
                    key = new Object[] { view.getModel().getDataEntity().getPkValue() };
                }
                final DynamicObject[] realCards = BusinessDataServiceHelper.load("fa_card_real", "id,barcode,number,assetname", new QFilter[] { new QFilter("id", "in", (Object)key) });
                error = innerCheckPrint(realCards);
                break;
            }
        }
        return error;
    }
    
    private static String innerCheckPrint(final DynamicObject[] realCards) {
        String error = null;
        final String regex = "^[0-9]{13}$";
        final Pattern p = Pattern.compile(regex);
        for (final DynamicObject realCard : realCards) {
            final String barCode = realCard.getString("barcode");
            final String number = realCard.getString("number");
            final String assetname = realCard.getString("assetname");
            final Matcher m = p.matcher(barCode);
            if (!m.matches()) {
                error = ResManager.loadKDString("\u8d44\u4ea7\u7f16\u7801\u4e3a", "FaRecardFilterList_3", "fi-fa-formplugin", new Object[0]) + number + ResManager.loadKDString("\uff0c\u8d44\u4ea7\u540d\u79f0\u4e3a\uff1a", "FaRecardFilterList_4", "fi-fa-formplugin", new Object[0]) + assetname + ResManager.loadKDString("\u7684\u5b9e\u7269\u5361\u7247\uff0c\u6761\u5f62\u7801\u4e0d\u7b26\u5408ean-13\u7684\u7801\u5236\uff0c\u8bf7\u8c03\u6574\u6761\u5f62\u7801", "FaRecardFilterList_5", "fi-fa-formplugin", new Object[0]);
                break;
            }
        }
        return error;
    }
}
