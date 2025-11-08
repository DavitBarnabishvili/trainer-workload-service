package com.gym.crm.workload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of action to perform on trainer workload")
public enum ActionType {
    @Schema(description = "Add training hours to workload")
    ADD,

    @Schema(description = "Remove training hours from workload")
    DELETE
}