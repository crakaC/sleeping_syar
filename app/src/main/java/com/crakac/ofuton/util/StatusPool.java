package com.crakac.ofuton.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import twitter4j.Status;

public class StatusPool {
    private static Map<Long, Status> statusMap = new ConcurrentHashMap<>();

    public static Status put(long id, Status status) {
        return statusMap.put(id, status);
    }

    public static List<Status> search(String query) {
        List<Status> list = new ArrayList<>();
        for (Status status : statusMap.values()) {
            if (status.getText().toLowerCase().contains(query.toLowerCase())) {
                list.add(status);
            }
        }
        Collections.sort(list, new StatusComparator());
        return list;
    }

    public static Status get(long id) {
        return statusMap.get(id);
    }

    private static class StatusComparator implements Comparator<Status> {
        @Override
        public int compare(Status lhs, Status rhs) {
            return (int) (rhs.getCreatedAt().compareTo(lhs.getCreatedAt()));
        }
    }
}
