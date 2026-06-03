package com.loan_org.notification_service.shared.exception.mongo;

public class RecordNotFoundException extends RuntimeException {
    public RecordNotFoundException(String collection, String id) {
        super(
                "No record found for in collection:" + collection + " for MongoID: " + id
        );
    }
}
