package com.medgenome.auth.dto;



import java.util.List;

public record TokenDetails(String username, String encodedPassword, Integer tenantId,
                           List<String> roles, List<String> allowedModules, List<String> domains,
                           long issuedTimestamp, long timeToLive) {

}
//todo - changed toekn details, check where u are using it