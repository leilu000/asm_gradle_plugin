package com.leilu.asm.gradle.libcompoment.launchframe.base;


import com.leilu.asm.gradle.libcompoment.launchframe.LaunchTaskWrapper;

import java.util.LinkedList;
import java.util.Map;

public interface ILaunchTaskSorter {

    LinkedList<LaunchTaskWrapper> sort(Map<String, LaunchTaskWrapper> map);

}
