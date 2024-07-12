// File managed by WebFX (DO NOT EDIT MANUALLY)

module modality.ecommerce.document.service.buscall {

    // Direct dependencies modules
    requires modality.ecommerce.document.service;
    requires webfx.platform.ast;
    requires webfx.platform.reflect;
    requires webfx.platform.util;
    requires webfx.stack.com.bus.call;
    requires webfx.stack.com.serial;

    // Exported packages
    exports one.modality.ecommerce.document.service.buscall;
    exports one.modality.ecommerce.document.service.buscall.serial;
    exports one.modality.ecommerce.document.service.buscall.serial.book;
    exports one.modality.ecommerce.document.service.buscall.serial.gateway;
    exports one.modality.ecommerce.document.service.buscall.serial.multiplebookings;
    exports one.modality.ecommerce.document.service.buscall.serial.registration;
    exports one.modality.ecommerce.document.service.buscall.serial.registration.line;
    exports one.modality.ecommerce.document.service.buscall.serial.security;

    // Provided services
    provides dev.webfx.stack.com.bus.call.spi.BusCallEndpoint with one.modality.ecommerce.document.service.buscall.LoadPolicyMethodEndpoint, one.modality.ecommerce.document.service.buscall.LoadDocumentMethodEndpoint, one.modality.ecommerce.document.service.buscall.SubmitDocumentMethodEndpoint;
    provides dev.webfx.stack.com.serial.spi.SerialCodec with one.modality.ecommerce.document.service.buscall.serial.book.AddAttendancesEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.book.AddDocumentEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.book.AddDocumentLineEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.book.AddMoneyTransferEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.book.CancelDocumentEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.book.RemoveAttendancesEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.gateway.UpdateMoneyTransferEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.multiplebookings.CancelOtherMultipleBookingsEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.multiplebookings.GetBackCancelledMultipleBookingsDepositEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.multiplebookings.MarkNotMultipleBookingEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.multiplebookings.MergeMultipleBookingsOptionsEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.registration.line.CancelDocumentLineEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.registration.line.RemoveDocumentLineEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.registration.FlagDocumentEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.registration.MarkDocumentAsArrivedEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.registration.ConfirmDocumentEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.registration.MarkDocumentAsReadEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.registration.MarkDocumentAsWillPayEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.registration.MarkDocumentPassAsReadyEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.registration.MarkDocumentPassAsUpdatedEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.security.MarkDocumentAsKnownEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.security.MarkDocumentAsUncheckedEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.security.MarkDocumentAsUnknownEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.security.MarkDocumentAsVerifiedEventSerialCodec, one.modality.ecommerce.document.service.buscall.serial.DocumentAggregateSerialCodec, one.modality.ecommerce.document.service.buscall.serial.LoadPolicyArgumentSerialCodec, one.modality.ecommerce.document.service.buscall.serial.LoadDocumentArgumentSerialCodec, one.modality.ecommerce.document.service.buscall.serial.SubmitDocumentChangesArgumentSerialCodec, one.modality.ecommerce.document.service.buscall.serial.SubmitDocumentChangesResultSerialCodec, one.modality.ecommerce.document.service.buscall.serial.PolicyAggregateSerialCodec;

}