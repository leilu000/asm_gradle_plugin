package com.leilu.asm.gradle.libcompoment.launchframe.impl;


import com.leilu.asm.gradle.libcompoment.launchframe.LaunchTaskWrapper;
import com.leilu.asm.gradle.libcompoment.launchframe.base.ILaunchTaskSorter;

import java.util.*;

/**
 * 1、计算每个task的入度数，得到一个map集合
 * 2、遍历入度map集合，将所有入度数为0的元素保存到队列中
 * 3、循环队列直到队列大小为0，找到所有依赖为当前出队元素（入度为0）的元素，将该元素的入度减去1，判断是否该元素
 * 的入度等于0，则将该元素也加入队列
 * 4、判断队列的元素大小和入度数的map是否相等，如果不相等，则说明存在循环依赖
 */
public class LaunchTaskSorterImpl implements ILaunchTaskSorter {

    @Override
    public LinkedList<LaunchTaskWrapper> sort(Map<String, LaunchTaskWrapper> map) {
        // 1、计算每个task的入度表
        Map<LaunchTaskWrapper, Integer> inDegreeMap = new HashMap<>();
        for (Map.Entry<String, LaunchTaskWrapper> entry : map.entrySet()) {
            LaunchTaskWrapper task = entry.getValue();
            if (!inDegreeMap.containsKey(task)) {
                inDegreeMap.put(task, 0);
            }
            List<LaunchTaskWrapper> dependencyTasks = task.getDependencyWrapperList();
            inDegreeMap.put(task, dependencyTasks.size());
        }
        // 2、找出所有入度为0的元素，保存到队列中
        Queue<LaunchTaskWrapper> zeroInDegreeQueue = new ArrayDeque<>();
        for (Map.Entry<LaunchTaskWrapper, Integer> entry : inDegreeMap.entrySet()) {
            if (entry.getValue() == 0) {
                zeroInDegreeQueue.add(entry.getKey());
            }
        }
        // 3、将入度为0的元素加到集合中，如果queue不为0，则一直遍历，直到入度全部为0
        LinkedList<LaunchTaskWrapper> resultList = new LinkedList<>();
        while (!zeroInDegreeQueue.isEmpty()) {
            LaunchTaskWrapper key = zeroInDegreeQueue.poll();
            resultList.add(key);
            for (Map.Entry<String, LaunchTaskWrapper> entry : map.entrySet()) {
                LaunchTaskWrapper task = entry.getValue();
                List<LaunchTaskWrapper> dependencyTasks = task.getDependencyWrapperList();
                if (dependencyTasks != null) {
                    for (LaunchTaskWrapper dependencyTask : dependencyTasks) {
                        if (dependencyTask.getLaunchTaskClassName().equals(key.getLaunchTaskClassName())) {
                            int inDegree = inDegreeMap.get(task) - 1;
                            inDegreeMap.put(task, inDegree);
                            if (inDegree == 0) {
                                zeroInDegreeQueue.add(task);
                            }
                        }
                    }
                }
            }
        }
        // 4、判断resultList和inDegreeMap的size是否相同，如果不相同则说明存在互相依赖
        if (resultList.size() != inDegreeMap.size()) {
            throw new RuntimeException("There are inter dependencies between tasks !");
        }
        return resultList;
    }
}
