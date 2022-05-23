package xasm.base.impl.modify.bean;

import xasm.base.inter.IHook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HookMethodWithAnnotation {

    public List<String> desc;
    public IHook.OnHookMethodWithAnnotationListener listener;
    public Map<String, List<Object>> annotationMap = new HashMap<>();

    public HookMethodWithAnnotation(List<String> desc, IHook.OnHookMethodWithAnnotationListener listener) {
        this.desc = desc;
        this.listener = listener;
    }

}
