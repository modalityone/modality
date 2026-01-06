package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.platform.util.Numbers;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom cell renderers for the Registration Dashboard table.
 * Exact CSS values extracted from RegistrationDashboardFull.jsx.
 *
 * @author Claude Code
 */
final class RegistrationRenderers {

    // ═══════════════════════════════════════════════════════════════════════════════
    // EXACT COLOR VALUES FROM JSX (lines 1820-1838)
    // ═══════════════════════════════════════════════════════════════════════════════

    // Primary colors
    private static final Color PRIMARY = Color.web("#0d6efd");
    private static final Color PRIMARY_LIGHT = Color.web("#e7f1ff");

    // Success colors
    private static final Color SUCCESS = Color.web("#198754");
    private static final Color SUCCESS_LIGHT = Color.web("#e8f5e9");

    // Warning colors
    private static final Color WARNING = Color.web("#fd7e14");
    private static final Color WARNING_LIGHT = Color.web("#fff3cd");
    private static final Color WARNING_TEXT = Color.web("#856404");

    // Danger colors
    private static final Color DANGER = Color.web("#dc3545");
    private static final Color DANGER_LIGHT = Color.web("#f8d7da");

    // Purple colors (for child age badge)
    private static final Color PURPLE = Color.web("#7c3aed");
    private static final Color PURPLE_LIGHT = Color.web("#ede9fe");

    // Unread indicator
    private static final Color UNREAD_AMBER = Color.web("#f59e0b");

    // Text colors
    private static final Color TEXT = Color.web("#3d3530");
    private static final Color TEXT_SECONDARY = Color.web("#5c5550");
    private static final Color TEXT_MUTED = Color.web("#8a857f");

    // Gender colors
    private static final Color GENDER_FEMALE = Color.web("#ec4899");
    private static final Color GENDER_MALE = Color.web("#3b82f6");

    // Background and border
    private static final Color BG = Color.web("#faf9f5");
    private static final Color BORDER = Color.web("#e0dbd4");

    // ═══════════════════════════════════════════════════════════════════════════════
    // FONT DEFINITIONS
    // ═══════════════════════════════════════════════════════════════════════════════

    private static final Font FONT_11 = Font.font("System", 11);
    private static final Font FONT_12 = Font.font("System", 12);
    private static final Font FONT_13 = Font.font("System", 13);
    private static final Font FONT_14 = Font.font("System", 14);
    private static final Font FONT_15_BOLD = Font.font("System", FontWeight.BOLD, 15);
    private static final Font FONT_10_BOLD = Font.font("System", FontWeight.BOLD, 10);
    private static final Font FONT_12_MEDIUM = Font.font("System", FontWeight.MEDIUM, 12);
    private static final Font FONT_13_MEDIUM = Font.font("System", FontWeight.MEDIUM, 13);
    private static final Font FONT_14_MEDIUM = Font.font("System", FontWeight.MEDIUM, 14);
    private static final Font FONT_14_BOLD = Font.font("System", FontWeight.BOLD, 14);

    // Date formatters
    private static final DateTimeFormatter DATE_FORMAT_SHORT = DateTimeFormatter.ofPattern("d MMM");
    private static final DateTimeFormatter DATE_FORMAT_FULL = DateTimeFormatter.ofPattern("d MMM yyyy");

    // Reference to the view for handling actions
    private static RegistrationListView view;

    static void setView(RegistrationListView registrationListView) {
        view = registrationListView;
    }

