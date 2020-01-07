package com.sangtang.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metric {

    public String name;
    public long timestamp;
    public Map<String, Object> fields;
    public Map<String, String> tags;
}
