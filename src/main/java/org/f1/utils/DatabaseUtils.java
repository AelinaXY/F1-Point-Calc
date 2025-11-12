package org.f1.utils;

import org.jooq.Condition;
import org.jooq.Field;

public class DatabaseUtils {

    public static <T> Condition equalOrIsNull(Field<T> fld, T val) {
        return val==null ? fld.isNull() : fld.equal(val);
    }
}
