// src/main/java/com/esprit/stageback/storage/WasabiProps.java
package com.esprit.stageback.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.stereotype.Component;  // <-- SUPPRIMER

@Getter @Setter
@ConfigurationProperties(prefix = "wasabi")   // <-- on garde
public class WasabiProps {
    private String bucket;
    private String region;
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private Integer presignTtl = 60;
    private boolean pathStyle = true; // + getter/setter Lombok

}
