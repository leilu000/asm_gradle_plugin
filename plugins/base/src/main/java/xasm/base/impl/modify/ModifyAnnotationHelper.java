package xasm.base.impl.modify;


import xasm.ASMUtil;
import xasm.base.inter.IAddAnnotation;

import java.util.*;

/**
 * 负责修改注解的帮助类
 */
public class ModifyAnnotationHelper {

    // 保持将要被添加的注解
    private final Map<String, Map<String, Map<String, Object>>> mPaddingAddFieldAnnotationMap = new HashMap<>();
    // 保持已经被添加的注解
    private final Map<String, List<String>> mAddedFieldAnnotationMap = new HashMap<>();
    // 保存将要被移除的注解
    private final Map<String, List<String>> mPaddingRemoveFieldAnnotationMap = new HashMap<>();

    private String mFieldName;
    private Object mVisitor;

    public ModifyAnnotationHelper() {

    }

    public void setVisitor(Object visitor, String fieldName) {
        mVisitor = visitor;
        mFieldName = fieldName;
    }

    public void visitEnd() {
        // 添加新的注解
        if (mPaddingAddFieldAnnotationMap.containsKey(mFieldName)) {
            ASMUtil.addAnnotations(mVisitor, new IAddAnnotation() {
                @Override
                public Map<String, Map<String, Object>> getAnnotationsKeyValue() {
                    return mPaddingAddFieldAnnotationMap.get(mFieldName);
                }
            }, new IAddAnnotation.OnStartAddAnnotationListener() {
                @Override
                public void onStartAddAnnotation(String annotationDescriber) {
                    // 真正添加注解的回调，此时需要把已经添加的注解存放到已经添加的集合中，防止重复添加
                    List<String> addedFieldList;
                    if (!mAddedFieldAnnotationMap.containsKey(mFieldName)) {
                        addedFieldList = new ArrayList<>();
                        mAddedFieldAnnotationMap.put(mFieldName, addedFieldList);
                    } else {
                        addedFieldList = mAddedFieldAnnotationMap.get(mFieldName);
                    }
                    addedFieldList.add(annotationDescriber);
                }
            });
        }
    }

    public void removeAnnotation(String name, String[] annotationClasses) {
        if (!mPaddingRemoveFieldAnnotationMap.containsKey(name)) {
            mPaddingRemoveFieldAnnotationMap.put(name, annotationClasses == null
                    ? null : new ArrayList<>(Arrays.asList(annotationClasses)));
        }
    }

    public void addAnnotation(String name, Map<String, Map<String, Object>> annotationMap) {
        if (annotationMap != null && annotationMap.size() > 0) {
            mPaddingAddFieldAnnotationMap.put(name, annotationMap);
        }
    }

    public boolean visitAnnotation(String descriptor, boolean visible) {
        // 判断一下新添加的注解是否存在，如果存在，则把旧的注解存放到将要删除的列表中
        // 然后再在visitEnd方法给他重新添加上
        if ((!mAddedFieldAnnotationMap.containsKey(mFieldName) || !mAddedFieldAnnotationMap.get(mFieldName).contains(descriptor))
                && mPaddingAddFieldAnnotationMap.containsKey(mFieldName)) {
            Map<String, Map<String, Object>> annoMap = mPaddingAddFieldAnnotationMap.get(mFieldName);
            if (annoMap.containsKey(descriptor)) {
                List<String> needRemoveAnnotation;
                if (!mPaddingRemoveFieldAnnotationMap.containsKey(mFieldName)) {
                    needRemoveAnnotation = new ArrayList<>();
                    mPaddingRemoveFieldAnnotationMap.put(mFieldName, needRemoveAnnotation);
                } else {
                    needRemoveAnnotation = mPaddingRemoveFieldAnnotationMap.get(mFieldName);
                }
                if (needRemoveAnnotation != null) {
                    needRemoveAnnotation.add(descriptor);
                }
                mPaddingRemoveFieldAnnotationMap.put(mFieldName, needRemoveAnnotation);
            }
        }
        // 删除属性对应的注解
        if (mPaddingRemoveFieldAnnotationMap.containsKey(mFieldName)) {
            if (ASMUtil.checkNeedRemoveAnnotation(descriptor, mPaddingRemoveFieldAnnotationMap.get(mFieldName))) {
                if (mPaddingRemoveFieldAnnotationMap.get(mFieldName).contains(descriptor)) {
                    mPaddingRemoveFieldAnnotationMap.get(mFieldName).remove(descriptor);
                    if (mPaddingRemoveFieldAnnotationMap.get(mFieldName).size() == 0) {
                        mPaddingRemoveFieldAnnotationMap.remove(mFieldName);
                    }
                }
                return false;
            }
        }
        return true;
    }

}