    /**
     * Registers all custom renderers for the registration table.
     */
    static void registerRenderers() {
        registerReferenceRenderer();
        registerGuestRenderer();
        registerEventRenderer();
        registerDatesRenderer();
        registerStatusRenderer();
        registerPaymentRenderer();
        registerAccommodationRenderer();
        registerActionsRenderer();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // REFERENCE COLUMN RENDERER
    // JSX lines 2355-2375
    // ═══════════════════════════════════════════════════════════════════════════════

    private static void registerReferenceRenderer() {
        ValueRendererRegistry.registerValueRenderer("regReference", (value, context) -> {
            Document doc = (Document) value;
            VBox container = new VBox(2);
            container.setAlignment(Pos.CENTER_LEFT);

            // Reference line with unread indicator
            HBox refLine = new HBox(8);
            refLine.setAlignment(Pos.CENTER_LEFT);

            // TODO: Check if requestRead is false for unread indicator
            // For now, showing indicator for all (can be controlled by a field later)
            boolean isUnread = !Boolean.TRUE.equals(doc.isRead()); // Check read field

            if (isUnread) {
                // Unread indicator: 8px circle, #f59e0b
                Circle unreadDot = new Circle(4, UNREAD_AMBER);
                // Note: Animation not supported in WebFX, but circle shows attention needed
                refLine.getChildren().add(unreadDot);
            }

            // Reference number: 13px, fontWeight 500, color: primary
            Label refLabel = new Label(String.valueOf(doc.getRef()));
            refLabel.setFont(FONT_13_MEDIUM);
            refLabel.setTextFill(PRIMARY);
            refLine.getChildren().add(refLabel);

            // Creation date: 11px, color: textMuted
            Object creationDate = doc.getFieldValue("creationDate");
            Label dateLabel = new Label(formatCreationDate(creationDate));
            dateLabel.setFont(FONT_11);
            dateLabel.setTextFill(TEXT_MUTED);
            if (isUnread) {
                dateLabel.setPadding(new Insets(0, 0, 0, 16)); // marginLeft: 16px when unread
            }

            container.getChildren().addAll(refLine, dateLabel);
            return container;
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GUEST NAME COLUMN RENDERER
    // JSX lines 2376-2421
    // ═══════════════════════════════════════════════════════════════════════════════

    private static void registerGuestRenderer() {
        ValueRendererRegistry.registerValueRenderer("regGuest", (value, context) -> {
            Document doc = (Document) value;
            VBox container = new VBox(2);
            container.setAlignment(Pos.CENTER_LEFT);

            // Name line with gender, name, age badge, language flag
            HBox nameLine = new HBox(6);
            nameLine.setAlignment(Pos.CENTER_LEFT);

            // Gender symbol: 15px, fontWeight 600, female=#ec4899, male=#3b82f6
            Boolean isMale = doc.isMale();
            Label genderLabel = new Label(Boolean.TRUE.equals(isMale) ? "♂" : "♀");
            genderLabel.setFont(FONT_15_BOLD);
            genderLabel.setTextFill(Boolean.TRUE.equals(isMale) ? GENDER_MALE : GENDER_FEMALE);
            nameLine.getChildren().add(genderLabel);

            // Name: 14px, fontWeight 600 (unread) or 500 (read), color: text
            String fullName = doc.getFullName();
            Label nameLabel = new Label(fullName != null ? fullName : "");
            boolean isUnread = !Boolean.TRUE.equals(doc.isRead());
            nameLabel.setFont(isUnread ? FONT_14_BOLD : FONT_14_MEDIUM);
            nameLabel.setTextFill(TEXT);
            nameLine.getChildren().add(nameLabel);

            // Child age badge: 10px, fontWeight 600, color=#7c3aed, bg=#ede9fe
            // Use person_age field (stored copy in document)
            Object ageObj = doc.getFieldValue("person_age");
            if (ageObj != null) {
                int age = ageObj instanceof Number ? ((Number) ageObj).intValue() : 0;
                if (age > 0 && age < 18) {
                    Label ageBadge = createBadge(age + "y", PURPLE, PURPLE_LIGHT, 10);
                    nameLine.getChildren().add(ageBadge);
                }
            }

            // Language flag: 12px (only for non-English speakers)
            Object langObj = doc.getFieldValue("person_lang");
            if (langObj != null) {
                String lang = langObj.toString();
                if (!"en".equals(lang)) {
                    String flag = getLanguageFlag(lang);
                    if (flag != null) {
                        Label flagLabel = new Label(flag);
                        flagLabel.setFont(FONT_12);
                        nameLine.getChildren().add(flagLabel);
                    }
                }
            }

            // Email: 12px, color: textMuted
            Object emailObj = doc.getFieldValue("person_email");
            Label emailLabel = new Label(emailObj != null ? emailObj.toString() : "");
            emailLabel.setFont(FONT_12);
            emailLabel.setTextFill(TEXT_MUTED);

            container.getChildren().addAll(nameLine, emailLabel);
            return container;
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // EVENT COLUMN RENDERER
    // JSX lines 2422-2424
    // ═══════════════════════════════════════════════════════════════════════════════

    private static void registerEventRenderer() {
        ValueRendererRegistry.registerValueRenderer("regEvent", (value, context) -> {
            // Value is the event name string from expression 'event.name'
            String eventName = value != null ? value.toString() : "";

            // Badge: padding 4px 10px, borderRadius 6px, 12px, fontWeight 500
            // bg: #faf9f5, color: #5c5550, border: #e0dbd4
            Label badge = new Label(eventName);
            badge.setFont(FONT_12_MEDIUM);
            badge.setTextFill(TEXT_SECONDARY);
            badge.setPadding(new Insets(4, 10, 4, 10));
            badge.setBackground(new Background(new BackgroundFill(BG, new CornerRadii(6), null)));
            badge.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID, new CornerRadii(6), BorderWidths.DEFAULT)));

            return badge;
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DATES COLUMN RENDERER
    // JSX lines 2440-2456 - displays arrival → departure dates
    // ═══════════════════════════════════════════════════════════════════════════════

    private static void registerDatesRenderer() {
        ValueRendererRegistry.registerValueRenderer("regDates", (value, context) -> {
            Document doc = (Document) value;
            VBox container = new VBox(2);
            container.setAlignment(Pos.CENTER_LEFT);

            // Date range line with calendar icon
            HBox dateLine = new HBox(4);
            dateLine.setAlignment(Pos.CENTER_LEFT);

            // Calendar icon: 📅 12px, color: textMuted
            Label calIcon = new Label("📅");
            calIcon.setFont(FONT_12);
            calIcon.setTextFill(TEXT_MUTED);
            dateLine.getChildren().add(calIcon);

            // Parse attendance dates from document's dates field
            // Format from compute_dates SQL function: "DD/MM" or "DD/MM-DD/MM" or "DD/MM,DD/MM-DD/MM"
            // Examples: "15/03", "15/03-22/03", "15/03,17/03-22/03"
            String datesStr = doc.getDates();
            LocalDate firstAttendanceDay = null;
            LocalDate lastAttendanceDay = null;

            // Get the year from the event to complete the dates
            Event event = doc.getEvent();
            int year = event != null && event.getStartDate() != null
                ? event.getStartDate().getYear()
                : LocalDate.now().getYear();

            if (datesStr != null && !datesStr.trim().isEmpty()) {
                // Parse the first date (before any comma or dash)
                // and the last date (after the last dash, or last comma segment's last dash)
                String firstPart = datesStr.split(",")[0].split("-")[0].trim();
                String[] segments = datesStr.split(",");
                String lastSegment = segments[segments.length - 1].trim();
                String lastPart = lastSegment.contains("-")
                    ? lastSegment.split("-")[1].trim()
                    : lastSegment.split("-")[0].trim();

                firstAttendanceDay = parseDDMMDate(firstPart, year);
                lastAttendanceDay = parseDDMMDate(lastPart, year);
            }

            String dateRange = "";
            String yearText = "";

            if (firstAttendanceDay != null && lastAttendanceDay != null) {
                // Format: "15 Mar → 22 Mar"
                dateRange = firstAttendanceDay.format(DATE_FORMAT_SHORT) + " → " + lastAttendanceDay.format(DATE_FORMAT_SHORT);

                // Year display: same year shows one year, different years shows range
                int firstYear = firstAttendanceDay.getYear();
                int lastYear = lastAttendanceDay.getYear();
                yearText = firstYear == lastYear
                    ? String.valueOf(firstYear)
                    : firstYear + " → " + lastYear;
            } else if (firstAttendanceDay != null) {
                // Single day attendance
                dateRange = firstAttendanceDay.format(DATE_FORMAT_SHORT);
                yearText = String.valueOf(firstAttendanceDay.getYear());
            }

            // Date range: 13px, color: textSecondary
            Label dateLabel = new Label(dateRange);
            dateLabel.setFont(FONT_13);
            dateLabel.setTextFill(TEXT_SECONDARY);
            dateLine.getChildren().add(dateLabel);

            // Year: 11px, color: textMuted, marginLeft: 16px
            Label yearLabel = new Label(yearText);
            yearLabel.setFont(FONT_11);
            yearLabel.setTextFill(TEXT_MUTED);
            yearLabel.setPadding(new Insets(0, 0, 0, 16));

            container.getChildren().addAll(dateLine, yearLabel);
            return container;
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // STATUS COLUMN RENDERER
    // JSX lines 2445-2449
    // ═══════════════════════════════════════════════════════════════════════════════

    private static void registerStatusRenderer() {
        ValueRendererRegistry.registerValueRenderer("regStatus", (value, context) -> {
            Document doc = (Document) value;

            // Badge: padding 4px 12px, borderRadius 6px, 12px, fontWeight 500
            Label badge;

            if (Boolean.TRUE.equals(doc.isCancelled())) {
                // Cancelled: bg=#f8d7da, color=#dc3545
                badge = createBadge("Cancelled", DANGER, DANGER_LIGHT, 12);
            } else if (Boolean.TRUE.equals(doc.isConfirmed())) {
                // Confirmed: bg=#e8f5e9, color=#198754
                badge = createBadge("Confirmed", SUCCESS, SUCCESS_LIGHT, 12);
            } else {
                // Pending: bg=#fff3cd, color=#856404
                badge = createBadge("Pending", WARNING_TEXT, WARNING_LIGHT, 12);
            }

            HBox container = new HBox(badge);
            container.setAlignment(Pos.CENTER);
            return container;
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // PAYMENT COLUMN RENDERER
    // JSX lines 2450-2455
    // ═══════════════════════════════════════════════════════════════════════════════

    private static void registerPaymentRenderer() {
        ValueRendererRegistry.registerValueRenderer("regPayment", (value, context) -> {
            Document doc = (Document) value;
            VBox container = new VBox(4);
            container.setAlignment(Pos.CENTER);

            // Calculate amounts
            int deposit = Numbers.toInteger(doc.getPriceDeposit());
            int total = Numbers.toInteger(doc.getPriceNet());
            int paidAmount = deposit; // priceDeposit represents paid amount
            int balance = total - paidAmount;

            // Badge: padding 4px 12px, borderRadius 6px, 12px, fontWeight 500
            Label badge;
            if (balance <= 0) {
                // Paid: bg=#e8f5e9, color=#198754
                badge = createBadge("Paid", SUCCESS, SUCCESS_LIGHT, 12);
            } else if (paidAmount > 0) {
                // Partial: bg=#fff3cd, color=#856404
                badge = createBadge("Partial", WARNING_TEXT, WARNING_LIGHT, 12);
            } else {
                // Unpaid: bg=#f8d7da, color=#dc3545
                badge = createBadge("Unpaid", DANGER, DANGER_LIGHT, 12);
            }

            // Amount: 12px, color: textMuted - format using EventPriceFormatter (converts cents to currency)
            Event event = doc.getEvent();
            String paidFormatted = EventPriceFormatter.formatWithCurrency(paidAmount, event);
            String totalFormatted = EventPriceFormatter.formatWithCurrency(total, event);
            Label amountLabel = new Label(paidFormatted + " / " + totalFormatted);
            amountLabel.setFont(FONT_12);
            amountLabel.setTextFill(TEXT_MUTED);

            container.getChildren().addAll(badge, amountLabel);
            return container;
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // ACCOMMODATION COLUMN RENDERER
    // JSX lines 2456-2484
    // ═══════════════════════════════════════════════════════════════════════════════

    private static void registerAccommodationRenderer() {
        ValueRendererRegistry.registerValueRenderer("regAccommodation", (value, context) -> {
            Document doc = (Document) value;
            VBox container = new VBox(2);
            container.setAlignment(Pos.CENTER_LEFT);

            // Get accommodation info from view's batch-loaded lookup map (O(1) access)
            // Use getPrimaryKey() for reliable HashMap lookup (EntityId objects don't match across queries)
            Object docPk = doc.getPrimaryKey();
            RegistrationListView.AccommodationInfo accoInfo = view != null ?
                view.getAccommodationInfo(docPk) : null;

            // Accommodation type line
            HBox typeLine = new HBox(6);
            typeLine.setAlignment(Pos.CENTER_LEFT);

            // Bed icon: 14px
            Label bedIcon = new Label("🛏️");
            bedIcon.setFont(FONT_14);
            typeLine.getChildren().add(bedIcon);

            // Accommodation type: 13px, fontWeight 500, color: text or muted if none
            String typeText = accoInfo != null && accoInfo.itemName() != null ?
                accoInfo.itemName() : "No accommodation";
            Label typeLabel = new Label(typeText);
            typeLabel.setFont(FONT_13_MEDIUM);
            typeLabel.setTextFill(accoInfo != null && accoInfo.itemName() != null ? TEXT : TEXT_MUTED);
            typeLine.getChildren().add(typeLabel);

            container.getChildren().add(typeLine);

            // Room allocation line (if available)
            String roomName = accoInfo != null ? accoInfo.roomName() : null;
            if (roomName != null && !roomName.isEmpty()) {
                HBox roomLine = new HBox(5);
                roomLine.setAlignment(Pos.CENTER_LEFT);

                // Arrow: 11px, color: textMuted
                Label arrowLabel = new Label("→");
                arrowLabel.setFont(FONT_11);
                arrowLabel.setTextFill(TEXT_MUTED);
                roomLine.getChildren().add(arrowLabel);

                // Room name: 12px, color: textMuted
                Label roomLabel = new Label(roomName);
                roomLabel.setFont(FONT_12);
                roomLabel.setTextFill(TEXT_MUTED);
                roomLine.getChildren().add(roomLabel);

                // Allocation method badge: 9px, discrete styling
                boolean autoAllocated = accoInfo.systemAllocated();
                Label methodBadge = new Label(autoAllocated ? "(auto)" : "(manual)");
                methodBadge.setFont(Font.font("System", FontWeight.MEDIUM, 9));
                methodBadge.setTextFill(autoAllocated ? TEXT_MUTED : WARNING); // amber for manual
                roomLine.getChildren().add(methodBadge);

                container.getChildren().add(roomLine);
            }

            // User request/comment indicator (check document.request field)
            String request = doc.getRequest();
            if (request != null && !request.trim().isEmpty()) {
                HBox requestLine = new HBox(4);
                requestLine.setAlignment(Pos.CENTER_LEFT);

                Label alertIcon = new Label("⚠️");
                alertIcon.setFont(FONT_12);
                requestLine.getChildren().add(alertIcon);

                Label requestLabel = new Label("Special request");
                requestLabel.setFont(FONT_11);
                requestLabel.setTextFill(WARNING);
                requestLine.getChildren().add(requestLabel);

                container.getChildren().add(requestLine);
            }

            return container;
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // ACTIONS COLUMN RENDERER
    // JSX lines 2485-2497
    // ═══════════════════════════════════════════════════════════════════════════════

    private static void registerActionsRenderer() {
        ValueRendererRegistry.registerValueRenderer("regActions", (value, context) -> {
            Document doc = (Document) value;

            // Button: padding 6px 12px, borderRadius 6px, border: 1px solid #e0dbd4
            // bg: #faf9f5, color: #5c5550
            Button viewBtn = new Button("👁 View");
            viewBtn.setFont(FONT_12);
            viewBtn.setTextFill(TEXT_SECONDARY);
            viewBtn.setPadding(new Insets(6, 12, 6, 12));
            viewBtn.setBackground(new Background(new BackgroundFill(BG, new CornerRadii(6), null)));
            viewBtn.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID, new CornerRadii(6), BorderWidths.DEFAULT)));

            viewBtn.setOnAction(e -> {
                if (view != null) {
                    view.openEditModal(doc);
                }
            });

            HBox container = new HBox(viewBtn);
            container.setAlignment(Pos.CENTER);
            return container;
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Creates a styled badge label.
     */
    private static Label createBadge(String text, Color textColor, Color bgColor, double fontSize) {
        Label badge = new Label(text);
        badge.setFont(Font.font("System", FontWeight.MEDIUM, fontSize));
        badge.setTextFill(textColor);
        badge.setPadding(new Insets(4, 12, 4, 12));
        badge.setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(6), null)));
        return badge;
    }

    /**
     * Formats creation date for display.
     */
    private static String formatCreationDate(Object dateObj) {
        if (dateObj == null) return "";
        if (dateObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateObj).format(DATE_FORMAT_FULL);
        }
        if (dateObj instanceof LocalDate) {
            return ((LocalDate) dateObj).format(DATE_FORMAT_FULL);
        }
        return dateObj.toString();
    }

    /**
     * Converts a field value to LocalDate.
     */
    private static LocalDate getLocalDateFromField(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        }
        // Try to parse string
        try {
            return LocalDate.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses a date in DD/MM format with the given year.
     * Format: "15/03" -> LocalDate(year, 3, 15)
     */
    private static LocalDate parseDDMMDate(String ddmm, int year) {
        if (ddmm == null || ddmm.isEmpty()) return null;
        try {
            String[] parts = ddmm.split("/");
            if (parts.length == 2) {
                int day = Integer.parseInt(parts[0].trim());
                int month = Integer.parseInt(parts[1].trim());
                return LocalDate.of(year, month, day);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return null;
    }

    /**
     * Gets flag emoji for language code.
     * JSX lines 1686-1702
     */
    private static String getLanguageFlag(String langCode) {
        if (langCode == null) return null;
        return switch (langCode.toLowerCase()) {
            case "en" -> "🇬🇧";
            case "fr" -> "🇫🇷";
            case "de" -> "🇩🇪";
            case "es" -> "🇪🇸";
            case "it" -> "🇮🇹";
            case "pt" -> "🇵🇹";
            case "nl" -> "🇳🇱";
            case "pl" -> "🇵🇱";
            case "ru" -> "🇷🇺";
            case "zh" -> "🇨🇳";
            case "ja" -> "🇯🇵";
            case "ko" -> "🇰🇷";
            default -> null;
        };
    }
}
