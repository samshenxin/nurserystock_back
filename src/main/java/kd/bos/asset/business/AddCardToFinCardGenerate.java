package kd.bos.asset.business;

import kd.bos.dataentity.entity.*;
import java.util.*;
import kd.bos.entity.*;
import kd.bos.dataentity.metadata.dynamicobject.*;

public class AddCardToFinCardGenerate extends AbstractFinCardGenerate
{
    @Override
    protected List<DynamicObject> generate(final List<DynamicObject> dynamicAdd, final Map<Object, DynamicObject[]> orgAssetbooksMap, final MainEntityType finCardType, final DynamicObjectType billHeadType, final Long realCardTableId, final Map<Object, Object> finCardAndrealCard) {
        return this.getFinCardDynamicObject(dynamicAdd, orgAssetbooksMap, finCardType, billHeadType, realCardTableId, finCardAndrealCard);
    }
}
