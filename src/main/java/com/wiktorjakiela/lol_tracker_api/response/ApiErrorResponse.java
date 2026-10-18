package com.wiktorjakiela.lol_tracker_api.response;


/**
 * Określa format błędu zwracanego przez backend.
 */
public record ApiErrorResponse(
        String message
) {
}
