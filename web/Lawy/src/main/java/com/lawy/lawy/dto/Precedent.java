package com.lawy.lawy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Precedent {
    private int imprisonment;
    private int probation;
    private int fine;
    private String judgementDecision;
    private String[] similarPrecedents;
}