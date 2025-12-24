package com.mobile.smartcalling;

import lombok.Data;

import java.io.Serializable;

@Data
public class GISInfo implements Serializable {

    private String cgi;

    private Double prbBelowAvg;

    private Double longitude;

    private Double latitude;

}
